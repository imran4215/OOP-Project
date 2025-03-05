package com;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class NavbarController {

    @FXML
    private TextField searchField;

    @FXML
    private Button loginBtn;

    @FXML
    private Button signupBtn;

    @FXML
    private Label usernameLabel;

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
    private void switchToSignUp() throws IOException {
        App.setRoot("signup");
    }

    @FXML
    private void logout() throws IOException {
        Auth.logout();
        updateNavbar();
        App.setRoot("home");
    }

    @FXML
    private void switchToUserProfile() throws IOException {
        App.setRoot("userProfile");
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

    public void initialize() {
        updateNavbar();
    }

    public void updateNavbar() {
        if (Auth.isAuthenticated()) {
            loginBtn.setVisible(false);
            signupBtn.setVisible(false);
            usernameLabel.setVisible(true);
            usernameLabel.setText(Auth.getUsername()); // Set the username label
            // System.out.println("Username Set: " + Auth.getUsername());
        } else {
            loginBtn.setVisible(true);
            signupBtn.setVisible(true);
            // userAvatar.setVisible(false);
            usernameLabel.setVisible(false);
        }
    }
}