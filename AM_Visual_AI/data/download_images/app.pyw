# app - Lanzador principal para el módulo de descarga de imágenes
"""
USO:
----
Doble clic en este archivo, o desde PowerShell:

  python app.pyw                                          # Modo diverse por defecto
  python app.pyw --mode diverse --images-per-category 200 # ~2000 imágenes
  python app.pyw --mode sample --count 2000               # Sin API keys
  python app.pyw --mode unsplash --query "landscape" --count 2000

IMPORTANTE: Usa el Python del venv o asegúrate que python-dotenv esté instalado.
"""

from __future__ import annotations
import sys
import os
import subprocess
from pathlib import Path

# Configurar rutas del proyecto
APP_PATH = Path(__file__).resolve()
PROJECT_ROOT = APP_PATH.parents[2]  # Subir desde data/download_images/ a raíz
VENV_PYTHON = PROJECT_ROOT / "venv" / "Scripts" / "python.exe"
DOWNLOAD_SCRIPT = APP_PATH.parent / "download_images.py"


def get_python_executable() -> str:
    """Retorna el ejecutable de Python a usar (venv preferido)."""
    if VENV_PYTHON.exists():
        return str(VENV_PYTHON)
    return sys.executable  # Fallback al Python actual


def main(argv: list[str] | None = None) -> int:
    """Ejecuta el script de descarga usando el Python del venv."""
    
    if not DOWNLOAD_SCRIPT.exists():
        print(f"❌ No se encontró el script: {DOWNLOAD_SCRIPT}")
        return 2
    
    python_exe = get_python_executable()
    
    print(f"""
╔═══════════════════════════════════════════════════════════════╗
║          LANZADOR PRINCIPAL - DESCARGA DE IMÁGENES           ║
║                    AM Visual AI Project                       ║
╚═══════════════════════════════════════════════════════════════╝

📂 Proyecto: {PROJECT_ROOT}
🐍 Python:   {python_exe}
📄 Script:   {DOWNLOAD_SCRIPT.name}
📁 Destino:  data/input/
🔑 API Keys: Se cargan desde .env en raíz del proyecto

""")
    
    # Construir comando: python download_images.py + argumentos pasados
    cmd = [python_exe, str(DOWNLOAD_SCRIPT)]
    if argv:
        cmd.extend(argv)
    else:
        # Pasar argumentos de línea de comandos (excepto el nombre del script)
        cmd.extend(sys.argv[1:])
    
    print(f"▶ Ejecutando: {' '.join(cmd)}\n")
    
    # Ejecutar el script en el directorio raíz del proyecto
    try:
        result = subprocess.run(
            cmd,
            cwd=str(PROJECT_ROOT),
            check=False
        )
        return result.returncode
        
    except KeyboardInterrupt:
        print("\n\n⚠️  Descarga interrumpida por el usuario.")
        return 130
    except Exception as e:
        print(f"\n❌ Error durante la ejecución: {e}")
        import traceback
        traceback.print_exc()
        return 1




def main(argv: list[str] | None = None) -> int:
    """Ejecuta el script de descarga usando el Python del venv."""
    
    if not DOWNLOAD_SCRIPT.exists():
        print(f"❌ No se encontró el script: {DOWNLOAD_SCRIPT}")
        return 2
    
    python_exe = get_python_executable()
    
    print(f"""
╔═══════════════════════════════════════════════════════════════╗
║          LANZADOR PRINCIPAL - DESCARGA DE IMÁGENES           ║
║                    AM Visual AI Project                       ║
╚═══════════════════════════════════════════════════════════════╝

📂 Proyecto: {PROJECT_ROOT}
� Python:   {python_exe}
📄 Script:   {DOWNLOAD_SCRIPT.name}
📁 Destino:  data/input/
🔑 API Keys: Se cargan desde .env en raíz del proyecto

""")
    
    # Construir comando: python download_images.py + argumentos pasados
    cmd = [python_exe, str(DOWNLOAD_SCRIPT)]
    if argv:
        cmd.extend(argv)
    else:
        # Pasar argumentos de línea de comandos (excepto el nombre del script)
        cmd.extend(sys.argv[1:])
    
    print(f"▶ Ejecutando: {' '.join(cmd)}\n")
    
    # Ejecutar el script en el directorio raíz del proyecto
    try:
        result = subprocess.run(
            cmd,
            cwd=str(PROJECT_ROOT),
            check=False
        )
        return result.returncode
        
    except KeyboardInterrupt:
        print("\n\n⚠️  Descarga interrumpida por el usuario.")
        return 130
    except Exception as e:
        print(f"\n❌ Error durante la ejecución: {e}")
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())
