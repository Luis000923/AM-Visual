package com.amvisual;

import com.amvisual.controller.MarcaAguaController;
import com.amvisual.controller.ProcesadorImagenesController;
import com.amvisual.model.ProyectoMarcaAgua;
import com.amvisual.view.PanelControles;
import com.amvisual.view.PanelInferior;
import com.amvisual.view.PanelPreview;

import javax.swing.*;
import java.awt.*;

/**
 * AM Visual - Aplicación de Marcas de Agua
 * Clase principal que orquesta todos los componentes
 * 
 * @author Vides_2GA
 * @version 1.0
 * @date 2025
 */
public class AplicacionMarcaAgua extends JFrame {
    
    // Modelo
    private ProyectoMarcaAgua proyecto;
    
    // Vista
    private PanelPreview panelPreview;
    private PanelControles panelControles;
    private PanelInferior panelInferior;
    
    // Controladores
    private MarcaAguaController marcaAguaController;
    private ProcesadorImagenesController procesadorController;
    
    /**
     * Constructor de la aplicación
     */
    public AplicacionMarcaAgua() {
        super("AM Visual - Aplicación de Marcas de Agua");
        inicializarModelo();
        inicializarVista();
        inicializarControladores();
        configurarVentana();
        conectarEventos();
    }
    
    /**
     * Inicializa el modelo de datos
     */
    private void inicializarModelo() {
        proyecto = new ProyectoMarcaAgua();
    }
    
    /**
     * Inicializa los componentes de la vista
     */
    private void inicializarVista() {
        // Crear paneles
        panelPreview = new PanelPreview(proyecto);
        panelControles = new PanelControles(proyecto, panelPreview);
        panelInferior = new PanelInferior();
        
        // Panel principal contenedor
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel central (preview + controles)
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.add(panelPreview, BorderLayout.CENTER);
        panelCentral.add(panelControles, BorderLayout.EAST);
        
        // Ensamblar
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        
        add(panelPrincipal);
    }
    
    /**
     * Inicializa los controladores
     */
    private void inicializarControladores() {
        marcaAguaController = new MarcaAguaController(
            proyecto, 
            panelPreview, 
            panelControles, 
            panelInferior, 
            this
        );
        
        procesadorController = new ProcesadorImagenesController(
            proyecto,
            panelInferior,
            this
        );
    }
    
    /**
     * Conecta los eventos entre vista y controladores
     */
    private void conectarEventos() {
        // Eventos del panel de controles
        panelControles.setOnAnadirMarca(v -> marcaAguaController.anadirMarcaAgua());
        
        // Eventos del panel inferior
        panelInferior.setOnSeleccionarEntrada(v -> marcaAguaController.seleccionarCarpetaEntrada());
        panelInferior.setOnSeleccionarSalida(v -> marcaAguaController.seleccionarCarpetaSalida());
        panelInferior.setOnProcesarImagenes(v -> procesadorController.procesarImagenes());
        panelInferior.setOnCancelarProceso(v -> procesadorController.cancelarProcesamiento());
    }
    
    /**
     * Configura las propiedades de la ventana principal
     */
    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null); // Centrar en pantalla
    }
    
    /**
     * Método principal para ejecutar la aplicación
     */
    public static void main(String[] args) {
        // Configurar Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo establecer el Look and Feel del sistema");
            e.printStackTrace();
        }
        
        // Crear y mostrar la aplicación en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            AplicacionMarcaAgua app = new AplicacionMarcaAgua();
            app.setVisible(true);
        });
    }
}
