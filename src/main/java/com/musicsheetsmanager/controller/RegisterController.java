package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;

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

        Utente user = new Utente(email, username, password);
        salvaUtenteInJson(user);

        feedbackText.setText("Registrazione avvenuta con successo!");

        // Vai alla schermata di login
        cambiaASchermataLogin();
    }

    private void salvaUtenteInJson(Utente user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);

        try (FileWriter writer = new FileWriter(
                "src/main/resources/com/musicsheetsmanager/data/user.json")) {
            writer.write(json);
            System.out.println("Utente salvato correttamente!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cambiaASchermataLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/musicsheetsmanager/view/LoginView.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della schermata di login:");
            e.printStackTrace();
        }
    }
}
