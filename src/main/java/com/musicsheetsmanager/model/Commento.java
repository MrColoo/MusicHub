package com.musicsheetsmanager.model;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Commento {
    private String idCommento;
    private String testo;
    private String username;
    private List<Commento> risposte;
    private boolean isNota;

    public Commento() {};

    public Commento(String testo, String username, boolean isNota) {
        this.idCommento = UUID.randomUUID().toString();     // genera id alfanumerico casuale
        this.testo = testo;
        this.username = username;
        this.risposte = new ArrayList<>();
        this.isNota = isNota;
    }

    public void aggiungiRisposta(Commento risposta) {
        risposte.add(risposta);
    }

    // Getter e Setter

    public void setRisposte(List<Commento> risposte) {
        this.risposte = risposte;
    }

    public String getIdCommento() {
        return idCommento;
    }

    public String getTesto() {
        return testo;
    }

    public String getUsername() {
        return username;
    }

    public List<Commento> getRisposte() {
        return risposte;
    }

    public boolean isNota() {
        return isNota;
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
