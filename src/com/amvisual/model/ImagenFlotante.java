// Este modelo representa una única marca de agua que puede ser aplicada a una imagen.
// Contiene la imagen original de la marca, su opacidad y su configuración de posicionamiento.
// Se conecta con:
// - WatermarkConfig: Almacena la configuración relativa de posicionamiento.
// - WatermarkPositioner: Delega el cálculo y la aplicación de la marca de agua.
// - ProyectoMarcaAgua: Es contenido y gestionado dentro de este modelo principal.
package com.amvisual.model;

import java.awt.image.BufferedImage;
import com.amvisual.model.posicionamiento.WatermarkConfig;
import com.amvisual.model.posicionamiento.WatermarkPositioner;

public class ImagenFlotante {
    
    private final BufferedImage imagenOriginal;
    private final String nombreArchivo;
    private float opacidad;
    
    // Una sola configuración para todas las imágenes
    private WatermarkConfig config;

    /**
     * Constructor.
     * @param imagen Imagen original de la marca de agua.
     * @param nombreArchivo Nombre del archivo de la imagen.
     */
    public ImagenFlotante(BufferedImage imagen, String nombreArchivo) {
        this.imagenOriginal = imagen;
        this.nombreArchivo = nombreArchivo;
        this.opacidad = 0.7f; // Valor por defecto
        this.config = null;
    }
    
    /**
     * Dibuja la marca de agua sobre una imagen base usando la configuración.
     * Si no existe configuración, crea una configuración por defecto inteligente.
     * 
     * @param imagenBase La imagen sobre la que se dibujará la marca.
     * @return Una nueva BufferedImage con la marca de agua aplicada.
     */
    public BufferedImage dibujar(BufferedImage imagenBase) {
        // Si no hay configuración, crear una por defecto inteligente
        if (config == null) {
            System.out.println("Advertencia: No hay configuración de marca de agua para '" 
                + nombreArchivo + "'. Creando configuración por defecto...");
            
            // Crear configuración centrada con tamaño adaptativo
            double imageWidth = imagenBase.getWidth();
            double imageHeight = imagenBase.getHeight();
            double wmWidth = imagenOriginal.getWidth();
            double wmHeight = imagenOriginal.getHeight();
            
            // Calcular tamaño relativo inteligente basado en la orientación
            double aspectRatio = imageWidth / imageHeight;
            double relativeSize;
            
            if (aspectRatio > 1.5) {
                // Imagen muy horizontal: marca más pequeña (12%)
                relativeSize = 0.12;
            } else if (aspectRatio < 0.67) {
                // Imagen muy vertical: marca más pequeña (12%)
                relativeSize = 0.12;
            } else {
                // Imagen cuadrada o normal: tamaño medio (15%)
                relativeSize = 0.15;
            }
            
            // Crear configuración por defecto
            config = WatermarkPositioner.createCenteredConfig(
                imageWidth, imageHeight,
                wmWidth, wmHeight,
                relativeSize
            );
        }
        
        // Aplicar la marca de agua usando el algoritmo de posicionamiento
        return WatermarkPositioner.applyWatermark(
            imagenBase,
            this.imagenOriginal,
            config,
            this.opacidad
        );
    }
    
    /**
     * Actualiza la configuración de la marca de agua
     * basándose en la posición y tamaño absolutos en una imagen de referencia.
     *
     * @param refWidth Ancho de la imagen de referencia.
     * @param refHeight Alto de la imagen de referencia.
     * @param watermarkX Posición X absoluta de la marca.
     * @param watermarkY Posición Y absoluta de la marca.
     * @param watermarkWidth Ancho absoluto de la marca.
     * @param watermarkHeight Alto absoluto de la marca.
     */
    public void actualizarConfiguracion(double refWidth, double refHeight, 
                                       double watermarkX, double watermarkY, 
                                       double watermarkWidth, double watermarkHeight) {
        // Calcular la nueva configuración relativa
        this.config = WatermarkPositioner.calculateRelativeConfig(
            refWidth, refHeight,
            watermarkX, watermarkY,
            watermarkWidth, watermarkHeight
        );
    }
    
    /**
     * Obtiene la configuración de la marca de agua.
     * 
     * @param orientacion Parámetro ignorado, se mantiene para compatibilidad.
     * @return La configuración actual, o null si no hay ninguna.
     */
    public WatermarkConfig getConfig(OrientacionImagen orientacion) {
        return config;
    }
    
    /**
     * Asigna una configuración a la marca de agua.
     * 
     * @param config La configuración a guardar.
     * @param orientacion Parámetro ignorado, se mantiene para compatibilidad.
     */
    public void setConfig(WatermarkConfig config, OrientacionImagen orientacion) {
        this.config = config;
    }
    
    
    // --- Getters y Setters simples ---
    
    public BufferedImage getImagenOriginal() { return imagenOriginal; }
    public String getNombreArchivo() { return nombreArchivo; }
    public float getOpacidad() { return opacidad; }
    public void setOpacidad(float opacidad) { this.opacidad = Math.max(0.0f, Math.min(1.0f, opacidad)); }
    
    public boolean hasConfig(OrientacionImagen orientacion) {
        return config != null;
    }
    
    public boolean hasAllConfigs() {
        return config != null;
    }

    @Override
    public String toString() {
        return nombreArchivo;
    }
}
