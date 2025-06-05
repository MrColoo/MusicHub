package com.musicsheetsmanager.controller;


import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.config.JsonUtils;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;


// Controller per la registrazione di nuovi utenti
public class RegisterController implements Controller{

    @FXML
    private TextField registerEmailField; // input per EMAIL
    @FXML
    private TextField registerUsernameField; // input per USERNAME
    @FXML
    private TextField registerPasswordField; // input per PASSWORD
    @FXML
    private Button registerButton; // bottone per la registrazione
    @FXML
    private Text feedbackText; // testo di feedback(comunicare verso l'utente se manca qualcosa)
    @FXML
    private Text registerToLogin; // testo che se cliccato porta al login

    private static final Path USER_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "user.json"
    );

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // campi per controllo mail
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"; // pattern per il controllo della mail

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);


    @FXML
    private void onRegisterClicked() { // prendi i valori dei campi, rimuove tramite trim() spazi iniziali e finali
        String email = registerEmailField.getText().trim();
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText().trim();


        // richiamo la funzione per controllare se la mail è valida
        if (!isValidEmail(email)) {
            feedbackText.setText("Formato email non valido!");
            return;
        }

        // se un campo è vuoto mostra il messaggio di errore tramite il feedbackText
        if (email.isEmpty()) {
            feedbackText.setText("Inserisci la tua mail!");
            return;
        }

        if (username.isEmpty()) { // controllo se l'username è vuoto
            feedbackText.setText("Inserisci il tuo username!");
            return;
        }

        if (password.isEmpty()) { // controllo se la password è vuota
            feedbackText.setText("Inserisci la tua password!");
            return;
        }

        // crea un nuovo utente con i dati inseriti
        Utente newUser = new Utente(email, username, password, false); // nuovo utente che si registra
        newUser.setApproved(false); // setto l'approvazione direttamente a FALSE
        salvaUtenteInJson(newUser);

        // Mostro la conferma di registrazione
        feedbackText.setText("Registrazione avvenuta con successo!");

    }

    // funzione per salvare l'utente nel file JSON
    private boolean salvaUtenteInJson(Utente newUser) {
        Type listType = new TypeToken<List<Utente>>() {}.getType();
        List<Utente> users = JsonUtils.leggiDaJson(USER_JSON_PATH, listType);

        for (Utente user : users) {
            if (user.getEmail().equalsIgnoreCase(newUser.getEmail())) {
                feedbackText.setText("Questa email è già registrata!"); // controlli su la mail inserita e il file JSON
                return false;
            }
            if (user.getUsername().equalsIgnoreCase(newUser.getUsername())) {
                feedbackText.setText("Questo username è già in uso!"); // controlli su la mail inserita e il file JSON
                return false;
            }
        }

        users.add(newUser);
        JsonUtils.scriviSuJson(users, USER_JSON_PATH);
        System.out.println("Utenti salvati correttamente: totale = " + users.size()); // linee per facilitare il debug da terminale
        return true;
    }


    @FXML // quando premo il testo Register mi cambia schermata a quella di register
    private void goToLogin(MouseEvent event) {
        mainController.show("Login");
    }


    // Restituisce true se la stringa passata è nel formato di una mail valida.
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}