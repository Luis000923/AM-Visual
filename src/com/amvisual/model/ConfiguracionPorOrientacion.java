package com.amvisual.model;

import java.awt.Point;

/**
 * Configuración de marca de agua específica para una orientación de imagen
 * Permite ajustes personalizados de posición, escala y opacidad por tipo de imagen
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class ConfiguracionPorOrientacion {
    private Point posicionRelativa;
    private double escala;
    private float opacidad;
    
    /**
     * Constructor con valores por defecto
     */
    public ConfiguracionPorOrientacion() {
        this.posicionRelativa = new Point(50, 50);
        this.escala = 0.5;
        this.opacidad = 0.7f;
    }
    
    /**
     * Constructor con valores específicos
     */
    public ConfiguracionPorOrientacion(int posX, int posY, double escala, float opacidad) {
        this.posicionRelativa = new Point(posX, posY);
        this.escala = escala;
        this.opacidad = opacidad;
    }
    
    /**
     * Crea una copia de esta configuración
     */
    public ConfiguracionPorOrientacion copia() {
        return new ConfiguracionPorOrientacion(
            posicionRelativa.x,
            posicionRelativa.y,
            escala,
            opacidad
        );
    }
    
    // Getters
    public Point getPosicionRelativa() {
        return new Point(posicionRelativa.x, posicionRelativa.y);
    }
    
    public double getEscala() {
        return escala;
    }
    
    public float getOpacidad() {
        return opacidad;
    }
    
    // Setters
    public void setPosicionRelativa(int x, int y) {
        this.posicionRelativa.x = Math.max(-100, Math.min(200, x));
        this.posicionRelativa.y = Math.max(-100, Math.min(200, y));
    }
    
    public void setEscala(double escala) {
        this.escala = Math.max(0.05, Math.min(10.0, escala));
    }
    
    public void setOpacidad(float opacidad) {
        this.opacidad = Math.max(0.0f, Math.min(1.0f, opacidad));
    }
}
