package com.amvisual.model;

import com.amvisual.util.ImageCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el proyecto completo de marcas de agua
 * Mantiene el estado de carpetas, imágenes y marcas de agua
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class ProyectoMarcaAgua {
    
    private File carpetaEntrada;
    private File carpetaSalida;
    private List<File> listaImagenes;
    private List<ImagenFlotante> listaMarcasAgua;
    private int indiceImagenActual;
    private int indiceMarcaSeleccionada;
    private ImageCache imageCache;
    
    /**
     * Constructor
     */
    public ProyectoMarcaAgua() {
        this.listaImagenes = new ArrayList<>();
        this.listaMarcasAgua = new ArrayList<>();
        this.indiceImagenActual = -1;
        this.indiceMarcaSeleccionada = -1;
        this.imageCache = new ImageCache();
    }
    
    /**
     * Establece la carpeta de entrada
     * @param carpeta Carpeta con imágenes a procesar
     */
    public void setCarpetaEntrada(File carpeta) {
        this.carpetaEntrada = carpeta;
    }
    
    /**
     * Establece la carpeta de salida
     * @param carpeta Carpeta donde se guardarán las imágenes procesadas
     */
    public void setCarpetaSalida(File carpeta) {
        this.carpetaSalida = carpeta;
    }
    
    /**
     * Establece la lista de imágenes
     * @param imagenes Lista de archivos de imagen
     */
    public void setListaImagenes(List<File> imagenes) {
        this.listaImagenes = imagenes;
        if (!imagenes.isEmpty()) {
            this.indiceImagenActual = 0;
        } else {
            this.indiceImagenActual = -1;
        }
    }

    /**
     * Obtiene la imagen actual que se está previsualizando.
     * @return El archivo de la imagen actual, o null si no hay imágenes.
     */
    public File getImagenActual() {
        if (indiceImagenActual != -1 && indiceImagenActual < listaImagenes.size()) {
            return listaImagenes.get(indiceImagenActual);
        }
        return null;
    }

    /**
     * Avanza a la siguiente imagen en la lista.
     * @return true si se pudo cambiar a la siguiente imagen, false si ya está en la última.
     */
    public boolean siguienteImagen() {
        if (indiceImagenActual < listaImagenes.size() - 1) {
            indiceImagenActual++;
            return true;
        }
        return false;
    }

    /**
     * Retrocede a la imagen anterior en la lista.
     * @return true si se pudo cambiar a la imagen anterior, false si ya está en la primera.
     */
    public boolean imagenAnterior() {
        if (indiceImagenActual > 0) {
            indiceImagenActual--;
            return true;
        }
        return false;
    }
    
    /**
     * Añade una marca de agua al proyecto
     * @param marca Marca de agua a añadir
     */
    public void addMarcaAgua(ImagenFlotante marca) {
        listaMarcasAgua.add(marca);
        indiceMarcaSeleccionada = listaMarcasAgua.size() - 1;
    }
    
    /**
     * Elimina una marca de agua del proyecto
     * @param indice Índice de la marca a eliminar
     */
    public void eliminarMarcaAgua(int indice) {
        if (indice >= 0 && indice < listaMarcasAgua.size()) {
            listaMarcasAgua.remove(indice);
            if (indiceMarcaSeleccionada >= listaMarcasAgua.size()) {
                indiceMarcaSeleccionada = listaMarcasAgua.size() - 1;
            }
        }
    }
    
    /**
     * Mueve una marca de agua en el orden Z
     * @param indice Índice actual
     * @param direccion Dirección del movimiento (-1 arriba, 1 abajo)
     * @return true si se pudo mover
     */
    public boolean moverMarcaEnOrden(int indice, int direccion) {
        int nuevoIndice = indice + direccion;
        
        if (nuevoIndice >= 0 && nuevoIndice < listaMarcasAgua.size()) {
            ImagenFlotante temp = listaMarcasAgua.get(indice);
            listaMarcasAgua.set(indice, listaMarcasAgua.get(nuevoIndice));
            listaMarcasAgua.set(nuevoIndice, temp);
            indiceMarcaSeleccionada = nuevoIndice;
            return true;
        }
        return false;
    }
    
    /**
     * Selecciona una marca de agua
     * @param indice Índice de la marca a seleccionar
     */
    public void seleccionarMarca(int indice) {
        if (indice >= 0 && indice < listaMarcasAgua.size()) {
            indiceMarcaSeleccionada = indice;
        }
    }
    
    /**
     * Obtiene la marca de agua seleccionada
     * @return Marca de agua seleccionada o null
     */
    public ImagenFlotante getMarcaSeleccionada() {
        if (indiceMarcaSeleccionada >= 0 && indiceMarcaSeleccionada < listaMarcasAgua.size()) {
            return listaMarcasAgua.get(indiceMarcaSeleccionada);
        }
        return null;
    }
    
    // Getters
    public File getCarpetaEntrada() { return carpetaEntrada; }
    public File getCarpetaSalida() { return carpetaSalida; }
    public List<File> getListaImagenes() { return listaImagenes; }
    public List<ImagenFlotante> getListaMarcasAgua() { return listaMarcasAgua; }
    public int getIndiceMarcaSeleccionada() { return indiceMarcaSeleccionada; }
    public ImageCache getImageCache() { return imageCache; }
    
    /**
     * Verifica si hay marcas de agua en el proyecto
     * @return true si hay al menos una marca
     */
    public boolean tieneMarcasAgua() {
        return !listaMarcasAgua.isEmpty();
    }
    
    /**
     * Verifica si hay imágenes en el proyecto
     * @return true si hay al menos una imagen
     */
    public boolean tieneImagenes() {
        return !listaImagenes.isEmpty();
    }
}
