package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NavBarController {

    @FXML
    private Button adminButton;

    public void setUser(User user) {
        if (user == null || !user.isAdmin()) {
            adminButton.setVisible(false);
            adminButton.setManaged(false); // Nasconde completamente
        }
    }
}

