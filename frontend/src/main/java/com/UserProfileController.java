package com;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;

public class UserProfileController {

    @FXML
    private ImageView profileImage;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, String> orderIdColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, Double> orderAmountColumn;

    @FXML
    private TableColumn<Order, String> orderStatusColumn;

    private ObservableList<Order> orderData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Set user data
        setUserData("JaneDoe", "janedoe@example.com");

        // Set up table columns
        orderIdColumn.setCellValueFactory(cellData -> cellData.getValue().orderIdProperty());
        orderDateColumn.setCellValueFactory(cellData -> cellData.getValue().orderDateProperty());
        orderAmountColumn.setCellValueFactory(cellData -> cellData.getValue().orderAmountProperty().asObject());
        orderStatusColumn.setCellValueFactory(cellData -> cellData.getValue().orderStatusProperty());

        // Populate table with sample data
        loadSampleOrders();

        // Bind the table to the data
        ordersTable.setItems(orderData);
    }

    public void setUserData(String username, String email) {
        usernameLabel.setText(username);
        emailLabel.setText(email);
    }

    private void loadSampleOrders() {
        orderData.add(new Order("ORD001", "2023-10-01", 150.75, "Shipped"));
        orderData.add(new Order("ORD002", "2023-09-15", 89.50, "Delivered"));
        orderData.add(new Order("ORD003", "2023-08-20", 45.00, "Pending"));
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Edit Profile button clicked!");
    }

    public static class Order {
        private final SimpleStringProperty orderId;
        private final SimpleStringProperty orderDate;
        private final SimpleDoubleProperty orderAmount;
        private final SimpleStringProperty orderStatus;

        public Order(String orderId, String orderDate, double orderAmount, String orderStatus) {
            this.orderId = new SimpleStringProperty(orderId);
            this.orderDate = new SimpleStringProperty(orderDate);
            this.orderAmount = new SimpleDoubleProperty(orderAmount);
            this.orderStatus = new SimpleStringProperty(orderStatus);
        }

        public SimpleStringProperty orderIdProperty() {
            return orderId;
        }

        public SimpleStringProperty orderDateProperty() {
            return orderDate;
        }

        public SimpleDoubleProperty orderAmountProperty() {
            return orderAmount;
        }

        public SimpleStringProperty orderStatusProperty() {
            return orderStatus;
        }
    }
}