// Este archivo actúa como una fachada, simplificando la interacción con el sistema
// de posicionamiento. Expone los métodos principales para calcular la configuración
// y aplicar la marca de agua, coordinando `WatermarkCalculator` y `WatermarkRenderer`.
package com.amvisual.model.posicionamiento;

import java.awt.image.BufferedImage;

public final class WatermarkPositioner {
    
    // Constructor privado para evitar instanciación
    private WatermarkPositioner() {
        throw new AssertionError("Clase de utilidad no instanciable");
    }
    
    // ==================== API Pública Principal ====================
    
    /**
     * MÉTODO 1: Calcula la configuración relativa desde coordenadas absolutas.
     * 
     * Este método se usa cuando el usuario posiciona una marca de agua en una
     * imagen de referencia. Convierte las coordenadas absolutas (píxeles) en
     * proporciones relativas que se pueden aplicar a cualquier imagen.
     * 
     * CASO DE USO:
     * - Usuario arrastra marca de agua en la interfaz
     * - Sistema captura posición (x, y) y tamaño (ancho, alto) en píxeles
     * - Este método convierte esos valores a configuración relativa
     * - Configuración se guarda y se puede aplicar a otras imágenes
     * 
     * @param imageWidth Ancho de la imagen de referencia
     * @param imageHeight Alto de la imagen de referencia
     * @param watermarkX Posición X de la marca (esquina superior izquierda)
     * @param watermarkY Posición Y de la marca (esquina superior izquierda)
     * @param watermarkWidth Ancho de la marca en píxeles
     * @param watermarkHeight Alto de la marca en píxeles
     * 
     * @return Configuración relativa inmutable
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public static WatermarkConfig calculateRelativeConfig(
            double imageWidth, double imageHeight,
            double watermarkX, double watermarkY,
            double watermarkWidth, double watermarkHeight) {
        
        return WatermarkCalculator.calculateRelativeConfig(
            imageWidth, imageHeight,
            watermarkX, watermarkY,
            watermarkWidth, watermarkHeight
        );
    }
    
    /**
     * MÉTODO 2: Aplica una marca de agua sobre una imagen destino.
     * 
     * Este método combina el cálculo de dimensiones absolutas y el renderizado
     * en una sola operación conveniente.
     * 
     * CASO DE USO:
     * - Sistema tiene una configuración relativa guardada
     * - Usuario quiere aplicar marca a una o varias imágenes
     * - Este método calcula dimensiones y renderiza en un solo paso
     * 
     * @param targetImage Imagen sobre la que se aplicará la marca
     * @param watermarkImage Imagen original de la marca de agua
     * @param config Configuración relativa a aplicar
     * @param opacity Opacidad de la marca (0.0 a 1.0)
     * 
     * @return Nueva imagen con la marca de agua aplicada
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public static BufferedImage applyWatermark(
            BufferedImage targetImage,
            BufferedImage watermarkImage,
            WatermarkConfig config,
            float opacity) {
        
        // Validación básica
        if (targetImage == null) {
            throw new IllegalArgumentException("targetImage no puede ser null");
        }
        if (watermarkImage == null) {
            throw new IllegalArgumentException("watermarkImage no puede ser null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config no puede ser null");
        }
        
        // PASO 1: Calcular dimensiones absolutas para la imagen destino
        WatermarkCalculator.AbsoluteDimensions dimensions = 
            WatermarkCalculator.applyToTarget(
                config,
                targetImage.getWidth(),
                targetImage.getHeight()
            );
        
        // PASO 2: Renderizar la marca sobre la imagen
        return WatermarkRenderer.applyWatermark(
            targetImage,
            watermarkImage,
            dimensions,
            opacity
        );
    }
    
    // ==================== Métodos de Utilidad ====================
    
    /**
     * Calcula dimensiones absolutas sin renderizar.
     * 
     * Útil para previsualización o cuando solo se necesitan las dimensiones
     * sin aplicar realmente la marca de agua.
     * 
     * @param config Configuración relativa
     * @param targetWidth Ancho de la imagen destino
     * @param targetHeight Alto de la imagen destino
     * @return Dimensiones absolutas calculadas
     */
    public static WatermarkCalculator.AbsoluteDimensions calculateAbsoluteDimensions(
            WatermarkConfig config, double targetWidth, double targetHeight) {
        
        return WatermarkCalculator.applyToTarget(config, targetWidth, targetHeight);
    }
    
    /**
     * Crea una configuración inicial centrada con tamaño proporcional.
     * 
     * Útil para inicializar nuevas marcas de agua con valores por defecto.
     * 
     * @param imageWidth Ancho de la imagen de referencia
     * @param imageHeight Alto de la imagen de referencia
     * @param watermarkWidth Ancho original de la marca
     * @param watermarkHeight Alto original de la marca
     * @param relativeSize Tamaño relativo deseado (ej: 0.20 = 20% de la dimensión base)
     * 
     * @return Configuración centrada con el tamaño especificado
     */
    public static WatermarkConfig createCenteredConfig(
            double imageWidth, double imageHeight,
            double watermarkWidth, double watermarkHeight,
            double relativeSize) {
        
        if (relativeSize <= 0) {
            throw new IllegalArgumentException("relativeSize debe ser positivo");
        }
        
        // Calcular dimensión base
        double baseDimension = Math.min(imageWidth, imageHeight);
        
        // Calcular tamaño absoluto de la marca
        double absoluteWidth = baseDimension * relativeSize;
        double aspectRatio = watermarkHeight / watermarkWidth;
        double absoluteHeight = absoluteWidth * aspectRatio;
        
        // Calcular posición centrada
        double centerX = (imageWidth - absoluteWidth) / 2.0;
        double centerY = (imageHeight - absoluteHeight) / 2.0;
        
        // Crear configuración relativa
        return calculateRelativeConfig(
            imageWidth, imageHeight,
            centerX, centerY,
            absoluteWidth, absoluteHeight
        );
    }
    
    /**
     * Valida que una configuración sea aplicable a una imagen dada.
     * 
     * @param config Configuración a validar
     * @param imageWidth Ancho de la imagen
     * @param imageHeight Alto de la imagen
     * @return true si la configuración es aplicable
     */
    public static boolean isConfigValidForImage(
            WatermarkConfig config, double imageWidth, double imageHeight) {
        
        if (config == null || !config.isValid()) {
            return false;
        }
        
        if (imageWidth <= 0 || imageHeight <= 0) {
            return false;
        }
        
        try {
            WatermarkCalculator.AbsoluteDimensions dims = 
                WatermarkCalculator.applyToTarget(config, imageWidth, imageHeight);
            return dims.isValid();
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== Métodos de Diagnóstico (v6.1) ====================
    
    /**
     * Genera información de diagnóstico detallada sobre el posicionamiento.
     * 
     * Útil para depuración y verificación de cálculos.
     * 
     * @param config Configuración a analizar
     * @param targetWidth Ancho de la imagen destino
     * @param targetHeight Alto de la imagen destino
     * @return String con información detallada
     */
    public static String getDiagnosticInfo(
            WatermarkConfig config, double targetWidth, double targetHeight) {
        
        return WatermarkCalculator.getDiagnosticInfo(config, targetWidth, targetHeight);
    }
    
    /**
     * Verifica si una configuración producirá una marca dentro de los límites.
     * 
     * @param config Configuración a verificar
     * @param imageWidth Ancho de la imagen
     * @param imageHeight Alto de la imagen
     * @return true si la marca quedará completamente dentro de la imagen
     */
    public static boolean willBeWithinBounds(
            WatermarkConfig config, double imageWidth, double imageHeight) {
        
        try {
            WatermarkCalculator.AbsoluteDimensions dims = 
                WatermarkCalculator.applyToTarget(config, imageWidth, imageHeight);
            return WatermarkCalculator.isWithinBounds(dims, imageWidth, imageHeight);
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== Métodos para Extensiones Futuras ====================
    
    /**
     * EXTENSIÓN FUTURA: Detección automática de área óptima.
     * 
     * Esta es una plantilla para implementar detección inteligente de posición.
     * Por ahora retorna la configuración sin cambios.
     * 
     * POSIBLES ALGORITMOS:
     * - Detección de bordes
     * - Análisis de luminosidad
     * - Detección de áreas de bajo contraste
     * - Machine learning para áreas óptimas
     */
    public static WatermarkConfig detectOptimalPosition(
            BufferedImage targetImage,
            WatermarkConfig currentConfig) {
        
        // TODO: Implementar detección inteligente
        System.out.println("Detección automática aún no implementada");
        return currentConfig;
    }
    
    /**
     * EXTENSIÓN FUTURA: Ajuste adaptativo de opacidad.
     * 
     * Plantilla para ajustar opacidad según características de la imagen.
     */
    public static float calculateAdaptiveOpacity(
            BufferedImage targetImage,
            WatermarkCalculator.AbsoluteDimensions position) {
        
        // TODO: Implementar cálculo adaptativo
        return 0.7f; // Valor por defecto
    }
}
