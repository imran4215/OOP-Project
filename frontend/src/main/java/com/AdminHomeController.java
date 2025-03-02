package com;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminHomeController {

    @FXML
    private StackPane contentArea;

    /**
     * Initializes the controller when the FXML is loaded.
     */
    @FXML
    public void initialize() {
        // Load the Dashboard page by default
        showDashboardPage();
    }

    @FXML
    private void showDashboardPage() {
        loadPage("/fxml/dashboard.fxml"); // Placeholder, replace with actual file if exists
    }

    @FXML
    private void showOrdersPage() {
        loadPage("/fxml/orders.fxml"); // Placeholder, replace with actual file if exists
    }

    @FXML
    private void showUsersPage() {
        loadPage("/fxml/demo.fxml"); // Loads the user management page
    }

    /**
     * Loads an FXML page into the content area.
     *
     * @param fxmlPath The path to the FXML file in the resources folder
     */
    private void loadPage(String fxmlPath) {
        try {
            Node page = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
            // contentArea.getChildren().setAll(new Label("Error loading page: " +
            // fxmlPath));
        }
    }
}