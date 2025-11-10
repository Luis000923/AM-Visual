// Este es el controlador principal de la interfaz de usuario.
// Gestiona todos los eventos de la UI, como clics de botones, arrastrar y soltar, y sliders.
// Se conecta con:
// - com.amvisual.model.ProyectoMarcaAgua: El modelo principal que contiene el estado de la aplicación.
// - com.amvisual.model.ImagenFlotante: Para manipular las marcas de agua individuales.
// - com.amvisual.util.*: Clases de utilidad para operaciones de imagen y archivos.
// - MainView.fxml: La vista que este controlador maneja.
package com.amvisual.controllerfx;

import com.amvisual.model.ImagenFlotante;
import com.amvisual.model.OrientacionImagen;
import com.amvisual.model.ProyectoMarcaAgua;
import com.amvisual.model.posicionamiento.WatermarkConfig;
import com.amvisual.util.FileUtils;
import com.amvisual.util.FxImageUtils;
import com.amvisual.util.ImageUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    // Root
    @FXML private BorderPane root;

    // Center
    @FXML private StackPane centerStack;
    @FXML private ScrollPane previewScroll;
    @FXML private ImageView previewImage;
    private final Pane watermarkLayer = new Pane(); // Capa para las marcas de agua
    @FXML private Label dropHint;
    @FXML private ScrollPane gridScroll;
    @FXML private TilePane thumbsGrid;

    // Right controls
    @FXML private VBox rightControls;
    @FXML private Button btnAddFolder;
    @FXML private Button btnAddWatermark;
    @FXML private ComboBox<String> comboWatermarks;
    @FXML private Slider sliderOpacity;
    @FXML private Label opacityLabel;
    @FXML private Slider sliderSize;
    @FXML private Label sizeLabel;
    @FXML private ToggleButton toggleGrid;
    @FXML private Button btnAddOutputFolder;
    @FXML private Button btnDeleteWatermark;
    
    // Navigation
    @FXML private Button btnPrevImage;
    @FXML private Button btnNextImage;
    @FXML private Label imageCountLabel;

    // Bottom
    @FXML private Button btnProcess;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Button btnCancel;

    // --- Estado del controlador ---
    private final ProyectoMarcaAgua proyecto = new ProyectoMarcaAgua();
    private List<File> displayedImages = new ArrayList<>(); // Lista filtrada de imágenes mostradas
    private Task<Void> processingTask;
    
    // Vistas de las marcas de agua y la marca seleccionada
    private final Map<ImagenFlotante, ImageView> watermarkViews = new HashMap<>();
    private final Map<ImagenFlotante, WatermarkConfig> watermarkBaseConfigs = new HashMap<>();
    private ImageView selectedWatermarkView;
    private ImagenFlotante selectedWatermarkModel;
    private boolean isUpdatingSizeSlider;

    // Contexto para arrastre y escalado
    private double dragStartX, dragStartY, initialTranslateX, initialTranslateY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDnD();
        setupControls();
        setupMouseDrag();
        setupViewToggles();
        setupNavigation();
        applyTheme();

        if (statusLabel != null) {
            statusLabel.setText("Listo para comenzar");
        }
        if (progressBar != null) {
            progressBar.setProgress(0);
        }
        updateImageCounter();

        // Ajustes visuales
        if (previewImage != null) {
            previewImage.setPreserveRatio(true);
        }
        if (dropHint != null && centerStack != null) {
            StackPane.setAlignment(dropHint, Pos.CENTER);
        }

        // Añadir la capa de marcas de agua sobre la imagen de vista previa
        if (centerStack != null) {
            watermarkLayer.setPickOnBounds(false); // Permite que los eventos de ratón pasen a la imagen de abajo
            centerStack.getChildren().add(watermarkLayer);
        }
        
        // Binding para que la imagen se adapte automáticamente al tamaño de la ventana
        if (previewImage != null && centerStack != null) {
            previewImage.fitWidthProperty().bind(centerStack.widthProperty().subtract(20));
            previewImage.fitHeightProperty().bind(centerStack.heightProperty().subtract(20));
        }
    }

    private void setupDnD() {
        // Permitir drag over en todo el root
        root.setOnDragOver(this::onDragOver);
        root.setOnDragDropped(this::onDragDropped);

        // También en el centro
        centerStack.setOnDragOver(this::onDragOver);
        centerStack.setOnDragDropped(this::onDragDropped);
    }

    private void onDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() && db.getFiles().stream().anyMatch(File::isDirectory)) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            Optional<File> dir = db.getFiles().stream().filter(File::isDirectory).findFirst();
            dir.ifPresent(this::loadFolder);
            success = dir.isPresent();
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void setupControls() {
        // Agregar carpeta (abrir selector de directorios)
        if (btnAddFolder != null) {
            btnAddFolder.setOnAction(e -> handleAddFolder());
        }
        
        // Añadir marca de agua
        if (btnAddWatermark != null) {
            btnAddWatermark.setOnAction(this::handleAddWatermark);
        }

        // Sliders optimizados con debouncing
        if (sliderOpacity != null) {
            sliderOpacity.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (opacityLabel != null) {
                    opacityLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                }
                if (selectedWatermarkModel != null) {
                    selectedWatermarkModel.setOpacidad(newVal.floatValue());
                    updateWatermarkView(selectedWatermarkModel); // Actualiza la vista
                }
            });
        }

        // Slider de tamaño
        if (sliderSize != null) {
            sliderSize.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (sizeLabel != null) {
                    sizeLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                }
                if (isUpdatingSizeSlider) {
                    return;
                }
                if (selectedWatermarkModel != null && previewImage.getImage() != null) {
                    // Aplicar escalado usando el sistema de posicionamiento adaptativo
                    OrientacionImagen orientacion = getCurrentImageOrientation();
                    if (orientacion == null) return;

                    WatermarkConfig baseConfig = watermarkBaseConfigs.get(selectedWatermarkModel);
                    if (baseConfig == null) {
                        baseConfig = selectedWatermarkModel.getConfig(orientacion);
                        if (baseConfig == null) return;
                        watermarkBaseConfigs.put(selectedWatermarkModel, baseConfig);
                    }

                    double scaleFactor = newVal.doubleValue();
                    WatermarkConfig newConfig = baseConfig.scaled(scaleFactor);
                    selectedWatermarkModel.setConfig(newConfig, orientacion);
                    updateWatermarkView(selectedWatermarkModel);
                }
            });
        }
        
        // Combo de marcas: Selecciona el modelo y actualiza la vista
        comboWatermarks.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                selectWatermark(null);
                return;
            }
            proyecto.getListaMarcasAgua().stream()
                .filter(m -> m.getNombreArchivo().equals(newVal))
                .findFirst()
                .ifPresent(this::selectWatermark);
        });

        // Procesar imágenes
        btnProcess.setOnAction(e -> startProcessing());
        btnCancel.setOnAction(e -> cancelProcessing());
        btnDeleteWatermark.setOnAction(this::handleDeleteWatermark);
        btnAddOutputFolder.setOnAction(this::handleSetOutputFolder);
    }

    private void setupMouseDrag() {
        watermarkLayer.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        watermarkLayer.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        watermarkLayer.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        watermarkLayer.addEventHandler(ScrollEvent.SCROLL, this::handleScroll);
    }

    private void handleMousePressed(MouseEvent event) {
        if (selectedWatermarkView == null || !selectedWatermarkView.getBoundsInParent().contains(event.getX(), event.getY())) {
            return;
        }
        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        initialTranslateX = selectedWatermarkView.getTranslateX();
        initialTranslateY = selectedWatermarkView.getTranslateY();
        selectedWatermarkView.setCursor(javafx.scene.Cursor.MOVE);
        event.consume();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (selectedWatermarkView == null) return;

        double offsetX = event.getSceneX() - dragStartX;
        double offsetY = event.getSceneY() - dragStartY;
        
        selectedWatermarkView.setTranslateX(initialTranslateX + offsetX);
        selectedWatermarkView.setTranslateY(initialTranslateY + offsetY);
        
        // No actualizamos el modelo aquí para un rendimiento más fluido.
        // La actualización se hará al soltar el ratón.
        event.consume();
    }

    private void handleMouseReleased(MouseEvent event) {
        if (selectedWatermarkView == null) return;
        
        selectedWatermarkView.setCursor(javafx.scene.Cursor.HAND);
        // Al soltar, actualizamos la configuración del modelo.
        updateConfigFromView();
        event.consume();
    }

    private void handleScroll(ScrollEvent event) {
        if (selectedWatermarkView == null || !selectedWatermarkView.getBoundsInParent().contains(event.getX(), event.getY())) {
            return;
        }

        double scrollDelta = event.getDeltaY();
        double scaleFactor = (scrollDelta > 0) ? 1.05 : 0.95; // 5% de aumento/disminución

        // Obtenemos la configuración actual para modificarla
        OrientacionImagen orientacion = getCurrentImageOrientation();
        if (orientacion == null) return;
        
        WatermarkConfig currentConfig = selectedWatermarkModel.getConfig(orientacion);
        if (currentConfig == null) return; // No se puede escalar si no hay config

        // Escalamos la configuración usando el método integrado
        WatermarkConfig newConfig = currentConfig.scaled(scaleFactor);

        // Guardamos la nueva configuración en el modelo
        selectedWatermarkModel.setConfig(newConfig, orientacion);

        // Actualizamos la vista para reflejar el cambio
        updateWatermarkView(selectedWatermarkModel);
        event.consume();
    }

    private void handleAddWatermark(ActionEvent e) {
        try {
            // Verificar primero que haya una imagen cargada
            if (previewImage == null || previewImage.getImage() == null) {
                showAlert(Alert.AlertType.WARNING, "Sin imagen", 
                    "Por favor, carga primero una carpeta con imágenes antes de agregar marcas de agua.");
                return;
            }
            
            FileChooser fc = new FileChooser();
            fc.setTitle("Seleccionar imagen para marca de agua");
            fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Todas las imágenes", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg")
            );
            
            // Establecer directorio inicial
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                File homeDir = new File(userHome);
                if (homeDir.exists()) {
                    fc.setInitialDirectory(homeDir);
                }
            }
            
            // Asegurar que tenemos una ventana válida para el diálogo
            javafx.stage.Window owner = null;
            if (root != null && root.getScene() != null) {
                owner = root.getScene().getWindow();
            }
            
            File f = fc.showOpenDialog(owner);
            if (f == null) return;
            
            var img = ImageUtils.cargarImagen(f);
            if (img == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la imagen de marca de agua.");
                return;
            }
            
            ImagenFlotante marca = new ImagenFlotante(img, f.getName());
        
        // --- Lógica de inicialización de la configuración ---
        // Al añadir una nueva marca, creamos una configuración inicial por defecto.
        // La colocaremos en el centro con un tamaño relativo inteligente.
        double refWidth = previewImage.getImage().getWidth();
        double refHeight = previewImage.getImage().getHeight();
        double wmOriginalWidth = marca.getImagenOriginal().getWidth();
        double wmOriginalHeight = marca.getImagenOriginal().getHeight();

        // Calcular tamaño relativo inteligente basado en la imagen
        double aspectRatio = refWidth / refHeight;
        double relativeSize;
        
        if (aspectRatio > 1.5) {
            // Imagen muy horizontal: marca más pequeña (15%)
            relativeSize = 0.15;
        } else if (aspectRatio < 0.67) {
            // Imagen muy vertical: marca más pequeña (15%)
            relativeSize = 0.15;
        } else {
            // Imagen cuadrada o normal: tamaño medio (20%)
            relativeSize = 0.20;
        }
        
        WatermarkConfig initialConfig = com.amvisual.model.posicionamiento.WatermarkPositioner.createCenteredConfig(
            refWidth, refHeight,
            wmOriginalWidth, wmOriginalHeight,
            relativeSize
        );
        
        // Establecer la configuración para ambas orientaciones
        marca.setConfig(initialConfig, OrientacionImagen.HORIZONTAL);
        marca.setConfig(initialConfig, OrientacionImagen.VERTICAL);
        watermarkBaseConfigs.put(marca, initialConfig);

        proyecto.addMarcaAgua(marca);
        createWatermarkView(marca);
        
        if (comboWatermarks != null) {
            comboWatermarks.getItems().add(f.getName());
            comboWatermarks.getSelectionModel().selectLast(); // Esto disparará el listener y seleccionará la marca
        }
        
        if (statusLabel != null) {
            statusLabel.setText("Marca de agua agregada: " + f.getName());
        }
            
        } catch (Exception ex) {
            System.err.println("Error al agregar marca de agua: " + ex.getMessage());
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al agregar marca de agua: " + ex.getMessage());
        }
    }

    private void handleDeleteWatermark(ActionEvent e) {
        if (selectedWatermarkModel == null) {
            showAlert(Alert.AlertType.WARNING, "Sin selección", "Selecciona una marca de agua para eliminar.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar eliminación");
        confirmation.setHeaderText("¿Seguro que quieres eliminar la marca '" + selectedWatermarkModel.getNombreArchivo() + "'?");
        confirmation.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 1. Eliminar del modelo del proyecto
            proyecto.removeMarcaAgua(selectedWatermarkModel);

            // 2. Eliminar la vista de la capa y del mapa
            ImageView viewToRemove = watermarkViews.remove(selectedWatermarkModel);
            if (viewToRemove != null) {
                watermarkLayer.getChildren().remove(viewToRemove);
            }
            watermarkBaseConfigs.remove(selectedWatermarkModel);

            // 3. Eliminar del ComboBox
            comboWatermarks.getItems().remove(selectedWatermarkModel.getNombreArchivo());

            // 4. Limpiar selección
            selectedWatermarkModel = null;
            selectedWatermarkView = null;
            comboWatermarks.getSelectionModel().clearSelection();
            
            // 5. Actualizar UI
            updateWatermarkViews();
        }
    }

    private void handleSetOutputFolder(ActionEvent e) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta de salida");
        File folder = dc.showDialog(root.getScene().getWindow());
        if (folder != null) {
            proyecto.setCarpetaSalida(folder);
            statusLabel.setText("Carpeta de salida: " + folder.getPath());
        }
    }

    /**
     * Selecciona una marca de agua, actualizando el modelo y la vista.
     */
    private void selectWatermark(ImagenFlotante marca) {
        selectedWatermarkModel = marca;
        selectedWatermarkView = (marca != null) ? watermarkViews.get(marca) : null;
        
        // Actualizar la apariencia de todas las vistas de marcas
        for (Map.Entry<ImagenFlotante, ImageView> entry : watermarkViews.entrySet()) {
            ImageView view = entry.getValue();
            boolean isSelected = entry.getKey().equals(marca);
            view.setMouseTransparent(!isSelected);
            view.setCursor(isSelected ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
            // Opcional: añadir un efecto visual para la selección
            view.setEffect(isSelected ? new javafx.scene.effect.DropShadow() : null);
        }

        // Actualizar controles de la UI
        if (marca != null) {
            sliderOpacity.setValue(marca.getOpacidad());
            resetSizeSliderBaseline(marca);
        } else {
            resetSizeSliderBaseline(null);
        }
    }

    /**
     * Actualiza la vista de una marca de agua (posición y tamaño) basándose
     * en su configuración de modelo y la imagen de previsualización actual.
     */
    private void updateWatermarkView(ImagenFlotante marca) {
        ImageView view = watermarkViews.get(marca);
        Image preview = previewImage.getImage();
        if (view == null || preview == null) return;

        // 1. Obtener la configuración correcta del modelo (con regla inteligente)
        OrientacionImagen orientacion = getCurrentImageOrientation();
        WatermarkConfig config = marca.getConfig(orientacion);

        if (config == null) {
            // Si no hay configuración, crear una por defecto
            System.out.println("Creando configuración por defecto para " + marca.getNombreArchivo());
            double refWidth = preview.getWidth();
            double refHeight = preview.getHeight();
            double wmOriginalWidth = marca.getImagenOriginal().getWidth();
            double wmOriginalHeight = marca.getImagenOriginal().getHeight();
            
            double aspectRatio = refWidth / refHeight;
            double relativeSize = (aspectRatio > 1.5 || aspectRatio < 0.67) ? 0.15 : 0.20;
            
            config = com.amvisual.model.posicionamiento.WatermarkPositioner.createCenteredConfig(
                refWidth, refHeight,
                wmOriginalWidth, wmOriginalHeight,
                relativeSize
            );
            marca.setConfig(config, orientacion);
            watermarkBaseConfigs.putIfAbsent(marca, config);
        }
        view.setVisible(true);

        // 2. Calcular dimensiones de la imagen de previsualización renderizada
        double viewWidth = previewImage.getBoundsInLocal().getWidth();
        double viewHeight = previewImage.getBoundsInLocal().getHeight();
        double imageWidth = preview.getWidth();
        double imageHeight = preview.getHeight();
        
        double actualImageWidth, actualImageHeight, offsetX = 0, offsetY = 0;
        if (imageWidth / imageHeight > viewWidth / viewHeight) {
            // La imagen es más ancha proporcionalmente - se ajusta al ancho del view (letterbox vertical)
            actualImageWidth = viewWidth;
            actualImageHeight = viewWidth / (imageWidth / imageHeight);
            offsetY = (viewHeight - actualImageHeight) / 2.0;
        } else {
            // La imagen es más alta proporcionalmente - se ajusta al alto del view (pillarbox horizontal)
            actualImageHeight = viewHeight;
            actualImageWidth = viewHeight * (imageWidth / imageHeight);
            offsetX = (viewWidth - actualImageWidth) / 2.0;
        }

        // 3. Aplicar la configuración calculando dimensiones absolutas
        // Usar el mismo algoritmo inteligente que WatermarkCalculator
        double aspectRatio = actualImageWidth / actualImageHeight;
        double targetBase;
        if (aspectRatio > 1.2) {
            // Imagen horizontal: usa el ancho como referencia
            targetBase = actualImageWidth;
        } else if (aspectRatio < 0.83) {
            // Imagen vertical: usa el alto como referencia
            targetBase = actualImageHeight;
        } else {
            // Imagen casi cuadrada: usa la dimensión mínima
            targetBase = Math.min(actualImageWidth, actualImageHeight);
        }
        
        double finalWidth = config.getRelativeWidth() * targetBase;
        double finalHeight = finalWidth * config.getAspectRatio();
        double finalX = config.getRelativeX() * actualImageWidth;
        double finalY = config.getRelativeY() * actualImageHeight;

        // 4. Actualizar el ImageView
        view.setImage(FxImageUtils.toFxImage(marca.getImagenOriginal()));
        view.setOpacity(marca.getOpacidad());
        view.setFitWidth(finalWidth);
        view.setFitHeight(finalHeight);
        view.setTranslateX(offsetX + finalX);
        view.setTranslateY(offsetY + finalY);
    }

    /**
     * PARTE 1 del algoritmo: Lee la posición y tamaño de la vista (ImageView)
     * y la guarda como una nueva configuración relativa en el modelo.
     */
    private void updateConfigFromView() {
        if (selectedWatermarkModel == null || selectedWatermarkView == null || previewImage.getImage() == null) {
            return;
        }

        // 1. Obtener dimensiones de la imagen de previsualización renderizada
        double viewWidth = previewImage.getBoundsInLocal().getWidth();
        double viewHeight = previewImage.getBoundsInLocal().getHeight();
        double imageWidth = previewImage.getImage().getWidth();
        double imageHeight = previewImage.getImage().getHeight();

        double actualImageWidth, actualImageHeight, offsetX = 0, offsetY = 0;
        if (imageWidth / imageHeight > viewWidth / viewHeight) {
            // La imagen es más ancha proporcionalmente - se ajusta al ancho del view (letterbox vertical)
            actualImageWidth = viewWidth;
            actualImageHeight = viewWidth / (imageWidth / imageHeight);
            offsetY = (viewHeight - actualImageHeight) / 2.0;
        } else {
            // La imagen es más alta proporcionalmente - se ajusta al alto del view (pillarbox horizontal)
            actualImageHeight = viewHeight;
            actualImageWidth = viewHeight * (imageWidth / imageHeight);
            offsetX = (viewWidth - actualImageWidth) / 2.0;
        }

        // 2. Obtener la posición y tamaño actuales de la vista
        double wmViewX = selectedWatermarkView.getTranslateX() - offsetX;
        double wmViewY = selectedWatermarkView.getTranslateY() - offsetY;
        double wmViewWidth = selectedWatermarkView.getFitWidth();
        double wmViewHeight = selectedWatermarkView.getFitHeight();

        // 3. Convertir las coordenadas de la vista a coordenadas de la imagen original
        double scaleFactor = imageWidth / actualImageWidth;
        
        double absoluteX = wmViewX * scaleFactor;
        double absoluteY = wmViewY * scaleFactor;
        double absoluteWidth = wmViewWidth * scaleFactor;
        double absoluteHeight = wmViewHeight * scaleFactor;
        
        // Validación: asegurar que los valores sean positivos y razonables
        absoluteX = Math.max(0, absoluteX);
        absoluteY = Math.max(0, absoluteY);
        absoluteWidth = Math.max(1, Math.min(absoluteWidth, imageWidth));
        absoluteHeight = Math.max(1, Math.min(absoluteHeight, imageHeight));

        // 4. Llamar al método del modelo para que calcule y guarde la nueva config
        try {
            selectedWatermarkModel.actualizarConfiguracion(
                imageWidth, imageHeight,
                absoluteX, absoluteY,
                absoluteWidth, absoluteHeight
            );
            System.out.println("Configuración actualizada para " + selectedWatermarkModel.getNombreArchivo() + 
                ": pos(" + String.format("%.0f", absoluteX) + "," + String.format("%.0f", absoluteY) + 
                ") tamaño(" + String.format("%.0f", absoluteWidth) + "x" + String.format("%.0f", absoluteHeight) + ")");
            storeBaseConfig(selectedWatermarkModel);
            resetSizeSliderBaseline(selectedWatermarkModel);
        } catch (Exception ex) {
            System.err.println("Error al actualizar configuración: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private OrientacionImagen getCurrentImageOrientation() {
        Image img = previewImage.getImage();
        if (img == null) return null;
        return (img.getWidth() >= img.getHeight()) ? OrientacionImagen.HORIZONTAL : OrientacionImagen.VERTICAL;
    }

    private void handleAddFolder() {
        try {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Seleccionar carpeta de imágenes");
            
            // Intentar establecer un directorio inicial
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                File homeDir = new File(userHome);
                if (homeDir.exists()) {
                    dc.setInitialDirectory(homeDir);
                }
            }
            
            // Asegurar que tenemos una ventana válida para el diálogo
            javafx.stage.Window owner = null;
            if (root != null && root.getScene() != null) {
                owner = root.getScene().getWindow();
            }
            
            File folder = dc.showDialog(owner);
            
            if (folder != null && folder.isDirectory()) {
                loadFolder(folder);
                // Mantener la vista previa (no cambiar a cuadrícula)
                if (toggleGrid != null) {
                    toggleGrid.setSelected(false);
                }
                if (gridScroll != null) gridScroll.setVisible(false);
                if (previewScroll != null) previewScroll.setVisible(true);
            }
        } catch (Exception e) {
            System.err.println("Error al abrir selector de carpeta: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                "No se pudo abrir el selector de carpeta: " + e.getMessage());
        }
    }

    private void loadFolder(File folder) {
        proyecto.setCarpetaEntrada(folder);
        List<File> imagenes = FileUtils.obtenerImagenesEnCarpeta(folder);
        proyecto.setListaImagenes(imagenes);
        
        displayedImages = new ArrayList<>(imagenes);

        if (displayedImages.isEmpty()) {
            statusLabel.setText("No se encontraron imágenes en la carpeta");
            previewImage.setImage(null);
            thumbsGrid.getChildren().clear();
            updateNavigationButtons();
            updateImageCounter();
            return;
        }

        statusLabel.setText("Cargando carpeta: " + folder.getName() + " - " + displayedImages.size() + " imágenes...");
        
        // Cargar solo la primera imagen de forma asíncrona
        proyecto.setIndiceImagenActual(0);
        updateNavigationButtons();
        updateImageCounter();
        loadPreviewAsync(displayedImages.get(0), true);
        
        // Caché deshabilitado: Las imágenes se cargarán bajo demanda cuando se navegue
        // Esto evita problemas de memoria con carpetas grandes
    }

    private void buildThumbnails(List<File> imagenes) {
        thumbsGrid.getChildren().clear();
        statusLabel.setText("Generando miniaturas...");
        
        // Crear placeholders primero (instantáneo)
        for (int i = 0; i < imagenes.size(); i++) {
            Label placeholder = new Label("Cargando...");
            placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10;");
            StackPane cell = new StackPane(placeholder);
            cell.getStyleClass().add("thumb-cell");
            cell.setPrefSize(180, 120);
            cell.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0;");
            thumbsGrid.getChildren().add(cell);
        }
        
        // Cargar miniaturas de forma asíncrona y progresiva
        Task<Void> thumbTask = new Task<>() {
            @Override
            protected Void call() {
                for (int i = 0; i < imagenes.size(); i++) {
                    if (isCancelled()) break;
                    
                    final int index = i;
                    final File f = imagenes.get(i);
                    
                    // Cargar imagen completa desde caché o archivo
                    var imagen = proyecto.getImageCache().get(f);
                    if (imagen == null) {
                        imagen = ImageUtils.cargarImagen(f);
                        if (imagen != null) {
                            proyecto.getImageCache().put(f, imagen);
                        }
                    }
                    
                    // Aplicar marcas de agua si existen
                    if (imagen != null && !proyecto.getListaMarcasAgua().isEmpty()) {
                        for (ImagenFlotante marca : proyecto.getListaMarcasAgua()) {
                            imagen = marca.dibujar(imagen);
                        }
                    }
                    
                    // Convertir a JavaFX Image (miniatura)
                    final var imagenFinal = imagen;
                    Image img = (imagenFinal != null) ? FxImageUtils.toFxImage(imagenFinal) : null;
                    
                    // Actualizar UI en el hilo de JavaFX
                    final Image finalImg = img;
                    Platform.runLater(() -> {
                        if (index < thumbsGrid.getChildren().size()) {
                            StackPane cell = (StackPane) thumbsGrid.getChildren().get(index);
                            cell.getChildren().clear();
                            

                            if (finalImg != null) {
                                ImageView iv = new ImageView(finalImg);
                                iv.setPreserveRatio(true);
                                iv.setFitWidth(180);
                                iv.getStyleClass().add("thumb");
                                iv.setOnMouseClicked(e -> {
                                    setCurrentImage(f);
                                });
                                
                                cell.getChildren().add(iv);
                                cell.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;");
                            } else {
                                Label errorLabel = new Label("Error");
                                errorLabel.setStyle("-fx-text-fill: #ef4444;");
                                cell.getChildren().add(errorLabel);
                            }
                        }
                    });
                    
                    // Pequeña pausa para no saturar
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
        
        thumbTask.setOnSucceeded(e -> statusLabel.setText("Miniaturas cargadas"));
        thumbTask.setOnFailed(e -> statusLabel.setText("Error al cargar miniaturas"));
        
        new Thread(thumbTask, "thumbnail-loader").start();
    }
    
    /**
     * Refresca las miniaturas en la cuadrícula aplicando las marcas de agua actuales
     */
    private void refreshThumbnails() {
        if (displayedImages == null || displayedImages.isEmpty() || thumbsGrid.getChildren().isEmpty()) {
            return;
        }
        
        buildThumbnails(displayedImages);
    }

    private void setCurrentImage(File file) {
        for (int i = 0; i < displayedImages.size(); i++) {
            if (displayedImages.get(i).equals(file)) {
                proyecto.setIndiceImagenActual(i);
                updateImageCounter();
                loadPreviewAsync(file, false);
                break;
            }
        }
    }

    private void loadPreviewAsync(File file, boolean preloadNext) {
        if (file == null) {
            previewImage.setImage(null);
            dropHint.setVisible(true);
            return;
        }
        
        dropHint.setVisible(false);
        statusLabel.setText("Cargando imagen: " + file.getName());
        
        // Mostrar indicador de carga
        previewImage.setOpacity(0.5);
        
        Task<Image> loadTask = new Task<>() {
            @Override
            protected Image call() {
                // Cargar imagen base (sin marcas de agua)
                var base = proyecto.getImageCache().get(file);
                if (base == null) {
                    base = ImageUtils.cargarImagen(file);
                    if (base != null) {
                        proyecto.getImageCache().put(file, base);
                    }
                }
                if (base == null) return null;

                // Pre-cargar la siguiente imagen en segundo plano
                if (preloadNext) {
                    if (displayedImages != null && !displayedImages.isEmpty()) {
                        int nextIndex = (proyecto.getIndiceImagenActual() + 1) % displayedImages.size();
                        File nextFile = displayedImages.get(nextIndex);
                        if (proyecto.getImageCache().get(nextFile) == null) {
                            var nextImage = ImageUtils.cargarImagen(nextFile);
                            if (nextImage != null) {
                                proyecto.getImageCache().put(nextFile, nextImage);
                            }
                        }
                    }
                }
                
                // Redimensionar para vista previa y convertir
                return FxImageUtils.toFxImage(ImageUtils.redimensionarParaPreview(base, 1600, 1200));
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            previewImage.setImage(loadTask.getValue());
            previewImage.setOpacity(1.0);
            statusLabel.setText("Imagen " + (proyecto.getIndiceImagenActual() + 1) + " de " + displayedImages.size());
            // Una vez cargada la imagen base, actualizar las marcas de agua
            updateWatermarkViews();
        });
        
        loadTask.setOnFailed(e -> {
            previewImage.setOpacity(1.0);
            statusLabel.setText("Error al cargar: " + file.getName());
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la imagen.");
        });
        
        new Thread(loadTask, "image-loader").start();
    }

    private void startProcessing() {
        if (!proyecto.tieneImagenes()) {
            showAlert(Alert.AlertType.WARNING, "Sin imágenes", "Arrastra una carpeta con imágenes antes de procesar.");
            return;
        }
        if (!proyecto.tieneMarcasAgua()) {
            showAlert(Alert.AlertType.WARNING, "Sin marcas", "Añade al menos una marca de agua.");
            return;
        }
        
        // Verificar que hay una imagen actual mostrada
        if (previewImage.getImage() == null) {
            showAlert(Alert.AlertType.WARNING, "Sin imagen", "No hay ninguna imagen cargada en la vista previa.");
            return;
        }
        
        // Obtener la imagen actual
        int currentIndex = proyecto.getIndiceImagenActual();
        if (currentIndex < 0 || currentIndex >= displayedImages.size()) {
            showAlert(Alert.AlertType.WARNING, "Error", "No se pudo obtener la imagen actual.");
            return;
        }
        File currentImageFile = displayedImages.get(currentIndex);
        if (currentImageFile == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "No se pudo obtener la imagen actual.");
            return;
        }

        File carpetaSalida = proyecto.getCarpetaSalida();
        if (carpetaSalida == null) {
            carpetaSalida = new File(proyecto.getCarpetaEntrada(), "imagenes_con_marca");
            proyecto.setCarpetaSalida(carpetaSalida);
        }
        if (!FileUtils.crearCarpetaSiNoExiste(carpetaSalida)) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo crear la carpeta de salida.");
            return;
        }

        final File destino = carpetaSalida;
        // Procesar TODAS las imágenes de la carpeta, no solo la actual
        List<File> imagenes = new ArrayList<>(displayedImages);

        processingTask = new Task<>() {
            @Override
            protected Void call() {
                int total = imagenes.size();
                int i = 0;
                for (File archivoImagen : imagenes) {
                    if (isCancelled()) break;
                    try {
                        var imagen = proyecto.getImageCache().get(archivoImagen);
                        if (imagen == null) {
                            imagen = ImageUtils.cargarImagen(archivoImagen);
                            if (imagen != null) proyecto.getImageCache().put(archivoImagen, imagen);
                        }
                        if (imagen == null) continue;
                        for (ImagenFlotante marca : proyecto.getListaMarcasAgua()) {
                            imagen = marca.dibujar(imagen);
                        }
                        // Convertir a RGB si JPG/JPEG
                        String ext = FileUtils.getExtension(archivoImagen).toLowerCase();
                        if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
                            imagen = ImageUtils.convertirARGB(imagen);
                        }
                        File out = new File(destino, archivoImagen.getName());
                        String formato = ext.isEmpty() ? "png" : ext.substring(1);
                        ImageUtils.guardarImagen(imagen, out, formato);
                    } catch (Exception ex) {
                        // Log simple
                    } finally {
                        i++;
                        updateProgress(i, total);
                        updateMessage("Procesando (" + i + "/" + total + ")...");
                    }
                }
                return null;
            }
        };

        progressBar.progressProperty().bind(processingTask.progressProperty());
        processingTask.messageProperty().addListener((obs, ov, nv) -> statusLabel.setText(nv));
        processingTask.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1);
            statusLabel.setText("¡Procesamiento completado! " + imagenes.size() + " imagen(es) procesada(s) en: " + destino.getAbsolutePath());
            btnCancel.setVisible(false);
        });
        processingTask.setOnCancelled(e -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            statusLabel.setText("Procesamiento cancelado");
            btnCancel.setVisible(false);
        });
        processingTask.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            statusLabel.setText("Error durante el procesamiento");
            btnCancel.setVisible(false);
        });

        btnCancel.setVisible(true);
        new Thread(processingTask, "process-images").start();
    }

    private void cancelProcessing() {
        if (processingTask != null && processingTask.isRunning()) {
            processingTask.cancel(true);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // --- Nuevos métodos para gestionar las vistas de las marcas de agua ---

    private void createWatermarkView(ImagenFlotante marca) {
        ImageView watermarkView = new ImageView();
        watermarkView.setPreserveRatio(true);
        watermarkView.setMouseTransparent(true); // Inicialmente transparente a eventos
        watermarkViews.put(marca, watermarkView);
        watermarkLayer.getChildren().add(watermarkView);
    }

    private void storeBaseConfig(ImagenFlotante marca) {
        if (marca == null) {
            return;
        }
        WatermarkConfig config = marca.getConfig(null);
        if (config != null) {
            watermarkBaseConfigs.put(marca, config);
        }
    }

    private void resetSizeSliderBaseline(ImagenFlotante marca) {
        if (sliderSize == null) {
            return;
        }
        if (marca != null) {
            storeBaseConfig(marca);
        }
        isUpdatingSizeSlider = true;
        double baseline = 1.0;
        sliderSize.setValue(baseline);
        isUpdatingSizeSlider = false;
        if (sizeLabel != null) {
            sizeLabel.setText(String.format("%.0f%%", baseline * 100));
        }
    }

    private void updateWatermarkViews() {
        for (Map.Entry<ImagenFlotante, ImageView> entry : watermarkViews.entrySet()) {
            updateWatermarkView(entry.getKey());
        }
        
        // Si la cuadrícula está visible, actualizar las miniaturas con las marcas de agua
        if (toggleGrid != null && toggleGrid.isSelected() && !displayedImages.isEmpty()) {
            refreshThumbnails();
        }
    }

    private void setupNavigation() {
        btnPrevImage.setOnAction(e -> navigateImages(-1));
        btnNextImage.setOnAction(e -> navigateImages(1));
    }

    private void navigateImages(int direction) {
        if (displayedImages.isEmpty()) return;
        
        int newIndex = proyecto.getIndiceImagenActual() + direction;
        if (newIndex >= 0 && newIndex < displayedImages.size()) {
            setCurrentImage(displayedImages.get(newIndex));
        }
    }

    private void updateNavigationButtons() {
        int size = displayedImages.size();
        int current = proyecto.getIndiceImagenActual();
        btnPrevImage.setDisable(current <= 0);
        btnNextImage.setDisable(current >= size - 1);
    }

    private void updateImageCounter() {
        int total = displayedImages.size();
        if (total == 0) {
            imageCountLabel.setText("0 / 0");
        } else {
            imageCountLabel.setText((proyecto.getIndiceImagenActual() + 1) + " / " + total);
        }
        updateNavigationButtons();
    }

    private void setupViewToggles() {
        toggleGrid.setOnAction(e -> {
            boolean isGrid = toggleGrid.isSelected();
            gridScroll.setVisible(isGrid);
            previewScroll.setVisible(!isGrid);
            watermarkLayer.setVisible(!isGrid); // Oculta la capa de marcas de agua flotantes

            if (isGrid) {
                buildThumbnails(displayedImages);
            }
        });
    }

    private void applyTheme() {
        // Cargar la hoja de estilos principal. Asume que está en la misma carpeta que el FXML.
        try {
            Scene scene = root.getScene();
            if (scene != null) {
                URL cssUrl = getClass().getResource("/com/amvisual/viewfx/css/light.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("No se pudo encontrar el archivo CSS: light.css");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el tema CSS: " + e.getMessage());
        }
    }
}