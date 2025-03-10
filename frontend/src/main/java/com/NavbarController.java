package com;

import java.io.IOException;
import java.util.ArrayList;
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
    private Button backButton; // Add this field

    private final ArrayList<String> navigationHistory = SharedData.getInstance().getNavigationHistory();

    @FXML
    private void switchToHome() throws IOException {
        addToHistory(App.getCurrentRoot()); // Store current page before switching
        App.setRoot("home");
    }

    @FXML
    private void switchToContactUs() throws IOException {
        addToHistory(App.getCurrentRoot());
        App.setRoot("contactUs");
    }

    @FXML
    private void switchToLogin() throws IOException {
        addToHistory(App.getCurrentRoot());
        App.setRoot("login");
    }

    @FXML
    private void switchToSignUp() throws IOException {
        addToHistory(App.getCurrentRoot());
        App.setRoot("signup");
    }

    @FXML
    private void logout() throws IOException {
        Auth.logout();
        updateNavbar();
        addToHistory(App.getCurrentRoot());
        App.setRoot("home");
    }

    @FXML
    private void switchToUserProfile() throws IOException {
        addToHistory(App.getCurrentRoot());
        App.setRoot("userProfile");
    }

    @FXML
    private void switchToSearchProducts() throws IOException {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            showAlert("Search Error", "Please enter a product name to begin your search.");
            return;
        }
        SharedData.getInstance().setSearchQuery(searchQuery);
        addToHistory(App.getCurrentRoot());
        App.setRoot("searchProducts");
    }

    @FXML
    private void goBack() throws IOException {
        if (navigationHistory.isEmpty()) {
            showAlert("Navigation", "No previous page to go back to.");
            return;
        }
        String previousPage = navigationHistory.remove(navigationHistory.size() - 1); // Pop the last page
        App.setRoot(previousPage);
    }

    // Helper method to add current page to history
    private void addToHistory(String currentPage) {
        if (currentPage != null && !currentPage.isEmpty()) {
            navigationHistory.add(currentPage);
        }
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
            usernameLabel.setText(Auth.getUsername());
        } else {
            loginBtn.setVisible(true);
            signupBtn.setVisible(true);
            usernameLabel.setVisible(false);
        }
    }
}