package com.amvisual.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utilidades para manejo de imágenes
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class ImageUtils {
    
    /**
     * Carga una imagen desde un archivo
     * @param archivo Archivo de imagen
     * @return BufferedImage o null si hay error
     */
    public static BufferedImage cargarImagen(File archivo) {
        try {
            return ImageIO.read(archivo);
        } catch (IOException e) {
            System.err.println("Error al cargar imagen: " + archivo.getName());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Guarda una imagen en un archivo
     * @param imagen Imagen a guardar
     * @param archivo Archivo destino
     * @param formato Formato (png, jpg, jpeg)
     * @return true si se guardó correctamente
     */
    public static boolean guardarImagen(BufferedImage imagen, File archivo, String formato) {
        try {
            return ImageIO.write(imagen, formato, archivo);
        } catch (IOException e) {
            System.err.println("Error al guardar imagen: " + archivo.getName());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Convierte una imagen a RGB (sin canal alpha)
     * @param imagen Imagen original
     * @return Imagen en RGB
     */
    public static BufferedImage convertirARGB(BufferedImage imagen) {
        BufferedImage nuevaImagen = new BufferedImage(
            imagen.getWidth(),
            imagen.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );
        
        nuevaImagen.createGraphics().drawImage(imagen, 0, 0, null);
        return nuevaImagen;
    }
    
    /**
     * Redimensiona una imagen manteniendo la proporción
     * @param imagen Imagen original
     * @param maxAncho Ancho máximo
     * @param maxAlto Alto máximo
     * @return Imagen redimensionada
     */
    public static BufferedImage redimensionarParaPreview(BufferedImage imagen, int maxAncho, int maxAlto) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        
        double escalaAncho = (double) maxAncho / ancho;
        double escalaAlto = (double) maxAlto / alto;
        double escala = Math.min(escalaAncho, escalaAlto);
        
        if (escala >= 1.0) {
            return imagen; // No redimensionar si ya es más pequeña
        }
        
        int nuevoAncho = (int) (ancho * escala);
        int nuevoAlto = (int) (alto * escala);
        
        BufferedImage imagenRedimensionada = new BufferedImage(
            nuevoAncho,
            nuevoAlto,
            BufferedImage.TYPE_INT_ARGB
        );
        
        imagenRedimensionada.createGraphics().drawImage(
            imagen,
            0, 0, nuevoAncho, nuevoAlto,
            null
        );
        
        return imagenRedimensionada;
    }
}
