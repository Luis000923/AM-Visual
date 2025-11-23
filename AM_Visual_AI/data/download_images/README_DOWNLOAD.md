# 📥 Descargador de Imágenes de Alta Calidad

Este script te permite descargar imágenes de alta calidad para entrenar tus modelos de IA desde **Unsplash**, **Pexels**, **Pixabay** o **Lorem Picsum** (muestras sin API).

## 🚀 Comandos rápidos

Todos los comandos asumen que usas el Python del entorno virtual:

```powershell
..\..\..\venv\Scripts\python.exe download_images.py --help
```

### 1) Descargar 2000 imágenes (dataset diverso)

Descarga ~2000 imágenes distribuidas en 10 categorías:

```powershell
..\..\..\venv\Scripts\python.exe download_images.py --mode diverse --images-per-category 200
```

Si tienes claves de API (Unsplash/Pexels/Pixabay), se usarán; si no, se generará un dataset con **Lorem Picsum**.

### 2) 2000 imágenes de muestra (sin API)

```powershell
..\..\..\venv\Scripts\python.exe download_images.py --mode sample --count 2000
```

### 3) 2000 imágenes de una fuente específica (con API)

Unsplash:

```powershell
..\..\..\venv\Scripts\python.exe download_images.py --mode unsplash --query "landscape" --count 2000
```

Pexels:

```powershell
..\..\..\venv\Scripts\python.exe download_images.py --mode pexels --query "city" --count 2000
```

Pixabay:

```powershell
..\..\..\venv\Scripts\python.exe download_images.py --mode pixabay --query "nature" --count 2000
```

Control del ritmo (evitar límites de API):

```powershell
..\..\..\venv\Scripts\python.exe download_images.py --mode unsplash --query "people" --count 2000 --rate-limit 1.0
```

## 🔑 Claves API necesarias

- Unsplash: https://unsplash.com/developers (env: `UNSPLASH_ACCESS_KEY`)
- Pexels: https://www.pexels.com/api/ (env: `PEXELS_API_KEY`)
- Pixabay: https://pixabay.com/api/docs/ (env: `PIXABAY_API_KEY`)

Puedes definirlas en PowerShell:

```powershell
$env:UNSPLASH_ACCESS_KEY="tu_api_key_unsplash"
$env:PEXELS_API_KEY="tu_api_key_pexels"
$env:PIXABAY_API_KEY="tu_api_key_pixabay"
```

O crea un archivo `.env` en la raíz del proyecto:

```
UNSPLASH_ACCESS_KEY=tu_api_key_unsplash
PEXELS_API_KEY=tu_api_key_pexels
PIXABAY_API_KEY=tu_api_key_pixabay
```

## 📁 Estructura de salida

```
data/input/
├── training_images/          # Imágenes descargadas
│   ├── sample_1920x1080_0001.jpg
│   ├── sample_2560x1440_0002.jpg
│   ├── unsplash_landscape_abc123.jpg
│   └── ...
└── dataset_metadata.json     # Información del dataset
```

## 🎯 Categorías de imágenes

El script descarga imágenes de estas categorías:
- 🏞️ Paisajes (landscape)
- 👤 Retratos (portrait)
- 🏛️ Arquitectura (architecture)
- 🌿 Naturaleza (nature)
- 🏙️ Urbano (urban)
- 🦁 Vida salvaje (wildlife)
- 🍕 Comida (food)
- 💻 Tecnología (technology)
- 🎨 Abstracto (abstract)
- 🏠 Interiores (indoor)

## 📐 Resoluciones disponibles

Las imágenes se descargan en alta resolución:
- 1920x1080 (Full HD)
- 2560x1440 (2K)
- 3840x2160 (4K)
- 2048x1536 (iPad)
- 1600x1200 (UXGA)

## 💡 Tips

- **Sin API keys**: Descarga ~200 imágenes de muestra para empezar
- **Con API keys**: Descarga datasets más grandes y específicos
- Las API keys son **gratuitas** para uso personal/educativo
- Las imágenes descargadas son de **alta calidad** (ideales para entrenamiento)

## ⚙️ Personalización

Puedes modificar el script para:
- Cambiar las categorías de búsqueda
- Ajustar las resoluciones
- Agregar más fuentes de imágenes
- Filtrar por criterios específicos

## 🔧 Solución de problemas

> Nota sobre límites de API: cada proveedor tiene límites/quotas que pueden afectar descargas grandes (p. ej., 2000 imágenes). Si alcanzas un límite, sube `--rate-limit`, espera y reintenta, o combina varias fuentes.

**Error: "requests module not found"**
```powershell
..\..\..\venv\Scripts\python.exe -m pip install requests
```

**Error de red/timeout**
- Verifica tu conexión a internet
- Reduce el número de imágenes a descargar
- Aumenta el timeout en el código

**API rate limit**
- Las APIs gratuitas tienen límites por hora
- Espera unos minutos y vuelve a intentar
- Usa la opción de imágenes de muestra mientras tanto
