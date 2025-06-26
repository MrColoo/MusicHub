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
import javafx.scene.layout.*;
import javafx.scene.image.PixelReader;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.musicsheetsmanager.model.Brano;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.stage.FileChooser;

public class BranoController {

    // ScrollPane che probabilmente contiene tutta la visualizzazione del brano (scrollabile)
    @FXML
    private ScrollPane branoScrollPane;

    // GridPane usato per mostrare contenuti multimediali (es. audio, video, immagini)
    @FXML
    private GridPane mediaGridPane;

    // GridPane usato per visualizzare eventuali allegati (es. PDF, immagini, spartiti)
    @FXML
    private GridPane allegatiGridPane;

    // Pulsante per inviare un commento (ad esempio sotto un brano)
    @FXML
    private Button inviaCommentoBtn;

    // Immagine di copertina del brano (tipicamente visibile nel banner o scheda)
    @FXML
    ImageView branoCover;

    // Testo che mostra il titolo del brano
    @FXML
    Text branoTitolo;

    // Testo che mostra il genere del brano
    @FXML
    Text genereText;

    // Testo che mostra gli strumenti musicali del brano
    @FXML
    Text strumentiMusicaliText;

    // Testo che mostra gli autori del brano
    @FXML
    Text branoAutori;

    // Barra/banner in alto che mostra info sintetiche del brano
    @FXML
    private HBox branoBanner;

    // Area di testo dove l'utente può scrivere un commento
    @FXML
    private TextArea campoCommento;

    // Area di testo dove l'utente può scrivere una nota (diversa da un commento, es. nota privata)
    @FXML
    private TextArea campoNota;

    // ID univoco del brano attualmente visualizzato o modificato
    private String idBrano;

    // Riferimento a un pulsante "Elimina" (forse creato dinamicamente)
    private Button deleteButton;

    // Nome utente o ID del proprietario del brano
    private String branoOwner;

    // Contenitore (VBox) per visualizzare tutti i commenti legati al brano
    @FXML
    private VBox commentiContainer;

    // Contenitore (VBox) per visualizzare tutte le note associate al brano
    @FXML
    private VBox noteContainer;

    // Oggetto che rappresenta il brano attualmente selezionato/visualizzato
    private Brano currentBrano;

    // Oggetto che rappresenta il commento attualmente selezionato (utile per risposte threadate)
    private Commento currentCommento;



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

    /**
     * Controlla se l'utente ha perso il focus dal campo commento in una risposta.
     * Se il focus esce da tutti i componenti rilevanti (campo, bottone invia o bottone reply),
     * viene annullato il riferimento al commento a cui si stava rispondendo (currentCommento).
     */
    private void checkReplyFocus() {
        ChangeListener<Boolean> focusListener = (obs, oldVal, newVal) -> { // Listener che si attiva quando cambia il focus sul campo commento

            // Esegui la logica solo se si stava rispondendo a un commento
            if (!newVal && currentCommento != null) { // focus perso su campoCommento o campoNota
                Scene scene = campoCommento.getScene();
                if (scene == null) return;

                // Ottiene il nodo attualmente in focus nella scena
                Node focusOwner = scene.getFocusOwner();
                if (focusOwner == null) {
                    currentCommento = null;
                    System.out.println("Focus fuori dalla scena");
                    return;
                }

                // Flag che indica se il focus è ancora su un'area valida
                boolean focusOnValidNode = false;

                //Se il focus è ancora sul campo commento e non è una nota
                if ((focusOwner == campoCommento && !currentCommento.isNota())
                        || (focusOwner == inviaCommentoBtn && !currentCommento.isNota())
                ) {
                    focusOnValidNode = true;
                } else if (focusOwner instanceof Button btn && btn.getStyleClass().contains("reply-btn")) { // Oppure se il focus è su un bottone risposta
                    focusOnValidNode = true;
                }

                if (!focusOnValidNode) {  // Se il focus non è su uno di questi elementi
                    System.out.println("Non rispondi più");
                    currentCommento = null;
                }
            }
        };

        campoCommento.focusedProperty().addListener(focusListener); // Aggiunge il listener alla proprietà di focus del campo commento
    }

    /**
     * Mostra i dati del brano selezionato dall'utente nella schermata "Esplora".
     * Carica titolo, autori, copertina, commenti, note, allegati, media e aggiorna la UI.
     *
     * @param brano il brano selezionato
     */
    public void fetchBranoData(Brano brano) {
        currentBrano = brano; // Salva il brano corrente per uso interno
        branoOwner = brano.getProprietario();  // Memorizza il proprietario e l'ID del brano
        idBrano = brano.getIdBrano();
        branoTitolo.setText(brano.getTitolo()); // Mostra il titolo del brano nell'interfaccia

        String generi = String.join(", ", brano.getGeneri());// Mostra i generi del brano, separati da virgole
        genereText.setText(generi); // Mostra il genere del brano nell'interfaccia

        String strumenti = String.join(", ", brano.getStrumentiMusicali());// Mostra gli strumenti del brano, separati da virgole
        strumentiMusicaliText.setText(strumenti);// Mostra gli strumenti usati nel brano nell'interfaccia

        // Mostra gli autori del brano, separati da virgole
        String autori = String.join(", ", brano.getAutori());
        branoAutori.setText(autori);

        // Gestione dell'immagine di copertina del brano
        String idBrano = brano.getIdBrano();
        File imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/covers/" + idBrano + ".jpg");

        // Se la copertina specifica non esiste, usa un'immagine di default
        if (!imageFile.exists()) {
            imageFile = new File("src/main/resources/com/musicsheetsmanager/ui/Cover.jpg");
        }
        branoCover.setImage(new Image(imageFile.toURI().toString()));

        // Carica tutti i commenti da json per la ricerca
        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> commenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        // Filtra solo i commenti legati a questo brano e che NON sono note
        List<Commento> commentiBrano = commenti.stream()
                .filter(c ->
                                (!c.isNota()) &&
                                        (brano.getIdCommenti().contains(c.getIdCommento()))
                )
                .toList();

        // Pulisce la sezione dei commenti e li reinserisce aggiornati
        commentiContainer.getChildren().clear();
        renderCommenti(commentiBrano, commentiContainer, 0);

        // Filtra le note appartenenti al brano
        List<Commento> noteBrano = commenti.stream()
                .filter(n ->
                        (n.isNota()) &&
                                (brano.getIdCommenti().contains(n.getIdCommento()))
                )
                .toList();

        mostraNote(noteBrano);

        caricaAllegatiBrano(brano, allegatiGridPane);

        caricaMediaBrano(brano, mediaGridPane);

        Image albumCover = new Image(imageFile.toURI().toString()); // Crea oggetto immagine per calcolo colore dominante

        // Estrae il colore dominante e lo imposta come sfondo del banner del brano
        BackgroundFill bgFill = new BackgroundFill(estraiColoriDominanti(albumCover), CornerRadii.EMPTY, Insets.EMPTY);
        branoBanner.setBackground(new Background(bgFill));

        branoCover.setImage(albumCover); // Reimposta l'immagine di copertina per sicurezza/riferimento
    }

    /**
     * Visualizza le note associate a un brano nel contenitore `noteContainer`.
     * Ogni nota è mostrata con l'autore e il contenuto.
     *
     * @param noteBrano lista di oggetti Commento che rappresentano le note del brano
     */
    public void mostraNote(List<Commento> noteBrano) {
        noteContainer.getChildren().clear();

        // Itera su ogni nota del brano e aggiunge al container
        for (Commento nota : noteBrano) {
            Text noteText = new Text("@" + nota.getUsername() + ": " + nota.getTesto());
            noteText.getStyleClass().addAll("text-white", "font-book", "text-base");
            noteText.setWrappingWidth(noteContainer.getMaxWidth() - 20);
            VBox.setMargin(noteText, new Insets(10, 0, 0, 0));
            noteContainer.getChildren().add(noteText);
        }
    }

    /**
     * Salva un nuovo commento o una nota, oppure una risposta a un commento esistente.
     *
     * @param testo   il contenuto del commento o della nota
     * @param isNota  true se si tratta di una nota, false se è un commento pubblico
     * @return true se il commento è stato salvato con successo, false se il testo era vuoto
     */
    private boolean aggiungiCommento(String testo, boolean isNota) {
        // Se il campo è vuoto o contiene solo spazi, non salvare nulla
        if(testo.isBlank()) {
            return false;
        }

        Commento nuovoCommento = new Commento(testo, SessionManager.getLoggedUser().getUsername(), isNota); // Crea un nuovo oggetto Commento con autore (utente loggato), testo e tipo

        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> listaCommenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        if(currentCommento != null) {  // Se si sta rispondendo a un commento esistente
           if(aggiungiRisposta(listaCommenti, currentCommento.getIdCommento(), nuovoCommento)) {
               System.out.println("Risposta aggiunta a " + currentCommento.toString());
           }
        } else {  // Altrimenti si tratta di un nuovo commento
            listaCommenti.add(nuovoCommento);
            Commento.linkIdcommentoBrano(idBrano, nuovoCommento.getIdCommento(), BRANI_JSON_PATH);
        }

        JsonUtils.scriviSuJson(listaCommenti, COMMENTI_JSON_PATH);
        currentCommento = null;

        return true;
    }

    /**
     * Funzione ricorsiva che cerca un commento all'interno di una struttura ad albero
     * e aggiunge una risposta al commento padre, se trovato.
     *
     * @param commenti           la lista di commenti
     * @param idCommentoPadre    l'ID del commento a cui si vuole rispondere
     * @param risposta           il nuovo commento da inserire come risposta
     * @return true se la risposta è stata aggiunta correttamente, false altrimenti
     */
    private boolean aggiungiRisposta (List<Commento> commenti, String idCommentoPadre, Commento risposta) {
        for (Commento commento: commenti) {
            // Controlla se il commento corrente è quello a cui si sta rispondendo
            if (commento.getIdCommento().equals(idCommentoPadre)) {
                commento.aggiungiRisposta(risposta);
                return true;
            }
            // In caso contrario, cerca ricorsivamente all'interno delle sue risposte
            if(commento.getRisposte() != null && aggiungiRisposta(commento.getRisposte(), idCommentoPadre, risposta)){
                return true;
            }
        }
        return false; // Se il commento padre non è stato trovato in nessun livello, restituisce false
    }

    /**
     * Metodo associato al pulsante per aggiunger un commento.
     * Recupera il testo inserito dall'utente, prova ad aggiungerlo come commento
     * e aggiorna l'interfaccia in caso di successo.
     */
    @FXML
    public void OnAddCommentoClick(){
        String testoCommento = campoCommento.getText().trim();

        // Prova ad aggiungere il commento
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

    /**
     * Metodo associato al pulsante per aggiungere nota.
     * Recupera il testo inserito nel campo nota, lo salva come commento
     * marcato come nota e aggiorna la visualizzazione del brano.
     */
    @FXML
    public void OnAddNotaClick(){
        String testoNota = campoNota.getText().trim();
        // Prova ad aggiungere la nota
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

    /**
     * Ricarica dal file JSON il brano corrente (identificato da idBrano)
     * per assicurarsi di lavorare con i dati più aggiornati.
     *
     * @return l'oggetto Brano aggiornato, oppure null se non trovato
     */
    private Brano reloadCurrentBrano() {
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> brani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        return brani.stream() // Cerca nella lista il brano con l'ID corrispondente a quello attualmente selezionato
                .filter(b -> b.getIdBrano().equals(idBrano))
                .findFirst()
                .orElse(null);
    }

    /**
     * Crea dinamicamente una struttura grafica (HBox) per rappresentare un commento,
     * con indentazione per visualizzare le risposte in forma gerarchica.
     *
     * @param commento    l'oggetto Commento da rappresentare
     * @param indentLevel livello di indentazione (0 = commento principale, >0 = risposta)
     * @return HBox che rappresenta il commento formattato
     */
    public HBox creaCommentoBox(Commento commento, int indentLevel) {
        // HBox esterno per gestire l'allineamento e l'indentazione
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.TOP_LEFT);
        wrapper.setSpacing(0);
        wrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Spazio di indentazione per le risposte annidate
        Region indent = new Region();
        indent.setPrefWidth(indentLevel * 50);
        wrapper.getChildren().add(indent);

        // VBox che conterrà il commento, autore, bottoni, testo
        VBox commentBox = new VBox();
        commentBox.getStyleClass().add("brano-allegati-container");
        commentBox.setPadding(new Insets(15));
        commentBox.setSpacing(5);

        HBox.setHgrow(commentBox, Priority.ALWAYS);
        commentBox.setMaxWidth(Double.MAX_VALUE);

        // Sezione superiore: nome utente, badge, pulsanti
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setSpacing(5);
        topRow.setPadding(new Insets(0, 0, 5, 0));

        // Testo con il nome utente che ha scritto il commento
        Text usernameText = new Text("@" + commento.getUsername());
        usernameText.getStyleClass().addAll("font-bold", "text-base");

        // Icona "verificato" se l'utente è uno degli autori del brano
        ImageView verifiedIcon = new ImageView();
        Image verifiedImage = new Image(Objects.requireNonNull(BranoController.class.getResource("/com/musicsheetsmanager/ui/icons/verified.png")).toExternalForm());
        if(currentBrano.getAutori().contains(commento.getUsername())){
            usernameText.setFill(Color.DODGERBLUE);
            verifiedIcon.setImage(verifiedImage);
            verifiedIcon.setFitWidth(20);
            verifiedIcon.setPreserveRatio(true);
        } else
            usernameText.setFill(Color.WHITE);

        // Pulsante per eliminare il commento
        deleteButton = new Button();
        deleteButton.getStyleClass().add("delete-btn");
        ImageView deleteIcon = new ImageView(
                new Image(Objects.requireNonNull(BranoController.class.getResource("/com/musicsheetsmanager/ui/icons/trash-bold.png")).toExternalForm())
        );
        deleteIcon.setFitWidth(20);
        deleteIcon.setPreserveRatio(true);
        deleteButton.setGraphic(deleteIcon);

        // Mostra il pulsante elimina solo se:
        // - l'utente è admin
        // - è l'autore del commento
        // - è il proprietario del brano
        if(SessionManager.getLoggedUser().isAdmin()
                || (SessionManager.getLoggedUser().getUsername().equals(commento.getUsername()))
                || (SessionManager.getLoggedUser().getUsername().equals(branoOwner))
        ){
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        } else {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }

        // Pulsante per rispondere al commento
        Button replyButton = new Button("Rispondi");
        replyButton.getStyleClass().add("reply-btn");

        topRow.getChildren().addAll(usernameText, verifiedIcon, deleteButton, replyButton);

        // Testo del commento
        Text commentText = new Text(commento.getTesto());
        commentText.getStyleClass().addAll("text-white", "font-book", "text-base");

        TextFlow commentFlow = new TextFlow(commentText); // TextFlow per gestire automaticamente il wrapping del testo
        commentFlow.setPrefWidth(600);
        commentText.wrappingWidthProperty().bind(commentFlow.widthProperty().subtract(10));

        commentBox.getChildren().addAll(topRow, commentFlow);
        wrapper.getChildren().add(commentBox);

        // Assegna le azioni ai pulsanti
        deleteButton.setOnAction(e -> onDeleteBtnClick(commento));
        replyButton.setOnAction(e -> onReplyBtnClick(commento));

        return wrapper;
    }


    /**
     * Renderizza ricorsivamente una lista di commenti all'interno di un contenitore VBox.
     * Ogni commento viene rappresentato graficamente con indentazione in base al livello.
     *
     * @param commenti     lista di commenti da visualizzare
     * @param container    contenitore VBox dove inserire i commenti renderizzati
     * @param indentLevel  livello di indentazione (0 = commenti principali, >0 = risposte)
     */
    private void renderCommenti(List<Commento> commenti, VBox container, int indentLevel) {
        for (Commento commento : commenti) {
            HBox commentoBox = creaCommentoBox(commento, indentLevel);
            container.getChildren().add(commentoBox);

            // Se il commento ha delle risposte, chiama ricorsivamente la funzione
            // con indentLevel incrementato per aumentare il rientro visivo
            if (commento.getRisposte() != null) {
                renderCommenti(commento.getRisposte(), container, indentLevel + 1);
            }
        }
    }

    /**
     * Gestisce la rimozione di un commento (o risposta) dal file JSON e aggiorna l'interfaccia.
     *
     * @param commento il commento da eliminare
     */
    public void onDeleteBtnClick (Commento commento) {
        Type commentoType = new TypeToken<List<Commento>>() {}.getType();
        List<Commento> listaCommenti = JsonUtils.leggiDaJson(COMMENTI_JSON_PATH, commentoType);

        listaCommenti = rimuoviCommentoRicorsivo(listaCommenti, commento.getIdCommento()); // Rimuove ricorsivamente il commento (e sue eventuali risposte)

        // Aggiorna JSON
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

    /**
     * Rimuove un commento (o nota o risposta) identificato da idCommento da una lista di commenti.
     * Se il commento da rimuovere ha risposte annidate, queste vengono rimosse ricorsivamente.
     *
     * @param commenti    lista dei commenti da elaborare
     * @param idCommento  ID del commento da rimuovere
     * @return una nuova lista di commenti aggiornata, senza il commento (e relative risposte) specificato
     */
    private List<Commento> rimuoviCommentoRicorsivo(List<Commento> commenti, String idCommento) {
        return commenti.stream()
                .filter(c -> !c.getIdCommento().equals(idCommento)) // Filtra i commenti: rimuove quello con ID corrispondente
                .peek(c -> { // Per ogni commento ancora presente, controlla se ha risposte annidate
                    if (c.getRisposte() != null) {
                        c.setRisposte(rimuoviCommentoRicorsivo(c.getRisposte(), idCommento));
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Gestisce il click sul pulsante "Rispondi" di un commento.
     * Imposta il commento selezionato come destinatario della risposta,
     * sposta il focus sul campo di inserimento e scrolla alla fine della pagina.
     *
     * @param commento il commento a cui si sta rispondendo
     */
    public void onReplyBtnClick (Commento commento) {
        currentCommento = commento;

        campoCommento.requestFocus();
        // Scrolla dolcemente fino in fondo allo ScrollPane
        double targetVvalue = 1.0;
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(branoScrollPane.vvalueProperty(), targetVvalue);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);   // 300ms animazione
        timeline.getKeyFrames().add(kf);
        timeline.play();
        System.out.println("Risposta al commento: " + commento.getTesto());
    }

    /**
     * Aggiunge una lista di file come allegati visivi all'interno di un GridPane.
     * Ogni file viene visualizzato in una cella, si adatta dinamicamente.
     *
     * @param files    lista di file selezionati dall'utente
     * @param gridPane il contenitore grafico dove mostrare i file
     */
    private void aggiungiFileAllegati(List<File> files, GridPane gridPane) {
        // Pulisce la griglia rimuovendo ogni contenuto precedente
        gridPane.getChildren().clear();
        int row = 0; // Contatore per la riga corrente

        // Cicla su tutti i file forniti
        for (File file : files) {

            // === Colonna 0: Nome del file ===
            Text fileNameText = new Text(file.getName()); // Crea un nodo testo con il nome del file
            fileNameText.getStyleClass().addAll("font-light", "text-base"); // Applica classi di stile CSS
            GridPane.setColumnIndex(fileNameText, 0); // Posiziona nella colonna 0
            GridPane.setRowIndex(fileNameText, row);  // Nella riga corrente

            // === Colonna 1: Bottone APRI ===
            Button apriButton = new Button();
            // Carica l'icona per il bottone "apri"
            ImageView apriIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/arrow-square-out-bold.png"))
            );
            apriIcon.setFitWidth(20); // Larghezza icona
            apriIcon.setPreserveRatio(true); // Mantiene proporzioni
            apriButton.setGraphic(apriIcon); // Imposta l'icona nel bottone

            // Quando cliccato, apre il file con l'app predefinita del sistema
            apriButton.setOnAction(e -> {
                try {
                    if (file.exists()) Desktop.getDesktop().open(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            GridPane.setColumnIndex(apriButton, 1); // Colonna 1
            GridPane.setRowIndex(apriButton, row);  // Riga corrente

            // === Colonna 2: Bottone DOWNLOAD ===
            Button downloadButton = new Button();
            // Carica l'icona per il bottone "download"
            ImageView downloadIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/download-simple-bold.png"))
            );
            downloadIcon.setFitWidth(20);
            downloadIcon.setPreserveRatio(true);
            downloadButton.setGraphic(downloadIcon);

            // Quando cliccato, apre un FileChooser per salvare il file
            downloadButton.setOnAction(e -> {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName(file.getName()); // Suggerisce il nome originale
                    File destinazione = fileChooser.showSaveDialog(null); // Mostra dialogo per salvataggio
                    if (destinazione != null) {
                        // Copia il file selezionato nella destinazione scelta
                        Files.copy(file.toPath(), destinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            GridPane.setColumnIndex(downloadButton, 2); // Colonna 2
            GridPane.setRowIndex(downloadButton, row);  // Riga corrente

            // === Aggiunta di tutti i nodi alla griglia ===
            gridPane.getChildren().addAll(fileNameText, apriButton, downloadButton);

            // Passa alla riga successiva
            row++;
        }
    }

    /**
     * Carica e visualizza gli allegati associati a un brano all'interno di un GridPane.
     * Gli allegati sono file che il brano ha collegati e vengono mostrati con nome e icona.
     *
     * @param brano             il brano da cui leggere gli allegati
     * @param allegatiGridPane  il GridPane in cui visualizzare gli allegati
     */
    public void caricaAllegatiBrano(Brano brano, GridPane allegatiGridPane) {
        // Costruisce il percorso della cartella degli allegati del brano
        File folder = new File("src/main/resources/attachments/" + idBrano);

        // Verifica l'esistenza e la validità della cartella
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("La cartella degli allegati non esiste per il brano: " + idBrano);
            return;
        }

        // Filtra i file: solo PDF, TXT, JPG e PNG
        File[] fileArray = folder.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".pdf") || name.endsWith(".txt") || name.endsWith(".jpg") || name.endsWith(".png")) && file.isFile();
        });

        // Mostra i file validi nella griglia
        if (fileArray != null && fileArray.length > 0) {
            aggiungiFileAllegati(List.of(fileArray), allegatiGridPane);
        } else {
            System.out.println("Nessun file allegato valido trovato per il brano: " + idBrano);
        }
    }

    /**
     * Carica e visualizza i media (es. immagini, video, ecc.) collegati al brano nel GridPane specificato.
     *
     * @param brano         il brano da cui leggere i media
     * @param mediaGridPane il contenitore GridPane in cui visualizzare i media
     */
    public void caricaMediaBrano(Brano brano, GridPane mediaGridPane) {
        // Costruisce il percorso della cartella degli allegati multimediali del brano
        File folder = new File("src/main/resources/attachments/" + idBrano);

        // Verifica l'esistenza della cartella
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("La cartella degli allegati non esiste per il brano: " + idBrano);
            return;
        }

        // Filtra i file: solo MP3 e MP4 e MIDI
        File[] fileArray = folder.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".mp3") || name.endsWith(".mp4") || name.endsWith(".midi")) && file.isFile();
        });

        // Mostra i file multimediali nella griglia
        if (fileArray != null && fileArray.length > 0) {
            aggiungiMediaAllegati(List.of(fileArray), mediaGridPane);
        } else {
            System.out.println("Nessun file allegato valido trovato per il brano: " + idBrano);
        }

        // Carica i dati dei brani dal file JSON per ottenere il link YouTube
        try {
            Path jsonPath = Paths.get("src/main/resources/com/musicsheetsmanager/data/brani.json");
            Type listType = new TypeToken<List<Brano>>() {}.getType();
            List<Brano> tuttiIBrani = JsonUtils.leggiDaJson(jsonPath, listType);

            if (tuttiIBrani == null || tuttiIBrani.isEmpty()) {
                return;
            }

            // Cerca il brano corrente nella lista
            tuttiIBrani.stream()
                    .filter(b -> b.getIdBrano().equals(brano.getIdBrano()))
                    .findFirst()
                    .ifPresent(b -> {
                        String youtubeLink = b.getYoutubeLink();
                        if (youtubeLink != null && !youtubeLink.isBlank()) {
                            aggiungiLinkYoutubeSingolo(youtubeLink, mediaGridPane);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Aggiunge un singolo link YouTube come anteprima embeddabile all'interno del GridPane.
     * Se il link è valido, viene mostrato il video con un iframe YouTube.
     *
     * @param link     link YouTube da visualizzare (deve essere nel formato valido)
     * @param gridPane il contenitore in cui verrà visualizzato il video
     */
    private void aggiungiLinkYoutubeSingolo(String link, GridPane gridPane) {
        // Calcola la prossima riga disponibile basandosi solo sui nodi che hanno un RowIndex esplicito
        int row = gridPane.getChildren().stream()
                .map(GridPane::getRowIndex)
                .filter(Objects::nonNull)
                .max(Integer::compare)
                .orElse(-1) + 1;

        // === Colonna 0: Etichetta descrittiva ===
        Text linkText = new Text("YouTube Link");
        linkText.getStyleClass().addAll("font-light", "text-base");
        GridPane.setColumnIndex(linkText, 0);
        GridPane.setRowIndex(linkText, row);

        // === Colonna 1: Bottone per aprire il link ===
        Button openButton = new Button();
        InputStream playStream = getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/arrow-square-out-bold.png");
        if (playStream != null) {
            ImageView playIcon = new ImageView(new Image(playStream));
            playIcon.setFitWidth(20);
            playIcon.setPreserveRatio(true);
            openButton.setGraphic(playIcon);
        } else {
            openButton.setText("Guarda"); // Fallback testuale se l'icona manca
        }

        openButton.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        GridPane.setColumnIndex(openButton, 1);
        GridPane.setRowIndex(openButton, row);

        // === Colonna 2: Bottone per copiare il link ===
        Button copyButton = new Button();
        InputStream copyStream = getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/copy-bold.png");
        if (copyStream != null) {
            ImageView copyIcon = new ImageView(new Image(copyStream));
            copyIcon.setFitWidth(20);
            copyIcon.setPreserveRatio(true);
            copyButton.setGraphic(copyIcon);
        } else {
            copyButton.setText("Copia");
        }

        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(link);
            clipboard.setContent(content);
        });

        GridPane.setColumnIndex(copyButton, 2);
        GridPane.setRowIndex(copyButton, row);

        // Aggiunge i nodi alla griglia
        gridPane.getChildren().addAll(linkText, openButton, copyButton);
    }

    /**
     * Aggiunge una lista di file media al GridPane fornito, creando un layout a griglia
     * con anteprime per immagini e icone generiche per altri tipi di file.
     *
     * @param files     lista dei file multimediali da aggiungere (es. immagini, audio, video)
     * @param gridPane  contenitore GridPane in cui visualizzare i media
     */
    private void aggiungiMediaAllegati(List<File> files, GridPane gridPane) {
        // Pulisce la griglia rimuovendo eventuali contenuti precedenti
        gridPane.getChildren().clear();
        int row = 0; // Indice della riga corrente nella griglia

        // Itera su ciascun file multimediale da visualizzare
        for (File file : files) {

            // === Colonna 0: Nome del file ===
            Text mediaNameText = new Text(file.getName()); // Crea il nodo testuale con il nome del file
            mediaNameText.getStyleClass().addAll("font-light", "text-base"); // Applica classi di stile CSS
            GridPane.setColumnIndex(mediaNameText, 0); // Posiziona nella colonna 0
            GridPane.setRowIndex(mediaNameText, row);  // Alla riga corrente

            // === Colonna 1: Bottone "Apri" per riprodurre il file ===
            Button playButton = new Button();
            // Carica l’icona per il bottone di apertura
            ImageView apriIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/play-bold.png"))
            );
            apriIcon.setFitWidth(20); // Imposta la larghezza dell’icona
            apriIcon.setPreserveRatio(true); // Mantiene le proporzioni
            playButton.setGraphic(apriIcon); // Assegna l’icona al bottone

            // Quando cliccato, apre il file con l’applicazione predefinita del sistema
            playButton.setOnAction(e -> {
                try {
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Logga l’eccezione in caso di errore
                }
            });

            GridPane.setColumnIndex(playButton, 1); // Posiziona il bottone nella colonna 1
            GridPane.setRowIndex(playButton, row);  // Alla riga corrente

            // === Colonna 2: Bottone "Download" per salvare una copia del file ===
            Button downloadButton = new Button();
            // Carica l’icona del bottone di download
            ImageView downloadIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/com/musicsheetsmanager/ui/icons/download-simple-bold.png"))
            );
            downloadIcon.setFitWidth(20);
            downloadIcon.setPreserveRatio(true);
            downloadButton.setGraphic(downloadIcon); // Assegna l’icona al bottone

            // Quando cliccato, apre un dialogo per salvare il file localmente
            downloadButton.setOnAction(e -> {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName(file.getName()); // Suggerisce il nome originale
                    File destinazione = fileChooser.showSaveDialog(null); // Mostra il dialogo di salvataggio

                    if (destinazione != null) {
                        // Copia il file originale nella destinazione scelta
                        Files.copy(file.toPath(), destinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // Logga eventuali eccezioni
                }
            });

            GridPane.setColumnIndex(downloadButton, 2); // Posiziona il bottone nella colonna 2
            GridPane.setRowIndex(downloadButton, row);  // Alla riga corrente

            // === Aggiunta dei nodi alla griglia ===
            gridPane.getChildren().addAll(mediaNameText, playButton, downloadButton);

            // Passa alla riga successiva
            row++;
        }
    }

    private LinearGradient estraiColoriDominanti(Image image) {
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

        javafx.scene.paint.Color colore1 = Color.rgb(r, g, b);
        javafx.scene.paint.Color colore2 = colore1.darker();

        // Crea nuovo gradiente
        LinearGradient nuovoGradiente = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, colore1),
                new Stop(1, colore2)
        );
        return nuovoGradiente;
    }

    /**
     * Metodo chiamato al click del pulsante "Aggiungi Media".
     * Permette di selezionare file dal filesystem e li aggiunge visivamente a un GridPane.
     */
    @FXML
    private void addMediaClicked() {
        // Crea un FileChooser per selezionare i file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona file da allegare");

        // Imposta i tipi di file supportati
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tutti i file supportati", "*.mp3", "*.mp4", "*.midi"),
                new FileChooser.ExtensionFilter("Tutti i file", "*.*")
        );

        // Mostra la finestra per selezionare più file
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles == null || selectedFiles.isEmpty()) return;

        for (File file : selectedFiles) {
            try {
                // Crea la cartella di destinazione basata sull'ID del brano
                String destFolderPath = "src/main/resources/attachments/" + idBrano;
                File destFolder = new File(destFolderPath);
                if (!destFolder.exists()) destFolder.mkdirs();

                // Crea il file di destinazione (nella cartella)
                File destFile = new File(destFolder, file.getName());

                // Copia il file nella cartella (sovrascrivendo se esiste già)
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Crea il path relativo con doppie backslash (per JSON)
                String relativePath = destFile.getPath().replace("\\", "\\\\");

                // Aggiunge il path al brano solo se non è già presente
                if (!currentBrano.getDocumenti().contains(relativePath)) {
                    currentBrano.getDocumenti().add(relativePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Salva le modifiche nel file JSON
        salvaJsonBranoAggiornato();

        // Ricarica il brano aggiornato dal JSON (per coerenza)
        currentBrano = reloadCurrentBrano();

        // Aggiorna la sezione media nella UI
        caricaMediaBrano(currentBrano, mediaGridPane);
    }

    /**
     * Metodo chiamato al click del pulsante "Aggiungi Allegati".
     * Apre un selettore file, consente la selezione multipla e mostra i file nella griglia.
     */
    @FXML
    private void addAllegatiClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona file da allegare");

        // Estensioni supportate (più estesa rispetto ai media)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tutti i file supportati", "*.pdf", "*.jpg", "*.png"),
                new FileChooser.ExtensionFilter("Tutti i file", "*.*")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles == null || selectedFiles.isEmpty()) return;

        for (File file : selectedFiles) {
            try {
                String destFolderPath = "src/main/resources/attachments/" + idBrano;
                File destFolder = new File(destFolderPath);
                if (!destFolder.exists()) destFolder.mkdirs();

                File destFile = new File(destFolder, file.getName());

                // Copia il file solo se NON esiste già (qui non si sovrascrive)
                if (!destFile.exists()) {
                    Files.copy(file.toPath(), destFile.toPath());
                }

                // Costruisce il path e lo aggiunge alla lista se non presente
                String relativePath = destFile.getPath().replace("\\", "\\\\");
                if (!currentBrano.getDocumenti().contains(relativePath)) {
                    currentBrano.getDocumenti().add(relativePath);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Salva modifiche su JSON
        salvaJsonBranoAggiornato();

        // Ricarica il brano aggiornato dal file
        currentBrano = reloadCurrentBrano();

        // Ricarica la UI per la sezione allegati
        caricaAllegatiBrano(currentBrano, allegatiGridPane);
    }

    /**
     * Sovrascrive il brano corrente nel file JSON con i suoi dati aggiornati.
     * Assicura che le modifiche (commenti, allegati, media, ecc.) siano persistenti.
     */
    private void salvaJsonBranoAggiornato() {
        // Legge tutti i brani dal file JSON
        Type branoType = new TypeToken<List<Brano>>() {}.getType();
        List<Brano> brani = JsonUtils.leggiDaJson(BRANI_JSON_PATH, branoType);

        for (int i = 0; i < brani.size(); i++) {
            Brano b = brani.get(i);

            // Trova il brano corrente
            if (b.getIdBrano().equals(currentBrano.getIdBrano())) {
                // Fai un merge: aggiungi solo i nuovi documenti
                List<String> documentiEsistenti = new ArrayList<>(b.getDocumenti());
                for (String nuovoDoc : currentBrano.getDocumenti()) {
                    if (!documentiEsistenti.contains(nuovoDoc)) {
                        documentiEsistenti.add(nuovoDoc);
                    }
                }

                // Aggiorna la lista documenti del brano
                b.setDocumenti(documentiEsistenti);

                // Sovrascrive il brano aggiornato nella lista
                brani.set(i, b);
                break;
            }
        }

        // Scrive l'intera lista aggiornata su JSON
        JsonUtils.scriviSuJson(brani, BRANI_JSON_PATH);
    }

}
