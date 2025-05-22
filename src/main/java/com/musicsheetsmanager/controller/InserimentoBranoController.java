package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.musicsheetsmanager.model.Brano;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.*;
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
                    errore.setText("Non puoi viaggiare nel futuro coglione");
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

            List<Brano> listaBrani = caricaBrani();

            listaBrani.add(nuovoBrano);

            salvaBrani(listaBrani);
    }


    // carica i brani dal file json in una lista
    private List<Brano> caricaBrani() {
        try (Reader reader = new FileReader("src/main/resources/com/musicsheetsmanager/data/brani.json")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Brano>>() {}.getType();
            List<Brano> brani = gson.fromJson(reader, listType);
            if(brani == null){
                return new ArrayList<>();
            }
            return brani;
        } catch (FileNotFoundException e){
            return new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // salva la lista di brani aggiornata sul file json
    private void salvaBrani(List<Brano> brani) {
        try (Writer writer = new FileWriter("src/main/resources/com/musicsheetsmanager/data/brani.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(brani, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
