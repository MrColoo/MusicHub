package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class DashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        // This is a protected view, so we should always have an authenticated user
        String username = ViewNavigator.getAuthenticatedUser();
        welcomeLabel.setText("Welcome to your dashboard, " + username + "!");
    }

    @FXML
    private void handleStatDetails() {
        ViewNavigator.navigateToStats();
    }


    @FXML
    private void handleViewProfile() {
        ViewNavigator.navigateToProfile();
    }

    @FXML
    private void handleLogout() {
        ViewNavigator.logout();
    }
}