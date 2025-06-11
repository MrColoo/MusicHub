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

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class EsploraController implements  Controller{
    @FXML private FlowPane container;

    private MainController mainController;

    // mostra Esplora o Catalogo
    // @FXML private Text viewType;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setBranoFileController(BranoController branoController) {
    }

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    //public Text getViewType() {
    //    return viewType;
    //}

    @FXML
    public void initialize(){}

    public void inizializzaBrani() {
        if (mainController != null) {
            mainController.setEsploraController(this);
        }

        // String viewTypeText = viewType.getText().toLowerCase();
        // mostraCard(viewTypeText);
        mostraCard("esplora");
    }

    // quando l'utente è nella homepage mostra tutti i brani
    public void mostraCard(String viewType) {
        if("esplora".equals(viewType)) {
            Type branoType = new TypeToken<List<Brano>>() {}.getType();
            List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);
            mostraBrani(listaBrani);
        } else {
            Path DIZIONARIO_JSON_PATH = Paths.get( // percorso verso il file JSON
                    "src", "main", "resources",
                    "com", "musicsheetsmanager", "data", viewType + ".json"
            );

            Type stringType = new TypeToken<List<String>>() {}.getType();
            List<String> dizionario = JsonUtils.leggiDaJson(DIZIONARIO_JSON_PATH, stringType);
            mostraCatalogo(dizionario);
        }
    }

     // mostra i brani trovati nella ricerca
    public void mostraBrani(List<Brano> listaBrani) {
        container.getChildren().clear(); // pulisce view precedente

        for(Brano brano: listaBrani) {
            VBox card = creaCardBrano(brano, brano.getIdBrano());
            container.getChildren().add(card);
        }
    }

    private VBox creaCardBrano(Brano brano, String idBrano) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setMaxWidth(154.0);
        vbox.getStyleClass().add("explore-card");
        vbox.setPadding(new Insets(15));
        vbox.setCursor(Cursor.HAND);

        mainController.goToBrano(vbox, brano, () -> {
            BranoController controller = mainController.getBranoFileController();
            if (controller != null) {
                controller.fetchBranoData(brano);
            }
        });

        File imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/covers/" + idBrano + ".jpg");
        if (!imageFile.exists()) {
            imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/Cover.jpg");
        }
        ImageView cover = new ImageView(new Image(imageFile.toURI().toString()));

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

    public void mostraCatalogo(List<String> dizionario) {
        container.getChildren().clear(); // pulisce view precedente

        for(String element: dizionario) {
            VBox card = creaCardCatalogo(element);
            container.getChildren().add(card);
        }
    }

    // crea una card generica per il catalogo
    private VBox creaCardCatalogo(String titoloCard) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setMaxWidth(154.0);
        vbox.getStyleClass().add("explore-card");
        vbox.setPadding(new Insets(15));
        vbox.setCursor(Cursor.HAND);

        //TODO CAMBIARE IMMAGINE COVER DEFAULT CATALOGO
        File imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/Cover.jpg");
        ImageView cover = new ImageView(new Image(imageFile.toURI().toString()));

        cover.setFitWidth(154);
        cover.setPreserveRatio(true);
        cover.setPickOnBounds(true);

        Text titoloCatalogo = new Text(titoloCard);
        titoloCatalogo.getStyleClass().addAll("text-white", "font-bold", "text-base");
        VBox.setMargin(titoloCatalogo, new Insets(7, 0, 0, 0));

        vbox.getChildren().addAll(cover, titoloCatalogo);
        return vbox;
    }
}
