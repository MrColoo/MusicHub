package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Concerto;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConcertoController {

    private static final Path PATH_BRANI_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/brani.json");
    private static final Path PATH_BRANICONCERTO_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/braniConcerto.json");

    private final Type tipoListaBrani = new TypeToken<List<Brano>>() {}.getType();

    @FXML
    private TextField inizioBranoConcerto;

    @FXML
    private TextField fineBranoConcerto;

    @FXML
    private Button aggiungiBrano;

    @FXML
    private WebView webView;

    @FXML
    private Text concertoTitolo;

    private String idConcerto;

    @FXML
    private ComboBox<Brano> selezionaBrani; // box per visualizzare i brani

    @FXML
    public void initialize() {
        caricaBrani();
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
            String html = String.format("""
                    <html>
                        <body style="margin:0;">
                            <iframe width="100%%" height="100%%" src="%s"
                                frameborder="0" allowfullscreen></iframe>
                        </body>
                    </html>
                """, embedUrl);

            webView.getEngine().loadContent(html, "text/html");
        } else {
            System.out.println("Link YouTube non valido: " + linkYoutube);
        }
    }

    private String convertToEmbedUrl(String url) {
        // Supporta anche short URL
        try {
            if (url.contains("youtube.com/watch?v=")) {
                String videoId = url.substring(url.indexOf("v=") + 2);
                int amp = videoId.indexOf('&');
                if (amp != -1) videoId = videoId.substring(0, amp);
                return "https://www.youtube.com/embed/" + videoId;
            } else if (url.contains("youtu.be/")) {
                String videoId = url.substring(url.indexOf("youtu.be/") + 9);
                int q = videoId.indexOf('?');
                if (q != -1) videoId = videoId.substring(0, q);
                return "https://www.youtube.com/embed/" + videoId;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void caricaBrani(){
        List<Brano> brani = JsonUtils.leggiDaJson(PATH_BRANI_JSON, tipoListaBrani);
        if(brani != null){
            selezionaBrani.getItems().setAll(brani);
        }else{
            System.out.println(("Errore di lettura del JSON"));
        }
    }

    private void creaBraniConcerto(){
       // List<Brano> braniConcerti = JsonUtils.scriviSuJson(tipoListaBrani, PATH_BRANICONCERTO_JSON);
    }
}
