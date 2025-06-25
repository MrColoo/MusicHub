package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Concerto;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConcertoController {

    private static final Path PATH_BRANI_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/brani.json");

    private final Type tipoListaBrani = new TypeToken<List<Brano>>() {}.getType();
    @FXML
    private WebView webView;

    @FXML
    private Text concertoTitolo;

    private String idConcerto;

    @FXML
    private ComboBox selezionaBrani; // box per visualizzare i brani

    @FXML
    public void initialize() {
    }

    // Mostra i dati del concerto (titolo + link YouTube se presente)
    public void fetchConcertoData(Concerto concerto) {
        idConcerto = concerto.getId();
        String titolo = concerto.getTitolo();

        if (titolo == null || titolo.isEmpty()) {
            concertoTitolo.setText("Titolo non disponibile");
        } else {
            concertoTitolo.setText(titolo);
        }

        // Mostra il video se disponibile nel Concerto
        String linkYoutube = concerto.getLink();
        if (linkYoutube != null && !linkYoutube.isEmpty()) {
            System.out.println("Carico video da link: " + linkYoutube);
            mostraVideo(linkYoutube);
        } else {
            System.out.println("Nessun link YouTube trovato per concerto con id: " + idConcerto);
        }

        System.out.println("Caricato concerto: " + titolo);
    }

    public void mostraVideo(String linkYoutube) {
        if (webView == null) {
            System.out.println("WebView non inizializzata");
            return;
        }

        if (linkYoutube != null && linkYoutube.contains("youtube.com/watch?v=")) {
            String embedUrl = convertToEmbedUrl(linkYoutube);
            String html = "<html><body style=\"margin:0;\">" +
                    "<iframe width=\"100%\" height=\"100%\" src=\"" + embedUrl + "\" " +
                    "frameborder=\"0\" allowfullscreen></iframe></body></html>";

            webView.getEngine().loadContent(html, "text/html");
        } else {
            System.out.println("Link YouTube non valido: " + linkYoutube);
        }
    }

    private String convertToEmbedUrl(String originalUrl) {
        return originalUrl.replace("watch?v=", "embed/");
    }


    private void caricaBrani(){
        List<Concerto> brani = JsonUtils.leggiDaJson(PATH_BRANI_JSON, tipoListaBrani);
    }
}
