"""
Script para descargar imágenes de alta calidad para entrenamiento del modelo.

Fuentes soportadas (según claves API disponibles):
- Unsplash (env: UNSPLASH_ACCESS_KEY)
- Pexels (env: PEXELS_API_KEY)
- Pixabay (env: PIXABAY_API_KEY)
- Lorem Picsum (sin API key) para muestras

Uso interactivo y por CLI (ver README_DOWNLOAD.md).
"""

import os
import requests
import time
from pathlib import Path
from typing import List, Dict
import json
import argparse
from dotenv import load_dotenv, find_dotenv


class ImageDownloader:
    """Descargador de imágenes de alta calidad desde APIs públicas."""
    
    def __init__(self, output_dir: str = "training_images", rate_limit_seconds: float = 0.5):
        """
        Inicializa el descargador de imágenes.
        
        Args:
            output_dir: Directorio donde se guardarán las imágenes
        """
        # Cargar variables desde .env en la raíz del proyecto
        # Subir dos niveles desde data/download_images/ hasta la raíz
        project_root = Path(__file__).resolve().parents[2]
        dotenv_path = project_root / ".env"
        if dotenv_path.exists():
            load_dotenv(dotenv_path, override=True)
            print(f"🔑 Cargando API keys desde: {dotenv_path}")
        else:
            print(f"⚠️ No se encontró .env en: {dotenv_path}")

        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        
        # APIs (necesitarás obtener tus propias API keys)
        self.unsplash_api_key = os.getenv("UNSPLASH_ACCESS_KEY", "")
        self.pexels_api_key = os.getenv("PEXELS_API_KEY", "")
        self.pixabay_api_key = os.getenv("PIXABAY_API_KEY", "")
        
        # Debug: mostrar si las keys fueron cargadas (sin revelar valores)
        print(f"   Unsplash: {'✓' if self.unsplash_api_key else '✗'}")
        print(f"   Pexels: {'✓' if self.pexels_api_key else '✗'}")
        print(f"   Pixabay: {'✓' if self.pixabay_api_key else '✗'}")
        
        # Contador de imágenes descargadas
        self.downloaded_count = 0
        # Throttling configurable para evitar límites de API
        self.rate_limit_seconds = rate_limit_seconds
        
    def download_from_unsplash(self, query: str, count: int = 30) -> List[str]:
        """
        Descarga imágenes de Unsplash.
        
        Args:
            query: Término de búsqueda
            count: Número de imágenes a descargar
            
        Returns:
            Lista de rutas de archivos descargados
        """
        if not self.unsplash_api_key:
            print("⚠️ No se encontró UNSPLASH_ACCESS_KEY. Salta este paso.")
            print("   Obtén tu API key en: https://unsplash.com/developers")
            return []
        
        downloaded_files = []
        url = "https://api.unsplash.com/search/photos"
        
        print(f"\n📥 Descargando {count} imágenes de Unsplash con query: '{query}'")
        
        for page in range(1, (count // 30) + 2):
            try:
                params = {
                    "query": query,
                    "per_page": min(30, count - len(downloaded_files)),
                    "page": page,
                    "orientation": "landscape"
                }
                
                headers = {"Authorization": f"Client-ID {self.unsplash_api_key}"}
                response = requests.get(url, params=params, headers=headers)
                response.raise_for_status()
                
                data = response.json()
                
                for idx, photo in enumerate(data.get("results", [])):
                    if len(downloaded_files) >= count:
                        break
                    
                    # Descargar imagen en alta resolución
                    img_url = photo["urls"]["full"]
                    img_id = photo["id"]
                    
                    filename = f"unsplash_{query.replace(' ', '_')}_{img_id}.jpg"
                    filepath = self.output_dir / filename
                    
                    self._download_image(img_url, filepath)
                    downloaded_files.append(str(filepath))
                    
                    print(f"  ✓ Descargada: {filename}")
                    time.sleep(1)  # Rate limiting
                
                if len(downloaded_files) >= count:
                    break
                    
            except Exception as e:
                print(f"  ❌ Error descargando de Unsplash: {e}")
                continue
        
        return downloaded_files

    def download_from_pixabay(self, query: str, count: int = 30) -> List[str]:
        """
        Descarga imágenes de Pixabay.

        Args:
            query: Término de búsqueda
            count: Número de imágenes a descargar

        Returns:
            Lista de rutas de archivos descargados
        """
        if not self.pixabay_api_key:
            print("⚠️ No se encontró PIXABAY_API_KEY. Salta este paso.")
            print("   Obtén tu API key en: https://pixabay.com/api/docs/")
            return []

        downloaded_files = []
        url = "https://pixabay.com/api/"

        print(f"\n📥 Descargando {count} imágenes de Pixabay con query: '{query}'")

        page = 1
        per_page = 200  # máximo permitido por Pixabay API
        while len(downloaded_files) < count:
            try:
                remaining = count - len(downloaded_files)
                params = {
                    "key": self.pixabay_api_key,
                    "q": query,
                    "image_type": "photo",
                    "orientation": "horizontal",
                    "safesearch": "true",
                    "per_page": min(per_page, remaining),
                    "page": page,
                }
                response = requests.get(url, params=params, timeout=30)
                response.raise_for_status()
                data = response.json()
                hits = data.get("hits", [])
                if not hits:
                    break
                for photo in hits:
                    if len(downloaded_files) >= count:
                        break
                    img_url = (
                        photo.get("fullHDURL")
                        or photo.get("largeImageURL")
                        or photo.get("webformatURL")
                    )
                    if not img_url:
                        continue

                    img_id = photo.get("id")
                    filename = f"pixabay_{query.replace(' ', '_')}_{img_id}.jpg"
                    filepath = self.output_dir / filename
                    self._download_image(img_url, filepath)
                    downloaded_files.append(str(filepath))
                    print(f"  ✓ Descargada: {filename}")
                    time.sleep(self.rate_limit_seconds)
                page += 1
            except Exception as e:
                print(f"  ❌ Error descargando de Pixabay: {e}")
                break

        return downloaded_files
    
    def download_from_pexels(self, query: str, count: int = 30) -> List[str]:
        """
        Descarga imágenes de Pexels.
        
        Args:
            query: Término de búsqueda
            count: Número de imágenes a descargar
            
        Returns:
            Lista de rutas de archivos descargados
        """
        if not self.pexels_api_key:
            print("⚠️ No se encontró PEXELS_API_KEY. Salta este paso.")
            print("   Obtén tu API key en: https://www.pexels.com/api/")
            return []
        
        downloaded_files = []
        url = "https://api.pexels.com/v1/search"
        
        print(f"\n📥 Descargando {count} imágenes de Pexels con query: '{query}'")
        
        for page in range(1, (count // 80) + 2):
            try:
                params = {
                    "query": query,
                    "per_page": min(80, count - len(downloaded_files)),
                    "page": page
                }
                
                headers = {"Authorization": self.pexels_api_key}
                response = requests.get(url, params=params, headers=headers)
                response.raise_for_status()
                
                data = response.json()
                
                for photo in data.get("photos", []):
                    if len(downloaded_files) >= count:
                        break
                    
                    # Descargar imagen original
                    img_url = photo["src"]["original"]
                    img_id = photo["id"]
                    
                    filename = f"pexels_{query.replace(' ', '_')}_{img_id}.jpg"
                    filepath = self.output_dir / filename
                    
                    self._download_image(img_url, filepath)
                    downloaded_files.append(str(filepath))
                    
                    print(f"  ✓ Descargada: {filename}")
                    time.sleep(1)  # Rate limiting
                
                if len(downloaded_files) >= count:
                    break
                    
            except Exception as e:
                print(f"  ❌ Error descargando de Pexels: {e}")
                continue
        
        return downloaded_files
    
    def download_sample_images(self, count: int = 50) -> List[str]:
        """
        Descarga imágenes de muestra de URLs públicas sin API key.
        Usa Lorem Picsum para obtener imágenes de alta calidad.
        
        Args:
            count: Número de imágenes a descargar
            
        Returns:
            Lista de rutas de archivos descargados
        """
        downloaded_files = []
        
        print(f"\n📥 Descargando {count} imágenes de muestra de Lorem Picsum")
        
        # Diferentes resoluciones de alta calidad
        resolutions = [
            (1920, 1080),  # Full HD
            (2560, 1440),  # 2K
            (3840, 2160),  # 4K
            (2048, 1536),  # iPad
            (1600, 1200),  # UXGA
        ]
        
        for i in range(count):
            try:
                # Rotar entre diferentes resoluciones
                width, height = resolutions[i % len(resolutions)]
                
                # Lorem Picsum proporciona imágenes aleatorias de alta calidad
                img_url = f"https://picsum.photos/{width}/{height}?random={i}"
                
                filename = f"sample_{width}x{height}_{i:04d}.jpg"
                filepath = self.output_dir / filename
                
                self._download_image(img_url, filepath)
                downloaded_files.append(str(filepath))
                
                print(f"  ✓ Descargada: {filename}")
                # Para Lorem Picsum no suele ser necesario tanto rate limiting
                time.sleep(min(self.rate_limit_seconds, 0.2))
                
            except Exception as e:
                print(f"  ❌ Error descargando imagen {i}: {e}")
                continue
        
        return downloaded_files
    
    def _download_image(self, url: str, filepath: Path):
        """
        Descarga una imagen desde una URL.
        
        Args:
            url: URL de la imagen
            filepath: Ruta donde guardar la imagen
        """
        try:
            response = requests.get(url, stream=True, timeout=30)
            response.raise_for_status()
            
            with open(filepath, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    f.write(chunk)
            
            self.downloaded_count += 1
            
        except Exception as e:
            raise Exception(f"Error descargando {url}: {e}")
    
    def download_diverse_dataset(self, images_per_category: int = 200):
        """
        Descarga un dataset diverso con diferentes categorías de imágenes.
        
        Args:
            images_per_category: Número de imágenes por categoría
        """
        categories = [
            "landscape",
            "portrait",
            "architecture",
            "nature",
            "urban",
            "wildlife",
            "food",
            "technology",
            "abstract",
            "indoor"
        ]
        
        all_files = []
        
        print("\n" + "="*60)
        print("🚀 INICIANDO DESCARGA DE DATASET DIVERSO")
        print("="*60)
        
        # Primero intentar con APIs si están disponibles
        if self.unsplash_api_key or self.pexels_api_key or self.pixabay_api_key:
            for category in categories:
                print(f"\n📂 Categoría: {category}")
                
                if self.unsplash_api_key:
                    files = self.download_from_unsplash(category, images_per_category // 2)
                    all_files.extend(files)
                
                if self.pexels_api_key:
                    files = self.download_from_pexels(category, images_per_category // 2)
                    all_files.extend(files)
                
                if self.pixabay_api_key and len([f for f in all_files if category in f]) < images_per_category:
                    # Completar con Pixabay si hace falta
                    remaining = images_per_category - len([f for f in all_files if category in f])
                    if remaining > 0:
                        files = self.download_from_pixabay(category, remaining)
                        all_files.extend(files)
        else:
            # Si no hay API keys, usar imágenes de muestra
            print("\n💡 No se detectaron API keys. Descargando imágenes de muestra...")
            all_files = self.download_sample_images(images_per_category * len(categories))
        
        # Guardar metadata
        metadata = {
            "total_images": len(all_files),
            "categories": categories,
            "images_per_category": images_per_category,
            "downloaded_files": all_files
        }
        
        metadata_path = self.output_dir / "dataset_metadata.json"
        with open(metadata_path, 'w') as f:
            json.dump(metadata, f, indent=2)
        
        print("\n" + "="*60)
        print(f"✅ DESCARGA COMPLETADA")
        print(f"📊 Total de imágenes descargadas: {len(all_files)}")
        print(f"📁 Directorio: {self.output_dir.absolute()}")
        print(f"📄 Metadata guardada en: {metadata_path}")
        print("="*60)
        
        return all_files


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Descargador de imágenes para datasets de IA")
    parser.add_argument("--mode", choices=["diverse", "sample", "unsplash", "pexels", "pixabay"], default="diverse", help="Modo de descarga")
    parser.add_argument("--query", type=str, default="landscape", help="Término de búsqueda para modos con API")
    parser.add_argument("--count", type=int, help="Número total de imágenes a descargar (modos simples)")
    parser.add_argument("--images-per-category", type=int, default=200, help="Imágenes por categoría en modo diverse")
    # Guardar por defecto en data/input (carpeta hermana de data/download_images)
    default_out = Path(__file__).resolve().parents[1] / "input"
    parser.add_argument("--output-dir", type=str, default=str(default_out), help="Directorio de salida")
    parser.add_argument("--rate-limit", type=float, default=0.5, help="Segundos de espera entre descargas")
    return parser


def main():
    """Función principal para ejecutar el descargador."""
    parser = build_arg_parser()
    args = parser.parse_args()

    print("""
    ╔════════════════════════════════════════════════════════════╗
    ║     DESCARGADOR DE IMÁGENES DE ALTA CALIDAD               ║
    ║     Para Entrenamiento de Modelos de IA                   ║
    ╚════════════════════════════════════════════════════════════╝
    """)

    output_dir = Path(args.output_dir)
    print("\n📋 CONFIGURACIÓN:")
    print(f"   Directorio de salida: {output_dir.absolute()}")
    print(f"   Modo: {args.mode}")
    if args.mode != "diverse":
        if args.count:
            print(f"   Count: {args.count}")
    else:
        print(f"   Imágenes por categoría: {args.images_per_category}")
    print(f"   Rate limit (s): {args.rate_limit}")
    print("\n💡 APIs opcionales: UNSPLASH_ACCESS_KEY, PEXELS_API_KEY, PIXABAY_API_KEY")
    print("   Si no configuras APIs, se usarán imágenes de Lorem Picsum (modo sample).\n")

    downloader = ImageDownloader(output_dir=str(output_dir), rate_limit_seconds=args.rate_limit)

    if args.mode == "diverse":
        downloader.download_diverse_dataset(args.images_per_category)
    elif args.mode == "sample":
        count = args.count or 2000
        downloader.download_sample_images(count)
    elif args.mode == "unsplash":
        count = args.count or 2000
        downloader.download_from_unsplash(args.query, count)
    elif args.mode == "pexels":
        count = args.count or 2000
        downloader.download_from_pexels(args.query, count)
    elif args.mode == "pixabay":
        count = args.count or 2000
        downloader.download_from_pixabay(args.query, count)

    print("\n✨ ¡Proceso completado exitosamente!")


if __name__ == "__main__":
    main()
