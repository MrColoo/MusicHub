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
            showLogin();
            loadTopBar();
            return;
        }

        loadNavBar();
    }

    private void loadContent(String fxmlPath, StackPane pane) {
        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            pane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMainContent(String fxmlPath){
        loadContent(fxmlPath, mainContentPane);
    }

    private void loadTopBar() {
        loadContent("/com/musicsheetsmanager/fxml/TopBar.fxml", topBarContainer);
    }

    private void loadNavBar() {
        loadContent("/com/musicsheetsmanager/fxml/NavBar.fxml", navBarContainer);
    }

    public void showCaricaBrano() {
        loadMainContent("/com/musicsheetsmanager/fxml/CaricaBrano.fxml");
    }

    public void showLogin() {
        loadMainContent("/com/musicsheetsmanager/fxml/Login.fxml");
    }

}
