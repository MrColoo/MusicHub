package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.Main;
import com.musicsheetsmanager.model.User;
import com.musicsheetsmanager.model.UserRepository;
import com.musicsheetsmanager.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    private UserRepository userRepository;

    @FXML
    public void initialize() {
        userRepository = Main.getUserRepository();
        statusLabel.setVisible(false);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill out all fields");
            return;
        }

        if (userRepository.usernameExists(username)) {
            showError("Username already exists");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Create and save new user
        User newUser = User.create(username, password);
        userRepository.saveUser(newUser);

        // Show success message
        showSuccess("Registration successful! Please log in.");

        // Clear fields
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleBackToLogin() {
        ViewNavigator.navigateToLogin();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setVisible(true);
    }
}
