package com;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Controller class for managing user data display and interactions in the
 * JavaFX application.
 */
public class AdminAllUsersController {

    @FXML
    private TableView<JSONObject> userTable;

    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String BASE_URL = "http://localhost:5000/api/user";

    /**
     * Initializes the controller automatically when the FXML is loaded.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        fetchUserData();
    }

    /**
     * Configures table columns including data columns and action column.
     */
    private void setupTableColumns() {
        addTableColumn("User Name", "username", 200);
        addTableColumn("Total Orders", "totalOrders", 150);
        addTableColumn("Pending Orders", "pendingOrders", 150);
        addTableColumn("Cancelled Orders", "canceledOrders", 150);
        addTableColumn("Delivered Orders", "deliveredOrders", 150);
        addActionColumn();
    }

    /**
     * Adds a data column to the table view.
     *
     * @param title   Column header text
     * @param jsonKey JSON key to extract data from
     * @param width   Preferred column width in pixels
     */
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

    /**
     * Adds an action column with Details and Delete buttons for each row.
     */
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

    /**
     * Creates a styled button with the specified properties.
     */
    private Button createButton(String text, String styleClass, Runnable onAction) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(e -> onAction.run());
        return button;
    }

    /**
     * Fetches user data from the backend API asynchronously.
     */
    private void fetchUserData() {
        Request request = new Request.Builder()
                .url(BASE_URL + "/allUsers")
                .get()
                .build();

        executeAsyncRequest(request, this::handleFetchResponse);
    }

    /**
     * Handles the response from the user data fetch request.
     */
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

    /**
     * Displays user details in an information alert.
     *
     * @param user JSONObject containing user data
     */
    private void showUserDetails(JSONObject user) {
        String message = String.format(
                "Username: %s\nTotal Orders: %d\nPending Orders: %d\nCancelled Orders: %d\nDelivered Orders: %d",
                user.optString("username", "N/A"),
                user.optInt("totalOrders", 0),
                user.optInt("pendingOrders", 0),
                user.optInt("canceledOrders", 0),
                user.optInt("deliveredOrders", 0));
        showAlert(Alert.AlertType.INFORMATION, "User Details", message);
    }

    /**
     * Deletes a user after confirmation.
     *
     * @param user JSONObject containing user data to delete
     */
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

    /**
     * Confirms user deletion with a dialog.
     */
    private boolean confirmDelete(String username) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setContentText("Are you sure you want to delete " + username + "?");
        return confirmation.showAndWait()
                .orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Handles the response from the delete request.
     */
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

    /**
     * Executes an HTTP request asynchronously.
     */
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

    /**
     * Displays an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Logs error messages to the console.
     */
    private void logError(String message) {
        System.err.println(message);
    }

    private void logError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }

    @FunctionalInterface
    private interface ResponseHandler {
        void handle(Response response) throws IOException;
    }
}