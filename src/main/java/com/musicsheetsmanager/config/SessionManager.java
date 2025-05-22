package com.musicsheetsmanager.config;

import com.musicsheetsmanager.model.Utente;

public class SessionManager {
    private static Utente currentUser = null;

    public static void login(Utente user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static Utente getLoggedUser() {
        return currentUser;
    }
}

