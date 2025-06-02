package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Commento;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.musicsheetsmanager.config.SessionManager.getLoggedUser;

public class InserimentoCommentoController {

    @FXML
    private Text idBrano;

    @FXML
    private Text errore;

    private static final Path COMMENTI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "commenti.json"
    );

     private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    @FXML
    private TextField campoCommento;

    public void initialize(){
        errore.setVisible(false);
    }

    // funzione per salvare un commento generico
    private void aggiungiCommento(String testo, boolean isNota) {
        // controllo se commento è vuoto
        if(testo.isBlank()) {
            errore.setText("Il testo non può essere vuoto");
            errore.setVisible(true);
            return;
        }

        Commento nuovoCommento = new Commento(testo, getLoggedUser(), isNota);

        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> listaCommenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        listaCommenti.add(nuovoCommento);

        JsonUtils.scriviSuJson(listaCommenti, COMMENTI_JSON_PATH);

        Commento.linkIdcommentoBrano(idBrano.toString(), nuovoCommento.getIdCommento(), BRANI_JSON_PATH);
    }

    @FXML
    public void OnAddCommentoClick(){
        String testoCommento = campoCommento.getText().trim();
        aggiungiCommento(testoCommento, false);
    }

    @FXML
    public void OnAddNotaClick(){
        String testoNota = campoCommento.getText().trim();
        aggiungiCommento(testoNota, true);
    }
}
