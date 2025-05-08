package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import com.musicsheetsmanager.model.User;
import com.musicsheetsmanager.model.UserRepository;
import com.musicsheetsmanager.view.ViewNavigator;
import com.musicsheetsmanager.view.components.UserListHeader;
import com.musicsheetsmanager.view.components.UserListRow;

import java.util.Map;

public class StatsController {

    @FXML
    private Label statsLabel;

    @FXML
    private Label statsContentLabel;

    @FXML
    private VBox userManagementSection;

    @FXML
    private VBox userListContainer;

    private UserRepository userRepository;
    private String currentUsername;
    private User user;

    @FXML
    public void initialize() {
        userRepository = Main.getUserRepository();
        currentUsername = ViewNavigator.getAuthenticatedUser();
        user = userRepository.getUser(currentUsername);

        if (user.isAdmin()) {
            // For admin users, show the user management interface
            statsLabel.setText("Admin Dashboard");
            statsContentLabel.setText("Welcome to the admin dashboard. You can manage users below.");

            // Make user management section visible
            userManagementSection.setVisible(true);
            userManagementSection.setManaged(true);

            // Populate the user list
            populateUserList();
        } else {
            // For regular users, just show the "coming soon" message
            statsLabel.setText("Stats");
            statsContentLabel.setText("Coming soon...");
        }
    }

    /**
     * Populate the user list with non-admin users
     */

    /**
     * Populate the user list with non-admin users
     */
    /**
     * Populate the user list with non-admin users
     */
    private void populateUserList() {
        // Clear the container first
        userListContainer.getChildren().clear();

        // Add a header row
        UserListHeader headerRow = new UserListHeader();
        userListContainer.getChildren().add(headerRow);

        // Get all users
        Map<String, User> users = userRepository.getAllUsers();

        // Check if there are any non-admin users
        boolean hasNonAdminUsers = false;

        // Add a row for each non-admin user
        for (User user : users.values()) {
            // Skip admin users (they should not appear in this list)
            if (user.isAdmin()) {
                continue;
            }

            hasNonAdminUsers = true;

            // Create a user row component and add it to the container
            UserListRow userRow = new UserListRow(
                    user,
                    () -> handleDeleteUser(user.getUsername()),
                    () -> handleMakeAdmin(user.getUsername())
            );

            userListContainer.getChildren().add(userRow);
        }

        // If no non-admin users were found, add a message
        if (!hasNonAdminUsers) {
            Label noUsersLabel = new Label("No regular users found.");
            userListContainer.getChildren().add(noUsersLabel);
        }
    }

    /**
     * Handle making a user an admin
     */
    private void handleMakeAdmin(String username) {
        User userToPromote = userRepository.getUser(username);
        if (userToPromote != null) {
            // Create a new user with admin privileges
            User promotedUser = new User(username, userToPromote.getPassword(), true);
            userRepository.saveUser(promotedUser);

            // Refresh the user list
            populateUserList();
        }
    }

    /**
     * Handle deleting a user
     */
    private void handleDeleteUser(String username) {
        User userToDelete = userRepository.getUser(username);
        if (userToDelete != null && !userToDelete.isAdmin()) {
            // Delete the user
            userRepository.deleteUser(username);

            // Refresh the user list
            populateUserList();
        }
    }

    /**
     * Handle navigating back to the dashboard
     */
    @FXML
    private void handleBackToDashboard() {
        ViewNavigator.navigateToDashboard();
    }
}
