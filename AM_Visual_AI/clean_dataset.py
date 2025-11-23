"""
Script para limpiar imágenes corruptas del dataset
"""

import sys
import io
from pathlib import Path
from PIL import Image
import shutil

# Fix encoding for Windows console
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def check_and_clean_dataset(data_dir='data/input', backup_dir='data/poor_quality_images'):
    """
    Verificar todas las imágenes y mover las corruptas a backup
    """
    data_path = Path(data_dir)
    backup_path = Path(backup_dir)
    backup_path.mkdir(parents=True, exist_ok=True)
    
    image_files = list(data_path.glob('*.jpg')) + list(data_path.glob('*.png'))
    
    print(f"Verificando {len(image_files)} imagenes...")
    print("=" * 60)
    
    corrupted = []
    valid = []
    
    for idx, img_path in enumerate(image_files, 1):
        try:
            # Intentar abrir y cargar completamente la imagen
            with Image.open(img_path) as img:
                img.load()  # Forzar carga completa
                width, height = img.size
                
                # Verificar dimensiones mínimas
                if width < 10 or height < 10:
                    raise ValueError(f"Image too small: {width}x{height}")
                
                # Verificar que se puede convertir a RGB
                _ = img.convert('RGB')
            
            valid.append(img_path)
            
            # Mostrar progreso cada 100 imágenes
            if idx % 100 == 0:
                print(f"  Verificadas {idx}/{len(image_files)} imagenes...")
                
        except Exception as e:
            print(f"\n[X] Imagen corrupta: {img_path.name}")
            print(f"   Error: {e}")
            corrupted.append((img_path, str(e)))
    
    print("\n" + "=" * 60)
    print(f"Verificacion completada:")
    print(f"  [OK] Validas: {len(valid)}")
    print(f"  [X] Corruptas: {len(corrupted)}")
    
    if corrupted:
        print("\n" + "=" * 60)
        print("Moviendo imagenes corruptas a backup...")
        
        for img_path, error in corrupted:
            try:
                # Mover a directorio de backup
                dest = backup_path / img_path.name
                shutil.move(str(img_path), str(dest))
                print(f"  Movida: {img_path.name}")
            except Exception as e:
                print(f"  [!] Error moviendo {img_path.name}: {e}")
        
        print(f"\n[OK] {len(corrupted)} imagenes corruptas movidas a {backup_dir}")
        print(f"[OK] {len(valid)} imagenes validas listas para entrenamiento")
    else:
        print("\n[OK] Todas las imagenes estan en buen estado!")
    
    return len(valid), len(corrupted)

def main():
    print("\n" + "=" * 60)
    print("LIMPIEZA DE DATASET - DETECCION DE IMAGENES CORRUPTAS")
    print("=" * 60 + "\n")
    
    valid_count, corrupted_count = check_and_clean_dataset()
    
    print("\n" + "=" * 60)
    print("RESUMEN FINAL")
    print("=" * 60)
    print(f"Imagenes validas: {valid_count}")
    print(f"Imagenes corruptas removidas: {corrupted_count}")
    
    if corrupted_count > 0:
        print(f"\nLas imagenes corruptas fueron movidas a: data/poor_quality_images/")
        print("Puedes eliminarlas o revisarlas manualmente.")
    
    print("\n[OK] Dataset limpio y listo para entrenamiento!")
    print("=" * 60 + "\n")
    
    return 0 if corrupted_count == 0 else 1

if __name__ == '__main__':
    sys.exit(main())
