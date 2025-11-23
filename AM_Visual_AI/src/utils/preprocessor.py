"""
Preprocessor Utility
Utilidades para preprocesar imágenes antes del procesamiento
"""

import cv2
import numpy as np
from typing import Tuple, Optional


class ImagePreprocessor:
    """
    Preprocesamiento de imágenes
    """
    
    @staticmethod
    def resize(image: np.ndarray,
               target_size: Optional[Tuple[int, int]] = None,
               max_size: Optional[int] = None,
               interpolation: int = cv2.INTER_LANCZOS4) -> np.ndarray:
        """
        Redimensionar imagen
        
        Args:
            image: Imagen BGR
            target_size: Tamaño objetivo (width, height)
            max_size: Tamaño máximo de la dimensión mayor
            interpolation: Método de interpolación
            
        Returns:
            Imagen redimensionada
        """
        h, w = image.shape[:2]
        
        if target_size is not None:
            new_w, new_h = target_size
        elif max_size is not None:
            # Redimensionar manteniendo aspect ratio
            if max(h, w) > max_size:
                scale = max_size / max(h, w)
                new_w = int(w * scale)
                new_h = int(h * scale)
            else:
                return image
        else:
            return image
            
        resized = cv2.resize(image, (new_w, new_h), interpolation=interpolation)
        return resized
    
    @staticmethod
    def normalize(image: np.ndarray,
                 mean: Tuple[float, float, float] = (0.485, 0.456, 0.406),
                 std: Tuple[float, float, float] = (0.229, 0.224, 0.225)) -> np.ndarray:
        """
        Normalizar imagen (para modelos pre-entrenados)
        
        Args:
            image: Imagen BGR [0-255]
            mean: Media para normalización
            std: Desviación estándar
            
        Returns:
            Imagen normalizada
        """
        # Convertir a float y escalar a [0, 1]
        image_float = image.astype(np.float32) / 255.0
        
        # Convertir BGR a RGB
        image_rgb = cv2.cvtColor(image_float, cv2.COLOR_BGR2RGB)
        
        # Normalizar
        mean = np.array(mean, dtype=np.float32)
        std = np.array(std, dtype=np.float32)
        
        normalized = (image_rgb - mean) / std
        
        return normalized
    
    @staticmethod
    def denormalize(image: np.ndarray,
                   mean: Tuple[float, float, float] = (0.485, 0.456, 0.406),
                   std: Tuple[float, float, float] = (0.229, 0.224, 0.225)) -> np.ndarray:
        """
        Denormalizar imagen
        
        Args:
            image: Imagen normalizada
            mean: Media usada en normalización
            std: Desviación estándar usada
            
        Returns:
            Imagen BGR [0-255]
        """
        mean = np.array(mean, dtype=np.float32)
        std = np.array(std, dtype=np.float32)
        
        # Denormalizar
        denormalized = (image * std) + mean
        
        # Convertir RGB a BGR
        bgr = cv2.cvtColor(denormalized, cv2.COLOR_RGB2BGR)
        
        # Escalar a [0, 255]
        bgr = np.clip(bgr * 255, 0, 255).astype(np.uint8)
        
        return bgr
    
    @staticmethod
    def crop(image: np.ndarray,
            x: int, y: int,
            width: int, height: int) -> np.ndarray:
        """
        Recortar imagen
        
        Args:
            image: Imagen BGR
            x, y: Coordenadas superior izquierda
            width, height: Dimensiones del recorte
            
        Returns:
            Imagen recortada
        """
        h, w = image.shape[:2]
        
        # Validar coordenadas
        x = max(0, min(x, w))
        y = max(0, min(y, h))
        x2 = min(x + width, w)
        y2 = min(y + height, h)
        
        return image[y:y2, x:x2]
    
    @staticmethod
    def pad(image: np.ndarray,
           top: int, bottom: int,
           left: int, right: int,
           color: Tuple[int, int, int] = (0, 0, 0)) -> np.ndarray:
        """
        Añadir padding a la imagen
        
        Args:
            image: Imagen BGR
            top, bottom, left, right: Cantidad de padding
            color: Color del padding (BGR)
            
        Returns:
            Imagen con padding
        """
        return cv2.copyMakeBorder(
            image,
            top, bottom, left, right,
            cv2.BORDER_CONSTANT,
            value=color
        )
    
    @staticmethod
    def to_square(image: np.ndarray,
                 size: Optional[int] = None,
                 color: Tuple[int, int, int] = (0, 0, 0)) -> np.ndarray:
        """
        Convertir imagen a cuadrada con padding
        
        Args:
            image: Imagen BGR
            size: Tamaño del cuadrado (None = usar dimensión mayor)
            color: Color del padding
            
        Returns:
            Imagen cuadrada
        """
        h, w = image.shape[:2]
        
        if size is None:
            size = max(h, w)
            
        # Calcular padding
        pad_h = (size - h) // 2
        pad_w = (size - w) // 2
        
        # Añadir padding
        squared = ImagePreprocessor.pad(
            image,
            pad_h, size - h - pad_h,
            pad_w, size - w - pad_w,
            color
        )
        
        return squared
    
    @staticmethod
    def auto_rotate(image: np.ndarray) -> np.ndarray:
        """
        Rotar automáticamente imagen basado en EXIF (si está disponible)
        
        Args:
            image: Imagen BGR
            
        Returns:
            Imagen rotada
        """
        # Esta función requeriría leer datos EXIF
        # Por ahora retorna la imagen sin cambios
        return image
    
    @staticmethod
    def convert_colorspace(image: np.ndarray,
                          target: str = 'RGB') -> np.ndarray:
        """
        Convertir espacio de color
        
        Args:
            image: Imagen BGR
            target: Espacio objetivo ('RGB', 'HSV', 'LAB', 'GRAY')
            
        Returns:
            Imagen en el espacio de color objetivo
        """
        target = target.upper()
        
        if target == 'RGB':
            return cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        elif target == 'HSV':
            return cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
        elif target == 'LAB':
            return cv2.cvtColor(image, cv2.COLOR_BGR2LAB)
        elif target == 'GRAY':
            return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        else:
            print(f"⚠ Espacio de color {target} no reconocido")
            return image
