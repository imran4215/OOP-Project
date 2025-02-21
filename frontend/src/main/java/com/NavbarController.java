package com;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

public class NavbarController {

    @FXML
    private TextField searchField;

    @FXML
    private void switchToHome() throws IOException {
        App.setRoot("home");
    }

    @FXML
    private void switchToContactUs() throws IOException {
        App.setRoot("contactUs");
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }

    @FXML
    private void switchToSearchProducts() throws IOException {
    String searchQuery = searchField.getText().trim(); // Trim to remove extra spaces

    // If searchQuery is empty, show an alert and return
    if (searchQuery.isEmpty()) {
        showAlert("Search Error", "Please enter a product name to begin your search.");
        return;
    }

    // Store the search query in the shared data model
    SharedData.getInstance().setSearchQuery(searchQuery);
    App.setRoot("searchProducts");
    }

    // Helper method to show an alert
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}