"""
AM Visual AI - Main Entry Point
Punto de entrada principal para el sistema de procesamiento de imágenes
"""

import argparse
import sys
from pathlib import Path

from src.pipeline.pipeline import ImageProcessingPipeline
from src.utils.image_loader import ImageLoader


def parse_arguments():
    """Parsear argumentos de línea de comandos"""
    parser = argparse.ArgumentParser(
        description='AM Visual AI - Mejora de imágenes y eliminación de objetos',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Ejemplos de uso:
  # Mejorar calidad de una imagen
  python main.py --mode enhance --input data/input/photo.jpg --output data/output/

  # Eliminar personas de una imagen
  python main.py --mode remove --input data/input/photo.jpg --classes person

  # Pipeline completo (eliminar + mejorar)
  python main.py --mode full --input data/input/photo.jpg

  # Procesar múltiples imágenes
  python main.py --mode full --batch --input data/input/ --output data/output/

  # Visualizar detecciones
  python main.py --mode detect --input data/input/photo.jpg
        """
    )
    
    parser.add_argument(
        '--mode', '-m',
        type=str,
        choices=['enhance', 'remove', 'full', 'detect'],
        default='full',
        help='Modo de procesamiento: enhance (mejorar), remove (eliminar objetos), full (ambos), detect (solo visualizar)'
    )
    
    parser.add_argument(
        '--input', '-i',
        type=str,
        required=True,
        help='Ruta a imagen o directorio de entrada'
    )
    
    parser.add_argument(
        '--output', '-o',
        type=str,
        default=None,
        help='Ruta de salida (usa config por defecto si no se especifica)'
    )
    
    parser.add_argument(
        '--config', '-c',
        type=str,
        default='config/config.yaml',
        help='Ruta al archivo de configuración'
    )
    
    parser.add_argument(
        '--classes',
        type=str,
        nargs='+',
        default=None,
        help='Clases de objetos a eliminar (ej: person car bicycle)'
    )
    
    parser.add_argument(
        '--batch', '-b',
        action='store_true',
        help='Procesar todas las imágenes en el directorio de entrada'
    )
    
    parser.add_argument(
        '--device',
        type=str,
        choices=['cuda', 'cpu', 'mps'],
        default=None,
        help='Dispositivo de procesamiento (sobreescribe config)'
    )
    
    return parser.parse_args()


def get_class_ids(class_names):
    """
    Convertir nombres de clase a IDs de COCO
    
    Args:
        class_names: Lista de nombres de clases
        
    Returns:
        Lista de IDs o None
    """
    if class_names is None:
        return None
    
    # Mapeo común de COCO dataset
    coco_classes = {
        'person': 0,
        'bicycle': 1,
        'car': 2,
        'motorcycle': 3,
        'airplane': 4,
        'bus': 5,
        'train': 6,
        'truck': 7,
        'boat': 8,
        'traffic light': 9,
        'fire hydrant': 10,
        'stop sign': 11,
        'parking meter': 12,
        'bench': 13,
        'bird': 14,
        'cat': 15,
        'dog': 16,
        'horse': 17,
        'sheep': 18,
        'cow': 19,
        'elephant': 20,
        'bear': 21,
        'zebra': 22,
        'giraffe': 23,
        'backpack': 24,
        'umbrella': 25,
        'handbag': 26,
        'tie': 27,
        'suitcase': 28,
        'frisbee': 29,
        'skis': 30,
        'snowboard': 31,
        'sports ball': 32,
        'kite': 33,
        'baseball bat': 34,
        'baseball glove': 35,
        'skateboard': 36,
        'surfboard': 37,
        'tennis racket': 38,
        'bottle': 39,
        'wine glass': 40,
        'cup': 41,
        'fork': 42,
        'knife': 43,
        'spoon': 44,
        'bowl': 45,
        'banana': 46,
        'apple': 47,
        'sandwich': 48,
        'orange': 49,
        'broccoli': 50,
        'carrot': 51,
        'hot dog': 52,
        'pizza': 53,
        'donut': 54,
        'cake': 55,
        'chair': 56,
        'couch': 57,
        'potted plant': 58,
        'bed': 59,
        'dining table': 60,
        'toilet': 61,
        'tv': 62,
        'laptop': 63,
        'mouse': 64,
        'remote': 65,
        'keyboard': 66,
        'cell phone': 67,
        'microwave': 68,
        'oven': 69,
        'toaster': 70,
        'sink': 71,
        'refrigerator': 72,
        'book': 73,
        'clock': 74,
        'vase': 75,
        'scissors': 76,
        'teddy bear': 77,
        'hair drier': 78,
        'toothbrush': 79
    }
    
    class_ids = []
    for name in class_names:
        name_lower = name.lower()
        if name_lower in coco_classes:
            class_ids.append(coco_classes[name_lower])
        else:
            print(f"⚠ Clase '{name}' no reconocida, se ignorará")
    
    return class_ids if class_ids else None


def main():
    """Función principal"""
    args = parse_arguments()
    
    print("="*60)
    print(" AM VISUAL AI - Image Enhancement & Object Removal")
    print("="*60)
    
    # Inicializar pipeline
    try:
        pipeline = ImageProcessingPipeline(config_path=args.config)
    except Exception as e:
        print(f"❌ Error al inicializar pipeline: {e}")
        sys.exit(1)
    
    # Sobreescribir device si se especificó
    if args.device:
        pipeline.config['general']['device'] = args.device
        print(f"Usando dispositivo: {args.device}")
    
    # Convertir nombres de clase a IDs
    class_ids = get_class_ids(args.classes)
    if class_ids:
        print(f"Clases a eliminar: {args.classes} (IDs: {class_ids})")
    
    # Validar input
    input_path = Path(args.input)
    if not input_path.exists():
        print(f"❌ No se encuentra: {input_path}")
        sys.exit(1)
    
    # Determinar output
    if args.output:
        output_path = Path(args.output)
    else:
        output_path = Path(pipeline.config['paths']['output_dir'])
    
    # Procesar
    try:
        if args.batch or input_path.is_dir():
            # Procesamiento por lotes
            print(f"\nModo: Procesamiento por lotes")
            print(f"Entrada: {input_path}")
            print(f"Salida: {output_path}\n")
            
            output_paths = pipeline.process_batch(
                input_dir=input_path,
                output_dir=output_path,
                mode=args.mode if args.mode != 'detect' else 'full',
                classes=class_ids
            )
            
            print(f"\n✓ Procesadas {len(output_paths)} imágenes")
            
        else:
            # Procesamiento de imagen única
            print(f"\nModo: {args.mode}")
            print(f"Entrada: {input_path}")
            
            if args.mode == 'enhance':
                result = pipeline.enhance_image(
                    input_path,
                    output_path=output_path / input_path.name
                )
                
            elif args.mode == 'remove':
                result, mask = pipeline.remove_objects(
                    input_path,
                    classes=class_ids,
                    output_path=output_path / input_path.name
                )
                
            elif args.mode == 'detect':
                result = pipeline.visualize_detections(
                    input_path,
                    classes=class_ids,
                    output_path=output_path / input_path.name
                )
                
            else:  # full
                result = pipeline.process_full(
                    input_path,
                    classes=class_ids,
                    output_path=output_path / input_path.name
                )
            
            print(f"\n✓ Procesamiento completado")
            
    except Exception as e:
        print(f"\n❌ Error durante el procesamiento: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    
    print("\n" + "="*60)
    print(" Procesamiento finalizado exitosamente")
    print("="*60)


if __name__ == '__main__':
    main()
