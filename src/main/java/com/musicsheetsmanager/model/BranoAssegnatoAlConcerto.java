package com.musicsheetsmanager.model;

public class BranoAssegnatoAlConcerto {
    private String idConcerto;
    private String idBrano;
    private String inizio;
    private String fine;

    public BranoAssegnatoAlConcerto(String idConcerto, String idBrano, String inizio, String fine) {
        this.idConcerto = idConcerto;
        this.idBrano = idBrano;
        this.inizio = inizio;
        this.fine = fine;
    }
}

