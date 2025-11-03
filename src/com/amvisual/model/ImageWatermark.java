package com.amvisual.model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Implementación de Watermark para marcas de agua basadas en imágenes
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class ImageWatermark implements Watermark {
    
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private String name;
    private double xRelativo;
    private double yRelativo;
    private double escala;
    private float opacidad;
    
    /**
     * Constructor
     * @param image Imagen original
     * @param name Nombre de la marca de agua
     */
    public ImageWatermark(BufferedImage image, String name) {
        this.originalImage = image;
        this.name = name;
        this.xRelativo = 0.5;  // Centro
        this.yRelativo = 0.5;  // Centro
        this.escala = 0.3;
        this.opacidad = 0.7f;
        updateProcessedRepresentation();
    }
    
    @Override
    public void draw(Graphics2D g2d, int panelWidth, int panelHeight) {
        if (processedImage == null) return;
        
        // Calcular posición absoluta
        int x = (int) (panelWidth * xRelativo) - (processedImage.getWidth() / 2);
        int y = (int) (panelHeight * yRelativo) - (processedImage.getHeight() / 2);
        
        // Dibujar imagen procesada
        g2d.drawImage(processedImage, x, y, null);
    }
    
    @Override
    public Rectangle getBounds(int panelWidth, int panelHeight) {
        if (processedImage == null) return new Rectangle(0, 0, 0, 0);
        
        int x = (int) (panelWidth * xRelativo) - (processedImage.getWidth() / 2);
        int y = (int) (panelHeight * yRelativo) - (processedImage.getHeight() / 2);
        
        return new Rectangle(x, y, processedImage.getWidth(), processedImage.getHeight());
    }
    
    @Override
    public void updateProcessedRepresentation() {
        if (originalImage == null) return;
        
        // 1. Calcular nuevas dimensiones con escala
        int nuevoAncho = (int) (originalImage.getWidth() * escala);
        int nuevoAlto = (int) (originalImage.getHeight() * escala);
        
        if (nuevoAncho <= 0 || nuevoAlto <= 0) return;
        
        // 2. Redimensionar con alta calidad usando AffineTransform
        AffineTransform at = new AffineTransform();
        at.scale(escala, escala);
        AffineTransformOp scaleOp = new AffineTransformOp(
            at, 
            AffineTransformOp.TYPE_BICUBIC
        );
        
        BufferedImage scaledImage = new BufferedImage(
            nuevoAncho, 
            nuevoAlto, 
            BufferedImage.TYPE_INT_ARGB
        );
        scaleOp.filter(originalImage, scaledImage);
        
        // 3. Aplicar opacidad
        processedImage = new BufferedImage(
            nuevoAncho, 
            nuevoAlto, 
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = processedImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
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
        this.escala = Math.max(0.1, Math.min(2.0, escala));
        updateProcessedRepresentation();
    }
    
    @Override
    public float getOpacidad() {
        return opacidad;
    }
    
    @Override
    public void setOpacidad(float opacidad) {
        this.opacidad = Math.max(0.0f, Math.min(1.0f, opacidad));
        updateProcessedRepresentation();
    }
    
    @Override
    public String getDisplayName() {
        return "[IMG] " + name;
    }
    
    public BufferedImage getOriginalImage() {
        return originalImage;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
