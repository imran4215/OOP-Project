package com;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductDetailsController {
    @FXML
    private ImageView bigProductImage;
    @FXML
    private Text bigProductTitle;
    @FXML
    private Text bigProductSource;
    @FXML
    private Button bigProductPrice;
    @FXML
    private VBox productListContainer;
    @FXML
    private Button viewDetailsButton; // New button
    @FXML
    private Button buyNowButton;

    private final ArrayList<String> navigationHistory = SharedData.getInstance().getNavigationHistory();

    public void initialize() {
        String jsonData = SharedData.getInstance().getSelectedProductData();
        if (jsonData != null && !jsonData.isEmpty()) {
            updateUI(parseAndSortProducts(jsonData));
        } else {
            bigProductTitle.setText("No product data available");
        }

        // Set up View Details button action
        viewDetailsButton.setOnAction(event -> {
            String jsonData_ = SharedData.getInstance().getSelectedProductData();
            if (jsonData_ != null && !jsonData_.isEmpty()) {
                List<JSONObject> products = parseAndSortProducts(jsonData_);
                if (!products.isEmpty()) {
                    JSONObject topProduct = products.get(0);
                    openUrl(topProduct.optString("productDetails", ""));
                } else {
                    showAlert("No Details", "No product details URL available.");
                }
            } else {
                showAlert("No Details", "No product data available.");
            }
        });

        // Set up Buy Now button action
        buyNowButton.setOnAction(event -> {
            try {
                if (!Auth.isAuthenticated()) {
                    addToHistory(App.getCurrentRoot());
                    App.setRoot("login");
                    return;
                } else {
                    String jsonData_ = SharedData.getInstance().getSelectedProductData();
                    JSONObject lowestPriceProduct = parseAndSortProducts(jsonData_).get(0);
                    SharedData.getInstance().setSelectedProduct(lowestPriceProduct.toString());
                    addToHistory(App.getCurrentRoot());
                    App.setRoot("takeOrder");
                }
            } catch (IOException e) {
                System.err.println("Failed to redirect to takeOrder page: " + e.getMessage());
            }
        });
    }

    private List<JSONObject> parseAndSortProducts(String jsonData) {
        JSONArray jsonArray = new JSONArray(jsonData);
        List<JSONObject> productList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            productList.add(jsonArray.getJSONObject(i));
        }
        productList.sort(Comparator.comparingDouble(p -> {
            String price = p.optString("productPrice", "").replace(",", "").trim();
            try {
                return price.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(price);
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        }));
        return productList;
    }

    private void updateUI(List<JSONObject> products) {
        if (products.isEmpty()) {
            bigProductTitle.setText("No products available");
            return;
        }

        JSONObject lowestPriceProduct = products.get(0);
        bigProductImage.setImage(loadImage(lowestPriceProduct.optString("productImage", "")));
        bigProductTitle.setText(truncateText(lowestPriceProduct.optString("productName", "Unnamed Product"), 50));
        bigProductSource.setText(lowestPriceProduct.optString("source", "Unknown Source"));
        String price = lowestPriceProduct.optString("productPrice", "N/A");
        bigProductPrice.setText("Tk. " + (price.isEmpty() ? "N/A" : price));
        bigProductPrice.setOnAction(event -> openUrl(lowestPriceProduct.optString("productDetails", "")));

        productListContainer.getChildren().clear();
        productListContainer.setSpacing(10);

        for (int i = 1; i < products.size(); i++) {
            JSONObject product = products.get(i);

            HBox productRow = new HBox(15);
            productRow.getStyleClass().add("product-row");

            ImageView productImage = new ImageView(loadImage(product.optString("productImage", "")));
            productImage.setFitHeight(80);
            productImage.setFitWidth(80);
            productImage.setPreserveRatio(true);
            addHoverEffect(productImage);

            VBox productDetails = new VBox(5);
            Text source = new Text(product.optString("source", "Unknown Source"));
            source.getStyleClass().add("company-name");
            Text title = new Text(product.optString("productName", "Unnamed Product"));
            title.getStyleClass().add("product-title");
            title.setWrappingWidth(200);
            productDetails.getChildren().addAll(source, title);
            HBox.setHgrow(productDetails, Priority.ALWAYS);

            String priceStr = product.optString("productPrice", "N/A");
            String fullPrice = "Tk. " + (priceStr.isEmpty() ? "N/A" : priceStr);
            Button priceButton = new Button(fullPrice);
            priceButton.getStyleClass().add("price-button");
            priceButton.setMinWidth(100);
            priceButton.setPrefHeight(35);
            priceButton.setWrapText(true);
            addHoverEffect(priceButton);

            priceButton.setOnAction(event -> {
                try {
                    double priceValue = parsePrice(priceStr);
                    if (priceStr.equals("N/A") || priceValue == 0) {
                        showAlert("Product Unavailable", "This product is not available for purchase.");
                    } else if (!Auth.isAuthenticated()) {
                        addToHistory(App.getCurrentRoot());
                        App.setRoot("login");
                    } else {
                        SharedData.getInstance().setSelectedProduct(product.toString());
                        addToHistory(App.getCurrentRoot());
                        App.setRoot("takeOrder");
                    }
                } catch (IOException e) {
                    System.err.println("Failed to redirect to takeOrder page: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            Button viewDetailsButtonRow = new Button("View Details");
            viewDetailsButtonRow.getStyleClass().add("view-details-button");
            viewDetailsButtonRow.setMinWidth(120);
            viewDetailsButtonRow.setPrefHeight(35);
            viewDetailsButtonRow.setWrapText(true);
            viewDetailsButtonRow.setOnAction(event -> openUrl(product.optString("productDetails", "")));
            addHoverEffect(viewDetailsButtonRow);

            HBox buttons = new HBox(15, priceButton, viewDetailsButtonRow);
            buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            productRow.getChildren().addAll(productImage, productDetails, buttons);
            productListContainer.getChildren().add(productRow);
        }
    }

    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.equals("N/A") || priceStr.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(priceStr.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Image loadImage(String imageUrl) {
        if (!imageUrl.isEmpty()) {
            try {
                return new Image(imageUrl, true);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + imageUrl);
            }
        }
        return new Image("https://via.placeholder.com/80?text=No+Image");
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    private void openUrl(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.err.println("Failed to open URL: " + url);
                e.printStackTrace();
            }
        } else {
            showAlert("No URL", "No product details URL available.");
        }
    }

    private void addHoverEffect(Button button) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
        scaleTransition.setToX(1.05);
        scaleTransition.setToY(1.05);

        button.setOnMouseEntered(event -> {
            scaleTransition.play();
            button.setStyle("-fx-background-color: #1186b4; -fx-text-fill: white;");
        });
        button.setOnMouseExited(event -> {
            scaleTransition.stop();
            button.setScaleX(1.0);
            button.setScaleY(1.0);
            button.setStyle("-fx-background-color: transparent; -fx-border-color: #1186b4; -fx-text-fill: #1186b4;");
        });
    }

    private void addHoverEffect(ImageView imageView) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), imageView);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);

        imageView.setOnMouseEntered(event -> scaleTransition.play());
        imageView.setOnMouseExited(event -> {
            scaleTransition.stop();
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
        });
    }

    private void addToHistory(String currentPage) {
        if (currentPage != null && !currentPage.isEmpty()) {
            navigationHistory.add(currentPage);
        }
    }
}