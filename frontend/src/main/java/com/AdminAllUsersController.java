package com;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class for managing user data display and interactions in the
 * JavaFX application.
 */
public class AdminAllUsersController {

    @FXML
    private TableView<JSONObject> userTable;

    private Pane contentPane; // Reference to the dashboard's content pane
    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String BASE_URL = "http://localhost:5000/api/user";
    private static final Logger LOGGER = Logger.getLogger(AdminAllUsersController.class.getName());

    @FXML
    public void initialize() {
        setupTableColumns();
        fetchUserData();
    }

    public void setContentPane(Pane contentPane) {
        this.contentPane = contentPane;
        if (contentPane == null) {
            LOGGER.severe("Received null contentPane in setContentPane.");
        } else {
            LOGGER.info("contentPane set successfully in AdminAllUsersController.");
        }
    }

    private void setupTableColumns() {
        addTableColumn("User Name", "username", 200);
        addTableColumn("Email", "email", 200);
        addTableColumn("Total Orders", "totalOrders", 150);
        addTableColumn("Pending Orders", "pendingOrders", 150);
        addTableColumn("Cancelled Orders", "canceledOrders", 150);
        addTableColumn("Delivered Orders", "deliveredOrders", 150);
        addActionColumn();
    }

    private void addTableColumn(String title, String jsonKey, int width) {
        TableColumn<JSONObject, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            JSONObject user = cellData.getValue();
            String value = user.optString(jsonKey, "N/A");
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        column.setPrefWidth(width);
        column.setStyle("-fx-alignment: CENTER;");
        userTable.getColumns().add(column);
    }

    private void addActionColumn() {
        TableColumn<JSONObject, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button detailsButton = createButton("Details", "button-details", this::handleDetailsClick);
            private final Button deleteButton = createButton("Delete", "button-delete", this::handleDeleteClick);
            private final HBox buttonBox = new HBox(10, detailsButton, deleteButton);

            {
                buttonBox.setAlignment(Pos.CENTER);
            }

            private void handleDetailsClick() {
                JSONObject user = getTableRow().getItem();
                if (user != null) {
                    showUserDetails(user);
                }
            }

            private void handleDeleteClick() {
                JSONObject user = getTableRow().getItem();
                if (user != null) {
                    deleteUser(user);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });
        actionColumn.setPrefWidth(200);
        userTable.getColumns().add(actionColumn);
    }

    private Button createButton(String text, String styleClass, Runnable onAction) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(e -> onAction.run());
        return button;
    }

    private void fetchUserData() {
        Request request = new Request.Builder()
                .url(BASE_URL + "/allUsers")
                .get()
                .build();

        executeAsyncRequest(request, this::handleFetchResponse);
    }

    private void handleFetchResponse(Response response) throws IOException {
        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            JSONArray usersArray = new JSONObject(responseBody).getJSONArray("users");
            ObservableList<JSONObject> userList = FXCollections.observableArrayList();

            for (int i = 0; i < usersArray.length(); i++) {
                userList.add(usersArray.getJSONObject(i));
            }

            Platform.runLater(() -> userTable.setItems(userList));
        } else {
            logError("Failed to load users. Server responded with code: " + response.code());
        }
    }

    private void showUserDetails(JSONObject user) {
        String username = user.optString("username", "N/A");
        SharedData.getInstance().setSelectedUsername(username);
        LOGGER.info("Selected user: " + username);

        try {
            String fxmlPath = "/com/components/admin/adminUserDetails.fxml";
            LOGGER.info("Attempting to load FXML from: " + fxmlPath);

            if (getClass().getResource(fxmlPath) == null) {
                throw new IOException("FXML file not found at: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent userDetailsRoot = loader.load();

            if (contentPane == null) {
                LOGGER.severe("contentPane is null. Cannot load user details.");
                showAlert(Alert.AlertType.ERROR, "Error", "Content pane is not initialized.");
                return;
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(userDetailsRoot);
            LOGGER.info("Successfully loaded adminUserDetails.fxml into contentPane for user: " + username);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load adminUserDetails.fxml: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load user details view: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error: " + e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void deleteUser(JSONObject user) {
        String username = user.optString("username", "N/A");
        if (confirmDelete(username)) {
            Request deleteRequest = new Request.Builder()
                    .url(BASE_URL + "/deleteUser/" + username)
                    .delete()
                    .build();

            executeAsyncRequest(deleteRequest, response -> handleDeleteResponse(response, user));
        }
    }

    private boolean confirmDelete(String username) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setContentText("Are you sure you want to delete " + username + "?");
        return confirmation.showAndWait()
                .orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void handleDeleteResponse(Response response, JSONObject user) throws IOException {
        if (response.isSuccessful()) {
            Platform.runLater(() -> {
                userTable.getItems().remove(user);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
            });
        } else {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user."));
        }
    }

    private void executeAsyncRequest(Request request, ResponseHandler handler) {
        new Thread(() -> {
            try (Response response = CLIENT.newCall(request).execute()) {
                handler.handle(response);
            } catch (IOException e) {
                logError("Request failed", e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Something went wrong."));
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
}