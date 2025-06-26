package com.musicsheetsmanager.config;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class UiUtils {

    /**
     * Estrae il colore dominante da un'immagine (campionamento 5x5 pixel).
     */
    public static Color estraiColoreDominante(Image image) {
        PixelReader reader = image.getPixelReader();
        int width = (int) image.getWidth(), height = (int) image.getHeight();
        long r = 0, g = 0, b = 0, count = 0;

        for (int y = 0; y < height; y += 5) {
            for (int x = 0; x < width; x += 5) {
                Color c = reader.getColor(x, y);
                r += c.getRed() * 255;
                g += c.getGreen() * 255;
                b += c.getBlue() * 255;
                count++;
            }
        }

        return Color.rgb((int)(r / count), (int)(g / count), (int)(b / count));
    }

    /**
     * Converte colore JavaFX in HEX
     */
    public static String toHexColor(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }


}
