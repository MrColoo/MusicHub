package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Concerto;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.List;

public class ConcertoController {

    @FXML
    private WebView webView;

    @FXML
    public void initialize() {
        try {
            Type listType = new TypeToken<List<Concerto>>() {}.getType();
            List<Concerto> concerti = JsonUtils.leggiDaJson(
                    Paths.get("src/main/resources/com/musicsheetsmanager/data/concerti.json"), listType
            );

            if (!concerti.isEmpty()) {
                String link = concerti.get(0).getLink();
                mostraVideo(link);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
