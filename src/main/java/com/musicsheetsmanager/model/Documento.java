package com.musicsheetsmanager.model;

public class Documento {
    private String username;
    private String nomeFile;
    private String percorso; // può essere un path locale o un link

    public Documento(String username, String nomeFile, String percorso) {
        this.username = username;
        this.nomeFile = nomeFile;
        this.percorso = percorso;
    }

    // Getter e Setter

    public String getNomeFile() {
        return nomeFile;
    }

    public String getPercorso() {
        return percorso;
    }

    public void setPercorso(String percorso) {
        this.percorso = percorso;
    }

    public String toString() {
        return nomeFile + ", " + "path: " + percorso;
    }
}
