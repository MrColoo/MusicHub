package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Concerto;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CaricaConcertoController implements Controller{

    @FXML
    private TextField campoLinkYoutube;

    @FXML
    private WebView webView;

    @FXML
    private Text errore;

    private static final Path PATH_CONCERTI_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/concerti.json");
    private final Type tipoListaConcerti = new TypeToken<List<Concerto>>() {}.getType();


    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    @FXML
    public void initialize() {
        errore.setVisible(false);

        webView.setVisible(false);
        webView.setManaged(false); // <-- non occupa spazio
        errore.setVisible(false);

        campoLinkYoutube.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                errore.setVisible(false);
                return;
            }

            String embedUrl = convertToEmbedUrl(newVal.trim());

            if (embedUrl != null) {
                String html = String.format("""
                    <html>
                        <body style="margin:0;">
                            <iframe width="100%%" height="100%%" src="%s"
                                frameborder="0" allowfullscreen></iframe>
                        </body>
                    </html>
                """, embedUrl);

                WebEngine engine = webView.getEngine();
                engine.loadContent(html, "text/html");
                errore.setVisible(false);
                webView.setVisible(true);
                webView.setManaged(true);
            } else {
                errore.setText("Link YouTube non valido");
                errore.setVisible(true);
                webView.setVisible(false);
                webView.setManaged(false);
            }
        });
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

    @FXML
    private void onAddConcertoClick() {
        String link = campoLinkYoutube.getText();

        if (link == null || link.trim().isEmpty()) {
            errore.setText("Link mancante");
            errore.setVisible(true);
            return;
        }

        String id = java.util.UUID.randomUUID().toString();
        Concerto nuovoConcerto = new Concerto(id, link);

        List<Concerto> concerti = JsonUtils.leggiDaJson(PATH_CONCERTI_JSON, tipoListaConcerti);
        concerti.add(nuovoConcerto);
        JsonUtils.scriviSuJson(concerti, PATH_CONCERTI_JSON);

        mainController.show("Concerto");
    }

}
