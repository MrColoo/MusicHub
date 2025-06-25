package com.musicsheetsmanager.model;

import java.util.List;
import java.util.stream.Collectors;

public class Concerto {
    private String titolo;
    private String id;
    private String link;

    public Concerto(String id, String link, String titolo) {
        this.id = id;
        this.link = link;
        this.titolo = titolo;
    }
    public void setTitolo(String titolo) {this.titolo = titolo; }

    public String getTitolo() {
        return titolo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public static List<Concerto> cercaConcerti (List<Concerto> concerti, String chiave) {
        if(chiave == null || chiave.isBlank()) return concerti;

        String key = chiave.toLowerCase();
        return concerti.stream()
                .filter(b ->
                        (b.getTitolo() != null && b.getTitolo().toLowerCase().contains(key)))
                .collect(Collectors.toList());
    }
}
