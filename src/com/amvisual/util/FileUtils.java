// Este módulo de utilidad proporciona métodos para operaciones con archivos.
// Se utiliza para listar imágenes en carpetas y gestionar extensiones de archivo.
// Se conecta con:
// - MainViewController: Para cargar imágenes y crear carpetas de salida.
package com.amvisual.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    
    private static final String[] EXTENSIONES_IMAGENES = {".png", ".jpg", ".jpeg"};
    
    /**
     * Obtiene todas las imágenes de una carpeta
     * @param carpeta Carpeta a explorar
     * @return Lista de archivos de imagen
     */
    public static List<File> obtenerImagenesEnCarpeta(File carpeta) {
        List<File> imagenes = new ArrayList<>();
        
        if (carpeta == null || !carpeta.exists() || !carpeta.isDirectory()) {
            return imagenes;
        }
        
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && esImagen(archivo)) {
                    imagenes.add(archivo);
                }
            }
        }
        
        return imagenes;
    }
    
    /**
     * Verifica si un archivo es una imagen soportada
     * @param archivo Archivo a verificar
     * @return true si es una imagen válida
     */
    public static boolean esImagen(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        for (String extension : EXTENSIONES_IMAGENES) {
            if (nombre.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Crea una carpeta si no existe
     * @param carpeta Carpeta a crear
     * @return true si se creó o ya existía
     */
    public static boolean crearCarpetaSiNoExiste(File carpeta) {
        if (!carpeta.exists()) {
            return carpeta.mkdirs();
        }
        return true;
    }
    
    /**
     * Obtiene el nombre de archivo sin extensión
     * @param archivo Archivo
     * @return Nombre sin extensión
     */
    public static String getNombreSinExtension(File archivo) {
        String nombre = archivo.getName();
        int puntoIndex = nombre.lastIndexOf('.');
        if (puntoIndex > 0) {
            return nombre.substring(0, puntoIndex);
        }
        return nombre;
    }
    
    /**
     * Obtiene la extensión de un archivo
     * @param archivo Archivo
     * @return Extensión con punto (ej: ".png")
     */
    public static String getExtension(File archivo) {
        String nombre = archivo.getName();
        int puntoIndex = nombre.lastIndexOf('.');
        if (puntoIndex > 0) {
            return nombre.substring(puntoIndex);
        }
        return "";
    }
}
