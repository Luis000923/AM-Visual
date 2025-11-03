package com.amvisual.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caché LRU (Least Recently Used) para imágenes
 * Almacena las imágenes cargadas para evitar recargarlas constantemente
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class ImageCache {
    
    private static final int MAX_CACHE_SIZE = 50; // Máximo de imágenes en caché
    private final Map<String, BufferedImage> cache;
    
    /**
     * Constructor
     */
    public ImageCache() {
        // LinkedHashMap con política LRU
        this.cache = new LinkedHashMap<String, BufferedImage>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
    }
    
    /**
     * Obtiene una imagen del caché
     * @param archivo Archivo de la imagen
     * @return La imagen si está en caché, null si no
     */
    public synchronized BufferedImage get(File archivo) {
        if (archivo == null) return null;
        return cache.get(archivo.getAbsolutePath());
    }
    
    /**
     * Añade una imagen al caché
     * @param archivo Archivo de la imagen
     * @param imagen Imagen a almacenar
     */
    public synchronized void put(File archivo, BufferedImage imagen) {
        if (archivo == null || imagen == null) return;
        cache.put(archivo.getAbsolutePath(), imagen);
    }
    
    /**
     * Verifica si una imagen está en el caché
     * @param archivo Archivo de la imagen
     * @return true si está en caché, false si no
     */
    public synchronized boolean contains(File archivo) {
        if (archivo == null) return false;
        return cache.containsKey(archivo.getAbsolutePath());
    }
    
    /**
     * Limpia el caché
     */
    public synchronized void clear() {
        cache.clear();
    }
    
    /**
     * Obtiene el tamaño actual del caché
     * @return Número de imágenes en caché
     */
    public synchronized int size() {
        return cache.size();
    }
}
