package com.amvisual.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel inferior con controles de procesamiento
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class PanelInferior extends JPanel {
    
    private JButton btnSeleccionarEntrada;
    private JButton btnSeleccionarSalida;
    private JButton btnProcesarImagenes;
    private JButton btnCancelarProceso;
    private JProgressBar barraProgreso;
    private JLabel labelEstado;
    
    // Callbacks
    private Consumer<Void> onSeleccionarEntrada;
    private Consumer<Void> onSeleccionarSalida;
    private Consumer<Void> onProcesarImagenes;
    private Consumer<Void> onCancelarProceso;
    
    /**
     * Constructor
     */
    public PanelInferior() {
        inicializarComponentes();
        configurarEventos();
    }
    
    /**
     * Inicializa los componentes del panel
     */
    private void inicializarComponentes() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        btnSeleccionarEntrada = new JButton("Seleccionar Carpeta de Entrada");
        btnSeleccionarSalida = new JButton("Seleccionar Carpeta de Salida");
        btnProcesarImagenes = new JButton("Procesar Imágenes");
        btnProcesarImagenes.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnCancelarProceso = new JButton("Cancelar");
        btnCancelarProceso.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelarProceso.setBackground(new Color(220, 50, 50));
        btnCancelarProceso.setForeground(Color.WHITE);
        btnCancelarProceso.setVisible(false); // Oculto por defecto
        
        panelBotones.add(btnSeleccionarEntrada);
        panelBotones.add(btnSeleccionarSalida);
        panelBotones.add(btnProcesarImagenes);
        panelBotones.add(btnCancelarProceso);
        
        // Panel de estado
        JPanel panelEstado = new JPanel(new BorderLayout(5, 0));
        
        labelEstado = new JLabel("Listo para comenzar");
        labelEstado.setBorder(new EmptyBorder(0, 5, 0, 5));
        
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setPreferredSize(new Dimension(200, 20));
        barraProgreso.setValue(0);
        
        JLabel labelFirma = new JLabel("Vides_2GA © 2025");
        labelFirma.setFont(new Font("Arial", Font.ITALIC, 10));
        labelFirma.setBorder(new EmptyBorder(0, 5, 0, 5));
        
        panelEstado.add(labelEstado, BorderLayout.WEST);
        panelEstado.add(barraProgreso, BorderLayout.CENTER);
        panelEstado.add(labelFirma, BorderLayout.EAST);
        
        add(panelBotones, BorderLayout.NORTH);
        add(panelEstado, BorderLayout.SOUTH);
    }
    
    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        btnSeleccionarEntrada.addActionListener(e -> {
            if (onSeleccionarEntrada != null) {
                onSeleccionarEntrada.accept(null);
            }
        });
        
        btnSeleccionarSalida.addActionListener(e -> {
            if (onSeleccionarSalida != null) {
                onSeleccionarSalida.accept(null);
            }
        });
        
        btnProcesarImagenes.addActionListener(e -> {
            if (onProcesarImagenes != null) {
                onProcesarImagenes.accept(null);
            }
        });
        
        btnCancelarProceso.addActionListener(e -> {
            if (onCancelarProceso != null) {
                onCancelarProceso.accept(null);
            }
        });
    }
    
    /**
     * Actualiza el mensaje de estado
     * @param mensaje Mensaje a mostrar
     */
    public void actualizarEstado(String mensaje) {
        labelEstado.setText(mensaje);
    }
    
    /**
     * Actualiza el progreso de la barra
     * @param progreso Progreso entre 0 y 100
     */
    public void actualizarProgreso(int progreso) {
        barraProgreso.setValue(progreso);
    }
    
    /**
     * Reinicia la barra de progreso
     */
    public void reiniciarProgreso() {
        barraProgreso.setValue(0);
    }
    
    /**
     * Alterna la visibilidad de los botones de procesamiento y cancelación
     * @param procesando true si el proceso está en marcha, false si no
     */
    public void alternarBotonesProcesamiento(boolean procesando) {
        btnProcesarImagenes.setVisible(!procesando);
        btnCancelarProceso.setVisible(procesando);
        btnSeleccionarEntrada.setEnabled(!procesando);
        btnSeleccionarSalida.setEnabled(!procesando);
    }
    
    // Setters para callbacks
    public void setOnSeleccionarEntrada(Consumer<Void> callback) {
        this.onSeleccionarEntrada = callback;
    }
    
    public void setOnSeleccionarSalida(Consumer<Void> callback) {
        this.onSeleccionarSalida = callback;
    }
    
    public void setOnProcesarImagenes(Consumer<Void> callback) {
        this.onProcesarImagenes = callback;
    }
    
    public void setOnCancelarProceso(Consumer<Void> callback) {
        this.onCancelarProceso = callback;
    }
}
