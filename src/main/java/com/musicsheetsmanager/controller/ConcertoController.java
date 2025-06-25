package com.musicsheetsmanager.controller;

// Importazioni necessarie
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.config.SessionManager;
import com.musicsheetsmanager.model.Brano;
import com.musicsheetsmanager.model.BranoAssegnatoAlConcerto;
import com.musicsheetsmanager.model.Concerto;
import com.musicsheetsmanager.model.Utente;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConcertoController {

    // Percorsi ai file JSON
    private static final Path PATH_BRANI_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/brani.json");
    private static final Path PATH_BRANICONCERTO_JSON = Paths.get("src/main/resources/com/musicsheetsmanager/data/braniConcerto.json");

    // Tipo generico per deserializzare una lista di Brano
    private final Type tipoListaBrani = new TypeToken<List<Brano>>() {}.getType();

    // Riferimenti agli elementi dell'interfaccia utente
    @FXML private TextField inizioBranoConcerto;
    @FXML private TextField fineBranoConcerto;
    @FXML private Button aggiungiBranoCanzone;
    @FXML private WebView webView;
    @FXML private Text concertoTitolo;
    @FXML private ComboBox<Brano> selezionaBrani;

    // ID del concerto attualmente visualizzato
    private String idConcerto;

    // Metodo chiamato automaticamente all'inizializzazione del controller
    @FXML
    public void initialize() {
        caricaBrani(); // Carica i brani dalla lista JSON e inizializza la ComboBox
    }

    /**
     * Carica i dati di un Concerto e aggiorna l'interfaccia
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
        if (linkYoutube != null && !linkYoutube.isEmpty()) { // controllo se c'e' qualcosa sul link YT
            System.out.println("Carico video da link: " + linkYoutube);
            mostraVideo(linkYoutube);
        } else {
            System.out.println("Nessun link YouTube trovato per concerto con id: " + idConcerto);
        }

        System.out.println("Caricato concerto: " + titolo);
    }

    /**
     * Carica il video in un WebView tramite URL convertito in formato embed
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
     */

    private void abilitaRicercaComboBoxBrani(List<Brano> listaBrani) {
        selezionaBrani.setEditable(true);

        ObservableList<Brano> originalItems = FXCollections.observableArrayList(listaBrani);
        FilteredList<Brano> filteredItems = new FilteredList<>(originalItems, p -> true);
        selezionaBrani.setItems(filteredItems);

        TextField editor = selezionaBrani.getEditor();

        // Flag per evitare che l'auto-complete sovrascriva la selezione dell'utente
        final BooleanProperty skipAutoComplete = new SimpleBooleanProperty(false);

        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (skipAutoComplete.get()) {
                return;
            }

            String input = newText == null ? "" : newText.trim().toLowerCase();

            filteredItems.setPredicate(brano -> {
                if (input.isEmpty()) return true;
                return brano.getTitolo().toLowerCase().contains(input);
            });

            // Riapre la dropdown ad ogni cambio di testo
            selezionaBrani.show();
        });

        // Se viene selezionato un oggetto dalla lista
        selezionaBrani.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                skipAutoComplete.set(true);
                editor.setText(newVal.getTitolo());
                skipAutoComplete.set(false);
            }
        });

        // Previene errori durante la selezione libera
        editor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Brano match = filteredItems.stream()
                        .filter(b -> b.getTitolo().equalsIgnoreCase(editor.getText().trim()))
                        .findFirst().orElse(null);
                selezionaBrani.setValue(match);
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
        if (branoSelezionato == null || inizio.isEmpty() || fine.isEmpty()) { // controllo che tutti i campi sono compilati
            System.out.println("Compila tutti i campi.");
            return;
        }

        if (!isValidTimeFormat(inizio) || !isValidTimeFormat(fine)) { // controllo del formato
            System.out.println("Formato orario non valido. Usa hh:mm:ss o mm:ss (es. 03:45 o 00:03:45).");
            return;
        }

        if (convertToSeconds(fine) <= convertToSeconds(inizio)) { // controllo che il tempo di fine non sia < del tempo di inizio
            System.out.println("Il tempo di fine deve essere maggiore del tempo di inizio.");
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
                branoSelezionato.getIdBrano(),
                inizio,
                fine,
                nomeUtente
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
    }

    /**
     * Converte un tempo nel formato hh:mm:ss o mm:ss in secondi totali
     */

    private int convertToSeconds(String time) {
        String[] parts = time.split(":");
        int seconds = 0;

        if (parts.length == 2) {
            seconds = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } else if (parts.length == 3) {
            seconds = Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60 + Integer.parseInt(parts[2]);
        }

        return seconds;
    }

    /**
     * Verifica se una stringa rappresenta un tempo nel formato hh:mm:ss o mm:ss
     */

    private boolean isValidTimeFormat(String time) {
        return time.matches("^(\\d{1,2}:)?[0-5]?\\d:[0-5]\\d$");
    }
}
