// Esta utilidad calcula el tamaño de las marcas de agua de forma proporcional
// al tamaño de la imagen base.
package com.amvisual.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public class WatermarkSizingUtil {

    /**
     * Calcula las dimensiones de la marca de agua para que se ajuste a un porcentaje del ancho de la imagen base,
     * manteniendo la relación de aspecto de la marca de agua.
     *
     * @param imagenBase      La imagen sobre la que se aplicará la marca de agua.
     * @param imagenMarcaAgua La imagen de la marca de agua.
     * @param scalePercentage El porcentaje del ancho de la imagen base que debe ocupar la marca de agua (e.g., 0.2 para 20%).
     * @return Una {@link Dimension} con el nuevo ancho y alto para la marca de agua.
     */
    public static Dimension calculateWatermarkSize(BufferedImage imagenBase, BufferedImage imagenMarcaAgua, double scalePercentage) {
        if (imagenBase == null || imagenMarcaAgua == null || scalePercentage <= 0) {
            return new Dimension(imagenMarcaAgua.getWidth(), imagenMarcaAgua.getHeight());
        }

        int baseWidth = imagenBase.getWidth();
        int watermarkOriginalWidth = imagenMarcaAgua.getWidth();
        int watermarkOriginalHeight = imagenMarcaAgua.getHeight();

        int newWidth = (int) (baseWidth * scalePercentage);
        
        double aspectRatio = (double) watermarkOriginalHeight / watermarkOriginalWidth;
        int newHeight = (int) (newWidth * aspectRatio);

        return new Dimension(newWidth, newHeight);
    }
}
