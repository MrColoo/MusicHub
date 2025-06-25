package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Concerto;
import com.musicsheetsmanager.model.Utente;
import com.musicsheetsmanager.config.SessionManager;
import javafx.application.Platform;
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
    @FXML private ConcertoController concertoController;
    @FXML private EsploraConcertiController esploraConcertiController;
    @FXML private NavBarController navBarController;
    @FXML private CronologiaController cronologiaController;



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

    public BranoController getBranoController() {
        return branoController;
    }
    public ConcertoController getConcertoController() {
        return concertoController;
    }

    public void setEsploraController(EsploraController esploraController) {
        this.esploraController = esploraController;
        if (topBarController != null) {
            topBarController.setEsploraController(esploraController);
        }
    }

    public void setEsploraConcertiController(EsploraConcertiController esploraConcertiController) {
        this.esploraConcertiController = esploraConcertiController;
        if (topBarController != null) {
            topBarController.setEsploraConcertiController(esploraConcertiController);
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

            // riferimenti controller specifici
            if (controller instanceof EsploraController esplora) {
                this.esploraController = esplora;
            } else if (controller instanceof EsploraConcertiController esploraConcerti) {
                this.esploraConcertiController = esploraConcerti;
            } else if (controller instanceof TopBarController topBar) {
                this.topBarController = topBar;
            } else if (controller instanceof NavBarController navBar) {
                this.navBarController = navBar;
            } else if (controller instanceof BranoController branoController) {
                this.branoController = branoController;
            } else if (controller instanceof ConcertoController concertoController) {
                this.concertoController = concertoController;
            } else if (controller instanceof CronologiaController cronologiaController) {
                this.cronologiaController = cronologiaController;
            }

            // collegamenti controller
            if (topBarController != null) {
                topBarController.setMainController(this);
                if (esploraController != null) topBarController.setEsploraController(esploraController);
                if (esploraConcertiController != null) topBarController.setEsploraConcertiController(esploraConcertiController);
                if (navBarController != null) topBarController.setNavBarController(navBarController);
                if (cronologiaController != null) topBarController.setCronologiaController(cronologiaController);
            }

            if (esploraController != null && branoController != null) {
                esploraController.setBranoController(branoController);
            }

            if (esploraConcertiController != null && concertoController != null) {
                esploraConcertiController.setConcertoController(concertoController);
            }

            if (navBarController != null) {
                navBarController.setMainController(this);
            }

            pane.getChildren().setAll(content);

            // iniziallizza brani e concerti di esplora
            if (nomePagina.equals("Esplora") && esploraController != null) {
                esploraController.inizializzaBrani();
            }
            if (nomePagina.equals("EsploraConcerti") && esploraConcertiController != null) {
                esploraConcertiController.inizializzaConcerti();
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    // vai alla pagina "Brano"
    public void goToBrano(Node node, Brano brano, Runnable onPageReady) {
        node.setUserData(brano);    // associa brano alla card

        show("Brano");
        Platform.runLater(onPageReady);
    }

    // vai alla pagina "Concerto"
    public void goToConcerto(Node node, Concerto concerto, Runnable onPageReady) {
        if (node != null) {
            node.setUserData(concerto);
        }

        show("Concerto");
        Platform.runLater(onPageReady);
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