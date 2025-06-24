package com.musicsheetsmanager.config;

public class StringUtils {

    /**
     * Capitalizza ogni parola di una stringa (prima lettera maiuscola, resto minuscolo).
     * Esempio: "paolo conte" → "Paolo Conte"
     */
    public static String capitalizzaTesto(String testo) {
        if (testo == null || testo.isEmpty()) return testo;

        String[] parole = testo.split(" ");
        StringBuilder risultato = new StringBuilder();

        for (String parola : parole) {
            if (!parola.isEmpty()) {
                risultato.append(Character.toUpperCase(parola.charAt(0)))
                        .append(parola.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return risultato.toString().trim();
    }

}
