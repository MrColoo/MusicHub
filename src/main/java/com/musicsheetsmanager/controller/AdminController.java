package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Utente;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class AdminController implements Controller, Initializable {

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Percorso verso il file JSON degli utenti
    private static final Path USER_JSON_PATH = Paths.get(
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "user.json"
    );

    // Leggo da JSON una List<Utente>
    private final Type userType = new TypeToken<List<Utente>>() {}.getType();
    private List<Utente> utenti = JsonUtils.leggiDaJson(USER_JSON_PATH, userType);

    // L’ObservableList che useremo per aggiornare la ListView “in tempo reale”
    private ObservableList<Utente> observableUtenti;

    // Qui collego la ListView definita in FXML (fx:id="userList")
    @FXML
    private ListView<Utente> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (utenti != null && !utenti.isEmpty()) {
            // 1) Filtro solo quelli con approved == false
            List<Utente> utentiPending = utenti.stream()
                    .filter(u -> !u.isApproved())
                    .collect(Collectors.toList());

            // 2) Creo l’ObservableList a partire dalla lista filtrata
            observableUtenti = FXCollections.observableArrayList(utentiPending);
            userList.setItems(observableUtenti);

            // 3) Imposto un CellFactory che mostra “username – email”
            userList.setCellFactory(lv -> new ListCell<Utente>() {
                @Override
                protected void updateItem(Utente item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getUsername() + " – " + item.getEmail());
                    }
                }
            });

            // 4) (Opzionale) Preseleziona il primo
            userList.getSelectionModel().selectFirst();
        }
    }

    /**
     * Metodo chiamato quando l’utente clicca “Approve”
     */
    @FXML
    private void onApproveClicked() {
        Utente selezionato = userList.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            return;
        }

        // 1) Setto approved = true sull’istanza principale
        selezionato.setApproved(true);

        // 2) Salvo subito su JSON l’intera lista “utenti”
        JsonUtils.scriviSuJson(utenti, USER_JSON_PATH);

        // 3) Rimuovo l’elemento approvato dall’ObservableList (sparirà dalla ListView)
        observableUtenti.remove(selezionato);
    }

    /**
     * Metodo chiamato quando l’utente clicca “Reject”
     */
    @FXML
    private void onRejectClicked() {
        Utente selezionato = userList.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            return;
        }

        // 1) Se vuoi mantenerlo in JSON con approved=false,
        //    basta rimuoverlo solo dal pending della ListView:
        //    selezionato.setApproved(false); // (già false di default)

        // 2) Rimuovo l’elemento rifiutato dall’ObservableList
        observableUtenti.remove(selezionato);

        // 3) Se invece volessi anche cancellarlo definitivamente dal JSON:
        //    utenti.remove(selezionato);

        // 4) Salvo su JSON l’elenco aggiornato
        JsonUtils.scriviSuJson(utenti, USER_JSON_PATH);
    }

    /**
     * Salva su JSON l’intera lista di utenti (con i cambiamenti al campo approved).
     */
    private void salvaListaSuJson() {
        JsonUtils.scriviSuJson(utenti, USER_JSON_PATH);
    }
}
