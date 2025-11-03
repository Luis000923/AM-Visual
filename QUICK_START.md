# 🚀 AM Visual Java - Guía Rápida

## ⚡ Inicio Rápido

### Compilar y Ejecutar (Opción 1 - PowerShell)
```powershell
.\build.ps1
```

### Compilar y Ejecutar (Opción 2 - CMD)
```cmd
build.bat
```

### Manual
```bash
# Compilar
cd src
javac com/amvisual/AplicacionMarcaAgua.java

# Ejecutar
java com.amvisual.AplicacionMarcaAgua
```

## 📂 Estructura del Proyecto

```
AM_visual_java/
├── src/
│   └── com/
│       └── amvisual/
│           ├── AplicacionMarcaAgua.java    ← Clase principal
│           ├── model/                       ← Datos
│           │   ├── ImagenFlotante.java
│           │   └── ProyectoMarcaAgua.java
│           ├── view/                        ← Interfaz
│           │   ├── PanelPreview.java
│           │   ├── PanelControles.java
│           │   └── PanelInferior.java
│           ├── controller/                  ← Lógica
│           │   ├── MarcaAguaController.java
│           │   └── ProcesadorImagenesController.java
│           └── util/                        ← Utilidades
│               ├── FileUtils.java
│               └── ImageUtils.java
├── README.md                    ← Documentación completa
├── ARQUITECTURA.md              ← Diagramas y diseño
├── build.ps1                    ← Script PowerShell
├── build.bat                    ← Script CMD
└── .gitignore
```

## 🎯 Características Principales

✅ **Arquitectura MVC modular**
- Separación clara de responsabilidades
- Fácil mantenimiento y escalabilidad

✅ **Componentes independientes**
- Model: Estado y lógica de dominio
- View: Paneles visuales reutilizables
- Controller: Coordinación y lógica de negocio
- Util: Funciones compartidas

✅ **Procesamiento en segundo plano**
- SwingWorker para no bloquear la UI
- Barra de progreso en tiempo real

✅ **Gestión completa de marcas de agua**
- Añadir múltiples marcas
- Arrastrar y posicionar
- Ajustar opacidad y escala
- Control de orden Z

## 🔧 Comandos de Build

### PowerShell (build.ps1)
```powershell
.\build.ps1 compile   # Solo compilar
.\build.ps1 run       # Solo ejecutar
.\build.ps1 clean     # Limpiar archivos
.\build.ps1 all       # Limpiar, compilar y ejecutar
```

### CMD (build.bat)
```cmd
build.bat compile
build.bat run
build.bat clean
build.bat all
```

## 📖 Agregar Nuevas Funcionalidades

### Ejemplo: Añadir rotación de marcas

1. **Model** (`ImagenFlotante.java`)
```java
private double angulo = 0.0;

public void setAngulo(double angulo) {
    this.angulo = angulo;
}
```

2. **View** (`PanelControles.java`)
```java
JSlider sliderRotacion = new JSlider(0, 360, 0);
```

3. **Controller** (`MarcaAguaController.java`)
```java
sliderRotacion.addChangeListener(e -> {
    marca.setAngulo(sliderRotacion.getValue());
    panelPreview.actualizarPreview();
});
```

## 🐛 Debugging

### Ver mensajes de consola
Los controladores imprimen información útil:
- Carpetas seleccionadas
- Marcas añadidas
- Errores de procesamiento

### Errores comunes

**Error: No se encuentra la clase principal**
```bash
# Asegúrate de estar en el directorio correcto
cd src
java com.amvisual.AplicacionMarcaAgua
```

**Error: Compilación fallida**
```bash
# Verifica que todas las dependencias estén presentes
javac -verbose com/amvisual/AplicacionMarcaAgua.java
```

## 📊 Comparación con Python

| Aspecto | Python (CustomTkinter) | Java (Swing) |
|---------|------------------------|--------------|
| **Arquitectura** | Monolítica | MVC Modular |
| **Archivos** | 1 archivo | 10 archivos organizados |
| **Mantenibilidad** | Media | Alta |
| **Escalabilidad** | Limitada | Excelente |
| **Rendimiento** | Bueno | Muy bueno |
| **Tipado** | Dinámico | Estático (menos errores) |

## 🎨 Personalización

### Cambiar el Look and Feel
En `AplicacionMarcaAgua.java`:
```java
// Windows
UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

// Metal (multiplataforma)
UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");

// Nimbus (moderno)
UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
```

### Cambiar colores de los paneles
En cada clase `PanelXXX.java`:
```java
setBackground(new Color(45, 45, 48)); // Tema oscuro
```

## 📝 Logs y Diagnóstico

Cada controlador imprime información:
```java
System.out.println("Marca añadida: " + archivo.getName());
System.err.println("Error al procesar: " + e.getMessage());
```

## 🔐 Buenas Prácticas Implementadas

✅ Encapsulación de datos (getters/setters)  
✅ Separación de responsabilidades  
✅ Nombres descriptivos de variables  
✅ Documentación JavaDoc  
✅ Manejo de excepciones  
✅ Validaciones de entrada  
✅ Uso de constantes  
✅ Callbacks en lugar de herencia  

## 📚 Recursos Adicionales

- **README.md**: Documentación completa de la arquitectura
- **ARQUITECTURA.md**: Diagramas y flujo de datos
- Código fuente comentado en detalle

## 🤝 Contribuir

Para añadir nuevas funcionalidades:
1. Identifica en qué capa pertenece (Model/View/Controller/Util)
2. Crea la clase en el paquete correspondiente
3. Integra mediante callbacks o inyección de dependencias
4. Documenta con JavaDoc
5. Prueba de forma aislada

---

**Autor**: Vides_2GA  
**Versión**: 1.0  
**Fecha**: 2025  
**Licencia**: MIT
