package com;

import java.io.IOException;
import javafx.fxml.FXML;

public class NavbarController {

    @FXML
    private void switchToHome() throws IOException {
        App.setRoot("home");
    }

    @FXML
    private void switchToContactUs() throws IOException {
        App.setRoot("contactUs");
    }

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }

}