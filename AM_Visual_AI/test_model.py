"""
Script para probar los modelos entrenados en imágenes
Carga el mejor modelo y mejora imágenes de prueba
"""

import torch
import cv2
import numpy as np
from pathlib import Path
import sys
import io

# Fix encoding
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Importar el modelo desde train.py
from train import EnhancementNet

# Configuracion 
def load_best_model(checkpoint_path='checkpoints/best_model.pth', device='cpu'):
    """Cargar el mejor modelo entrenado"""
    print(f"Cargando modelo desde: {checkpoint_path}")
    
    model = EnhancementNet(num_channels=3)
    checkpoint = torch.load(checkpoint_path, map_location=device, weights_only=False)
    model.load_state_dict(checkpoint['model_state_dict'])
    model.to(device)
    model.eval()
    
    print(f"[OK] Modelo cargado - Epoca: {checkpoint.get('epoch', '?')} | "
          f"PSNR: {checkpoint.get('psnr', 0):.2f} dB | Loss: {checkpoint.get('loss', 0):.4f}")
    
    return model, checkpoint


def enhance_image(model, image_path, device='cpu', save_path=None):
    """Mejorar una imagen usando el modelo entrenado"""
    img = cv2.imread(str(image_path))
    if img is None:
        raise ValueError(f"No se pudo cargar la imagen: {image_path}")
    
    h, w = img.shape[:2]
    print(f"Procesando: {image_path.name} ({w}x{h})")
    
    # Convertir a tensor
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img_tensor = torch.from_numpy(img_rgb).permute(2, 0, 1).float().div(255.0).unsqueeze(0).to(device)
    
    # Aplicar modelo
    with torch.no_grad():
        enhanced_tensor = model(img_tensor)
    
    # Convertir de vuelta a imagen
    enhanced_img = enhanced_tensor.squeeze(0).cpu().permute(1, 2, 0).mul(255).clamp(0, 255).byte().numpy()
    enhanced_img = cv2.cvtColor(enhanced_img, cv2.COLOR_RGB2BGR)
    
    if save_path:
        cv2.imwrite(str(save_path), enhanced_img)
        print(f"  Guardada: {save_path.name}")
    
    return img, enhanced_img


def create_comparison(original, enhanced, output_path):
    """Crear imagen de comparación lado a lado"""
    h, w = original.shape[:2]
    gap = 20
    
    # Crear canvas y pegar imágenes
    comparison = np.full((h, w * 2 + gap, 3), 255, dtype=np.uint8)
    comparison[:h, :w] = original
    comparison[:h, w + gap:] = enhanced
    
    # Agregar etiquetas
    cv2.putText(comparison, 'ORIGINAL', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
    cv2.putText(comparison, 'MEJORADA', (w + gap + 10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
    
    cv2.imwrite(str(output_path), comparison)
    print(f"  Comparacion: {output_path.name}")


def process_images(model, input_dir='data/input', output_dir='data/output', num_samples=5):
    """Procesar múltiples imágenes de prueba"""
    input_path = Path(input_dir)
    output_path = Path(output_dir)
    output_path.mkdir(parents=True, exist_ok=True)
    
    # Obtener imágenes (jpg y png)
    image_files = sorted(input_path.glob('*.jpg'))[:num_samples] + \
                  sorted(input_path.glob('*.png'))[:num_samples]
    image_files = image_files[:num_samples]
    
    if not image_files:
        print(f"[X] No se encontraron imagenes en {input_dir}")
        return
    
    print(f"\n{'='*60}\nProcesando {len(image_files)} imagenes...\n{'='*60}")
    
    for idx, img_path in enumerate(image_files, 1):
        try:
            print(f"\n[{idx}/{len(image_files)}]")
            enhanced_path = output_path / f"enhanced_{img_path.name}"
            original, enhanced = enhance_image(model, img_path, save_path=enhanced_path)
            
            comparison_path = output_path / f"comparison_{img_path.stem}.jpg"
            create_comparison(original, enhanced, comparison_path)
            
        except Exception as e:
            print(f"  [X] Error: {e}")
    
    print(f"\n{'='*60}\n[OK] Completado! Resultados en: {output_path}\n{'='*60}\n")


def test_specific_checkpoint(checkpoint_name, num_samples=3):
    """Probar un checkpoint específico"""
    checkpoint_path = Path('checkpoints') / checkpoint_name
    
    if not checkpoint_path.exists():
        print(f"[X] Checkpoint no encontrado: {checkpoint_path}")
        return
    
    print(f"\n{'='*60}\nPROBANDO: {checkpoint_name}\n{'='*60}")
    model, _ = load_best_model(checkpoint_path)
    process_images(model, num_samples=num_samples)


def main():
    """
    Menú principal
    """
    print("\n" + "="*60)
def main():
    """Menú principal"""
    print("\n" + "="*60)
    print(" "*15 + "PRUEBA DE MODELOS ENTRENADOS")
    print("="*60 + "\n")
    
    checkpoints_dir = Path('checkpoints')
    if not checkpoints_dir.exists():
        print("[X] No se encontro el directorio de checkpoints")
        return 1
    
    checkpoints = sorted(checkpoints_dir.glob('*.pth'))
    
    print("Checkpoints disponibles:")
    for idx, cp in enumerate(checkpoints, 1):
        print(f"  {idx}. {cp.name}")
    
    print("\nOpciones:")
    print("  1. Usar MEJOR MODELO (best_model.pth) - RECOMENDADO")
    print("  2. Probar un checkpoint especifico")
    print("  3. Salir")
    
    choice = input("\nElige una opcion (1-3): ").strip()
    if choice == '1':
        best_model_path = checkpoints_dir / 'best_model.pth'
        if not best_model_path.exists():
            print("\n[X] best_model.pth no existe")
            epoch_checkpoints = [cp for cp in checkpoints if 'epoch' in cp.name]
            if epoch_checkpoints:
                best_model_path = max(epoch_checkpoints)
                print(f"[!] Usando: {best_model_path.name}")
            else:
                print("[X] No hay checkpoints disponibles")
                return 1
        
        model, _ = load_best_model(best_model_path)
        
        num = input("\n¿Cuantas imagenes? (default=5): ").strip()
        num = int(num) if num.isdigit() else 5
        
        process_images(model, num_samples=num)
        
    elif choice == '2':
        cp_idx = input(f"\nElige checkpoint (1-{len(checkpoints)}): ").strip()
        if cp_idx.isdigit() and 1 <= int(cp_idx) <= len(checkpoints):
            num = input("¿Cuantas imagenes? (default=3): ").strip()
            num = int(num) if num.isdigit() else 3
            test_specific_checkpoint(checkpoints[int(cp_idx)-1].name, num)
        else:
            print("[X] Opcion invalida")
            return 1
    
    elif choice == '3':
        print("\nSaliendo...")
        return 0
    
    else:
        print("\n[X] Opcion invalida")
        return 1
    
    print("\n[OK] Imagenes en: data/output/")
    print("     enhanced_*.jpg = Mejorada | comparison_*.jpg = Comparacion\n")
    
    return 0


if __name__ == '__main__':
    sys.exit(main())
