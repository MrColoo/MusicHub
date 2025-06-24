package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.model.Concerto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

public class ConcertoController {

    @FXML
    private WebView webView;

    @FXML
    private Text concertoTitolo;

    private String idConcerto;

    @FXML
    public void initialize() {
        // opzionalmente puoi loggare per verificare che l'inizializzazione avvenga
        System.out.println("ConcertoController inizializzato");
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

        // ✅ Mostra il video se disponibile nel Concerto
        String linkYoutube = concerto.getLink();
        if (linkYoutube != null && !linkYoutube.isEmpty()) {
            System.out.println("🎬 Carico video da link: " + linkYoutube);
            mostraVideo(linkYoutube);
        } else {
            System.out.println("Nessun link YouTube trovato per concerto con id: " + idConcerto);
        }

        System.out.println("🎵 Caricato concerto: " + titolo);
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
}
