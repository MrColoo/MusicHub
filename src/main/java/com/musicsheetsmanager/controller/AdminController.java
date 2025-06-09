package com.musicsheetsmanager.controller;

// Importazioni necessarie
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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

/**
 * Controller per la schermata dell'amministratore.
 * Gestisce la visualizzazione e l'approvazione degli utenti non ancora approvati.
 */
public class AdminController implements Controller, Initializable {

    private MainController mainController;

    // Metodo per collegare il controller principale
    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Percorso del file JSON contenente gli utenti
    private static final Path USER_JSON_PATH = Paths.get(
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "user.json"
    );

    // Tipo per deserializzare una lista di oggetti Utente da JSON
    private final Type userType = new TypeToken<List<Utente>>() {}.getType();

    // Lista degli utenti caricata dal file JSON
    private List<Utente> utenti = JsonUtils.leggiDaJson(USER_JSON_PATH, userType);

    // Componente della GUI in cui verranno visualizzati gli utenti
    @FXML
    private GridPane userListGridPane;

    /**
     * Metodo chiamato automaticamente quando il controller viene inizializzato.
     * Carica la lista degli utenti non approvati.
     */
    public void initialize(URL location, ResourceBundle resources) {
        aggiornaUtenti();
    }

    /**
     * Ricarica dal file JSON e aggiorna la GUI per mostrare gli utenti non approvati.
     */
    public void aggiornaUtenti() {
        userListGridPane.getChildren().clear(); // Pulisce la griglia esistente

        // Ricarica la lista degli utenti dal file JSON
        utenti = JsonUtils.leggiDaJson(USER_JSON_PATH, userType);

        // Filtra solo gli utenti non ancora approvati
        List<Utente> nonApprovati = utenti.stream()
                .filter(u -> !u.isApproved())
                .collect(Collectors.toList());

        int row = 0;
        for (Utente utente : nonApprovati) {
            try {
                // Carica dinamicamente un riquadro per ogni utente (FXML)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/musicsheetsmanager/fxml/AdminRow.fxml"));
                Node rowNode = loader.load();

                // Imposta l'utente sul controller della riga
                AdminRowController rowController = loader.getController();
                rowController.setUtente(utente, this);

                // Aggiunge la riga alla griglia
                userListGridPane.add(rowNode, 0, row++);
                GridPane.setColumnSpan(rowNode, 4); // Occupa 4 colonne
            } catch (Exception e) {
                e.printStackTrace(); // Log dell'errore
            }
        }
    }

    /**
     * Salva la lista aggiornata degli utenti su JSON e aggiorna la GUI.
     */
    public void salvaEaggiorna() {
        JsonUtils.scriviSuJson(utenti, USER_JSON_PATH); // Scrive la lista aggiornata
        aggiornaUtenti(); // Aggiorna la visualizzazione
    }

    /**
     * Rimuove un utente dalla lista, aggiorna il file JSON e la GUI.
     * @param utente L'utente da rimuovere.
     */
    public void rimuoviUtente(Utente utente) {
        utenti.remove(utente);
        JsonUtils.scriviSuJson(utenti, USER_JSON_PATH);
        aggiornaUtenti();
    }
}
