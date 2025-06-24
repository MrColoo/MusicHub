package com.musicsheetsmanager.model;

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
}
