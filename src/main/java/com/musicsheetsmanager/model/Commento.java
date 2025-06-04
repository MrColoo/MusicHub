package com.musicsheetsmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Commento {
    private String idCommento;
    private String testo;
    private Utente autore;
    private List<Commento> risposte;

    public Commento() {};

    public Commento(String idCommento, String testo, Utente autore) {
        this.idCommento = idCommento;
        this.testo = testo;
        this.autore = autore;
        this.risposte = new ArrayList<>();
    }

    public void aggiungiRisposta(Commento risposta) {
        risposte.add(risposta);
    }

    // Getter e Setter

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
}
