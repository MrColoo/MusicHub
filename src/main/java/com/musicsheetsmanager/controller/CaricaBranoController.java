package com.musicsheetsmanager.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Documento;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class CaricaBranoController implements Controller {

    @FXML private TextField campoTitolo;
    @FXML private TextField campoAutori;
    @FXML private TextField campoAnnoDiComposizione;
    @FXML private TextField campoGeneri;
    @FXML private TextField campoStrumentiMusicali;
    @FXML private TextField campoLinkYoutube;
    @FXML private Text errore;
    @FXML private Button allegaFileBtn;
    @FXML private Text cardTitolo;
    @FXML private Text cardAutore;
    @FXML private VBox previewContainer;
    @FXML private StackPane previewStackPane;
    @FXML private Rectangle previewBackground;
    @FXML private ImageView cover;
    @FXML private Button caricaBottone;
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

    public void initialize(){
        // genero stringa alfanumerica casuale per ogni brano
        idBrano = UUID.randomUUID().toString();

        realtimeWriting();
        initializePreviewBackground();

        errore.setVisible(false);
    }

    private void initializePreviewBackground(){
        // 1. Rectangle che funge da sfondo
        previewBackground.widthProperty().bind(previewStackPane.widthProperty());
        previewBackground.heightProperty().bind(previewStackPane.heightProperty());

        Color[] gradientColors = estraiColoriDominanti(defaultCover);
        aggiornaSfondoGradiente(previewBackground, gradientColors);
    }

    private void realtimeWriting(){

        // Listeners per aggiornamento automatico del nome inserito
        campoTitolo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                cardTitolo.setText("Titolo");
            } else {
                cardTitolo.setText(newValue);
            }
        });

        campoAutori.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                cardAutore.setText("Autore");
            } else {
                cardAutore.setText(newValue);
            }
        });

        // Listeners per capire quando scaricare la cover
        campoTitolo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) whenDownloadCover();
        });
        campoAutori.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) whenDownloadCover();
        });
    }

    private void whenDownloadCover() {
        String titolo = campoTitolo.getText().trim();
        String autore = campoAutori.getText().trim();

        // Controllo input validi
        if (titolo.isEmpty() || autore.isEmpty() ||
                !titolo.matches("[\\w\\sÀ-ÿ’',.!?\\-]+") ||
                !autore.matches("[\\w\\sÀ-ÿ’',.!?\\-]+")) {
            cambiaImmagineConFade(cover, defaultCover);
            return;
        }
        downloadCover();
    }

    public void downloadCover(){
        String titolo = campoTitolo.getText().trim();
        String autore = campoAutori.getText().trim();
        String query = URLEncoder.encode(titolo + " " + autore, StandardCharsets.UTF_8);
        String apiUrl = "https://itunes.apple.com/search?term=" + query + "&media=music&limit=1";
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    responseBuilder.append(line);
                reader.close();

                // Usa Gson per parse
                JsonObject jsonObject = JsonParser.parseString(responseBuilder.toString()).getAsJsonObject();
                JsonArray results = jsonObject.getAsJsonArray("results");

                if (results.size() == 0) {
                    System.out.println("Nessuna copertina trovata per: " + titolo + " - " + autore);
                    Platform.runLater(() -> {
                        Color[] gradientColors = estraiColoriDominanti(defaultCover);
                        aggiornaSfondoGradiente(previewBackground, gradientColors);
                        cambiaImmagineConFade(cover, defaultCover);
                    });
                    return;
                }

                JsonObject firstResult = results.get(0).getAsJsonObject();
                String artworkUrl = firstResult.get("artworkUrl100").getAsString();
                coverURL = artworkUrl.replace("100x100", "300x300");

                System.out.println(coverURL);

                // Carica l'immagine nel componente ImageView
                Image fxImage = new Image(coverURL);
                Color[] gradientColors = estraiColoriDominanti(fxImage);
                Platform.runLater(() -> {
                    aggiornaSfondoGradiente(previewBackground, gradientColors);
                    cambiaImmagineConFade(cover, fxImage);
                });

            } catch (IOException e) {
                System.err.println("Errore nel download della copertina: " + e.getMessage());
                cambiaImmagineConFade(cover, defaultCover);
                initializePreviewBackground();
            }
        }).start();
    }

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

    private void salvaCover() throws IOException {
        BufferedImage image = ImageIO.read(new URL(coverURL));
        // Percorso salvataggio
        Path coverDir = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "ui", "covers");
        Files.createDirectories(coverDir);

        File outputFile = coverDir.resolve(idBrano + ".jpg").toFile();
        ImageIO.write(image, "jpg", outputFile);

        System.out.println("Copertina salvata come: " + outputFile.getAbsolutePath());

    }

    // form per l'aggiunta di un nuovo brano da parte dell'utente
    @FXML
    public void onAddBranoClick() throws IOException {
        if(!creaBrano(idBrano)) {
            return;
        }

        salvaCover();
        salvaFileAllegati(idBrano);

        // aggiorna file json contenente i documenti
        Type documentoType = new TypeToken<List<Documento>>() {}.getType();     // documenti letti dal json
        List<Documento> listaDocumenti = JsonUtils.leggiDaJson(DOCUMENTI_JSON_PATH, documentoType);
        listaDocumenti.addAll(fileAllegati);
        JsonUtils.scriviSuJson(listaDocumenti, DOCUMENTI_JSON_PATH);
    }

    // crea il brano e aggiorna i vari dizionari (autori, generi, titoli, strumentiMusicali)
    private boolean creaBrano(String idBrano){
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

        String generiText = campoGeneri.getText().trim().toLowerCase();
        if(generiText.isEmpty()) {
            errore.setText("Campo generi obbligatorio");
            errore.setVisible(true);
            return false;
        }

        // controlla che l'anno di composizione sia un numero intero valido
        int anno;
        try {
            String annoText = campoAnnoDiComposizione.getText().trim();
            if(annoText.isEmpty()) {
                errore.setText("Campo anno di composizione obbligatorio");
                errore.setVisible(true);
                return false;
            }
            anno = Integer.parseInt(campoAnnoDiComposizione.getText().trim());
            int annoCorrente = Year.now().getValue();
            if(anno > annoCorrente) {
                errore.setText("Siamo ancora nel " + annoCorrente);
                errore.setVisible(true);
                return false;
            }
        } catch (NumberFormatException e) {
            errore.setText("Inserisci un numero intero");
            errore.setVisible(true);
            return false;
        }

        List<String> autori = List.of(campoAutori.getText().trim().split(",\\s*"));
        List<String> generi = List.of(campoGeneri.getText().toLowerCase().trim().split(",\\s*"));

        String strumentiText = campoStrumentiMusicali.getText().toLowerCase().trim();
        List<String> strumentiMusicali = strumentiText.isEmpty()
                ? new ArrayList<>()
                : List.of(strumentiText.split(",\\s*"));


        String linkYoutube = campoLinkYoutube.getText().trim();
        if(linkYoutube.isEmpty()) {
            linkYoutube = "";
        }

        List<String> pathAllegati = fileAllegati.stream()
                .map(Documento::getPercorso)
                .collect(Collectors.toList());


        Brano nuovoBrano = new Brano(idBrano, titolo, autori, generi, anno, linkYoutube, strumentiMusicali, pathAllegati);

        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType); // brani letti dal json

        // controllo che il brano non esista già (stesso titolo, autori, anno di composizione, esecutori, strumenti musicali)
        for(Brano brano: listaBrani){

            if(brano.equals(nuovoBrano)) {
                errore.setText("Il brano esiste già");
                errore.setVisible(true);
                return false;
            }
        }

        // aggiorna file json contenente tutti i brani
        listaBrani.add(nuovoBrano);
        JsonUtils.scriviSuJson(listaBrani, BRANI_JSON_PATH);

        System.out.println("Brano salvato con successo");

        // aggiorna dizionari
        aggiornaDizionario(Collections.singletonList(titolo), "titoli");
        aggiornaDizionario(autori, "autori");
        aggiornaDizionario(generi, "generi");
        aggiornaDizionario(strumentiMusicali, "strumentiMusicali");

        System.out.println("Dizionari salvati con successo");

        mainController.goToBrano(caricaBottone, nuovoBrano, () -> {
            BranoController controller = mainController.getBranoFileController();
            if (controller != null) {
                controller.fetchBranoData(nuovoBrano);
            }
        });

        return true;
    }

    // permette all'utente di caricare documenti inerenti al brano(es. spartiti, testi...)
    @FXML
    private void onAllegaFileClick() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(allegaFileBtn.getScene().getWindow());

        if(selectedFile != null && !fileAllegati.contains(selectedFile)){
            Documento documento = new Documento(SessionManager.getLoggedUser().getUsername(), selectedFile.getName(), selectedFile.getAbsolutePath());

            // controllo per duplicati
            boolean exist = fileAllegati.stream()
                    .anyMatch(doc -> doc.getPercorso().equals(documento.getPercorso()));
            if(!exist) {
                fileAllegati.add(documento);
                //viewFile(documento);
            }
        }

    }

    // TODO implementare visualizzazione allegati caricati e relativa possibilità di eliminarli
    // aggiunge il file alla Hbox
    /*
    private void viewFile(Documento documento) {
        Label nomeFile = new Label(documento.getNomeFile());
        listaFileBox.getChildren().add(nomeFile);
    }
    */


    // salva i documenti nella cartella /attachments se l'utente carica il brano
    private void salvaFileAllegati(String idBrano) {

        // nella cartella attachment crea directory con l'id del brano
        Path attachmentsDirectory = Paths.get(
                "src", "main", "resources",
                "attachments", idBrano
        );
        try{
            Files.createDirectories(attachmentsDirectory);      // crea la directory se non esiste

            for (Documento documento : fileAllegati) {
                Path sourcePath = Path.of(documento.getPercorso());
                Path destinationPath = attachmentsDirectory.resolve(documento.getNomeFile());   // ottieni path completa:
                                                                                                // path cartella + nome file
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // aggiorna in percorso relativo
                documento.setPercorso(destinationPath.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // aggiorna i vari dizionari
    public void aggiornaDizionario(List<String> nuoviElementiDizionario, String tipoDizionario) {
        Path DIZIONARIO_JSON_PATH = Paths.get( // percorso verso il file JSON
                "src", "main", "resources",
                "com", "musicsheetsmanager", "data", tipoDizionario + ".json"
        );

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> listaDizionario = JsonUtils.leggiDaJson(DIZIONARIO_JSON_PATH, listType);

        for(String element: nuoviElementiDizionario) {
            if(!listaDizionario.contains(element)){         // controllo per duplicati
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
}
