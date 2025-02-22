package com;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DemoController {
    @FXML private ImageView bigProductImage;
    @FXML private Text bigProductTitle;
    @FXML private Text bigProductSource;
    @FXML private Button bigProductPrice;
    @FXML private VBox productListContainer;

    public void initialize() {
        // Sample JSON Data
        String jsonData = SharedData.getInstance().getSelectedProductData();
        // Parse and sort products
        updateUI(parseAndSortProducts(jsonData));
    }

    private List<JSONObject> parseAndSortProducts(String jsonData) {
        JSONArray jsonArray = new JSONArray(jsonData);
        List<JSONObject> productList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            productList.add(jsonArray.getJSONObject(i));
        }
        // Sort products by price (ascending order)
        productList.sort(Comparator.comparingInt(p -> Integer.parseInt(p.getString("productPrice").replace(",", ""))));
        return productList;
    }

    private void updateUI(List<JSONObject> products) {
        if (products.isEmpty()) return;

        // Get the lowest-priced product for the big section
        JSONObject lowestPriceProduct = products.get(0);
        bigProductImage.setImage(new Image(lowestPriceProduct.getString("productImage")));
        bigProductTitle.setText(lowestPriceProduct.getString("productName"));
        bigProductSource.setText(lowestPriceProduct.getString("source"));
        bigProductPrice.setText("TK " + lowestPriceProduct.getString("productPrice"));

        // Populate additional products dynamically
        productListContainer.getChildren().clear();
        for (int i = 1; i < products.size(); i++) {
            JSONObject product = products.get(i);

            // Create a product row
            HBox productRow = new HBox(20);
            productRow.getStyleClass().add("product-row");

            // Product Image
            ImageView productImage = new ImageView(new Image(product.getString("productImage")));
            productImage.setFitHeight(80);
            productImage.setFitWidth(80);
            productImage.setPreserveRatio(true);
            addHoverEffect(productImage);

            // Product Details (Source and Title)
            VBox productDetails = new VBox(5);
            Text source = new Text(product.getString("source"));
            source.getStyleClass().add("company-name");
            Text title = new Text(product.getString("productName"));
            title.getStyleClass().add("product-title");
            productDetails.getChildren().addAll(source, title);

            // Buttons (Price and View More)
            Button priceButton = new Button("TK " + product.getString("productPrice"));
            priceButton.getStyleClass().add("price-button");
            addHoverEffect(priceButton);

            Button viewMoreButton = new Button("View More");
            viewMoreButton.getStyleClass().add("view-more-button");
            addHoverEffect(viewMoreButton);

            HBox buttons = new HBox(10, priceButton, viewMoreButton);
            buttons.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Add components to the product row
            productRow.getChildren().addAll(productImage, productDetails, buttons);
            productListContainer.getChildren().add(productRow);
        }
    }

    // Helper method to add hover effects
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
}