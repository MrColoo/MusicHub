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
import java.lang.reflect.Type;

public class InserimentoBranoController {

    @FXML private TextField campoTitolo;
    @FXML private TextField campoAutori;
    @FXML private TextField campoAnnoDiComposizione;
    @FXML private TextField campoGenere;
    @FXML private TextField campoEsecutori;

    // form per l'aggiunta di un nuovo brano da parte dell'utente
    @FXML
    public void onAddBranoClick() {

            String titolo = campoTitolo.getText();
            List<String> autori = List.of(campoAutori.getText().split(",\\s*"));

            // controlla che l'anno di composizione sia un numero intero valido
            int anno;
            try {
                anno = Integer.parseInt(campoAnnoDiComposizione.getText());
                int annoCorrente = Year.now().getValue();

                if(anno > annoCorrente) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore input");
                    alert.setHeaderText("Anno troppo grande");
                    alert.setContentText("Non puoi viaggiare nel futuro");
                    alert.showAndWait();
                    return;
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore di input");
                alert.setHeaderText("Anno non valido");
                alert.setContentText("L'anno deve essere un numero intero");
                alert.showAndWait();
                return;
            }

            String genere = campoGenere.getText();
            List<String> esecutori = List.of(campoEsecutori.getText().split(",\\s*"));

            Brano nuovoBrano = new Brano(titolo, autori, genere, anno, esecutori);

            List<Brano> listaBrani = caricaBrani();

            listaBrani.add(nuovoBrano);

            salvaBrani(listaBrani);
    }


    // carica i brani dal file json in una lista e aggiunge il nuovo brano
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
