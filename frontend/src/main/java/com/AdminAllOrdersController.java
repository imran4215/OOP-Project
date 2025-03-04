package com;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class for managing the All Orders display in the JavaFX
 * application.
 */
public class AdminAllOrdersController {

    @FXML
    private TableView<Order> allOrdersTable;
    @FXML
    private TableColumn<Order, String> orderIdCol, orderDateCol, orderStatusCol;
    @FXML
    private TableColumn<Order, Double> orderPriceCol;
    @FXML
    private TableColumn<Order, Void> viewDetailsCol;

    private Pane contentPane; // Reference to the dashboard's content pane for navigation
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String BASE_URL = "http://localhost:5000/api/order/getAllOrders";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("$#,##0.00");
    private static final Logger LOGGER = Logger.getLogger(AdminAllOrdersController.class.getName());
    private ObservableList<Order> orderData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        fetchOrderData();
    }

    public void setContentPane(Pane contentPane) {
        this.contentPane = contentPane;
        if (contentPane == null) {
            LOGGER.severe("Received null contentPane in setContentPane.");
        } else {
            LOGGER.info("contentPane set successfully in AllOrdersController.");
        }
    }

    private void setupTableColumns() {
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

        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #4b5563; -fx-font-size: 13px; -fx-padding: 8px;");
            }
        });

        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        orderStatusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    setStyle("-fx-alignment: CENTER; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px;");
                    switch (item.toLowerCase()) {
                        case "delivered":
                            getStyleClass().add("status-delivered");
                            break;
                        case "pending":
                            getStyleClass().add("status-pending");
                            break;
                        case "canceled":
                            getStyleClass().add("status-canceled");
                            break;
                    }
                }
            }
        });

        orderPriceCol.setCellValueFactory(new PropertyValueFactory<>("orderPrice"));
        orderPriceCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : DECIMAL_FORMAT.format(item));
                setStyle("-fx-alignment: CENTER; -fx-font-size: 13px; -fx-padding: 8px;");
                getStyleClass().add("price");
            }
        });

        viewDetailsCol.setCellFactory(
                createButtonCellFactory("View Details", "button-view-details", this::handleViewDetailsClick));
    }

    private Callback<TableColumn<Order, Void>, TableCell<Order, Void>> createButtonCellFactory(String text,
            String styleClass, RunnableWithOrder onAction) {
        return column -> new TableCell<>() {
            private final Button button = new Button(text);
            {
                button.getStyleClass().add(styleClass);
                button.setOnAction(e -> {
                    Order order = getTableRow().getItem();
                    if (order != null) {
                        onAction.run(order);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        };
    }

    private void fetchOrderData() {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .build();

        executeAsyncRequest(request, this::handleFetchResponse);
    }

    private void handleFetchResponse(Response response) throws IOException {
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            JSONArray ordersArray = new JSONArray(responseBody);
            orderData.clear();

            for (int i = 0; i < ordersArray.length(); i++) {
                JSONObject orderJson = ordersArray.getJSONObject(i);
                orderData.add(new Order(
                        orderJson.optString("_id", "N/A"),
                        orderJson.optString("createdAt", "N/A"),
                        orderJson.optString("orderStatus", "Unknown"),
                        orderJson.optDouble("totalPrice", 0.0)));
            }

            Platform.runLater(() -> {
                orderData.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate())); // Sort by date descending
                allOrdersTable.setItems(orderData);
            });
        } else {
            logError("Failed to load orders. Server responded with code: " + response.code());
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to fetch orders. Code: " + response.code()));
        }
    }

    private void handleViewDetailsClick(Order order) {
        LOGGER.info("Viewing details for order: " + order.getOrderId());
        String selectedOrderId = order.getOrderId();
        SharedData.getInstance().setSelectedOrderId(selectedOrderId);
        try {
            String fxmlPath = "/com/components/admin/adminOrderDetails.fxml";
            LOGGER.info("Attempting to load FXML from: " + fxmlPath);

            if (getClass().getResource(fxmlPath) == null) {
                throw new IOException("FXML file not found at: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent orderDetailsRoot = loader.load();

            if (contentPane == null) {
                LOGGER.severe("contentPane is null. Cannot load order details.");
                showAlert(Alert.AlertType.ERROR, "Error", "Content pane is not initialized.");
                return;
            }

            // Pass order ID to the details controller if needed
            // AdminOrderDetailsController controller = loader.getController();
            // controller.setOrderId(order.getOrderId());

            contentPane.getChildren().clear();
            contentPane.getChildren().add(orderDetailsRoot);
            LOGGER.info("Successfully loaded adminOrderDetails.fxml into contentPane for order: " + order.getOrderId());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load adminOrderDetails.fxml: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load order details view: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void executeAsyncRequest(Request request, ResponseHandler handler) {
        new Thread(() -> {
            try (Response response = CLIENT.newCall(request).execute()) {
                handler.handle(response);
            } catch (IOException e) {
                logError("Request failed", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Network error: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-font-size: 14px;");
        alert.showAndWait();
    }

    private void logError(String message) {
        LOGGER.severe(message);
    }

    private void logError(String message, Exception e) {
        LOGGER.log(Level.SEVERE, message, e);
    }

    @FunctionalInterface
    private interface ResponseHandler {
        void handle(Response response) throws IOException;
    }

    @FunctionalInterface
    private interface RunnableWithOrder {
        void run(Order order);
    }

    public static class Order {
        private final SimpleStringProperty orderId;
        private final SimpleStringProperty orderDate;
        private final SimpleStringProperty orderStatus;
        private final SimpleDoubleProperty orderPrice;

        public Order(String orderId, String createdAt, String orderStatus, double orderPrice) {
            this.orderId = new SimpleStringProperty(orderId);
            this.orderDate = new SimpleStringProperty();
            this.orderStatus = new SimpleStringProperty(orderStatus);
            this.orderPrice = new SimpleDoubleProperty(orderPrice);

            try {
                this.orderDate.set(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                        .format(java.time.ZonedDateTime.parse(createdAt)));
            } catch (Exception e) {
                this.orderDate.set("N/A");
            }
        }

        public String getOrderId() {
            return orderId.get();
        }

        public SimpleStringProperty orderIdProperty() {
            return orderId;
        }

        public String getOrderDate() {
            return orderDate.get();
        }

        public SimpleStringProperty orderDateProperty() {
            return orderDate;
        }

        public String getOrderStatus() {
            return orderStatus.get();
        }

        public SimpleStringProperty orderStatusProperty() {
            return orderStatus;
        }

        public double getOrderPrice() {
            return orderPrice.get();
        }

        public SimpleDoubleProperty orderPriceProperty() {
            return orderPrice;
        }
    }
}