# AM Visual AI - Image Enhancement & Object Removal

Un sistema modular de IA para mejorar la calidad de imágenes y eliminar objetos, construido con Python y Deep Learning.

## 🎯 Características

- **Mejora de Calidad de Imagen**
  - Super-resolución (upscaling 2x, 4x)
  - Reducción de ruido
  - Mejora de nitidez
  - Corrección de color automática

- **Eliminación de Objetos**
  - Detección automática de objetos (YOLOv8)
  - Segmentación precisa (SAM)
  - Inpainting inteligente (LaMa)
  - Selección manual de áreas

## 📁 Estructura del Proyecto

```
AM_Visual_AI/
├── src/
│   ├── models/              # Módulos de modelos de IA
│   │   ├── image_enhancement.py
│   │   └── object_removal.py
│   ├── utils/               # Utilidades
│   │   ├── image_loader.py
│   │   ├── preprocessor.py
│   │   └── postprocessor.py
│   └── pipeline/            # Pipeline de procesamiento
│       └── pipeline.py
├── data/
│   ├── input/               # Imágenes de entrada
│   ├── output/              # Resultados procesados
│   └── temp/                # Archivos temporales
├── config/
│   └── config.yaml          # Configuración del sistema
├── tests/                   # Tests unitarios
├── notebooks/               # Jupyter notebooks para experimentación
├── models/                  # Modelos pre-entrenados
├── requirements.txt
└── main.py                  # Punto de entrada principal
```

## 🚀 Instalación

1. **Clonar o descargar el proyecto**

2. **Crear entorno virtual (recomendado)**
```bash
python -m venv venv
.\venv\Scripts\activate  # Windows
```

3. **Instalar dependencias**
```bash
pip install -r requirements.txt
```

## 💻 Uso

### Uso Básico

```python
from src.pipeline.pipeline import ImageProcessingPipeline

# Inicializar pipeline
pipeline = ImageProcessingPipeline()

# Mejorar calidad de imagen
enhanced_img = pipeline.enhance_image("data/input/photo.jpg")

# Eliminar objetos
cleaned_img = pipeline.remove_objects("data/input/photo.jpg", object_classes=[0])  # 0 = persona
```

### Uso desde Línea de Comandos

```bash
# Mejorar calidad
python main.py --mode enhance --input data/input/photo.jpg

# Eliminar objetos
python main.py --mode remove --input data/input/photo.jpg --classes person car

# Pipeline completo
python main.py --mode full --input data/input/photo.jpg
```

## ⚙️ Configuración

Edita `config/config.yaml` para personalizar:

- Dispositivo de procesamiento (CPU/GPU)
- Modelos a utilizar
- Parámetros de calidad
- Formatos de salida

## 📦 Modelos Soportados

### Super-Resolución
- RealESRGAN (x2, x4)
- RealESRNet

### Detección de Objetos
- YOLOv8 (nano, small, medium, large, xlarge)

### Segmentación
- SAM (Segment Anything Model)

### Inpainting
- LaMa (Large Mask Inpainting)

## 🧪 Testing

```bash
pytest tests/
```

## 📝 Requisitos del Sistema

- Python 3.8+
- CUDA 11.8+ (opcional, para GPU)
- 8GB RAM mínimo (16GB recomendado)
- GPU con 6GB+ VRAM (opcional pero recomendado)

## 🔄 Roadmap

- [ ] Interfaz gráfica (Gradio/Streamlit)
- [ ] API REST
- [ ] Procesamiento por lotes
- [ ] Soporte para video
- [ ] Modelos personalizables
- [ ] Docker deployment

## 📄 Licencia

MIT License

## 👨‍💻 Autor

Luis - AM Visual AI
