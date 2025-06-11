package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Commento;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class BranoController {

    @FXML
    private VBox fileListVBox;

    @FXML
    private VBox mediaListVBox;

    @FXML
    ImageView branoCover;
    @FXML
    Text branoTitolo;
    @FXML
    Text branoAutori;

    @FXML
    private TextArea campoCommento;
    @FXML
    private TextArea campoNota;

    private String idBrano;

    @FXML
    public void initialize() {
        // errore.setVisible(false);
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
        branoCover.setImage(new Image(imageFile.toURI().toString()));

        // Carica allegati
        fileListVBox.getChildren().clear();
        for (String path : brano.getDocumenti()) {
            File file = new File(path);
            fileListVBox.getChildren().add(creaPulsanteFile(file));
        }

        // Carica link YouTube
        mediaListVBox.getChildren().clear();
        if (brano.getYoutubeLink() != null && !brano.getYoutubeLink().isBlank()) {
            mediaListVBox.getChildren().add(creaPulsanteYouTube(brano.getYoutubeLink()));
        }
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

    private HBox creaPulsanteFile(File file) {
        Text nome = new Text(file.getName());
        Button apri = new Button("Apri");
        Button scarica = new Button("Scarica");

        apri.setOnAction(e -> {
            try {
                if (file.exists()) Desktop.getDesktop().open(file);
                else System.out.println("File non trovato: " + file.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        scarica.setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(file.getName());
                File destinazione = fileChooser.showSaveDialog(null);
                if (destinazione != null) {
                    Files.copy(file.toPath(), destinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox box = new HBox(10, nome, apri, scarica);
        return box;
    }

    private HBox creaPulsanteYouTube(String url) {
        Text label = new Text("YouTube:");
        Hyperlink link = new Hyperlink(url);

        link.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox box = new HBox(10, label, link);
        return box;
    }
}
