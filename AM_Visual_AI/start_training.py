"""
Quick Start Training Script
Iniciador rápido para entrenar el modelo
"""

import sys
import subprocess
from pathlib import Path

def print_banner():
    print("\n" + "=" * 70)
    print(" " * 15 + "AI IMAGE ENHANCEMENT TRAINING")
    print("=" * 70 + "\n")

def check_system():
    """Verificación rápida del sistema"""
    print("Verificando sistema...")
    
    result = subprocess.run(
        [sys.executable, "check_readiness.py"],
        capture_output=True,
        text=True
    )
    
    if result.returncode != 0:
        print("\n❌ Sistema no está listo. Ejecuta check_readiness.py para detalles.\n")
        return False
    
    print("✅ Sistema verificado y listo!\n")
    return True

def ask_config():
    """Preguntar al usuario sobre configuración"""
    print("Configuración de entrenamiento:")
    print("-" * 70)
    
    # Opción de prueba rápida
    print("\n¿Quieres hacer una prueba rápida o entrenamiento completo?")
    print("  1. Prueba rápida (5 épocas, ~30 min)")
    print("  2. Entrenamiento medio (50 épocas, ~5 horas)")
    print("  3. Entrenamiento completo (100 épocas, ~10 horas)")
    print("  4. Usar configuración del config.yaml")
    
    while True:
        choice = input("\nElige una opción (1-4): ").strip()
        if choice in ['1', '2', '3', '4']:
            break
        print("Opción inválida. Por favor elige 1, 2, 3 o 4.")
    
    return choice

def update_config(choice):
    """Actualizar configuración según elección"""
    if choice == '4':
        print("\nUsando configuración de config.yaml...\n")
        return
    
    # Mapeo de opciones a epochs
    epochs_map = {'1': 5, '2': 50, '3': 100}
    epochs = epochs_map[choice]
    
    print(f"\nConfigurando para {epochs} épocas...")
    
    # Aquí podrías actualizar el config.yaml si quieres
    # Por ahora solo informamos
    print(f"NOTA: Para {epochs} épocas, edita config.yaml y cambia num_epochs: {epochs}\n")

def start_training():
    """Iniciar el entrenamiento"""
    print("=" * 70)
    print("INICIANDO ENTRENAMIENTO...")
    print("=" * 70)
    print("\nPresiona Ctrl+C para detener en cualquier momento.\n")
    
    try:
        # Ejecutar train.py
        subprocess.run([sys.executable, "train.py"], check=True)
        
        print("\n" + "=" * 70)
        print("✅ ENTRENAMIENTO COMPLETADO!")
        print("=" * 70)
        print("\nRevisa los resultados en:")
        print("  - checkpoints/best_model.pth (mejor modelo)")
        print("  - checkpoints/training_log.txt (logs)")
        print("\n")
        
    except KeyboardInterrupt:
        print("\n\n⚠️  Entrenamiento detenido por el usuario.")
        print("Los checkpoints guardados están en checkpoints/\n")
        
    except subprocess.CalledProcessError as e:
        print(f"\n❌ Error durante el entrenamiento: {e}\n")
        return False
    
    return True

def main():
    print_banner()
    
    # Verificar sistema
    if not check_system():
        return 1
    
    # Preguntar configuración
    choice = ask_config()
    update_config(choice)
    
    # Confirmar inicio
    print("\n¿Listo para comenzar el entrenamiento?")
    confirm = input("Escribe 'SI' para continuar: ").strip().upper()
    
    if confirm != 'SI':
        print("\nEntrenamiento cancelado.\n")
        return 0
    
    # Iniciar entrenamiento
    success = start_training()
    
    return 0 if success else 1

if __name__ == '__main__':
    sys.exit(main())
