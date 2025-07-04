package com.musicsheetsmanager.controller;

import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

// Controller per la registrazione di nuovi utenti
public class RegisterController implements Controller {

    @FXML
    private TextField registerEmailField;     // input per EMAIL
    @FXML
    private TextField registerUsernameField;  // input per USERNAME
    @FXML
    private PasswordField registerPasswordField;  // input per PASSWORD
    @FXML
    private Button registerButton;            // bottone per la registrazione
    @FXML
    private Text feedbackText;                // testo di feedback (comunicare verso l'utente se manca qualcosa)
    @FXML
    private Text registerToLogin;             // testo che se cliccato porta al login

    private static final Path USER_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "user.json"
    );

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Pattern per il controllo della mail
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Metodo chiamato automaticamente da JavaFX subito dopo l'inizializzazione
     * dei componenti FXML. Qui impostiamo il pulsante di default e aggiungiamo
     * un listener sul campo password per intercettare INVIO.
     */
    @FXML
    private void initialize() {
        // Rende registerButton “predefinito”: quando si preme Invio (ENTER) da uno dei TextField, viene fatto click su di esso.
        registerButton.setDefaultButton(true);

        // Listener sul TextField della password: se si preme Invio mentre il focus è su registerPasswordField, simula il click sul bottone.
        registerPasswordField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                registerButton.fire();
                event.consume();
            }
        });
    }

    /**
     * Metodo chiamato quando l'utente clicca sul pulsante "Registrati".
     * Valida i dati inseriti, crea un nuovo utente e lo salva nel file JSON.
     */
    @FXML
    private void onRegisterClicked() {
        // Prendi i valori dei campi, rimuove spazi iniziali e finali
        String email = registerEmailField.getText().trim();
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText().trim();

        // Controllo formato email (viene prima controllato il formato, poi se è vuoto)
        if (email.isEmpty()) {
            feedbackText.setFill(Color.RED);
            feedbackText.setText("Inserisci la tua mail!");
            return;
        }
        if (!isValidEmail(email)) {
            feedbackText.setFill(Color.RED);
            feedbackText.setText("Formato email non valido!");
            return;
        }

        // Controlli su username e password
        if (username.isEmpty()) {
            feedbackText.setFill(Color.RED);
            feedbackText.setText("Inserisci il tuo username!");
            return;
        }
        if (password.isEmpty()) {
            feedbackText.setFill(Color.RED);
            feedbackText.setText("Inserisci la tua password!");
            return;
        }

        // Crea un nuovo utente con i dati inseriti (setta di default approved e admin a false)
        Utente newUser = new Utente(email, username, password, false, false);

        // Provo a salvare l'utente nel JSON
        if (!salvaUtenteInJson(newUser)) {
            // In caso di duplicati, salvaUtenteInJson() ha già impostato feedbackText
            return;
        }

        // Mostro la conferma di registrazione
        feedbackText.setFill(Color.GREEN);
        feedbackText.setText("Registrazione avvenuta con successo!");
    }

    /**
     * Salva un nuovo utente nel file JSON, dopo aver controllato che
     * email e username non siano già presenti.
     *
     * @param newUser l'oggetto Utente da aggiungere
     * @return true se il salvataggio è andato a buon fine, false altrimenti
     */
    private boolean salvaUtenteInJson(Utente newUser) {
        Type listType = new TypeToken<List<Utente>>() {}.getType();
        List<Utente> users = JsonUtils.leggiDaJson(USER_JSON_PATH, listType);

        // Se per qualche motivo il file è vuoto o non leggibile, inizializzo lista pulita
        if (users == null) {
            feedbackText.setFill(Color.RED);
            feedbackText.setText("Errore: impossibile leggere il file utenti.");
            return false;
        }

        for (Utente user : users) {
            if (user.getEmail().equalsIgnoreCase(newUser.getEmail())) { // controllo che l'email non sia gia' presente
                feedbackText.setFill(Color.RED);
                feedbackText.setText("Questa email è già registrata!");
                return false;
            }
            if (user.getUsername().equalsIgnoreCase(newUser.getUsername())) { // controllo che l'username non sia gia' presente
                feedbackText.setFill(Color.RED);
                feedbackText.setText("Questo username è già in uso!");
                return false;
            }
        }

        users.add(newUser);
        JsonUtils.scriviSuJson(users, USER_JSON_PATH);
        System.out.println("Utenti salvati correttamente: totale = " + users.size());
        return true;
    }

    /**
     * Metodo collegato a un evento click (MouseEvent) per passare alla schermata di login.
     *
     * @param event l'evento generato dal click del mouse
     */
    @FXML
    private void goToLogin(MouseEvent event) {
        mainController.show("Login");
    }

    /**
     * Controlla se l'email ha un formato valido.
     *
     * @param email la stringa da validare
     * @return true se è una email ben formata, false altrimenti
     */
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
