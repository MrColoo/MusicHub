package com.musicsheetsmanager.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.musicsheetsmanager.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.FileReader;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField loginUsernameField;
    @FXML
    private TextField loginPasswordField;

    private final String USER_JSON_PATH =
            "C:\\Documenti\\UNI\\Ingegneria Software\\Progetto_Spartiti_Musicali\\Codice\\src\\main\\resources\\com\\musicsheetsmanager\\data\\user.json";

    @FXML
    private void onLoginClicked() {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();

        if (username.isEmpty()) {
            System.out.println("Inserisci il tuo username!");
            return;
        }
        if (password.isEmpty()) {
            System.out.println("Inserisci la tua password!");
            return;
        }

        Utente utenteSalvato = caricaUtenteDaJson();

        if (utenteSalvato == null) {
            System.out.println("Errore: impossibile leggere il file utente.");
            return;
        }

        if (username.equals(utenteSalvato.getUsername()) &&
                password.equals(utenteSalvato.getPassword())) {
            System.out.println("Login riuscito!");
        } else {
            System.out.println("Credenziali errate.");
        }
    }

    private Utente caricaUtenteDaJson() {
        try (FileReader reader = new FileReader(USER_JSON_PATH)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, Utente.class);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}

