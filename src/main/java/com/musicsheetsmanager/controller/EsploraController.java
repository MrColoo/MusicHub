package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.Cursor;

import javafx.scene.input.MouseEvent;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class EsploraController implements  Controller{
    @FXML private FlowPane braniContainer;

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private List<Brano> listaBraniPota;
    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );
    public void initialize() {
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        listaBraniPota = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);
        mostraBraniPota(listaBraniPota);
    }

    public void mostraBraniPota(List<Brano> listaBrani) {
         braniContainer.getChildren().clear(); // pulisce view precedente

         for(Brano brano: listaBrani) {
             VBox card = creaCardBrano(brano, brano.getIdBrano());
             braniContainer.getChildren().add(card);
         }
     }

    public void mostraBrani(List<Brano> listaBrani) {
        braniContainer.getChildren().clear(); // pulisce view precedente

        for(Brano brano: listaBrani) {
            VBox card = creaCardBrano(brano, brano.getIdBrano());
            braniContainer.getChildren().add(card);
        }
    }

    private VBox creaCardBrano(Brano brano, String idBrano) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setMaxWidth(154.0);
        vbox.getStyleClass().add("explore-card");
        vbox.setPadding(new Insets(15));
        vbox.setCursor(Cursor.HAND);

        ImageView cover = new ImageView(new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/covers/" + idBrano + ".jpg")));
        cover.setFitWidth(154);
        cover.setPreserveRatio(true);
        cover.setPickOnBounds(true);

        Text titolo = new Text(brano.getTitolo());
        titolo.getStyleClass().addAll("text-white", "font-bold", "text-base");
        VBox.setMargin(titolo, new Insets(7, 0, 0, 0));

        Text autore = new Text(brano.getAutori().stream().collect(Collectors.joining(" ,")));
        autore.getStyleClass().addAll("text-white", "font-light", "text-sm");
        VBox.setMargin(autore, new Insets(2, 0, 0, 0));

        vbox.getChildren().addAll(cover, titolo, autore);
        return vbox;
    }

    @FXML
    private void onBranoClick(MouseEvent event) {
        mainController.show("Brano");
    }
}
