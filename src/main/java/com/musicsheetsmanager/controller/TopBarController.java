package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class TopBarController {

    @FXML private Button mainButton; // "Accedi" o "Carica Brano"
    @FXML private HBox searchBar; // Box campo di ricerca
    @FXML private TextField campoRicerca;
    @FXML private ListView<String> listaRisultati;  // brani trovati

    private List<Brano> brani;

    @FXML
    public void initialize(){

        campoRicerca.setOnAction(event -> onSearchBarEnter());

        changeTopBar();
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

    @FXML
    public void onSearchBarEnter (){
        String chiave = campoRicerca.getText();
        List<Brano> risultati = cerca(brani, chiave);
        listaRisultati.getItems().clear();

        if(risultati.isEmpty()){
            listaRisultati.getItems().add("Nessun brano trovato con i criteri specificati.");
        } else {
            listaRisultati.getItems().addAll(risultati.stream()
                    .map(Brano::toString)
                    .toList());
        }
    }

    // ricerca per titolo-autori-esecutori
    public static List<Brano> cerca (List<Brano> brani, String chiave){
        if(chiave == null || chiave.isBlank()) return brani;

        String key = chiave.toLowerCase();

        return brani.stream()
                .filter(b ->
                        (b.getTitolo() != null && b.getTitolo().toLowerCase().contains(key)) ||
                                (b.getAutori() != null && b.getAutori().stream()
                                        .anyMatch(e -> e.toLowerCase().contains(key))) ||
                                (b.getEsecutori() != null && b.getEsecutori().stream()
                                        .anyMatch(e -> e.toLowerCase().contains(key)))
                )
                .collect(Collectors.toList());
    }
}

