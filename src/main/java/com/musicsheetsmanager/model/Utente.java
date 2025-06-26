package com.musicsheetsmanager.model;

public class Utente {
    private String email;
    private String username;
    private String password;
    private boolean approved;
    private boolean admin;

    // Costruttore di default (richiesto da Gson)
    public Utente() { }

    // Costruttore con username
    public Utente(String email, String username, String password, boolean approved, boolean admin) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.approved = approved;
        this.admin = admin;
    }

    // Getter e Setter

    public String getEmail() {
        return email;
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

    public boolean isApproved(){
        return approved;
    }

    public void setApproved(boolean approved){
        this.approved = approved;
    }

    public boolean isAdmin() { return admin; }

    public void setAdmin(boolean admin) {  this.admin = admin; }
}


