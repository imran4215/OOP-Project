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

import java.io.IOException;

public class DemoController {

    @FXML
    private GridPane gridPane;

    public void initialize() {
        String selectedProduct = SharedData.getInstance().getSelectedProduct();
        System.out.println("Selected Product: " + selectedProduct);

        if (selectedProduct != null && !selectedProduct.isEmpty()) {
            loadProducts(selectedProduct);
        } else {
            System.out.println("No search query found.");
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
                    JSONObject productsJson = new JSONObject(responseBody);

                    // Navigate to the "data" object inside "products"
                    JSONObject productsData = productsJson.getJSONObject("products").getJSONObject("data");

                    Platform.runLater(() -> {
                        int columns = 5; // Number of columns per row
                        int row = 0;
                        int col = 0;

                        // Iterate through each product name in the "data" object
                        for (String productName : productsData.keySet()) {
                            JSONArray productEntries = productsData.getJSONArray(productName);
                            JSONObject lowestPriceProduct = findLowestPriceProduct(productEntries);

                            if (lowestPriceProduct != null) {
                                String title = lowestPriceProduct.getString("productName");
                                String description = lowestPriceProduct.getString("productDetails");
                                String imageUrl = lowestPriceProduct.getString("productImage");
                                String price = lowestPriceProduct.getString("productPrice");

                                VBox productCard = createProductCard(title, description, imageUrl, price,
                                        productEntries);
                                gridPane.add(productCard, col, row);

                                col++;
                                if (col == columns) {
                                    col = 0;
                                    row++;
                                }
                            }
                        }
                    });
                } else {
                    System.out.println("Failed to fetch products. Response code: " + response.code());
                    System.out.println("Response Message: " + response.message());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Helper method to find the product with the lowest price
    private JSONObject findLowestPriceProduct(JSONArray productEntries) {
        JSONObject lowestPriceProduct = null;
        int lowestPrice = Integer.MAX_VALUE;

        for (int i = 0; i < productEntries.length(); i++) {
            JSONObject product = productEntries.getJSONObject(i);
            String priceString = product.getString("productPrice").replace(",", "");
            int price = Integer.parseInt(priceString);

            if (price < lowestPrice) {
                lowestPrice = price;
                lowestPriceProduct = product;
            }
        }

        return lowestPriceProduct;
    }

    // Helper method to create a product card
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

        try {
            productImage.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            System.out.println("Failed to load image: " + imageUrl);
        }

        Label productName = new Label(title);
        productName.getStyleClass().add("product-name");

        Label productDescription = new Label(description);
        productDescription.getStyleClass().add("product-description");

        Label productPrice = new Label("Tk. " + price);
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

    private void switchToProductDetails(JSONArray productEntries) throws IOException {
        SharedData.getInstance().setSelectedProductData(productEntries.toString());
        App.setRoot("productDetails");
    }

    private void addHoverEffects(VBox productCard, ImageView productImage, Button viewMoreButton) {
        ScaleTransition cardScaleTransition = new ScaleTransition(Duration.millis(200), productCard);
        cardScaleTransition.setToX(1.02);
        cardScaleTransition.setToY(1.02);

        productCard.setOnMouseEntered(event -> {
            cardScaleTransition.play();
            productCard.setStyle("-fx-background-color: #f0f8ff;");
        });

        productCard.setOnMouseExited(event -> {
            cardScaleTransition.stop();
            productCard.setScaleX(1.0);
            productCard.setScaleY(1.0);
            productCard.setStyle("-fx-background-color: #ffffff;");
        });

        ScaleTransition imageScaleTransition = new ScaleTransition(Duration.millis(400), productImage);
        imageScaleTransition.setToX(1.05);
        imageScaleTransition.setToY(1.05);

        productImage.setOnMouseEntered(event -> imageScaleTransition.play());
        productImage.setOnMouseExited(event -> {
            imageScaleTransition.stop();
            productImage.setScaleX(1.0);
            productImage.setScaleY(1.0);
        });

        ScaleTransition buttonScaleTransition = new ScaleTransition(Duration.millis(200), viewMoreButton);
        buttonScaleTransition.setToX(1.05);
        buttonScaleTransition.setToY(1.05);

        viewMoreButton.setOnMouseEntered(event -> {
            buttonScaleTransition.play();
            viewMoreButton.setStyle("-fx-background-color: #218838;");
        });

        viewMoreButton.setOnMouseExited(event -> {
            buttonScaleTransition.stop();
            viewMoreButton.setScaleX(1.0);
            viewMoreButton.setScaleY(1.0);
            viewMoreButton.setStyle("-fx-background-color: #28a745;");
        });
    }
}