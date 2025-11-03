package com.amvisual.controller;

import com.amvisual.model.ImagenFlotante;
import com.amvisual.model.ProyectoMarcaAgua;
import com.amvisual.util.FileChooserUtils;
import com.amvisual.util.FileUtils;
import com.amvisual.util.ImageUtils;
import com.amvisual.view.PanelControles;
import com.amvisual.view.PanelInferior;
import com.amvisual.view.PanelPreview;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Controlador para la gestión de marcas de agua
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class MarcaAguaController {
    
    private ProyectoMarcaAgua proyecto;
    private PanelPreview panelPreview;
    private PanelControles panelControles;
    private PanelInferior panelInferior;
    private JFrame ventanaPrincipal;
    
    /**
     * Constructor
     * @param proyecto Proyecto de marcas de agua
     * @param panelPreview Panel de vista previa
     * @param panelControles Panel de controles
     * @param panelInferior Panel inferior
     * @param ventanaPrincipal Ventana principal
     */
    public MarcaAguaController(ProyectoMarcaAgua proyecto, 
                               PanelPreview panelPreview,
                               PanelControles panelControles,
                               PanelInferior panelInferior,
                               JFrame ventanaPrincipal) {
        this.proyecto = proyecto;
        this.panelPreview = panelPreview;
        this.panelControles = panelControles;
        this.panelInferior = panelInferior;
        this.ventanaPrincipal = ventanaPrincipal;
    }
    
    /**
     * Selecciona la carpeta de entrada
     */
    public void seleccionarCarpetaEntrada() {
        File carpeta = FileChooserUtils.elegirCarpeta(
            ventanaPrincipal, 
            "Seleccionar Carpeta de Entrada", 
            null
        );
        
        if (carpeta != null) {
            proyecto.setCarpetaEntrada(carpeta);
            
            List<File> imagenes = FileUtils.obtenerImagenesEnCarpeta(carpeta);
            proyecto.setListaImagenes(imagenes);
            
            if (!imagenes.isEmpty()) {
                panelPreview.cargarImagen(imagenes.get(0));
                panelInferior.actualizarEstado(
                    String.format("Carpeta: %s - %d imágenes encontradas", 
                    carpeta.getName(), imagenes.size())
                );
            } else {
                panelInferior.actualizarEstado("No se encontraron imágenes en la carpeta");
                JOptionPane.showMessageDialog(
                    ventanaPrincipal,
                    "No se encontraron imágenes válidas en la carpeta seleccionada.",
                    "Sin imágenes",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }
    
    /**
     * Selecciona la carpeta de salida
     */
    public void seleccionarCarpetaSalida() {
        File carpeta = FileChooserUtils.elegirCarpeta(
            ventanaPrincipal,
            "Seleccionar Carpeta de Salida",
            proyecto.getCarpetaEntrada()
        );
        
        if (carpeta != null) {
            proyecto.setCarpetaSalida(carpeta);
            panelInferior.actualizarEstado("Carpeta de salida: " + carpeta.getName());
        }
    }
    
    /**
     * Añade una marca de agua al proyecto
     */
    public void anadirMarcaAgua() {
        File archivo = FileChooserUtils.elegirImagenMarcaAgua(ventanaPrincipal);
        
        if (archivo != null) {
            BufferedImage imagen = ImageUtils.cargarImagen(archivo);
            
            if (imagen != null) {
                ImagenFlotante marca = new ImagenFlotante(imagen, archivo.getName());
                proyecto.addMarcaAgua(marca);
                panelControles.agregarMarcaALista(archivo.getName());
                panelInferior.actualizarEstado("Marca de agua añadida: " + archivo.getName());
                
                if (panelPreview.getImagenBase() != null) {
                    panelPreview.actualizarPreview();
                }
            } else {
                JOptionPane.showMessageDialog(
                    ventanaPrincipal,
                    "Error al cargar la imagen de marca de agua.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
