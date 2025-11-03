package com.amvisual.model;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Interfaz que define el contrato para cualquier tipo de marca de agua
 * (imagen o texto)
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public interface Watermark {
    
    /**
     * Dibuja la marca de agua en el contexto gráfico
     * @param g2d Contexto gráfico donde se dibujará
     * @param panelWidth Ancho del panel
     * @param panelHeight Alto del panel
     */
    void draw(Graphics2D g2d, int panelWidth, int panelHeight);
    
    /**
     * Devuelve los límites de la marca de agua en el panel
     * Usado para detección de clics
     * @param panelWidth Ancho del panel
     * @param panelHeight Alto del panel
     * @return Rectangle con los límites
     */
    Rectangle getBounds(int panelWidth, int panelHeight);
    
    /**
     * Actualiza la representación procesada de la marca de agua
     * Pre-renderiza la marca con opacidad y escala aplicadas
     */
    void updateProcessedRepresentation();
    
    /**
     * Obtiene la posición X relativa (0.0 - 1.0)
     * @return Posición X relativa
     */
    double getXRelativo();
    
    /**
     * Establece la posición X relativa (0.0 - 1.0)
     * @param xRelativo Nueva posición X
     */
    void setXRelativo(double xRelativo);
    
    /**
     * Obtiene la posición Y relativa (0.0 - 1.0)
     * @return Posición Y relativa
     */
    double getYRelativo();
    
    /**
     * Establece la posición Y relativa (0.0 - 1.0)
     * @param yRelativo Nueva posición Y
     */
    void setYRelativo(double yRelativo);
    
    /**
     * Obtiene la escala de la marca de agua
     * @return Escala actual
     */
    double getEscala();
    
    /**
     * Establece la escala de la marca de agua
     * @param escala Nueva escala
     */
    void setEscala(double escala);
    
    /**
     * Obtiene la opacidad de la marca de agua (0.0 - 1.0)
     * @return Opacidad actual
     */
    float getOpacidad();
    
    /**
     * Establece la opacidad de la marca de agua (0.0 - 1.0)
     * @param opacidad Nueva opacidad
     */
    void setOpacidad(float opacidad);
    
    /**
     * Obtiene el nombre para mostrar en el JComboBox
     * @return Nombre descriptivo
     */
    String getDisplayName();
}
