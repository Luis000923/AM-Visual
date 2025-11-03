package com.amvisual.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Implementación de Watermark para marcas de agua de texto
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class TextWatermark implements Watermark {
    
    private String text;
    private Font font;
    private Color color;
    private double xRelativo;
    private double yRelativo;
    private double escala;
    private float opacidad;
    
    /**
     * Constructor con valores predeterminados
     * @param text Texto de la marca de agua
     */
    public TextWatermark(String text) {
        this.text = text;
        this.font = new Font("Arial", Font.BOLD, 48);
        this.color = Color.WHITE;
        this.xRelativo = 0.5;
        this.yRelativo = 0.5;
        this.escala = 1.0;
        this.opacidad = 0.7f;
    }
    
    /**
     * Constructor completo
     * @param text Texto
     * @param font Fuente
     * @param color Color
     */
    public TextWatermark(String text, Font font, Color color) {
        this.text = text;
        this.font = font;
        this.color = color;
        this.xRelativo = 0.5;
        this.yRelativo = 0.5;
        this.escala = 1.0;
        this.opacidad = 0.7f;
    }
    
    @Override
    public void draw(Graphics2D g2d, int panelWidth, int panelHeight) {
        if (text == null || text.isEmpty()) return;
        
        // Guardar estado original
        AffineTransform originalTransform = g2d.getTransform();
        Composite originalComposite = g2d.getComposite();
        Font originalFont = g2d.getFont();
        Color originalColor = g2d.getColor();
        
        // Calcular posición
        int x = (int) (panelWidth * xRelativo);
        int y = (int) (panelHeight * yRelativo);
        
        // Configurar fuente y color
        g2d.setFont(font);
        g2d.setColor(color);
        
        // Aplicar opacidad
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
        
        // Crear transformación de escala
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.scale(escala, escala);
        
        // Obtener métricas del texto
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // Centrar el texto
        at.translate(-textWidth / 2.0, textHeight / 4.0);
        
        g2d.setTransform(at);
        g2d.drawString(text, 0, 0);
        
        // Restaurar estado original
        g2d.setTransform(originalTransform);
        g2d.setComposite(originalComposite);
        g2d.setFont(originalFont);
        g2d.setColor(originalColor);
    }
    
    @Override
    public Rectangle getBounds(int panelWidth, int panelHeight) {
        if (text == null || text.isEmpty()) return new Rectangle(0, 0, 0, 0);
        
        // Crear una imagen temporal para obtener FontMetrics
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tempImage.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        
        int textWidth = (int) (fm.stringWidth(text) * escala);
        int textHeight = (int) (fm.getHeight() * escala);
        
        g2d.dispose();
        
        int x = (int) (panelWidth * xRelativo) - (textWidth / 2);
        int y = (int) (panelHeight * yRelativo) - (textHeight / 2);
        
        return new Rectangle(x, y, textWidth, textHeight);
    }
    
    @Override
    public void updateProcessedRepresentation() {
        // Para texto no necesitamos pre-renderizar
        // La representación se calcula en tiempo real en draw()
    }
    
    // Getters y Setters
    @Override
    public double getXRelativo() {
        return xRelativo;
    }
    
    @Override
    public void setXRelativo(double xRelativo) {
        this.xRelativo = Math.max(0.0, Math.min(1.0, xRelativo));
    }
    
    @Override
    public double getYRelativo() {
        return yRelativo;
    }
    
    @Override
    public void setYRelativo(double yRelativo) {
        this.yRelativo = Math.max(0.0, Math.min(1.0, yRelativo));
    }
    
    @Override
    public double getEscala() {
        return escala;
    }
    
    @Override
    public void setEscala(double escala) {
        this.escala = Math.max(0.1, Math.min(5.0, escala));
    }
    
    @Override
    public float getOpacidad() {
        return opacidad;
    }
    
    @Override
    public void setOpacidad(float opacidad) {
        this.opacidad = Math.max(0.0f, Math.min(1.0f, opacidad));
    }
    
    @Override
    public String getDisplayName() {
        return "[TXT] " + text.substring(0, Math.min(text.length(), 20)) + 
               (text.length() > 20 ? "..." : "");
    }
    
    // Métodos específicos de TextWatermark
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public Font getFont() {
        return font;
    }
    
    public void setFont(Font font) {
        this.font = font;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
