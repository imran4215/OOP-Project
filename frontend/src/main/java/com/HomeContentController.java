package com;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeContentController implements Initializable {

    @FXML
    private GridPane productGrid;

    private final String[][] products = new String[][] {
            { "Processor", "/com/images/Processor.jpeg" },
            { "Motherboard", "/com/images/Motherboard.jpeg" },
            { "RAM", "/com/images/RAM.jpeg" },
            { "Laptop", "/com/images/Laptop.jpeg" },
            { "Hard Disk", "/com/images/Hard Disk.jpeg" },
            { "SSD", "/com/images/SSD.jpg" },
            { "Graphics Card", "/com/images/Graphics Card.jpeg" },
            { "Mouse", "/com/images/Mouse.jpeg" },
            { "Keyboard", "/com/images/Keyboard.jpeg" },
            { "Power Supply", "/com/images/Power Supply.jpeg" },
            { "Pendrive", "/com/images/Pendrive.jpeg" },
            { "Computer Casing", "/com/images/Computer Casing.jpeg" },
            { "CPU Cooler", "/com/images/CPU Cooler.jpg" },
            { "Webcam", "/com/images/Webcam.jpeg" },
            { "DVD Writer", "/com/images/DVD Writer.jpeg" },
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        final int columns = 5;

        // Optionally, add spacing directly on the grid if not set in FXML.
        productGrid.setHgap(20);
        productGrid.setVgap(20);

        for (String[] product : products) {
            VBox card = createProductCard(product[0], product[1]);
            productGrid.add(card, col, row);
            col++;
            if (col >= columns) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(String name, String imagePath) {
        // Create the card container
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");
        card.setMinWidth(150);
        card.setMaxWidth(180);

        // Create the image view
        ImageView imageView = new ImageView();
        try {
            URL imageUrl = getClass().getResource(imagePath);
            if (imageUrl == null) {
                System.err.println("Image not found at path: " + imagePath);
            } else {
                Image image = new Image(imageUrl.toExternalForm());
                imageView.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Could not load image: " + imagePath);
            e.printStackTrace();
        }
        imageView.setFitWidth(120);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        // Create the label for the product name
        Label label = new Label(name);
        label.getStyleClass().add("product-label");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMaxWidth(140);

        // Add children to the card
        card.getChildren().addAll(imageView, label);

        return card;
    }
}
