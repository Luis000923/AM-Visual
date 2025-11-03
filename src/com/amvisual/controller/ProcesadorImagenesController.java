package com.amvisual.controller;

import com.amvisual.model.ImagenFlotante;
import com.amvisual.model.ProyectoMarcaAgua;
import com.amvisual.util.FileUtils;
import com.amvisual.util.ImageUtils;
import com.amvisual.util.WatermarkSizingUtil;
import com.amvisual.view.PanelInferior;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Controlador para el procesamiento de imágenes
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class ProcesadorImagenesController {
    
    private ProyectoMarcaAgua proyecto;
    private PanelInferior panelInferior;
    private JFrame ventanaPrincipal;
    private SwingWorker<Void, Integer> workerProcesamiento;
    
    /**
     * Constructor
     * @param proyecto Proyecto de marcas de agua
     * @param panelInferior Panel inferior
     * @param ventanaPrincipal Ventana principal
     */
    public ProcesadorImagenesController(ProyectoMarcaAgua proyecto,
                                        PanelInferior panelInferior,
                                        JFrame ventanaPrincipal) {
        this.proyecto = proyecto;
        this.panelInferior = panelInferior;
        this.ventanaPrincipal = ventanaPrincipal;
    }
    
    /**
     * Procesa todas las imágenes del proyecto
     */
    public void procesarImagenes() {
        // Validaciones
        if (!proyecto.tieneMarcasAgua()) {
            JOptionPane.showMessageDialog(
                ventanaPrincipal,
                "Debe añadir al menos una marca de agua antes de procesar.",
                "Sin marcas de agua",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        if (!proyecto.tieneImagenes()) {
            JOptionPane.showMessageDialog(
                ventanaPrincipal,
                "No hay imágenes para procesar. Seleccione una carpeta de entrada.",
                "Sin imágenes",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Configurar carpeta de salida
        File carpetaSalida = proyecto.getCarpetaSalida();
        if (carpetaSalida == null) {
            carpetaSalida = new File(proyecto.getCarpetaEntrada(), "imagenes_con_marca");
            proyecto.setCarpetaSalida(carpetaSalida);
        }
        
        if (!FileUtils.crearCarpetaSiNoExiste(carpetaSalida)) {
            JOptionPane.showMessageDialog(
                ventanaPrincipal,
                "No se pudo crear la carpeta de salida.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Variable final para usar en la clase interna
        final File carpetaSalidaFinal = carpetaSalida;
        
        // Procesar en un hilo separado para no bloquear la UI
        workerProcesamiento = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<File> imagenes = proyecto.getListaImagenes();
                int total = imagenes.size();
                
                for (int i = 0; i < total; i++) {
                    // Verificar si se ha cancelado la tarea
                    if (isCancelled()) {
                        break;
                    }
                    
                    File archivoImagen = imagenes.get(i);
                    
                    try {
                        // Cargar imagen desde el caché si es posible
                        BufferedImage imagen = proyecto.getImageCache().get(archivoImagen);
                        if (imagen == null) {
                            imagen = ImageUtils.cargarImagen(archivoImagen);
                            if (imagen != null) {
                                proyecto.getImageCache().put(archivoImagen, imagen);
                            }
                        }

                        if (imagen == null) {
                            System.err.println("Error al cargar: " + archivoImagen.getName());
                            continue;
                        }
                        
                        // Aplicar todas las marcas de agua
                        for (ImagenFlotante marca : proyecto.getListaMarcasAgua()) {
                            imagen = marca.dibujar(imagen);
                        }
                        
                        // Convertir a RGB para guardar como JPG
                        if (FileUtils.getExtension(archivoImagen).toLowerCase().equals(".jpg") ||
                            FileUtils.getExtension(archivoImagen).toLowerCase().equals(".jpeg")) {
                            imagen = ImageUtils.convertirARGB(imagen);
                        }
                        
                        // Guardar imagen procesada
                        File archivoSalida = new File(carpetaSalidaFinal, archivoImagen.getName());
                        String formato = FileUtils.getExtension(archivoImagen).substring(1);
                        ImageUtils.guardarImagen(imagen, archivoSalida, formato);
                        
                        // Publicar progreso
                        int progreso = (int) (((i + 1) * 100.0) / total);
                        publish(progreso);
                        
                    } catch (Exception e) {
                        System.err.println("Error procesando: " + archivoImagen.getName());
                        e.printStackTrace();
                    }
                }
                
                return null;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                // Actualizar la barra de progreso con el último valor
                if (!chunks.isEmpty()) {
                    int ultimoProgreso = chunks.get(chunks.size() - 1);
                    panelInferior.actualizarProgreso(ultimoProgreso);
                }
            }
            
            @Override
            protected void done() {
                // Asegurarse de que los botones se restauran
                panelInferior.alternarBotonesProcesamiento(false);
                
                if (isCancelled()) {
                    panelInferior.actualizarEstado("Procesamiento cancelado por el usuario.");
                    panelInferior.reiniciarProgreso();
                } else {
                    panelInferior.actualizarProgreso(100);
                    panelInferior.actualizarEstado(
                        String.format("Procesamiento completado: %d imágenes procesadas", 
                        proyecto.getListaImagenes().size())
                    );
                    
                    JOptionPane.showMessageDialog(
                        ventanaPrincipal,
                        String.format(
                            "Se han procesado %d imágenes.\nLas imágenes se guardaron en:\n%s",
                            proyecto.getListaImagenes().size(),
                            carpetaSalidaFinal.getAbsolutePath()
                        ),
                        "Proceso Completado",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        };
        
        panelInferior.alternarBotonesProcesamiento(true);
        panelInferior.reiniciarProgreso();
        panelInferior.actualizarEstado("Procesando imágenes...");
        workerProcesamiento.execute();
    }
    
    /**
     * Cancela el proceso de imágenes en curso
     */
    public void cancelarProcesamiento() {
        if (workerProcesamiento != null && !workerProcesamiento.isDone()) {
            workerProcesamiento.cancel(true);
        }
    }
}
