# Proyecto AM Visual (Versión JavaFX)

Este proyecto es una aplicación de escritorio para aplicar marcas de agua a imágenes de forma masiva. Originalmente desarrollado en Python y luego en Java Swing, esta versión ha sido migrada a **JavaFX** para aprovechar su moderna arquitectura de UI y capacidades de binding.

## Dependencias del Proyecto

Este proyecto utiliza las siguientes tecnologías y bibliotecas:

- **Java Development Kit (JDK)**: Versión 11 o superior.
- **JavaFX SDK**: Biblioteca para la interfaz gráfica de usuario. Es necesario configurar el proyecto para que el IDE (como VS Code) pueda localizar y utilizar los módulos de JavaFX. Los módulos utilizados son:
  - `javafx.controls`
  - `javafx.fxml`
  - `javafx.swing` (para la interoperabilidad con AWT/Swing si es necesario)
- **Maven o Gradle (Recomendado)**: Para una gestión de dependencias más sencilla, aunque actualmente el proyecto se compila con scripts `build.ps1`/`build.bat`.

## Arquitectura

El proyecto sigue el patrón de diseño **Modelo-Vista-Controlador (MVC)**, adaptado a la arquitectura de JavaFX:

- **Modelo (`com.amvisual.model`)**: Contiene la lógica de negocio y el estado de la aplicación. No tiene conocimiento de la interfaz de usuario.
- **Vista (`com.amvisual.viewfx`)**: Definida en archivos FXML, describe la estructura de la interfaz de usuario.
- **Controlador (`com.amvisual.controllerfx`)**: Conecta la vista (FXML) con el modelo. Gestiona los eventos de la UI y actualiza el estado de la aplicación.

## Cómo Compilar y Ejecutar

Se proporcionan scripts para facilitar la compilación y ejecución desde la terminal.

1.  **Abre una terminal** en el directorio raíz `AM_visual_java`.
2.  **Usando PowerShell**:
    -   Compilar: `.\build.ps1 compileFX`
    -   Ejecutar: `.\build.ps1 runFX`
3.  **Usando CMD**:
    -   Compilar: `build.bat compileFX`
    -   Ejecutar: `build.bat runFX`

La aplicación se iniciará, mostrando la interfaz principal para añadir imágenes y marcas de agua.


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
