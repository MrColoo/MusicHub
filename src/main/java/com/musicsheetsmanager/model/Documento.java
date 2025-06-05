package com.musicsheetsmanager.model;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;

public class Documento {
    public enum Tipo {
        SPARTITO, TESTO, ACCORDI, MP3, MP4, MIDI, YOUTUBE, ALTRO
    }

    private String username;
    private String nomeFile;
    private Tipo tipo;
    private String percorso; // può essere un path locale o un link

    public Documento(String username, String nomeFile, String percorso) {
        this.username = username;
        this.nomeFile = nomeFile;
        this.tipo = tipo;
        this.percorso = percorso;
    }


    // Getter e Setter

    public String getNomeFile() {
        return nomeFile;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getPercorso() {
        return percorso;
    }

    public void setNomeFile(String nomeFile) {
        this.nomeFile = nomeFile;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public void setPercorso(String percorso) {
        this.percorso = percorso;
    }
}
