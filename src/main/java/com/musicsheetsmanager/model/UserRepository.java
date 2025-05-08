package com.musicsheetsmanager.model;

import com.musicsheetsmanager.config.AppConfig;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private Map<String, User> users = new HashMap<>();
    private final String csvFilePath;

    public UserRepository() {
        this(AppConfig.USERS_CSV_PATH);
    }

    public UserRepository(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        ensureCsvExists();
        loadUsers();
    }

    /**
     * Make sure the CSV file exists, create it if needed
     */
    private void ensureCsvExists() {
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists()) {
            try {
                // Create parent directories if they don't exist
                File parentDir = csvFile.getParentFile();
                if (parentDir != null) {
                    parentDir.mkdirs();
                }

                // Create the file and write headers
                try (FileWriter writer = new FileWriter(csvFile)) {
                    writer.write("username,password,is_admin\n");
                    writer.write("admin,admin,1\n");
                }
            } catch (IOException e) {
                System.err.println("Error creating users CSV file: " + e.getMessage());
            }
        }
    }

    /**
     * Load users from the CSV file
     */
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            // Skip header line
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String username = parts[0];
                    String password = parts[1];
                    boolean isAdmin = parts[2].equals("1");
                    users.put(username, new User(username, password, isAdmin));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users from CSV: " + e.getMessage());
        }
    }

    /**
     * Save all users to the CSV file
     */
    private void saveAllUsers() {
        String isAdmin;
        try (FileWriter writer = new FileWriter(csvFilePath)) {
            // Write header
            writer.write("username,password,is_admin\n");

            // Write users
            for (User user : users.values()) {
                isAdmin = "0";
                if (user.isAdmin()) {
                    isAdmin = "1";
                }
                writer.write(user.getUsername() + "," + user.getPassword() + "," + isAdmin +  "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving users to CSV: " + e.getMessage());
        }
    }

    /**
     * Save a user to the repository
     */
    public void saveUser(User user) {
        users.put(user.getUsername(), user);
        saveAllUsers();
    }

    /**
     * Delete a user from the repository
     */
    public void deleteUser(String username) {
        if (users.containsKey(username)) {
            User user = users.get(username);
            // Never delete admin users as a safety measure
            if (!user.isAdmin()) {
                users.remove(username);
                saveAllUsers();
            }
        }
    }

    /**
     * Get a user by username
     */
    public User getUser(String username) {
        return users.get(username);
    }

    /**
     * Check if a username exists
     */
    public boolean usernameExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Get all users
     */
    public Map<String, User> getAllUsers() {
        return new HashMap<>(users);
    }
}
