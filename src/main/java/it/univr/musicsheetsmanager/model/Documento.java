package it.univr.musicsheetsmanager.model;

public class Documento {
    public enum Tipo {
        SPARTITO, TESTO, ACCORDI, MP3, MP4, MIDI, YOUTUBE, ALTRO
    }

    private String nomeFile;
    private Tipo tipo;
    private String percorso; // può essere un path locale o un link

    public Documento(String nomeFile, Tipo tipo, String percorso) {
        this.nomeFile = nomeFile;
        this.tipo = tipo;
        this.percorso = percorso;
    }

    // Getter e Setter
}
