package com.musicsheetsmanager.controller;

import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

// Controller per il login degli utenti
public class LoginController {

    @FXML private TextField loginUsernameField; // campo testo per l'USERNAME
    @FXML private TextField loginPasswordField; // campo testo per la PASSWORD
    @FXML private Button    loginButton; // bottone per il login
    @FXML private Text loginToRegisterButton; // testo che se cliccato porta alla registrazione

    private final String USER_JSON_PATH =
            "src/main/resources/com/musicsheetsmanager/data/user.json"; // percorso del file JSON

    @FXML
    private void handleLogin(ActionEvent event) { // prende il testo e gli rimuove li spazi tramite trim()
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();

        if (username.isEmpty()) { // controlla che l'USERNAME non sia vuoto
            showAlert("Inserisci il tuo username!");
            return;
        }
        if (password.isEmpty()) { // controlla che la PASSWORD non sia vuota
            showAlert("Inserisci la tua password!");
            return;
        }

        List<Utente> utenti = caricaListaUtenti(); // carica la lista degli utente dal JSON
        if (utenti == null) {
            showAlert("Errore: impossibile leggere il file utenti."); // scrive sul Feedback Text l'errore
            return;
        }

        for (Utente u : utenti) { // confronto le credenziali con ciascuna credenziale della lista
            if (username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                showAlert("Login riuscito!"); // se trova conferma, scrive sulla FeedbackBox il messaggio
                // show(Main); // passo alla schermata principale
                return;
            }
        }

        showAlert("Credenziali errate.");
    }

    private void showAlert(String msg) { // manda un ALERT, DA RIMUOVERE
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Login");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private List<Utente> caricaListaUtenti() {
        try (FileReader r = new FileReader(USER_JSON_PATH)) {
            Type listType = new TypeToken<List<Utente>>(){}.getType(); // definisce il file generico List<Utente>
            return new Gson().fromJson(r, listType); // deserializza il file JSON in una List<Utente>
        } catch (IOException e) {
            // in caso di errore stampo lo stack trace e ritorno NULL
            e.printStackTrace();
            return null;
        }
    }

    @FXML // quando premo il testo Register mi cambia schermata a quella di register
    private void goToRegister(MouseEvent event) {
        show("Register");
    }
}
