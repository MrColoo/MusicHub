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

import com.musicsheetsmanager.config.StringUtils;

public class EsploraController implements Controller {

    @FXML private FlowPane container;

    @FXML private ToggleButton esploraBtn, autoriBtn, generiBtn, esecutoriBtn;
    @FXML private ToggleGroup catalogoGroup;
    @FXML private Text esploraTitle;

    private MainController mainController;
    private static final Path BRANI_JSON_PATH = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", "brani.json");
    private static final String COVER_PATH = "src/main/resources/com/musicsheetsmanager/ui/covers/";
    private static final String DEFAULT_COVER = "src/main/resources/com/musicsheetsmanager/ui/Cover.jpg";
    private static final String AUTORI_COVER_PATH = "src/main/resources/com/musicsheetsmanager/ui/autori/";
    private static final String ESECUTORI_COVER_PATH = "src/main/resources/com/musicsheetsmanager/ui/esecutori/";


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
     *
     * @param viewType Toggle selezionato (Esplora, Autori, Generi, Esecutori)
     */
    private void mostraCatalogo(String viewType) {
        esploraTitle.setText(StringUtils.capitalizzaTesto(viewType));

        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> brani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        if ("esplora".equals(viewType)) {
            generaCatalogo(brani, this::creaCardBrano);
        } else {
            Path dizionarioPath = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", viewType + ".json");
            Type stringListType = new TypeToken<List<String>>() {}.getType();
            List<String> dizionario = JsonUtils.leggiDaJson(dizionarioPath, stringListType);

            if ("generi".equals(viewType)) {
                generaCatalogo(dizionario, genere -> creaCardGenere(brani, genere));
            } else {
                generaCatalogo(dizionario, this::creaCardCatalogo);
            }
        }
    }

    /**
     * Genera un catalogo a partire da una lista e una funzione di creazione card.
     *
     * @param elements Lista di cui si vuole creare la card (brani o dizionari)
     * @param creaCard Funzione per creare la card che si intende creare (default o genere)
     */
    public <T> void generaCatalogo(List<T> elements, Function<T, Node> creaCard) {
        container.getChildren().clear();
        elements.stream().map(creaCard).forEach(container.getChildren()::add);
    }

    /**
     * Crea una card generica per brani, autori, ecc.
     *
     * @param titolo Titolo della card (nome del brano o elemento del dizionario)
     * @param sottotitolo Usato solo nelle card brano per l'autore del brano
     * @param imageFile Cover della card
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
        titoloText.setWrappingWidth(154);
        titoloText.setTextAlignment(TextAlignment.CENTER);
        titoloText.getStyleClass().addAll("text-white", "font-bold", "text-base");

        card.getChildren().addAll(cover, titoloText);

        if (sottotitolo != null && !sottotitolo.isEmpty()) {
            Text autoreText = new Text(StringUtils.capitalizzaTesto(sottotitolo));
            autoreText.getStyleClass().addAll("text-white", "font-light", "text-sm");
            autoreText.setWrappingWidth(154);
            autoreText.setTextAlignment(TextAlignment.CENTER);
            card.getChildren().add(autoreText);
        }

        return card;
    }

    /**
     * Crea una card specifica per un brano.
     *
     * @param brano Brano di cui si vuole creare la card
     */
    public VBox creaCardBrano(Brano brano) {
        File imageFile = new File(COVER_PATH + brano.getIdBrano() + ".jpg");
        VBox card = creaCard(brano.getTitolo(), String.join(", ", brano.getAutori()), imageFile);

        card.setOnMouseClicked(e -> mainController.goToBrano(card, brano, () -> {
            BranoController controller = mainController.getBranoController();
            if (controller != null) controller.fetchBranoData(brano);
        }));

        return card;
    }

    /**
     * Crea una card per il catalogo generico (autori, esecutori).
     *
     * @param titoloCard Titolo della card
     */
    public VBox creaCardCatalogo(String titoloCard) {
        String viewType = getViewType(); // "autori" o "esecutori"

        File imageFile = null;

        if ("autori".equals(viewType)) {
            imageFile = new File(AUTORI_COVER_PATH + titoloCard.replaceAll("\\s+", "_") + ".jpg");
        } else if ("esecutori".equals(viewType)) {
            imageFile = new File(ESECUTORI_COVER_PATH + titoloCard.replaceAll("\\s+", "_") + ".png");
        }

        VBox card = creaCard(titoloCard, null, imageFile);

        card.setOnMouseClicked(e -> {
            Type branoType = new TypeToken<List<Brano>>() {}.getType();
            List<Brano> brani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

            List<Brano> braniFiltrati;
            if ("autori".equals(viewType)) {
                braniFiltrati = brani.stream()
                        .filter(b -> b.getAutori().contains(titoloCard))
                        .toList();
            } else if ("esecutori".equals(viewType)) {
                braniFiltrati = brani.stream()
                        .filter(b -> b.getEsecutori().contains(titoloCard))
                        .toList();
            } else {
                braniFiltrati = List.of();
            }

            generaCatalogo(braniFiltrati, this::creaCardBrano);
            esploraTitle.setText(StringUtils.capitalizzaTesto(titoloCard));
        });

        return card;
    }

    /**
     * Crea una card personalizzata per un genere musicale.
     *
     * @param brani Lista dei brani letta da json
     * @param genere Genere di cui si vuole creare la card
     */
    public GridPane creaCardGenere(List<Brano> brani, String genere) {
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
        Text titolo = new Text(StringUtils.capitalizzaTesto(genere));
        titolo.getStyleClass().addAll("text-white", "font-black", "text-2xl");
        GridPane.setMargin(titolo, new Insets(15));
        card.add(titolo, 0, 0);

        // Ottieni immagine del primo brano nel genere
        String idBrano = Brano.cercaBranoConDizionario(brani, genere, "generi").getFirst().getIdBrano();
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
            generaCatalogo(braniDelGenere, this::creaCardBrano);
            // Imposta il titolo al genere selezionato
            esploraTitle.setText(StringUtils.capitalizzaTesto(genere));
        });

        return card;
    }

    /**
     * Estrae il colore dominante da un'immagine (campionamento 5x5 pixel).
     *
     * @param image Immagine di cui si vuole estrarre il colore
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
