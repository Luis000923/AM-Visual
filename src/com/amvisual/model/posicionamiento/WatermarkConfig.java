// Este archivo define un objeto inmutable para almacenar la configuración relativa
// de una marca de agua (posición y tamaño). Es utilizado por `WatermarkCalculator`
// para encapsular los resultados de sus cálculos.
package com.amvisual.model.posicionamiento;

public final class WatermarkConfig {
    
    /** Posición X relativa (0.0 - 1.0+) respecto al ancho de la imagen */
    private final double relativeX;
    
    /** Posición Y relativa (0.0 - 1.0+) respecto al alto de la imagen */
    private final double relativeY;
    
    /** Ancho relativo (0.0 - 1.0+) respecto a la dimensión más corta */
    private final double relativeWidth;
    
    /** Relación de aspecto (alto/ancho) de la marca de agua original */
    private final double aspectRatio;

    /**
     * Constructor para crear una configuración de marca de agua.
     * 
     * @param relativeX Posición X relativa (0.0 = borde izquierdo)
     * @param relativeY Posición Y relativa (0.0 = borde superior)
     * @param relativeWidth Ancho relativo a la dimensión más corta
     * @param aspectRatio Relación alto/ancho original de la marca
     * 
     * @throws IllegalArgumentException si algún valor es negativo o NaN
     */
    public WatermarkConfig(double relativeX, double relativeY, 
                          double relativeWidth, double aspectRatio) {
        validateParameter(relativeX, "relativeX");
        validateParameter(relativeY, "relativeY");
        validateParameter(relativeWidth, "relativeWidth");
        validateParameter(aspectRatio, "aspectRatio");
        
        if (relativeWidth <= 0) {
            throw new IllegalArgumentException("relativeWidth debe ser mayor que 0");
        }
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("aspectRatio debe ser mayor que 0");
        }
        
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeWidth = relativeWidth;
        this.aspectRatio = aspectRatio;
    }
    
    private void validateParameter(double value, String name) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException(name + " no puede ser NaN");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " no puede ser infinito");
        }
    }

    // ==================== Getters ====================
    
    public double getRelativeX() {
        return relativeX;
    }

    public double getRelativeY() {
        return relativeY;
    }

    public double getRelativeWidth() {
        return relativeWidth;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }
    
    // ==================== Métodos de Utilidad ====================
    
    /**
     * Crea una copia de esta configuración con una nueva posición X.
     */
    public WatermarkConfig withRelativeX(double newRelativeX) {
        return new WatermarkConfig(newRelativeX, relativeY, relativeWidth, aspectRatio);
    }
    
    /**
     * Crea una copia de esta configuración con una nueva posición Y.
     */
    public WatermarkConfig withRelativeY(double newRelativeY) {
        return new WatermarkConfig(relativeX, newRelativeY, relativeWidth, aspectRatio);
    }
    
    /**
     * Crea una copia de esta configuración con un nuevo ancho relativo.
     */
    public WatermarkConfig withRelativeWidth(double newRelativeWidth) {
        return new WatermarkConfig(relativeX, relativeY, newRelativeWidth, aspectRatio);
    }
    
    /**
     * Crea una copia escalada de esta configuración.
     * 
     * @param scaleFactor Factor de escala (1.0 = sin cambio, 2.0 = doble tamaño)
     */
    public WatermarkConfig scaled(double scaleFactor) {
        if (scaleFactor <= 0) {
            throw new IllegalArgumentException("scaleFactor debe ser positivo");
        }
        return new WatermarkConfig(relativeX, relativeY, 
                                   relativeWidth * scaleFactor, aspectRatio);
    }
    
    /**
     * Verifica si esta configuración es válida para renderizado.
     */
    public boolean isValid() {
        return relativeWidth > 0 && aspectRatio > 0 
            && !Double.isNaN(relativeX) && !Double.isNaN(relativeY)
            && !Double.isInfinite(relativeX) && !Double.isInfinite(relativeY);
    }
    
    // ==================== Object Methods ====================
    
    @Override
    public String toString() {
        return String.format("WatermarkConfig[x=%.3f, y=%.3f, width=%.3f, aspect=%.3f]",
                           relativeX, relativeY, relativeWidth, aspectRatio);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WatermarkConfig)) return false;
        
        WatermarkConfig other = (WatermarkConfig) obj;
        return Double.compare(relativeX, other.relativeX) == 0
            && Double.compare(relativeY, other.relativeY) == 0
            && Double.compare(relativeWidth, other.relativeWidth) == 0
            && Double.compare(aspectRatio, other.aspectRatio) == 0;
    }
    
    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(relativeX);
        bits ^= Double.doubleToLongBits(relativeY) * 31;
        bits ^= Double.doubleToLongBits(relativeWidth) * 37;
        bits ^= Double.doubleToLongBits(aspectRatio) * 41;
        return (int)(bits ^ (bits >>> 32));
    }
}
