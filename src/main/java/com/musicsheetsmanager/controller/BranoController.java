package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Commento;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.*;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;

public class BranoController {

    @FXML
    private VBox fileListVBox;
    @FXML
    ImageView branoCover;
    @FXML
    Text branoTitolo;
    @FXML
    Text branoAutori;
    @FXML
    private HBox branoBanner;
    @FXML
    private TextArea campoCommento;
    @FXML
    private TextArea campoNota;

    private String idBrano;

    @FXML
    public void initialize() {

    }

    private static final Path COMMENTI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "commenti.json"
    );

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    // mostra i dati del brano(titolo, autore ecc...) quando l'utente interagisce con un brano in Esplora
    public void fetchBranoData(Brano brano) {
        idBrano = brano.getIdBrano();
        branoTitolo.setText(brano.getTitolo());

        String autori = String.join(" ,", brano.getAutori());
        branoAutori.setText(autori);

        // cover brano
        String idBrano = brano.getIdBrano();
        File imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/covers/" + idBrano + ".jpg");
        if (!imageFile.exists()) {
            imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/Cover.jpg");
        }
        Image albumCover = new Image(imageFile.toURI().toString());

        // Applico il colore come background al banner
        BackgroundFill bgFill = new BackgroundFill(estraiColoriDominanti(albumCover), CornerRadii.EMPTY, Insets.EMPTY);
        branoBanner.setBackground(new Background(bgFill));

        branoCover.setImage(albumCover);
    }

    //TODO AGGIUNGERE MESSAGGIO DI ERRORE COMMENTO VUOTO

    // funzione per salvare un commento generico
    private void aggiungiCommento(String testo, boolean isNota) {
        // controllo se commento è vuoto
        if(testo.isBlank()) {
            //errore.setText("Il testo non può essere vuoto");
            //errore.setVisible(true);
            return;
        }

        Commento nuovoCommento = new Commento(testo, SessionManager.getLoggedUser().getUsername(), isNota);

        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> listaCommenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        listaCommenti.add(nuovoCommento);

        JsonUtils.scriviSuJson(listaCommenti, COMMENTI_JSON_PATH);

        Commento.linkIdcommentoBrano( idBrano, nuovoCommento.getIdCommento(), BRANI_JSON_PATH);
    }

    @FXML
    public void OnAddCommentoClick(){
        String testoCommento = campoCommento.getText().trim();
        aggiungiCommento(testoCommento, false);
        System.out.println("Commento salvato con successo");
        campoCommento.clear();
    }

    @FXML
    public void OnAddNotaClick(){
        String testoNota = campoNota.getText().trim();
        aggiungiCommento(testoNota, true);
        System.out.println("Nota salvata con successo");
        campoNota.clear();
    }

    private LinearGradient estraiColoriDominanti(Image image) {
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

        // Crea nuovo gradiente
        LinearGradient nuovoGradiente = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, colore1),
                new Stop(1, colore2)
        );
        return nuovoGradiente;
    }

}
