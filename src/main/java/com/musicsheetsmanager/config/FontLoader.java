package com.musicsheetsmanager.config;

import javafx.scene.text.Font;

public class FontLoader {

    public static void loadCircularFonts() {
        String[] fontFiles = {
                "CircularStd-Black.otf",
                "CircularStd-BlackItalic.otf",
                "CircularStd-Bold.otf",
                "CircularStd-BoldItalic.otf",
                "CircularStd-Book.otf",
                "CircularStd-BookItalic.otf",
                "CircularStd-Light.otf",
                "CircularStd-LightItalic.otf",
                "CircularStd-Medium.otf",
                "CircularStd-MediumItalic.otf"
        };

        for (String fontFile : fontFiles) {
            Font font = Font.loadFont(FontLoader.class.getResource("/com/musicsheetsmanager/ui/fonts/" + fontFile).toExternalForm(), 10);
            if (font != null) {
                System.out.println("Loaded font: " + font.getName());
            } else {
                System.err.println("Failed to load: " + fontFile);
            }
        }
    }
}
