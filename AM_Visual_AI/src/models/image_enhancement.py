"""
Image Enhancement Module
Módulo para mejorar la calidad de imágenes utilizando técnicas de Deep Learning
"""

import torch
import numpy as np
import cv2
from typing import Optional, Tuple, Union
from pathlib import Path


class ImageEnhancer:
    """
    Clase base para mejora de imágenes
    """
    
    def __init__(self, device: str = 'cuda'):
        """
        Inicializar el mejorador de imágenes
        
        Args:
            device: 'cuda', 'cpu', o 'mps'
        """
        self.device = self._setup_device(device)
        
    def _setup_device(self, device: str) -> str:
        """Configurar el dispositivo de procesamiento"""
        if device == 'cuda' and torch.cuda.is_available():
            return 'cuda'
        elif device == 'mps' and torch.backends.mps.is_available():
            return 'mps'
        else:
            return 'cpu'
    
    def enhance(self, image: np.ndarray, **kwargs) -> np.ndarray:
        """
        Método base para mejorar imagen
        
        Args:
            image: Imagen en formato numpy array (BGR)
            **kwargs: Parámetros adicionales
            
        Returns:
            Imagen mejorada
        """
        raise NotImplementedError("Subclass must implement enhance method")


class SuperResolution(ImageEnhancer):
    """
    Super-resolución utilizando RealESRGAN
    """
    
    def __init__(self, 
                 model_name: str = 'RealESRGAN_x4plus',
                 scale: int = 4,
                 tile_size: int = 512,
                 tile_pad: int = 10,
                 device: str = 'cuda'):
        """
        Inicializar modelo de super-resolución
        
        Args:
            model_name: Nombre del modelo (RealESRGAN_x4plus, RealESRNet_x4plus, etc.)
            scale: Factor de escala (2, 4)
            tile_size: Tamaño de tile para procesamiento
            tile_pad: Padding para tiles
            device: Dispositivo de procesamiento
        """
        super().__init__(device)
        self.model_name = model_name
        self.scale = scale
        self.tile_size = tile_size
        self.tile_pad = tile_pad
        self.model = None
        
    def load_model(self):
        """Cargar modelo de super-resolución"""
        try:
            from basicsr.archs.rrdbnet_arch import RRDBNet
            from realesrgan import RealESRGANer
            
            # Configurar modelo según el nombre
            if 'RealESRNet' in self.model_name:
                model = RRDBNet(num_in_ch=3, num_out_ch=3, num_feat=64, 
                               num_block=23, num_grow_ch=32, scale=self.scale)
            else:  # RealESRGAN
                model = RRDBNet(num_in_ch=3, num_out_ch=3, num_feat=64, 
                               num_block=23, num_grow_ch=32, scale=self.scale)
            
            # Inicializar upsampler
            model_path = f'models/{self.model_name}.pth'
            
            self.model = RealESRGANer(
                scale=self.scale,
                model_path=model_path,
                model=model,
                tile=self.tile_size,
                tile_pad=self.tile_pad,
                pre_pad=0,
                half=True if self.device == 'cuda' else False,
                device=self.device
            )
            
            print(f"✓ Modelo {self.model_name} cargado exitosamente")
            
        except Exception as e:
            print(f"⚠ Error al cargar modelo: {e}")
            print("Asegúrate de descargar el modelo pre-entrenado")
            
    def enhance(self, image: np.ndarray, **kwargs) -> np.ndarray:
        """
        Aplicar super-resolución
        
        Args:
            image: Imagen BGR
            
        Returns:
            Imagen mejorada con mayor resolución
        """
        if self.model is None:
            self.load_model()
            
        try:
            output, _ = self.model.enhance(image, outscale=self.scale)
            return output
        except Exception as e:
            print(f"Error en super-resolución: {e}")
            return image


class Denoiser(ImageEnhancer):
    """
    Reducción de ruido en imágenes
    """
    
    def __init__(self, 
                 method: str = 'bilateral',
                 strength: float = 0.5,
                 device: str = 'cuda'):
        """
        Inicializar denoiser
        
        Args:
            method: Método de denoising ('bilateral', 'nlm', 'gaussian')
            strength: Intensidad del denoising (0.0 - 1.0)
            device: Dispositivo de procesamiento
        """
        super().__init__(device)
        self.method = method
        self.strength = strength
        
    def enhance(self, image: np.ndarray, **kwargs) -> np.ndarray:
        """
        Aplicar reducción de ruido
        
        Args:
            image: Imagen BGR
            
        Returns:
            Imagen sin ruido
        """
        if self.method == 'bilateral':
            # Bilateral filter preserva bordes
            d = int(9 * self.strength)
            sigma_color = int(75 * self.strength)
            sigma_space = int(75 * self.strength)
            return cv2.bilateralFilter(image, d, sigma_color, sigma_space)
            
        elif self.method == 'nlm':
            # Non-local means denoising
            h = int(10 * self.strength)
            return cv2.fastNlMeansDenoisingColored(image, None, h, h, 7, 21)
            
        elif self.method == 'gaussian':
            # Gaussian blur (más simple)
            kernel_size = int(5 * self.strength)
            if kernel_size % 2 == 0:
                kernel_size += 1
            return cv2.GaussianBlur(image, (kernel_size, kernel_size), 0)
            
        else:
            print(f"Método {self.method} no reconocido")
            return image


class Sharpener(ImageEnhancer):
    """
    Mejora de nitidez en imágenes
    """
    
    def __init__(self, strength: float = 0.3, device: str = 'cuda'):
        """
        Inicializar sharpener
        
        Args:
            strength: Intensidad del sharpening (0.0 - 1.0)
            device: Dispositivo de procesamiento
        """
        super().__init__(device)
        self.strength = strength
        
    def enhance(self, image: np.ndarray, **kwargs) -> np.ndarray:
        """
        Aplicar sharpening
        
        Args:
            image: Imagen BGR
            
        Returns:
            Imagen con mayor nitidez
        """
        # Unsharp masking
        gaussian = cv2.GaussianBlur(image, (0, 0), 2.0)
        sharpened = cv2.addWeighted(image, 1.0 + self.strength, 
                                   gaussian, -self.strength, 0)
        return sharpened


class ColorCorrector(ImageEnhancer):
    """
    Corrección de color automática
    """
    
    def __init__(self, 
                 auto_contrast: bool = True,
                 auto_brightness: bool = False,
                 device: str = 'cuda'):
        """
        Inicializar corrector de color
        
        Args:
            auto_contrast: Aplicar contraste automático
            auto_brightness: Aplicar brillo automático
            device: Dispositivo de procesamiento
        """
        super().__init__(device)
        self.auto_contrast = auto_contrast
        self.auto_brightness = auto_brightness
        
        
    def enhance(self, image: np.ndarray, **kwargs) -> np.ndarray:
        """
        Aplicar corrección de color
        
        Args:
            image: Imagen BGR
            
        Returns:
            Imagen con color corregido
        """
        result = image.copy()
        
        if self.auto_contrast:
            # CLAHE (Contrast Limited Adaptive Histogram Equalization)
            lab = cv2.cvtColor(result, cv2.COLOR_BGR2LAB)
            l, a, b = cv2.split(lab)
            
            clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8, 8))
            l = clahe.apply(l)
            
            result = cv2.merge([l, a, b])
            result = cv2.cvtColor(result, cv2.COLOR_LAB2BGR)
            
        if self.auto_brightness:
            # Ajuste automático de brillo
            hsv = cv2.cvtColor(result, cv2.COLOR_BGR2HSV)
            h, s, v = cv2.split(hsv)
            
            mean_v = np.mean(v)
            target_v = 128
            scale = target_v / mean_v if mean_v > 0 else 1.0
            
            v = np.clip(v * scale, 0, 255).astype(np.uint8)
            
            result = cv2.merge([h, s, v])
            result = cv2.cvtColor(result, cv2.COLOR_HSV2BGR)
            
        return result


class EnhancementPipeline:
    """
    Pipeline completo de mejora de imagen
    """
    
    def __init__(self, config: dict):
        """
        Inicializar pipeline de mejora
        
        Args:
            config: Diccionario de configuración
        """
        self.config = config
        self.enhancers = []
        self._setup_pipeline()
        
    def _setup_pipeline(self):
        """Configurar pipeline según config"""
        device = self.config.get('general', {}).get('device', 'cuda')
        enhancement_config = self.config.get('enhancement', {})
        
        # Super-resolución
        if enhancement_config.get('super_resolution', {}).get('enabled', False):
            sr_config = enhancement_config['super_resolution']
            self.enhancers.append(SuperResolution(
                model_name=sr_config.get('model', 'RealESRGAN_x4plus'),
                scale=sr_config.get('scale', 4),
                tile_size=sr_config.get('tile_size', 512),
                tile_pad=sr_config.get('tile_pad', 10),
                device=device
            ))
            
        # Denoising
        if enhancement_config.get('denoising', {}).get('enabled', False):
            dn_config = enhancement_config['denoising']
            self.enhancers.append(Denoiser(
                method=dn_config.get('method', 'bilateral'),
                strength=dn_config.get('strength', 0.5),
                device=device
            ))
            
        # Sharpening
        if enhancement_config.get('sharpening', {}).get('enabled', False):
            sh_config = enhancement_config['sharpening']
            self.enhancers.append(Sharpener(
                strength=sh_config.get('strength', 0.3),
                device=device
            ))
            
        # Color correction
        if enhancement_config.get('color_correction', {}).get('enabled', False):
            cc_config = enhancement_config['color_correction']
            self.enhancers.append(ColorCorrector(
                auto_contrast=cc_config.get('auto_contrast', True),
                auto_brightness=cc_config.get('auto_brightness', False),
                device=device
            ))
            
    def process(self, image: np.ndarray) -> np.ndarray:
        """
        Procesar imagen a través del pipeline
        
        Args:
            image: Imagen BGR
            
        Returns:
            Imagen mejorada
        """
        result = image.copy()
        
        for enhancer in self.enhancers:
            print(f"Aplicando {enhancer.__class__.__name__}...")
            result = enhancer.enhance(result)
            
        return result
