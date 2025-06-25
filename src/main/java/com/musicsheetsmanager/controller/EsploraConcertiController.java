package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Concerto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class EsploraConcertiController implements Controller {

    private MainController mainController;

    @FXML FlowPane container;

    private static final Path CONCERTI_JSON_PATH = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", "concerti.json");

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void inizializzaConcerti() {
        if (mainController != null) {
            mainController.setEsploraConcertiController(this);
        }

        Type concertoType = new TypeToken<List<Concerto>>() {}.getType();
        List<Concerto> concerti = JsonUtils.leggiDaJson(CONCERTI_JSON_PATH, concertoType);
        mostraCardConcerti(concerti);
    }

    public void setConcertoController(ConcertoController concertoController) {
        // Metodo richiesto dall'interfaccia, ma non usato qui.
    }

    public void mostraCardConcerti(List<Concerto> concerti) {

        container.getChildren().clear();
        for (Concerto c : concerti) {
            container.getChildren().add(creaCardConcerto(c));
        }
    }

    /**
     * Crea una card personalizzata per un genere musicale.
     */
    private GridPane creaCardConcerto(Concerto concerto) {
        GridPane card = new GridPane();
        card.setPrefSize(326, 183);
        card.getColumnConstraints().addAll(new ColumnConstraints(100), new ColumnConstraints(100));
        card.getRowConstraints().add(new RowConstraints(30, 30, 30, Priority.SOMETIMES, VPos.TOP, true));
        card.setCursor(Cursor.HAND);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        clip.setArcWidth(11);
        clip.setArcHeight(11);
        card.setClip(clip);

        // Titolo del genere
        Text titolo = new Text(concerto.getTitolo());
        titolo.getStyleClass().addAll("text-white", "font-black", "text-2xl");
        titolo.setWrappingWidth(316);
        GridPane.setMargin(titolo, new Insets(15));
        card.add(titolo, 0, 0);

        // === Sfondo con immagine ===
        Path imagePath = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "ui", "concerti", concerto.getId() + ".jpg");
        File imageFile = imagePath.toFile();

        if (imageFile.exists()) {
            String url = imageFile.toURI().toString();
            card.setBackground(new Background(new BackgroundImage(
                    new Image(url, false), // no forced scaling at load
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, // center the image
                    new BackgroundSize(100, 100, true, true, false, true) // cover=true
            )));
        }

        // click handler
        card.setOnMouseClicked(e -> mainController.goToConcerto(card, concerto, () -> {
            ConcertoController controller = mainController.getConcertoController();
            if (controller != null){
                controller.fetchConcertoData(concerto);
            }
        }));

        return card;
    }



}
