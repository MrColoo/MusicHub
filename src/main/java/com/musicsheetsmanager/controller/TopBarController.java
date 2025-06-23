package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TopBarController implements Controller{

    @FXML private Button mainButton; // "Accedi" o "Carica Brano"
    @FXML private Button caricaConcertoBtn;
    @FXML private HBox searchBar; // Box campo di ricerca
    @FXML private TextField campoRicerca;

    private EsploraController esploraController;

    private List<Brano> risultatiRircercaBrani;
    private List<String> risultatiRircercaCatalogo;

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    private static final Path DIZIONARIO_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEsploraController(EsploraController esploraController) {
        this.esploraController = esploraController;
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
            caricaConcertoBtn.setOnAction(event -> mainController.show("CaricaConcerto"));
        }
    }

    // restituisce una lista con i brani trovati
    @FXML
    private void onSearchBarEnter (){
        String viewTypeText = esploraController.getViewType();
        String chiave = campoRicerca.getText();

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
    }
}

