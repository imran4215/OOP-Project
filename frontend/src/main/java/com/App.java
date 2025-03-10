package com;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static String currentRoot; // Add this to track the current page

    @Override
    public void start(Stage stage) throws IOException {
        currentRoot = "home"; // Set initial root
        scene = new Scene(loadFXML("home"));
        // scene = new Scene(loadFXML("takeOrder"));
        // scene = new Scene(loadFXML("adminDashboard"));
        // stage.setFullScreen(true);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        currentRoot = fxml; // Update currentRoot before changing the scene
        scene.setRoot(loadFXML(fxml));
    }

    // Add this method to get the current root
    public static String getCurrentRoot() {
        return currentRoot;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}