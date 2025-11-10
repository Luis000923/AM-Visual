// Esta utilidad proporciona métodos para operaciones básicas con imágenes, como cargar,
// guardar y redimensionar.
// Se conecta con:
// - MainViewController: Para cargar y procesar imágenes.
package com.amvisual.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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
     * Carga una imagen desde un archivo, redimensionándola durante la carga para ahorrar memoria.
     * Se utiliza un método de dos pasos: primero se submuestrea la imagen al tamaño más cercano
     * posible y luego se escala al tamaño exacto para mayor calidad.
     *
     * @param archivo Archivo de imagen.
     * @param targetWidth Ancho exacto deseado para la imagen final.
     * @return BufferedImage redimensionada o null si hay error.
     */
    public static BufferedImage cargarImagenRedimensionada(File archivo, int targetWidth) {
        try (ImageInputStream input = ImageIO.createImageInputStream(archivo)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return null;
            }

            ImageReader reader = readers.next();
            reader.setInput(input);

            int originalWidth = reader.getWidth(0);
            int originalHeight = reader.getHeight(0);

            // Si la imagen ya es más pequeña que el objetivo, cargarla directamente.
            if (originalWidth <= targetWidth) {
                return reader.read(0);
            }
            
            // 1. Submuestreo: Cargar una versión más pequeña pero mayor que el objetivo
            int subsampling = Math.max(1, (int) Math.floor((double) originalWidth / (targetWidth * 2)));
            
            javax.imageio.ImageReadParam param = reader.getDefaultReadParam();
            param.setSourceSubsampling(subsampling, subsampling, 0, 0);
            
            BufferedImage subsampledImage = reader.read(0, param);

            // 2. Escalado de alta calidad: Redimensionar la imagen submuestreada al tamaño final
            double aspectRatio = (double) originalHeight / originalWidth;
            int targetHeight = (int) (targetWidth * aspectRatio);

            BufferedImage finalImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = finalImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(subsampledImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            return finalImage;

        } catch (IOException e) {
            System.err.println("Error al cargar imagen redimensionada: " + archivo.getName());
            return null;
        }
    }

    /**
     * Obtiene las dimensiones de una imagen sin cargarla completamente en memoria.
     * @param archivo El archivo de imagen.
     * @return Un objeto Dimension con el ancho y alto, o null si ocurre un error.
     */
    public static Dimension obtenerDimensiones(File archivo) {
        try (ImageInputStream in = ImageIO.createImageInputStream(archivo)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException e) {
            System.err.println("Error al obtener dimensiones de: " + archivo.getName());
        }
        return null;
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
