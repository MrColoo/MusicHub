// src/main/java/com/musicsheetsmanager/model/Utente.java
package com.musicsheetsmanager.model;

public class Utente {
    private String email;
    private String username;
    private String password;

    // Costruttore di default (richiesto da Gson)
    public Utente() { }

    // Costruttore con username
    public Utente(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
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
}
