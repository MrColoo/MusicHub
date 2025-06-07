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

    @FXML private TopBarController topBarController;
    @FXML private EsploraController esploraController;
    @FXML private BranoController branoController;


    private Utente currentUser;


    public void initialize() {
        if (!SessionManager.isLoggedIn()) {
            show("Login");
            showTopBar();
            return;
        }

        topBarController.setMainController(this);
        topBarController.setEsploraController(esploraController);

        showNavBar();
    }

    public BranoController getBranoFileController() {
        return branoController;
    }

    public void setEsploraController(EsploraController esploraController) {
        this.esploraController = esploraController;
        if (topBarController != null) {
            topBarController.setEsploraController(esploraController);
        }
    }

    public void setBranoFileController(BranoController branoController) {
        this.branoController = branoController;
        if (esploraController != null) {
            esploraController.setBranoFileController(branoController);
        }
    }

    // Funzione generale per caricare una pagina FXML
    private void loadContent(String nomePagina, StackPane pane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/musicsheetsmanager/fxml/" + nomePagina + ".fxml"));
            Node content = loader.load();
            Object controller = loader.getController();

            if (controller instanceof Controller) {
                ((Controller) controller).setMainController(this);
            }

            if (controller instanceof EsploraController) {
                this.esploraController = (EsploraController) controller;
                if (topBarController != null) {
                    topBarController.setEsploraController(esploraController);
                }
            } else if (controller instanceof TopBarController) {
                this.topBarController = (TopBarController) controller;
                this.topBarController.setMainController(this);
                if (esploraController != null) {
                    this.topBarController.setEsploraController(esploraController);
                }
            } else if (controller instanceof BranoController) {
                this.branoController = (BranoController) controller;
                if (esploraController != null) {
                    esploraController.setBranoFileController(this.branoController);
                }
            }

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
    public void showNavBar() {
        loadContent("NavBar", navBarContainer);
    }

    // Nasconde NavBar
    public void hideNavBar() {
        navBarContainer.getChildren().clear();
    }

    // Ricarica TopBar
    public void reloadTopBar() {
        navBarContainer.getChildren().clear();
        loadContent("TopBar", topBarContainer);
    }

}