package it.univr.musicsheetsmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Commento {
    private String testo;
    private Utente autore;
    private List<Commento> risposte;

    public Commento(String testo, Utente autore) {
        this.testo = testo;
        this.autore = autore;
        this.risposte = new ArrayList<>();
    }

    public void aggiungiRisposta(Commento risposta) {
        risposte.add(risposta);
    }

    // Getter e Setter
}
