package com;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AdminOrderDetailsController {
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String FETCH_URL = "http://localhost:5000/api/order/getOrderDetails/";
    private static final String UPDATE_URL = "http://localhost:5000/api/order/updateOrderStatus/"; // Adjust as needed
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private VBox container;

    private TextField usernameField, emailField, orderIdField, orderDateField, totalPriceField;
    private TextField cityField, countryField, addressField, postalCodeField, phoneField;
    private TableView<Product> productsTable;
    private ComboBox<String> statusComboBox;
    private Button updateButton;
    private String orderId;

    public void initialize() {

        // Get the selected order ID from the SharedData singleton
        orderId = SharedData.getInstance().getSelectedOrderId();
        createUI();
        loadOrderDetails(orderId);
    }

    private void createUI() {
        // Customer Details Section
        VBox customerSection = createSection("Customer Details");
        usernameField = createTextField("Username:");
        emailField = createTextField("Email:");
        customerSection.getChildren().add(createHBox(usernameField, emailField));

        // Order Info Section
        VBox orderSection = createSection("Order Information");
        orderIdField = createTextField("Order ID:");
        orderDateField = createTextField("Date:");
        totalPriceField = createTextField("Total:");
        orderSection.getChildren().add(createHBox(orderIdField, orderDateField, totalPriceField));

        // Products Section
        VBox productsSection = createSection("Products");
        productsTable = new TableView<>();
        productsTable.setPrefHeight(250);
        productsTable.getStyleClass().add("table-view");
        TableColumn<Product, String> productColumn = new TableColumn<>("Product");
        productColumn.getStyleClass().add("table-column");
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.getStyleClass().add("table-column");
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.getStyleClass().add("table-column");
        productsTable.getColumns().addAll(productColumn, priceColumn, quantityColumn);
        productColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "$" + DECIMAL_FORMAT.format(item));
            }
        });
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        productsSection.getChildren().add(productsTable);

        // Shipping Section
        VBox shippingSection = createSection("Shipping Details");
        cityField = createTextField("City:");
        countryField = createTextField("Country:");
        postalCodeField = createTextField("Postal:");
        shippingSection.getChildren().add(createHBox(cityField, countryField, postalCodeField));
        addressField = createTextField("Address:");
        addressField.getStyleClass().add("text-field-wide");
        phoneField = createTextField("Phone:");
        shippingSection.getChildren().add(createHBox(addressField, phoneField));

        // Status Section
        VBox statusSection = createSection("Order Status");
        statusComboBox = new ComboBox<>();
        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "Delivered", "Cancelled"));
        statusComboBox.getStyleClass().add("combo-box");
        statusComboBox.setPromptText("Select Status");
        updateButton = new Button("Update Order");
        updateButton.getStyleClass().add("button");
        updateButton.setOnAction(event -> updateOrderStatus());
        statusSection.getChildren().add(createHBox(statusComboBox, updateButton));

        // Add all sections to container
        container.getChildren().addAll(customerSection, orderSection, productsSection, shippingSection, statusSection);
    }

    private VBox createSection(String title) {
        VBox section = new VBox(10);
        section.getStyleClass().add("section-box");
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("section-title");
        section.getChildren().add(sectionTitle);
        return section;
    }

    private TextField createTextField(String labelText) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        TextField field = new TextField();
        field.setEditable(false);
        field.getStyleClass().add("text-field");
        box.getChildren().addAll(label, field);
        return field;
    }

    private HBox createHBox(Control... nodes) {
        HBox hbox = new HBox(15);
        hbox.getChildren().addAll(nodes);
        return hbox;
    }

    private void loadOrderDetails(String orderId) {
        String url = FETCH_URL + orderId;
        Request request = new Request.Builder().url(url).get().build();

        new Thread(() -> {
            try (Response response = CLIENT.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONObject order = jsonObject.getJSONObject("order");
                    Platform.runLater(() -> populateUI(order));
                } else {
                    Platform.runLater(() -> showErrorAlert("Failed to fetch order: " + response.code()));
                }
            } catch (IOException e) {
                Platform.runLater(() -> showErrorAlert("Network error: " + e.getMessage()));
            }
        }).start();
    }

    private void populateUI(JSONObject order) {
        JSONObject user = order.getJSONObject("userId");
        usernameField.setText(user.getString("username"));
        emailField.setText(user.getString("email"));

        orderIdField.setText(order.getString("_id"));
        orderDateField.setText(ZonedDateTime.parse(order.getString("createdAt")).format(DATE_FORMATTER));
        totalPriceField.setText("$" + DECIMAL_FORMAT.format(order.getDouble("totalPrice")));

        ObservableList<Product> products = FXCollections.observableArrayList();
        JSONArray productsArray = order.getJSONArray("products");
        for (int i = 0; i < productsArray.length(); i++) {
            JSONObject product = productsArray.getJSONObject(i);
            products.add(new Product(
                    product.getString("title"),
                    product.getDouble("price"),
                    product.getInt("quantity")));
        }
        productsTable.setItems(products);

        JSONArray addressArray = order.getJSONArray("address");
        if (addressArray.length() > 0) {
            JSONObject address = addressArray.getJSONObject(0);
            cityField.setText(address.getString("city"));
            countryField.setText(address.getString("country"));
            addressField.setText(address.getString("address"));
            postalCodeField.setText(address.getString("postalCode"));
            phoneField.setText(address.getString("phoneNumber"));
        }

        statusComboBox.getSelectionModel().select(
                order.getString("orderStatus").substring(0, 1).toUpperCase() +
                        order.getString("orderStatus").substring(1));
    }

    private void updateOrderStatus() {
        String selectedStatus = statusComboBox.getSelectionModel().getSelectedItem();
        if (selectedStatus == null) {
            showErrorAlert("Please select a status");
            return;
        }

        // API URL
        String url = UPDATE_URL + orderId;

        // Prepare JSON payload for the API request
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("orderStatus", selectedStatus.toLowerCase());

        // Create the request body
        RequestBody requestBody = RequestBody.create(
                jsonPayload.toString(),
                MediaType.get("application/json; charset=utf-8"));

        // Build the HTTP PUT request
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        // Send the request asynchronously
        CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> showErrorAlert("Failed to connect to the server: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                // System.out.println(responseBody);
                response.close();

                Platform.runLater(() -> {
                    try {
                        // Parse the response body as JSON
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (response.isSuccessful()) {
                            // Handle success
                            showSuccessAlert("Order status updated successfully");
                            loadOrderDetails(orderId); // Refresh the order details
                        } else {
                            // Handle error response
                            String errorMessage = jsonResponse.optString("message", "An unknown error occurred");
                            showErrorAlert("Failed to update order status: " + errorMessage);
                        }
                    } catch (Exception e) {
                        // Handle JSON parsing errors
                        showErrorAlert("Invalid response from the server: " + responseBody);
                    }
                });
            }
        });
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Product {
        private String title;
        private double price;
        private int quantity;

        public Product(String title, double price, int quantity) {
            this.title = title;
            this.price = price;
            this.quantity = quantity;
        }

        public String getTitle() {
            return title;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}