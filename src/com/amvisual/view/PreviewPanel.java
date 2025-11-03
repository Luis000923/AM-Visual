package com.amvisual.view;

import com.amvisual.model.Watermark;
import com.amvisual.util.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel personalizado que renderiza la imagen de vista previa y todas las marcas de agua
 * Gestiona la interacción del ratón (arrastrar para mover, rueda para escalar)
 * 
 * @author Vides_2GA
 * @version 1.0
 */
public class PreviewPanel extends JPanel {
    
    private BufferedImage previewImage;
    private List<Watermark> watermarks;
    private Watermark selectedWatermark;
    private Point dragOffset;
    private JComboBox<Watermark> watermarkComboBox;
    
    /**
     * Constructor
     */
    public PreviewPanel() {
        this.watermarks = new ArrayList<>();
        this.dragOffset = new Point();
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(240, 240, 240));
        setupMouseListeners();
    }
    
    /**
     * Establece la referencia al combo box para sincronizar selección
     * @param comboBox ComboBox de marcas de agua
     */
    public void setWatermarkComboBox(JComboBox<Watermark> comboBox) {
        this.watermarkComboBox = comboBox;
    }
    
    /**
     * Carga la imagen de vista previa desde un archivo
     * @param imageFile Archivo de imagen
     */
    public void setPreviewImage(File imageFile) {
        if (imageFile != null && imageFile.exists()) {
            previewImage = ImageUtils.cargarImagen(imageFile);
            repaint();
        }
    }
    
    /**
     * Establece la lista de marcas de agua
     * @param watermarks Lista de marcas
     */
    public void setWatermarks(List<Watermark> watermarks) {
        this.watermarks = watermarks;
        repaint();
    }
    
    /**
     * Establece la marca de agua seleccionada
     * @param watermark Marca seleccionada
     */
    public void setSelectedWatermark(Watermark watermark) {
        this.selectedWatermark = watermark;
        repaint();
    }
    
    /**
     * Obtiene la marca de agua seleccionada
     * @return Marca seleccionada
     */
    public Watermark getSelectedWatermark() {
        return selectedWatermark;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Habilitar antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // Dibujar imagen de fondo si existe
        if (previewImage != null) {
            // Escalar imagen para ajustar al panel manteniendo aspecto
            int imgWidth = previewImage.getWidth();
            int imgHeight = previewImage.getHeight();
            
            double scale = Math.min(
                (double) panelWidth / imgWidth,
                (double) panelHeight / imgHeight
            );
            
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);
            
            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;
            
            g2d.drawImage(previewImage, x, y, scaledWidth, scaledHeight, null);
        } else {
            // Mostrar mensaje si no hay imagen
            g2d.setColor(Color.GRAY);
            String message = "Seleccione una carpeta de imágenes para comenzar";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(message);
            g2d.drawString(message, (panelWidth - msgWidth) / 2, panelHeight / 2);
        }
        
        // Dibujar todas las marcas de agua en orden
        for (Watermark watermark : watermarks) {
            watermark.draw(g2d, panelWidth, panelHeight);
        }
        
        // Dibujar borde resaltado alrededor de la marca seleccionada
        if (selectedWatermark != null) {
            Rectangle bounds = selectedWatermark.getBounds(panelWidth, panelHeight);
            g2d.setColor(new Color(0, 120, 215));
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                         10.0f, new float[]{10.0f}, 0.0f));
            g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            
            // Dibujar esquinas
            g2d.setStroke(new BasicStroke(2.0f));
            int cornerSize = 8;
            g2d.fillRect(bounds.x - cornerSize/2, bounds.y - cornerSize/2, cornerSize, cornerSize);
            g2d.fillRect(bounds.x + bounds.width - cornerSize/2, bounds.y - cornerSize/2, cornerSize, cornerSize);
            g2d.fillRect(bounds.x - cornerSize/2, bounds.y + bounds.height - cornerSize/2, cornerSize, cornerSize);
            g2d.fillRect(bounds.x + bounds.width - cornerSize/2, bounds.y + bounds.height - cornerSize/2, cornerSize, cornerSize);
        }
        
        g2d.dispose();
    }
    
    /**
     * Configura los listeners de mouse
     */
    private void setupMouseListeners() {
        // Mouse Listener para detectar clics
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point clickPoint = e.getPoint();
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                
                // Iterar hacia atrás para seleccionar la marca superior
                for (int i = watermarks.size() - 1; i >= 0; i--) {
                    Watermark watermark = watermarks.get(i);
                    Rectangle bounds = watermark.getBounds(panelWidth, panelHeight);
                    
                    if (bounds.contains(clickPoint)) {
                        selectedWatermark = watermark;
                        
                        // Calcular offset para arrastrar
                        double centerX = bounds.x + bounds.width / 2.0;
                        double centerY = bounds.y + bounds.height / 2.0;
                        dragOffset.x = (int) (clickPoint.x - centerX);
                        dragOffset.y = (int) (clickPoint.y - centerY);
                        
                        // Actualizar selección en ComboBox
                        if (watermarkComboBox != null) {
                            watermarkComboBox.setSelectedItem(watermark);
                        }
                        
                        repaint();
                        return;
                    }
                }
            }
        });
        
        // Mouse Motion Listener para arrastrar
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedWatermark != null) {
                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    
                    // Calcular nueva posición relativa
                    double newX = (e.getX() - dragOffset.x) / (double) panelWidth;
                    double newY = (e.getY() - dragOffset.y) / (double) panelHeight;
                    
                    selectedWatermark.setXRelativo(newX);
                    selectedWatermark.setYRelativo(newY);
                    
                    repaint();
                }
            }
        });
        
        // Mouse Wheel Listener para escalar
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (selectedWatermark != null) {
                    double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
                    double newScale = selectedWatermark.getEscala() * factor;
                    selectedWatermark.setEscala(newScale);
                    repaint();
                }
            }
        });
    }
}
