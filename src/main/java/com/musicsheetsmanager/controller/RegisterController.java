package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RegisterController {

    @FXML
    private TextField registerEmailField;
    @FXML
    private TextField registerUsernameField;
    @FXML
    private TextField registerPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Text feedbackText;

    private static final Path USER_JSON_PATH = Paths.get(
            "src", "main", "resources",
            "com", "musicsheetsmanager", "data", "user.json"
    );

    @FXML
    private void onRegisterClicked() {
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

        Utente newUser = new Utente(email, username, password);
        salvaUtenteInJson(newUser);

        feedbackText.setText("Registrazione avvenuta con successo!");

        // Vai alla schermata di login
        cambiaASchermataLogin();
    }

    private void salvaUtenteInJson(Utente newUser) {
        // Crea un Gson con pretty printing
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

    private void cambiaASchermataLogin() {
        try {
            Parent root = FXMLLoader.load(getClass()
                    .getResource("/com/musicsheetsmanager/view/LoginView.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della schermata di login:");
            e.printStackTrace();
        }
    }
}
