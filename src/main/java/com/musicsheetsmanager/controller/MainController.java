package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.model.Utente;
import com.musicsheetsmanager.config.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.io.IOException;


public class MainController {

    @FXML private StackPane topBarContainer;
    @FXML private StackPane navBarContainer;
    @FXML private StackPane mainContentPane;

    private Utente currentUser;


    public void initialize() {
        if (!SessionManager.isLoggedIn()) {
            showLoginPage();
            loadTopBar();
            return;
        }

        loadNavBar();
        loadDefaultPage();
    }

    private void loadTopBar() {
        try {
            Node topBar = FXMLLoader.load(getClass().getResource("/com/musicsheetsmanager/fxml/TopBar.fxml"));
            topBarContainer.getChildren().add(topBar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNavBar() {
        try {
            Node navBar = FXMLLoader.load(getClass().getResource("/com/musicsheetsmanager/fxml/NavBar.fxml"));
            navBarContainer.getChildren().add(navBar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeContent(String fxmlPath) {
        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainContentPane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultPage() {
        changeContent("/com/musicsheetsmanager/fxml/CaricaBrano.fxml"); // Es. pagina iniziale
    }

    private void showLoginPage() {
        try {
            Node login = FXMLLoader.load(getClass().getResource("/com/musicsheetsmanager/fxml/Login.fxml"));
            navBarContainer.getChildren().clear();
            mainContentPane.getChildren().setAll(login);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
