package it.univr.musicsheetsmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Brano {
    private String titolo;
    private List<String> autori;
    private String genere;
    private Integer annoComposizione;

    private List<Documento> documenti;
    private List<Commento> commenti;

    public Brano(String titolo, List<String> autori, String genere, Integer annoComposizione) {
        this.titolo = titolo;
        this.autori = autori;
        this.genere = genere;
        this.annoComposizione = annoComposizione;
        this.documenti = new ArrayList<>();
        this.commenti = new ArrayList<>();
    }

    // Getter e Setter
}
