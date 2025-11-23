"""
Postprocessor Utility
Utilidades para postprocesar imágenes después del procesamiento
"""

import cv2
import numpy as np
from typing import Tuple, Optional, List


class ImagePostprocessor:
    """
    Postprocesamiento de imágenes
    """
    
    @staticmethod
    def clip_values(image: np.ndarray) -> np.ndarray:
        """
        Recortar valores fuera del rango [0, 255]
        
        Args:
            image: Imagen
            
        Returns:
            Imagen con valores recortados
        """
        return np.clip(image, 0, 255).astype(np.uint8)
    
    @staticmethod
    def adjust_brightness(image: np.ndarray,
                         factor: float = 1.0) -> np.ndarray:
        """
        Ajustar brillo
        
        Args:
            image: Imagen BGR
            factor: Factor de ajuste (< 1.0 oscurece, > 1.0 aclara)
            
        Returns:
            Imagen ajustada
        """
        adjusted = image.astype(np.float32) * factor
        return ImagePostprocessor.clip_values(adjusted)
    
    @staticmethod
    def adjust_contrast(image: np.ndarray,
                       factor: float = 1.0) -> np.ndarray:
        """
        Ajustar contraste
        
        Args:
            image: Imagen BGR
            factor: Factor de ajuste (< 1.0 reduce, > 1.0 aumenta)
            
        Returns:
            Imagen ajustada
        """
        # Calcular media
        mean = np.mean(image, axis=(0, 1), keepdims=True)
        
        # Ajustar contraste
        adjusted = mean + factor * (image - mean)
        
        return ImagePostprocessor.clip_values(adjusted)
    
    @staticmethod
    def adjust_saturation(image: np.ndarray,
                         factor: float = 1.0) -> np.ndarray:
        """
        Ajustar saturación
        
        Args:
            image: Imagen BGR
            factor: Factor de ajuste (0.0 = escala de grises, > 1.0 aumenta)
            
        Returns:
            Imagen ajustada
        """
        # Convertir a HSV
        hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV).astype(np.float32)
        
        # Ajustar canal S (saturación)
        hsv[:, :, 1] = hsv[:, :, 1] * factor
        
        # Recortar valores
        hsv[:, :, 1] = np.clip(hsv[:, :, 1], 0, 255)
        
        # Convertir de vuelta a BGR
        hsv = hsv.astype(np.uint8)
        adjusted = cv2.cvtColor(hsv, cv2.COLOR_HSV2BGR)
        
        return adjusted
    
    @staticmethod
    def add_watermark(image: np.ndarray,
                     text: str,
                     position: str = 'bottom-right',
                     font_scale: float = 1.0,
                     color: Tuple[int, int, int] = (255, 255, 255),
                     opacity: float = 0.5) -> np.ndarray:
        """
        Añadir marca de agua
        
        Args:
            image: Imagen BGR
            text: Texto de la marca
            position: Posición ('top-left', 'top-right', 'bottom-left', 'bottom-right')
            font_scale: Escala del texto
            color: Color del texto (BGR)
            opacity: Opacidad (0.0 - 1.0)
            
        Returns:
            Imagen con marca de agua
        """
        result = image.copy()
        h, w = result.shape[:2]
        
        # Configurar fuente
        font = cv2.FONT_HERSHEY_SIMPLEX
        thickness = 2
        
        # Calcular tamaño del texto
        (text_w, text_h), baseline = cv2.getTextSize(text, font, font_scale, thickness)
        
        # Calcular posición
        margin = 10
        if position == 'top-left':
            x, y = margin, margin + text_h
        elif position == 'top-right':
            x, y = w - text_w - margin, margin + text_h
        elif position == 'bottom-left':
            x, y = margin, h - margin
        else:  # bottom-right
            x, y = w - text_w - margin, h - margin
            
        # Crear overlay
        overlay = result.copy()
        cv2.putText(overlay, text, (x, y), font, font_scale, color, thickness)
        
        # Aplicar opacidad
        cv2.addWeighted(overlay, opacity, result, 1 - opacity, 0, result)
        
        return result
    
    @staticmethod
    def create_comparison(images: List[np.ndarray],
                         labels: Optional[List[str]] = None,
                         grid_size: Optional[Tuple[int, int]] = None) -> np.ndarray:
        """
        Crear imagen de comparación side-by-side
        
        Args:
            images: Lista de imágenes
            labels: Etiquetas opcionales
            grid_size: (filas, columnas) - auto-calcula si None
            
        Returns:
            Imagen combinada
        """
        if not images:
            return np.array([])
            
        # Auto-calcular grid
        n_images = len(images)
        if grid_size is None:
            cols = int(np.ceil(np.sqrt(n_images)))
            rows = int(np.ceil(n_images / cols))
        else:
            rows, cols = grid_size
            
        # Obtener tamaño máximo
        max_h = max(img.shape[0] for img in images)
        max_w = max(img.shape[1] for img in images)
        
        # Crear canvas
        canvas = np.zeros((max_h * rows, max_w * cols, 3), dtype=np.uint8)
        
        # Colocar imágenes
        for idx, img in enumerate(images):
            row = idx // cols
            col = idx % cols
            
            if row >= rows:
                break
                
            h, w = img.shape[:2]
            y_offset = row * max_h
            x_offset = col * max_w
            
            # Centrar imagen en su celda
            y_start = y_offset + (max_h - h) // 2
            x_start = x_offset + (max_w - w) // 2
            
            canvas[y_start:y_start+h, x_start:x_start+w] = img
            
            # Añadir etiqueta si se proporciona
            if labels and idx < len(labels):
                cv2.putText(canvas, labels[idx],
                          (x_start + 10, y_start + 30),
                          cv2.FONT_HERSHEY_SIMPLEX, 1.0,
                          (255, 255, 255), 2)
                          
        return canvas
    
    @staticmethod
    def resize_to_match(image: np.ndarray,
                       target_shape: Tuple[int, int]) -> np.ndarray:
        """
        Redimensionar imagen para que coincida con shape objetivo
        
        Args:
            image: Imagen BGR
            target_shape: (height, width)
            
        Returns:
            Imagen redimensionada
        """
        target_h, target_w = target_shape
        return cv2.resize(image, (target_w, target_h), interpolation=cv2.INTER_LANCZOS4)
    
    @staticmethod
    def add_border(image: np.ndarray,
                  border_size: int = 5,
                  color: Tuple[int, int, int] = (0, 0, 0)) -> np.ndarray:
        """
        Añadir borde a la imagen
        
        Args:
            image: Imagen BGR
            border_size: Grosor del borde
            color: Color del borde (BGR)
            
        Returns:
            Imagen con borde
        """
        return cv2.copyMakeBorder(
            image,
            border_size, border_size, border_size, border_size,
            cv2.BORDER_CONSTANT,
            value=color
        )
