package com.musicsheetsmanager.model;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Brano {
    private String titolo;
    private List<String> autori;
    private List<String> generi;
    private Integer annoComposizione;
    private List<String> esecutori;
    private List<String> strumentiMusicali;
    private String linkYoutube;
    private String idBrano;


    private List<String> documenti; // path dei documenti
    private List<String> idCommenti;

    public Brano(){};

    public Brano(String idBrano, String titolo, List<String> autori, List<String> generi, Integer annoComposizione, List<String> esecutori, String linkYoutube, List<String> strumentiMusicali) {
        this.idBrano = idBrano;     // genera id alfanumerico casuale
        this.titolo = titolo;
        this.autori = autori;
        this.generi = generi;
        this.annoComposizione = annoComposizione;
        this.esecutori = esecutori;
        this.linkYoutube = linkYoutube;
        this.strumentiMusicali = strumentiMusicali;

        this.documenti = new ArrayList<>();
        this.idCommenti = new ArrayList<>();
    }

    // Getter e Setter
    public String getYoutubeLink() {return linkYoutube;}


    public String getTitolo() {
        return titolo;
    }

    public List<String> getAutori() {
        return autori;
    }

    public List<String> getGeneri() {
        return generi;
    }

    public List<String> getEsecutori() {
        return esecutori;
    }

    public Integer getAnnoComposizione() {
        return annoComposizione;
    }

    public List<String> getDocumenti() {
        return documenti;
    }

    public List<String> getIdCommenti() {
        return idCommenti;
    }

    public String getIdBrano() {
        return idBrano;
    }

    public void setDocumenti(List<String> documenti) {
        this.documenti = documenti;
    }

    public String toString() {
        return titolo +
                " - " +
                String.join(", ", autori) +
                " (" +
                annoComposizione +
                ")";
    }

    public static Brano getBranoById(List<Brano> brani, String idBrano) {
        return brani.stream()
                .filter(brano -> brano.getIdBrano().equals(idBrano))
                .findFirst()
                .orElse(null);
    }

    // ricerca per titolo-autori-esecutori
    public static List<Brano> cercaBrano (List<Brano> brani, String chiave){
        if(chiave == null || chiave.isBlank()) return brani;

        String key = chiave.toLowerCase();
        return brani.stream()
                .filter(b ->
                        (b.getTitolo() != null && b.getTitolo().toLowerCase().contains(key)) ||
                                (b.getAutori() != null && b.getAutori().stream()
                                        .anyMatch(e -> e.toLowerCase().contains(key)))
                )
                .collect(Collectors.toList());
    }

    public static List<Brano> cercaBranoConDizionario(List<Brano> brani, String chiave, String tipoDizionario) {
        if (chiave == null || chiave.isBlank() || tipoDizionario == null) return brani;

        String key = chiave.toLowerCase();

        return brani.stream().filter(b -> {
            switch (tipoDizionario.toLowerCase()) {
                case "autori":
                    return b.getAutori() != null && b.getAutori().stream()
                            .anyMatch(a -> a.toLowerCase().contains(key));
                case "generi":
                    return b.getGeneri() != null && b.getGeneri().stream()
                            .anyMatch(g -> g.equalsIgnoreCase(key));
                case "esecutori":
                    return b.getEsecutori() != null && b.getAutori().stream()
                            .anyMatch(a -> a.toLowerCase().contains(key));
                default:
                    return false;
            }
        }).collect(Collectors.toList());
    }

    public static List<String> cercaCatalogo (List<String> dizionario, String chiave){

        if(chiave == null || chiave.isBlank()) return dizionario;

        String key = chiave.toLowerCase();
        return dizionario.stream()
                .filter(b -> b.contains(key))
                .collect(Collectors.toList());
    }

    public void aggiungiCommento(String idCommento) {
        if (idCommenti == null) idCommenti = new ArrayList<>();
        idCommenti.add(idCommento);
    }

    public static void rimuoviCommentoBrano(String idBrano, String idCommento, Path BRANI_JSON_PATH) {
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        for (Brano b : listaBrani) {
            if (b.getIdBrano().equals(idBrano)) {
                b.getIdCommenti().remove(idCommento);
                break;
            }
        }

        JsonUtils.scriviSuJson(listaBrani, BRANI_JSON_PATH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                titolo,
                autori,
                annoComposizione,
                esecutori == null ? Collections.emptySet() : new HashSet<>(esecutori),
                strumentiMusicali == null ? Collections.emptySet() : new HashSet<>(strumentiMusicali)
        );
    }

    // due brani sono uguali se hanno stesso titolo, autori, anno di composizione, esecutori e strumenti musicali
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Brano other = (Brano) obj;

        return Objects.equals(titolo, other.titolo)
                && Objects.equals(autori, other.autori)
                && Objects.equals(annoComposizione, other.annoComposizione)
                // non mi importa l'ordine degli esecutori e degli strumenti musicali
                //
                // dato che non sono campi obbligatori possono anche essere vuoti
                && (esecutori == null ? Collections.emptySet() : new HashSet<>(esecutori))
                .equals(other.esecutori == null ? Collections.emptySet() : new HashSet<>(other.esecutori))
                && (strumentiMusicali == null ? Collections.emptySet() : new HashSet<>(strumentiMusicali))
                .equals(other.strumentiMusicali == null ? Collections.emptySet() : new HashSet<>(other.strumentiMusicali));
    }
}
