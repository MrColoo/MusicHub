package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TopBarController {

    @FXML
    private Button mainButton; // "Accedi" o "Carica Brano"

    @FXML private TextField campoRicerca;
    @FXML private ListView<String> listaRisultati;  // brani trovati

    private List<Brano> brani;

    @FXML
    public void initialize(){
        // carica json
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = getClass().getClassLoader().getResourceAsStream("brani.json");
            // mappa brani.json su una classe java
            brani = mapper.readValue(input, new TypeReference<List<Brano>>() {});
        } catch (Exception e){
            e.printStackTrace();
        }

        campoRicerca.setOnAction(event -> onSearchBarEnter());
    }


    /*public void setUser(User user) {
        if (user == null) {
            mainButton.setText("Accedi");
        } else {
            mainButton.setText("Carica Brano");
        }
    }*/

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

