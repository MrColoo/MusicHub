package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.Cursor;

import javax.imageio.ImageIO;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

public class EsploraController implements  Controller{
    @FXML private FlowPane container;

    private MainController mainController;

    // mostra tipo di catalogo
    @FXML private ToggleButton esploraBtn;
    @FXML private ToggleButton autoriBtn;
    @FXML private ToggleButton generiBtn;
    @FXML private ToggleButton esecutoriBtn;

    @FXML private ToggleGroup catalogoGroup;

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

    public String getViewType() {
        ToggleButton selectedBtn = (ToggleButton) catalogoGroup.getSelectedToggle();
        return selectedBtn.getText().toLowerCase();
    }

    @FXML
    public void initialize(){
        toggleGroup();

    }

    public void inizializzaBrani() {
        if (mainController != null) {
            mainController.setEsploraController(this);
        }

        String viewTypeText = getViewType();
        if (viewTypeText != null) {
            mostraCatalogo(viewTypeText);
        }
    }

    // inizializza il toggle group
    public void toggleGroup() {
        catalogoGroup = new ToggleGroup();

        esploraBtn.setToggleGroup(catalogoGroup);
        autoriBtn.setToggleGroup(catalogoGroup);
        generiBtn.setToggleGroup(catalogoGroup);
        esecutoriBtn.setToggleGroup(catalogoGroup);

        // default
        esploraBtn.setSelected(true);

        // listener che reagisce al cambio selezione
        catalogoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selected = (ToggleButton) newToggle;
                String viewTypeText = selected.getText().toLowerCase();
                mostraCatalogo(viewTypeText);
            } else {
                // rimetto il toggle precedente se nessuno selezionato
                Platform.runLater(() -> catalogoGroup.selectToggle(oldToggle));
            }
        });

    }

    public void mostraCatalogo(String viewType) {
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> listaBrani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);
        if("esplora".equals(viewType)) {
            generaCatalogo(listaBrani, brano -> creaCardBrano(brano, brano.getIdBrano()));
        }else {
            Path DIZIONARIO_JSON_PATH = Paths.get( // percorso verso il file JSON
                    "src", "main", "resources",
                    "com", "musicsheetsmanager", "data", viewType + ".json"
            );

            Type stringType = new TypeToken<List<String>>() {}.getType();
            List<String> dizionario = JsonUtils.leggiDaJson(DIZIONARIO_JSON_PATH, stringType);

            if("generi".equals(viewType)) {
                generaCatalogo(dizionario, element -> creaCardGenere(listaBrani, element, viewType));
            } else {
                generaCatalogo(dizionario, this::creaCardCatalogo);
            }
        }
    }

    public <T> void generaCatalogo(List<T> elements, Function<T, Node> creaCard) {
        container.getChildren().clear(); // pulisce view precedente

        for(T element: elements) {
            Node card = creaCard.apply(element);
            container.getChildren().add(card);
        }
    }

    // crea una card generica
    private VBox creaCard(
            String titoloCard,
            String autoreBrano,    // può essere null
            File imageFile         // può essere null
    ) {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setMaxWidth(154.0);
        vbox.getStyleClass().add("explore-card");
        vbox.setPadding(new Insets(15));
        vbox.setCursor(Cursor.HAND);

        if (imageFile == null || !imageFile.exists()) {
            imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/Cover.jpg");
        }

        ImageView cover = new ImageView(new Image(imageFile.toURI().toString()));

        cover.setFitWidth(154);
        cover.setPreserveRatio(true);
        cover.setPickOnBounds(true);

        Text titolo = new Text(titoloCard);
        titolo.getStyleClass().addAll("text-white", "font-bold", "text-base");
        VBox.setMargin(titolo, new Insets(7, 0, 0, 0));

        vbox.getChildren().add(cover);
        vbox.getChildren().add(titolo);

        if (autoreBrano != null && !autoreBrano.isEmpty()) {
            Text autore = new Text(autoreBrano);
            autore.getStyleClass().addAll("text-white", "font-light", "text-sm");
            VBox.setMargin(autore, new Insets(2, 0, 0, 0));
            vbox.getChildren().add(autore);
        }

        return vbox;
    }

    public VBox creaCardBrano(Brano brano, String idBrano) {
        File imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/covers/" + idBrano + ".jpg");

        VBox card = creaCard(brano.getTitolo(), String.join(" ,", brano.getAutori()), imageFile);

        Runnable onClick = () -> mainController.goToBrano(card, brano, () -> {
            BranoController controller = mainController.getBranoFileController();
            if (controller != null) {
                controller.fetchBranoData(brano);
            }
        });

        card.setOnMouseClicked(e -> onClick.run());

        return card;
    }

    public VBox creaCardCatalogo(String titoloCard) {
        return creaCard(titoloCard, null, null);
    }

    private GridPane creaCardGenere(List<Brano> listaBrani, String genere, String viewType) {
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(240.0, 150.0);

        // Column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.SOMETIMES);
        col1.setMinWidth(10);
        col1.setPrefWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.SOMETIMES);
        col2.setMinWidth(10);
        col2.setPrefWidth(100);

        gridPane.getColumnConstraints().addAll(col1, col2);

    // Row constraints
        RowConstraints row = new RowConstraints();
        row.setMinHeight(10);
        row.setPrefHeight(30);
        row.setValignment(VPos.TOP);
        row.setVgrow(Priority.SOMETIMES);

        gridPane.getRowConstraints().add(row);

        // Clip per nascondere overflow
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(gridPane.widthProperty());
        clip.heightProperty().bind(gridPane.heightProperty());
        clip.setArcHeight(11);
        clip.setArcWidth(11);
        gridPane.setClip(clip);

        // Testo (titolo genere) nella prima riga, prima colonna
        Text titoloText = new Text(genere);
        titoloText.getStyleClass().addAll("text-white", "font-black", "text-2xl");
        GridPane.setMargin(titoloText, new Insets(15, 15, 15, 15));
        gridPane.add(titoloText, 0, 0);

        // Prendo la cover del primo brano di quel genere
        String idBrano = Brano.cercaBranoConDizionario(listaBrani, genere, viewType).getFirst().getIdBrano();
        File imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/covers/" + idBrano + ".jpg");
        if (!imageFile.exists()) {
            imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/Cover.jpg");
        }

        // Applico il colore come background
        BackgroundFill bgFill = new BackgroundFill(estraiColoreDominante(new Image(imageFile.toURI().toString())), new CornerRadii(11), Insets.EMPTY);
        gridPane.setBackground(new Background(bgFill));

        Image image = new Image(imageFile.toURI().toString());

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(110.0);
        imageView.setPreserveRatio(true);
        imageView.setPickOnBounds(true);
        imageView.setRotate(23.0);

        // Imposto immagine in seconda riga (1), seconda colonna (1), in basso a destra
        GridPane.setColumnIndex(imageView, 1);
        GridPane.setRowIndex(imageView, 1);
        GridPane.setValignment(imageView, VPos.BOTTOM);
        GridPane.setMargin(imageView, new Insets(0, 0, 0, 28));

        gridPane.getChildren().add(imageView);

        return gridPane;
    }

    private Color estraiColoreDominante(Image image) {
        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        long redSum = 0, greenSum = 0, blueSum = 0;
        int count = 0;

        for (int y = 0; y < height; y += 5) {
            for (int x = 0; x < width; x += 5) {
                javafx.scene.paint.Color c = reader.getColor(x, y);
                redSum += (int)(c.getRed() * 255);
                greenSum += (int)(c.getGreen() * 255);
                blueSum += (int)(c.getBlue() * 255);
                count++;
            }
        }

        int r = (int)(redSum / count);
        int g = (int)(greenSum / count);
        int b = (int)(blueSum / count);

        return Color.rgb(r, g, b, 1.0);
    }
}
