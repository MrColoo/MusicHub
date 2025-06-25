package com.musicsheetsmanager.model;

public class BranoAssegnatoAlConcerto {
    private String idConcerto;
    private String idBrano;
    private String inizio;
    private String fine;
    private String nomeUtente;

    public BranoAssegnatoAlConcerto(String idConcerto, String idBrano, String inizio, String fine, String nomeUtente) {
        this.idConcerto = idConcerto;
        this.idBrano = idBrano;
        this.inizio = inizio;
        this.fine = fine;
        this.nomeUtente = nomeUtente;
    }

    // Getter per tutti i campi
    public String getIdConcerto() {
        return idConcerto;
    }

    public String getIdBrano() {
        return idBrano;
    }

    public String getInizio() {
        return inizio;
    }

    public String getFine() {
        return fine;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }
}
