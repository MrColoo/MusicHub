package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Commento;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class BranoController {

    @FXML
    private ScrollPane branoScrollPane;

    @FXML
    private VBox fileListVBox;

    @FXML
    private Button inviaCommentoBtn;

    @FXML
    ImageView branoCover;
    @FXML
    Text branoTitolo;
    @FXML
    Text branoAutori;

    @FXML
    private TextArea campoCommento;
    @FXML
    private TextArea campoNota;

    private String idBrano;

    @FXML
    private VBox commentiContainer;

    @FXML
    private VBox noteContainer;

    private Brano currentBrano;

    private Commento currentCommento; // commento a cui si sta rispondendo

    private static final Path COMMENTI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "commenti.json"
    );

    private static final Path BRANI_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "brani.json"
    );

    @FXML
    public void initialize() {
        // errore.setVisible(false);
        Platform.runLater(this::checkReplyFocus);
    }

    // se l'utente deseleziona il textField(ignora reply, invia) non risponde più
    private void checkReplyFocus() {
        ChangeListener<Boolean> focusListener = (obs, oldVal, newVal) -> {
            if (!newVal && currentCommento != null) { // focus perso su campoCommento o campoNota
                Scene scene = campoCommento.getScene();
                if (scene == null) return;

                Node focusOwner = scene.getFocusOwner();
                if (focusOwner == null) {
                    currentCommento = null;
                    System.out.println("Focus fuori dalla scena");
                    return;
                }

                boolean focusOnValidNode = false;

                if ((focusOwner == campoCommento && !currentCommento.isNota())
                        || (focusOwner == inviaCommentoBtn && !currentCommento.isNota())
                ) {
                    focusOnValidNode = true;
                } else if (focusOwner instanceof Button btn && btn.getStyleClass().contains("reply-btn")) {
                    // vedo se è replyButton
                    focusOnValidNode = true;
                }

                if (!focusOnValidNode) {
                    System.out.println("Non rispondi più");
                    currentCommento = null;
                }
            }
        };

        campoCommento.focusedProperty().addListener(focusListener);
    }


    // mostra i dati del brano(titolo, autore ecc...) quando l'utente interagisce con un brano in Esplora
    public void fetchBranoData(Brano brano) {
        currentBrano = brano;
        idBrano = brano.getIdBrano();
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

        // carica tutti i commenti da json per la ricerca
        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> commenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        // filtra i commenti appartenenti al brano
        List<Commento> commentiBrano = commenti.stream()
                .filter(c ->
                                (!c.isNota()) &&
                                        (brano.getIdCommenti().contains(c.getIdCommento()))
                )
                .toList();

        commentiContainer.getChildren().clear();
        renderCommenti(commentiBrano, commentiContainer, 0);

        // filtra le note appartenenti al brano
        List<Commento> noteBrano = commenti.stream()
                .filter(n ->
                        (n.isNota()) &&
                                (brano.getIdCommenti().contains(n.getIdCommento()))
                )
                .toList();

        mostraNote(noteBrano);
    }

    public void mostraNote(List<Commento> noteBrano) {
        noteContainer.getChildren().clear();

        for (Commento nota : noteBrano) {
            Text noteText = new Text("@" + nota.getUsername() + ": " + nota.getTesto());
            noteText.getStyleClass().addAll("text-white", "font-book", "text-base");
            noteText.setWrappingWidth(noteContainer.getMaxWidth() - 20);
            VBox.setMargin(noteText, new Insets(10, 0, 0, 0));
            noteContainer.getChildren().add(noteText);
        }
    }

    //TODO AGGIUNGERE MESSAGGIO DI ERRORE COMMENTO VUOTO

    // funzione per salvare un commento generico
    private boolean aggiungiCommento(String testo, boolean isNota) {
        // controllo se commento è vuoto
        if(testo.isBlank()) {
            //errore.setText("Il testo non può essere vuoto");
            //errore.setVisible(true);
            return false;
        }

        Commento nuovoCommento = new Commento(testo, SessionManager.getLoggedUser().getUsername(), isNota);

        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> listaCommenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        if(currentCommento != null) { // sto rispondendo
           if(aggiungiRisposta(listaCommenti, currentCommento.getIdCommento(), nuovoCommento)) {
               System.out.println("Risposta aggiunta a " + currentCommento.toString());
           }
        } else { // commento nuovo
            listaCommenti.add(nuovoCommento);
            Commento.linkIdcommentoBrano(idBrano, nuovoCommento.getIdCommento(), BRANI_JSON_PATH);
        }

        JsonUtils.scriviSuJson(listaCommenti, COMMENTI_JSON_PATH);
        currentCommento = null;

        return true;
    }

    // funzione ricorsiva che trova il commento a cui si sta rispondendo nella struttura ad albero
    private boolean aggiungiRisposta (List<Commento> commenti, String idCommentoPadre, Commento risposta) {
        for (Commento commento: commenti) {
            if (commento.getIdCommento().equals(idCommentoPadre)) {     // trovato commento padre
                commento.aggiungiRisposta(risposta);
                return true;
            }
            // vedo nelle risposte annidate
            if(commento.getRisposte() != null && aggiungiRisposta(commento.getRisposte(), idCommentoPadre, risposta)){
                return true;
            }
        }
        return false;
    }

    @FXML
    public void OnAddCommentoClick(){
        String testoCommento = campoCommento.getText().trim();
        if(aggiungiCommento(testoCommento, false)) {
            System.out.println("Commento salvato con successo: "
                    + testoCommento
                    + ", id brano: "
                    + idBrano
                    + ", username: " + SessionManager.getLoggedUser().getUsername());
            campoCommento.clear();
            currentBrano = reloadCurrentBrano();
            fetchBranoData(currentBrano);
        }
    }

    @FXML
    public void OnAddNotaClick(){
        String testoNota = campoNota.getText().trim();
        if(aggiungiCommento(testoNota, true)) {
            System.out.println("Nota salvata con successo: "
                    + testoNota
                    + ", id brano: "
                    + idBrano
                    + ", username: " + SessionManager.getLoggedUser().getUsername());
            campoNota.clear();
            currentBrano = reloadCurrentBrano();
            fetchBranoData(currentBrano);
        }
    }

    // ricarica brano corrente con dati aggiornati
    private Brano reloadCurrentBrano() {
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> brani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        return brani.stream()
                .filter(b -> b.getIdBrano().equals(idBrano))
                .findFirst()
                .orElse(null);
    }

    public HBox creaCommentoBox(Commento commento, int indentLevel) {
        // HBox esterno per gestire l'allineamento e l'indentazione
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.TOP_LEFT);
        wrapper.setSpacing(0);
        wrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // indentazione
        Region indent = new Region();
        indent.setPrefWidth(indentLevel * 50);
        wrapper.getChildren().add(indent);

        VBox commentBox = new VBox();
        commentBox.getStyleClass().add("brano-allegati-container");
        commentBox.setPadding(new Insets(15));
        commentBox.setSpacing(5);

        HBox.setHgrow(commentBox, Priority.ALWAYS);
        commentBox.setMaxWidth(Double.MAX_VALUE);

        // riga utente + bottoni
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setSpacing(10);
        topRow.setPadding(new Insets(0, 0, 5, 0));

        Text usernameText = new Text("@" + commento.getUsername());
        usernameText.getStyleClass().addAll("text-white", "font-bold", "text-base");

        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("delete-btn");
        ImageView deleteIcon = new ImageView(
                new Image(Objects.requireNonNull(BranoController.class.getResource("/com/musicsheetsmanager/ui/icons/trash-bold.png")).toExternalForm())
        );
        deleteIcon.setFitWidth(20);
        deleteIcon.setPreserveRatio(true);
        deleteButton.setGraphic(deleteIcon);

        Button replyButton = new Button("Rispondi");
        replyButton.getStyleClass().add("reply-btn");

        topRow.getChildren().addAll(usernameText, deleteButton, replyButton);

        // testo commento
        Text commentText = new Text(commento.getTesto());
        commentText.getStyleClass().addAll("text-white", "font-book", "text-base");

        TextFlow commentFlow = new TextFlow(commentText);
        commentFlow.setPrefWidth(600);
        commentText.wrappingWidthProperty().bind(commentFlow.widthProperty().subtract(10));

        commentBox.getChildren().addAll(topRow, commentFlow);
        wrapper.getChildren().add(commentBox);

        deleteButton.setOnAction(e -> onDeleteBtnClick(commento));
        replyButton.setOnAction(e -> onReplyBtnClick(commento));

        return wrapper;
    }


    // renderizza i commenti in base al livello di annidamento
    private void renderCommenti(List<Commento> commenti, VBox container, int indentLevel) {
        for (Commento commento : commenti) {
            HBox commentoBox = creaCommentoBox(commento, indentLevel);
            container.getChildren().add(commentoBox);
            if (commento.getRisposte() != null) {
                renderCommenti(commento.getRisposte(), container, indentLevel + 1);
            }
        }
    }

    public void onDeleteBtnClick (Commento commento) {
        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> listaCommenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        listaCommenti = rimuoviCommentoRicorsivo(listaCommenti, commento.getIdCommento());

        // aggiorna json
        JsonUtils.scriviSuJson(listaCommenti, COMMENTI_JSON_PATH);

        Brano.rimuoviCommentoBrano(idBrano, commento.getIdCommento(), BRANI_JSON_PATH);

        currentBrano = reloadCurrentBrano();
        fetchBranoData(currentBrano);

        System.out.println("Commento eliminato con successo: "
                + commento.getTesto()
                + ", id brano: "
                + idBrano
                + ", username: " + commento.getUsername());
    }

    // rimuove il commento/la nota/la risposta e le eventuali risposte annidate
    private List<Commento> rimuoviCommentoRicorsivo(List<Commento> commenti, String idCommento) {
        return commenti.stream()
                .filter(c -> !c.getIdCommento().equals(idCommento))
                .peek(c -> {
                    if (c.getRisposte() != null) {
                        c.setRisposte(rimuoviCommentoRicorsivo(c.getRisposte(), idCommento));
                    }
                })
                .collect(Collectors.toList());
    }

    public void onReplyBtnClick (Commento commento) {
        currentCommento = commento;

        campoCommento.requestFocus();
        // scroll fino a fondo pagina dove si trova campoCommento
        double targetVvalue = 1.0; // fondo pagina
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(branoScrollPane.vvalueProperty(), targetVvalue);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);   // 300ms animazione
        timeline.getKeyFrames().add(kf);
        timeline.play();
        System.out.println("Risposta al commento: " + commento.getTesto());
    }
}
