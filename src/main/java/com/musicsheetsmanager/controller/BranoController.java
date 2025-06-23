package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Commento;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

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
        // Pulisce la griglia rimuovendo ogni contenuto precedente
        gridPane.getChildren().clear();
        int row = 0; // Contatore per la riga corrente

        // Cicla su tutti i file forniti
        for (File file : files) {

            // === Colonna 0: Nome del file ===
            Text fileNameText = new Text(file.getName()); // Crea un nodo testo con il nome del file
            fileNameText.getStyleClass().addAll("font-light", "text-base"); // Applica classi di stile CSS
            GridPane.setColumnIndex(fileNameText, 0); // Posiziona nella colonna 0
            GridPane.setRowIndex(fileNameText, row);  // Nella riga corrente

            // === Colonna 1: Bottone APRI ===
            Button apriButton = new Button();
            // Carica l'icona per il bottone "apri"
            ImageView apriIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/arrow-square-out-bold.png"))
            );
            apriIcon.setFitWidth(20); // Larghezza icona
            apriIcon.setPreserveRatio(true); // Mantiene proporzioni
            apriButton.setGraphic(apriIcon); // Imposta l'icona nel bottone

            // Quando cliccato, apre il file con l'app predefinita del sistema
            apriButton.setOnAction(e -> {
                try {
                    if (file.exists()) Desktop.getDesktop().open(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            GridPane.setColumnIndex(apriButton, 1); // Colonna 1
            GridPane.setRowIndex(apriButton, row);  // Riga corrente

            // === Colonna 2: Bottone DOWNLOAD ===
            Button downloadButton = new Button();
            // Carica l'icona per il bottone "download"
            ImageView downloadIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/download-simple-bold.png"))
            );
            downloadIcon.setFitWidth(20);
            downloadIcon.setPreserveRatio(true);
            downloadButton.setGraphic(downloadIcon);

            // Quando cliccato, apre un FileChooser per salvare il file
            downloadButton.setOnAction(e -> {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName(file.getName()); // Suggerisce il nome originale
                    File destinazione = fileChooser.showSaveDialog(null); // Mostra dialogo per salvataggio
                    if (destinazione != null) {
                        // Copia il file selezionato nella destinazione scelta
                        Files.copy(file.toPath(), destinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            GridPane.setColumnIndex(downloadButton, 2); // Colonna 2
            GridPane.setRowIndex(downloadButton, row);  // Riga corrente

            // === Aggiunta di tutti i nodi alla griglia ===
            gridPane.getChildren().addAll(fileNameText, apriButton, downloadButton);

            // Passa alla riga successiva
            row++;
        }
    }

    public void caricaAllegatiBrano(Brano brano, GridPane allegatiGridPane) {
        // Costruisce il percorso della cartella degli allegati del brano
        File folder = new File("src/main/resources/attachments/" + idBrano);

        // Verifica l'esistenza e la validità della cartella
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("La cartella degli allegati non esiste per il brano: " + idBrano);
            return;
        }

        // Filtra i file: solo PDF, TXT, JPG e PNG
        File[] fileArray = folder.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".pdf") || name.endsWith(".txt") || name.endsWith(".jpg") || name.endsWith(".png")) && file.isFile();
        });

        // Mostra i file validi nella griglia
        if (fileArray != null && fileArray.length > 0) {
            aggiungiFileAllegati(List.of(fileArray), allegatiGridPane);
        } else {
            System.out.println("Nessun file allegato valido trovato per il brano: " + idBrano);
        }
    }

    public void caricaMediaBrano(Brano brano, GridPane mediaGridPane) {
        // Costruisce il percorso della cartella degli allegati multimediali del brano
        File folder = new File("src/main/resources/attachments/" + idBrano);

        // Verifica l'esistenza della cartella
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("La cartella degli allegati non esiste per il brano: " + idBrano);
            return;
        }

        // Filtra i file: solo MP3 e MP4 e MIDI
        File[] fileArray = folder.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".mp3") || name.endsWith(".mp4") || name.endsWith(".midi")) && file.isFile();
        });

        // Mostra i file multimediali nella griglia
        if (fileArray != null && fileArray.length > 0) {
            aggiungiMediaAllegati(List.of(fileArray), mediaGridPane);
        } else {
            System.out.println("Nessun file allegato valido trovato per il brano: " + idBrano);
        }

        // Carica i dati dei brani dal file JSON per ottenere il link YouTube
        try {
            Path jsonPath = Paths.get("src/main/resources/com/musicsheetsmanager/data/brani.json");
            Type listType = new TypeToken<List<Brano>>() {}.getType();
            List<Brano> tuttiIBrani = JsonUtils.leggiDaJson(jsonPath, listType);

            if (tuttiIBrani == null || tuttiIBrani.isEmpty()) {
                return;
            }

            // Cerca il brano corrente nella lista
            tuttiIBrani.stream()
                    .filter(b -> b.getIdBrano().equals(brano.getIdBrano()))
                    .findFirst()
                    .ifPresent(b -> {
                        String youtubeLink = b.getYoutubeLink();
                        if (youtubeLink != null && !youtubeLink.isBlank()) {
                            aggiungiLinkYoutubeSingolo(youtubeLink, mediaGridPane);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aggiungiLinkYoutubeSingolo(String link, GridPane gridPane) {
        // Calcola la prossima riga disponibile basandosi solo sui nodi che hanno un RowIndex esplicito
        int row = gridPane.getChildren().stream()
                .map(GridPane::getRowIndex)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .orElse(-1) + 1;

        // === Colonna 0: Etichetta descrittiva ===
        Text linkText = new Text("YouTube Link");
        linkText.getStyleClass().addAll("font-light", "text-base");
        GridPane.setColumnIndex(linkText, 0);
        GridPane.setRowIndex(linkText, row);

        // === Colonna 1: Bottone per aprire il link ===
        Button openButton = new Button();
        InputStream playStream = getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/play-bold.png");
        if (playStream != null) {
            ImageView playIcon = new ImageView(new Image(playStream));
            playIcon.setFitWidth(20);
            playIcon.setPreserveRatio(true);
            openButton.setGraphic(playIcon);
        } else {
            openButton.setText("Guarda"); // Fallback testuale se l'icona manca
        }

        openButton.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        GridPane.setColumnIndex(openButton, 1);
        GridPane.setRowIndex(openButton, row);

        // === Colonna 2: Bottone per copiare il link ===
        Button copyButton = new Button();
        InputStream copyStream = getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/copy-bold.png");
        if (copyStream != null) {
            ImageView copyIcon = new ImageView(new Image(copyStream));
            copyIcon.setFitWidth(20);
            copyIcon.setPreserveRatio(true);
            copyButton.setGraphic(copyIcon);
        } else {
            copyButton.setText("Copia");
        }

        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(link);
            clipboard.setContent(content);
        });

        GridPane.setColumnIndex(copyButton, 2);
        GridPane.setRowIndex(copyButton, row);

        // Aggiunge i nodi alla griglia
        gridPane.getChildren().addAll(linkText, openButton, copyButton);
    }

    private void aggiungiMediaAllegati(List<File> files, GridPane gridPane) {
        // Pulisce la griglia rimuovendo eventuali contenuti precedenti
        gridPane.getChildren().clear();
        int row = 0; // Indice della riga corrente nella griglia

        // Itera su ciascun file multimediale da visualizzare
        for (File file : files) {

            // === Colonna 0: Nome del file ===
            Text mediaNameText = new Text(file.getName()); // Crea il nodo testuale con il nome del file
            mediaNameText.getStyleClass().addAll("font-light", "text-base"); // Applica classi di stile CSS
            GridPane.setColumnIndex(mediaNameText, 0); // Posiziona nella colonna 0
            GridPane.setRowIndex(mediaNameText, row);  // Alla riga corrente

            // === Colonna 1: Bottone "Apri" per riprodurre il file ===
            Button playButton = new Button();
            // Carica l’icona per il bottone di apertura
            ImageView apriIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/play-bold.png"))
            );
            apriIcon.setFitWidth(20); // Imposta la larghezza dell’icona
            apriIcon.setPreserveRatio(true); // Mantiene le proporzioni
            playButton.setGraphic(apriIcon); // Assegna l’icona al bottone

            // Quando cliccato, apre il file con l’applicazione predefinita del sistema
            playButton.setOnAction(e -> {
                try {
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Logga l’eccezione in caso di errore
                }
            });

            GridPane.setColumnIndex(playButton, 1); // Posiziona il bottone nella colonna 1
            GridPane.setRowIndex(playButton, row);  // Alla riga corrente

            // === Colonna 2: Bottone "Download" per salvare una copia del file ===
            Button downloadButton = new Button();
            // Carica l’icona del bottone di download
            ImageView downloadIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/download-simple-bold.png"))
            );
            downloadIcon.setFitWidth(20);
            downloadIcon.setPreserveRatio(true);
            downloadButton.setGraphic(downloadIcon); // Assegna l’icona al bottone

            // Quando cliccato, apre un dialogo per salvare il file localmente
            downloadButton.setOnAction(e -> {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName(file.getName()); // Suggerisce il nome originale
                    File destinazione = fileChooser.showSaveDialog(null); // Mostra il dialogo di salvataggio

                    if (destinazione != null) {
                        // Copia il file originale nella destinazione scelta
                        Files.copy(file.toPath(), destinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Logga eventuali eccezioni
                }
            });

            GridPane.setColumnIndex(downloadButton, 2); // Posiziona il bottone nella colonna 2
            GridPane.setRowIndex(downloadButton, row);  // Alla riga corrente

            // === Aggiunta dei nodi alla griglia ===
            gridPane.getChildren().addAll(mediaNameText, playButton, downloadButton);

            // Passa alla riga successiva
            row++;
        }
    }

}
