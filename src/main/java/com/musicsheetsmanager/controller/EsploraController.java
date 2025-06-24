package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

public class EsploraController implements Controller {

    @FXML private FlowPane container;

    @FXML private ToggleButton esploraBtn, autoriBtn, generiBtn, esecutoriBtn;
    @FXML private ToggleGroup catalogoGroup;
    @FXML private Text esploraTitle;

    private MainController mainController;
    private static final Path BRANI_JSON_PATH = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", "brani.json");
    private static final String COVER_PATH = "src/main/resources/com/musicsheetsmanager/ui/covers/";
    private static final String DEFAULT_COVER = "src/main/resources/com/musicsheetsmanager/ui/Cover.jpg";

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setBranoController(BranoController branoController) {
        // Metodo richiesto dall'interfaccia, ma non usato qui.
    }

    @FXML
    public void initialize() {
        inizializzaToggleGroup();
        esploraTitle.setText("Esplora");
    }

    public void inizializzaBrani() {
        if (mainController != null) {
            mainController.setEsploraController(this);
        }

        String viewType = getViewType();
        if (viewType != null) {
            mostraCatalogo(viewType);
        }
    }

    /**
     * Inizializza i toggle del catalogo e aggiunge un listener per il cambio selezione.
     */
    private void inizializzaToggleGroup() {
        catalogoGroup = new ToggleGroup();
        ToggleButton[] buttons = {esploraBtn, autoriBtn, generiBtn, esecutoriBtn};
        for (ToggleButton btn : buttons) {
            btn.setToggleGroup(catalogoGroup);
        }

        esploraBtn.setSelected(true); // selezione di default

        catalogoGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                mostraCatalogo(((ToggleButton) newToggle).getText().toLowerCase());
            } else {
                Platform.runLater(() -> catalogoGroup.selectToggle(oldToggle));
            }
        });
    }

    /**
     * Restituisce il tipo di vista selezionata (esplora, generi, ecc.).
     */
    public String getViewType() {
        ToggleButton selectedBtn = (ToggleButton) catalogoGroup.getSelectedToggle();
        return selectedBtn != null ? selectedBtn.getText().toLowerCase() : null;
    }

    /**
     * Carica e mostra il catalogo secondo la vista selezionata.
     */
    private void mostraCatalogo(String viewType) {
        esploraTitle.setText(viewType.substring(0, 1).toUpperCase() + viewType.substring(1));

        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> brani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        if ("esplora".equals(viewType)) {
            generaCatalogo(brani, brano -> creaCardBrano(brano, brano.getIdBrano()));
        } else {
            Path dizionarioPath = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", viewType + ".json");
            Type stringListType = new TypeToken<List<String>>() {}.getType();
            List<String> dizionario = JsonUtils.leggiDaJson(dizionarioPath, stringListType);

            if ("generi".equals(viewType)) {
                generaCatalogo(dizionario, genere -> creaCardGenere(brani, genere, viewType));
            } else {
                generaCatalogo(dizionario, this::creaCardCatalogo);
            }
        }
    }

    /**
     * Genera un catalogo a partire da una lista e una funzione di creazione card.
     */
    public <T> void generaCatalogo(List<T> elements, Function<T, Node> creaCard) {
        container.getChildren().clear();
        elements.stream().map(creaCard).forEach(container.getChildren()::add);
    }

    /**
     * Crea una card generica per brani, autori, ecc.
     */
    private VBox creaCard(String titolo, String sottotitolo, File imageFile) {
        VBox card = new VBox(7);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(154);
        card.getStyleClass().add("explore-card");
        card.setPadding(new Insets(15));
        card.setCursor(Cursor.HAND);

        if (imageFile == null || !imageFile.exists()) {
            imageFile = new File(DEFAULT_COVER);
        }

        ImageView cover = new ImageView(new Image(imageFile.toURI().toString()));
        cover.setFitWidth(154);
        cover.setPreserveRatio(true);
        cover.setPickOnBounds(true);

        Text titoloText = new Text(titolo);
        titoloText.getStyleClass().addAll("text-white", "font-bold", "text-base");

        card.getChildren().addAll(cover, titoloText);

        if (sottotitolo != null && !sottotitolo.isEmpty()) {
            Text autoreText = new Text(sottotitolo);
            autoreText.getStyleClass().addAll("text-white", "font-light", "text-sm");
            autoreText.setWrappingWidth(154);
            autoreText.setTextAlignment(TextAlignment.CENTER);
            card.getChildren().add(autoreText);
        }

        return card;
    }

    /**
     * Crea una card specifica per un brano.
     */
    public VBox creaCardBrano(Brano brano, String idBrano) {
        File imageFile = new File(COVER_PATH + idBrano + ".jpg");
        VBox card = creaCard(brano.getTitolo(), String.join(", ", brano.getAutori()), imageFile);

        card.setOnMouseClicked(e -> mainController.goToBrano(card, brano, () -> {
            BranoController controller = mainController.getBranoController();
            if (controller != null) controller.fetchBranoData(brano);
        }));

        return card;
    }

    /**
     * Crea una card per il catalogo generico (autori, esecutori).
     */
    public VBox creaCardCatalogo(String titoloCard) {
        return creaCard(titoloCard, null, null);
    }

    /**
     * Crea una card personalizzata per un genere musicale.
     */
    private GridPane creaCardGenere(List<Brano> brani, String genere, String viewType) {
        GridPane card = new GridPane();
        card.setPrefSize(240, 150);
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
        Text titolo = new Text(genere);
        titolo.getStyleClass().addAll("text-white", "font-black", "text-2xl");
        GridPane.setMargin(titolo, new Insets(15));
        card.add(titolo, 0, 0);

        // Ottieni immagine del primo brano nel genere
        String idBrano = Brano.cercaBranoConDizionario(brani, genere, viewType).getFirst().getIdBrano();
        File imageFile = new File(COVER_PATH + idBrano + ".jpg");
        if (!imageFile.exists()) imageFile = new File(DEFAULT_COVER);

        // Colore dominante come background
        Image image = new Image(imageFile.toURI().toString());
        card.setBackground(new Background(new BackgroundFill(estraiColoreDominante(image), new CornerRadii(11), Insets.EMPTY)));

        // ImageView ruotata come decorazione
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(110);
        imageView.setPreserveRatio(true);
        imageView.setPickOnBounds(true);
        imageView.setRotate(23);
        GridPane.setConstraints(imageView, 1, 1, 1, 1, HPos.LEFT, VPos.BOTTOM, Priority.NEVER, Priority.ALWAYS, new Insets(0, 0, 0, 45));
        card.getChildren().add(imageView);

        card.setOnMouseClicked(e -> {
            // Filtra i brani che contengono il genere selezionato
            List<Brano> braniDelGenere = brani.stream()
                    .filter(brano -> brano.getGeneri().contains(genere))
                    .toList();

            // Genera il catalogo solo con questi brani
            generaCatalogo(braniDelGenere, b -> creaCardBrano(b, b.getIdBrano()));
            // Imposta il titolo al genere selezionato
            esploraTitle.setText(genere.substring(0, 1).toUpperCase() + genere.substring(1));
        });

        return card;
    }

    /**
     * Estrae il colore dominante da un'immagine (campionamento 5x5 pixel).
     */
    private Color estraiColoreDominante(Image image) {
        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth(), height = (int) image.getHeight();
        long r = 0, g = 0, b = 0, count = 0;

        for (int y = 0; y < height; y += 5) {
            for (int x = 0; x < width; x += 5) {
                Color c = reader.getColor(x, y);
                r += c.getRed() * 255;
                g += c.getGreen() * 255;
                b += c.getBlue() * 255;
                count++;
            }
        }

        return Color.rgb((int)(r / count), (int)(g / count), (int)(b / count));
    }
}
