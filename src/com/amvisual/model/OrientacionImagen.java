package com.amvisual.model;

/**
 * Enumeración que representa la orientación de una imagen
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public enum OrientacionImagen {
    /**
     * Imagen horizontal (ancho > alto)
     */
    HORIZONTAL,
    
    /**
     * Imagen vertical (alto > ancho)
     */
    VERTICAL,
    
    /**
     * Imagen cuadrada (ancho ≈ alto, diferencia < 5%)
     */
    CUADRADA;
    
    /**
     * Determina la orientación de una imagen basándose en sus dimensiones
     * @param ancho Ancho de la imagen
     * @param alto Alto de la imagen
     * @return La orientación de la imagen
     */
    public static OrientacionImagen determinar(int ancho, int alto) {
        if (ancho <= 0 || alto <= 0) {
            return CUADRADA; // Valor por defecto
        }
        
        double ratio = (double) ancho / alto;
        
        // Si la diferencia es menor al 5%, considerarla cuadrada
        if (ratio >= 0.95 && ratio <= 1.05) {
            return CUADRADA;
        } else if (ratio > 1.05) {
            return HORIZONTAL;
        } else {
            return VERTICAL;
        }
    }
    
    /**
     * Determina la orientación de una imagen BufferedImage
     * @param imagen Imagen a analizar
     * @return La orientación de la imagen
     */
    public static OrientacionImagen determinar(java.awt.image.BufferedImage imagen) {
        if (imagen == null) {
            return CUADRADA;
        }
        return determinar(imagen.getWidth(), imagen.getHeight());
    }
}
