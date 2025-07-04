package com.musicsheetsmanager.controller;

import com.musicsheetsmanager.config.JsonUtils;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.musicsheetsmanager.config.SessionManager.login;

// Controller per il login degli utenti
public class LoginController implements Controller {

    @FXML private TextField loginUsernameField;   // campo testo per l'USERNAME
    @FXML private PasswordField loginPasswordField;   // campo testo per la PASSWORD
    @FXML private Button    loginButton;          // bottone per il login
    @FXML private Text      loginToRegisterButton;// testo che se cliccato porta alla registrazione
    @FXML private Text      feedbackText;         // testo di Feedback

    private static final Path USER_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "user.json"
    );

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Metodo chiamato automaticamente da JavaFX subito dopo l'inizializzazione
     * dei componenti FXML. Qui impostiamo il pulsante di default e aggiungiamo
     * un listener sul campo password per intercettare INVIO.
     */
    @FXML
    private void initialize() {
        // Rende il loginButton “predefinito”: quando si preme Invio (ENTER) da uno dei TextField, viene fatto click su di esso.
        loginButton.setDefaultButton(true);

        loginPasswordField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // “Clicca” sul pulsante di login:
                loginButton.fire();
                event.consume();
            }
        });
    }

    /**
     * Metodo collegato al bottone di login (tramite FXML).
     * Valida le credenziali inserite, verifica l'approvazione dell'utente
     * e gestisce il passaggio alla schermata successiva se il login ha successo.
     *
     * @param event evento generato dal clic sul bottone di login
     */
    @FXML
    private void handleLogin(ActionEvent event) { // gestione del bottone per il login
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();

        // Reset feedback
        feedbackText.setText("");

        if (username.isEmpty()) {
            feedbackText.setText("Inserisci il tuo username!");
            return;
        }
        if (password.isEmpty()) {
            feedbackText.setText("Inserisci la tua password!");
            return;
        }
        Type userType = new TypeToken<List<Utente>>() {}.getType();
        List<Utente> utenti = JsonUtils.leggiDaJson(USER_JSON_PATH, userType);

        if (utenti == null) {
            feedbackText.setText("Errore: impossibile leggere il file utenti.");
            return;
        }

        for (Utente u : utenti) { // controllo sull'approvazione dell'utente
            if (username.equals(u.getUsername()) && password.equals(u.getPassword())) {
              
                if (!u.isApproved()) {
                    feedbackText.setText("Il tuo account non è ancora approvato.");
                    return;
                }

                // Imposta l'utente attuale come loggato in SessionManager
                login(u);
                // Login riuscito: cambia scena
                mainController.show("Esplora");
                mainController.reloadTopBar();
                mainController.showNavBar();
                return;
            }
        }

        feedbackText.setText("Credenziali errate.");
    }

    /**
     * Funzione di passaggio alla schermata di register.
     */
    @FXML
    private void goToRegister(javafx.scene.input.MouseEvent event) {
        // Naviga alla schermata di registrazione
        mainController.show("Register");
    }
}
