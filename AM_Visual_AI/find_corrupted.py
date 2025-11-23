"""
Script rápido para encontrar la imagen corrupta específica
"""

import sys
import io
from pathlib import Path
from PIL import Image, ImageFile

# Permitir cargar imágenes truncadas parcialmente para identificarlas
ImageFile.LOAD_TRUNCATED_IMAGES = False

# Fix encoding
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def find_corrupted_images(data_dir='data/input'):
    """
    Encontrar imágenes corruptas rápidamente
    """
    data_path = Path(data_dir)
    image_files = sorted(list(data_path.glob('*.jpg')) + list(data_path.glob('*.png')))
    
    print(f"Buscando imagenes corruptas entre {len(image_files)} archivos...")
    print("=" * 60)
    
    corrupted = []
    checked = 0
    
    for img_path in image_files:
        checked += 1
        if checked % 200 == 0:
            print(f"  Verificadas {checked}/{len(image_files)}...")
        
        try:
            with Image.open(img_path) as img:
                img.load()
                _ = img.convert('RGB')
        except Exception as e:
            error_msg = str(e)
            if 'truncated' in error_msg.lower() or 'cannot identify' in error_msg.lower():
                corrupted.append((img_path, error_msg))
                print(f"\n[X] ENCONTRADA: {img_path.name}")
                print(f"    Error: {error_msg}")
    
    print(f"\n{'=' * 60}")
    print(f"Total verificadas: {checked}")
    print(f"Corruptas encontradas: {len(corrupted)}")
    
    if corrupted:
        print(f"\n{'=' * 60}")
        print("IMAGENES CORRUPTAS:")
        for img_path, error in corrupted:
            print(f"  - {img_path.name}: {error}")
    
    return corrupted

if __name__ == '__main__':
    corrupted = find_corrupted_images()
    
    if corrupted:
        print(f"\n{'=' * 60}")
        print("Para eliminarlas, ejecuta:")
        print("  python clean_dataset.py")
        sys.exit(1)
    else:
        print("\n[OK] No se encontraron imagenes corruptas!")
        sys.exit(0)
