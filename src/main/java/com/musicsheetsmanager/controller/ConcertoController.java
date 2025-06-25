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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConcertoController {

    // Percorsi ai file JSON
    private static final Path PATH_BRANI_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/brani.json");
    private static final Path PATH_BRANICONCERTO_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/braniConcerto.json");
    private static final String DEFAULT_COVER = "src/main/resources/com/musicsheetsmanager/ui/Cover.jpg";
    private static final String COVER_PATH = "src/main/resources/com/musicsheetsmanager/ui/covers/";

    // Tipo generico per deserializzare una lista di Brano
    private final Type tipoListaBrani = new TypeToken<List<Brano>>() {}.getType();

    // Riferimenti agli elementi dell'interfaccia utente
    @FXML private TextField inizioBranoConcerto;
    @FXML private TextField fineBranoConcerto;
    @FXML private Button aggiungiBranoCanzone;
    @FXML private WebView webView;
    @FXML private Text concertoTitolo;
    @FXML private ComboBox<Brano> selezionaBrani;
    @FXML private FlowPane containerBrani;

    // ID del concerto attualmente visualizzato
    private String idConcerto;

    private Concerto currentConcerto;

    // Metodo chiamato automaticamente all'inizializzazione del controller
    @FXML
    public void initialize() {
        caricaBrani(); // Carica i brani dalla lista JSON e inizializza la ComboBox
    }

    /**
     * Carica i dati di un Concerto e aggiorna l'interfaccia
     */
    public void fetchConcertoData(Concerto concerto) {
        currentConcerto = concerto;
        idConcerto = concerto.getId();
        String titolo = concerto.getTitolo();

        if (titolo == null || titolo.isEmpty()) {
            concertoTitolo.setText("Titolo non disponibile");
        } else {
            concertoTitolo.setText(titolo);
        }

        // Carica il video YouTube, se presente
        String linkYoutube = concerto.getLink();
        if (linkYoutube != null && !linkYoutube.isEmpty()) {
            System.out.println("Carico video da link: " + linkYoutube);
            mostraVideo(linkYoutube);
        } else {
            System.out.println("Nessun link YouTube trovato per concerto con id: " + idConcerto);
        }

        System.out.println("Caricato concerto: " + titolo);

        mostraCardBraniConcerto(idConcerto);
    }

    /**
     * Carica il video in un WebView tramite URL convertito in formato embed
     */
    public void mostraVideo(String linkYoutube) {
        if (webView == null) {
            System.out.println("WebView non inizializzata");
            return;
        }

        if (linkYoutube != null && linkYoutube.contains("youtube.com/watch?v=")) {
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
        } else {
            System.out.println("Link YouTube non valido: " + linkYoutube);
        }
    }

    /**
     * Converte un URL normale di YouTube in formato "embed"
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
        List<Brano> brani = JsonUtils.leggiDaJson(PATH_BRANI_JSON, tipoListaBrani);
        if (brani != null) {
            abilitaRicercaComboBoxBrani(brani); // Aggiunge filtro di ricerca
        } else {
            System.out.println("Errore di lettura del JSON");
        }

        // Imposta il modo in cui i brani sono visualizzati nella ComboBox
        selezionaBrani.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Brano brano) {
                return (brano != null) ? brano.getTitolo() : "";
            }

            @Override
            public Brano fromString(String string) {
                return selezionaBrani.getItems().stream()
                        .filter(b -> b.getTitolo().equals(string))
                        .findFirst()
                        .orElse(null);
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
     */
    private void abilitaRicercaComboBoxBrani(List<Brano> listaBrani) {
        ObservableList<Brano> braniOriginali = FXCollections.observableArrayList(listaBrani);
        FilteredList<Brano> braniFiltrati = new FilteredList<>(braniOriginali, b -> true);

        selezionaBrani.setItems(braniFiltrati);
        selezionaBrani.setEditable(true);

        selezionaBrani.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Brano brano) {
                return brano != null ? brano.getTitolo() : "";
            }

            @Override
            public Brano fromString(String titolo) {
                return braniOriginali.stream()
                        .filter(b -> b.getTitolo().equalsIgnoreCase(titolo))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Filtro in tempo reale mentre si digita
        selezionaBrani.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            String filtro = newVal.toLowerCase();

            braniFiltrati.setPredicate(brano -> {
                if (filtro == null || filtro.isEmpty()) return true;
                return brano.getTitolo().toLowerCase().contains(filtro);
            });

            selezionaBrani.show(); // Mostra la lista filtrata
        });

        // Aggiorna il testo nel campo di input dopo selezione
        selezionaBrani.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selezionaBrani.getEditor().setText(newVal.getTitolo());
            }
        });
    }

    /**
     * Aggiunge un brano al concerto corrente dopo aver validato i dati inseriti
     */
    @FXML
    private void addConcertoClicked() {
        Brano branoSelezionato = selezionaBrani.getValue();
        String inizio = inizioBranoConcerto.getText().trim();
        String fine = fineBranoConcerto.getText().trim();

        // Controlli base
        if (branoSelezionato == null || inizio.isEmpty() || fine.isEmpty()) {
            System.out.println("Compila tutti i campi.");
            return;
        }

        if (!isValidTimeFormat(inizio) || !isValidTimeFormat(fine)) {
            System.out.println("Formato orario non valido. Usa hh:mm:ss o mm:ss (es. 03:45 o 00:03:45).");
            return;
        }

        // Ottieni l'utente loggato
        Utente utente = SessionManager.getLoggedUser();
        if (utente == null) {
            System.out.println("Nessun utente loggato.");
            return;
        }

        // Usa il nome utente
        String nomeUtente = utente.getUsername(); // Assicurati che esista getUsername()

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

        // Leggi lista e salva su file
        Type tipoLista = new TypeToken<List<BranoAssegnatoAlConcerto>>() {}.getType();
        List<BranoAssegnatoAlConcerto> lista = JsonUtils.leggiDaJson(PATH_BRANICONCERTO_JSON, tipoLista);
        if (lista == null) {
            lista = new java.util.ArrayList<>();
        }

        lista.add(assegnato);
        JsonUtils.scriviSuJson(lista, PATH_BRANICONCERTO_JSON);

        System.out.println("Brano collegato al concerto da utente: " + nomeUtente);

        inizioBranoConcerto.clear();
        fineBranoConcerto.clear();
        selezionaBrani.getSelectionModel().clearSelection();
        selezionaBrani.hide();

        fetchConcertoData(currentConcerto);
    }


    /**
     * Verifica se una stringa rappresenta un tempo nel formato hh:mm:ss o mm:ss
     */
    private boolean isValidTimeFormat(String time) {
        return time.matches("^(\\d{1,2}:)?[0-5]?\\d:[0-5]\\d$");
    }

    /**
     * Mostra le card dei brani del concerto
     * @param idConcerto Id del concerto
     */
    public void mostraCardBraniConcerto (String idConcerto) {
        containerBrani.getChildren().clear();

        Type branoConcertoType = new TypeToken<List<BranoAssegnatoAlConcerto>>() {}.getType();
        List<BranoAssegnatoAlConcerto> listaBraniConcerto = JsonUtils.leggiDaJson(PATH_BRANICONCERTO_JSON, branoConcertoType);

        for(BranoAssegnatoAlConcerto brano: listaBraniConcerto) {
            if(brano.getIdConcerto().equals(idConcerto)) {
                containerBrani.getChildren().add(creaCard(brano));
            }
        }
    }

    /**
     * Crea una card per il brano
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

        //card.setOnMouseClicked(e -> mainController.goToBrano(card, brano, () -> {
        //    BranoController controller = mainController.getBranoController();
        //    if (controller != null) controller.fetchBranoData(brano);
        //}));

        return card;
    }
}
