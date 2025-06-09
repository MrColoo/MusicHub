package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

/**
 * Controller per una singola riga nella lista utenti della schermata Admin.
 * Gestisce la visualizzazione delle informazioni dell'utente e le azioni di approvazione/rifiuto.
 */
public class AdminRowController {

    // Collegamenti ai nodi FXML della riga utente
    @FXML private Text usernameText;     // Visualizza lo username
    @FXML private Text emailText;        // Visualizza l'email
    @FXML private Button approveButton;  // Bottone per approvare l'utente
    @FXML private Button rejectButton;   // Bottone per rifiutare l'utente

    private Utente utente;                       // Oggetto utente associato a questa riga
    private AdminController parentController;    // Riferimento al controller padre (AdminController)

    /**
     * Metodo chiamato per inizializzare questa riga con i dati dell'utente e
     * collegare i bottoni alle rispettive azioni.
     *
     * @param utente L'utente da visualizzare
     * @param parentController Il controller principale che gestisce la lista utenti
     */
    public void setUtente(Utente utente, AdminController parentController) {
        this.utente = utente;
        this.parentController = parentController;

        // Visualizza le informazioni dell'utente nella GUI
        usernameText.setText(utente.getUsername());
        emailText.setText(utente.getEmail());

        // Azione del bottone "Approva": imposta l'utente come approvato e aggiorna la lista
        approveButton.setOnAction(e -> {
            utente.setApproved(true);                  // Modifica lo stato
            parentController.salvaEaggiorna();         // Salva e ricarica la lista
        });

        // Azione del bottone "Rifiuta": rimuove l'utente dalla lista
        rejectButton.setOnAction(e -> {
            parentController.rimuoviUtente(utente);    // Elimina e aggiorna
        });
    }
}
