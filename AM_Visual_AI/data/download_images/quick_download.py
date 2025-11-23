"""
Script simple para descargar imágenes de muestra rápidamente.
Ejecuta este archivo para obtener 50 imágenes de alta calidad sin configuración.
"""

from download_images import ImageDownloader

# Crear descargador
downloader = ImageDownloader(output_dir="training_images")

# Descargar 50 imágenes de muestra de alta calidad
print("\n🚀 Descargando 50 imágenes de muestra...")
print("   No se requieren API keys\n")

files = downloader.download_sample_images(count=50)

print(f"\n✅ ¡Listo! Se descargaron {len(files)} imágenes")
print(f"📁 Ubicación: {downloader.output_dir.absolute()}")
