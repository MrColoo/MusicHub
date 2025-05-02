package it.univr.musicsheetsmanager.model;

public class Utente {
    public enum Ruolo { ADMIN, NORMALE }

    private String username;
    private Ruolo ruolo;
    private boolean èAutore;
    private boolean èInterprete;

    public Utente(String username, Ruolo ruolo) {
        this.username = username;
        this.ruolo = ruolo;
    }

    // Getter e Setter
}
