"""
Object Removal Module
Módulo para detectar, segmentar y eliminar objetos de imágenes
"""

import torch
import numpy as np
import cv2
from typing import List, Optional, Tuple, Union
from pathlib import Path


class ObjectDetector:
    """
    Detección de objetos utilizando YOLOv8
    """
    
    def __init__(self, 
                 model_name: str = 'yolov8n',
                 confidence_threshold: float = 0.25,
                 iou_threshold: float = 0.45,
                 device: str = 'cuda'):
        """
        Inicializar detector de objetos
        
        Args:
            model_name: Modelo YOLO (yolov8n, yolov8s, yolov8m, yolov8l, yolov8x)
            confidence_threshold: Umbral de confianza (0.0 - 1.0)
            iou_threshold: Umbral IoU para NMS
            device: Dispositivo de procesamiento
        """
        self.model_name = model_name
        self.confidence_threshold = confidence_threshold
        self.iou_threshold = iou_threshold
        self.device = device
        self.model = None
        
    def load_model(self):
        """Cargar modelo YOLOv8"""
        try:
            from ultralytics import YOLO
            
            self.model = YOLO(f'{self.model_name}.pt')
            self.model.to(self.device)
            
            print(f"✓ Modelo {self.model_name} cargado exitosamente")
            
        except Exception as e:
            print(f"⚠ Error al cargar modelo YOLO: {e}")
            
    def detect(self, 
               image: np.ndarray,
               classes: Optional[List[int]] = None) -> List[dict]:
        """
        Detectar objetos en la imagen
        
        Args:
            image: Imagen BGR
            classes: Lista de IDs de clases a detectar (None = todas)
            
        Returns:
            Lista de detecciones con formato:
            [{'bbox': [x1, y1, x2, y2], 'class': int, 'confidence': float}]
        """
        if self.model is None:
            self.load_model()
            
        try:
            # Realizar detección
            results = self.model(
                image,
                conf=self.confidence_threshold,
                iou=self.iou_threshold,
                classes=classes,
                verbose=False
            )
            
            detections = []
            for result in results:
                boxes = result.boxes
                for i in range(len(boxes)):
                    bbox = boxes.xyxy[i].cpu().numpy()
                    cls = int(boxes.cls[i].cpu().numpy())
                    conf = float(boxes.conf[i].cpu().numpy())
                    
                    detections.append({
                        'bbox': bbox.tolist(),
                        'class': cls,
                        'confidence': conf,
                        'class_name': result.names[cls]
                    })
                    
            return detections
            
        except Exception as e:
            print(f"Error en detección: {e}")
            return []
    
    def draw_detections(self, 
                       image: np.ndarray,
                       detections: List[dict]) -> np.ndarray:
        """
        Dibujar detecciones en la imagen
        
        Args:
            image: Imagen BGR
            detections: Lista de detecciones
            
        Returns:
            Imagen con detecciones dibujadas
        """
        result = image.copy()
        
        for det in detections:
            x1, y1, x2, y2 = map(int, det['bbox'])
            label = f"{det['class_name']}: {det['confidence']:.2f}"
            
            # Dibujar bbox
            cv2.rectangle(result, (x1, y1), (x2, y2), (0, 255, 0), 2)
            
            # Dibujar label
            cv2.putText(result, label, (x1, y1 - 10),
                       cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
                       
        return result


class ObjectSegmenter:
    """
    Segmentación de objetos utilizando SAM (Segment Anything Model)
    """
    
    def __init__(self, 
                 model_type: str = 'vit_h',
                 device: str = 'cuda'):
        """
        Inicializar segmentador
        
        Args:
            model_type: Tipo de modelo SAM ('vit_h', 'vit_l', 'vit_b')
            device: Dispositivo de procesamiento
        """
        self.model_type = model_type
        self.device = device
        self.predictor = None
        
    def load_model(self):
        """Cargar modelo SAM"""
        try:
            from segment_anything import sam_model_registry, SamPredictor
            
            checkpoint_path = f"models/sam_{self.model_type}.pth"
            sam = sam_model_registry[self.model_type](checkpoint=checkpoint_path)
            sam.to(device=self.device)
            
            self.predictor = SamPredictor(sam)
            
            print(f"✓ Modelo SAM cargado exitosamente")
            
        except Exception as e:
            print(f"⚠ Error al cargar SAM: {e}")
            print("Descarga el modelo desde: https://github.com/facebookresearch/segment-anything")
            
    def segment_bbox(self, 
                    image: np.ndarray,
                    bbox: List[float]) -> np.ndarray:
        """
        Segmentar objeto usando bounding box
        
        Args:
            image: Imagen RGB
            bbox: [x1, y1, x2, y2]
            
        Returns:
            Máscara binaria
        """
        if self.predictor is None:
            self.load_model()
            
        try:
            # Convertir BGR a RGB
            if len(image.shape) == 3 and image.shape[2] == 3:
                image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            else:
                image_rgb = image
                
            # Set image
            self.predictor.set_image(image_rgb)
            
            # Convertir bbox al formato de SAM
            input_box = np.array(bbox)
            
            # Predecir máscara
            masks, scores, logits = self.predictor.predict(
                box=input_box,
                multimask_output=False
            )
            
            # Retornar la mejor máscara
            return masks[0].astype(np.uint8) * 255
            
        except Exception as e:
            print(f"Error en segmentación: {e}")
            # Fallback: crear máscara del bbox
            mask = np.zeros(image.shape[:2], dtype=np.uint8)
            x1, y1, x2, y2 = map(int, bbox)
            mask[y1:y2, x1:x2] = 255
            return mask
    
    def segment_multiple(self,
                        image: np.ndarray,
                        bboxes: List[List[float]]) -> np.ndarray:
        """
        Segmentar múltiples objetos
        
        Args:
            image: Imagen RGB
            bboxes: Lista de bounding boxes
            
        Returns:
            Máscara combinada
        """
        combined_mask = np.zeros(image.shape[:2], dtype=np.uint8)
        
        for bbox in bboxes:
            mask = self.segment_bbox(image, bbox)
            combined_mask = cv2.bitwise_or(combined_mask, mask)
            
        return combined_mask


class Inpainter:
    """
    Inpainting para rellenar áreas eliminadas
    """
    
    def __init__(self, 
                 method: str = 'lama',
                 dilate_kernel_size: int = 15,
                 device: str = 'cuda'):
        """
        Inicializar inpainter
        
        Args:
            method: Método de inpainting ('lama', 'opencv')
            dilate_kernel_size: Tamaño del kernel para dilatar máscara
            device: Dispositivo de procesamiento
        """
        self.method = method
        self.dilate_kernel_size = dilate_kernel_size
        self.device = device
        self.model = None
        
    def load_model(self):
        """Cargar modelo de inpainting"""
        if self.method == 'lama':
            try:
                # LaMa requiere instalación especial
                print("⚠ LaMa requiere configuración adicional")
                print("Usando OpenCV como fallback")
                self.method = 'opencv'
            except Exception as e:
                print(f"Error al cargar LaMa: {e}")
                self.method = 'opencv'
                
    def inpaint(self, 
                image: np.ndarray,
                mask: np.ndarray) -> np.ndarray:
        """
        Aplicar inpainting
        
        Args:
            image: Imagen BGR
            mask: Máscara binaria (255 = área a rellenar)
            
        Returns:
            Imagen con inpainting aplicado
        """
        # Dilatar máscara para cubrir bordes
        if self.dilate_kernel_size > 0:
            kernel = np.ones((self.dilate_kernel_size, self.dilate_kernel_size), 
                           np.uint8)
            mask = cv2.dilate(mask, kernel, iterations=1)
            
        if self.method == 'opencv':
            # Usar inpainting de OpenCV (rápido pero menos preciso)
            result = cv2.inpaint(image, mask, 3, cv2.INPAINT_TELEA)
            return result
            
        elif self.method == 'lama':
            # Aquí iría la implementación de LaMa
            # Por ahora usamos OpenCV como fallback
            result = cv2.inpaint(image, mask, 3, cv2.INPAINT_TELEA)
            return result
            
        else:
            print(f"Método {self.method} no reconocido")
            return image


class ObjectRemovalPipeline:
    """
    Pipeline completo para eliminación de objetos
    """
    
    def __init__(self, config: dict):
        """
        Inicializar pipeline de eliminación
        
        Args:
            config: Diccionario de configuración
        """
        self.config = config
        self.detector = None
        self.segmenter = None
        self.inpainter = None
        self._setup_pipeline()
        
    def _setup_pipeline(self):
        """Configurar pipeline según config"""
        device = self.config.get('general', {}).get('device', 'cuda')
        removal_config = self.config.get('object_removal', {})
        
        # Detector
        if removal_config.get('detection', {}).get('enabled', False):
            det_config = removal_config['detection']
            self.detector = ObjectDetector(
                model_name=det_config.get('model', 'yolov8n'),
                confidence_threshold=det_config.get('confidence_threshold', 0.25),
                iou_threshold=det_config.get('iou_threshold', 0.45),
                device=device
            )
            
        # Segmenter
        if removal_config.get('segmentation', {}).get('enabled', False):
            seg_config = removal_config['segmentation']
            self.segmenter = ObjectSegmenter(
                device=device
            )
            
        # Inpainter
        if removal_config.get('inpainting', {}).get('enabled', False):
            inp_config = removal_config['inpainting']
            self.inpainter = Inpainter(
                method=inp_config.get('model', 'opencv'),
                dilate_kernel_size=inp_config.get('dilate_kernel_size', 15),
                device=device
            )
            
    def remove_objects(self,
                      image: np.ndarray,
                      classes: Optional[List[int]] = None,
                      manual_masks: Optional[List[np.ndarray]] = None) -> Tuple[np.ndarray, np.ndarray]:
        """
        Eliminar objetos de la imagen
        
        Args:
            image: Imagen BGR
            classes: Clases de objetos a eliminar (None = detectar y usar manual_masks)
            manual_masks: Máscaras manuales para eliminar
            
        Returns:
            (imagen_limpia, máscara_combinada)
        """
        h, w = image.shape[:2]
        combined_mask = np.zeros((h, w), dtype=np.uint8)
        
        # Detección automática
        if classes is not None and self.detector is not None:
            print("Detectando objetos...")
            detections = self.detector.detect(image, classes=classes)
            print(f"Encontrados {len(detections)} objetos")
            
            # Segmentación de objetos detectados
            if len(detections) > 0 and self.segmenter is not None:
                print("Segmentando objetos...")
                bboxes = [det['bbox'] for det in detections]
                combined_mask = self.segmenter.segment_multiple(image, bboxes)
                
        # Añadir máscaras manuales
        if manual_masks is not None:
            for mask in manual_masks:
                combined_mask = cv2.bitwise_or(combined_mask, mask)
                
        # Aplicar inpainting
        if np.any(combined_mask > 0) and self.inpainter is not None:
            print("Aplicando inpainting...")
            result = self.inpainter.inpaint(image, combined_mask)
            return result, combined_mask
        else:
            print("No se encontraron objetos para eliminar")
            return image, combined_mask
    
    def visualize_detection(self,
                          image: np.ndarray,
                          classes: Optional[List[int]] = None) -> np.ndarray:
        """
        Visualizar detecciones sin eliminar
        
        Args:
            image: Imagen BGR
            classes: Clases a detectar
            
        Returns:
            Imagen con detecciones dibujadas
        """
        if self.detector is None:
            print("Detector no habilitado")
            return image
            
        detections = self.detector.detect(image, classes=classes)
        return self.detector.draw_detections(image, detections)
