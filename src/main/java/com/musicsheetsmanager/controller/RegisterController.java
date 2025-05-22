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




    @FXML
    private void onRegisterClicked() { // prendi i valori dei campi, rimuove tramite trim() spazi iniziali e finali
        String email = registerEmailField.getText().trim();
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText().trim();


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

        // Vai alla schermata di login
        //show("Login");
    }

    private boolean salvaUtenteInJson(Utente newUser) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        List<Utente> users;
        Type listType = new TypeToken<List<Utente>>() {}.getType();
        try (FileReader reader = new FileReader(USER_JSON_PATH.toFile())) {
            users = gson.fromJson(reader, listType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (IOException | JsonSyntaxException e) {
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
    private void handleClick() {
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
            mainController.show("Esplora");
        }
    }

    @FXML // quando premo il testo Register mi cambia schermata a quella di register
    private void goToLogin(MouseEvent event) {
        mainController.show("Login");
    }
}
