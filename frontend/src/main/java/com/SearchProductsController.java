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

public class SearchProductsController {

    @FXML
    private GridPane gridPane;

    public void initialize() {
        String searchQuery = SharedData.getInstance().getSearchQuery();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            loadProducts(searchQuery);
        } else {
            System.out.println("No search query found.");
        }
    }

    private void loadProducts(String searchQuery) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:5000/api/product/demo?query=" + searchQuery;
        Request request = new Request.Builder().url(url).build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONArray products = new JSONArray(responseBody);

                    Platform.runLater(() -> {
                        int columns = 5;
                        int row = 0;

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject product = products.getJSONObject(i);
                            String title = product.getString("productName");
                            String description = product.getString("productDetails");
                            String imageUrl = product.getString("productImage");
                            String price = product.getString("productPrice");

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

                            priceAndButtonContainer.getChildren().addAll(productPrice, viewMoreButton);
                            productCard.getChildren().addAll(productImage, productName, productDescription, priceAndButtonContainer);

                            addHoverEffects(productCard, productImage, viewMoreButton);

                            int col = i % columns;
                            if (col == 0 && i != 0) {
                                row++;
                            }

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