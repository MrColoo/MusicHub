package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HomeController {
    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    public void initialize() {
        // If we're already authenticated, hide login/register buttons
        if (ViewNavigator.isAuthenticated()) {
            loginButton.setVisible(false);
            registerButton.setVisible(false);
        }
    }

    @FXML
    private void handleLogin() {
        ViewNavigator.navigateToLogin();
    }

    @FXML
    private void handleRegister() {
        ViewNavigator.navigateToRegister();
    }

    @FXML
    private void handleDashboard() {
        ViewNavigator.navigateToDashboard();
    }
}
