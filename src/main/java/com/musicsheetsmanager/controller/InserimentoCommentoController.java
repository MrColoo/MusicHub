package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Commento;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.musicsheetsmanager.config.SessionManager.getLoggedUser;

public class InserimentoCommentoController {

    @FXML
    private TextField campoCommento;

    @FXML
    public void OnAddCommentoClick(){
        String testoCommento = campoCommento.getText().trim();

        String idNuovoCommento = UUID.randomUUID().toString();  // genera id alfanumerico casuale

        Commento nuovoCommento = new Commento(idNuovoCommento, testoCommento, getLoggedUser() );

        List<Commento> listaCommenti = caricaCommenti();
        listaCommenti.add(nuovoCommento);
        salvaCommenti(listaCommenti);
    }

    private List<Commento> caricaCommenti() {
        try (Reader reader = new FileReader("src/main/resources/com/musicsheetsmanager/data/commenti.json")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Commento>>() {}.getType();
            List<Commento> commenti = gson.fromJson(reader, listType);
            if(commenti == null){
                return new ArrayList<>();
            }
            return commenti;
        } catch (FileNotFoundException e){
            return new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void salvaCommenti(List<Commento> commenti) {
        try (Writer writer = new FileWriter("src/main/resources/com/musicsheetsmanager/data/commenti.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(commenti, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
