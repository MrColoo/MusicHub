package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.Main;
import com.musicsheetsmanager.model.User;
import com.musicsheetsmanager.model.UserRepository;
import com.musicsheetsmanager.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

/**
 * Controller for the profile view.
 * Handles displaying user information and updating the user's password.
 */
public class ProfileController {
    @FXML
    private Label usernameLabel;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    private UserRepository userRepository;
    private String currentUsername;

    /**
     * Initialize the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        userRepository = Main.getUserRepository();
        currentUsername = ViewNavigator.getAuthenticatedUser();

        // Display the current username
        usernameLabel.setText(currentUsername);

        // Hide the status label initially
        statusLabel.setVisible(false);
    }

    /**
     * Handle updating the user's password.
     * This method is called when the user clicks the "Update Password" button.
     */
    @FXML
    private void handleUpdatePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill out all password fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Update the user with the new password
        User currentUser = userRepository.getUser(currentUsername);
        User updatedUser = new User(currentUsername, newPassword, currentUser.isAdmin());
        userRepository.saveUser(updatedUser);

        showSuccess("Password updated successfully");

        // Clear fields
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * Handle navigating back to the dashboard.
     * This method is called when the user clicks the "Back to Dashboard" button.
     */
    @FXML
    private void handleBackToDashboard() {
        ViewNavigator.navigateToDashboard();
    }

    /**
     * Show an error message in the status label.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setVisible(true);
    }

    /**
     * Show a success message in the status label.
     *
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setVisible(true);
    }
}
