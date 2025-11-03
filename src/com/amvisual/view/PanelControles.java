package com.amvisual.view;

import com.amvisual.model.ImagenFlotante;
import com.amvisual.model.ProyectoMarcaAgua;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Panel de controles para ajustar marcas de agua
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class PanelControles extends JPanel {
    
    private ProyectoMarcaAgua proyecto;
    private PanelPreview panelPreview;
    
    // Componentes
    private JButton btnAnadirMarca;
    private JComboBox<String> comboMarcasAgua;
    private DefaultComboBoxModel<String> modeloComboMarcas;
    private JSlider sliderOpacidad;
    private JLabel labelOpacidad;
    private JLabel labelEscala;
    private JButton btnEscalaMas;
    private JButton btnEscalaMenos;
    private JButton btnAutoEscala;
    private JButton btnEliminar;
    private JButton btnSubir;
    private JButton btnBajar;
    private JButton btnImagenAnterior;
    private JButton btnImagenSiguiente;
    
    // Callbacks
    private Consumer<Void> onAnadirMarca;
    
    /**
     * Constructor
     * @param proyecto Proyecto de marcas de agua
     * @param panelPreview Panel de vista previa
     */
    public PanelControles(ProyectoMarcaAgua proyecto, PanelPreview panelPreview) {
        this.proyecto = proyecto;
        this.panelPreview = panelPreview;
        inicializarComponentes();
        configurarEventos();
    }
    
    /**
     * Inicializa los componentes del panel
     */
    private void inicializarComponentes() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(280, 0));
        
        // Título
        JLabel lblTitulo = new JLabel("Configuración");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblTitulo);
        add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Botón añadir marca
        btnAnadirMarca = new JButton("Añadir Marca de Agua");
        btnAnadirMarca.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAnadirMarca.setMaximumSize(new Dimension(250, 35));
        add(btnAnadirMarca);
        add(Box.createRigidArea(new Dimension(0, 10)));
        
        crearPanelNavegacion();
        crearPanelListaMarcas();
        crearPanelAjustes();
    }

    private void crearPanelNavegacion() {
        JPanel panelNavegacion = new JPanel();
        panelNavegacion.setLayout(new BoxLayout(panelNavegacion, BoxLayout.Y_AXIS));
        panelNavegacion.setBorder(BorderFactory.createTitledBorder("Navegación"));
        panelNavegacion.setMaximumSize(new Dimension(270, 80));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        btnImagenAnterior = new JButton("Anterior");
        btnImagenSiguiente = new JButton("Siguiente");
        panelBotones.add(btnImagenAnterior);
        panelBotones.add(btnImagenSiguiente);

        panelNavegacion.add(panelBotones);
        add(panelNavegacion);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Crea el panel de lista de marcas de agua
     */
    private void crearPanelListaMarcas() {
        JPanel panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBorder(BorderFactory.createTitledBorder("Marcas de Agua"));
        panelLista.setMaximumSize(new Dimension(270, 150));
        
        modeloComboMarcas = new DefaultComboBoxModel<>();
        comboMarcasAgua = new JComboBox<>(modeloComboMarcas);
        comboMarcasAgua.setMaximumSize(new Dimension(250, 30));
        panelLista.add(comboMarcasAgua);
        panelLista.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        btnEliminar = new JButton("Eliminar");
        btnEliminar.setPreferredSize(new Dimension(90, 30));
        
        btnSubir = new JButton("▲");
        btnSubir.setPreferredSize(new Dimension(50, 30));
        
        btnBajar = new JButton("▼");
        btnBajar.setPreferredSize(new Dimension(50, 30));
        
        panelBotones.add(btnEliminar);
        panelBotones.add(btnSubir);
        panelBotones.add(btnBajar);
        
        panelLista.add(panelBotones);
        add(panelLista);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Crea el panel de ajustes
     */
    private void crearPanelAjustes() {
        JPanel panelAjustes = new JPanel();
        panelAjustes.setLayout(new BoxLayout(panelAjustes, BoxLayout.Y_AXIS));
        panelAjustes.setBorder(BorderFactory.createTitledBorder("Ajustes de Marca de Agua"));
        panelAjustes.setMaximumSize(new Dimension(270, 180));
        
        // Opacidad
        JLabel lblOpacidad = new JLabel("Opacidad:");
        lblOpacidad.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAjustes.add(lblOpacidad);
        panelAjustes.add(Box.createRigidArea(new Dimension(0, 5)));
        
        sliderOpacidad = new JSlider(JSlider.HORIZONTAL, 0, 100, 70);
        sliderOpacidad.setMajorTickSpacing(25);
        sliderOpacidad.setMinorTickSpacing(5);
        sliderOpacidad.setPaintTicks(true);
        sliderOpacidad.setMaximumSize(new Dimension(250, 50));
        panelAjustes.add(sliderOpacidad);
        
        labelOpacidad = new JLabel("70%");
        labelOpacidad.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelAjustes.add(labelOpacidad);
        panelAjustes.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Escala
        JLabel lblTamano = new JLabel("Tamaño:");
        lblTamano.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelAjustes.add(lblTamano);
        panelAjustes.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel panelEscala = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelEscala.setMaximumSize(new Dimension(270, 40));
        
        btnEscalaMenos = new JButton("-");
        btnEscalaMenos.setPreferredSize(new Dimension(50, 30));
        
        labelEscala = new JLabel("100%");
        labelEscala.setPreferredSize(new Dimension(60, 30));
        labelEscala.setHorizontalAlignment(SwingConstants.CENTER);
        
        btnEscalaMas = new JButton("+");
        btnEscalaMas.setPreferredSize(new Dimension(50, 30));
        
        panelEscala.add(btnEscalaMenos);
        panelEscala.add(labelEscala);
        panelEscala.add(btnEscalaMas);
        
        panelAjustes.add(panelEscala);
        
        // Botón de ajuste automático
        btnAutoEscala = new JButton("Ajustar Automáticamente");
        btnAutoEscala.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAutoEscala.setMaximumSize(new Dimension(250, 30));
        panelAjustes.add(Box.createRigidArea(new Dimension(0, 5)));
        panelAjustes.add(btnAutoEscala);
        
        add(panelAjustes);
    }
    
    /**
     * Configura los eventos de los componentes
     */
    private void configurarEventos() {
        btnAnadirMarca.addActionListener(e -> {
            if (onAnadirMarca != null) {
                onAnadirMarca.accept(null);
            }
        });
        
        sliderOpacidad.addChangeListener(e -> {
            int valor = sliderOpacidad.getValue();
            labelOpacidad.setText(valor + "%");
            
            ImagenFlotante marca = proyecto.getMarcaSeleccionada();
            if (marca != null) {
                marca.setOpacidad(valor / 100.0f);
                panelPreview.actualizarPreview();
            }
        });
        
        btnEscalaMas.addActionListener(e -> ajustarEscala(1.1));
        btnEscalaMenos.addActionListener(e -> ajustarEscala(0.9));
        
        btnAutoEscala.addActionListener(e -> ajustarEscalaAutomatica());
        
        btnEliminar.addActionListener(e -> eliminarMarcaSeleccionada());
        btnSubir.addActionListener(e -> moverMarca(-1));
        btnBajar.addActionListener(e -> moverMarca(1));
        
        comboMarcasAgua.addActionListener(e -> seleccionarMarca());

        btnImagenAnterior.addActionListener(event -> {
            if (proyecto.imagenAnterior()) {
                panelPreview.cargarImagen(proyecto.getImagenActual());
            }
        });

        btnImagenSiguiente.addActionListener(event -> {
            if (proyecto.siguienteImagen()) {
                panelPreview.cargarImagen(proyecto.getImagenActual());
            }
        });
    }
    
    /**
     * Ajusta la escala de la marca seleccionada
     */
    private void ajustarEscala(double factor) {
        ImagenFlotante marca = proyecto.getMarcaSeleccionada();
        if (marca != null) {
            marca.ajustarEscala(factor);
            int escalaPercent = (int) (marca.getEscala() * 100);
            labelEscala.setText(escalaPercent + "%");
            panelPreview.actualizarPreview();
        }
    }
    
    /**
     * Ajusta automáticamente la escala de la marca según el tamaño de la imagen
     */
    private void ajustarEscalaAutomatica() {
        ImagenFlotante marca = proyecto.getMarcaSeleccionada();
        BufferedImage imagenBase = panelPreview.getImagenBase();
        
        if (marca != null && imagenBase != null) {
            // Ajustar la marca de agua para que ocupe aproximadamente el 15% del ancho de la imagen
            marca.ajustarEscalaSegunImagen(imagenBase.getWidth(), 15.0);
            int escalaPercent = (int) (marca.getEscala() * 100);
            labelEscala.setText(escalaPercent + "%");
            panelPreview.actualizarPreview();
        }
    }
    
    /**
     * Elimina la marca de agua seleccionada
     */
    private void eliminarMarcaSeleccionada() {
        int indice = comboMarcasAgua.getSelectedIndex();
        if (indice >= 0) {
            proyecto.eliminarMarcaAgua(indice);
            modeloComboMarcas.removeElementAt(indice);
            panelPreview.actualizarPreview();
        }
    }
    
    /**
     * Mueve la marca en el orden Z
     */
    private void moverMarca(int direccion) {
        int indice = comboMarcasAgua.getSelectedIndex();
        if (proyecto.moverMarcaEnOrden(indice, direccion)) {
            actualizarListaMarcas();
            comboMarcasAgua.setSelectedIndex(indice + direccion);
            panelPreview.actualizarPreview();
        }
    }
    
    /**
     * Selecciona una marca de la lista
     */
    private void seleccionarMarca() {
        int indice = comboMarcasAgua.getSelectedIndex();
        if (indice >= 0) {
            proyecto.seleccionarMarca(indice);
            ImagenFlotante marca = proyecto.getMarcaSeleccionada();
            if (marca != null) {
                sliderOpacidad.setValue((int) (marca.getOpacidad() * 100));
                labelEscala.setText((int) (marca.getEscala() * 100) + "%");
            }
        }
    }
    
    /**
     * Añade una marca a la lista
     */
    public void agregarMarcaALista(String nombreArchivo) {
        modeloComboMarcas.addElement(nombreArchivo);
        comboMarcasAgua.setSelectedIndex(modeloComboMarcas.getSize() - 1);
    }
    
    /**
     * Actualiza la lista de marcas
     */
    public void actualizarListaMarcas() {
        modeloComboMarcas.removeAllElements();
        for (ImagenFlotante marca : proyecto.getListaMarcasAgua()) {
            modeloComboMarcas.addElement(marca.getNombreArchivo());
        }
    }
    
    // Setters para callbacks
    public void setOnAnadirMarca(Consumer<Void> callback) {
        this.onAnadirMarca = callback;
    }
}
