package com.musicsheetsmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.text.Text;

public class BranoController extends CaricaCommentoController {

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
    }

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
