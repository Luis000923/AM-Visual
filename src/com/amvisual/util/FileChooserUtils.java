// Esta utilidad proporciona diálogos de selección de archivos mejorados para la aplicación.
// Se utiliza para elegir imágenes para marcas de agua y para seleccionar carpetas.
package com.amvisual.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.io.File;

public class FileChooserUtils {
    
    /**
     * Crea un JFileChooser mejorado para seleccionar imágenes
     * @param parent Componente padre
     * @return Archivo seleccionado o null
     */
    public static File elegirImagenMarcaAgua(Component parent) {
        JFileChooser fileChooser = crearFileChooserMejorado();
        
        // Configuración específica para imágenes
        fileChooser.setDialogTitle("Seleccionar Marca de Agua");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        // Filtros de archivo
        FileNameExtensionFilter filterPNG = new FileNameExtensionFilter(
            "Imágenes PNG (*.png)", "png");
        FileNameExtensionFilter filterJPG = new FileNameExtensionFilter(
            "Imágenes JPEG (*.jpg, *.jpeg)", "jpg", "jpeg");
        FileNameExtensionFilter filterTodos = new FileNameExtensionFilter(
            "Todas las imágenes (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg");
        
        fileChooser.addChoosableFileFilter(filterPNG);
        fileChooser.addChoosableFileFilter(filterJPG);
        fileChooser.addChoosableFileFilter(filterTodos);
        fileChooser.setFileFilter(filterTodos);
        
        // Panel de vista previa
        fileChooser.setAccessory(crearPanelVistaPrevia(fileChooser));
        
        int resultado = fileChooser.showOpenDialog(parent);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    /**
     * Crea un JFileChooser mejorado para seleccionar carpetas
     * @param parent Componente padre
     * @param titulo Título del diálogo
     * @param carpetaInicial Carpeta inicial (puede ser null)
     * @return Carpeta seleccionada o null
     */
    public static File elegirCarpeta(Component parent, String titulo, File carpetaInicial) {
        JFileChooser fileChooser = crearFileChooserMejorado();
        
        fileChooser.setDialogTitle(titulo);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        if (carpetaInicial != null && carpetaInicial.exists()) {
            fileChooser.setCurrentDirectory(carpetaInicial);
        }
        
        // Panel de información
        fileChooser.setAccessory(crearPanelInfoCarpeta(fileChooser));
        
        int resultado = fileChooser.showOpenDialog(parent);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
    
    /**
     * Crea un JFileChooser con configuración mejorada
     * @return JFileChooser configurado
     */
    private static JFileChooser crearFileChooserMejorado() {
        JFileChooser fileChooser = new JFileChooser();
        
        // Configuración general
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setApproveButtonText("Seleccionar");
        fileChooser.setApproveButtonToolTipText("Seleccionar archivo/carpeta");
        
        // Vista de archivos personalizada
        fileChooser.setFileView(new FileView() {
            @Override
            public Icon getIcon(File f) {
                if (f.isDirectory()) {
                    return UIManager.getIcon("FileView.directoryIcon");
                } else if (esImagen(f)) {
                    return UIManager.getIcon("FileView.fileIcon");
                }
                return super.getIcon(f);
            }
            
            @Override
            public String getDescription(File f) {
                if (esImagen(f)) {
                    return "Archivo de imagen";
                }
                return super.getDescription(f);
            }
        });
        
        // Establecer fuente más grande
        Font font = new Font("Segoe UI", Font.PLAIN, 12);
        setFontRecursively(fileChooser, font);
        
        return fileChooser;
    }
    
    /**
     * Crea un panel de vista previa para imágenes
     * @param fileChooser FileChooser asociado
     * @return Panel de vista previa
     */
    private static JPanel crearPanelVistaPrevia(JFileChooser fileChooser) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 300));
        panel.setBorder(BorderFactory.createTitledBorder("Vista Previa"));
        
        JLabel labelImagen = new JLabel("", SwingConstants.CENTER);
        labelImagen.setPreferredSize(new Dimension(180, 180));
        labelImagen.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JLabel labelInfo = new JLabel("<html><center>Seleccione una imagen</center></html>", 
                                      SwingConstants.CENTER);
        labelInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        
        panel.add(labelImagen, BorderLayout.CENTER);
        panel.add(labelInfo, BorderLayout.SOUTH);
        
        // Listener para actualizar la vista previa
        fileChooser.addPropertyChangeListener(evt -> {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                File file = (File) evt.getNewValue();
                if (file != null && esImagen(file)) {
                    try {
                        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                        Image img = icon.getImage();
                        
                        // Redimensionar para vista previa
                        int maxWidth = 170;
                        int maxHeight = 170;
                        int width = icon.getIconWidth();
                        int height = icon.getIconHeight();
                        
                        if (width > maxWidth || height > maxHeight) {
                            double scale = Math.min((double)maxWidth/width, (double)maxHeight/height);
                            width = (int)(width * scale);
                            height = (int)(height * scale);
                            img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        }
                        
                        labelImagen.setIcon(new ImageIcon(img));
                        labelInfo.setText(String.format(
                            "<html><center><b>%s</b><br>%dx%d píxeles<br>%.2f KB</center></html>",
                            file.getName(),
                            icon.getIconWidth(),
                            icon.getIconHeight(),
                            file.length() / 1024.0
                        ));
                    } catch (Exception e) {
                        labelImagen.setIcon(null);
                        labelInfo.setText("<html><center>Error al cargar imagen</center></html>");
                    }
                } else {
                    labelImagen.setIcon(null);
                    labelInfo.setText("<html><center>No es una imagen válida</center></html>");
                }
            }
        });
        
        return panel;
    }
    
    /**
     * Crea un panel de información para carpetas
     * @param fileChooser FileChooser asociado
     * @return Panel de información
     */
    private static JPanel crearPanelInfoCarpeta(JFileChooser fileChooser) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(200, 300));
        panel.setBorder(BorderFactory.createTitledBorder("Información"));
        
        JLabel labelNombre = new JLabel("Carpeta: ");
        JLabel labelRuta = new JLabel("Ruta: ");
        JLabel labelImagenes = new JLabel("Imágenes: ");
        JLabel labelEspacio = new JLabel("Tamaño: ");
        
        Font font = new Font("Arial", Font.PLAIN, 11);
        labelNombre.setFont(font);
        labelRuta.setFont(font);
        labelImagenes.setFont(font);
        labelEspacio.setFont(font);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(labelNombre);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(labelRuta);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(labelImagenes);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(labelEspacio);
        panel.add(Box.createVerticalGlue());
        
        // Listener para actualizar la información
        fileChooser.addPropertyChangeListener(evt -> {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                File file = (File) evt.getNewValue();
                if (file != null && file.isDirectory()) {
                    labelNombre.setText("<html><b>Carpeta:</b> " + file.getName() + "</html>");
                    labelRuta.setText("<html><b>Ruta:</b><br>" + 
                                    acortarRuta(file.getAbsolutePath(), 25) + "</html>");
                    
                    // Contar imágenes
                    int numImagenes = contarImagenes(file);
                    labelImagenes.setText("<html><b>Imágenes:</b> " + numImagenes + "</html>");
                    
                    // Calcular tamaño
                    long tamaño = calcularTamañoCarpeta(file);
                    labelEspacio.setText("<html><b>Tamaño:</b> " + formatearTamaño(tamaño) + "</html>");
                } else {
                    labelNombre.setText("Carpeta: -");
                    labelRuta.setText("Ruta: -");
                    labelImagenes.setText("Imágenes: -");
                    labelEspacio.setText("Tamaño: -");
                }
            }
        });
        
        return panel;
    }
    
    /**
     * Verifica si un archivo es una imagen
     */
    private static boolean esImagen(File file) {
        if (file == null || !file.isFile()) return false;
        String nombre = file.getName().toLowerCase();
        return nombre.endsWith(".png") || nombre.endsWith(".jpg") || nombre.endsWith(".jpeg");
    }
    
    /**
     * Cuenta las imágenes en una carpeta
     */
    private static int contarImagenes(File carpeta) {
        if (!carpeta.isDirectory()) return 0;
        
        File[] archivos = carpeta.listFiles();
        if (archivos == null) return 0;
        
        int count = 0;
        for (File file : archivos) {
            if (esImagen(file)) count++;
        }
        return count;
    }
    
    /**
     * Calcula el tamaño total de una carpeta
     */
    private static long calcularTamañoCarpeta(File carpeta) {
        if (!carpeta.isDirectory()) return 0;
        
        long tamaño = 0;
        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File file : archivos) {
                if (file.isFile()) {
                    tamaño += file.length();
                }
            }
        }
        return tamaño;
    }
    
    /**
     * Formatea el tamaño en bytes a formato legible
     */
    private static String formatearTamaño(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * Acorta una ruta para mostrar
     */
    private static String acortarRuta(String ruta, int maxLength) {
        if (ruta.length() <= maxLength) return ruta;
        return "..." + ruta.substring(ruta.length() - maxLength);
    }
    
    /**
     * Establece la fuente recursivamente en un componente
     */
    private static void setFontRecursively(Component component, Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setFontRecursively(child, font);
            }
        }
    }
}
