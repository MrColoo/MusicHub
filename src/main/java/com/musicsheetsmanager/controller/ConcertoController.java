package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.model.Concerto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

    }


    // mostra i dati del concerto(titolo, link ecc...) quando l'utente interagisce con un concerto

    public void fetchConcertoData(Concerto concerto) {
        idConcerto = concerto.getId();
        String titolo = concerto.getTitolo();
        if (titolo == null || titolo.isEmpty()) {
            concertoTitolo.setText("Titolo non disponibile");
        } else {
            concertoTitolo.setText(titolo);
        }

        System.out.println("🎵 Caricato concerto: " + titolo);
    }



        public void mostraVideo(String linkYoutube) {
        if (webView == null) {
            return;
        }

        if (linkYoutube != null && linkYoutube.contains("youtube.com/watch?v=")) {
            String embedUrl = convertToEmbedUrl(linkYoutube);
            String html = "<html><body style=\"margin:0;\">" +
                    "<iframe width=\"100%\" height=\"100%\" src=\"" + embedUrl + "\" " +
                    "frameborder=\"0\" allowfullscreen></iframe></body></html>";

            webView.getEngine().loadContent(html, "text/html");
        }
    }

    private String convertToEmbedUrl(String originalUrl) {
        return originalUrl.replace("watch?v=", "embed/");
    }
}
