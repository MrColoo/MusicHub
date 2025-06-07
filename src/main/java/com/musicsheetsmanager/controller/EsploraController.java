package com.musicsheetsmanager.controller;

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

import java.util.List;
import java.util.stream.Collectors;

public class EsploraController{
    @FXML private FlowPane braniContainer;

    public void mostraBrani(List<Brano> listaBrani) {
        braniContainer.getChildren().clear(); // pulisce view precedente

        for(Brano brano: listaBrani) {
            VBox card = creaCardBrano(brano);
            braniContainer.getChildren().add(card);
        }
    }

    private VBox creaCardBrano(Brano brano) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setMaxWidth(154.0);
        vbox.getStyleClass().add("explore-card");
        vbox.setPadding(new Insets(15));
        vbox.setCursor(Cursor.HAND);

        ImageView cover = new ImageView(new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/covers/alfa.jpg")));
        cover.setFitWidth(154);
        cover.setPreserveRatio(true);
        cover.setPickOnBounds(true);

        Text titolo = new Text(brano.getTitolo()); // Assicurati che esista getTitolo()
        titolo.getStyleClass().addAll("text-white", "font-bold", "text-base");
        VBox.setMargin(titolo, new Insets(7, 0, 0, 0));

        Text autore = new Text(brano.getAutori().stream().collect(Collectors.joining(" ,")));
        autore.getStyleClass().addAll("text-white", "font-light", "text-sm");
        VBox.setMargin(autore, new Insets(2, 0, 0, 0));

        vbox.getChildren().addAll(cover, titolo, autore);
        return vbox;
    }

}
