# Proyecto AM Visual (Versión Java)

## 1. Resumen del Proyecto

Este proyecto es una adaptación de la aplicación de escritorio `AM_Visual` originalmente desarrollada en Python a Java, utilizando la biblioteca **Java Swing** para la interfaz gráfica. El objetivo principal es replicar y mejorar la funcionalidad de la aplicación original, que permite a los usuarios aplicar marcas de agua (imágenes) a sus fotos de manera interactiva y procesarlas en lote.

## 2. Evolución y Arquitectura

El desarrollo ha seguido un proceso iterativo, enfocado en la calidad y la escalabilidad del código:

1.  **Prototipo Inicial**: Se comenzó con una versión monolítica para establecer la funcionalidad básica.
2.  **Refactorización a MVC**: A petición, el proyecto fue completamente reestructurado para seguir el patrón de diseño **Modelo-Vista-Controlador (MVC)**. Esto ha permitido una clara separación de responsabilidades, facilitando el mantenimiento, la depuración y la adición de nuevas características.
    *   **Modelo (`com.amvisual.model`)**: Contiene la lógica de negocio y el estado de la aplicación (datos de imágenes, marcas de agua, etc.).
    *   **Vista (`com.amvisual.view`)**: Responsable de la interfaz de usuario (ventanas, paneles, botones). No contiene lógica de negocio.
    *   **Controlador (`com.amvisual.controller`)**: Actúa como intermediario, gestionando las acciones del usuario y actualizando el modelo y la vista según corresponda.
    *   **Utilidades (`com.amvisual.util`)**: Clases de ayuda para tareas comunes como manipulación de archivos e imágenes.

## 3. Características Implementadas

*   **Carga de Imágenes**: Carga de una imagen base desde el sistema de archivos para previsualizar la marca de agua.
*   **Añadir Marca de Agua**: Selección de una imagen para usarla como marca de agua.
*   **Previsualización Interactiva**:
    *   La marca de agua se muestra sobre la imagen base en un panel de previsualización.
    *   **Ajuste de Opacidad y Escala**: Mediante deslizadores (sliders) se puede controlar la transparencia y el tamaño de la marca de agua en tiempo real.
    *   **Movimiento de la Marca de Agua**: La marca de agua se puede arrastrar y soltar en cualquier parte de la imagen base con el ratón.
*   **Selección de Destino**: Elección de una carpeta de salida donde se guardarán las imágenes procesadas.
*   **Procesamiento de Imágenes**:
    *   Aplica la marca de agua con la posición, escala y opacidad configuradas a la imagen original.
    *   Guarda la imagen resultante en la carpeta de destino.
*   **Interfaz de Usuario Mejorada**: Se implementó un selector de archivos y directorios (`JFileChooser`) personalizado que muestra una vista previa de las imágenes, mejorando la experiencia de usuario.

## 4. Estructura del Proyecto

El proyecto está organizado de la siguiente manera para facilitar su comprensión y compilación:

```
AM_visual_java/
├── src/                      # Código fuente Java
│   └── com/
│       └── amvisual/
│           ├── model/
│           ├── view/
│           ├── controller/
│           └── util/
├── bin/                      # Archivos .class compilados
├── build.bat                 # Script para compilar y ejecutar en Windows (CMD)
├── build.ps1                 # Script para compilar y ejecutar en Windows (PowerShell)
└── README.md                 # Este archivo
```

## 5. Componentes Clave

*   `AplicacionMarcaAgua.java`: La clase principal que inicializa y ensambla los componentes MVC para lanzar la aplicación.
*   `PanelPreview.java`: Un `JPanel` personalizado que renderiza la imagen base y las marcas de agua. Gestiona los eventos del ratón para el movimiento interactivo.
*   `MarcaAguaController.java`: Maneja las acciones del usuario, como hacer clic en los botones para cargar imágenes o añadir marcas de agua.
*   `ProyectoMarcaAgua.java`: El modelo principal que almacena la información sobre la imagen base, la lista de marcas de agua y la configuración del proceso.
*   `ImagenFlotante.java`: Modelo que representa una marca de agua individual, con sus propiedades (posición, escala, opacidad) y su lógica de dibujado.
*   `FileChooserUtils.java`: Utilidad que crea diálogos de selección de archivos mejorados con previsualización.

## 6. Mejoras y Solución de Problemas

Durante el desarrollo, se abordó un problema crítico que impedía mover las marcas de agua correctamente.

*   **Bug**: El movimiento del ratón no se traducía correctamente a la posición de la marca de agua.
*   **Causa Raíz**: Discrepancia entre el sistema de coordenadas del panel de previsualización (que muestra una imagen escalada) y el modelo (que trabaja con las coordenadas de la imagen original en alta resolución).
*   **Solución**: Se implementó una lógica de conversión en `PanelPreview.java`. Ahora, antes de notificar al modelo sobre un movimiento, las coordenadas del evento del ratón se convierten calculando el factor de escala entre la imagen original y su representación en la vista. Esto asegura que la marca de agua se mueva de forma precisa e intuitiva, sin importar el tamaño de la ventana.

## 7. Cómo Compilar y Ejecutar

Para facilitar el proceso, se han incluido scripts de automatización:

1.  **Abre una terminal** en el directorio raíz `AM_visual_java`.
2.  **Si usas PowerShell**:
    *   Compilar: `.\build.ps1 compile`
    *   Ejecutar: `.\build.ps1 run`
3.  **Si usas CMD**:
    *   Compilar: `build.bat compile`
    *   Ejecutar: `build.bat run`

La aplicación se iniciará y estará lista para usarse.

## 8. Futuras Mejoras (Trabajo en Progreso)

Se ha sentado la base para una arquitectura más avanzada que permitirá futuras expansiones:

*   **Interfaz `Watermark`**: Se creó una interfaz para abstraer el concepto de una marca de agua.
*   **Implementaciones Concretas**: Se desarrollaron las clases `ImageWatermark` y `TextWatermark`. El siguiente paso es integrarlas en la aplicación para permitir no solo imágenes, sino también texto como marcas de agua.
*   **Procesamiento en Lote (`BatchProcessor`)**: Se planea usar un `SwingWorker` para procesar múltiples imágenes en segundo plano sin bloquear la interfaz de usuario, mostrando el progreso en una barra de estado.

Este proyecto ha evolucionado hasta convertirse en una aplicación Java robusta y bien estructurada, sentando las bases para un desarrollo continuo y eficiente.
2. **Vista notifica al Controlador** mediante callbacks
3. **Controlador actualiza el Modelo** (ProyectoMarcaAgua)
4. **Controlador actualiza la Vista** (PanelPreview)

## ✨ Ventajas de esta Arquitectura

### 1. **Separación de Responsabilidades**
- Cada clase tiene una única responsabilidad
- Fácil de entender y mantener

### 2. **Escalabilidad**
- Fácil añadir nuevas funcionalidades
- Nuevos paneles se integran sin modificar código existente

### 3. **Testabilidad**
- Componentes independientes
- Fácil crear tests unitarios

### 4. **Reutilización**
- Utilidades compartidas (FileUtils, ImageUtils)
- Componentes de vista modulares

### 5. **Mantenibilidad**
- Errores fáciles de localizar
- Cambios aislados en módulos específicos

## 🚀 Compilación y Ejecución

### Compilar
```bash
cd src
javac com/amvisual/AplicacionMarcaAgua.java
```

### Ejecutar
```bash
java com.amvisual.AplicacionMarcaAgua
```

### Compilar todo el proyecto
```bash
cd src
javac com/amvisual/**/*.java
```

## 📝 Añadir Nuevas Funcionalidades

### Ejemplo: Añadir rotación de marcas

1. **Modelo**: Añadir campo `angulo` en `ImagenFlotante`
2. **Vista**: Añadir slider de rotación en `PanelControles`
3. **Controlador**: Conectar evento en `MarcaAguaController`
4. **Utilidad**: Si es necesario, añadir métodos en `ImageUtils`

## 🔧 Patrones de Diseño Utilizados

- **MVC**: Separación modelo-vista-controlador
- **Observer**: Callbacks entre vista y controlador
- **Singleton implícito**: ProyectoMarcaAgua como estado compartido
- **Strategy**: ImagenFlotante encapsula algoritmos de dibujo

## � Métricas del Proyecto

| Métrica | Valor |
|---------|-------|
| **Archivos Java** | 10 |
| **Líneas de código** | ~1,500 |
| **Paquetes** | 4 (model, view, controller, util) |
| **Clases** | 10 |
| **Documentación** | 3 archivos MD |

## 🎓 Comparación con Versión Python

| Aspecto | Python | Java |
|---------|--------|------|
| Archivos | 1 monolítico | 10 modulares |
| Arquitectura | Procedimental | MVC |
| Líneas/archivo | ~800 | ~150 promedio |
| Mantenibilidad | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Escalabilidad | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Rendimiento | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

## �📄 Licencia

MIT License - Vides_2GA © 2025
