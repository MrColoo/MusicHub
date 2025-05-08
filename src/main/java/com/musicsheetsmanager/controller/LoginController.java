package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.Main;
import com.musicsheetsmanager.model.User;
import com.musicsheetsmanager.model.UserRepository;
import com.musicsheetsmanager.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private UserRepository userRepository;

    @FXML
    public void initialize() {
        userRepository = Main.getUserRepository();
        statusLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        User user = userRepository.getUser(username);
        if (user != null && user.checkPassword(password)) {
            // Login successful
            ViewNavigator.setAuthenticatedUser(username);
            ViewNavigator.navigateToDashboard();
        } else {
            showError("Invalid username or password");
        }
    }

    @FXML
    private void handleRegister() {
        ViewNavigator.navigateToRegister();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setVisible(true);
    }
}
