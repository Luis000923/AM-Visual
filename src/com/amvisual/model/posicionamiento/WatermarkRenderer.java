// Este archivo se encarga de renderizar (dibujar) la marca de agua sobre la imagen
// principal. Recibe las dimensiones calculadas y la opacidad, y devuelve una nueva
// imagen con la marca aplicada. Es utilizado por `WatermarkPositioner`.
package com.amvisual.model.posicionamiento;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class WatermarkRenderer {
    
    // Constructor privado para evitar instanciación
    private WatermarkRenderer() {
        throw new AssertionError("Clase de utilidad no instanciable");
    }
    
    /**
     * Aplica una marca de agua sobre una imagen destino.
     * 
     * @param targetImage Imagen sobre la que se aplicará la marca
     * @param watermarkImage Imagen original de la marca de agua
     * @param dimensions Dimensiones absolutas calculadas (posición y tamaño)
     * @param opacity Opacidad de la marca (0.0 = transparente, 1.0 = opaco)
     * 
     * @return Nueva imagen con la marca de agua aplicada
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public static BufferedImage applyWatermark(
            BufferedImage targetImage,
            BufferedImage watermarkImage,
            WatermarkCalculator.AbsoluteDimensions dimensions,
            float opacity) {
        
        // Validación de parámetros
        validateImages(targetImage, watermarkImage);
        validateDimensions(dimensions);
        validateOpacity(opacity);
        
        // Si las dimensiones son muy pequeñas, no renderizar
        if (dimensions.getWidth() < 1 || dimensions.getHeight() < 1) {
            System.err.println("Advertencia: Dimensiones de marca muy pequeñas, se omite renderizado");
            return targetImage;
        }
        
        // PASO 1: Redimensionar la marca de agua al tamaño calculado
        BufferedImage resizedWatermark = resizeWatermark(
            watermarkImage,
            (int) Math.round(dimensions.getWidth()),
            (int) Math.round(dimensions.getHeight())
        );
        
        // PASO 2: Crear imagen resultado con canal alpha
        BufferedImage resultImage = new BufferedImage(
            targetImage.getWidth(),
            targetImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        
        // PASO 3: Componer la imagen con la marca de agua
        Graphics2D g2d = resultImage.createGraphics();
        try {
            // Configurar renderizado de alta calidad
            configureHighQualityRendering(g2d);
            
            // Dibujar la imagen base
            g2d.drawImage(targetImage, 0, 0, null);
            
            // Aplicar composición con opacidad
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            
            // Dibujar la marca de agua en la posición calculada
            g2d.drawImage(
                resizedWatermark,
                (int) Math.round(dimensions.getX()),
                (int) Math.round(dimensions.getY()),
                null
            );
            
        } finally {
            g2d.dispose();
        }
        
        return resultImage;
    }
    
    /**
     * Redimensiona una imagen de marca de agua con alta calidad.
     * 
     * @param watermark Imagen original
     * @param targetWidth Ancho deseado
     * @param targetHeight Alto deseado
     * @return Imagen redimensionada
     */
    private static BufferedImage resizeWatermark(
            BufferedImage watermark, int targetWidth, int targetHeight) {
        
        // Validar dimensiones objetivo
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException(
                String.format("Dimensiones objetivo inválidas: %dx%d", targetWidth, targetHeight));
        }
        
        // Crear imagen redimensionada con canal alpha
        BufferedImage resized = new BufferedImage(
            targetWidth,
            targetHeight,
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = resized.createGraphics();
        try {
            // Configurar interpolación de alta calidad
            configureHighQualityRendering(g2d);
            
            // Redimensionar con interpolación bilineal
            g2d.drawImage(watermark, 0, 0, targetWidth, targetHeight, null);
            
        } finally {
            g2d.dispose();
        }
        
        return resized;
    }
    
    /**
     * Configura Graphics2D para renderizado de alta calidad.
     */
    private static void configureHighQualityRendering(Graphics2D g2d) {
        // Interpolación bilineal para suavizado
        g2d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        
        // Antialiasing para bordes suaves
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        
        // Renderizado de alta calidad
        g2d.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        );
        
        // Alpha interpolation de calidad
        g2d.setRenderingHint(
            RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        );
    }
    
    // ==================== Métodos de Validación ====================
    
    private static void validateImages(BufferedImage target, BufferedImage watermark) {
        if (target == null) {
            throw new IllegalArgumentException("targetImage no puede ser null");
        }
        if (watermark == null) {
            throw new IllegalArgumentException("watermarkImage no puede ser null");
        }
        if (target.getWidth() <= 0 || target.getHeight() <= 0) {
            throw new IllegalArgumentException("targetImage tiene dimensiones inválidas");
        }
        if (watermark.getWidth() <= 0 || watermark.getHeight() <= 0) {
            throw new IllegalArgumentException("watermarkImage tiene dimensiones inválidas");
        }
    }
    
    private static void validateDimensions(WatermarkCalculator.AbsoluteDimensions dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions no puede ser null");
        }
        if (!dimensions.isValid()) {
            throw new IllegalArgumentException("dimensions no es válida para renderizado");
        }
    }
    
    private static void validateOpacity(float opacity) {
        if (Float.isNaN(opacity)) {
            throw new IllegalArgumentException("opacity no puede ser NaN");
        }
        if (opacity < 0.0f || opacity > 1.0f) {
            throw new IllegalArgumentException(
                String.format("opacity debe estar entre 0.0 y 1.0, recibido: %.2f", opacity));
        }
    }
}
