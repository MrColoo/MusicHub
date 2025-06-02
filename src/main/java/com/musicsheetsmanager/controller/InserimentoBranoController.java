package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import javafx.scene.text.Text;

import java.lang.reflect.Type;

public class InserimentoBranoController {

    @FXML private TextField campoTitolo;
    @FXML private TextField campoAutori;
    @FXML private TextField campoAnnoDiComposizione;
    @FXML private TextField campoGenere;
    @FXML private TextField campoStrumentiMusicali;
    @FXML private TextField campoLinkYoutube;
    @FXML private Text errore;

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    public void initialize(){
        errore.setVisible(false);
    }

    // form per l'aggiunta di un nuovo brano da parte dell'utente
    @FXML
    public void onAddBranoClick() {
        String titolo = campoTitolo.getText().trim();

        List<String> autori = List.of(campoAutori.getText().trim().split(",\\s*"));

        // controlla che l'anno di composizione sia un numero intero valido
        int anno;
        try {
            anno = Integer.parseInt(campoAnnoDiComposizione.getText().trim());
            int annoCorrente = Year.now().getValue();
            if(anno > annoCorrente) {
                errore.setText("Siamo ancora nel " + annoCorrente);
                errore.setVisible(true);
                return;
            }
        } catch (NumberFormatException e) {
            errore.setText("Inserisci un numero intero");
            errore.setVisible(true);
            return;
        }

        String genere = campoGenere.getText();

        String strumentiText = campoStrumentiMusicali.getText().trim();
        List<String> strumenti = strumentiText.isEmpty()
                ? new ArrayList<>()
                : List.of(strumentiText.split(",\\s*"));

        String linkYoutube = campoLinkYoutube.getText().trim();
        if(linkYoutube.isEmpty()) {
            linkYoutube = "";
        }

        Brano nuovoBrano = new Brano(titolo, autori, genere, anno, linkYoutube, strumenti);

        Type branoType = new TypeToken<List<Brano>>() {}.getType();

        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        listaBrani.add(nuovoBrano);

        JsonUtils.scriviSuJson(listaBrani, BRANI_JSON_PATH);
    }
}
