package com.musicsheetsmanager.config;

import java.io.File;

public class AppConfig {
    // Application settings
    public static final String APP_TITLE = "Dash App - JavaFX Version";

    // Data storage settings
    public static final String DATA_DIR = "src/resources/data";
    public static final String USERS_CSV_PATH = DATA_DIR + "/users.csv";

    // Create data directory if it doesn't exist
    static {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
}
