package com;

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
    private GridPane gridPane; // This corresponds to the fx:id in your FXML file

    public void initialize() {
        loadProducts();
    }

    private void loadProducts() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:5000/api/product/getAllProducts") // Backend API URL
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONArray products = new JSONArray(responseBody);

                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);

                    // Extract product details
                    String title = product.getString("title");
                    String description = product.getString("description");
                    String imageUrl = product.getString("imageUrl");

                    // Create a VBox for the product card
                    VBox productCard = new VBox();
                    productCard.getStyleClass().add("product-card"); // Styling from CSS

                    // Create and set the product image
                    ImageView productImage = new ImageView();
                    productImage.setFitWidth(250);
                    productImage.setFitHeight(150);
                    productImage.setPreserveRatio(true);
                    productImage.setImage(new Image(imageUrl)); // Load image from URL

                    // Create and set the product details (title and description)
                    Label productName = new Label(title);
                    productName.getStyleClass().add("product-name");

                    Label productDescription = new Label(description);
                    productDescription.getStyleClass().add("product-description");

                    productCard.getChildren().addAll(productImage, productName, productDescription);

                    // Add the product card to the GridPane
                    gridPane.add(productCard, i % 3, i / 3); // Position in grid (3 columns)
                }
            } else {
                System.out.println("Failed to fetch products. Response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
