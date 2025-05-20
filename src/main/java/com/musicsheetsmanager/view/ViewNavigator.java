package com.musicsheetsmanager.view;

import com.musicsheetsmanager.MainOld;
import com.musicsheetsmanager.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

/**
 * This class handles navigation between different views in the application.
 * It works as a bridge between controllers and views, allowing for simplified navigation.
 */
public class ViewNavigator {
    // Reference to the main controller
    private static MainController mainController;

    // Current authenticated username
    private static String authenticatedUser = null;

    /**
     * Set the main controller reference
     * @param controller The MainController instance
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    /**
     * Load and switch to a view
     * @param fxml The name of the FXML file to load
     */
    public static void loadView(String fxml) {
        try {
            URL fxmlUrl = MainOld.class.getResource("/com/musicsheetsmanager/fxml/" + fxml);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();
            //mainController.setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxml);
        }
    }

    /**
     * Navigate to the home view
     */
    public static void navigateToHome() {
        loadView("Home.fxml");
    }

    /**
     * Navigate to the login view
     */
    public static void navigateToLogin() {
        loadView("LoginView.fxml");
    }

    /**
     * Navigate to the register view
     */
    public static void navigateToRegister() {
        loadView("RegisterView.fxml");
    }

    /**
     * Navigate to the dashboard view (protected)
     * Will redirect to login if not authenticated
     */
    public static void navigateToDashboard() {
        if (isAuthenticated()) {
            loadView("DashboardView.fxml");
        } else {
            navigateToLogin();
        }
    }

    /**
     * Navigate to the profile view (protected)
     * Will redirect to login if not authenticated
     */
    public static void navigateToProfile() {
        if (isAuthenticated()) {
            loadView("ProfileView.fxml");
        } else {
            navigateToLogin();
        }
    }

    public static void navigateToStats() {
        if (isAuthenticated()) {
            loadView("StatsView.fxml");
        } else {
            navigateToLogin();
        }
    }

    /**
     * Set the authenticated user
     * @param username The username of the authenticated user
     */
    public static void setAuthenticatedUser(String username) {
        authenticatedUser = username;
        //mainController.updateNavBar(isAuthenticated());
    }

    /**
     * Get the authenticated user
     * @return The username of the authenticated user, or null if not authenticated
     */
    public static String getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * Check if a user is authenticated
     * @return true if a user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return authenticatedUser != null;
    }

    /**
     * Logout the current user
     */
    public static void logout() {
        authenticatedUser = null;
        //mainController.updateNavBar(false);
        navigateToHome();
    }
}