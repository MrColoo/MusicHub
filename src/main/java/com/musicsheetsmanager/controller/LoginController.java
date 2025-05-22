package com.musicsheetsmanager.controller;

import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

// Controller per il login degli utenti
public class LoginController implements Controller {

    @FXML private TextField loginUsernameField; // campo testo per l'USERNAME
    @FXML private TextField loginPasswordField; // campo testo per la PASSWORD
    @FXML private Button loginButton; // bottone per il login
    @FXML private Text loginToRegisterButton; // testo che se cliccato porta alla registrazione
    @FXML private Text feedbackText; // testo di Feedback

    private static final String USER_JSON_PATH =
            "src/main/resources/com/musicsheetsmanager/data/user.json"; // percorso del file JSON

    private MainController mainController;

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
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

        List<Utente> utenti = caricaListaUtenti();
        if (utenti == null) {
            feedbackText.setText("Errore: impossibile leggere il file utenti.");
            return;
        }

        for (Utente u : utenti) {
            if (username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                // Login riuscito: cambia scena
                mainController.show("Esplora");
                return;
            }
        }

        feedbackText.setText("Credenziali errate.");
    }

    // Carica la lista di utenti dal file JSON
    private List<Utente> caricaListaUtenti() {
        try (FileReader r = new FileReader(USER_JSON_PATH)) {
            Type listType = new TypeToken<List<Utente>>() {}.getType();
            return new Gson().fromJson(r, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void goToRegister(MouseEvent event) {
        // Naviga alla schermata di registrazione
        mainController.show("Register");
    }
}