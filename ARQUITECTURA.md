# Diagrama de Arquitectura AM Visual Java

## Estructura de Paquetes

```
┌─────────────────────────────────────────────────────────────────┐
│                     com.amvisual                                 │
│                  AplicacionMarcaAgua                             │
│                 (Clase Principal)                                │
│                                                                  │
│  • Inicializa Modelo, Vista y Controladores                     │
│  • Conecta eventos entre componentes                            │
│  • Orquesta el flujo de la aplicación                           │
└────────────┬────────────────┬────────────────┬───────────────────┘
             │                │                │
             │                │                │
┌────────────▼──────┐  ┌──────▼──────┐  ┌─────▼────────────┐
│                   │  │             │  │                  │
│   MODEL           │  │    VIEW     │  │   CONTROLLER     │
│                   │  │             │  │                  │
└───────────────────┘  └─────────────┘  └──────────────────┘
```

## 📦 MODEL (com.amvisual.model)

```
┌──────────────────────────────────────┐
│      ProyectoMarcaAgua               │
├──────────────────────────────────────┤
│ - carpetaEntrada: File               │
│ - carpetaSalida: File                │
│ - listaImagenes: List<File>          │
│ - listaMarcasAgua: List<ImagenFlot.> │
│ - indiceMarcaSeleccionada: int       │
├──────────────────────────────────────┤
│ + addMarcaAgua()                     │
│ + eliminarMarcaAgua()                │
│ + moverMarcaEnOrden()                │
│ + getMarcaSeleccionada()             │
└──────────────────────────────────────┘
               │ 1
               │
               │ *
┌──────────────▼───────────────────────┐
│       ImagenFlotante                 │
├──────────────────────────────────────┤
│ - imagenOriginal: BufferedImage      │
│ - imagenActual: BufferedImage        │
│ - posicionRelativa: Point            │
│ - escala: double                     │
│ - opacidad: float                    │
│ - nombreArchivo: String              │
├──────────────────────────────────────┤
│ + dibujar(imagen): BufferedImage     │
│ + iniciarMovimiento()                │
│ + mover()                            │
│ + ajustarEscala()                    │
│ + setOpacidad()                      │
└──────────────────────────────────────┘
```

## 🖼️ VIEW (com.amvisual.view)

```
┌──────────────────────────────────────┐
│       PanelPreview                   │
├──────────────────────────────────────┤
│ - labelImagen: JLabel                │
│ - imagenBase: BufferedImage          │
│ - proyecto: ProyectoMarcaAgua        │
├──────────────────────────────────────┤
│ + cargarImagen(archivo)              │
│ + actualizarPreview()                │
│ + eventos de mouse (drag & drop)     │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│      PanelControles                  │
├──────────────────────────────────────┤
│ - btnAnadirMarca: JButton            │
│ - comboMarcasAgua: JComboBox         │
│ - sliderOpacidad: JSlider            │
│ - btnEscalaMas/Menos: JButton        │
│ - btnEliminar/Subir/Bajar: JButton   │
├──────────────────────────────────────┤
│ + agregarMarcaALista()               │
│ + actualizarListaMarcas()            │
│ + callbacks para eventos             │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│       PanelInferior                  │
├──────────────────────────────────────┤
│ - btnSeleccionarEntrada: JButton     │
│ - btnSeleccionarSalida: JButton      │
│ - btnProcesarImagenes: JButton       │
│ - barraProgreso: JProgressBar        │
│ - labelEstado: JLabel                │
├──────────────────────────────────────┤
│ + actualizarEstado(mensaje)          │
│ + actualizarProgreso(valor)          │
│ + callbacks para eventos             │
└──────────────────────────────────────┘
```

## 🎮 CONTROLLER (com.amvisual.controller)

```
┌──────────────────────────────────────┐
│    MarcaAguaController               │
├──────────────────────────────────────┤
│ - proyecto: ProyectoMarcaAgua        │
│ - panelPreview: PanelPreview         │
│ - panelControles: PanelControles     │
│ - panelInferior: PanelInferior       │
├──────────────────────────────────────┤
│ + seleccionarCarpetaEntrada()        │
│ + seleccionarCarpetaSalida()         │
│ + anadirMarcaAgua()                  │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│  ProcesadorImagenesController        │
├──────────────────────────────────────┤
│ - proyecto: ProyectoMarcaAgua        │
│ - panelInferior: PanelInferior       │
├──────────────────────────────────────┤
│ + procesarImagenes()                 │
│   └─> SwingWorker (hilo separado)   │
│       • Validaciones                 │
│       • Aplicar marcas de agua       │
│       • Guardar imágenes             │
│       • Actualizar progreso          │
└──────────────────────────────────────┘
```

## 🛠️ UTIL (com.amvisual.util)

```
┌──────────────────────────────────────┐
│         FileUtils                    │
│         (Clase Estática)             │
├──────────────────────────────────────┤
│ + obtenerImagenesEnCarpeta()         │
│ + esImagen()                         │
│ + crearCarpetaSiNoExiste()           │
│ + getNombreSinExtension()            │
│ + getExtension()                     │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│        ImageUtils                    │
│        (Clase Estática)              │
├──────────────────────────────────────┤
│ + cargarImagen()                     │
│ + guardarImagen()                    │
│ + convertirARGB()                    │
│ + redimensionarParaPreview()         │
└──────────────────────────────────────┘
```

## 🔄 Flujo de Datos

```
┌──────────────┐
│   Usuario    │
└──────┬───────┘
       │ Acción
       ▼
┌──────────────────┐
│   Vista (View)   │
│   - PanelXXX     │
└──────┬───────────┘
       │ Callback
       ▼
┌───────────────────────┐
│  Controlador          │
│  - MarcaAguaCtrl      │
│  - ProcesadorCtrl     │
└──────┬────────────────┘
       │ Modifica
       ▼
┌──────────────────────┐
│   Modelo (Model)     │
│   - ProyectoMarca    │
│   - ImagenFlotante   │
└──────┬───────────────┘
       │ Notifica
       ▼
┌──────────────────────┐
│   Vista (View)       │
│   Actualiza UI       │
└──────────────────────┘
```

## 📋 Responsabilidades

### Modelo
✅ Estado de la aplicación  
✅ Lógica de dominio  
✅ Datos de marcas de agua  
❌ NO conoce la UI  

### Vista
✅ Presentación visual  
✅ Captura de eventos del usuario  
✅ Callbacks a controladores  
❌ NO tiene lógica de negocio  

### Controlador
✅ Coordinación entre Modelo y Vista  
✅ Lógica de negocio  
✅ Validaciones  
❌ NO conoce detalles de implementación de UI  

### Utilidades
✅ Funciones reutilizables  
✅ Sin estado  
✅ Independientes del resto  

## 🎯 Beneficios

| Característica | Beneficio |
|----------------|-----------|
| **Modularidad** | Cada clase tiene una única responsabilidad |
| **Escalabilidad** | Fácil añadir nuevas funcionalidades |
| **Mantenibilidad** | Errores localizados rápidamente |
| **Testabilidad** | Componentes independientes testables |
| **Reutilización** | Utilidades compartidas |

## 🚀 Extensibilidad

Para añadir una nueva funcionalidad (ej: Rotación de marcas):

1. **Modelo**: Añadir campo `angulo` en `ImagenFlotante`
2. **Vista**: Añadir slider en `PanelControles`
3. **Controlador**: Conectar evento en `MarcaAguaController`
4. **Utilidad**: Si es necesario, añadir en `ImageUtils`

✅ Sin modificar código existente  
✅ Principio Open/Closed
