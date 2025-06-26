package com.musicsheetsmanager.model;

import java.util.List;
import java.util.stream.Collectors;

public class Concerto {
    private String titolo;
    private String id;
    private String link;
    private String creatore;

    public Concerto(String id, String link, String titolo, String creatore) {
        this.id = id;
        this.link = link;
        this.titolo = titolo;
        this.creatore = creatore;
    }

    public void setTitolo(String titolo) {this.titolo = titolo; }

    public String getTitolo() {return titolo;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    /**
     *  Ricerca dei concerti data una chiave
     *
     * @param concerti Lista dei concerti
     * @param chiave Chiave di ricerca
     *
     * @return Lista di concerti trovati
     */
    public static List<Concerto> cercaConcerti (List<Concerto> concerti, String chiave) {
        if(chiave == null || chiave.isBlank()) return concerti;

        String key = chiave.toLowerCase();
        return concerti.stream()
                .filter(b ->
                        (b.getTitolo() != null && b.getTitolo().toLowerCase().contains(key)))
                .collect(Collectors.toList());
    }
}
