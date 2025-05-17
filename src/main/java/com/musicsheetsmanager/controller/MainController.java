package com.musicsheetsmanager.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.io.IOException;


public class MainController {

    @FXML private StackPane topBarContainer;
    @FXML private StackPane navBarContainer;

    public void initialize() {
        loadTopBar();
        loadNavBar();
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
}
