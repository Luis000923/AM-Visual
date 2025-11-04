package com.amvisual.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilidades para interoperabilidad entre AWT BufferedImage y JavaFX Image.
 */
public class FxImageUtils {

    /**
     * Convierte un BufferedImage a Image de JavaFX.
     */
    public static Image toFxImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) return null;
        WritableImage img = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        return SwingFXUtils.toFXImage(bufferedImage, img);
    }

    /**
     * Convierte un Image de JavaFX a BufferedImage.
     */
    public static BufferedImage toBufferedImage(Image fxImage) {
        if (fxImage == null) return null;
        return SwingFXUtils.fromFXImage(fxImage, null);
    }

    /**
     * Carga una imagen desde archivo como JavaFX Image (opcionalmente escalada en ancho).
     */
    public static Image loadFxImage(File file, double requestedWidth) {
        if (file == null) return null;
        if (requestedWidth > 0) {
            return new Image(file.toURI().toString(), requestedWidth, 0, true, true);
        }
        return new Image(file.toURI().toString());
    }
}
