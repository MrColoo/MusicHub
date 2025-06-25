package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Concerto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private String idConcerto;

    @FXML private Button caricaBtn;

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

        String titolo = estraiTitoloDaYoutube(link.trim());
        String id = java.util.UUID.randomUUID().toString();
        Concerto nuovoConcerto = new Concerto(id, link, titolo);

        List<Concerto> concerti = JsonUtils.leggiDaJson(PATH_CONCERTI_JSON, tipoListaConcerti);
        concerti.add(nuovoConcerto);
        JsonUtils.scriviSuJson(concerti, PATH_CONCERTI_JSON);
        idConcerto = id;

        // Cambia scena
        mainController.goToConcerto(caricaBtn, nuovoConcerto, () -> {
            ConcertoController controller = mainController.getConcertoController();
            if (controller != null) {
                controller.fetchConcertoData(nuovoConcerto);
            }
        });
    }



    private String estraiTitoloDaYoutube(String videoUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(videoUrl).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // Alcuni siti bloccano client Java se non lo imposti
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder html = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
            reader.close();

            // Cerca il contenuto del <title>...</title>
            String htmlContent = html.toString();
            int titleStart = htmlContent.indexOf("<title>");
            int titleEnd = htmlContent.indexOf("</title>");

            if (titleStart != -1 && titleEnd != -1 && titleEnd > titleStart) {
                String title = htmlContent.substring(titleStart + 7, titleEnd);
                return title.replace(" - YouTube", "").trim();
            } else {
                return "Titolo non trovato";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore caricamento titolo";
        }
    }
}