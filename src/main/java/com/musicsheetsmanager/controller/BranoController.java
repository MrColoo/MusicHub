package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Commento;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;


import java.awt.*;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import javax.print.attribute.standard.Media;

public class BranoController {

    @FXML
    private GridPane mediaGridPane;
    @FXML
    private GridPane allegatiGridPane;
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

        caricaAllegatiBrano(brano, allegatiGridPane);

        caricaMediaBrano(brano, mediaGridPane);
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

    private void aggiungiFileAllegati(List<File> files, GridPane gridPane) {
        gridPane.getChildren().clear();
        int row = 0;

        for (File file : files) {

            // 1. Nome del file (colonna 0)
            Text fileNameText = new Text(file.getName());
            fileNameText.getStyleClass().addAll("font-light", "text-base");
            GridPane.setColumnIndex(fileNameText, 0);
            GridPane.setRowIndex(fileNameText, row);

            // 2. Bottone APRI (colonna 1)
            Button apriButton = new Button();
            ImageView apriIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/arrow-square-out-bold.png")));
            apriIcon.setFitWidth(20);
            apriIcon.setPreserveRatio(true);
            apriButton.setGraphic(apriIcon);

            apriButton.setOnAction(e -> {
                try {
                    if (file.exists()) Desktop.getDesktop().open(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            GridPane.setColumnIndex(apriButton, 1);
            GridPane.setRowIndex(apriButton, row);

            // 3. Bottone DOWNLOAD (colonna 2)
            Button downloadButton = new Button();
            ImageView downloadIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/download-simple-bold.png")));
            downloadIcon.setFitWidth(20);
            downloadIcon.setPreserveRatio(true);
            downloadButton.setGraphic(downloadIcon);

            downloadButton.setOnAction(e -> {
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

            GridPane.setColumnIndex(downloadButton, 2);
            GridPane.setRowIndex(downloadButton, row);

            // 4. Aggiunta di tutti i nodi alla riga
            gridPane.getChildren().addAll(fileNameText, apriButton, downloadButton);

            row++;
        }
    }

    public void caricaAllegatiBrano(Brano brano, GridPane allegatiGridPane) {
        File folder = new File("src/main/resources/attachments/" + idBrano);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("La cartella degli allegati non esiste per il brano: " + idBrano);
            return;
        }

        File[] fileArray = folder.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".pdf") || name.endsWith(".txt")) && file.isFile();
        });

        if (fileArray != null && fileArray.length > 0) {
            aggiungiFileAllegati(List.of(fileArray), allegatiGridPane);
        } else {
            System.out.println("Nessun file allegato valido trovato per il brano: " + idBrano);
        }
    }

    public void caricaMediaBrano(Brano brano, GridPane mediaGridPane) {
        File folder = new File("src/main/resources/attachments/" + idBrano);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("La cartella degli allegati non esiste per il brano: " + idBrano);
            return;
        }

        File[] fileArray = folder.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".mp3") || name.endsWith(".mp4")) && file.isFile();
        });

        if (fileArray != null && fileArray.length > 0) {
            aggiungiMediaAllegati(List.of(fileArray), mediaGridPane);
        } else {
            System.out.println("Nessun file allegato valido trovato per il brano: " + idBrano);
        }
    }


    private void aggiungiMediaAllegati(List<File> files, GridPane gridPane) {
        gridPane.getChildren().clear();
        int row = 0;

        for (File file : files) {

            // 1. Nome del file (colonna 0)
            Text mediaNameText = new Text(file.getName());
            mediaNameText.getStyleClass().addAll("font-light", "text-base");
            GridPane.setColumnIndex(mediaNameText, 0);
            GridPane.setRowIndex(mediaNameText, row);

            // 2. Bottone APRI (colonna 1)
            Button playButton = new Button();
            ImageView apriIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/play-bold.png")));
            apriIcon.setFitWidth(20);
            apriIcon.setPreserveRatio(true);
            playButton.setGraphic(apriIcon);

            playButton.setOnAction(e -> {
                try {
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);  // tenta di aprire con il player predefinito
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            GridPane.setColumnIndex(playButton, 1);
            GridPane.setRowIndex(playButton, row);

            // 3. Bottone DOWNLOAD (colonna 2)
            Button downloadButton = new Button();
            ImageView downloadIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/download-simple-bold.png")));
            downloadIcon.setFitWidth(20);
            downloadIcon.setPreserveRatio(true);
            downloadButton.setGraphic(downloadIcon);

            downloadButton.setOnAction(e -> {
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

            GridPane.setColumnIndex(downloadButton, 2);
            GridPane.setRowIndex(downloadButton, row);
            gridPane.getChildren().addAll(mediaNameText, playButton, downloadButton);

            row++;
        }
    }
}
