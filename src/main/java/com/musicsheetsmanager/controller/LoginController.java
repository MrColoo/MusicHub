package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musicsheetsmanager.model.Utente;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class LoginController {

    @FXML private TextField loginUsernameField;
    @FXML private TextField loginPasswordField;
    @FXML private Button    loginButton;

    private final String USER_JSON_PATH =
            "src/main/resources/com/musicsheetsmanager/data/user.json";

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();

        if (username.isEmpty()) {
            showAlert("Inserisci il tuo username!");
            return;
        }
        if (password.isEmpty()) {
            showAlert("Inserisci la tua password!");
            return;
        }

        List<Utente> utenti = caricaListaUtenti();
        if (utenti == null) {
            showAlert("Errore: impossibile leggere il file utenti.");
            return;
        }

        for (Utente u : utenti) {
            if (username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                showAlert("Login riuscito!");
                cambiaSchermataAMain(event);
                return;
            }
        }

        showAlert("Credenziali errate.");
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Login");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private List<Utente> caricaListaUtenti() {
        try (FileReader r = new FileReader(USER_JSON_PATH)) {
            Type listType = new TypeToken<List<Utente>>(){}.getType();
            return new Gson().fromJson(r, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cambiaSchermataAMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/musicsheetsmanager/fxml/Main.fxml")
            );
            Parent root = loader.load();

            // Prendi il controller e invoca showEsplora()
            MainController mainCtrl = loader.getController();
            mainCtrl.showEsplora();

            // Cambia la scena
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(new Scene(root));
            window.show();

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Impossibile caricare la schermata principale:\n" + e.getMessage()
            ).showAndWait();
            e.printStackTrace();
        }
    }
}
