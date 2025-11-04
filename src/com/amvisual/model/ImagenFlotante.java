package com.amvisual.model;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Representa una imagen de marca de agua flotante
 * Maneja su posición, escala y opacidad
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class ImagenFlotante {
    
    private BufferedImage imagenOriginal;
    private BufferedImage imagenActual;
    private Point posicionRelativa; // Posición relativa (0.0 - 1.0)
    private double escala;
    private float opacidad;
    private boolean enMovimiento;
    private Point offset;
    private String nombreArchivo;
    
    /**
     * Constructor
     * @param imagen Imagen original de la marca de agua
     * @param nombreArchivo Nombre del archivo de la imagen
     */
    public ImagenFlotante(BufferedImage imagen, String nombreArchivo) {
        this.imagenOriginal = imagen;
        this.nombreArchivo = nombreArchivo;
        this.posicionRelativa = new Point(50, 50); // Centro en porcentaje
        this.escala = 0.5;
        this.opacidad = 0.7f;
        this.enMovimiento = false;
        this.offset = new Point(0, 0);
        actualizarImagen();
    }
    
    /**
     * Actualiza la imagen aplicando escala
     */
    public void actualizarImagen() {
        int nuevoAncho = (int) (imagenOriginal.getWidth() * escala);
        int nuevoAlto = (int) (imagenOriginal.getHeight() * escala);
        
        imagenActual = new BufferedImage(nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagenActual.createGraphics();
        g2d.drawImage(imagenOriginal, 0, 0, nuevoAncho, nuevoAlto, null);
        g2d.dispose();
    }
    
    /**
     * Calcula la posición absoluta en píxeles basándose en la imagen base
     * @param anchoBase Ancho de la imagen base
     * @param altoBase Alto de la imagen base
     * @return Punto con coordenadas absolutas
     */
    public Point calcularPosicionAbsoluta(int anchoBase, int altoBase) {
        int xCentro = (int) (anchoBase * posicionRelativa.x / 100.0);
        int yCentro = (int) (altoBase * posicionRelativa.y / 100.0);
        
        int x = xCentro - (imagenActual.getWidth() / 2);
        int y = yCentro - (imagenActual.getHeight() / 2);
        
        return new Point(x, y);
    }
    
    /**
     * Dibuja la marca de agua sobre una imagen base
     * @param imagenBase Imagen sobre la que se dibujará la marca
     * @return Nueva imagen con la marca de agua aplicada
     */
    public BufferedImage dibujar(BufferedImage imagenBase) {
        BufferedImage resultado = new BufferedImage(
            imagenBase.getWidth(), 
            imagenBase.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = resultado.createGraphics();
        g2d.drawImage(imagenBase, 0, 0, null);
        
        Point posicion = calcularPosicionAbsoluta(imagenBase.getWidth(), imagenBase.getHeight());
        
        // Aplicar opacidad
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
        g2d.drawImage(imagenActual, posicion.x, posicion.y, null);
        g2d.dispose();
        
        return resultado;
    }
    
    /**
     * Inicia el movimiento de la marca de agua
     * @param x Coordenada x del clic
     * @param y Coordenada y del clic
     * @param anchoBase Ancho de la imagen base
     * @param altoBase Alto de la imagen base
     * @return true si el clic está dentro de la marca
     */
    public boolean iniciarMovimiento(int x, int y, int anchoBase, int altoBase) {
        Point posicion = calcularPosicionAbsoluta(anchoBase, altoBase);
        
        if (x >= posicion.x && x <= posicion.x + imagenActual.getWidth() &&
            y >= posicion.y && y <= posicion.y + imagenActual.getHeight()) {
            
            enMovimiento = true;
            offset.x = x - posicion.x;
            offset.y = y - posicion.y;
            return true;
        }
        return false;
    }
    
    /**
     * Mueve la marca de agua a una nueva posición
     * @param x Nueva coordenada x
     * @param y Nueva coordenada y
     * @param anchoBase Ancho de la imagen base
     * @param altoBase Alto de la imagen base
     */
    public void mover(int x, int y, int anchoBase, int altoBase) {
        if (!enMovimiento) return;
        
        // Calcular la nueva posición de la esquina superior izquierda
        int nuevoX = x - offset.x;
        int nuevoY = y - offset.y;
        
        // Calcular el centro de la marca de agua
        int xCentro = nuevoX + (imagenActual.getWidth() / 2);
        int yCentro = nuevoY + (imagenActual.getHeight() / 2);
        
        // Convertir a porcentaje (limitar entre 0 y 100)
        double porcentajeX = Math.max(0, Math.min(100, (xCentro * 100.0) / anchoBase));
        double porcentajeY = Math.max(0, Math.min(100, (yCentro * 100.0) / altoBase));
        
        posicionRelativa.x = (int) porcentajeX;
        posicionRelativa.y = (int) porcentajeY;
    }
    
    /**
     * Finaliza el movimiento de la marca de agua
     */
    public void finalizarMovimiento() {
        enMovimiento = false;
    }
    
    /**
     * Ajusta la escala de la marca de agua
     * @param factor Factor de escala (1.1 para aumentar, 0.9 para disminuir)
     */
    public void ajustarEscala(double factor) {
        escala = Math.max(0.05, Math.min(10.0, escala * factor));
        actualizarImagen();
    }
    
    /**
     * Establece la escala directamente
     * @param nuevaEscala Nueva escala (0.05 a 10.0)
     */
    public void setEscala(double nuevaEscala) {
        escala = Math.max(0.05, Math.min(10.0, nuevaEscala));
        actualizarImagen();
    }
    
    /**
     * Establece la opacidad de la marca de agua
     * @param opacidad Valor entre 0.0 y 1.0
     */
    public void setOpacidad(float opacidad) {
        this.opacidad = Math.max(0.0f, Math.min(1.0f, opacidad));
    }
    
    /**
     * Calcula y aplica una escala recomendada basada en el tamaño de la imagen base.
     * La marca de agua ocupará aproximadamente un porcentaje del ancho de la imagen.
     * 
     * @param anchoImagenBase Ancho de la imagen sobre la que se aplicará la marca
     * @param porcentajeAncho Porcentaje del ancho que debe ocupar la marca (ej: 15.0 para 15%)
     */
    public void ajustarEscalaSegunImagen(int anchoImagenBase, double porcentajeAncho) {
        if (imagenOriginal == null || anchoImagenBase <= 0) return;
        
        // Calcular el ancho deseado para la marca de agua
        double anchoDeseado = anchoImagenBase * (porcentajeAncho / 100.0);
        
        // Calcular la escala necesaria
        double nuevaEscala = anchoDeseado / imagenOriginal.getWidth();
        
        // Aplicar la escala con límites
        setEscala(nuevaEscala);
    }
    
    // Getters
    public BufferedImage getImagenOriginal() { return imagenOriginal; }
    public BufferedImage getImagenActual() { return imagenActual; }
    public double getEscala() { return escala; }
    public float getOpacidad() { return opacidad; }
    public String getNombreArchivo() { return nombreArchivo; }
    public boolean isEnMovimiento() { return enMovimiento; }
    
    public Point getPosicionRelativa() {
        return posicionRelativa;
    }

    public void setPosicionRelativa(double percentX, double percentY) {
        this.posicionRelativa.x = (int) Math.max(0, Math.min(100, percentX));
        this.posicionRelativa.y = (int) Math.max(0, Math.min(100, percentY));
    }

    @Override
    public String toString() {
        return nombreArchivo;
    }
}
