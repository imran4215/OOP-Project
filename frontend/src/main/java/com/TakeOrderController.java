package com;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;

public class TakeOrderController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField titleField, imageField, priceField;
    @FXML
    private TextField cityField, countryField, addressField, postalCodeField, phoneNumberField;
    @FXML
    private Text quantityText, totalPriceText;
    @FXML
    private Label statusLabel;
    @FXML
    private Button decreaseQtyBtn, increaseQtyBtn, submitBtn;

    private double totalPrice = 0.0;
    private double unitPrice = 0.0;
    private int quantity = 1;
    private final OkHttpClient client = new OkHttpClient();

    @FXML
    private void initialize() {
        // Auto-fill fields from SharedData
        usernameField.setText(Auth.getUsername());

        String selectedProductJson = SharedData.getInstance().getSelectedProduct(); // Use getSelectedProduct
        if (selectedProductJson != null && !selectedProductJson.isEmpty()) {
            JSONObject product = new JSONObject(selectedProductJson);
            titleField.setText(product.optString("productName", ""));
            imageField.setText(product.optString("productImage", ""));
            String priceStr = product.optString("productPrice", "0").replace(",", "").trim();
            unitPrice = Double.parseDouble(priceStr);
            priceField.setText(String.format("%.2f", unitPrice));
            updateTotalPrice();
        }

        // Set up button actions
        submitBtn.setOnAction(event -> {
            try {
                handleOrderSubmission();
            } catch (IOException e) {
                showErrorDialog("Error", "Failed to connect to the server.");
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void increaseQuantity() {
        quantity++;
        quantityText.setText(String.valueOf(quantity));
        updateTotalPrice();
    }

    @FXML
    private void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityText.setText(String.valueOf(quantity));
            updateTotalPrice();
        }
    }

    private void updateTotalPrice() {
        totalPrice = unitPrice * quantity;
        totalPriceText.setText(String.format("$%.2f", totalPrice));
    }

    private void handleOrderSubmission() throws IOException {
        String username = usernameField.getText();

        // Validate input fields
        if (username.isEmpty()) {
            showErrorDialog("Validation Error", "Username is required.");
            return;
        }

        // Collect product
        JSONObject product = new JSONObject();
        product.put("title", titleField.getText());
        product.put("image", imageField.getText());
        product.put("price", unitPrice);
        product.put("quantity", quantity);

        // Collect address
        JSONObject address = new JSONObject();
        address.put("city", cityField.getText());
        address.put("country", countryField.getText());
        address.put("address", addressField.getText());
        address.put("postalCode", postalCodeField.getText());
        address.put("phoneNumber", phoneNumberField.getText());

        if (address.getString("city").isEmpty() || address.getString("country").isEmpty() ||
                address.getString("address").isEmpty()) {
            showErrorDialog("Validation Error", "City, country, and address are required.");
            return;
        }

        // Prepare JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("username", username);
        jsonPayload.put("products", new JSONArray().put(product));
        jsonPayload.put("totalPrice", totalPrice);
        jsonPayload.put("address", address);

        // Create the request body
        RequestBody requestBody = RequestBody.create(
                jsonPayload.toString(),
                MediaType.get("application/json; charset=utf-8"));

        // Build the HTTP POST request
        Request request = new Request.Builder()
                .url("http://localhost:5000/api/order/takeOrder") // Adjust URL as needed
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
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        showInfoDialog("Success", "Order placed successfully!");
                        clearForm();
                    });
                } else {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String errorMessage = jsonResponse.optString("message", "An unknown error occurred");
                    Platform.runLater(() -> showErrorDialog("Error", errorMessage));
                }
                response.close();
            }
        });
    }

    private void clearForm() {
        cityField.clear();
        countryField.clear();
        addressField.clear();
        postalCodeField.clear();
        phoneNumberField.clear();
        quantity = 1;
        quantityText.setText("1");
        updateTotalPrice();
        statusLabel.setText("");
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