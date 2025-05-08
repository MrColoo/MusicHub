package com.musicsheetsmanager.model;

public class Utente {
    public enum Ruolo { ADMIN, NORMALE }

    private String username;
    private Ruolo ruolo;
    private boolean isAutore;
    private boolean isInterprete;

    public Utente(String username, Ruolo ruolo) {
        this.username = username;
        this.ruolo = ruolo;
    }

    // Getter e Setter
}
