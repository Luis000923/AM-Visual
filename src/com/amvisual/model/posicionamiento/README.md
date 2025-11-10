# Sistema de Posicionamiento de Marcas de Agua

## 🔴 ESTADO: NO FUNCIONAL 🔴

**Atención:** Los módulos de posicionamiento y escalado descritos en este documento **no están funcionando correctamente**. El algoritmo actual presenta fallos críticos que impiden su uso en producción.

**Problemas conocidos:**
- Cálculo incorrecto de dimensiones y posiciones.
- Comportamiento inesperado en diferentes orientaciones de imagen.
- Inconsistencias en el escalado.

**Acción requerida:** Es necesario desarrollar e implementar un nuevo algoritmo antes de utilizar estas clases.

---

## Descripción del Sistema (Concepto Original)

Este módulo **tenía como objetivo** implementar un algoritmo para el posicionamiento y escalado de marcas de agua en imágenes, independientemente de su resolución o relación de aspecto. Sin embargo, la implementación actual es defectuosa.

## Arquitectura del Sistema

El sistema está diseñado con una arquitectura modular de 4 componentes principales:

### 1. **WatermarkConfig** - Configuración Inmutable
```
Almacena las proporciones calculadas:
- relativeX: Posición X relativa al ancho (0.0 - 1.0+)
- relativeY: Posición Y relativa al alto (0.0 - 1.0+)
- relativeWidth: Ancho relativo a la dimensión base inteligente [v6.1]
- aspectRatio: Relación alto/ancho original
```

**Características**:
- Clase inmutable (thread-safe)
- Validación robusta de parámetros
- Métodos de utilidad (scaled, withX, withY, etc.)
- Soporte completo de equals/hashCode

### 2. **WatermarkCalculator** - Motor de Cálculo
```
Algoritmo Principal (v6.1):
1. Identifica orientación de imagen (horizontal/vertical/cuadrada)
2. Selecciona dimensión base inteligente
3. Calcula posiciones relativas
4. Calcula tamaño relativo a dimensión base
5. Preserva aspect ratio original
6. Valida y ajusta límites automáticamente [NUEVO v6.1]
```

**Métodos principales**:
- `calculateRelativeConfig()`: Absolutas → Relativas
- `applyToTarget()`: Relativas → Absolutas con ajuste de límites [MEJORADO v6.1]
- `getDiagnosticInfo()`: Información de diagnóstico [NUEVO v6.1]
- `isWithinBounds()`: Validación de límites [NUEVO v6.1]
- Clase interna `AbsoluteDimensions` para resultados

### 3. **WatermarkRenderer** - Motor de Renderizado
```
Pipeline de Renderizado:
1. Redimensiona marca al tamaño calculado
2. Aplica transparencia (opacidad)
3. Compone sobre imagen destino
4. Renderizado de alta calidad
```

**Características**:
- Interpolación bilinear para suavizado
- Antialiasing para bordes
- Alpha compositing para transparencia
- Preservación del canal alpha
- Recibe dimensiones pre-validadas [v6.1]

### 4. **WatermarkPositioner** - Fachada Principal
```
API Pública:
- calculateRelativeConfig(): Convierte coordenadas
- applyWatermark(): Renderiza marca
- createCenteredConfig(): Config inicial
- getDiagnosticInfo(): Información de diagnóstico [NUEVO v6.1]
- willBeWithinBounds(): Validación preventiva [NUEVO v6.1]
- Plantillas para extensiones futuras
```

**Responsabilidades**:
- Coordina Calculator + Renderer
- Provee API simplificada
- Validación de alto nivel
- Preparado para extensiones

## Algoritmo de Posicionamiento Relativo v6.1

### Ventajas del Algoritmo

1. **Escalado Proporcional**: La marca mantiene su tamaño visual relativo en imágenes de cualquier resolución
2. **Consistencia Visual**: Dimensión base inteligente según orientación [v6.1]
3. **Sin Distorsión**: Preservación del aspect ratio evita deformaciones
4. **Orientación Agnóstica**: Funciona igual en horizontal, vertical o cuadrada
5. **Límites Garantizados**: Marcas siempre dentro de la imagen [v6.1]
6. **Depuración Fácil**: Herramientas de diagnóstico integradas [v6.1]

### Flujo de Trabajo

#### Configuración Inicial (Usuario posiciona marca)
```
Imagen 1920x1080 (horizontal) → Usuario coloca marca en (960, 540) con tamaño 192x108
                 ↓
         WatermarkPositioner.calculateRelativeConfig()
                 ↓
         [v6.1] Detecta orientación: Horizontal (ratio 1.778)
         [v6.1] Dimensión base = 1920 (ancho)
                 ↓
         Configuración relativa guardada
         (relX=0.5, relY=0.5, relWidth=0.1, aspect=0.5625)
```

#### Aplicación a Otras Imágenes
```
Nueva imagen 3840x2160 + Configuración guardada
                 ↓
         WatermarkPositioner.applyWatermark()
                 ↓
    Calculador [v6.1]: Detecta orientación: Horizontal
                       Dimensión base = 3840 (ancho)
                       Ancho final = 3840 * 0.1 = 384px
                       Alto final = 384 * 0.5625 = 216px
                       Posición = (1920, 1080)
                       Validación límites: OK ✓
                 ↓
         Renderizador: Aplica marca escalada
                 ↓
         Imagen con marca proporcional y bien posicionada
```

## Integración con el Sistema

### ImagenFlotante
- Almacena dos configuraciones (horizontal/vertical)
- Método `dibujar()` aplica marca usando el nuevo sistema
- Método `actualizarConfiguracion()` para sincronizar con UI
- Regla inteligente: usa config de otra orientación si falta

### MainViewController
- `handleScroll()`: Zoom con scroll del mouse
- `updateWatermarkView()`: Visualización en preview
- `updateConfigFromView()`: Sincronización modelo-vista
- `handleAddWatermark()`: Inicialización centrada automática

## Extensiones Futuras (Plantillas Incluidas)

El sistema incluye plantillas para futuras extensiones:

### 1. Detección Automática de Posición
```java
WatermarkPositioner.detectOptimalPosition()
```
**Posibles algoritmos**:
- Detección de bordes
- Análisis de luminosidad
- Áreas de bajo contraste
- Machine learning

### 2. Opacidad Adaptativa
```java
WatermarkPositioner.calculateAdaptiveOpacity()
```
**Basado en**:
- Características de la imagen
- Zona de aplicación
- Contraste local

### 3. Transformaciones Avanzadas
- Rotación inteligente
- Warping adaptativo
- Multi-marca coordinada

## Uso del Sistema

### Ejemplo Básico (v6.1)
```java
// 1. Calcular configuración desde posición de usuario
WatermarkConfig config = WatermarkPositioner.calculateRelativeConfig(
    1920, 1080,  // Dimensiones imagen referencia (horizontal)
    960, 540,    // Posición de la marca
    192, 108     // Tamaño de la marca
);
// [v6.1] Automáticamente detecta que es horizontal y usa ancho como base
// relativeWidth = 192 / 1920 = 0.1 (10% del ancho)

// 2. [NUEVO v6.1] Verificar antes de aplicar
String diagnostico = WatermarkPositioner.getDiagnosticInfo(config, 3840, 2160);
System.out.println(diagnostico);

boolean dentroLimites = WatermarkPositioner.willBeWithinBounds(config, 3840, 2160);
System.out.println("Dentro de límites: " + dentroLimites);

// 3. Aplicar a cualquier imagen
BufferedImage result = WatermarkPositioner.applyWatermark(
    targetImage,     // Imagen destino
    watermarkImage,  // Marca de agua
    config,          // Configuración calculada
    0.7f             // Opacidad
);
// [v6.1] Se ajusta automáticamente si sale de límites
```

### Ejemplo con Config Centrada
```java
WatermarkConfig centered = WatermarkPositioner.createCenteredConfig(
    imageWidth, imageHeight,
    watermarkWidth, watermarkHeight,
    0.20  // 20% de la dimensión base inteligente [v6.1]
);
```

### Ejemplo de Depuración (NUEVO v6.1)
```java
// Obtener información detallada de diagnóstico
WatermarkConfig config = // ... tu configuración
String info = WatermarkPositioner.getDiagnosticInfo(config, 1920, 1080);
System.out.println(info);

/* Salida esperada:
=== Diagnóstico de Posicionamiento ===
Imagen destino: 1920 x 1080 px
Aspect ratio destino: 1.778 (Horizontal)

Config relativa:
  X: 0.5000 (50.0%)
  Y: 0.5000 (50.0%)
  Width: 0.1000
  Aspect Ratio: 0.5625

Dimensiones absolutas calculadas:
  Posición: (960.0, 540.0)
  Tamaño: 192.0 x 108.0 px
  Borde derecho: 1152.0 (límite: 1920)
  Borde inferior: 648.0 (límite: 1080)
  Dentro de límites: SÍ

Dimensión base usada: 1920.0 px
Porcentaje de la imagen: 10.0%
*/
```

## Testing y Validación

El sistema incluye validación exhaustiva:
- ✅ Parámetros NaN o infinitos
- ✅ Dimensiones negativas o cero
- ✅ Opacidad fuera de rango
- ✅ Configuraciones inválidas
- ✅ Null safety completo
- ✅ [v6.1] Validación automática de límites
- ✅ [v6.1] Ajuste automático de posición/tamaño

## Métricas de Calidad

- **Modularidad**: 4 clases con responsabilidades bien definidas
- **Mantenibilidad**: Código documentado y autoexplicativo
- **Extensibilidad**: Preparado para futuras mejoras
- **Robustez**: Validación completa y manejo de errores
- **Performance**: Algoritmo eficiente O(1) para cálculos
- **[v6.1] Adaptabilidad**: Ajuste inteligente según orientación
- **[v6.1] Depurabilidad**: Herramientas de diagnóstico integradas

## Changelog

### v6.1 (Noviembre 2025)
- ✅ Dimensión base inteligente según orientación
- ✅ Validación automática de límites
- ✅ Ajuste automático de posición y tamaño
- ✅ Métodos de diagnóstico (`getDiagnosticInfo`, `isWithinBounds`)
- ✅ Optimización específica para imágenes horizontales
- ✅ 100% retrocompatible con v6.0

### v6.0 (Noviembre 2025)
- ✅ Sistema modular completo
- ✅ 4 componentes principales
- ✅ Algoritmo de posicionamiento relativo
- ✅ Renderizado de alta calidad

---

**Implementado**: 2025-11-10  
**Versión Actual**: 6.1  
**Arquitecto**: Vides_2GA (con IA)

---

## Enlaces Rápidos

- 📄 [Guía de Mejoras v6.1](./MEJORAS_v6.1.md) - Documentación detallada de las mejoras
- 📋 [Guía de Uso](./USAGE_GUIDE.md) - Manual de uso del sistema (si existe)

## Soporte

Para problemas específicos de posicionamiento en imágenes horizontales:
1. Usa `getDiagnosticInfo()` para ver los cálculos detallados
2. Verifica con `willBeWithinBounds()` antes de aplicar
3. El sistema ajustará automáticamente si es necesario

Para otras consultas, revisar la documentación completa en los archivos mencionados.
