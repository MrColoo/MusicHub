package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class TopBarController {

    @FXML
    private Button mainButton; // "Accedi" o "Carica Brano"

    @FXML private TextField campoRicerca;
    @FXML private ListView<String> listaRisultati;  // brani trovati

    private List<Brano> brani;

    @FXML
    public void initialize(){

        caricaBrani();
        campoRicerca.setOnAction(event -> onSearchBarEnter());
    }


    /*public void setUser(User user) {
        if (user == null) {
            mainButton.setText("Accedi");
        } else {
            mainButton.setText("Carica Brano");
        }
    }*/

    public void caricaBrani () {
        try {
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream("src/main/resources/com/musicsheetsmanager/data/brani.json")
            );
            Type listType = new TypeToken<List<Brano>>() {}.getType();
            brani = gson.fromJson(reader, listType);

            reader.close();
        } catch (Exception e){
            e.printStackTrace();
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

