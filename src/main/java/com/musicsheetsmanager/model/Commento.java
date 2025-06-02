package com.musicsheetsmanager.model;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Commento {
    private String idCommento;
    private String testo;
    private Utente autore;
    private List<Commento> risposte;

    public Commento() {};

    public Commento(String testo, Utente autore) {
        this.idCommento = UUID.randomUUID().toString();     // genera id alfanumerico casuale
        this.testo = testo;
        this.autore = autore;
        this.risposte = new ArrayList<>();
    }

    public void aggiungiRisposta(Commento risposta) {
        risposte.add(risposta);
    }

    // Getter e Setter

    public String getIdCommento() {
        return idCommento;
    }

    public String getTesto() {
        return testo;
    }

    public Utente getAutore() {
        return autore;
    }

    public List<Commento> getRisposte() {
        return risposte;
    }

    public String toString(){
        return testo;
    }

    // aggiunge l'id del commento al brano corrispondente
    public static void linkIdcommentoBrano (String idBrano, String idCommento, Path BRANI_JSON_PATH){
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        // cerca il brano e aggiunge l'id del commento
        for (Brano b: listaBrani) {
            if (b.getIdBrano().equals(idBrano)) {
                b.aggiungiCommento(idCommento);
                break;
            }
        }

        JsonUtils.scriviSuJson(listaBrani, BRANI_JSON_PATH);
    }
}
