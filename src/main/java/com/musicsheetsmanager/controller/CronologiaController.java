package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.Commento;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import com.musicsheetsmanager.config.SessionManager;

public class CronologiaController implements Controller{

    @FXML private FlowPane container;

    private MainController mainController;

    private List<Brano> braniCommentati;

    private final String USERNAME = SessionManager.getLoggedUser().getUsername();
    private static final Path BRANI_JSON_PATH = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", "brani.json");
    private static final Path COMMENTI_JSON_PATH = Paths.get("src", "main", "resources", "com", "musicsheetsmanager", "data", "commenti.json");
    private static final String COVER_PATH = "src/main/resources/com/musicsheetsmanager/ui/covers/";
    private static final String DEFAULT_COVER = "src/main/resources/com/musicsheetsmanager/ui/Cover.jpg";

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public List<Brano> getBraniCommentati() {
        return braniCommentati;
    }

    @FXML
    public void initialize() {
        mostraCronologiaCommenti();
    }

    /**
     * Renderizza le card dei brani in cui l'utente ha commentato
     */
    private void mostraCronologiaCommenti() {
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        Type commentoType = new TypeToken<List<Commento>>() {}.getType();

        List<Brano> brani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);
        List<Commento> commenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        // Inverti per ottenere l’ordine dal più recente
        Collections.reverse(commenti);

        Map<String, Commento> commentiUtentePerBrano = new LinkedHashMap<>();

        for (Commento commento : commenti) {
            if (USERNAME.equals(commento.getUsername())) {
                for (Brano brano : brani) {
                    if (brano.getIdCommenti().contains(commento.getIdCommento())
                            && !commentiUtentePerBrano.containsKey(brano.getIdBrano())) {
                        commentiUtentePerBrano.put(brano.getIdBrano(), commento);
                        break;
                    }
                }
            }
        }

        braniCommentati = commentiUtentePerBrano.keySet().stream()
                .map(id -> brani.stream().filter(b -> b.getIdBrano().equals(id)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();

        generaCatalogo(braniCommentati, this::creaCardBrano);
    }

    /**
     * Aggiunge al container le card generate
     *
     * @param elementi Lista di cui si vuole creare le card
     * @param creaCard Funzione di creazione card utilizzata
     */
    public <T> void generaCatalogo(List<T> elementi, Function<T, Node> creaCard) {
        container.getChildren().clear();
        elementi.stream().map(creaCard).forEach(container.getChildren()::add);
    }

    /**
     * Crea una card personalizzata per il brano
     *
     * @param brano Brano di cui si vuole creare la card
     *
     * @return Card creata a partire dal brano
     */
    public VBox creaCardBrano(Brano brano) {
        File imageFile = new File(COVER_PATH + brano.getIdBrano() + ".jpg");
        VBox card = creaCard(brano.getTitolo(), String.join(", ", brano.getAutori()), imageFile);

        // Funzione per andare alla pagina del brano quando si clicca la card
        card.setOnMouseClicked(e -> mainController.goToBrano(card, brano, () -> {
            BranoController controller = mainController.getBranoController();
            if (controller != null) controller.fetchBranoData(brano);
        }));

        return card;
    }

    /**
     * Funzione generica per creare una card
     *
     * @param titolo Titolo del brano
     * @param sottotitolo Nome dell'autore
     * @param imageFile Cover da assegnare alla card
     *
     * @return Card creata
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
        titoloText.setWrappingWidth(154);
        titoloText.setTextAlignment(TextAlignment.CENTER);

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
}

