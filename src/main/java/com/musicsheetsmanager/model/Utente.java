package com.musicsheetsmanager.model;

public class Utente {
    private String email;
    private String username;
    private String password;
    private boolean approvazione;

    // Costruttore di default (richiesto da Gson)
    public Utente() {}

    // Costruttore completo
    public Utente(String email, String username, String password, boolean approvazione) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.approvazione = approvazione;
    }

    // Getter e Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getApprovazione() {
        return approvazione;
    }

    public void setApprovazione(boolean approvazione) {
        this.approvazione = approvazione;
    }
}
