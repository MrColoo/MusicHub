package com.musicsheetsmanager.model;

import java.util.List;

public class BranoAssegnatoAlConcerto extends Brano {
    private String idConcerto;
    private String inizio;
    private String fine;

    public BranoAssegnatoAlConcerto(
            String idConcerto,
            String inizio,
            String fine,
            String proprietario,
            String idBrano,
            String titolo,
            List<String> autori,
            List<String> generi,
            Integer annoComposizione,
            List<String> esecutori,
            String linkYoutube,
            List<String> strumentiMusicali
    ) {
        super(proprietario, idBrano, titolo, autori, generi, annoComposizione, esecutori, linkYoutube, strumentiMusicali);
        this.idConcerto = idConcerto;
        this.inizio = inizio;
        this.fine = fine;
    }

    // Getter specifici
    public String getIdConcerto() {
        return idConcerto;
    }

    public String getInizio() {
        return inizio;
    }

    public String getFine() {
        return fine;
    }
}
