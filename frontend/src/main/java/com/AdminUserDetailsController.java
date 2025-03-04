package com;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminUserDetailsController {
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String BASE_URL = "http://localhost:5000/api/user/userDetails/";

    @FXML
    private Label usernameLabel, emailLabel, userIdLabel, totalOrdersLabel, pendingOrdersLabel,
            deliveredOrdersLabel, totalSpentLabel, pendingSpentLabel;
    @FXML
    private TableView<UserOrder> ordersTable;
    @FXML
    private TableColumn<UserOrder, String> orderIdCol, statusCol, createdAtCol;
    @FXML
    private TableColumn<UserOrder, Void> productsCol;
    @FXML
    private TableColumn<UserOrder, Double> totalPriceCol;

    private ObservableList<UserOrder> userOrderData = FXCollections.observableArrayList();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("$#,##0.00");

    public void initialize() {
        String username = SharedData.getInstance().getSelectedUsername();
        if (username != null && !username.isEmpty()) {
            setupTableColumns();
            loadUserDetails(username);
        } else {
            showErrorAlert("No username provided");
        }
    }

    private void setupTableColumns() {
        ordersTable.getStyleClass().add("orders-table");
        ordersTable.setRowFactory(tv -> {
            TableRow<UserOrder> row = new TableRow<>();
            row.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
            row.hoverProperty().addListener((obs, oldVal, newVal) -> {
                row.setStyle(newVal ? "-fx-background-color: #f9fafb;"
                        : "-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
                FadeTransition ft = new FadeTransition(Duration.millis(150), row);
                ft.setFromValue(newVal ? 1.0 : 0.95);
                ft.setToValue(newVal ? 0.95 : 1.0);
                ft.play();
            });
            return row;
        });

        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle(
                        "-fx-alignment: CENTER-LEFT; -fx-text-fill: #1f2937; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px;");
            }
        });

        productsCol.setCellFactory(column -> new TableCell<>() {
            private final VBox vbox = new VBox(8);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    UserOrder order = getTableRow().getItem();
                    vbox.getChildren().clear();
                    vbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    vbox.setPadding(new javafx.geometry.Insets(8));
                    vbox.setPrefWidth(column.getWidth() - 16);

                    for (Product p : order.getProducts()) {
                        HBox productBox = createProductBox(p);
                        productBox.setPrefWidth(vbox.getPrefWidth());
                        vbox.getChildren().add(productBox);
                    }
                    setGraphic(vbox);

                    Platform.runLater(() -> {
                        TableRow<?> row = getTableRow();
                        if (row != null) {
                            row.requestLayout();
                            double height = vbox.getChildren().size() * 60 + 16;
                            row.setMinHeight(height);
                            row.setPrefHeight(height);
                            row.setMaxHeight(height);
                        }
                    });
                }
            }
        });

        totalPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalPriceCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : DECIMAL_FORMAT.format(item));
                setStyle(
                        "-fx-alignment: CENTER; -fx-text-fill: #059669; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8px;");
            }
        });

        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.toUpperCase());
                    setStyle("-fx-alignment: CENTER; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px;");
                    switch (item.toLowerCase()) {
                        case "delivered":
                            setStyle(
                                    "-fx-background-color: #d1fae5; -fx-text-fill: #059669; -fx-alignment: CENTER; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px;");
                            break;
                        case "pending":
                            setStyle(
                                    "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-alignment: CENTER; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px;");
                            break;
                        case "canceled":
                            setStyle(
                                    "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-alignment: CENTER; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px;");
                            break;
                    }
                }
            }
        });

        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #4b5563; -fx-font-size: 13px; -fx-padding: 8px;");
            }
        });
    }

    private HBox createProductBox(Product p) {
        HBox productBox = new HBox(10);
        productBox.getStyleClass().add("product-item");
        productBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(p.getImage(), true));
            imageView.setFitWidth(40);
            imageView.setFitHeight(40);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        } catch (Exception ignored) {
            imageView.setImage(null);
        }

        VBox details = new VBox(4);
        details.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Text title = new Text(p.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-fill: #1f2937; -fx-font-size: 13px;");
        HBox priceQtyBox = new HBox(8);
        priceQtyBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Text price = new Text(DECIMAL_FORMAT.format(p.getPrice()));
        price.setStyle("-fx-fill: #059669; -fx-font-size: 12px; -fx-font-weight: bold;");
        Text qty = new Text("× " + p.getQuantity());
        qty.setStyle("-fx-fill: #6b7280; -fx-font-size: 12px;");

        priceQtyBox.getChildren().addAll(price, qty);
        details.getChildren().addAll(title, priceQtyBox);
        productBox.getChildren().addAll(imageView, details);

        return productBox;
    }

    private void loadUserDetails(String username) {
        String url = BASE_URL + username;
        Request request = new Request.Builder().url(url).get().build();

        new Thread(() -> {
            try (Response response = CLIENT.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    Platform.runLater(() -> processUserDetails(jsonObject));
                } else {
                    Platform.runLater(() -> showErrorAlert("Failed to fetch user details. Code: " + response.code()));
                }
            } catch (IOException e) {
                Platform.runLater(() -> showErrorAlert("Network error: " + e.getMessage()));
            }
        }).start();
    }

    private void processUserDetails(JSONObject jsonObject) {
        try {
            if (jsonObject.has("error")) {
                showErrorAlert("API error: " + jsonObject.getString("error"));
                return;
            }

            JSONObject user = jsonObject.getJSONObject("user");
            usernameLabel.setText(user.optString("username", "N/A"));
            emailLabel.setText(user.optString("email", "N/A"));
            userIdLabel.setText(user.optString("_id", "N/A"));

            totalOrdersLabel.setText(String.valueOf(jsonObject.optInt("totalOrders", 0)));
            pendingOrdersLabel.setText(String.valueOf(jsonObject.optInt("pendingOrders", 0)));
            deliveredOrdersLabel.setText(String.valueOf(jsonObject.optInt("deliveredOrders", 0)));

            JSONArray ordersArray = jsonObject.getJSONArray("orders");
            userOrderData.clear();

            double totalSpent = 0.0, pendingSpent = 0.0;

            for (int i = 0; i < ordersArray.length(); i++) {
                JSONObject order = ordersArray.getJSONObject(i);
                List<Product> products = new ArrayList<>();
                JSONArray productsArray = order.getJSONArray("products");

                for (int j = 0; j < productsArray.length(); j++) {
                    JSONObject product = productsArray.getJSONObject(j);
                    products.add(new Product(
                            product.optString("title", "Unknown"),
                            product.optString("image", ""),
                            product.optDouble("price", 0.0),
                            product.optInt("quantity", 0)));
                }

                double orderTotalPrice = order.optDouble("totalPrice", 0.0);
                String orderStatus = order.optString("orderStatus", "Unknown").toLowerCase();

                totalSpent += orderTotalPrice;
                if ("pending".equals(orderStatus))
                    pendingSpent += orderTotalPrice;

                userOrderData.add(new UserOrder(
                        order.optString("_id", "N/A"),
                        products,
                        orderTotalPrice,
                        orderStatus,
                        order.optString("createdAt", "N/A")));
            }

            totalSpentLabel.setText(DECIMAL_FORMAT.format(totalSpent));
            pendingSpentLabel.setText(DECIMAL_FORMAT.format(pendingSpent));

            userOrderData.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
            ordersTable.setItems(userOrderData);

        } catch (Exception e) {
            showErrorAlert("Error processing data: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 14px;");
        alert.showAndWait();
    }

    public static class UserOrder {
        private final StringProperty orderId, status, createdAt;
        private final List<Product> products;
        private final DoubleProperty totalPrice;

        public UserOrder(String orderId, List<Product> products, double totalPrice, String status, String createdAt) {
            this.orderId = new SimpleStringProperty(orderId);
            this.products = products;
            this.totalPrice = new SimpleDoubleProperty(totalPrice);
            this.status = new SimpleStringProperty(status);
            this.createdAt = new SimpleStringProperty();
            try {
                this.createdAt.set(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                        .format(java.time.ZonedDateTime.parse(createdAt)));
            } catch (Exception e) {
                this.createdAt.set("N/A");
            }
        }

        public String getOrderId() {
            return orderId.get();
        }

        public StringProperty orderIdProperty() {
            return orderId;
        }

        public List<Product> getProducts() {
            return products;
        }

        public double getTotalPrice() {
            return totalPrice.get();
        }

        public DoubleProperty totalPriceProperty() {
            return totalPrice;
        }

        public String getStatus() {
            return status.get();
        }

        public StringProperty statusProperty() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt.get();
        }

        public StringProperty createdAtProperty() {
            return createdAt;
        }
    }

    public static class Product {
        private final String title, image;
        private final double price;
        private final int quantity;

        public Product(String title, String image, double price, int quantity) {
            this.title = title;
            this.image = image;
            this.price = price;
            this.quantity = quantity;
        }

        public String getTitle() {
            return title;
        }

        public String getImage() {
            return image;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}