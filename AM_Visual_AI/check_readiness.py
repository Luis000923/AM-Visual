"""
Quick test script to verify that everything is ready for training
"""

import sys
import torch
import cv2
import numpy as np
from pathlib import Path

# Fix encoding for Windows console
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

def test_imports():
    """Test that all necessary libraries are installed"""
    print("Testing imports...")
    try:
        import torch
        print(f"[OK] PyTorch {torch.__version__}")
        
        import torchvision
        print(f"[OK] Torchvision {torchvision.__version__}")
        
        import cv2
        print(f"[OK] OpenCV {cv2.__version__}")
        
        import PIL
        print(f"[OK] Pillow {PIL.__version__}")
        
        import numpy
        print(f"[OK] NumPy {numpy.__version__}")
        
        import yaml
        print(f"[OK] PyYAML")
        
        import tqdm
        print(f"[OK] tqdm")
        
        return True
    except ImportError as e:
        print(f"[FAIL] Import failed: {e}")
        return False

def test_cuda():
    """Test CUDA availability"""
    print("\nTesting CUDA...")
    if torch.cuda.is_available():
        print(f"[OK] CUDA available")
        print(f"  Device: {torch.cuda.get_device_name(0)}")
        print(f"  CUDA version: {torch.version.cuda}")
        return True
    else:
        print("[INFO] CUDA not available, will use CPU")
        return False

def test_dataset():
    """Test dataset availability"""
    print("\nTesting dataset...")
    data_dir = Path("data/input")
    
    if not data_dir.exists():
        print(f"[FAIL] Data directory not found: {data_dir}")
        return False
    
    image_files = list(data_dir.glob('*.jpg')) + list(data_dir.glob('*.png'))
    
    if len(image_files) == 0:
        print(f"[FAIL] No images found in {data_dir}")
        return False
    
    print(f"[OK] Found {len(image_files)} training images")
    
    # Test loading a sample image
    sample_img = cv2.imread(str(image_files[0]))
    if sample_img is not None:
        print(f"[OK] Successfully loaded sample image: {image_files[0].name}")
        print(f"  Image shape: {sample_img.shape}")
        return True
    else:
        print(f"[FAIL] Failed to load sample image")
        return False

def test_model():
    """Test model creation"""
    print("\nTesting model creation...")
    try:
        from train import EnhancementNet
        
        model = EnhancementNet(num_channels=3)
        print(f"[OK] Model created successfully")
        
        # Test forward pass
        dummy_input = torch.randn(1, 3, 128, 128)
        with torch.no_grad():
            output = model(dummy_input)
        
        if output.shape == dummy_input.shape:
            print(f"[OK] Model forward pass successful")
            print(f"  Input shape: {dummy_input.shape}")
            print(f"  Output shape: {output.shape}")
            return True
        else:
            print(f"[FAIL] Output shape mismatch")
            return False
            
    except Exception as e:
        print(f"[FAIL] Model test failed: {e}")
        return False

def test_config():
    """Test config file"""
    print("\nTesting configuration...")
    config_path = Path("config/config.yaml")
    
    if not config_path.exists():
        print(f"[FAIL] Config file not found: {config_path}")
        return False
    
    try:
        import yaml
        with open(config_path, 'r') as f:
            config = yaml.safe_load(f)
        
        if 'training' in config:
            print(f"[OK] Config file loaded successfully")
            print(f"  Training config found:")
            for key, value in config['training'].items():
                print(f"    {key}: {value}")
            return True
        else:
            print(f"[FAIL] Training config not found in config file")
            return False
            
    except Exception as e:
        print(f"[FAIL] Config test failed: {e}")
        return False

def main():
    print("=" * 60)
    print("AI MODEL TRAINING READINESS CHECK")
    print("=" * 60)
    
    results = []
    
    # Run all tests
    results.append(("Imports", test_imports()))
    results.append(("CUDA", test_cuda()))
    results.append(("Dataset", test_dataset()))
    results.append(("Model", test_model()))
    results.append(("Config", test_config()))
    
    # Summary
    print("\n" + "=" * 60)
    print("SUMMARY")
    print("=" * 60)
    
    all_passed = True
    for test_name, passed in results:
        status = "[OK] PASS" if passed else "[FAIL] FAIL"
        print(f"{test_name}: {status}")
        if not passed and test_name != "CUDA":  # CUDA is optional
            all_passed = False
    
    print("=" * 60)
    
    if all_passed:
        print("\nAll checks passed! Ready to train!")
        print("\nTo start training, run:")
        print("  python train.py")
        print("\nOr with venv:")
        print(f"  {Path('venv/Scripts/python.exe').absolute()} train.py")
        return 0
    else:
        print("\nSome checks failed. Please fix the issues before training.")
        return 1

if __name__ == '__main__':
    sys.exit(main())
