package com;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.collections.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AdminDashboardController {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());

    // UI Components
    @FXML
    private LineChart<String, Number> salesChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, String> orderIdCol;
    @FXML
    private TableColumn<Order, String> customerCol;
    @FXML
    private TableColumn<Order, Double> amountCol;
    @FXML
    private TableColumn<Order, String> statusCol;
    @FXML
    private Label usersCount;
    @FXML
    private Label ordersCount;
    @FXML
    private Label revenueCount;
    @FXML
    private StackPane contentPane; // For dynamic content
    @FXML
    private VBox dashboardContent; // Default dashboard content
    @FXML
    private Button usersBtn;
    @FXML
    private Button ordersBtn;

    private ObservableList<Order> orderData = FXCollections.observableArrayList();
    private Node usersView; // Cache for users view

    @FXML
    public void initialize() {
        try {
            setupTableColumns();
            loadSampleData();
            setupChart();
            updateStats();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Initialization failed: " + e.getMessage(), e);
            showAlert("Error", "Failed to initialize dashboard");
        }
    }

    private void setupTableColumns() {
        orderIdCol.setCellValueFactory(cellData -> cellData.getValue().orderIdProperty());
        customerCol.setCellValueFactory(cellData -> cellData.getValue().customerProperty());
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        ordersTable.setItems(orderData);
    }

    private void setupChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");
        series.getData().addAll(
                new XYChart.Data<>("Jan", 100),
                new XYChart.Data<>("Feb", 200),
                new XYChart.Data<>("Mar", 150),
                new XYChart.Data<>("Apr", 300),
                new XYChart.Data<>("May", 400));
        salesChart.getData().add(series);
    }

    private void loadSampleData() {
        orderData.addAll(
                new Order("1001", "John Doe", 120.0, "Delivered"),
                new Order("1002", "Jane Smith", 250.0, "Pending"),
                new Order("1003", "Alice Johnson", 80.0, "Shipped"));
    }

    private void updateStats() {
        usersCount.setText("1,234");
        ordersCount.setText(String.valueOf(orderData.size()));
        double totalRevenue = orderData.stream().mapToDouble(Order::getAmount).sum();
        revenueCount.setText(String.format("$%,.2f", totalRevenue));
    }

    @FXML
    private void handleLogout() throws IOException {
        App.setRoot("login");
    }

    @FXML
    private void showDashboard() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(dashboardContent);
        LOGGER.info("Switched to Dashboard view");
    }

    @FXML
    private void showUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/components/admin/adminAllUsers.fxml"));
            usersView = loader.load();
            AdminAllUsersController controller = loader.getController();
            controller.setContentPane(contentPane); // Pass contentPane reference
            contentPane.getChildren().clear();
            contentPane.getChildren().add(usersView);
            LOGGER.info("Switched to Users view");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load adminAllUsers.fxml: " + e.getMessage(), e);
            showAlert("Error", "Could not load Users view");
        }
    }

    @FXML
    private void showOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/components/admin/adminAllOrders.fxml"));
            usersView = loader.load();
            AdminAllOrdersController controller = loader.getController();
            controller.setContentPane(contentPane);
            contentPane.getChildren().clear();
            contentPane.getChildren().add(usersView);
            LOGGER.info("Switched to Users view");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load adminAllOrders.fxml: " + e.getMessage(), e);
            showAlert("Error", "Could not load Users view");
        }
    }

    @FXML
    private void showSettings() {
        // Implement settings view if needed
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Order class with proper properties
    public static class Order {
        private final StringProperty orderId = new SimpleStringProperty();
        private final StringProperty customer = new SimpleStringProperty();
        private final DoubleProperty amount = new SimpleDoubleProperty();
        private final StringProperty status = new SimpleStringProperty();

        public Order(String orderId, String customer, double amount, String status) {
            this.orderId.set(orderId);
            this.customer.set(customer);
            this.amount.set(amount);
            this.status.set(status);
        }

        public StringProperty orderIdProperty() {
            return orderId;
        }

        public StringProperty customerProperty() {
            return customer;
        }

        public DoubleProperty amountProperty() {
            return amount;
        }

        public StringProperty statusProperty() {
            return status;
        }

        public double getAmount() {
            return amount.get();
        }
    }
}