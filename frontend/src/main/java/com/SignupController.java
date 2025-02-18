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

public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signUpButton;

    // Create a new OkHttpClient instance to send HTTP requests
    private final OkHttpClient client = new OkHttpClient();

    @FXML
    private void initialize() {
        // Set up the action for the Sign Up button
        signUpButton.setOnAction(event -> {
            try {
                handleSignUp();
            } catch (IOException e) {
                showErrorDialog("Error", "Failed to connect to the server.");
                e.printStackTrace();
            }
        });
    }

    private void handleSignUp() throws IOException {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorDialog("Validation Error", "All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showErrorDialog("Validation Error", "Passwords do not match.");
            return;
        }

        // Prepare JSON payload for the API request
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("username", username);
        jsonPayload.put("email", email);
        jsonPayload.put("password", password);
        jsonPayload.put("confirmPassword", confirmPassword);

        // Create the request body
        RequestBody requestBody = RequestBody.create(
                jsonPayload.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        // Build the HTTP POST request
        Request request = new Request.Builder()
                .url("http://localhost:5000/api/user/signup")
                .post(requestBody)
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorDialog("Error", "Failed to connect to the server."));
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                // Get the response from backend as a string
                String responseBody = response.body().string();
                //System.out.println(responseBody);

                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        showInfoDialog("Success", "Account created successfully!");
                        try {
                            App.setRoot("login");
                        } catch (IOException ex) {
                            showErrorDialog("Error", "Failed to redirect to login page.");
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
    private void switchToLogin() throws IOException {
        App.setRoot("login");
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