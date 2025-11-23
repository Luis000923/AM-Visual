"""
Image Processing Pipeline
Pipeline principal para orquestar el procesamiento de imágenes
"""

import yaml
import numpy as np
from pathlib import Path
from typing import Optional, List, Union, Tuple

from ..models.image_enhancement import EnhancementPipeline
from ..models.object_removal import ObjectRemovalPipeline
from ..utils.image_loader import ImageLoader
from ..utils.preprocessor import ImagePreprocessor
from ..utils.postprocessor import ImagePostprocessor


class ImageProcessingPipeline:
    """
    Pipeline principal para procesamiento de imágenes
    """
    
    def __init__(self, config_path: str = 'config/config.yaml'):
        """
        Inicializar pipeline
        
        Args:
            config_path: Ruta al archivo de configuración
        """
        self.config = self._load_config(config_path)
        self.enhancement_pipeline = None
        self.removal_pipeline = None
        
        # Inicializar pipelines según configuración
        self._initialize_pipelines()
        
    def _load_config(self, config_path: str) -> dict:
        """Cargar configuración desde YAML"""
        config_path = Path(config_path)
        
        if not config_path.exists():
            print(f"⚠ Archivo de configuración no encontrado: {config_path}")
            print("Usando configuración por defecto")
            return self._default_config()
            
        try:
            with open(config_path, 'r', encoding='utf-8') as f:
                config = yaml.safe_load(f)
            print(f"✓ Configuración cargada desde {config_path}")
            return config
        except Exception as e:
            print(f"⚠ Error al cargar configuración: {e}")
            return self._default_config()
    
    def _default_config(self) -> dict:
        """Configuración por defecto"""
        return {
            'general': {
                'device': 'cuda',
                'batch_size': 1
            },
            'paths': {
                'input_dir': 'data/input',
                'output_dir': 'data/output',
                'temp_dir': 'data/temp'
            },
            'enhancement': {
                'super_resolution': {'enabled': False},
                'denoising': {'enabled': False},
                'sharpening': {'enabled': False},
                'color_correction': {'enabled': False}
            },
            'object_removal': {
                'detection': {'enabled': False},
                'segmentation': {'enabled': False},
                'inpainting': {'enabled': False}
            },
            'output': {
                'format': 'png',
                'quality': 95,
                'save_intermediate': False,
                'add_suffix': True
            }
        }
    
    def _initialize_pipelines(self):
        """Inicializar sub-pipelines"""
        # Enhancement pipeline
        enhancement_enabled = any([
            self.config.get('enhancement', {}).get('super_resolution', {}).get('enabled', False),
            self.config.get('enhancement', {}).get('denoising', {}).get('enabled', False),
            self.config.get('enhancement', {}).get('sharpening', {}).get('enabled', False),
            self.config.get('enhancement', {}).get('color_correction', {}).get('enabled', False)
        ])
        
        if enhancement_enabled:
            self.enhancement_pipeline = EnhancementPipeline(self.config)
            print("✓ Enhancement pipeline inicializado")
        
        # Object removal pipeline
        removal_enabled = any([
            self.config.get('object_removal', {}).get('detection', {}).get('enabled', False),
            self.config.get('object_removal', {}).get('segmentation', {}).get('enabled', False),
            self.config.get('object_removal', {}).get('inpainting', {}).get('enabled', False)
        ])
        
        if removal_enabled:
            self.removal_pipeline = ObjectRemovalPipeline(self.config)
            print("✓ Object removal pipeline inicializado")
    
    def enhance_image(self, 
                     image_input: Union[str, Path, np.ndarray],
                     output_path: Optional[Union[str, Path]] = None) -> np.ndarray:
        """
        Mejorar calidad de imagen
        
        Args:
            image_input: Ruta a imagen o array numpy
            output_path: Ruta de salida (opcional)
            
        Returns:
            Imagen mejorada
        """
        # Cargar imagen
        if isinstance(image_input, (str, Path)):
            image = ImageLoader.load(image_input)
            input_name = Path(image_input).stem
        else:
            image = image_input
            input_name = "image"
            
        if image is None:
            raise ValueError("No se pudo cargar la imagen")
        
        # Aplicar enhancement
        if self.enhancement_pipeline is None:
            print("⚠ Enhancement pipeline no está habilitado")
            return image
            
        print("\n=== Mejorando calidad de imagen ===")
        enhanced = self.enhancement_pipeline.process(image)
        
        # Guardar si se especifica output_path
        if output_path is not None:
            self._save_output(enhanced, output_path, input_name, suffix='enhanced')
        
        return enhanced
    
    def remove_objects(self,
                      image_input: Union[str, Path, np.ndarray],
                      classes: Optional[List[int]] = None,
                      output_path: Optional[Union[str, Path]] = None) -> Tuple[np.ndarray, np.ndarray]:
        """
        Eliminar objetos de imagen
        
        Args:
            image_input: Ruta a imagen o array numpy
            classes: Clases de objetos a eliminar
            output_path: Ruta de salida (opcional)
            
        Returns:
            (imagen_limpia, máscara)
        """
        # Cargar imagen
        if isinstance(image_input, (str, Path)):
            image = ImageLoader.load(image_input)
            input_name = Path(image_input).stem
        else:
            image = image_input
            input_name = "image"
            
        if image is None:
            raise ValueError("No se pudo cargar la imagen")
        
        # Aplicar object removal
        if self.removal_pipeline is None:
            print("⚠ Object removal pipeline no está habilitado")
            return image, np.zeros(image.shape[:2], dtype=np.uint8)
            
        print("\n=== Eliminando objetos ===")
        result, mask = self.removal_pipeline.remove_objects(image, classes=classes)
        
        # Guardar si se especifica output_path
        if output_path is not None:
            self._save_output(result, output_path, input_name, suffix='cleaned')
            
            # Guardar máscara si save_intermediate está habilitado
            if self.config.get('output', {}).get('save_intermediate', False):
                mask_path = Path(output_path).parent / f"{input_name}_mask.png"
                ImageLoader.save(mask, mask_path)
        
        return result, mask
    
    def process_full(self,
                    image_input: Union[str, Path, np.ndarray],
                    classes: Optional[List[int]] = None,
                    output_path: Optional[Union[str, Path]] = None) -> np.ndarray:
        """
        Pipeline completo: eliminar objetos + mejorar calidad
        
        Args:
            image_input: Ruta a imagen o array numpy
            classes: Clases de objetos a eliminar
            output_path: Ruta de salida (opcional)
            
        Returns:
            Imagen procesada
        """
        # Cargar imagen
        if isinstance(image_input, (str, Path)):
            image = ImageLoader.load(image_input)
            input_name = Path(image_input).stem
        else:
            image = image_input
            input_name = "image"
            
        if image is None:
            raise ValueError("No se pudo cargar la imagen")
        
        print("\n" + "="*50)
        print("PROCESAMIENTO COMPLETO")
        print("="*50)
        
        # Paso 1: Eliminar objetos
        if self.removal_pipeline is not None:
            image, mask = self.removal_pipeline.remove_objects(image, classes=classes)
            
            # Guardar intermedio
            if self.config.get('output', {}).get('save_intermediate', False) and output_path:
                temp_path = Path(self.config['paths']['temp_dir']) / f"{input_name}_step1_cleaned.png"
                ImageLoader.save(image, temp_path)
        
        # Paso 2: Mejorar calidad
        if self.enhancement_pipeline is not None:
            image = self.enhancement_pipeline.process(image)
        
        # Guardar resultado final
        if output_path is not None:
            self._save_output(image, output_path, input_name, suffix='processed')
        
        print("\n✓ Procesamiento completo finalizado")
        return image
    
    def process_batch(self,
                     input_dir: Optional[Union[str, Path]] = None,
                     output_dir: Optional[Union[str, Path]] = None,
                     mode: str = 'full',
                     classes: Optional[List[int]] = None) -> List[Path]:
        """
        Procesar múltiples imágenes
        
        Args:
            input_dir: Directorio de entrada (usa config si None)
            output_dir: Directorio de salida (usa config si None)
            mode: 'enhance', 'remove', o 'full'
            classes: Clases para object removal
            
        Returns:
            Lista de rutas de imágenes procesadas
        """
        # Usar directorios de configuración si no se especifican
        if input_dir is None:
            input_dir = Path(self.config['paths']['input_dir'])
        else:
            input_dir = Path(input_dir)
            
        if output_dir is None:
            output_dir = Path(self.config['paths']['output_dir'])
        else:
            output_dir = Path(output_dir)
            
        # Cargar imágenes
        images = ImageLoader.load_batch(input_dir)
        
        if not images:
            print(f"⚠ No se encontraron imágenes en {input_dir}")
            return []
        
        print(f"\n{'='*50}")
        print(f"PROCESAMIENTO POR LOTES - {len(images)} imágenes")
        print(f"{'='*50}\n")
        
        output_paths = []
        
        # Procesar cada imagen
        for i, (filename, image) in enumerate(images, 1):
            print(f"\n[{i}/{len(images)}] Procesando: {filename}")
            
            # Generar ruta de salida
            output_path = output_dir / filename
            
            try:
                # Procesar según modo
                if mode == 'enhance':
                    self.enhance_image(image, output_path)
                elif mode == 'remove':
                    self.remove_objects(image, classes=classes, output_path=output_path)
                else:  # full
                    self.process_full(image, classes=classes, output_path=output_path)
                
                output_paths.append(output_path)
                
            except Exception as e:
                print(f"⚠ Error al procesar {filename}: {e}")
                continue
        
        print(f"\n{'='*50}")
        print(f"✓ Procesadas {len(output_paths)}/{len(images)} imágenes")
        print(f"{'='*50}")
        
        return output_paths
    
    def _save_output(self, 
                    image: np.ndarray,
                    output_path: Union[str, Path],
                    input_name: str,
                    suffix: str = '') -> bool:
        """
        Guardar imagen de salida
        
        Args:
            image: Imagen a guardar
            output_path: Ruta de salida
            input_name: Nombre original
            suffix: Sufijo a añadir
            
        Returns:
            True si se guardó exitosamente
        """
        output_path = Path(output_path)
        
        # Añadir sufijo si está habilitado
        if self.config.get('output', {}).get('add_suffix', True) and suffix:
            stem = output_path.stem
            ext = output_path.suffix
            output_path = output_path.parent / f"{stem}_{suffix}{ext}"
        
        # Determinar formato
        output_config = self.config.get('output', {})
        format = output_config.get('format', 'png')
        quality = output_config.get('quality', 95)
        
        # Asegurar extensión correcta
        if not output_path.suffix:
            output_path = output_path.with_suffix(f'.{format}')
        
        # Guardar
        return ImageLoader.save(image, output_path, quality=quality)
    
    def visualize_detections(self,
                           image_input: Union[str, Path, np.ndarray],
                           classes: Optional[List[int]] = None,
                           output_path: Optional[Union[str, Path]] = None) -> np.ndarray:
        """
        Visualizar detecciones sin eliminar objetos
        
        Args:
            image_input: Ruta a imagen o array numpy
            classes: Clases a detectar
            output_path: Ruta de salida (opcional)
            
        Returns:
            Imagen con detecciones dibujadas
        """
        # Cargar imagen
        if isinstance(image_input, (str, Path)):
            image = ImageLoader.load(image_input)
            input_name = Path(image_input).stem
        else:
            image = image_input
            input_name = "image"
            
        if image is None:
            raise ValueError("No se pudo cargar la imagen")
        
        if self.removal_pipeline is None:
            print("⚠ Object removal pipeline no está habilitado")
            return image
        
        # Visualizar
        result = self.removal_pipeline.visualize_detection(image, classes=classes)
        
        # Guardar si se especifica
        if output_path is not None:
            self._save_output(result, output_path, input_name, suffix='detections')
        
        return result
