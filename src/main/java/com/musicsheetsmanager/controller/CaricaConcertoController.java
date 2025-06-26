package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Concerto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CaricaConcertoController implements Controller{

    @FXML
    private TextField campoLinkYoutube; // Campo di input in cui l'utente può incollare un link YouTube

    @FXML
    private WebView webView; // WebView che mostrerà il video YouTube all'interno dell'interfaccia

    @FXML
    private Text errore; // Etichetta testuale usata per mostrare eventuali messaggi di errore

    @FXML
    private VBox webViewContainer;

    private String idConcerto; // ID del concerto attualmente selezionato o in fase di modifica


    // Percorso al file JSON contenente la lista dei concerti
    private static final Path PATH_CONCERTI_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/concerti.json");

    // Percorso alla cartella contenente le immagini associate ai concerti
    private static final Path PATH_CONCERTI_IMAGE = Paths.get("src/main/resources/com/musicsheetsmanager/ui/concerti/");

    // Tipo generico usato per deserializzare una lista di oggetti Concerto da JSON
    private final Type tipoListaConcerti = new TypeToken<List<Concerto>>() {}.getType();

    // Riferimento al controller principale
    private MainController mainController;

    // Metodo usato per iniettare il controller principale nella classe corrente
    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Metodo chiamato automaticamente all'inizializzazione del controller.
     * Gestisce la logica per la visualizzazione dinamica di un video YouTube nella WebView,
     * mostrando o nascondendo messaggi di errore in base alla validità del link.
     */

    @FXML
    public void initialize() {
        errore.setVisible(false);
        errore.setVisible(false);
        errore.setVisible(false); // Nasconde inizialmente il messaggio di error

        Platform.runLater(() -> {
            webView = new WebView();
            webView.setPrefHeight(215);
            webView.setPrefWidth(460);
        });

        campoLinkYoutube.textProperty().addListener((obs, oldVal, newVal) -> {
            // Se il campo è vuoto o nullo, nasconde l'errore e non fa nulla
            if (newVal == null || newVal.isBlank()) {
                errore.setVisible(false);
                return;
            }

            // Tenta di convertire il link in formato embed valido
            String embedUrl = convertToEmbedUrl(newVal.trim());

            if (embedUrl != null && !embedUrl.isEmpty()) {
                String html = String.format("""
                    <html>
                        <body style="margin:0;">
                            <iframe width="100%%" height="100%%" src="%s"
                                frameborder="0" allowfullscreen></iframe>
                        </body>
                    </html>
                """, embedUrl);

                webView.getEngine().loadContent(html, "text/html");
                webViewContainer.getChildren().setAll(webView);
                webView.setVisible(true);
                webView.setManaged(true);
                errore.setVisible(false);
            } else {
                errore.setText("Link YouTube non valido");
                // Aggiorna la GUI: mostra il video e nasconde l’errore
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


    /**
     * Metodo chiamato quando l'utente clicca per aggiungere un nuovo concerto.
     * Verifica il link, crea il concerto, lo salva nel JSON e passa alla schermata del concerto.
     */
    @FXML
    private void onAddConcertoClick() {
        String link = campoLinkYoutube.getText(); // Recupera il link YouTube inserito dall'utente
        // Tenta di convertire il link in formato embed valido
        String embedUrl = convertToEmbedUrl(campoLinkYoutube.getText().trim());

        // Se il campo è vuoto o nullo, mostra un messaggio di errore e interrompe l'azione
        if (link == null || link.trim().isEmpty()) {
            errore.setText("Link mancante");
            errore.setVisible(true);
            return;
        }

        if (embedUrl == null || embedUrl.isEmpty()) {
            return;
        }

        String titolo = estraiTitoloDaYoutube(link.trim()); // Estrae il titolo del video

        // Recuper l'utente attualmente loggato
        var utente = SessionManager.getLoggedUser();
        if (utente == null) {
            errore.setText("Utente non loggato");
            errore.setVisible(true);
            return;
        }
        // Ottiene il nome dell'utente da salvare nel concerto
        String nomeUtente = utente.getUsername();

        // Genera un id univoco per il nuovo concerto
        String id = java.util.UUID.randomUUID().toString();

        // Crea un nuovo oggetto Concerto con i dati raccolti
        Concerto nuovoConcerto = new Concerto(id, link, titolo  , nomeUtente);

        // Legge la lista dei concerti esistenti dal file JSON
        List<Concerto> concerti = JsonUtils.leggiDaJson(PATH_CONCERTI_JSON, tipoListaConcerti);
        if (concerti == null) {
            concerti = new java.util.ArrayList<>(); // Se il file è vuoto o non esiste
        }

        concerti.add(nuovoConcerto); // Aggiunge il nuovo concerto alla lista
        JsonUtils.scriviSuJson(concerti, PATH_CONCERTI_JSON); // Salva la lista aggiornata nel file JSON

        idConcerto = id; // Aggiorna l'ID del concerto corrente

        // Scarica automaticamente l'immagine di copertina dal video di YouTube

        scaricaCopertinaYoutube(nuovoConcerto);

        // Passa alla schermata di dettaglio del concerto appena creato
        mainController.goToConcerto(null, nuovoConcerto, () -> {
            // Una volta caricata la schermata, passa i dati al relativo controller
            ConcertoController controller = mainController.getConcertoController();
            if (controller != null) {
                controller.fetchConcertoData(nuovoConcerto);
            }
        });
    }


    /**
     * Estrae il titolo di un video YouTube dal suo URL accedendo direttamente alla pagina HTML.
     * Attenzione: funziona solo se la pagina è pubblica e YouTube non blocca il client.
     *
     * @param videoUrl il link diretto al video di YouTube
     * @return il titolo del video, oppure un messaggio di errore se non riuscito
     */
    private String estraiTitoloDaYoutube(String videoUrl) {
        try {
            // Apre una connessione HTTP all'URL del video
            HttpURLConnection conn = (HttpURLConnection) new URL(videoUrl).openConnection();

            // Alcuni server (come YouTube) bloccano richieste da client "sospetti":
            // impostiamo uno user-agent di un normale browser
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Imposta timeout per evitare blocchi in caso di problemi di rete
            conn.setConnectTimeout(5000); // max 5 secondi per leggere
            conn.setReadTimeout(5000); // max 5 secondi per leggere

            // Legge il contenuto HTML della pagina
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder html = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line); // Accumula tutte le righe HTML in un'unica stringa
            }
            reader.close();

            // Cerca il contenuto all'interno dei tag <title>...</title>
            String htmlContent = html.toString();
            int titleStart = htmlContent.indexOf("<title>");
            int titleEnd = htmlContent.indexOf("</title>");

            // Se trova entrambi i tag e sono nella posizione corretta
            if (titleStart != -1 && titleEnd != -1 && titleEnd > titleStart) {
                String title = htmlContent.substring(titleStart + 7, titleEnd); // Estrae il contenuto tra <title> e </title>
                return title.replace(" - YouTube", "").trim();// Rimuove la parte finale tipica " - YouTube"
            } else {
                return "Titolo non trovato";  // Se i tag non sono trovati correttamente
            }
        } catch (Exception e) {
            e.printStackTrace(); // In caso di errori di connessione/parsing restituisce un messaggio d’errore
            return "Errore caricamento titolo";
        }
    }

    private String getYoutubeThumbnail(String youtubeUrl) {
        try {
            String videoId = null;

            if (youtubeUrl.contains("v=")) {
                videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
                int ampIndex = videoId.indexOf('&');
                if (ampIndex != -1) {
                    videoId = videoId.substring(0, ampIndex);
                }
            } else if (youtubeUrl.contains("youtu.be/")) {
                videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
            }

            if (videoId == null || videoId.isEmpty()) return null;

            return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void scaricaCopertinaYoutube(Concerto concerto) {

        try {
            String imageUrl = getYoutubeThumbnail(concerto.getLink());
            BufferedImage original = ImageIO.read(new URL(imageUrl));
            if (original == null) throw new IOException("Immagine non trovata");

            // Cropping: rimuove 10% sopra e sotto (per eliminare bande nere)
            int cropPercent = 12;
            int cropPixels = (original.getHeight() * cropPercent) / 100;
            BufferedImage cropped = original.getSubimage(
                    0,
                    cropPixels,
                    original.getWidth(),
                    original.getHeight() - 2 * cropPixels
            );

            // Crea un'immagine nuova con overlay nero
            BufferedImage finalImage = new BufferedImage(
                    cropped.getWidth(),
                    cropped.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g = finalImage.createGraphics();
            // Disegna l'immagine originale
            g.drawImage(cropped, 0, 0, null);

            // Applica overlay nero semitrasparente (50%)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setColor(new java.awt.Color(0, 0, 0));
            g.fillRect(0, 0, cropped.getWidth(), cropped.getHeight());
            g.dispose();

            // Salvataggio su disco
            Files.createDirectories(PATH_CONCERTI_IMAGE);
            File outputFile = PATH_CONCERTI_IMAGE.resolve(concerto.getId() + ".jpg").toFile();
            ImageIO.write(finalImage, "jpg", outputFile);

            System.out.println("Copertina salvata come: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Errore nel caricamento immagine YouTube: " + e.getMessage());
        }

    }
}