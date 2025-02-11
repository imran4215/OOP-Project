package com;

import java.io.IOException;

import javafx.fxml.FXML;

public class LoginController {

    @FXML
    private void switchToSignup() throws IOException {
        App.setRoot("signup");
    }
}
