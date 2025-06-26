package com.musicsheetsmanager.controller;

// Importazioni necessarie
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.config.StringUtils;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.BranoAssegnatoAlConcerto;
import com.musicsheetsmanager.model.Concerto;
import com.musicsheetsmanager.model.Utente;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import com.musicsheetsmanager.config.UiUtils;

public class ConcertoController implements Controller{

    // Percorsi ai file JSON
    private static final Path PATH_BRANI_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/brani.json");
    private static final Path PATH_BRANICONCERTO_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/braniConcerto.json");
    private static final Path PATH_CONCERTI_IMAGE = Paths.get("src/main/resources/com/musicsheetsmanager/ui/concerti/");
    private static final String DEFAULT_COVER = "src/main/resources/com/musicsheetsmanager/ui/Cover.jpg";
    private static final String COVER_PATH = "src/main/resources/com/musicsheetsmanager/ui/covers/";

    // Tipo generico per deserializzare una lista di Brano
    private final Type tipoListaBrani = new TypeToken<List<Brano>>() {}.getType();

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Riferimenti agli elementi dell'interfaccia utente
    @FXML private TextField inizioBranoConcerto;
    @FXML private TextField fineBranoConcerto;
    @FXML private WebView webView;
    @FXML private Text concertoTitolo;
    @FXML private ComboBox<Brano> selezionaBrani;
    @FXML private VBox webViewContainer;
    @FXML private AnchorPane backgroundConcerto;
    @FXML private FlowPane containerBrani;

    // ID del concerto attualmente visualizzato
    private String idConcerto;

    // Metodo chiamato automaticamente all'inizializzazione del controller
    @FXML
    public void initialize() {
        caricaBrani(); // Carica i brani dalla lista JSON e inizializza la ComboBox
    }

    /**
     * Carica i dati di un concerto selezionato e aggiorna l'interfaccia utente.
     *
     * @param concerto oggetto Concerto contenente le informazioni da visualizzare
     */
    public void fetchConcertoData(Concerto concerto) {
        idConcerto = concerto.getId();
        String titolo = concerto.getTitolo();

        if (titolo == null || titolo.isEmpty()) { // controllo sul titolo
            concertoTitolo.setText("Titolo non disponibile");
        } else {
            concertoTitolo.setText(titolo);
        }

        // Carica il video YouTube, se presente
        String linkYoutube = concerto.getLink();

        // Thread separato crea Webview per evitare rallentamenti e mostra video
        Platform.runLater(() -> {
            webView = new WebView();
            webView.setPrefHeight(515);
            webView.setPrefWidth(760);
            if (linkYoutube != null && !linkYoutube.isEmpty()) { // controllo se c'e' qualcosa sul link YT
                System.out.println("Carico video da link: " + linkYoutube);
                mostraVideo(linkYoutube);
            } else {
                System.out.println("Nessun link YouTube trovato per concerto con id: " + idConcerto);
            }
        });

        System.out.println("Caricato concerto: " + titolo);

        changeBackgroundColor(idConcerto);
        mostraCardBraniConcerto(idConcerto);
    }

    /**
     * Mostra un video di YouTube all'interno della WebView del concerto.
     * Accetta link nel formato "https://www.youtube.com/watch?v=..."
     *
     * @param linkYoutube link diretto al video YouTube fornito dall'oggetto Concerto
     */
    public void mostraVideo(String linkYoutube) {
        if (webView == null) { // controllo se la Web view e' inizializzata
            System.out.println("WebView non inizializzata");
            return;
        }

        if (linkYoutube != null && linkYoutube.contains("youtube.com/watch?v=")) { // CONTROLLI E FUNZIONI PER MOSTRARE IL VIDEO
            String embedUrl = convertToEmbedUrl(linkYoutube);

            String html = String.format("""
                <html>
                    <body style="margin:0;">
                        <iframe width="100%%" height="100%%" src="%s"
                            frameborder="0" allowfullscreen></iframe>
                    </body>
                </html>
            """, embedUrl);

            webView.getEngine().loadContent(html, "text/html");
            webViewContainer.getChildren().setAll(webView);
        } else {
            System.out.println("Link YouTube non valido: " + linkYoutube);
        }
    }

    /**
     * Converte un URL di YouTube in formato "embed" per l'inserimento in un iframe.
     * Supporta sia link standard (youtube.com/watch?v=...) che link brevi (youtu.be/...).
     *
     * @param url l'URL originale del video YouTube
     * @return l'URL convertito in formato embed, oppure null se non riconosciuto o in caso di errore
     */
    private String convertToEmbedUrl(String url) {
        try {
            if (url.contains("youtube.com/watch?v=")) {
                String videoId = url.substring(url.indexOf("v=") + 2);
                int amp = videoId.indexOf('&');
                if (amp != -1) videoId = videoId.substring(0, amp);
                return "https://www.youtube.com/embed/" + videoId;
            } else if (url.contains("youtu.be/")) {
                String videoId = url.substring(url.indexOf("youtu.be/") + 9);
                int q = videoId.indexOf('?');
                if (q != -1) videoId = videoId.substring(0, q);
                return "https://www.youtube.com/embed/" + videoId;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Carica i brani dal file JSON e imposta il comportamento della ComboBox
     */
    private void caricaBrani() {
        List<Brano> brani = JsonUtils.leggiDaJson(PATH_BRANI_JSON, tipoListaBrani); // brani letti dal JSON
        if (brani != null) {
            abilitaRicercaComboBoxBrani(brani); // Aggiunge filtro di ricerca
        } else {
            System.out.println("Errore di lettura del JSON");
        }

        // Imposta il modo in cui i brani sono visualizzati nella ComboBox
        selezionaBrani.setConverter(new javafx.util.StringConverter<>() {

            // Metodo chiamato per convertire un oggetto Brano in una stringa da visualizzare nella ComboBox
            @Override
            public String toString(Brano brano) {
                // Se il brano non è nullo, restituisce il titolo del brano; altrimenti una stringa vuota
                return (brano != null) ? brano.getTitolo() : "";
            }

            // Metodo chiamato per convertire una stringa (selezionata o scritta) in un oggetto Brano
            @Override
            public Brano fromString(String string) {
                // Cerca tra gli elementi della ComboBox quello con un titolo uguale alla stringa passata
                return selezionaBrani.getItems().stream()
                        .filter(b -> b.getTitolo().equals(string)) // Filtra i brani con titolo uguale alla stringa
                        .findFirst() // Prende il primo che corrisponde
                        .orElse(null); // Se non trova nulla, restituisce null
            }
        });


        // Come ogni elemento è rappresentato nella lista dropdown
        selezionaBrani.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Brano brano, boolean empty) {
                super.updateItem(brano, empty);
                setText((brano == null || empty) ? null : brano.getTitolo());
            }
        });
    }

    /**
     * Abilita ricerca testuale all'interno della ComboBox dei brani
     *
     * @param listaBrani spieare cosa fa
     */
    private void abilitaRicercaComboBoxBrani(List<Brano> listaBrani) {
        selezionaBrani.setEditable(true); // rende la ComboBox editabile

        ObservableList<Brano> originalItems = FXCollections.observableArrayList(listaBrani);  // Lista osservabile di tutti i brani originali
        FilteredList<Brano> filteredItems = new FilteredList<>(originalItems, p -> true);  // Lista filtrata da aggiornare dinamicamente in base all'input dell’utente
        selezionaBrani.setItems(filteredItems);

        TextField editor = selezionaBrani.getEditor();  // Ottiene l’editor testuale della ComboBox

        // Flag per evitare che l'auto-complete sovrascriva la selezione dell'utente
        final BooleanProperty skipAutoComplete = new SimpleBooleanProperty(false);

        // Listener che filtra i risultati in base al testo digitato
        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (skipAutoComplete.get()) {
                return;
            }

            String input = newText == null ? "" : newText.trim().toLowerCase();

            // Applica il filtro: mostra solo i brani che contengono il testo digitato
            filteredItems.setPredicate(brano -> {
                // Se input è vuoto, resetta la selezione
                if (input.isEmpty()) return true;
                return brano.getTitolo().toLowerCase().contains(input);
            });

            // Mostra o nasconde il menu a tendina in base all’input
            if (!selezionaBrani.isShowing() && !input.isEmpty()) {
                selezionaBrani.show();
            }

        });

        // serve per far resettare bene la grafica, e per resettare il focus
        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (skipAutoComplete.get()) return;

            String input = newText == null ? "" : newText.trim().toLowerCase();

            // Filtro lista in base all'input
            filteredItems.setPredicate(brano -> {
                if (input.isEmpty()) return true;
                return brano.getTitolo().toLowerCase().contains(input);
            });

            // Se l'input è vuoto, resetta anche la selezione
            if (input.isEmpty()) {
                selezionaBrani.setValue(null);
            }

            // Mostra dropdown solo se ha senso
            if (!input.isEmpty()) {
                selezionaBrani.show();
            } else {
                selezionaBrani.hide();
            }
        });


        // Listener su selezione da lista dropdown
        selezionaBrani.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Impedisce l’autocompletamento forzato mentre aggiorniamo il camp
                skipAutoComplete.set(true);
                editor.setText(newVal.getTitolo()); // mostra il titolo del brano selezionato
                skipAutoComplete.set(false);
            }
        });

        // Listener per gestire quando il campo perde il focus
        editor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // Se il testo corrisponde a un brano, lo seleziona
                Brano match = filteredItems.stream()
                        .filter(b -> b.getTitolo().equalsIgnoreCase(editor.getText().trim()))
                        .findFirst().orElse(null);
                // Se non c'è corrispondenza, resetta la selezione
                selezionaBrani.setValue(match);
            }
        });
    }

    /**
     * Aggiunge un brano al concerto corrente dopo aver validato i dati inseriti
     */
    @FXML
    private void addConcertoClicked() {
        Brano branoSelezionato = selezionaBrani.getValue(); // il brano selezionata dalla ComboBox
        // orari inseriti nel campo testo
        String inizio = inizioBranoConcerto.getText().trim();
        String fine = fineBranoConcerto.getText().trim();

        // Controllo che tutti i campi siano compilati
        if (branoSelezionato == null || inizio.isEmpty() || fine.isEmpty()) {
            System.out.println("Compila tutti i campi.");
            return;
        }
        // Controllo formato orario
        if (!isValidTimeFormat(inizio) || !isValidTimeFormat(fine)) {
            System.out.println("Formato orario non valido. Usa hh:mm:ss o mm:ss (es. 03:45 o 00:03:45).");
            return;
        }

        // Converte gli orari in secondi
        int nuovoInizio = convertToSeconds(inizio);
        int nuovoFine = convertToSeconds(fine);

        // Controllo orario fine non deve essere prima di inizio
        if (nuovoFine <= nuovoInizio) {
            System.out.println("Il tempo di fine deve essere maggiore del tempo di inizio.");
            return;
        }

        //Controllo delle sovrapposizioni degli orari tra brani
        Type tipoLista = new TypeToken<List<BranoAssegnatoAlConcerto>>() {}.getType();
        List<BranoAssegnatoAlConcerto> lista = JsonUtils.leggiDaJson(PATH_BRANICONCERTO_JSON, tipoLista);

        if (lista != null) {
            for (BranoAssegnatoAlConcerto b : lista) {
                // Ignora i brani di altri concerti
                if (!idConcerto.equals(b.getIdConcerto())) continue;

                // Converte orari esistenti in secondi
                int esistenteInizio = convertToSeconds(b.getInizio());
                int esistenteFine = convertToSeconds(b.getFine());

                // Verifica sovrapposizione
                if (nuovoInizio < esistenteFine && nuovoFine > esistenteInizio) {
                    System.out.println("Errore: sovrapposizione con brano " + b.getIdBrano() + " da " + b.getInizio() + " a " + b.getFine());
                    return;
                }
            }
        }

        // Ottiene l'utente attualmente loggato
        Utente utente = SessionManager.getLoggedUser();
        if (utente == null) {
            System.out.println("Nessun utente loggato.");
            return;
        }

        // Recupera il nome utente da salvare nel brano assegnato
        String nomeUtente = utente.getUsername();

        // Crea un nuovo oggetto da salvare nel JSON
        BranoAssegnatoAlConcerto assegnato = new BranoAssegnatoAlConcerto(
                idConcerto,
                inizio,
                fine,
                nomeUtente,
                branoSelezionato.getIdBrano(),
                branoSelezionato.getTitolo(),
                branoSelezionato.getAutori(),
                branoSelezionato.getGeneri(),
                branoSelezionato.getAnnoComposizione(),
                branoSelezionato.getEsecutori(),
                branoSelezionato.getYoutubeLink(),
                branoSelezionato.getStrumentiMusicali()
        );

        for(String idCommento: branoSelezionato.getIdCommenti()) {
            assegnato.aggiungiCommento(idCommento);
        }

        // Se la lista era nulla
        if (lista == null) {
            lista = new java.util.ArrayList<>();
        }

        // Aggiunge un nuovo brano alla lista
        lista.add(assegnato);
        // Scrive la lista aggiornata nel file JSON
        JsonUtils.scriviSuJson(lista, PATH_BRANICONCERTO_JSON);

        System.out.println("Brano collegato al concerto da utente: " + nomeUtente);

        // Clear dei campi
        inizioBranoConcerto.clear();
        fineBranoConcerto.clear();
        selezionaBrani.getSelectionModel().clearSelection();
        selezionaBrani.hide();

        // Refresh del flowpane contenente le card dei brani
        mostraCardBraniConcerto(idConcerto);
    }


    /**
     * Converte un orario in formato "hh:mm:ss" o "mm:ss" in secondi totali.
     *
     * @param time una stringa che rappresenta un orario, ad esempio "01:30:00" o "03:45"
     * @return il numero totale di secondi corrispondente all'orario
     */
    private int convertToSeconds(String time) {
        String[] parts = time.split(":");
        int seconds = 0;

        if (parts.length == 2) {
            seconds = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } else if (parts.length == 3) {
            seconds = Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60 + Integer.parseInt(parts[2]); // fa prima ora -> secondi, minuti -> secondi
        }

        return seconds;
    }

    /**
     * Verifica se una stringa rappresenta un tempo nel formato hh:mm:ss o mm:ss
     */
    private boolean isValidTimeFormat(String time) {
        return time.matches("^(\\d{1,2}:)?[0-5]?\\d:[0-5]\\d$");
    }

    /**
     * Mostra le card dei brani del concerto
     *
     * @param idConcerto Id del concerto
     */
    public void mostraCardBraniConcerto (String idConcerto) {
        containerBrani.getChildren().clear();

        Type branoConcertoType = new TypeToken<List<BranoAssegnatoAlConcerto>>() {}.getType();
        List<BranoAssegnatoAlConcerto> listaBraniConcerto = JsonUtils.leggiDaJson(PATH_BRANICONCERTO_JSON, branoConcertoType);

        // Ordino i brani in base al tempo di inizio
        List<BranoAssegnatoAlConcerto> ordinati = listaBraniConcerto.stream()
                .sorted(Comparator.comparing(brano -> convertToSeconds(brano.getInizio())))
                .toList();

        // Creo le card per i brani appartenenti al concerto
        for(BranoAssegnatoAlConcerto brano: ordinati) {
            if(brano.getIdConcerto().equals(idConcerto)) {
                containerBrani.getChildren().add(creaCard(brano));
            }
        }
    }

    /**
     * Crea una card per il brano
     *
     * @param brano Brano eseguito durante il concerto
     */
    private VBox creaCard(BranoAssegnatoAlConcerto brano) {
        File imageFile = new File(COVER_PATH + brano.getIdBrano() + ".jpg");

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

        Text titoloText = new Text(brano.getTitolo());
        titoloText.setWrappingWidth(154);
        titoloText.setTextAlignment(TextAlignment.CENTER);
        titoloText.getStyleClass().addAll("text-white", "font-bold", "text-base");

        card.getChildren().addAll(cover, titoloText);

        Text autoreText = new Text(StringUtils.capitalizzaTesto(String.join(", ", brano.getAutori())));
        autoreText.getStyleClass().addAll("text-white", "font-light", "text-sm");
        autoreText.setWrappingWidth(154);
        autoreText.setTextAlignment(TextAlignment.CENTER);
        card.getChildren().add(autoreText);

        Text timelineText = new Text(StringUtils.capitalizzaTesto(brano.getInizio() + "-" + brano.getFine()));
        timelineText.getStyleClass().addAll("text-white", "font-light", "text-sm");
        timelineText.setWrappingWidth(154);
        timelineText.setTextAlignment(TextAlignment.CENTER);
        card.getChildren().add(timelineText);

        card.setOnMouseClicked(e -> mainController.goToBrano(card, brano, () -> {
            BranoController controller = mainController.getBranoController();
            if (controller != null) controller.fetchBranoData(brano);
        }));

        return card;
    }

    private void changeBackgroundColor(String idConcerto){
        File imageFile = PATH_CONCERTI_IMAGE.resolve(idConcerto + ".jpg").toFile();
        System.out.println(PATH_CONCERTI_IMAGE + idConcerto + ".jpg");
        // Colore dominante come background
        Image image = new Image(imageFile.toURI().toString());

        // Ottieni il colore dominante
        Color coloreDominante = UiUtils.estraiColoreDominante(image);

        // Convertilo in formato CSS (hex)
        String hexColor = UiUtils.toHexColor(coloreDominante);

        // Applica lo stile con gradiente
        String gradientStyle = String.format("""
            -fx-background-color: linear-gradient(from 0%% 0%% to 0%% 30%%, %s, #000000);
        """, hexColor);

        // Applica lo stile al ScrollPane
        backgroundConcerto.setStyle(gradientStyle);
    }
}
