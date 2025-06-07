package com.musicsheetsmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.text.Text;

public class BranoFileController extends CaricaCommentoController {

    @FXML
    private VBox fileListVBox;

    @FXML
    ImageView branoCover;
    @FXML
    Text branoTitolo;
    @FXML
    Text branoAutori;


    @FXML
    public void initialize() {

        // Esempio di chiamata: cambia questo percorso con quello corretto sul tuo filesystem
        //String cartella = "src/main/resources/attachments/1175ee37-fa80-484d-b362-1f25dd93ebc9";
        //mostraDocumentiDaCartella(cartella);
    }

    public void inizializzaConBrano(Brano brano) {
        fetchBranoData(brano);
    }

   // /**
   //  * Legge tutti i file all'interno della cartella indicata dal pathString
   //  * e per ciascun file aggiunge una Label alla VBox.
   //  *
   //  * @param folderPathString percorso (assoluto o relativo) della cartella da cui leggere i file
   //  */
   // public void mostraDocumentiDaCartella(String folderPathString) {
   //     // Ottieni un Path a partire dalla stringa
   //     Path folderPath = Paths.get(folderPathString);
//
   //     // Controlli base: esistenza e tipo
   //     if (!Files.exists(folderPath)) {
   //         System.out.println("La cartella non esiste: " + folderPath.toAbsolutePath());
   //         return;
   //     }
   //     if (!Files.isDirectory(folderPath)) {
   //         System.out.println("Non è una cartella valida: " + folderPath.toAbsolutePath());
   //         return;
   //     }
//
   //     try {
   //         // Pulisce eventuali Label precedenti
   //         fileListVBox.getChildren().clear();
//
   //         // Elenca tutti i file regolari nella cartella
   //         Files.list(folderPath)
   //                 .filter(Files::isRegularFile)
   //                 .forEach(filePath -> {
   //                     String fileName = filePath.getFileName().toString();
   //                     Label fileLabel = new Label(fileName);
   //                     fileLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
   //                     fileListVBox.getChildren().add(fileLabel);
   //                 });
//
   //     } catch (IOException e) {
   //         e.printStackTrace();
   //         System.out.println("Errore durante la lettura dei file in: " + folderPath.toAbsolutePath());
   //     }
   // }

    // mostra i dati del brano(titolo, autore ecc...) quando l'utente interagisce con un brano in Esplora
    public void fetchBranoData(Brano brano) {
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
    }
}
