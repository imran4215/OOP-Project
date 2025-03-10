package com;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryProductsController {

    @FXML
    private GridPane gridPane;

    private final ArrayList<String> navigationHistory = SharedData.getInstance().getNavigationHistory();

    public void initialize() {
        String selectedProduct = SharedData.getInstance().getSelectedProduct();
        gridPane.getChildren().clear();

        if (selectedProduct != null && !selectedProduct.trim().isEmpty()) {
            loadProducts(selectedProduct);
        } else {
            displayFallbackMessage("No product selected. Please go back and select a product.");
        }
    }

    private void loadProducts(String selectedProduct) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:5000/api/product/getProducts?query=" + selectedProduct;
        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    if (responseBody.trim().isEmpty() || responseBody.equals("{}")) {
                        displayFallbackMessage("Empty response from server.");
                        return;
                    }

                    JSONObject productsJson = new JSONObject(responseBody);
                    JSONObject productsData;

                    try {
                        if (!productsJson.has("products")) {
                            displayFallbackMessage("No 'products' key in response.");
                            return;
                        }
                        JSONObject products = productsJson.getJSONObject("products");
                        if (!products.has("data")) {
                            displayFallbackMessage("No 'data' key in products.");
                            return;
                        }
                        productsData = products.getJSONObject("data");
                    } catch (JSONException e) {
                        System.err.println("JSON parsing error: " + e.getMessage());
                        displayFallbackMessage("Invalid data format from server.");
                        return;
                    }

                    Platform.runLater(() -> {
                        gridPane.getChildren().clear();
                        int columns = 5;
                        int row = 0;
                        int col = 0;

                        if (productsData.length() == 0) {
                            displayFallbackMessage("No products found for: " + selectedProduct);
                            return;
                        }

                        for (String productName : productsData.keySet()) {
                            JSONArray productEntries = productsData.getJSONArray(productName);
                            JSONObject lowestPriceProduct = findLowestPriceProduct(productEntries);

                            if (lowestPriceProduct != null) {
                                String title = lowestPriceProduct.optString("productName", "Unnamed Product");
                                String description = lowestPriceProduct.optString("productDetails",
                                        "No details available");
                                String imageUrl = lowestPriceProduct.optString("productImage", "");
                                String price = lowestPriceProduct.optString("productPrice", "N/A");

                                VBox productCard = createProductCard(title, description, imageUrl, price,
                                        productEntries);
                                gridPane.add(productCard, col, row);

                                col++;
                                if (col >= columns) {
                                    col = 0;
                                    row++;
                                }
                            }
                        }
                    });
                } else {
                    displayFallbackMessage("Failed to load products. Server error: " + response.code());
                }
            } catch (IOException e) {
                System.err.println("IOException in loadProducts: " + e.getMessage());
                displayFallbackMessage("Error connecting to the server. Please check your connection.");
            }
        }).start();
    }

    private JSONObject findLowestPriceProduct(JSONArray productEntries) {
        JSONObject lowestPriceProduct = null;
        double lowestPrice = Double.MAX_VALUE;

        for (int i = 0; i < productEntries.length(); i++) {
            JSONObject product = productEntries.getJSONObject(i);
            String priceString = product.optString("productPrice", "").replace(",", "").trim();

            if (priceString.isEmpty()) {
                continue; // Skip products with no price
            }

            try {
                double price = Double.parseDouble(priceString);
                if (price < lowestPrice) {
                    lowestPrice = price;
                    lowestPriceProduct = product;
                }
            } catch (NumberFormatException e) {
                System.err.println(
                        "Invalid price format: " + priceString + " for product: " + product.optString("productName"));
            }
        }
        return lowestPriceProduct;
    }

    private VBox createProductCard(String title, String description, String imageUrl, String price,
            JSONArray productEntries) {
        VBox productCard = new VBox(10);
        productCard.setAlignment(Pos.CENTER);
        productCard.getStyleClass().add("product-card");

        HBox priceAndButtonContainer = new HBox(10);
        priceAndButtonContainer.setAlignment(Pos.CENTER_LEFT);

        ImageView productImage = new ImageView();
        productImage.setFitWidth(200);
        productImage.setFitHeight(150);
        productImage.setPreserveRatio(true);
        productImage.getStyleClass().add("image-view");

        if (!imageUrl.isEmpty()) {
            try {
                productImage.setImage(new Image(imageUrl, true));
            } catch (Exception e) {
                System.err.println("Failed to load image: " + imageUrl);
                productImage.setImage(new Image("https://via.placeholder.com/200x150?text=No+Image"));
            }
        } else {
            productImage.setImage(new Image("https://via.placeholder.com/200x150?text=No+Image"));
        }

        Label productName = new Label(truncateText(title, 50));
        productName.getStyleClass().add("product-name");

        Label productDescription = new Label(truncateText(description, 100));
        productDescription.getStyleClass().add("product-description");

        Label productPrice = new Label("Tk. " + (price.isEmpty() ? "N/A" : price));
        productPrice.getStyleClass().add("product-price");

        Button viewMoreButton = new Button("View More");
        viewMoreButton.getStyleClass().add("view-more-button");
        viewMoreButton.setOnAction(event -> {
            try {
                switchToProductDetails(productEntries);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        priceAndButtonContainer.getChildren().addAll(productPrice, viewMoreButton);
        productCard.getChildren().addAll(productImage, productName, productDescription, priceAndButtonContainer);

        addHoverEffects(productCard, productImage, viewMoreButton);
        return productCard;
    }

    private void displayFallbackMessage(String message) {
        Platform.runLater(() -> {
            Label noDataLabel = new Label(message);
            noDataLabel.getStyleClass().add("fallback-label");
            gridPane.add(noDataLabel, 0, 0);
        });
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    private void switchToProductDetails(JSONArray productEntries) throws IOException {
        SharedData.getInstance().setSelectedProductData(productEntries.toString());
        // Add the current page to history before switching
        addToHistory(App.getCurrentRoot());
        App.setRoot("productDetails");
    }

    private void addHoverEffects(VBox productCard, ImageView productImage, Button viewMoreButton) {
        ScaleTransition cardScale = new ScaleTransition(Duration.millis(200), productCard);
        cardScale.setToX(1.02);
        cardScale.setToY(1.02);

        productCard.setOnMouseEntered(event -> {
            cardScale.play();
            productCard.setStyle("-fx-background-color: #f0f8ff;");
        });
        productCard.setOnMouseExited(event -> {
            cardScale.stop();
            productCard.setScaleX(1.0);
            productCard.setScaleY(1.0);
            productCard.setStyle("-fx-background-color: #ffffff;");
        });

        ScaleTransition imageScale = new ScaleTransition(Duration.millis(400), productImage);
        imageScale.setToX(1.05);
        imageScale.setToY(1.05);

        productImage.setOnMouseEntered(event -> imageScale.play());
        productImage.setOnMouseExited(event -> {
            imageScale.stop();
            productImage.setScaleX(1.0);
            productImage.setScaleY(1.0);
        });

        ScaleTransition buttonScale = new ScaleTransition(Duration.millis(200), viewMoreButton);
        buttonScale.setToX(1.05);
        buttonScale.setToY(1.05);

        viewMoreButton.setOnMouseEntered(event -> {
            buttonScale.play();
            viewMoreButton.setStyle("-fx-background-color: #218838;");
        });
        viewMoreButton.setOnMouseExited(event -> {
            buttonScale.stop();
            viewMoreButton.setScaleX(1.0);
            viewMoreButton.setScaleY(1.0);
            viewMoreButton.setStyle("-fx-background-color: #28a745;");
        });
    }

    private void addToHistory(String currentPage) {
        if (currentPage != null && !currentPage.isEmpty()) {
            navigationHistory.add(currentPage);
        }
    }
}