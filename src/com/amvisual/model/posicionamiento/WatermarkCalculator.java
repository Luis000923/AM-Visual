// Este archivo es el núcleo del sistema de posicionamiento de marcas de agua.
// Contiene la lógica para calcular las dimensiones y coordenadas de la marca de agua
// en una imagen. Se conecta con `WatermarkConfig` para almacenar la configuración
// y es utilizado por `WatermarkPositioner` como parte de la API pública.
package com.amvisual.model.posicionamiento;

public final class WatermarkCalculator {
    
    /**
     * MARGEN BASE: Porcentaje mínimo del borde de la imagen que se reserva como margen.
     * Este es el margen base que se aplica a todas las imágenes.
     */
    private static final double BASE_MARGIN_PERCENTAGE = 0.015; // 1.5% de margen base
    
    /**
     * MARGEN ADAPTATIVO: Factor adicional que se calcula según el tamaño de la imagen.
     * Imágenes más grandes tendrán márgenes proporcionalmente más pequeños.
     */
    private static final double ADAPTIVE_MARGIN_FACTOR = 0.005; // 0.5% adicional adaptativo
    
    /**
     * TAMAÑO MÁXIMO: Porcentaje máximo que puede ocupar la marca de agua.
     * Se ajusta dinámicamente según la imagen.
     */
    private static final double BASE_MAX_SIZE_PERCENTAGE = 0.85; // 85% del tamaño máximo base
    
    /**
     * UMBRAL DE IMAGEN PEQUEÑA: Imágenes con dimensiones menores a este umbral
     * se consideran pequeñas y tendrán márgenes más generosos.
     */
    private static final double SMALL_IMAGE_THRESHOLD = 800.0; // píxeles
    
    /**
     * UMBRAL DE IMAGEN GRANDE: Imágenes con dimensiones mayores a este umbral
     * se consideran grandes y tendrán márgenes más optimizados.
     */
    private static final double LARGE_IMAGE_THRESHOLD = 2000.0; // píxeles
    
    // Constructor privado para evitar instanciación (clase de utilidad)
    private WatermarkCalculator() {
        throw new AssertionError("Clase de utilidad no instanciable");
    }
    
    /**
     * Calcula los bordes adaptativos según las dimensiones específicas de la imagen.
     * 
     * ALGORITMO ADAPTATIVO:
     * - Imágenes pequeñas (< 800px): Márgenes más generosos para evitar que la marca se vea muy pegada
     * - Imágenes medianas (800-2000px): Márgenes balanceados
     * - Imágenes grandes (> 2000px): Márgenes optimizados para aprovechar mejor el espacio
     * 
     * También considera el aspect ratio para ajustar los márgenes independientemente en X e Y.
     * 
     * @param imageWidth Ancho de la imagen en píxeles
     * @param imageHeight Alto de la imagen en píxeles
     * @return Configuración de bordes adaptativa para esta imagen específica
     */
    private static AdaptiveBounds calculateAdaptiveBounds(double imageWidth, double imageHeight) {
        // Calcular la dimensión principal para clasificar el tamaño de la imagen
        double maxDimension = Math.max(imageWidth, imageHeight);
        double minDimension = Math.min(imageWidth, imageHeight);
        double aspectRatio = maxDimension / minDimension;
        
        // FACTOR DE TAMAÑO: Ajusta los márgenes según el tamaño total de la imagen
        double sizeFactorX, sizeFactorY;
        double maxSizePercentage;
        
        if (maxDimension < SMALL_IMAGE_THRESHOLD) {
            // Imágenes pequeñas: márgenes más generosos
            sizeFactorX = 1.5; // 50% más margen
            sizeFactorY = 1.5;
            maxSizePercentage = 0.75; // Máximo 75% para evitar que se vea muy grande
        } else if (maxDimension > LARGE_IMAGE_THRESHOLD) {
            // Imágenes grandes: márgenes optimizados
            sizeFactorX = 0.7; // 30% menos margen
            sizeFactorY = 0.7;
            maxSizePercentage = 0.90; // Puede usar hasta 90% del espacio
        } else {
            // Imágenes medianas: márgenes balanceados
            sizeFactorX = 1.0; // Margen base
            sizeFactorY = 1.0;
            maxSizePercentage = BASE_MAX_SIZE_PERCENTAGE;
        }
        
        // FACTOR DE ASPECT RATIO: Ajusta márgenes según la forma de la imagen
        if (aspectRatio > 2.0) {
            // Imágenes muy panorámicas: reducir margen en la dimensión larga
            if (imageWidth > imageHeight) {
                sizeFactorX *= 0.8; // Menos margen horizontal
            } else {
                sizeFactorY *= 0.8; // Menos margen vertical
            }
        } else if (aspectRatio < 1.2) {
            // Imágenes casi cuadradas: márgenes uniformes más pequeños
            sizeFactorX *= 0.9;
            sizeFactorY *= 0.9;
        }
        
        // Calcular márgenes finales en píxeles
        double baseMarginX = imageWidth * BASE_MARGIN_PERCENTAGE;
        double baseMarginY = imageHeight * BASE_MARGIN_PERCENTAGE;
        
        double adaptiveMarginX = imageWidth * ADAPTIVE_MARGIN_FACTOR;
        double adaptiveMarginY = imageHeight * ADAPTIVE_MARGIN_FACTOR;
        
        double finalMarginX = (baseMarginX + adaptiveMarginX) * sizeFactorX;
        double finalMarginY = (baseMarginY + adaptiveMarginY) * sizeFactorY;
        
        // Asegurar márgenes mínimos absolutos (al menos 5 píxeles)
        finalMarginX = Math.max(finalMarginX, 5.0);
        finalMarginY = Math.max(finalMarginY, 5.0);
        
        return new AdaptiveBounds(finalMarginX, finalMarginY, maxSizePercentage, 
                                 imageWidth, imageHeight);
    }
    
    /**
     * Calcula la configuración relativa desde coordenadas absolutas.
     * 
     * Este es el MÉTODO PRINCIPAL del algoritmo de posicionamiento.
     * Convierte coordenadas absolutas (píxeles) en proporciones relativas
     * que se pueden aplicar a cualquier imagen.
     * 
     * MEJORA v6.1: Algoritmo mejorado para imágenes horizontales
     * - Usa el ancho como base para imágenes horizontales (aspect ratio > 1.2)
     * - Usa el alto como base para imágenes verticales (aspect ratio < 0.83)
     * - Usa la dimensión mínima para imágenes casi cuadradas
     * 
     * @param imageWidth Ancho de la imagen de referencia en píxeles
     * @param imageHeight Alto de la imagen de referencia en píxeles
     * @param watermarkX Posición X absoluta de la marca (esquina superior izquierda)
     * @param watermarkY Posición Y absoluta de la marca (esquina superior izquierda)
     * @param watermarkWidth Ancho absoluto de la marca en píxeles
     * @param watermarkHeight Alto absoluto de la marca en píxeles
     * 
     * @return Configuración relativa inmutable
     * @throws IllegalArgumentException si las dimensiones son inválidas
     */
    public static WatermarkConfig calculateRelativeConfig(
            double imageWidth, double imageHeight,
            double watermarkX, double watermarkY,
            double watermarkWidth, double watermarkHeight) {
        
        // Validación de parámetros
        validateImageDimensions(imageWidth, imageHeight, "imagen de referencia");
        validateWatermarkDimensions(watermarkWidth, watermarkHeight);
        validatePosition(watermarkX, watermarkY);
        
        // PASO 1: Calcular posiciones relativas
        // La posición X es relativa al ancho, la Y al alto
        double relativeX = watermarkX / imageWidth;
        double relativeY = watermarkY / imageHeight;
        
        // PASO 2: Calcular dimensión base inteligente según orientación
        // Esto garantiza consistencia visual en diferentes aspect ratios
        double baseDimension = calculateSmartBaseDimension(imageWidth, imageHeight);
        
        // PASO 3: Calcular ancho relativo respecto a la dimensión base
        double relativeWidth = watermarkWidth / baseDimension;
        
        // PASO 4: Calcular y preservar la relación de aspecto
        double aspectRatio = watermarkHeight / watermarkWidth;
        
        return new WatermarkConfig(relativeX, relativeY, relativeWidth, aspectRatio);
    }
    
    /**
     * Calcula la dimensión base inteligente según la orientación de la imagen.
     * 
     * ALGORITMO MEJORADO (v6.1):
     * - Imágenes horizontales (ratio > 1.2): usa el ancho como base
     * - Imágenes verticales (ratio < 0.83): usa el alto como base
     * - Imágenes cuadradas (0.83 <= ratio <= 1.2): usa la dimensión mínima
     * 
     * Esto mejora significativamente el posicionamiento en imágenes horizontales.
     * 
     * @param width Ancho de la imagen
     * @param height Alto de la imagen
     * @return Dimensión base calculada
     */
    private static double calculateSmartBaseDimension(double width, double height) {
        double aspectRatio = width / height;
        
        if (aspectRatio > 1.2) {
            // Imagen horizontal: usa el ancho como referencia
            return width;
        } else if (aspectRatio < 0.83) {
            // Imagen vertical: usa el alto como referencia
            return height;
        } else {
            // Imagen casi cuadrada: usa la dimensión mínima (comportamiento original)
            return Math.min(width, height);
        }
    }
    
    /**
     * Aplica una configuración relativa a una imagen destino específica.
     * 
     * Convierte las proporciones relativas de vuelta a coordenadas absolutas
     * para la imagen destino.
     * 
     * MEJORA v6.1: Validación de límites y ajuste automático para evitar
     * que la marca de agua se salga de la imagen.
     * 
     * @param config Configuración relativa a aplicar
     * @param targetWidth Ancho de la imagen destino
     * @param targetHeight Alto de la imagen destino
     * 
     * @return Dimensiones absolutas calculadas y ajustadas a los límites
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    public static AbsoluteDimensions applyToTarget(
            WatermarkConfig config, double targetWidth, double targetHeight) {
        
        if (config == null) {
            throw new IllegalArgumentException("config no puede ser null");
        }
        if (!config.isValid()) {
            throw new IllegalArgumentException("config no es válida para renderizado");
        }
        validateImageDimensions(targetWidth, targetHeight, "imagen destino");
        
        // PASO 1: Calcular dimensión base inteligente de la imagen destino
        double targetBaseDimension = calculateSmartBaseDimension(targetWidth, targetHeight);
        
        // PASO 2: Calcular ancho absoluto desde el ancho relativo
        double absoluteWidth = config.getRelativeWidth() * targetBaseDimension;
        
        // PASO 3: Calcular alto absoluto usando el aspect ratio
        double absoluteHeight = absoluteWidth * config.getAspectRatio();
        
        // PASO 4: Calcular posiciones absolutas
        double absoluteX = config.getRelativeX() * targetWidth;
        double absoluteY = config.getRelativeY() * targetHeight;
        
        // PASO 5: Ajustar a los límites de la imagen (MEJORA v6.1)
        AbsoluteDimensions dimensions = new AbsoluteDimensions(
            absoluteX, absoluteY, absoluteWidth, absoluteHeight
        );
        
        return constrainToBounds(dimensions, targetWidth, targetHeight);
    }
    
    /**
     * Ajusta las dimensiones para que la marca de agua no se salga de los límites.
     * 
     * ALGORITMO CON MÁRGENES DE SEGURIDAD:
     * 1. Calcula el área útil (imagen menos márgenes)
     * 2. Verifica si la marca cabe en el área útil
     * 3. Si es muy grande, la reduce manteniendo el aspect ratio
     * 4. Ajusta la posición para que quede dentro del área útil
     * 5. Garantiza que NUNCA toque los bordes
     * 
     * MEJORA v6.2: Sistema de márgenes configurables que aseguran que las marcas
     * nunca toquen los bordes de la imagen, creando un borde visual limpio.
     * 
     * @param dimensions Dimensiones calculadas originalmente
     * @param imageWidth Ancho de la imagen contenedora
     * @param imageHeight Alto de la imagen contenedora
     * @return Dimensiones ajustadas con márgenes de seguridad
     */
    private static AbsoluteDimensions constrainToBounds(
            AbsoluteDimensions dimensions, double imageWidth, double imageHeight) {
        
        double x = dimensions.getX();
        double y = dimensions.getY();
        double width = dimensions.getWidth();
        double height = dimensions.getHeight();
        
        // PASO 1: Calcular márgenes adaptativos según el tamaño de la imagen
        AdaptiveBounds bounds = calculateAdaptiveBounds(imageWidth, imageHeight);
        double marginX = bounds.getMarginX();
        double marginY = bounds.getMarginY();
        
        // PASO 2: Calcular área útil (imagen menos márgenes adaptativos)
        double usableWidth = imageWidth - (2 * marginX);
        double usableHeight = imageHeight - (2 * marginY);
        
        // PASO 3: Calcular tamaño máximo permitido dentro del área útil
        double maxWidth = usableWidth * bounds.getMaxSizePercentage();
        double maxHeight = usableHeight * bounds.getMaxSizePercentage();
        
        // PASO 4: Si la marca es más grande que el área permitida, reducirla
        if (width > maxWidth || height > maxHeight) {
            // Calcular factor de reducción que preserva el aspect ratio
            double widthScale = maxWidth / width;
            double heightScale = maxHeight / height;
            double scaleFactor = Math.min(widthScale, heightScale);
            
            width *= scaleFactor;
            height *= scaleFactor;
            
            System.out.println(String.format(
                "Marca reducida a %.0f%% para ajustarse al área útil (%.0fx%.0f)",
                scaleFactor * 100, width, height));
        }
        
        // PASO 5: Ajustar posición para respetar márgenes
        // Posición mínima = margen
        // Posición máxima = imagen - margen - tamaño de marca
        double minX = marginX;
        double minY = marginY;
        double maxX = imageWidth - marginX - width;
        double maxY = imageHeight - marginY - height;
        
        // PASO 6: Aplicar restricciones
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
        
        // PASO 7: Verificación de seguridad adicional
        // Si la marca aún no cabe (casos extremos), centrarla y reducirla más
        if (x < minX || x > maxX || y < minY || y > maxY) {
            System.err.println("Advertencia: Ajuste extremo necesario. Centrando marca de agua.");
            
            // Reducir más si es necesario
            if (width > usableWidth) {
                double ratio = height / width;
                width = usableWidth * 0.8; // 80% del área útil
                height = width * ratio;
            }
            if (height > usableHeight) {
                double ratio = width / height;
                height = usableHeight * 0.8; // 80% del área útil
                width = height * ratio;
            }
            
            // Centrar en el área útil
            x = marginX + (usableWidth - width) / 2.0;
            y = marginY + (usableHeight - height) / 2.0;
        }
        
        // PASO 8: Validación final
        double finalRightEdge = x + width;
        double finalBottomEdge = y + height;
        
        if (x < marginX || y < marginY || 
            finalRightEdge > (imageWidth - marginX) || 
            finalBottomEdge > (imageHeight - marginY)) {
            
            System.err.println(String.format(
                "ADVERTENCIA: Marca fuera de márgenes. Pos:(%.1f,%.1f) Tamaño:(%.1fx%.1f) " +
                "Límites:(%.1f,%.1f)-(%.1f,%.1f)",
                x, y, width, height,
                marginX, marginY, imageWidth - marginX, imageHeight - marginY));
        }
        
        return new AbsoluteDimensions(x, y, width, height);
    }
    
    /**
     * Calcula las dimensiones base de una imagen.
     */
    public static double calculateBaseDimension(double width, double height) {
        validateImageDimensions(width, height, "imagen");
        return calculateSmartBaseDimension(width, height);
    }
    
    /**
     * Obtiene el margen de seguridad base como porcentaje.
     * Por ejemplo, 0.015 = 1.5% de margen base en cada borde.
     * 
     * @return El porcentaje de margen de seguridad base
     */
    public static double getBaseSafetyMarginPercentage() {
        return BASE_MARGIN_PERCENTAGE;
    }
    
    /**
     * Calcula el margen efectivo para una imagen específica.
     * 
     * @param imageWidth Ancho de la imagen
     * @param imageHeight Alto de la imagen
     * @return El margen efectivo como porcentaje de las dimensiones
     */
    public static double getEffectiveMarginPercentage(double imageWidth, double imageHeight) {
        AdaptiveBounds bounds = calculateAdaptiveBounds(imageWidth, imageHeight);
        // Devolver el promedio de los márgenes como porcentaje
        double avgMargin = (bounds.getMarginX() / imageWidth + bounds.getMarginY() / imageHeight) / 2.0;
        return avgMargin;
    }
    
    /**
     * Calcula los márgenes adaptativos en píxeles para una imagen específica.
     * 
     * @param imageWidth Ancho de la imagen
     * @param imageHeight Alto de la imagen
     * @return Array con [marginX, marginY] en píxeles calculados adaptativamente
     */
    public static double[] calculateMargins(double imageWidth, double imageHeight) {
        validateImageDimensions(imageWidth, imageHeight, "imagen");
        AdaptiveBounds bounds = calculateAdaptiveBounds(imageWidth, imageHeight);
        return new double[] {
            bounds.getMarginX(),
            bounds.getMarginY()
        };
    }
    
    /**
     * Calcula el área útil adaptativa (sin márgenes) de una imagen.
     * 
     * @param imageWidth Ancho de la imagen
     * @param imageHeight Alto de la imagen
     * @return Array con [usableWidth, usableHeight] en píxeles calculados adaptativamente
     */
    public static double[] calculateUsableArea(double imageWidth, double imageHeight) {
        validateImageDimensions(imageWidth, imageHeight, "imagen");
        AdaptiveBounds bounds = calculateAdaptiveBounds(imageWidth, imageHeight);
        return new double[] {
            imageWidth - (2 * bounds.getMarginX()),
            imageHeight - (2 * bounds.getMarginY())
        };
    }
    
    /**
     * Calcula el aspect ratio de unas dimensiones.
     */
    public static double calculateAspectRatio(double width, double height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width debe ser positivo");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height debe ser positivo");
        }
        return height / width;
    }
    
    /**
     * Obtiene información detallada sobre los bordes adaptativos para una imagen.
     * 
     * @param imageWidth Ancho de la imagen
     * @param imageHeight Alto de la imagen
     * @return Información completa sobre los bordes calculados
     */
    public static String getAdaptiveBoundsInfo(double imageWidth, double imageHeight) {
        validateImageDimensions(imageWidth, imageHeight, "imagen");
        
        AdaptiveBounds bounds = calculateAdaptiveBounds(imageWidth, imageHeight);
        double maxDimension = Math.max(imageWidth, imageHeight);
        double aspectRatio = imageWidth / imageHeight;
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Información de Bordes Adaptativos ===\n");
        sb.append(String.format("Imagen: %.0f x %.0f px\n", imageWidth, imageHeight));
        sb.append(String.format("Aspect ratio: %.3f (%s)\n", aspectRatio, getOrientationName(imageWidth, imageHeight)));
        sb.append(String.format("Dimensión mayor: %.0f px ", maxDimension));
        
        if (maxDimension < SMALL_IMAGE_THRESHOLD) {
            sb.append("(PEQUEÑA - márgenes generosos)\n");
        } else if (maxDimension > LARGE_IMAGE_THRESHOLD) {
            sb.append("(GRANDE - márgenes optimizados)\n");
        } else {
            sb.append("(MEDIANA - márgenes balanceados)\n");
        }
        
        sb.append(String.format("\nMárgenes calculados:\n"));
        sb.append(String.format("  Horizontal: %.1f px (%.2f%%)\n", 
                  bounds.getMarginX(), (bounds.getMarginX() / imageWidth) * 100));
        sb.append(String.format("  Vertical: %.1f px (%.2f%%)\n", 
                  bounds.getMarginY(), (bounds.getMarginY() / imageHeight) * 100));
        
        sb.append(String.format("\nÁrea de trabajo:\n"));
        sb.append(String.format("  Área total: %.0f x %.0f = %.0f px²\n", 
                  imageWidth, imageHeight, imageWidth * imageHeight));
        sb.append(String.format("  Área útil: %.0f x %.0f = %.0f px²\n", 
                  bounds.getUsableWidth(), bounds.getUsableHeight(), 
                  bounds.getUsableWidth() * bounds.getUsableHeight()));
        sb.append(String.format("  Eficiencia: %.1f%%\n", 
                  (bounds.getUsableWidth() * bounds.getUsableHeight()) / 
                  (imageWidth * imageHeight) * 100));
        
        sb.append(String.format("\nLimitaciones de marca de agua:\n"));
        sb.append(String.format("  Tamaño máximo permitido: %.1f%% del área útil\n", 
                  bounds.getMaxSizePercentage() * 100));
        sb.append(String.format("  Dimensiones máximas: %.0f x %.0f px\n",
                  bounds.getUsableWidth() * bounds.getMaxSizePercentage(),
                  bounds.getUsableHeight() * bounds.getMaxSizePercentage()));
        
        return sb.toString();
    }
    
    /**
     * Método de demostración: Compara el sistema anterior vs el nuevo sistema adaptativo.
     * 
     * @param imageWidth Ancho de la imagen
     * @param imageHeight Alto de la imagen
     * @return Comparación detallada entre ambos sistemas
     */
    public static String compareMarginSystems(double imageWidth, double imageHeight) {
        validateImageDimensions(imageWidth, imageHeight, "imagen");
        
        // Sistema anterior (margen fijo)
        double oldMarginPercent = 0.02; // El antiguo SAFETY_MARGIN_PERCENTAGE
        double oldMarginX = imageWidth * oldMarginPercent;
        double oldMarginY = imageHeight * oldMarginPercent;
        double oldUsableWidth = imageWidth - (2 * oldMarginX);
        double oldUsableHeight = imageHeight - (2 * oldMarginY);
        
        // Sistema nuevo (adaptativo)
        AdaptiveBounds bounds = calculateAdaptiveBounds(imageWidth, imageHeight);
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Comparación de Sistemas de Márgenes ===\n");
        sb.append(String.format("Imagen: %.0f x %.0f px\n\n", imageWidth, imageHeight));
        
        sb.append("SISTEMA ANTERIOR (Margen fijo 2%):\n");
        sb.append(String.format("  Márgenes: %.1f x %.1f px\n", oldMarginX, oldMarginY));
        sb.append(String.format("  Área útil: %.0f x %.0f px (%.0f px²)\n", 
                  oldUsableWidth, oldUsableHeight, oldUsableWidth * oldUsableHeight));
        sb.append(String.format("  Eficiencia: %.1f%%\n\n", 
                  (oldUsableWidth * oldUsableHeight) / (imageWidth * imageHeight) * 100));
        
        sb.append("SISTEMA NUEVO (Adaptativo):\n");
        sb.append(String.format("  Márgenes: %.1f x %.1f px\n", 
                  bounds.getMarginX(), bounds.getMarginY()));
        sb.append(String.format("  Área útil: %.0f x %.0f px (%.0f px²)\n", 
                  bounds.getUsableWidth(), bounds.getUsableHeight(),
                  bounds.getUsableWidth() * bounds.getUsableHeight()));
        sb.append(String.format("  Eficiencia: %.1f%%\n", 
                  (bounds.getUsableWidth() * bounds.getUsableHeight()) / (imageWidth * imageHeight) * 100));
        sb.append(String.format("  Tamaño máx permitido: %.1f%%\n\n", 
                  bounds.getMaxSizePercentage() * 100));
        
        // Calcular mejora
        double oldArea = oldUsableWidth * oldUsableHeight;
        double newArea = bounds.getUsableWidth() * bounds.getUsableHeight();
        double improvement = ((newArea - oldArea) / oldArea) * 100;
        
        sb.append("MEJORA:\n");
        if (improvement > 0) {
            sb.append(String.format("  +%.1f%% más área útil disponible\n", improvement));
        } else if (improvement < 0) {
            sb.append(String.format("  %.1f%% menos área (márgenes más conservadores)\n", Math.abs(improvement)));
        } else {
            sb.append("  Área prácticamente igual\n");
        }
        
        double marginReduction = ((oldMarginX + oldMarginY) - (bounds.getMarginX() + bounds.getMarginY())) 
                                / (oldMarginX + oldMarginY) * 100;
        sb.append(String.format("  Ajuste de márgenes: %.1f%%\n", marginReduction));
        
        return sb.toString();
    }
    
    /**
     * Método de diagnóstico: verifica si unas dimensiones absolutas están dentro de los límites.
     * 
     * @param dimensions Dimensiones a verificar
     * @param imageWidth Ancho de la imagen contenedora
     * @param imageHeight Alto de la imagen contenedora
     * @return true si está completamente dentro de los límites
     */
    public static boolean isWithinBounds(
            AbsoluteDimensions dimensions, double imageWidth, double imageHeight) {
        
        if (dimensions == null || !dimensions.isValid()) {
            return false;
        }
        
        double x = dimensions.getX();
        double y = dimensions.getY();
        double width = dimensions.getWidth();
        double height = dimensions.getHeight();
        
        return x >= 0 && y >= 0 
            && (x + width) <= imageWidth 
            && (y + height) <= imageHeight;
    }
    
    /**
     * Método de diagnóstico: calcula información detallada sobre el posicionamiento.
     * 
     * @param config Configuración a analizar
     * @param targetWidth Ancho de la imagen destino
     * @param targetHeight Alto de la imagen destino
     * @return String con información de diagnóstico
     */
    public static String getDiagnosticInfo(
            WatermarkConfig config, double targetWidth, double targetHeight) {
        
        if (config == null) {
            return "Config es null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Diagnóstico de Posicionamiento ===\n");
        sb.append(String.format("Imagen destino: %.0f x %.0f px\n", targetWidth, targetHeight));
        sb.append(String.format("Aspect ratio destino: %.3f (%s)\n", 
            targetWidth / targetHeight,
            getOrientationName(targetWidth, targetHeight)));
        sb.append(String.format("\nConfig relativa:\n"));
        sb.append(String.format("  X: %.4f (%.1f%%)\n", config.getRelativeX(), config.getRelativeX() * 100));
        sb.append(String.format("  Y: %.4f (%.1f%%)\n", config.getRelativeY(), config.getRelativeY() * 100));
        sb.append(String.format("  Width: %.4f\n", config.getRelativeWidth()));
        sb.append(String.format("  Aspect Ratio: %.4f\n", config.getAspectRatio()));
        
        try {
            AbsoluteDimensions dims = applyToTarget(config, targetWidth, targetHeight);
            double[] margins = calculateMargins(targetWidth, targetHeight);
            double[] usableArea = calculateUsableArea(targetWidth, targetHeight);
            
            AdaptiveBounds bounds = calculateAdaptiveBounds(targetWidth, targetHeight);
            double effectiveMarginPercent = getEffectiveMarginPercentage(targetWidth, targetHeight);
            
            sb.append(String.format("\nMárgenes adaptativos:\n"));
            sb.append(String.format("  Porcentaje base: %.1f%%\n", BASE_MARGIN_PERCENTAGE * 100));
            sb.append(String.format("  Porcentaje efectivo: %.1f%%\n", effectiveMarginPercent * 100));
            sb.append(String.format("  Margen horizontal: %.1f px\n", margins[0]));
            sb.append(String.format("  Margen vertical: %.1f px\n", margins[1]));
            sb.append(String.format("  Área útil: %.0f x %.0f px\n", usableArea[0], usableArea[1]));
            sb.append(String.format("  Tamaño máximo permitido: %.1f%%\n", bounds.getMaxSizePercentage() * 100));
            
            sb.append(String.format("\nDimensiones absolutas calculadas:\n"));
            sb.append(String.format("  Posición: (%.1f, %.1f)\n", dims.getX(), dims.getY()));
            sb.append(String.format("  Tamaño: %.1f x %.1f px\n", dims.getWidth(), dims.getHeight()));
            sb.append(String.format("  Borde derecho: %.1f (límite: %.0f, max con margen: %.0f)\n", 
                dims.getX() + dims.getWidth(), targetWidth, targetWidth - margins[0]));
            sb.append(String.format("  Borde inferior: %.1f (límite: %.0f, max con margen: %.0f)\n", 
                dims.getY() + dims.getHeight(), targetHeight, targetHeight - margins[1]));
            
            // Verificar si está dentro de los márgenes
            boolean withinMargins = dims.getX() >= margins[0] && 
                                   dims.getY() >= margins[1] &&
                                   (dims.getX() + dims.getWidth()) <= (targetWidth - margins[0]) &&
                                   (dims.getY() + dims.getHeight()) <= (targetHeight - margins[1]);
            
            sb.append(String.format("  Dentro de límites básicos: %s\n", 
                isWithinBounds(dims, targetWidth, targetHeight) ? "SÍ" : "NO"));
            sb.append(String.format("  Respeta márgenes de seguridad: %s\n", 
                withinMargins ? "SÍ" : "NO"));
            
            double baseDim = calculateSmartBaseDimension(targetWidth, targetHeight);
            sb.append(String.format("\nDimensión base usada: %.1f px\n", baseDim));
            sb.append(String.format("Porcentaje de la imagen: %.1f%%\n", 
                (dims.getWidth() / targetWidth) * 100));
            
        } catch (Exception e) {
            sb.append("\nError al calcular dimensiones: ").append(e.getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * Obtiene el nombre de la orientación basado en las dimensiones.
     */
    private static String getOrientationName(double width, double height) {
        double ratio = width / height;
        if (ratio > 1.2) return "Horizontal";
        if (ratio < 0.83) return "Vertical";
        return "Cuadrada";
    }
    
    // ==================== Métodos de Validación ====================
    
    private static void validateImageDimensions(double width, double height, String context) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                String.format("Dimensiones de %s inválidas: width=%.2f, height=%.2f", 
                            context, width, height));
        }
        if (Double.isNaN(width) || Double.isNaN(height)) {
            throw new IllegalArgumentException("Las dimensiones no pueden ser NaN");
        }
        if (Double.isInfinite(width) || Double.isInfinite(height)) {
            throw new IllegalArgumentException("Las dimensiones no pueden ser infinitas");
        }
    }
    
    private static void validateWatermarkDimensions(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                String.format("Dimensiones de marca de agua inválidas: width=%.2f, height=%.2f", 
                            width, height));
        }
    }
    
    private static void validatePosition(double x, double y) {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new IllegalArgumentException("La posición no puede ser NaN");
        }
        if (Double.isInfinite(x) || Double.isInfinite(y)) {
            throw new IllegalArgumentException("La posición no puede ser infinita");
        }
    }
    
    // ==================== Clases Internas ====================
    
    /**
     * Configuración de bordes adaptativos para una imagen específica.
     * 
     * Esta clase encapsula todos los parámetros calculados para crear bordes
     * que se adapten perfectamente al tamaño y forma de cada imagen individual.
     */
    private static final class AdaptiveBounds {
        private final double marginX;
        private final double marginY;
        private final double maxSizePercentage;
        private final double imageWidth;
        private final double imageHeight;
        
        public AdaptiveBounds(double marginX, double marginY, double maxSizePercentage, 
                            double imageWidth, double imageHeight) {
            this.marginX = marginX;
            this.marginY = marginY;
            this.maxSizePercentage = maxSizePercentage;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }
        
        public double getMarginX() { return marginX; }
        public double getMarginY() { return marginY; }
        public double getMaxSizePercentage() { return maxSizePercentage; }
        public double getImageWidth() { return imageWidth; }
        public double getImageHeight() { return imageHeight; }
        
        /**
         * Calcula el área útil (sin márgenes) de la imagen.
         */
        public double getUsableWidth() {
            return imageWidth - (2 * marginX);
        }
        
        public double getUsableHeight() {
            return imageHeight - (2 * marginY);
        }
        
        /**
         * Verifica si una marca de agua cabe dentro de los límites con estos bordes.
         */
        public boolean canFitWatermark(double watermarkWidth, double watermarkHeight) {
            return watermarkWidth <= getUsableWidth() * maxSizePercentage &&
                   watermarkHeight <= getUsableHeight() * maxSizePercentage;
        }
        
        @Override
        public String toString() {
            return String.format("AdaptiveBounds[marginX=%.1f, marginY=%.1f, maxSize=%.1f%%, " +
                               "usableArea=%.0fx%.0f]",
                               marginX, marginY, maxSizePercentage * 100,
                               getUsableWidth(), getUsableHeight());
        }
    }
    
    /**
     * Contenedor inmutable para dimensiones absolutas calculadas.
     */
    public static final class AbsoluteDimensions {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        
        public AbsoluteDimensions(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        
        /**
         * Verifica si estas dimensiones son válidas para renderizado.
         */
        public boolean isValid() {
            return width > 0 && height > 0 
                && !Double.isNaN(x) && !Double.isNaN(y)
                && !Double.isInfinite(width) && !Double.isInfinite(height);
        }
        
        @Override
        public String toString() {
            return String.format("Dimensions[x=%.2f, y=%.2f, w=%.2f, h=%.2f]",
                               x, y, width, height);
        }
    }
}
