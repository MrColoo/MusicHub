package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class BranoFileController extends CaricaCommentoController {

    private static final Path USER_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brano.json"
    );

    @FXML
    private VBox fileListVBox;

    @FXML
    private VBox mediaListVBox;

    // Estensioni multimediali supportate
    private static final List<String> MEDIA_EXTENSIONS = Arrays.asList(".mp4", ".midi", ".mid");

    @FXML
    public void initialize() {
        String docFolder = "src/main/resources/attachments/325c96dd-d639-47b7-8f2f-a180ecd0e4e0";
        mostraDocumentiDaCartella(docFolder);

        String mediaFolder = "src/main/resources/media";
        mostraMediaDaCartella(mediaFolder);


        getYoutubeLink();
    }

    private void getYoutubeLink() {
        Type userType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> brani = JsonUtils.leggiDaJson(USER_JSON_PATH, userType);

        if (brani != null) {
            for (Brano brano : brani) {
                System.out.println("Link YouTube: " + brano.getYoutubeLink());
            }
        } else {
            System.out.println("Errore nella lettura del file JSON.");
        }
    }


    /**
     * Mostra documenti (file) in fileListVBox
     */
    private void mostraDocumentiDaCartella(String folderPathString) {
        Path folderPath = Paths.get(folderPathString);
        if (!Files.isDirectory(folderPath)) {
            System.out.println("Cartella documenti non valida: " + folderPath);
            return;
        }
        try {
            fileListVBox.getChildren().clear();
            Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .forEach(p -> fileListVBox.getChildren().add(createFileRow(p)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra file multimediali in mediaListVBox
     */
    private void mostraMediaDaCartella(String folderPathString) {
        Path folderPath = Paths.get(folderPathString);
        if (!Files.isDirectory(folderPath)) {
            System.out.println("Cartella media non valida: " + folderPath);
            return;
        }
        try {
            mediaListVBox.getChildren().clear();
            Files.list(folderPath)
                    .filter(Files::isRegularFile)
                    .filter(p -> MEDIA_EXTENSIONS.stream()
                            .anyMatch(ext -> p.getFileName().toString().toLowerCase().endsWith(ext)))
                    .forEach(p -> mediaListVBox.getChildren().add(createMediaRow(p)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Crea riga file con pulsanti Visualizza e Scarica
     */
    private HBox createFileRow(Path filePath) {
        Label label = new Label(filePath.getFileName().toString());
        Button viewBtn = new Button("Visualizza");
        viewBtn.setOnAction(e -> openFile(filePath));
        Button dlBtn = new Button("Scarica");
        dlBtn.setOnAction(e -> saveFile(filePath));
        HBox box = new HBox(10, label, viewBtn, dlBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /**
     * Crea riga media con pulsante Play e Scarica
     */
    private HBox createMediaRow(Path filePath) {
        Label label = new Label(filePath.getFileName().toString());
        Button playBtn = new Button("Play");
        playBtn.setOnAction(e -> openFile(filePath));
        Button dlBtn = new Button("Scarica");
        dlBtn.setOnAction(e -> saveFile(filePath));
        HBox box = new HBox(10, label, playBtn, dlBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void openFile(Path filePath) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(filePath.toFile());
            else java.awt.Desktop.getDesktop().browse(filePath.toUri());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveFile(Path filePath) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleziona cartella destinazione");
        Window win = fileListVBox.getScene().getWindow();
        java.io.File dest = chooser.showDialog(win);
        if (dest != null) {
            try {
                Files.copy(filePath, dest.toPath().resolve(filePath.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
