package com.amvisual.view;

import com.amvisual.model.ImagenFlotante;
import com.amvisual.model.ProyectoMarcaAgua;
import com.amvisual.util.ImageUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Panel de vista previa de la imagen con marcas de agua
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class PanelPreview extends JPanel {
    
    private JLabel labelImagen;
    private BufferedImage imagenBase;
    private BufferedImage imagenConMarcas;
    private BufferedImage imagenPreview;
    private ProyectoMarcaAgua proyecto;
    private double factorEscalaPreview = 1.0;
    private boolean actualizandoPreview = false;
    
    /**
     * Constructor
     * @param proyecto Proyecto de marcas de agua
     */
    public PanelPreview(ProyectoMarcaAgua proyecto) {
        this.proyecto = proyecto;
        inicializarComponentes();
        configurarEventos();
    }
    
    /**
     * Inicializa los componentes del panel
     */
    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Vista Previa",
            TitledBorder.LEFT,
            TitledBorder.TOP
        ));
        setPreferredSize(new Dimension(600, 500));
        setBackground(new Color(240, 240, 240));
        
        labelImagen = new JLabel("Seleccione una carpeta de imágenes para comenzar");
        labelImagen.setHorizontalAlignment(SwingConstants.CENTER);
        labelImagen.setVerticalAlignment(SwingConstants.CENTER);
        labelImagen.setForeground(Color.GRAY);
        
        add(labelImagen, BorderLayout.CENTER);
    }
    
    /**
     * Configura los eventos del panel
     */
    private void configurarEventos() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            private ImagenFlotante marcaEnMovimiento;
            private Point ultimaPosicion;
            private long ultimaActualizacion = 0;
            private static final long INTERVALO_ACTUALIZACION = 50; // ms entre actualizaciones
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (imagenBase == null || factorEscalaPreview == 0 || imagenPreview == null) return;
                
                // Obtener coordenadas relativas al icono
                Point coordenadasIcono = obtenerCoordenadasRelativasAlIcono(e.getPoint());
                if (coordenadasIcono == null) return;
                
                // Convertir coordenadas del preview a coordenadas de la imagen original
                int xOriginal = (int) (coordenadasIcono.x / factorEscalaPreview);
                int yOriginal = (int) (coordenadasIcono.y / factorEscalaPreview);
                
                ultimaPosicion = new Point(xOriginal, yOriginal);
                ImagenFlotante marcaSeleccionada = proyecto.getMarcaSeleccionada();
                
                if (marcaSeleccionada != null) {
                    if (marcaSeleccionada.iniciarMovimiento(
                            xOriginal, yOriginal,
                            imagenBase.getWidth(), imagenBase.getHeight())) {
                        marcaEnMovimiento = marcaSeleccionada;
                    }
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (marcaEnMovimiento != null && imagenBase != null && 
                    ultimaPosicion != null && factorEscalaPreview != 0 && imagenPreview != null) {
                    
                    // Obtener coordenadas relativas al icono
                    Point coordenadasIcono = obtenerCoordenadasRelativasAlIcono(e.getPoint());
                    if (coordenadasIcono == null) return;
                    
                    // Convertir coordenadas del preview a coordenadas de la imagen original
                    int xOriginal = (int) (coordenadasIcono.x / factorEscalaPreview);
                    int yOriginal = (int) (coordenadasIcono.y / factorEscalaPreview);
                    
                    marcaEnMovimiento.mover(
                        xOriginal, yOriginal,
                        imagenBase.getWidth(), imagenBase.getHeight()
                    );
                    ultimaPosicion = new Point(xOriginal, yOriginal);
                    
                    // Limitar la frecuencia de actualización para mejorar el rendimiento
                    long ahora = System.currentTimeMillis();
                    if (ahora - ultimaActualizacion > INTERVALO_ACTUALIZACION) {
                        actualizarPreview();
                        ultimaActualizacion = ahora;
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (marcaEnMovimiento != null) {
                    marcaEnMovimiento.finalizarMovimiento();
                    marcaEnMovimiento = null;
                    // Actualizar una vez al soltar para asegurar la posición final
                    actualizarPreview();
                }
                ultimaPosicion = null;
            }
        };
        
        // Añadir listeners solo al label de la imagen
        labelImagen.addMouseListener(mouseHandler);
        labelImagen.addMouseMotionListener(mouseHandler);
    }
    
    /**
     * Obtiene las coordenadas relativas al icono de la imagen dentro del JLabel
     * @param puntoLabel Punto relativo al JLabel
     * @return Punto relativo al icono, o null si está fuera del icono
     */
    private Point obtenerCoordenadasRelativasAlIcono(Point puntoLabel) {
        if (labelImagen.getIcon() == null || imagenPreview == null) return null;
        
        int labelWidth = labelImagen.getWidth();
        int labelHeight = labelImagen.getHeight();
        int iconWidth = imagenPreview.getWidth();
        int iconHeight = imagenPreview.getHeight();
        
        // Calcular el offset del icono dentro del label (está centrado)
        int offsetX = (labelWidth - iconWidth) / 2;
        int offsetY = (labelHeight - iconHeight) / 2;
        
        // Coordenadas relativas al icono
        int x = puntoLabel.x - offsetX;
        int y = puntoLabel.y - offsetY;
        
        // Verificar que el clic esté dentro del icono
        if (x < 0 || x >= iconWidth || y < 0 || y >= iconHeight) {
            return null;
        }
        
        return new Point(x, y);
    }
    
    /**
     * Carga una imagen para la vista previa
     * @param archivo Archivo de imagen
     */
    public void cargarImagen(File archivo) {
        if (archivo == null) return;
        
        // Verificar si está en caché
        BufferedImage imagenCacheada = proyecto.getImageCache().get(archivo);
        if (imagenCacheada != null) {
            imagenBase = imagenCacheada;
            actualizarPreview();
            return;
        }
        
        // Mostrar mensaje de carga
        labelImagen.setText("Cargando imagen...");
        labelImagen.setIcon(null);
        
        // Cargar en hilo de fondo
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return ImageUtils.cargarImagen(archivo);
            }
            
            @Override
            protected void done() {
                try {
                    imagenBase = get();
                    if (imagenBase != null) {
                        proyecto.getImageCache().put(archivo, imagenBase);
                        actualizarPreview();
                    } else {
                        labelImagen.setText("Error al cargar la imagen");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    labelImagen.setText("Error al cargar la imagen");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Actualiza la vista previa con las marcas de agua aplicadas
     */
    public void actualizarPreview() {
        if (imagenBase == null || actualizandoPreview) {
            return;
        }
        
        actualizandoPreview = true;
        
        // Usar SwingWorker para no bloquear la interfaz
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                // Copiar la imagen base
                BufferedImage tempImagen = copiarImagen(imagenBase);
                
                // Aplicar todas las marcas de agua en orden
                for (ImagenFlotante marca : proyecto.getListaMarcasAgua()) {
                    tempImagen = marca.dibujar(tempImagen);
                }
                
                // Redimensionar para la vista previa
                return ImageUtils.redimensionarParaPreview(tempImagen, 580, 480);
            }
            
            @Override
            protected void done() {
                try {
                    imagenConMarcas = copiarImagen(imagenBase);
                    for (ImagenFlotante marca : proyecto.getListaMarcasAgua()) {
                        imagenConMarcas = marca.dibujar(imagenConMarcas);
                    }
                    
                    imagenPreview = get();
                    
                    // Calcular el factor de escala usado
                    int anchoOriginal = imagenConMarcas.getWidth();
                    factorEscalaPreview = (double) imagenPreview.getWidth() / anchoOriginal;
                    
                    labelImagen.setIcon(new ImageIcon(imagenPreview));
                    labelImagen.setText("");
                    repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    actualizandoPreview = false;
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Copia una imagen
     * @param original Imagen original
     * @return Copia de la imagen
     */
    private BufferedImage copiarImagen(BufferedImage original) {
        BufferedImage copia = new BufferedImage(
            original.getWidth(),
            original.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        copia.createGraphics().drawImage(original, 0, 0, null);
        return copia;
    }
    
    /**
     * Limpia la vista previa
     */
    public void limpiar() {
        imagenBase = null;
        imagenConMarcas = null;
        labelImagen.setIcon(null);
        labelImagen.setText("Seleccione una carpeta de imágenes para comenzar");
    }
    
    /**
     * Obtiene la imagen base
     * @return Imagen base
     */
    public BufferedImage getImagenBase() {
        return imagenBase;
    }
}
