package com.musicsheetsmanager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Brano {
    private String titolo;
    private List<String> autori;
    private String genere;
    private Integer annoComposizione;
    private List<String> esecutori;
    private List<String> strumentiMusicali;
    private String linkYoutube;
    private String idBrano;


    private List<Documento> documenti;
    private List<String> idCommenti;

    public Brano(){};

    public Brano(String titolo, List<String> autori, String genere, Integer annoComposizione, String linkYoutube, List<String> strumentiMusicali) {
        this.idBrano = UUID.randomUUID().toString();     // genera id alfanumerico casuale
        this.titolo = titolo;
        this.autori = autori;
        this.genere = genere;
        this.annoComposizione = annoComposizione;
        this.linkYoutube = linkYoutube;
        this.strumentiMusicali = new ArrayList<>();

        this.documenti = new ArrayList<>();
        this.idCommenti = new ArrayList<>();
    }

    // Getter e Setter

    public String getTitolo() {
        return titolo;
    }

    public List<String> getAutori() {
        return autori;
    }

    public String getGenere() {
        return genere;
    }

    public List<String> getEsecutori() {
        return esecutori;
    }

    public Integer getAnnoComposizione() {
        return annoComposizione;
    }

    public List<Documento> getDocumenti() {
        return documenti;
    }

    public List<String> getIdCommenti() {
        return idCommenti;
    }

    public String getIdBrano() {
        return idBrano;
    }

    public String toString() {
        return titolo +
                " - " +
                String.join(", ", autori) +
                " (" +
                annoComposizione +
                ")";
    }

    // ricerca per titolo-autori-esecutori
    public static List<Brano> cerca (List<Brano> brani, String chiave){

        if(chiave == null || chiave.isBlank()) return brani;

        String key = chiave.toLowerCase();
        return brani.stream()
                .filter(b ->
                        (b.getTitolo() != null && b.getTitolo().toLowerCase().contains(key)) ||
                                (b.getAutori() != null && b.getAutori().stream()
                                        .anyMatch(e -> e.toLowerCase().contains(key))) ||
                                (b.getEsecutori() != null && b.getEsecutori().stream()
                                        .anyMatch(e -> e.toLowerCase().contains(key)))
                )
                .collect(Collectors.toList());
    }

    public void aggiungiCommento(String idCommento) {
        if (idCommenti == null) idCommenti = new ArrayList<>();
        idCommenti.add(idCommento);
    }
}
