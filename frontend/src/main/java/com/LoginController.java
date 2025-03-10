package com;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button homeButton; // Add this field

    private final OkHttpClient client = new OkHttpClient();

    @FXML
    private void initialize() {
        // Set up the action for the Login button
        loginButton.setOnAction(event -> {
            try {
                handleSignUp();
            } catch (IOException e) {
                showErrorDialog("Error", "Failed to connect to the server.");
                e.printStackTrace();
            }
        });

        // Set up the action for the Home button
        homeButton.setOnAction(event -> {
            try {
                goToHome();
            } catch (IOException e) {
                showErrorDialog("Error", "Failed to redirect to Home page.");
                e.printStackTrace();
            }
        });
    }

    private void handleSignUp() throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showErrorDialog("Validation Error", "All fields are required.");
            return;
        }

        if (email.equals("admin") && password.equals("admin")) {
            App.setRoot("adminLogin");
            return;
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("email", email);
        jsonPayload.put("password", password);

        RequestBody requestBody = RequestBody.create(
                jsonPayload.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://localhost:5000/api/user/login")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorDialog("Error", "Failed to connect to the server."));
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        String username = new JSONObject(responseBody).getJSONObject("user").getString("username");
                        Auth.login(username);
                        showInfoDialog("Success", "Logged In successfully!");
                        try {
                            App.setRoot("home");
                        } catch (IOException ex) {
                            showErrorDialog("Error", "Failed to redirect to Home page.");
                            ex.printStackTrace();
                        }
                    });
                } else {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String errorMessage = jsonResponse.optString("error", "An unknown error occurred");
                    Platform.runLater(() -> showErrorDialog("Error", errorMessage));
                }
                response.close();
            }
        });
    }

    @FXML
    private void switchToSignup() throws IOException {
        App.setRoot("signup");
    }

    @FXML
    private void goToHome() throws IOException {
        App.setRoot("home"); // Redirect to home page
    }

    private void showErrorDialog(String title, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}