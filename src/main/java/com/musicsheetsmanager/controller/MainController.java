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
            show("Login");
            showTopBar();
            return;
        }

        showNavBar();
    }

    // Funzione generale per caricare una pagina FXML
    private void loadContent(String nomePagina, StackPane pane) {
        try {
            Node content = FXMLLoader.load(getClass().getResource("/com/musicsheetsmanager/fxml/" + nomePagina + ".fxml"));
            pane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Funzione per caricare pagine FXML nella sezione MAIN
    public void show(String nomePagina){
        loadContent(nomePagina, mainContentPane);
    }

    // Carica TopBar
    private void showTopBar() {
        loadContent("TopBar", topBarContainer);
    }

    // Carica NavBar
    private void showNavBar() {
        loadContent("NavBar", navBarContainer);
    }

}
