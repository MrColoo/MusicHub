package com.musicsheetsmanager.view.components;

import com.musicsheetsmanager.view.ViewNavigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class NavBar extends VBox {
    private boolean isAuthenticated;
    private String username;

    public NavBar() {
        this(false, null);
    }

    public NavBar(boolean isAuthenticated, String username) {
        this.isAuthenticated = isAuthenticated;
        this.username = username;
        initialize();
    }

    /**
     * Initialize the navigation bar
     */
    private void initialize() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: #007bff;");

        Label brandLabel = new Label("Dash App");
        brandLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        this.getChildren().add(brandLabel);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        this.getChildren().add(spacer);

        // Create buttons based on authentication status
        if (isAuthenticated) {
            createAuthenticatedNavButtons();
        } else {
            createUnauthenticatedNavButtons();
        }
    }

    /**
     * Create navigation buttons for authenticated users
     */
    private void createAuthenticatedNavButtons() {
        Button homeBtn = createNavButton("Home", e -> ViewNavigator.navigateToHome());
        Button dashboardBtn = createNavButton("Dashboard", e -> ViewNavigator.navigateToDashboard());
        Button profileBtn = createNavButton("Profile", e -> ViewNavigator.navigateToProfile());

        Label userLabel = new Label("Hello, " + username);
        userLabel.setStyle("-fx-text-fill: white;");

        Button logoutBtn = createNavButton("Logout", e -> ViewNavigator.logout());

        this.getChildren().addAll(homeBtn, dashboardBtn, profileBtn, userLabel, logoutBtn);
    }

    /**
     * Create navigation buttons for unauthenticated users
     */
    private void createUnauthenticatedNavButtons() {
        Button homeBtn = createNavButton("Home", e -> ViewNavigator.navigateToHome());
        Button loginBtn = createNavButton("Login", e -> ViewNavigator.navigateToLogin());
        Button registerBtn = createNavButton("Register", e -> ViewNavigator.navigateToRegister());

        this.getChildren().addAll(homeBtn, loginBtn, registerBtn);
    }

    /**
     * Create a styled navigation button
     */
    private Button createNavButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        button.setOnAction(handler);

        // Hover effect
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #0069d9; -fx-text-fill: white; -fx-cursor: hand;"));
        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;"));

        return button;
    }

    /**
     * Update the navigation bar based on authentication status
     */
    public void updateAuthStatus(boolean isAuthenticated, String username) {
        this.isAuthenticated = isAuthenticated;
        this.username = username;
        this.getChildren().clear();
        initialize();
    }
}
