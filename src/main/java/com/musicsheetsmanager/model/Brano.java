package com.musicsheetsmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Brano {
    private String titolo;
    private List<String> autori;
    private String genere;
    private Integer annoComposizione;
    private List<String> esecutori;

    private List<Documento> documenti;
    private List<Commento> commenti;

    public Brano(String titolo, List<String> autori, String genere, Integer annoComposizione) {
        this.titolo = titolo;
        this.autori = autori;
        this.genere = genere;
        this.annoComposizione = annoComposizione;
        this.esecutori = esecutori;

        this.documenti = new ArrayList<>();
        this.commenti = new ArrayList<>();
    }

    // Getter e Setter

    public String getTitolo() {
        return titolo;
    }

    public List<String> getAutori() {
        return autori;
    }

    public String getGenere() {
        return genere;
    }

    public List<String> getEsecutori() {
        return esecutori;
    }

    public Integer getAnnoComposizione() {
        return annoComposizione;
    }

    public List<Documento> getDocumenti() {
        return documenti;
    }

    public List<Commento> getCommenti() {
        return commenti;
    }

    public String toString() {
        return titolo +
                " - " +
                String.join(", ", autori) +
                " (" +
                genere +
                "), eseguito da: " +
                String.join(", ", esecutori);
    }
}
