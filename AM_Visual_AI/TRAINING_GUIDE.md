# Guía de Entrenamiento del Modelo IA

## ✅ Estado del Proyecto

**TODO ESTÁ LISTO PARA ENTRENAR**

- ✅ 1,737 imágenes de entrenamiento descargadas
- ✅ PyTorch 2.9.0 instalado
- ✅ Modelo de red neuronal configurado
- ✅ Dataset y DataLoader listos
- ✅ Script de entrenamiento completo

## 📊 Resumen del Sistema

### Hardware Detectado
- **Procesador**: CPU (CUDA no disponible)
- **Memoria**: Suficiente para entrenamiento en CPU

### Software Instalado
- Python 3.11.6
- PyTorch 2.9.0 + Torchvision 0.24.0
- OpenCV 4.12.0
- NumPy 2.2.6
- Pillow 11.3.0
- BasicSR, RealESRGAN, GFPGAN

### Dataset
- **Ubicación**: `data/input/`
- **Cantidad**: 1,737 imágenes
- **Fuentes**: Unsplash, Pexels, Pixabay
- **Categorías**: Abstract, Architecture, Food, Indoor, Landscape, Nature, Portrait, Technology, Urban, Wildlife

## 🚀 Cómo Entrenar el Modelo

### Opción 1: Entrenamiento Rápido (Configuración por Defecto)

```bash
# Activar el entorno virtual
.\venv\Scripts\Activate.ps1

# Ejecutar el entrenamiento
python train.py
```

O sin activar el venv:
```bash
.\venv\Scripts\python.exe train.py
```

### Opción 2: Personalizar Configuración

Editar `config/config.yaml` sección `training`:

```yaml
training:
  data_dir: "data/input"           # Directorio de imágenes
  checkpoint_dir: "checkpoints"    # Donde guardar modelos
  num_epochs: 100                  # Número de épocas (reducir para prueba rápida)
  batch_size: 16                   # Tamaño de lote (reducir si falta RAM)
  learning_rate: 0.0001           # Tasa de aprendizaje
  patch_size: 128                 # Tamaño de patches (128x128 px)
  scale: 2                        # Factor de escala (2x mejora)
  save_freq: 5                    # Guardar checkpoint cada 5 épocas
  val_split: 0.1                  # 10% para validación
```

### Opción 3: Prueba Rápida (5 épocas)

Para una prueba rápida, edita `config/config.yaml` y cambia:
```yaml
num_epochs: 5
batch_size: 8
```

Luego ejecuta:
```bash
python train.py
```

## 📈 Durante el Entrenamiento

El script mostrará:
- **Progress Bar**: Progreso de cada época
- **Loss**: Pérdida (debe disminuir)
- **PSNR**: Peak Signal-to-Noise Ratio en dB (debe aumentar, >30dB es bueno)

Ejemplo de salida:
```
Epoch 1: 100%|████████| 100/100 [05:23<00:00, loss=0.0523, PSNR=28.45 dB]
Epoch 1: Train Loss=0.0523, Train PSNR=28.45dB, Val Loss=0.0489, Val PSNR=29.12dB
Checkpoint saved: checkpoints/checkpoint_epoch_1.pth
```

## 💾 Checkpoints

Los modelos se guardan en `checkpoints/`:
- `checkpoint_epoch_X.pth` - Checkpoint cada X épocas
- `best_model.pth` - Mejor modelo (mayor PSNR)
- `training_log.txt` - Log de entrenamiento

## 🔍 Verificar que Todo Está Listo

Antes de entrenar, verifica el estado:

```bash
python check_readiness.py
```

Debe mostrar:
```
Imports: [OK] PASS
CUDA: [INFO] (opcional)
Dataset: [OK] PASS
Model: [OK] PASS
Config: [OK] PASS
```

## ⚙️ Arquitectura del Modelo

**EnhancementNet** - Red neuronal para mejora de imágenes:
- **Input**: Imagen de baja calidad (128x128x3)
- **Output**: Imagen mejorada (128x128x3)
- **Layers**: 
  - Conv2D (64 filters, 9x9) - Feature extraction
  - Conv2D (32 filters, 5x5) - Non-linear mapping
  - Conv2D (32 filters, 5x5) - Deep mapping
  - Conv2D (3 filters, 5x5) - Reconstruction
- **Residual Connection**: Suma input + output
- **Activation**: ReLU
- **Output Range**: [0, 1] (clamped)

## 📊 Cómo Funciona el Dataset

El dataset de entrenamiento crea automáticamente:

1. **Imagen HR (High Resolution)**: Imagen original de alta calidad
2. **Imagen LR (Low Resolution)**: Versión degradada:
   - Downsampling con interpolación cúbica
   - Ruido gaussiano aleatorio (simula ruido de cámara)
   - Blur gaussiano aleatorio (simula desenfoque)
   - Upsampling a tamaño original

El modelo aprende a recuperar la imagen HR desde la LR.

## 🎯 Métricas de Evaluación

### PSNR (Peak Signal-to-Noise Ratio)
- **Rango**: 0-100 dB
- **>20 dB**: Aceptable
- **>30 dB**: Bueno
- **>35 dB**: Excelente
- **>40 dB**: Casi perfecto

### Loss (L1 Loss)
- Diferencia absoluta entre imagen predicha y real
- Debe disminuir con cada época
- Valores típicos: 0.1 → 0.01

## 🛠️ Troubleshooting

### Problema: "Out of Memory"
**Solución**: Reducir `batch_size` en `config.yaml`
```yaml
batch_size: 8  # O incluso 4
```

### Problema: Entrenamiento muy lento
**Solución**: 
1. Reducir `num_epochs` para prueba
2. Reducir `patch_size` a 96 o 64
3. Instalar CUDA si tienes GPU NVIDIA

### Problema: PSNR no mejora
**Solución**:
1. Verificar que hay suficientes imágenes variadas
2. Aumentar `num_epochs`
3. Ajustar `learning_rate` (probar 1e-5 o 5e-4)

## 📁 Estructura del Proyecto

```
AM_Visual_AI/
├── data/
│   ├── input/           # 1,737 imágenes de entrenamiento
│   ├── output/          # Resultados de inferencia
│   └── temp/            # Archivos temporales
├── checkpoints/         # Se crea al entrenar
│   ├── checkpoint_epoch_X.pth
│   ├── best_model.pth
│   └── training_log.txt
├── src/
│   ├── models/          # Definiciones de modelos
│   ├── utils/           # Utilidades
│   └── pipeline/        # Pipeline de procesamiento
├── config/
│   └── config.yaml      # Configuración
├── train.py             # Script de entrenamiento ⭐
├── check_readiness.py   # Verificación de sistema
└── venv/                # Entorno virtual
```

## 🚀 Próximos Pasos

1. **Entrenar el modelo básico**:
   ```bash
   python train.py
   ```

2. **Evaluar resultados**:
   - Revisar `checkpoints/training_log.txt`
   - Verificar PSNR > 30 dB

3. **Usar el modelo entrenado**:
   - Cargar `best_model.pth`
   - Aplicar a imágenes nuevas

4. **Mejorar el modelo** (opcional):
   - Aumentar `num_epochs` a 200-500
   - Descargar más imágenes de entrenamiento
   - Experimentar con diferentes arquitecturas

## 📚 Recursos Adicionales

- **PyTorch Tutorials**: https://pytorch.org/tutorials/
- **Image Super-Resolution**: https://github.com/xinntao/Real-ESRGAN
- **PSNR y métricas**: https://en.wikipedia.org/wiki/Peak_signal-to-noise_ratio

## 🎉 ¡Listo para Entrenar!

El sistema está completamente configurado. Solo ejecuta:

```bash
python train.py
```

El entrenamiento comenzará inmediatamente. Puedes dejar que corra durante la noche para mejores resultados.

**Tiempo estimado**:
- 5 épocas: ~30 minutos (CPU)
- 50 épocas: ~5 horas (CPU)
- 100 épocas: ~10 horas (CPU)

Con GPU sería 10-20x más rápido.

---

**Última verificación**: ✅ Sistema listo
**Imágenes disponibles**: 1,737
**Modelo**: EnhancementNet
**Estado**: READY TO TRAIN! 🚀
