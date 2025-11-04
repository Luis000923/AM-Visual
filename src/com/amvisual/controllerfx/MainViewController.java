package com.amvisual.controllerfx;

import com.amvisual.model.ImagenFlotante;
import com.amvisual.model.ProyectoMarcaAgua;
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
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador principal de la interfaz JavaFX.
 * Implementa:
 * - Drag & drop de carpetas
 * - Vista previa o cuadrícula de miniaturas
 * - Controles de marcas de agua (añadir, opacidad, tamaño, posición)
 * - Progreso global de procesamiento y estado
 */
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
    @FXML private Slider sliderSize;
    @FXML private Label opacityLabel;
    @FXML private Label sizeLabel;
    @FXML private ToggleButton toggleGrid;
    @FXML private Button btnAddOutputFolder;
    @FXML private Button btnDeleteWatermark;
    @FXML private ProgressBar imageCacheProgressBar;
    
    // Navigation
    @FXML private Button btnPrevImage;
    @FXML private Button btnNextImage;
    @FXML private Label imageCountLabel;

    // Bottom
    @FXML private Button btnProcess;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Button btnCancel;

    // Modelo y estado
    private final ProyectoMarcaAgua proyecto = new ProyectoMarcaAgua();
    private Task<Void> processingTask;
    private Task<Void> cachePopulationTask;
    private final Map<ImagenFlotante, ImageView> watermarkViews = new HashMap<>();
    private ImageView selectedWatermarkView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDnD();
        setupControls();
        setupMouseDrag();
        setupViewToggles();
        setupNavigation();
        applyTheme();

        statusLabel.setText("Listo para comenzar");
        progressBar.setProgress(0);
        updateImageCounter();

        // Ajustes visuales
        previewImage.setPreserveRatio(true);
        StackPane.setAlignment(dropHint, Pos.CENTER);

        // Añadir la capa de marcas de agua sobre la imagen de vista previa
        watermarkLayer.setPickOnBounds(false); // Permite que los eventos de ratón pasen a la imagen de abajo
        centerStack.getChildren().add(watermarkLayer);
        
        // Binding para que la imagen se adapte automáticamente al tamaño de la ventana
        previewImage.fitWidthProperty().bind(centerStack.widthProperty().subtract(20));
        previewImage.fitHeightProperty().bind(centerStack.heightProperty().subtract(20));
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
        btnAddWatermark.setOnAction(this::handleAddWatermark);

        // Sliders optimizados con debouncing
        setupOptimizedSliders();

        // Combo de marcas
        comboWatermarks.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> {
            int idx = n == null ? -1 : n.intValue();
            proyecto.seleccionarMarca(idx);
            updateWatermarkViews(); // Actualiza la vista de las marcas
            if (idx >= 0) {
                ImagenFlotante m = proyecto.getMarcaSeleccionada();
                if (m != null) {
                    sliderOpacity.setValue(m.getOpacidad());
                    sliderSize.setValue(m.getEscala());
                }
            }
        });

        // Procesar imágenes
        btnProcess.setOnAction(e -> startProcessing());
        btnCancel.setOnAction(e -> cancelProcessing());
    }
    
    private void setupOptimizedSliders() {
        // Actualizar solo las etiquetas en tiempo real
        sliderOpacity.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (opacityLabel != null) {
                opacityLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
            }
            // Actualización en tiempo real
            ImagenFlotante marca = proyecto.getMarcaSeleccionada();
            if (marca != null) {
                marca.setOpacidad(newVal.floatValue());
                updateWatermarkView(marca);
            }
        });
        
        sliderSize.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sizeLabel != null) {
                // Mostrar el porcentaje real basado en el rango (0.05 a 5.0)
                sizeLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
            }
            // Actualización en tiempo real
            ImagenFlotante marca = proyecto.getMarcaSeleccionada();
            if (marca != null) {
                marca.setEscala(newVal.doubleValue());
                updateWatermarkView(marca);
            }
        });
        
        // Ya no se necesita el listener en MouseReleased o KeyReleased
        // La actualización es ahora en tiempo real y eficiente.
    }

    private void handleAddFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Seleccionar carpeta de imágenes");
        File folder = dc.showDialog(root.getScene() != null ? root.getScene().getWindow() : null);
        if (folder != null && folder.isDirectory()) {
            loadFolder(folder);
            // Mantener la vista previa (no cambiar a cuadrícula)
            if (toggleGrid != null) {
                toggleGrid.setSelected(false);
            }
            gridScroll.setVisible(false);
            previewScroll.setVisible(true);
        }
    }

    private void setupMouseDrag() {
        final double[] dragContext = new double[4]; // startX, startY, initialTranslateX, initialTranslateY

        watermarkLayer.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (selectedWatermarkView == null) return;

            // Asegurarse de que el evento ocurrió dentro de los límites del ImageView de la marca
            if (selectedWatermarkView.getBoundsInParent().contains(event.getX(), event.getY())) {
                dragContext[0] = event.getSceneX();
                dragContext[1] = event.getSceneY();
                dragContext[2] = selectedWatermarkView.getTranslateX();
                dragContext[3] = selectedWatermarkView.getTranslateY();
                selectedWatermarkView.setCursor(javafx.scene.Cursor.MOVE);
                event.consume(); // Consumir el evento para que no se propague
            }
        });

        watermarkLayer.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (selectedWatermarkView == null) return;

            // Arrastre completamente libre - sin restricciones
            double offsetX = event.getSceneX() - dragContext[0];
            double offsetY = event.getSceneY() - dragContext[1];
            double newTranslateX = dragContext[2] + offsetX;
            double newTranslateY = dragContext[3] + offsetY;

            // Aplicar directamente la nueva posición sin límites
            selectedWatermarkView.setTranslateX(newTranslateX);
            selectedWatermarkView.setTranslateY(newTranslateY);
            
            // Actualizar la posición en el modelo en tiempo real para feedback visual
            updateWatermarkPositionFromView(proyecto.getMarcaSeleccionada());
            
            event.consume();
        });

        watermarkLayer.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (selectedWatermarkView == null) return;

            selectedWatermarkView.setCursor(javafx.scene.Cursor.HAND);
            // Actualización final de la posición - el algoritmo ajusta automáticamente
            updateWatermarkPositionFromView(proyecto.getMarcaSeleccionada());
            event.consume();
        });
    }

    private void setupViewToggles() {
        toggleGrid.setOnAction(e -> {
            boolean showGrid = toggleGrid.isSelected();
            if (showGrid) {
                // Cargar miniaturas solo cuando se active la cuadrícula
                List<File> imagenes = proyecto.getListaImagenes();
                if (imagenes != null && !imagenes.isEmpty() && thumbsGrid.getChildren().isEmpty()) {
                    buildThumbnails(imagenes);
                }
            }
            gridScroll.setVisible(showGrid);
            previewScroll.setVisible(!showGrid);
        });
    }
    
    private void setupNavigation() {
        btnPrevImage.setOnAction(e -> navigatePrevious());
        btnNextImage.setOnAction(e -> navigateNext());
        updateNavigationButtons();
    }
    
    private void navigatePrevious() {
        List<File> imagenes = proyecto.getListaImagenes();
        if (imagenes == null || imagenes.isEmpty()) return;
        
        int current = proyecto.getIndiceImagenActual();
        int newIndex = current - 1;
        if (newIndex < 0) newIndex = imagenes.size() - 1; // Circular
        
        proyecto.setIndiceImagenActual(newIndex);
        updateImageCounter();
        loadPreviewAsync(imagenes.get(newIndex), true);
    }
    
    private void navigateNext() {
        List<File> imagenes = proyecto.getListaImagenes();
        if (imagenes == null || imagenes.isEmpty()) return;
        
        int current = proyecto.getIndiceImagenActual();
        int newIndex = current + 1;
        if (newIndex >= imagenes.size()) newIndex = 0; // Circular
        
        proyecto.setIndiceImagenActual(newIndex);
        updateImageCounter();
        loadPreviewAsync(imagenes.get(newIndex), true);
    }
    
    private void updateNavigationButtons() {
        List<File> imagenes = proyecto.getListaImagenes();
        boolean hasImages = imagenes != null && !imagenes.isEmpty();
        btnPrevImage.setDisable(!hasImages);
        btnNextImage.setDisable(!hasImages);
    }
    
    private void updateImageCounter() {
        List<File> imagenes = proyecto.getListaImagenes();
        if (imagenes == null || imagenes.isEmpty()) {
            imageCountLabel.setText("0 / 0");
        } else {
            int current = proyecto.getIndiceImagenActual() + 1;
            int total = imagenes.size();
            imageCountLabel.setText(current + " / " + total);
        }
    }

    private void applyTheme() {
        Scene scene = root.getScene();
        if (scene == null) {
             Platform.runLater(() -> applyTheme());
             return;
        }
        scene.getStylesheets().removeIf(s -> s.contains("/css/"));
        String path = "/com/amvisual/viewfx/css/light.css";
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
    }

    private void handleAddWatermark(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        File f = fc.showOpenDialog(root.getScene().getWindow());
        if (f == null) return;
        var img = ImageUtils.cargarImagen(f);
        if (img == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la imagen de marca de agua.");
            return;
        }
        ImagenFlotante marca = new ImagenFlotante(img, f.getName());
        proyecto.addMarcaAgua(marca);
        
        // Crear y añadir la vista para la nueva marca
        createWatermarkView(marca);

        comboWatermarks.getItems().add(f.getName());
        comboWatermarks.getSelectionModel().selectLast();
        statusLabel.setText("Marca de agua añadida: " + f.getName());
        
        updateWatermarkViews();
    }

    private void loadFolder(File folder) {
        proyecto.setCarpetaEntrada(folder);
        List<File> imagenes = FileUtils.obtenerImagenesEnCarpeta(folder);
        proyecto.setListaImagenes(imagenes);

        if (imagenes.isEmpty()) {
            statusLabel.setText("No se encontraron imágenes en la carpeta");
            previewImage.setImage(null);
            thumbsGrid.getChildren().clear();
            updateNavigationButtons();
            updateImageCounter();
            return;
        }

        statusLabel.setText("Cargando carpeta: " + folder.getName() + " - " + imagenes.size() + " imágenes...");
        
        // Cargar solo la primera imagen de forma asíncrona
        proyecto.setIndiceImagenActual(0);
        updateNavigationButtons();
        updateImageCounter();
        loadPreviewAsync(imagenes.get(0), true);
        
        // Iniciar la tarea de población de caché en segundo plano
        populateCacheInBackground(imagenes);
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
                    
                    // Cargar miniatura en background
                    Image img = FxImageUtils.loadFxImage(f, 180);
                    
                    // Actualizar UI en el hilo de JavaFX
                    final Image finalImg = img;
                    Platform.runLater(() -> {
                        if (index < thumbsGrid.getChildren().size()) {
                            StackPane cell = (StackPane) thumbsGrid.getChildren().get(index);
                            cell.getChildren().clear();
                            
                            ImageView iv = new ImageView(finalImg);
                            iv.setPreserveRatio(true);
                            iv.setFitWidth(180);
                            iv.getStyleClass().add("thumb");
                            iv.setOnMouseClicked(e -> {
                                setCurrentImage(f);
                            });
                            
                            cell.getChildren().add(iv);
                            cell.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;");
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

    private void populateCacheInBackground(List<File> images) {
        if (cachePopulationTask != null && cachePopulationTask.isRunning()) {
            cachePopulationTask.cancel();
        }

        cachePopulationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < images.size(); i++) {
                    if (isCancelled()) break;
                    
                    File imageFile = images.get(i);
                    if (proyecto.getImageCache().get(imageFile) == null) {
                        var image = ImageUtils.cargarImagen(imageFile);
                        if (image != null) {
                            proyecto.getImageCache().put(imageFile, image);
                        }
                    }
                    
                    // Actualizar el progreso
                    updateProgress(i + 1, images.size());
                }
                return null;
            }
        };

        // Enlazar la barra de progreso de la caché a la tarea
        imageCacheProgressBar.progressProperty().bind(cachePopulationTask.progressProperty());

        new Thread(cachePopulationTask, "cache-population-thread").start();
    }

    private void setCurrentImage(File file) {
        List<File> lista = proyecto.getListaImagenes();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).equals(file)) {
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
                    List<File> imagenes = proyecto.getListaImagenes();
                    if (imagenes != null && !imagenes.isEmpty()) {
                        int nextIndex = (proyecto.getIndiceImagenActual() + 1) % imagenes.size();
                        File nextFile = imagenes.get(nextIndex);
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
            statusLabel.setText("Imagen " + (proyecto.getIndiceImagenActual() + 1) + " de " + proyecto.getListaImagenes().size());
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
        List<File> imagenes = new ArrayList<>(proyecto.getListaImagenes());

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
            statusLabel.setText("Procesamiento completado: " + imagenes.size() + " imágenes");
            btnCancel.setVisible(false);
        });
        processingTask.setOnCancelled(e -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            statusLabel.setText("Procesamiento cancelado por el usuario");
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

    private void updateWatermarkViews() {
        ImagenFlotante selected = proyecto.getMarcaSeleccionada();
        selectedWatermarkView = null;

        for (Map.Entry<ImagenFlotante, ImageView> entry : watermarkViews.entrySet()) {
            ImagenFlotante marca = entry.getKey();
            ImageView view = entry.getValue();
            
            boolean isSelected = marca.equals(selected);
            view.setVisible(true);
            view.setMouseTransparent(!isSelected); // Solo la seleccionada es interactiva

            if (isSelected) {
                selectedWatermarkView = view;
                view.setCursor(javafx.scene.Cursor.HAND);
            }

            updateWatermarkView(marca);
        }
    }

    private void updateWatermarkView(ImagenFlotante marca) {
        ImageView view = watermarkViews.get(marca);
        if (view == null || previewImage.getImage() == null) return;

        // Actualizar imagen de la marca
        view.setImage(FxImageUtils.toFxImage(marca.getImagenOriginal()));
        
        // Actualizar opacidad
        view.setOpacity(marca.getOpacidad());
        
        // Calcular la escala visual de la marca en relación a la imagen preview
        double imageWidth = previewImage.getBoundsInLocal().getWidth();
        if (imageWidth > 0 && marca.getImagenOriginal().getWidth() > 0) {
            // Factor de escala que relaciona el tamaño de la imagen preview con la original
            double previewScale = imageWidth / previewImage.getImage().getWidth();
            
            // Escala final: escala del usuario * escala de la preview
            double finalScale = marca.getEscala() * previewScale;
            
            view.setScaleX(finalScale);
            view.setScaleY(finalScale);
        }

        // Actualizar posición basada en el porcentaje almacenado
        positionWatermarkView(marca);
    }

    private void positionWatermarkView(ImagenFlotante marca) {
        ImageView view = watermarkViews.get(marca);
        if (view == null || previewImage.getImage() == null) return;

        // Obtener dimensiones del ImageView y de la Imagen
        double viewWidth = previewImage.getBoundsInLocal().getWidth();
        double viewHeight = previewImage.getBoundsInLocal().getHeight();
        double imageWidth = previewImage.getImage().getWidth();
        double imageHeight = previewImage.getImage().getHeight();

        if (viewWidth <= 0 || viewHeight <= 0 || imageWidth <= 0 || imageHeight <= 0) return;

        // Calcular las dimensiones reales de la imagen renderizada dentro del ImageView
        // considerando que preserveRatio está activo
        double imageRatio = imageWidth / imageHeight;
        double viewRatio = viewWidth / viewHeight;
        
        double actualImageWidth;
        double actualImageHeight;
        double offsetX = 0;
        double offsetY = 0;

        if (imageRatio > viewRatio) {
            // La imagen es más ancha proporcionalmente - se ajusta al ancho del view (letterbox vertical)
            actualImageWidth = viewWidth;
            actualImageHeight = viewWidth / imageRatio;
            offsetY = (viewHeight - actualImageHeight) / 2.0;
        } else {
            // La imagen es más alta proporcionalmente - se ajusta al alto del view (pillarbox horizontal)
            actualImageHeight = viewHeight;
            actualImageWidth = viewHeight * imageRatio;
            offsetX = (viewWidth - actualImageWidth) / 2.0;
        }

        // Calcular la posición del centro de la marca basándose en el porcentaje
        // El porcentaje se aplica sobre las dimensiones reales de la imagen renderizada
        double centerX = offsetX + (actualImageWidth * marca.getPosicionRelativa().x / 100.0);
        double centerY = offsetY + (actualImageHeight * marca.getPosicionRelativa().y / 100.0);

        // Obtener dimensiones de la marca de agua escalada
        double wmWidth = view.getImage().getWidth() * view.getScaleX();
        double wmHeight = view.getImage().getHeight() * view.getScaleY();
        
        // Posicionar la marca centrándola en el punto calculado
        view.setTranslateX(centerX - wmWidth / 2.0);
        view.setTranslateY(centerY - wmHeight / 2.0);
    }

    private void updateWatermarkPositionFromView(ImagenFlotante marca) {
        if (marca == null || selectedWatermarkView == null || previewImage.getImage() == null) return;

        // Obtener dimensiones del ImageView y de la Imagen
        double viewWidth = previewImage.getBoundsInLocal().getWidth();
        double viewHeight = previewImage.getBoundsInLocal().getHeight();
        double imageWidth = previewImage.getImage().getWidth();
        double imageHeight = previewImage.getImage().getHeight();

        if (viewWidth <= 0 || viewHeight <= 0 || imageWidth <= 0 || imageHeight <= 0) return;

        // Calcular las dimensiones reales de la imagen renderizada y sus offsets
        double imageRatio = imageWidth / imageHeight;
        double viewRatio = viewWidth / viewHeight;
        
        double actualImageWidth;
        double actualImageHeight;
        double offsetX = 0;
        double offsetY = 0;

        if (imageRatio > viewRatio) {
            // La imagen es más ancha proporcionalmente - letterbox vertical
            actualImageWidth = viewWidth;
            actualImageHeight = viewWidth / imageRatio;
            offsetY = (viewHeight - actualImageHeight) / 2.0;
        } else {
            // La imagen es más alta proporcionalmente - pillarbox horizontal
            actualImageHeight = viewHeight;
            actualImageWidth = viewHeight * imageRatio;
            offsetX = (viewWidth - actualImageWidth) / 2.0;
        }

        // Obtener el centro actual de la marca de agua en coordenadas de la capa
        double wmWidth = selectedWatermarkView.getImage().getWidth() * selectedWatermarkView.getScaleX();
        double wmHeight = selectedWatermarkView.getImage().getHeight() * selectedWatermarkView.getScaleY();
        double centerX = selectedWatermarkView.getTranslateX() + wmWidth / 2.0;
        double centerY = selectedWatermarkView.getTranslateY() + wmHeight / 2.0;

        // Convertir a porcentaje relativo a la imagen real renderizada
        // No limitamos aquí - permitimos valores fuera de 0-100 para arrastre libre
        double percentX = ((centerX - offsetX) / actualImageWidth) * 100.0;
        double percentY = ((centerY - offsetY) / actualImageHeight) * 100.0;

        // Redondear para precisión pero sin limitar el rango
        int finalPercentX = (int) Math.round(percentX);
        int finalPercentY = (int) Math.round(percentY);
        
        // Limitar solo al guardar en el modelo para evitar valores extremos
        // pero permitir cierta libertad fuera del rango visual
        finalPercentX = Math.max(-50, Math.min(150, finalPercentX));
        finalPercentY = Math.max(-50, Math.min(150, finalPercentY));

        // Actualizar el modelo
        marca.setPosicionRelativa(finalPercentX, finalPercentY);
        
        // Mostrar feedback al usuario
        String posInfo = String.format("Posición: %d%%, %d%%", finalPercentX, finalPercentY);
        if (finalPercentX < 0 || finalPercentX > 100 || finalPercentY < 0 || finalPercentY > 100) {
            posInfo += " (Fuera del área visible)";
        }
        statusLabel.setText(posInfo);
    }
}
