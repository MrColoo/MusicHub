package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(EMAIL_REGEX);


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

        if (username.isEmpty()) {
            feedbackText.setText("Inserisci il tuo username!");
            return;
        }

        if (password.isEmpty()) {
            feedbackText.setText("Inserisci la tua password!");
            return;
        }

        // crea un nuovo utente con i dati inseriti
        Utente newUser = new Utente(email, username, password, false); // nuovo utente che si registra
        newUser.setApproved(false);
        salvaUtenteInJson(newUser);

        // Mostro la conferma di registrazione
        feedbackText.setText("Registrazione avvenuta con successo!");

    }

    // funzione per salvare l'utente nel file JSON
    private boolean salvaUtenteInJson(Utente newUser) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting() // per sciverlo in maniera facilmente leggibile nel JSON
                .create();

        List<Utente> users;
        // Definisce il tipo List<Utente> per la deserializzazione
        Type listType = new TypeToken<List<Utente>>() {}.getType();
        // apre in lettura il file JSON dove sono salvati gli utenti
        try (FileReader reader = new FileReader(USER_JSON_PATH.toFile())) {
            // Deserializza il contenuto del file in una List<Utente>
            users = gson.fromJson(reader, listType);
            if (users == null) {
                // Se il file esiste ma è vuoto o contiene “null”, inizializza una lista vuota
                users = new ArrayList<>();
            }
        } catch (IOException | JsonSyntaxException e) {
            // Se il file non esiste o il JSON è malformato, crea comunque una lista vuota
            users = new ArrayList<>();
        }

        // Controlla se email o username esistono già
        for (Utente user : users) {
            if (user.getEmail().equalsIgnoreCase(newUser.getEmail())) {
                feedbackText.setText("Questa email è già registrata!");
                return false;
            }
            if (user.getUsername().equalsIgnoreCase(newUser.getUsername())) {
                feedbackText.setText("Questo username è già in uso!");
                return false;
            }
        }

        // Aggiunge il nuovo utente
        users.add(newUser);

        // Salva la lista aggiornata
        try (FileWriter writer = new FileWriter(USER_JSON_PATH.toFile())) {
            gson.toJson(users, writer);
            System.out.println("Utenti salvati correttamente: totale = " + users.size());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            feedbackText.setText("Errore durante il salvataggio dell'utente.");
            return false;
        }
    }



    @FXML
    private void handleClick() { // gestione del bottone per la registrazione
        String email = registerEmailField.getText().trim();
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText().trim();

        if (email.isEmpty()) {
            feedbackText.setText("Inserisci la tua mail!");
            return;
        }

        if (username.isEmpty()) {
            feedbackText.setText("Inserisci il tuo username!");
            return;
        }

        if (password.isEmpty()) {
            feedbackText.setText("Inserisci la tua password!");
            return;
        }

        Utente newUser = new Utente(email, username, password, false);
        newUser.setApproved(false);

        boolean success = salvaUtenteInJson(newUser);
        if (success) {
            feedbackText.setText("Registrazione avvenuta con successo!");
            mainController.show("Login");
        }
    }

    @FXML // quando premo il testo Register mi cambia schermata a quella di register
    private void goToLogin(MouseEvent event) {
        mainController.show("Login");
    }


    // Restituisce true se la stringa passata è nel formato di una mail valida.
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
