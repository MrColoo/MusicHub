package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Commento;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

public class BranoController {

    @FXML
    private VBox fileListVBox;

    @FXML
    private Button inviaBtn;

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

                if (focusOwner == campoCommento || focusOwner == campoNota || focusOwner == inviaBtn) {
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
        campoNota.focusedProperty().addListener(focusListener);
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
                .filter(c -> brano.getIdCommenti().contains(c.getIdCommento()))
                .toList();

        commentiContainer.getChildren().clear();
        renderCommenti(commentiBrano, commentiContainer, 0);
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

    public VBox creaCommentoBox(Commento commento, int indentLevel) {
        // container esterno
        VBox commentBox = new VBox();
        commentBox.getStyleClass().add("brano-allegati-container");
        commentBox.setTranslateX(indentLevel * 30);
        commentBox.setPadding(new Insets(15));

        // riga utente + bottoni
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setPadding(new Insets(0, 0, 5, 0));
        topRow.setSpacing(10);

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

        commentBox.getChildren().addAll(topRow, commentFlow);

        deleteButton.setOnAction(e -> onDeleteBtnClick(commento));

        replyButton.setOnAction(e -> onReplyCommentoBtnClick(commento));

        return commentBox;
    }

    // renderizza i commenti in base al livello di annidamento
    private void renderCommenti(List<Commento> commenti, VBox container, int indentLevel) {
        for (Commento commento : commenti) {
            VBox commentoBox = creaCommentoBox(commento, indentLevel);
            container.getChildren().add(commentoBox);
            if (commento.getRisposte() != null) {
                renderCommenti(commento.getRisposte(), container, indentLevel + 1);
            }
        }
    }

    public void onDeleteBtnClick (Commento commento) {
        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> listaCommenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        // elimina il commento
        listaCommenti = listaCommenti.stream()
                .filter(c -> !c.getIdCommento().equals(commento.getIdCommento()))
                .collect(Collectors.toList());

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

    public void onReplyCommentoBtnClick (Commento commento) {
        currentCommento = commento;
        Platform.runLater(() -> campoCommento.requestFocus());
        System.out.println("Risposta al commento: " + commento.getTesto());
    }
}
