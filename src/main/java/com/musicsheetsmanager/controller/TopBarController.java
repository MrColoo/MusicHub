package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TopBarController implements Controller{

    @FXML
    private Button mainButton; // "Accedi" o "Carica Brano"
    @FXML private HBox searchBar; // Box campo di ricerca
    @FXML private TextField campoRicerca;
    @FXML private ListView<String> listaRisultati;  // brani trovati

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
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
        }else{
            searchBar.setVisible(true);
            searchBar.setManaged(true);
            mainButton.setText("Carica Brano");
        }
    }

    // restituisce una lista con i brani trovati
    @FXML
    public void onSearchBarEnter (){
        String chiave = campoRicerca.getText();

        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        List<Brano> risultati = Brano.cerca(listaBrani, chiave);
        listaRisultati.getItems().clear();

        if(risultati.isEmpty()){
            listaRisultati.getItems().add("Nessun brano trovato con i criteri specificati.");
        } else {
            listaRisultati.getItems().addAll(risultati.stream()
                    .map(Brano::toString)
                    .toList());
        }
    }

}

