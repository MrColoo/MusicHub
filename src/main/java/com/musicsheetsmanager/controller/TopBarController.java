package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Concerto;

import com.musicsheetsmanager.controller.NavBarController;

import com.musicsheetsmanager.model.Concerto;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TopBarController implements Controller{

    @FXML private Button mainButton; // "Accedi" o "Carica Brano"
    @FXML private Button caricaConcertoBtn; // Bottone per caricare caricare un concerto
    @FXML private HBox searchBar; // Box campo di ricerca
    @FXML private TextField campoRicerca;

    private EsploraController esploraController;
    private EsploraConcertiController esploraConcertiController;

    private NavBarController navBarController;

    private List<Brano> risultatiRircercaBrani;
    private List<String> risultatiRircercaCatalogo;
    private List<Concerto> risultatiRicercaConcerti;

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    private static final Path CONCERTI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "concerti.json"
    );

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEsploraController(EsploraController esploraController) {
        this.esploraController = esploraController;
    }

    public void setNavBarController(NavBarController navBarController) {
        this.navBarController = navBarController;
    }

    public void setEsploraConcertiController(EsploraConcertiController esploraConcertiController) {
        this.esploraConcertiController = esploraConcertiController;
    }

    public void initialize(){
        campoRicerca.setOnAction(event -> onSearchBarEnter());

        changeTopBar(); // cambia aspetto topbar nel caso l'utente sia loggato o meno
    }

    private void changeTopBar() {
        if (!SessionManager.isLoggedIn()) {
            searchBar.setVisible(false);
            searchBar.setManaged(false);
            mainButton.setText("Accedi");
            mainButton.setOnAction(event -> mainController.show("Login"));
            caricaConcertoBtn.setVisible(false);
            caricaConcertoBtn.setManaged(false);
        }else{
            searchBar.setVisible(true);
            searchBar.setManaged(true);
            mainButton.setText("Carica Brano");
            mainButton.setOnAction(event -> mainController.show("CaricaBrano"));
            caricaConcertoBtn.setVisible(true);
            caricaConcertoBtn.setManaged(true);
            caricaConcertoBtn.setOnAction(event -> {
                mainController.show("CaricaConcerto");
            });
        }
    }

    // restituisce una lista con i brani trovati
    @FXML
    private void onSearchBarEnter (){
        String pagina = navBarController.getCurrentPage();
        String chiave = campoRicerca.getText();

        switch (pagina) {
            case "esploraBtn":
                String viewTypeText = esploraController.getViewType();

                if("esplora".equals(viewTypeText)) {        // se sono in esplora cerco i brani per nome e/o titolo
                    Type branoType = new TypeToken<List<Brano>>() {}.getType();
                    List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

                    risultatiRircercaBrani = Brano.cercaBrano(listaBrani, chiave);

                    esploraController.generaCatalogo(risultatiRircercaBrani, brano -> esploraController.creaCardBrano(brano, brano.getIdBrano()));
                } else {
                    Path DIZIONARIO_JSON_PATH = Paths.get( // percorso verso il file JSON
                            "src", "main", "resources",
                            "com", "musicsheetsmanager", "data", viewTypeText + ".json"
                    );

                    Type stringType = new TypeToken<List<String>>() {}.getType();
                    List<String> dizionario = JsonUtils.leggiDaJson(DIZIONARIO_JSON_PATH, stringType);

                    risultatiRircercaCatalogo = Brano.cercaCatalogo(dizionario, chiave);

                    esploraController.generaCatalogo(risultatiRircercaCatalogo, esploraController::creaCardCatalogo);
                }
                break;

            case "concertiBtn":
                Type concertoType = new TypeToken<List<Concerto>>() {}.getType();
                List<Concerto> listaConcerti = JsonUtils.leggiDaJson(CONCERTI_JSON_PATH, concertoType);

                risultatiRicercaConcerti = Concerto.cercaConcerti(listaConcerti, chiave);

                if (esploraConcertiController != null) {
                    esploraConcertiController.mostraCardConcerti(risultatiRicercaConcerti);
                }

                break;

            default:
                break;
        }

    }
}

