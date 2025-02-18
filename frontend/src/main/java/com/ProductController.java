package com;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ProductController {

    @FXML
    private GridPane gridPane; // This corresponds to the fx:id in FXML file

    public void initialize() {
        loadProducts();
    }

    private void loadProducts() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:5000/api/product/demo?query=fury ram") // Backend API URL
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONArray products = new JSONArray(responseBody);

                    Platform.runLater(() -> {
                        int columns = 5; // Number of columns per row
                        int row = 0;

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject product = products.getJSONObject(i);

                            // Extract product details
                            String title = product.getString("productName");
                            String description = product.getString("productDetails");
                            String imageUrl = product.getString("productImage");
                            String price = product.getString("productPrice"); // Extract product price

                            // Create a VBox for the product card
                            VBox productCard = new VBox(10); // Add spacing between elements
                            productCard.getStyleClass().add("product-card"); // Styling from CSS

                            // Create and set the product image
                            ImageView productImage = new ImageView();
                            productImage.setFitWidth(200); 
                            productImage.setFitHeight(150);
                            productImage.setPreserveRatio(true);
                            try {
                                productImage.setImage(new Image(imageUrl, true));
                            } catch (Exception e) {
                                System.out.println("Failed to load image: " + imageUrl);
                            }

                            // Create and set the product details (title, description, and price)
                            Label productName = new Label(title);
                            productName.getStyleClass().add("product-name");

                            Label productDescription = new Label(description);
                            productDescription.getStyleClass().add("product-description");

                            Label productPrice = new Label("Tk. " + price);
                            productPrice.getStyleClass().add("product-price"); // Add a CSS class for styling
                            
                            productCard.getChildren().addAll(productImage, productName, productDescription, productPrice);

                            // Calculate column and row indices for 5 columns
                            int col = i % columns;
                            if (col == 0 && i != 0) {
                                row++; // Move to the next row after every 5th product
                            }

                            // Add the product card to the GridPane
                            gridPane.add(productCard, col, row);
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
}