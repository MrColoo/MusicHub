package com.musicsheetsmanager.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Documento;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;

import javafx.geometry.Insets;

import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.scene.image.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.*;

import com.google.gson.reflect.TypeToken;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class CaricaBranoController implements Controller {

    @FXML private TextField campoTitolo;
    @FXML private TextField campoAutori;
    @FXML private TextField campoAnnoDiComposizione;
    @FXML private TextField campoGeneri;
    @FXML private TextField campoEsecutori;
    @FXML private TextField campoStrumentiMusicali;
    @FXML private TextField campoLinkYoutube;
    @FXML private Text errore;
    @FXML private Button allegaFileBtn;
    @FXML private Text cardTitolo;
    @FXML private Text cardAutore;
    @FXML private StackPane previewStackPane;
    @FXML private Rectangle previewBackground;
    @FXML private ImageView cover;
    @FXML private Button caricaBottone;
    @FXML private FlowPane allegatiPane;
    private final Image defaultCover = new Image(getClass().getResource("/com/musicsheetsmanager/ui/Cover.jpg").toExternalForm());
    private String coverURL;

    private String idBrano = "";

    private final List<Documento> fileAllegati = new ArrayList<>();     // salva temporaneamente i file
                                                                        // allegati finché l'utente
                                                                        // non carica il brano

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    private static final Path DOCUMENTI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "documenti.json"
    );

    /**
     *  Crea id brano
     *  inizializza realtimewriting()
     *  inizializza lo sfondo della card di preview
     */
    public void initialize(){
        // genero stringa alfanumerica casuale per ogni brano
        idBrano = UUID.randomUUID().toString();

        realtimeWriting();
        initializePreviewBackground();

        errore.setVisible(false);
    }

    /**
     *  Inizializza lo sfondo della card di preview
     */
    private void initializePreviewBackground(){
        // 1. Rectangle che funge da sfondo
        previewBackground.widthProperty().bind(previewStackPane.widthProperty());
        previewBackground.heightProperty().bind(previewStackPane.heightProperty());

        Color[] gradientColors = estraiColoriDominanti(defaultCover);
        aggiornaSfondoGradiente(previewBackground, gradientColors);
    }

    private String lastTitolo = "";
    private String lastAutore = "";

    private PauseTransition debounceTimer = new PauseTransition(Duration.millis(500));


    /**
     *  Permette la visione in tempo reale dell'inserimento di titolo e autore
     */
    private void realtimeWriting() {
        // Aggiorna dinamicamente i testi nelle card
        campoTitolo.textProperty().addListener((obs, oldValue, newValue) -> {
            cardTitolo.setText((newValue == null || newValue.trim().isEmpty()) ? "Titolo" : newValue);
        });

        campoAutori.textProperty().addListener((obs, oldValue, newValue) -> {
            cardAutore.setText((newValue == null || newValue.trim().isEmpty()) ? "Autore" : newValue);
        });

        // Avvia debounce solo quando entrambi i campi hanno perso il focus
        ChangeListener<Boolean> focusListener = (obs, oldVal, newVal) -> {
            if (!campoTitolo.isFocused() && !campoAutori.isFocused()) {
                debounceTimer.stop();
                debounceTimer.setOnFinished(e -> checkAndDownloadCover());
                debounceTimer.playFromStart();
            }
        };

        campoTitolo.focusedProperty().addListener(focusListener);
        campoAutori.focusedProperty().addListener(focusListener);
    }

    /**
     *  Controlli sugli input utente dei campi titolo e autore
     */
    private void checkAndDownloadCover() {
        String titoloCorrente = campoTitolo.getText().trim();
        String autoreCorrente = campoAutori.getText().trim();

        if (!titoloCorrente.equals(lastTitolo) || !autoreCorrente.equals(lastAutore)) {
            lastTitolo = titoloCorrente;
            lastAutore = autoreCorrente;
            whenDownloadCover();
        }
    }

    /**
     * Funzione che determina quando è il momento di scaricare la copertina e i metadati dal brano
     */
    private void whenDownloadCover() {
        String titolo = campoTitolo.getText().trim();
        String autore = campoAutori.getText().trim();

        if (!isValidInput(titolo) || !isValidInput(autore)) {
            Platform.runLater(() -> {
                cambiaImmagineConFade(cover, defaultCover);
                initializePreviewBackground();
            });
            return;
        }

        downloadCover();
    }

    private boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty()
                && input.matches(".*[\\p{L}\\p{N}].*");
    }

    /**
     * Controlla se i campi titolo o autore hanno valori non validi
     * Successivamente scarica i metadati del brano, compresa la copertina e li visualizza all'utente
     * Titolo, autore, anno di composizione e copertina vengono scaricati da Spotify grazie alle API e una query
     * Successivamente il genere viene preso da Itunes sfruttando la API
     * Il tutto eseguito da Threads separati per evitare rallentamenti al sistema
     */
    public void downloadCover() {
        String titolo = campoTitolo.getText().trim();
        String autore = campoAutori.getText().trim();

        if (titolo.isEmpty() ||
                !titolo.matches("[\\w\\sÀ-ÿ’',.!?\\-]+") ||
                !autore.matches("[\\w\\sÀ-ÿ’',.!?\\-]+")) {
            cambiaImmagineConFade(cover, defaultCover);
            return;
        }

        new Thread(() -> {
            try {
                // 1. Access Token Spotify
                String clientId = "";
                String clientSecret = "";
                String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

                HttpURLConnection tokenConn = (HttpURLConnection) new URL("https://accounts.spotify.com/api/token").openConnection();
                tokenConn.setRequestMethod("POST");
                tokenConn.setRequestProperty("Authorization", "Basic " + auth);
                tokenConn.setDoOutput(true);
                tokenConn.getOutputStream().write("grant_type=client_credentials".getBytes());

                JsonObject tokenResponse = JsonParser.parseReader(new InputStreamReader(tokenConn.getInputStream())).getAsJsonObject();
                String accessToken = tokenResponse.get("access_token").getAsString();

                // 2. Cerca brano su Spotify
                String query = URLEncoder.encode(titolo + " " + autore, StandardCharsets.UTF_8);
                HttpURLConnection searchConn = (HttpURLConnection)
                        new URL("https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=1").openConnection();
                searchConn.setRequestMethod("GET");
                searchConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                JsonObject searchResult = JsonParser.parseReader(new InputStreamReader(searchConn.getInputStream())).getAsJsonObject();
                JsonArray items = searchResult.getAsJsonObject("tracks").getAsJsonArray("items");
                if (items.isEmpty()) {
                    Platform.runLater(() -> {
                        aggiornaSfondoGradiente(previewBackground, estraiColoriDominanti(defaultCover));
                        cambiaImmagineConFade(cover, defaultCover);
                    });
                    return;
                }

                JsonObject track = items.get(0).getAsJsonObject();
                String trackName = track.get("name").getAsString();
                JsonArray artistsArray = track.getAsJsonArray("artists");
                List<String> artistNames = new ArrayList<>();

                for (int i = 0; i < artistsArray.size(); i++) {
                    JsonObject artistObj = artistsArray.get(i).getAsJsonObject();
                    artistNames.add(artistObj.get("name").getAsString());
                }

                String artistName = String.join(", ", artistNames);
                String releaseDate = track.getAsJsonObject("album").get("release_date").getAsString();
                JsonArray images = track.getAsJsonObject("album").getAsJsonArray("images");
                String imageUrl = images.get(images.size() - 2).getAsJsonObject().get("url").getAsString(); // medio formato

                coverURL = imageUrl;

                // Scarica immagine
                Image fxImage = new Image(imageUrl);
                Color[] gradientColors = estraiColoriDominanti(fxImage);

                // Scarica genere da iTunes in un altro thread
                new Thread(() -> {
                    String finalGenre = "N/A";
                    try {
                        String queryItunes = URLEncoder.encode(trackName + " " + artistName, StandardCharsets.UTF_8);
                        String apiUrl = "https://itunes.apple.com/search?term=" + queryItunes + "&media=music&limit=1";
                        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                        conn.setRequestMethod("GET");

                        JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
                        JsonArray results = json.getAsJsonArray("results");
                        if (results.size() > 0) {
                            JsonObject first = results.get(0).getAsJsonObject();
                            finalGenre = first.get("primaryGenreName").getAsString();
                        }
                    } catch (Exception e) {
                        System.err.println("Errore durante recupero genere: " + e.getMessage());
                    }

                    String finalGenreCopy = finalGenre;
                    Platform.runLater(() -> campoGeneri.setText(finalGenreCopy));
                }).start();

                lastAutore = artistName;
                lastTitolo = trackName;
                // Aggiorna UI principale
                Platform.runLater(() -> {
                    aggiornaSfondoGradiente(previewBackground, gradientColors);
                    cambiaImmagineConFade(cover, fxImage);
                    campoAutori.setText(artistName);
                    campoAnnoDiComposizione.setText(releaseDate.substring(0, 4));
                    campoTitolo.setText(trackName);
                });

            } catch (Exception e) {
                System.err.println("Errore nel download da Spotify: " + e.getMessage());
                Platform.runLater(() -> {
                    cambiaImmagineConFade(cover, defaultCover);
                    initializePreviewBackground();
                });
            }
        }).start();
    }

    /**
     * Cambia l'immagine di sfondo della preview con una dissolvenza
     *
     * @param imageView la imageView da modificare
     * @param nuovaImmagine Immagine da mettere come sfondo
     */
    private void cambiaImmagineConFade(ImageView imageView, Image nuovaImmagine) {
        Image attuale = imageView.getImage();

        // Controlla se è la stessa immagine (confronta URL se disponibili)
        if (attuale != null && attuale.getUrl() != null && nuovaImmagine.getUrl() != null) {
            if (attuale.getUrl().equals(nuovaImmagine.getUrl())) {
                return; // Nessun cambiamento → niente fade
            }
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), imageView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), imageView);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(event -> {
            imageView.setImage(nuovaImmagine);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Salva su disco la immagine scaricata da Spotify
     */
    private void salvaCover() throws IOException {
        BufferedImage image = ImageIO.read(new URL(coverURL));
        // Percorso salvataggio
        Path coverDir = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "ui", "covers");
        Files.createDirectories(coverDir);

        File outputFile = coverDir.resolve(idBrano + ".jpg").toFile();
        ImageIO.write(image, "jpg", outputFile);

        System.out.println("Copertina salvata come: " + outputFile.getAbsolutePath());

    }

    /**
     * Form per l'aggiunta di un nuovo brano da parte dell'utente
     */
    @FXML
    public void onAddBranoClick() throws IOException {
        if(!creaBrano(idBrano)) {
            return;
        }

        // Scarica automaticamente le immagini degli autori
        List<String> autoriList = Arrays.stream(
                        campoAutori.getText()
                                .toLowerCase()
                                .trim()
                                .split("\\s*(,|&|/|feat\\.?|ft\\.?|con|e)\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        scaricaFotoArtisti(autoriList);

        if(!campoEsecutori.getText().isEmpty()){
            // Genera automaticamente gli avatar degli esecutori
            List<String> esecutoriList = Arrays.stream(
                            campoEsecutori.getText()
                                    .toLowerCase()
                                    .trim()
                                    .split("\\s*(,|&|/|feat\\.?|ft\\.?|con|e)\\s+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            generaAvatarEsecutori(esecutoriList);
        }

        salvaCover();
        salvaFileAllegati(idBrano);

        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType); // brani letti dal json
        Brano nuovoBrano = Brano.getBranoById(listaBrani, idBrano);

        // Aggiorna path allegati del brano
        List<String> pathAllegati = fileAllegati.stream()
                .map(Documento::getPercorso)
                .collect(Collectors.toList());

        nuovoBrano.setDocumenti(pathAllegati);

        // Aggiorna file json contenente tutti i brani
        JsonUtils.scriviSuJson(listaBrani, BRANI_JSON_PATH);
        System.out.println("Path allegati modificati con successo");

        // Aggiorna file json contenente i documenti
        Type documentoType = new TypeToken<List<Documento>>() {}.getType();     // Documenti letti dal json
        List<Documento> listaDocumenti = JsonUtils.leggiDaJson(DOCUMENTI_JSON_PATH, documentoType);
        listaDocumenti.addAll(fileAllegati);
        JsonUtils.scriviSuJson(listaDocumenti, DOCUMENTI_JSON_PATH);
    }

    /**
     * Crea il brano e aggiorna i vari dizionari (autori, generi, titoli, strumentiMusicali)
     *
     * @param idBrano Id alfanumerico generato per il brano
     *
     * @return true se il brano è stato creato correttamente
     */
    private boolean creaBrano(String idBrano){
        // Legge i vari campi del form
        String titolo = campoTitolo.getText().trim();
        if(titolo.isEmpty()) {
            errore.setText("Campo titolo obbligatorio");
            errore.setVisible(true);
            return false;
        }

        String autoriText = campoAutori.getText().trim();
        if(autoriText.isEmpty()) {
            errore.setText("Campo autori obbligatorio");
            errore.setVisible(true);
            return false;
        }

        String annoText = campoAnnoDiComposizione.getText().trim().toLowerCase();
        if(annoText.isEmpty()) {
            return false;
        }

        String generiText = campoGeneri.getText().trim().toLowerCase();
        if(generiText.isEmpty()) {
            return false;
        }

        List<String> generi = List.of(campoGeneri.getText().toLowerCase().trim().split(",\\s*"));

        int anno = Integer.parseInt(campoAnnoDiComposizione.getText().trim());

        List<String> autori = Arrays.stream(
                        campoAutori.getText()
                                .toLowerCase()
                                .trim()
                                .split("\\s*(,|&|/|feat\\.?|ft\\.?|con|e)\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        String strumentiText = campoStrumentiMusicali.getText().toLowerCase().trim();
        List<String> strumentiMusicali = strumentiText.isEmpty()
                ? new ArrayList<>()
                : List.of(strumentiText.split(",\\s*"));

        String esecutoriText = campoEsecutori.getText().toLowerCase().trim();
        List<String> esecutori = esecutoriText.isEmpty()
                ? new ArrayList<>()
                : List.of(esecutoriText.split(",\\s*"));


        String linkYoutube = campoLinkYoutube.getText().trim();
        if(linkYoutube.isEmpty()) {
            linkYoutube = "";
        }

        Brano nuovoBrano = new Brano(SessionManager.getLoggedUser().getUsername(), idBrano, titolo, autori, generi, anno, esecutori, linkYoutube, strumentiMusicali);

        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType); // brani letti dal json

        // Controllo che il brano non esista già (stesso titolo, autori, anno di composizione, esecutori, strumenti musicali)
        for(Brano brano: listaBrani){
            if(brano.equals(nuovoBrano)) {
                errore.setText("Il brano esiste già");
                errore.setVisible(true);
                return false;
            }
        }

        // Aggiorna file json contenente tutti i brani
        listaBrani.add(nuovoBrano);
        JsonUtils.scriviSuJson(listaBrani, BRANI_JSON_PATH);

        // Aggiorna dizionari
        aggiornaDizionario(Collections.singletonList(titolo), "titoli");
        aggiornaDizionario(autori, "autori");
        aggiornaDizionario(generi, "generi");
        aggiornaDizionario(esecutori, "esecutori");
        aggiornaDizionario(strumentiMusicali, "strumentiMusicali");

        System.out.println("Dizionari salvati con successo");

        // Cambia pagina dopo aver caricato il brano
        mainController.goToBrano(caricaBottone, nuovoBrano, () -> {
            BranoController controller = mainController.getBranoController();
            if (controller != null) {
                controller.fetchBranoData(nuovoBrano);
            }
        });

        return true;
    }

    /**
     * Permette all'utente di caricare documenti inerenti al brano(es. spartiti, testi...)
     */
    @FXML
    private void onAllegaFileClick() {
        FileChooser fileChooser = new FileChooser();

        // Filtri file consentiti
        FileChooser.ExtensionFilter filtroTutti = new FileChooser.ExtensionFilter(
                "Tutti i file supportati",
                "*.mp3", "*.mp4", "*.pdf", "*.jpg", "*.png", "*.midi"
        );

        FileChooser.ExtensionFilter filtroMP3 = new FileChooser.ExtensionFilter("File MP3 (.mp3)", "*.mp3");
        FileChooser.ExtensionFilter filtroMP4 = new FileChooser.ExtensionFilter("File MP4 (.mp4)", "*.mp4");
        FileChooser.ExtensionFilter filtroPDF = new FileChooser.ExtensionFilter("File PDF (.pdf)", "*.pdf");
        FileChooser.ExtensionFilter filtroJPG = new FileChooser.ExtensionFilter("File JPG (.jpg)", "*.jpg");
        FileChooser.ExtensionFilter filtroPNG = new FileChooser.ExtensionFilter("File PNG (.png)", "*.png");
        FileChooser.ExtensionFilter filtroMIDI = new FileChooser.ExtensionFilter("File MIDI (.midi)", "*.midi");

        fileChooser.getExtensionFilters().addAll(filtroTutti, filtroMP3, filtroMP4, filtroPDF, filtroJPG, filtroPNG, filtroMIDI);

        File selectedFile = fileChooser.showOpenDialog(allegaFileBtn.getScene().getWindow());

        // Crea l'oggetto Documento
        if(selectedFile != null && !fileAllegati.contains(selectedFile)){
            Documento documento = new Documento(SessionManager.getLoggedUser().getUsername(), selectedFile.getName(), selectedFile.getAbsolutePath());

            // Controllo per duplicati
            boolean exist = fileAllegati.stream()
                    .anyMatch(doc -> doc.getPercorso().equals(documento.getPercorso()));
            if(!exist) {
                fileAllegati.add(documento);
                viewFile(documento);
            }
        }
    }

    /**
     * Crea un bottone ogni volta che l'utente carica un allegato
     *
     * @param documento Documento allegato
     */
    private void viewFile(Documento documento) {
        Button btn = new Button(documento.getNomeFile());
        btn.setContentDisplay(ContentDisplay.RIGHT);
        btn.setPadding(new Insets(5, 5, 5, 15));
        btn.getStyleClass().addAll("text-sm", "font-book", "allegato-btn");

        ImageView icon = new ImageView(new Image(
                getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/x-circle_black.png")
        ));

        icon.setFitWidth(20);
        icon.setPreserveRatio(true);
        icon.setPickOnBounds(true);

        btn.setOnMouseClicked((MouseEvent e) -> onEliminaAllegatoClick(btn, documento));

        btn.setGraphic(icon);

        allegatiPane.getChildren().add(btn);
    }

    /**
     * Funzione per eliminare un documento prima di caricare il brano
     *
     * @param btn Bottone del documento da rimuovere
     * @param documento Documento da rimuovere
     */
    public void onEliminaAllegatoClick (Button btn, Documento documento) {
        // Rimuove bottone
        allegatiPane.getChildren().remove(btn);

        // Rimuove allegato
        fileAllegati.remove(documento);

        System.out.println("Documento: " + documento.toString() + " rimosso con successo");
    }


    /**
     * Salva i documenti nella cartella /attachments se l'utente carica il brano
     *
     * @param idBrano Id del brano
     */
    private void salvaFileAllegati(String idBrano) {
        // Nella cartella attachment crea directory con l'id del brano
        Path attachmentsDirectory = Paths.get(
                "src", "main", "resources",
                "attachments", idBrano
        );
        try{
            Files.createDirectories(attachmentsDirectory);      // crea la directory se non esiste

            for (Documento documento : fileAllegati) {
                Path sourcePath = Path.of(documento.getPercorso());
                Path destinationPath = attachmentsDirectory.resolve(documento.getNomeFile());   // Ottieni path completa:
                                                                                                // path cartella + nome file
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // Aggiorna in percorso relativo
                documento.setPercorso(destinationPath.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Aggiorna i vari dizionari
     *
     * @param nuoviElementiDizionario Elementi da aggiungere al dizionario
     * @param tipoDizionario Dizionario che si intende aggiornare
     */
    public void aggiornaDizionario(List<String> nuoviElementiDizionario, String tipoDizionario) {
        Path DIZIONARIO_JSON_PATH = Paths.get( // Percorso verso il file JSON
                "src", "main", "resources",
                "com", "musicsheetsmanager", "data", tipoDizionario + ".json"
        );

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> listaDizionario = JsonUtils.leggiDaJson(DIZIONARIO_JSON_PATH, listType);

        for(String element: nuoviElementiDizionario) {
            if(!listaDizionario.contains(element)){         // Controllo per duplicati
                    listaDizionario.add(element);
            }
        }

        JsonUtils.scriviSuJson(listaDizionario, DIZIONARIO_JSON_PATH);
    }

    private Color[] estraiColoriDominanti(Image image) {
        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        long redSum = 0, greenSum = 0, blueSum = 0;
        int count = 0;

        for (int y = 0; y < height; y += 5) {
            for (int x = 0; x < width; x += 5) {
                javafx.scene.paint.Color c = reader.getColor(x, y);
                redSum += (int)(c.getRed() * 255);
                greenSum += (int)(c.getGreen() * 255);
                blueSum += (int)(c.getBlue() * 255);
                count++;
            }
        }

        int r = (int)(redSum / count);
        int g = (int)(greenSum / count);
        int b = (int)(blueSum / count);

        javafx.scene.paint.Color colore1 = Color.rgb(r, g, b);
        javafx.scene.paint.Color colore2 = colore1.darker();

        return new Color[]{colore1, colore2};
    }

    private void aggiornaSfondoGradiente(Rectangle background, Color[] nuoviColori) {
        // Crea nuovo gradiente
        LinearGradient nuovoGradiente = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, nuoviColori[0]),
                new Stop(1, nuoviColori[1])
        );

        // Transizione di opacità solo sul fill del background
        Rectangle overlay = new Rectangle();
        overlay.widthProperty().bind(background.widthProperty());
        overlay.heightProperty().bind(background.heightProperty());
        overlay.arcHeightProperty().bind(background.arcHeightProperty());
        overlay.arcWidthProperty().bind(background.arcWidthProperty());
        overlay.setFill(nuovoGradiente);
        overlay.setOpacity(0);

        ((Pane) background.getParent()).getChildren().add(1, overlay);

        // Dissolvenza in entrata
        FadeTransition fade = new FadeTransition(Duration.millis(400), overlay);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setOnFinished(e -> {
            // Una volta completato, sostituisce il fill originale
            background.setFill(nuovoGradiente);
            ((Pane) background.getParent()).getChildren().remove(overlay);
        });
        fade.play();
    }

    /**
     * Scarica la foto profilo degli artisti grazie alla API di Spotify
     *
     * @param autori Lista degli autori dei brani
     */
    private void scaricaFotoArtisti(List<String> autori) {
        new Thread(() -> {
            try {
                String clientId = "";
                String clientSecret = "";

                String auth = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
                URL url = new URL("https://accounts.spotify.com/api/token");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Basic " + auth);
                conn.setDoOutput(true);
                conn.getOutputStream().write("grant_type=client_credentials".getBytes());

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonObject tokenResponse = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();
                String accessToken = tokenResponse.get("access_token").getAsString();

                Path autoriDir = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "ui", "autori");
                Files.createDirectories(autoriDir);

                for (String autore : autori) {
                    String filename = autore.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";
                    File outputFile = autoriDir.resolve(filename).toFile();

                    // Salta se l'immagine esiste già
                    if (outputFile.exists()) {
                        System.out.println("Foto già presente per: " + autore);
                        continue;
                    }

                    String query = URLEncoder.encode(autore, StandardCharsets.UTF_8);
                    String searchUrl = "https://api.spotify.com/v1/search?q=" + query + "&type=artist&limit=1";

                    HttpURLConnection searchConn = (HttpURLConnection) new URL(searchUrl).openConnection();
                    searchConn.setRequestMethod("GET");
                    searchConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                    BufferedReader searchReader = new BufferedReader(new InputStreamReader(searchConn.getInputStream()));
                    JsonObject searchResult = JsonParser.parseReader(searchReader).getAsJsonObject();
                    searchReader.close();

                    JsonArray items = searchResult.getAsJsonObject("artists").getAsJsonArray("items");
                    if (items.size() == 0) continue;

                    JsonObject artist = items.get(0).getAsJsonObject();
                    JsonArray images = artist.getAsJsonArray("images");
                    if (images.size() == 0) continue;

                    String imageUrl = images.get(images.size() - 1).getAsJsonObject().get("url").getAsString();
                    BufferedImage originalImage = ImageIO.read(new URL(imageUrl));

                    BufferedImage resizedImage = new BufferedImage(154, 154, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.drawImage(originalImage, 0, 0, 154, 154, null);
                    g.dispose();

                    ImageIO.write(resizedImage, "jpg", outputFile);
                    System.out.println("Scaricata immagine per: " + autore);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Genera avatar per l'esecutore immesso nel form, se non già esistente
     *
     * @param esecutori Esecutori dei brani
     */
    private void generaAvatarEsecutori(List<String> esecutori) {
        new Thread(() -> {
            try {
                Path esecutoriDir = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "ui", "esecutori");
                Files.createDirectories(esecutoriDir);

                for (String esecutore : esecutori) {
                    String safeName = esecutore.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
                    File outputFile = esecutoriDir.resolve(safeName + ".png").toFile();

                    // Salta se l'immagine esiste già
                    if (outputFile.exists()) {
                        System.out.println("Avatar già presente per: " + esecutore);
                        continue;
                    }

                    String avatarUrl = "https://api.dicebear.com/7.x/bottts-neutral/png?seed=" + URLEncoder.encode(esecutore, StandardCharsets.UTF_8);
                    BufferedImage originalImage = ImageIO.read(new URL(avatarUrl));
                    if (originalImage == null) continue;

                    BufferedImage resizedImage = new BufferedImage(154, 154, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = resizedImage.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(originalImage, 0, 0, 154, 154, null);
                    g.dispose();

                    ImageIO.write(resizedImage, "png", outputFile);
                    System.out.println("Generato avatar per esecutore: " + esecutore);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


}
