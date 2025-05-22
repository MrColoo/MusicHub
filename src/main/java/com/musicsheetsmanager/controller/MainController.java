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
            show("Register");
            loadTopBar();
            return;
        }

        loadNavBar();
    }

    // Funzione generale per caricare una pagina FXML
    private void loadContent(String fxmlPath, StackPane pane) {
        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            pane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Funzione per caricare pagine FXML nella sezione MAIN
    private void show(String NomePagina){
        loadContent("/com/musicsheetsmanager/fxml/" + NomePagina + ".fxml", mainContentPane);
    }

    // Carica TopBar
    private void loadTopBar() {
        loadContent("/com/musicsheetsmanager/fxml/TopBar.fxml", topBarContainer);
    }

    // Carica NavBar
    private void loadNavBar() {
        loadContent("/com/musicsheetsmanager/fxml/NavBar.fxml", navBarContainer);
    }

}
