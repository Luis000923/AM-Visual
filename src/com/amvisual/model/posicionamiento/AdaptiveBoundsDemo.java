// Este archivo sirve para demostrar el funcionamiento del sistema de posicionamiento de marcas de agua.
// No se conecta con ninguna otra parte principal de la aplicación, es solo para pruebas.
package com.amvisual.model.posicionamiento;

public class AdaptiveBoundsDemo {
    
    /**
     * Ejecuta una demostración del sistema adaptativo con diferentes tipos de imágenes.
     */
    public static void main(String[] args) {
        System.out.println("=== DEMOSTRACIÓN DEL SISTEMA DE BORDES ADAPTATIVOS ===\n");
        
        // Diferentes tipos de imágenes para probar
        double[][] testImages = {
            {600, 400},    // Imagen pequeña horizontal
            {400, 600},    // Imagen pequeña vertical
            {500, 500},    // Imagen pequeña cuadrada
            {1920, 1080},  // Imagen mediana horizontal (Full HD)
            {1080, 1920},  // Imagen mediana vertical (móvil)
            {1500, 1500},  // Imagen mediana cuadrada
            {4000, 2000},  // Imagen grande panorámica
            {3000, 4000},  // Imagen grande vertical (retrato)
            {6000, 4000},  // Imagen muy grande horizontal
            {800, 3200}    // Imagen muy vertical (banner)
        };
        
        for (int i = 0; i < testImages.length; i++) {
            double width = testImages[i][0];
            double height = testImages[i][1];
            
            System.out.println("CASO " + (i + 1) + ":");
            System.out.println(WatermarkCalculator.getAdaptiveBoundsInfo(width, height));
            System.out.println(WatermarkCalculator.compareMarginSystems(width, height));
            System.out.println("=" + "=".repeat(60) + "\n");
        }
        
        // Demostración con una configuración específica
        demonstrateWatermarkPositioning();
    }
    
    /**
     * Demuestra cómo se posiciona una marca de agua con el sistema adaptativo.
     */
    private static void demonstrateWatermarkPositioning() {
        System.out.println("=== DEMOSTRACIÓN DE POSICIONAMIENTO ESPECÍFICO ===\n");
        
        // Imagen de referencia (donde se diseña la marca)
        double refWidth = 1000;
        double refHeight = 800;
        
        // Marca de agua (esquina superior derecha)
        double watermarkX = 750;  // 75% a la derecha
        double watermarkY = 50;   // 50px desde arriba
        double watermarkWidth = 200;
        double watermarkHeight = 100;
        
        System.out.println("1. CONFIGURACIÓN INICIAL:");
        System.out.printf("   Imagen de referencia: %.0fx%.0f px\n", refWidth, refHeight);
        System.out.printf("   Marca de agua: posición (%.0f, %.0f), tamaño %.0fx%.0f px\n\n",
                         watermarkX, watermarkY, watermarkWidth, watermarkHeight);
        
        // Calcular configuración relativa
        WatermarkConfig config = WatermarkCalculator.calculateRelativeConfig(
            refWidth, refHeight, watermarkX, watermarkY, watermarkWidth, watermarkHeight);
        
        System.out.println("2. CONFIGURACIÓN RELATIVA CALCULADA:");
        System.out.printf("   Posición relativa: (%.3f, %.3f)\n", 
                         config.getRelativeX(), config.getRelativeY());
        System.out.printf("   Ancho relativo: %.3f\n", config.getRelativeWidth());
        System.out.printf("   Aspect ratio: %.3f\n\n", config.getAspectRatio());
        
        // Probar en diferentes imágenes destino
        double[][] targetImages = {
            {800, 600},    // Más pequeña
            {1920, 1080},  // Full HD
            {4000, 3000}   // 4K
        };
        
        System.out.println("3. APLICACIÓN A DIFERENTES IMÁGENES DESTINO:");
        
        for (int i = 0; i < targetImages.length; i++) {
            double targetWidth = targetImages[i][0];
            double targetHeight = targetImages[i][1];
            
            System.out.printf("\n   IMAGEN DESTINO %d: %.0fx%.0f px\n", 
                             i + 1, targetWidth, targetHeight);
            
            // Aplicar con sistema adaptativo
            WatermarkCalculator.AbsoluteDimensions result = 
                WatermarkCalculator.applyToTarget(config, targetWidth, targetHeight);
            
            System.out.printf("   → Posición final: (%.1f, %.1f)\n", 
                             result.getX(), result.getY());
            System.out.printf("   → Tamaño final: %.1fx%.1f px\n", 
                             result.getWidth(), result.getHeight());
            
            // Verificar que está dentro de los bordes
            double[] margins = WatermarkCalculator.calculateMargins(targetWidth, targetHeight);
            boolean withinMargins = result.getX() >= margins[0] && 
                                   result.getY() >= margins[1] &&
                                   (result.getX() + result.getWidth()) <= (targetWidth - margins[0]) &&
                                   (result.getY() + result.getHeight()) <= (targetHeight - margins[1]);
            
            System.out.printf("   → Márgenes respetados: %s (%.1f, %.1f px)\n", 
                             withinMargins ? "✓ SÍ" : "✗ NO", margins[0], margins[1]);
            
            // Calcular porcentaje de la imagen que ocupa
            double areaPercent = (result.getWidth() * result.getHeight()) / 
                               (targetWidth * targetHeight) * 100;
            System.out.printf("   → Área ocupada: %.2f%%\n", areaPercent);
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✓ El sistema adaptativo garantiza que la marca de agua:");
        System.out.println("  • Nunca se salga de los bordes de la imagen");
        System.out.println("  • Mantenga proporciones consistentes");
        System.out.println("  • Se ajuste automáticamente al tamaño de cada imagen");
        System.out.println("  • Respete márgenes de seguridad adaptativos");
    }
}