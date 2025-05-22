package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
public class RegisterController {

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

    private static final Path USER_JSON_PATH = Paths.get( // percorso verso il file JSON
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "user.json"
    );

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

    private void salvaUtenteInJson(Utente newUser) {
        // Crea un Gson con la funzione di pretty printing (formattandolo in maniera leggibile e con nuove linee)
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        // Deserializza la lista esistente di utenti
        List<Utente> users;
        Type listType = new TypeToken<List<Utente>>() {}.getType();
        try (FileReader reader = new FileReader(USER_JSON_PATH.toFile())) {
            users = gson.fromJson(reader, listType);
            if (users == null) {
                users = new ArrayList<>();
            }
        } catch (IOException | JsonSyntaxException e) {
            // Se file mancante o JSON malformato, crea lista vuota
            users = new ArrayList<>();
        }

        // Aggiunge il nuovo utente
        users.add(newUser);

        // Serializza e salva la lista aggiornata
        try (FileWriter writer = new FileWriter(USER_JSON_PATH.toFile())) {
            gson.toJson(users, writer);
            System.out.println("Utenti salvati correttamente: totale = " + users.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
