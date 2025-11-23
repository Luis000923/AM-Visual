"""
Image Loader Utility
Utilidades para cargar, guardar y convertir imágenes
"""

import cv2
import numpy as np
from pathlib import Path
from typing import Union, Optional, Tuple
from PIL import Image


class ImageLoader:
    """
    Clase para cargar y guardar imágenes de forma robusta
    """
    
    @staticmethod
    def load(image_path: Union[str, Path]) -> Optional[np.ndarray]:
        """
        Cargar imagen desde archivo
        
        Args:
            image_path: Ruta a la imagen
            
        Returns:
            Imagen en formato BGR numpy array, o None si falla
        """
        image_path = Path(image_path)
        
        if not image_path.exists():
            print(f"⚠ Archivo no encontrado: {image_path}")
            return None
            
        try:
            # Intentar con OpenCV primero (más rápido)
            image = cv2.imread(str(image_path))
            
            if image is None:
                # Intentar con PIL como fallback
                pil_image = Image.open(image_path)
                image = cv2.cvtColor(np.array(pil_image), cv2.COLOR_RGB2BGR)
                
            print(f"✓ Imagen cargada: {image_path.name} ({image.shape[1]}x{image.shape[0]})")
            return image
            
        except Exception as e:
            print(f"⚠ Error al cargar imagen: {e}")
            return None
    
    @staticmethod
    def save(image: np.ndarray, 
             output_path: Union[str, Path],
             quality: int = 95,
             format: Optional[str] = None) -> bool:
        """
        Guardar imagen a archivo
        
        Args:
            image: Imagen BGR
            output_path: Ruta de salida
            quality: Calidad para JPEG/WebP (1-100)
            format: Formato de salida (png, jpg, webp) - auto-detecta de la extensión
            
        Returns:
            True si se guardó exitosamente
        """
        output_path = Path(output_path)
        
        # Crear directorio si no existe
        output_path.parent.mkdir(parents=True, exist_ok=True)
        
        try:
            # Determinar formato
            if format is None:
                format = output_path.suffix.lower().replace('.', '')
                
            # Parámetros de guardado
            if format in ['jpg', 'jpeg']:
                params = [cv2.IMWRITE_JPEG_QUALITY, quality]
            elif format == 'png':
                # PNG es lossless, quality se interpreta como compresión
                compression = int((100 - quality) / 10)
                params = [cv2.IMWRITE_PNG_COMPRESSION, compression]
            elif format == 'webp':
                params = [cv2.IMWRITE_WEBP_QUALITY, quality]
            else:
                params = []
                
            # Guardar
            success = cv2.imwrite(str(output_path), image, params)
            
            if success:
                print(f"✓ Imagen guardada: {output_path}")
                return True
            else:
                print(f"⚠ Error al guardar imagen: {output_path}")
                return False
                
        except Exception as e:
            print(f"⚠ Error al guardar imagen: {e}")
            return False
    
    @staticmethod
    def load_batch(image_dir: Union[str, Path],
                  extensions: Tuple[str, ...] = ('.jpg', '.jpeg', '.png', '.webp', '.bmp')) -> list:
        """
        Cargar múltiples imágenes de un directorio
        
        Args:
            image_dir: Directorio con imágenes
            extensions: Extensiones permitidas
            
        Returns:
            Lista de tuplas (nombre_archivo, imagen)
        """
        image_dir = Path(image_dir)
        
        if not image_dir.exists():
            print(f"⚠ Directorio no encontrado: {image_dir}")
            return []
            
        images = []
        for ext in extensions:
            for image_path in image_dir.glob(f'*{ext}'):
                image = ImageLoader.load(image_path)
                if image is not None:
                    images.append((image_path.name, image))
                    
        print(f"✓ Cargadas {len(images)} imágenes desde {image_dir}")
        return images
    
    @staticmethod
    def get_image_info(image: np.ndarray) -> dict:
        """
        Obtener información de la imagen
        
        Args:
            image: Imagen BGR
            
        Returns:
            Diccionario con información
        """
        height, width = image.shape[:2]
        channels = image.shape[2] if len(image.shape) == 3 else 1
        dtype = image.dtype
        
        # Calcular tamaño en MB
        size_bytes = image.nbytes
        size_mb = size_bytes / (1024 * 1024)
        
        return {
            'width': width,
            'height': height,
            'channels': channels,
            'dtype': str(dtype),
            'size_mb': round(size_mb, 2),
            'aspect_ratio': round(width / height, 2)
        }
