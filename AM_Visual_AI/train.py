"""
Training Script for Image Enhancement Model
Script para entrenar el modelo de mejora de calidad de imágenes
"""

import os
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
from PIL import Image
import numpy as np
from pathlib import Path
from tqdm import tqdm
import yaml
import cv2
from datetime import datetime


class ImageEnhancementDataset(Dataset):
    """
    Dataset para entrenamiento de mejora de imágenes
    Crea pares de imágenes degradadas (LR) y de alta calidad (HR)
    """
    
    def __init__(self, image_dir, transform=None, patch_size=128, scale=2):
        """
        Args:
            image_dir: Directorio con imágenes de entrenamiento
            transform: Transformaciones a aplicar
            patch_size: Tamaño de los patches para entrenamiento
            scale: Factor de escala (2x, 4x, etc.)
        """
        self.image_dir = Path(image_dir)
        self.image_files = list(self.image_dir.glob('*.jpg')) + \
                          list(self.image_dir.glob('*.png'))
        self.transform = transform
        self.patch_size = patch_size
        self.scale = scale
        
        print(f"Loaded {len(self.image_files)} training images")
    
    def __len__(self):
        return len(self.image_files)
    
    def __getitem__(self, idx):
        # Intentar cargar imagen con manejo de errores
        max_retries = 3
        for retry in range(max_retries):
            try:
                # Cargar imagen original (alta calidad)
                img_path = self.image_files[idx]
                
                # Usar PIL con manejo de imágenes truncadas
                with Image.open(img_path) as img:
                    # Cargar completamente la imagen para detectar problemas
                    img.load()
                    hr_image = img.convert('RGB')
                    # Convertir a numpy array
                    hr_image = np.array(hr_image)
                
                # Validar que la imagen tenga dimensiones válidas
                if hr_image.shape[0] < 10 or hr_image.shape[1] < 10:
                    raise ValueError(f"Image too small: {hr_image.shape}")
                
                # Extraer patch aleatorio
                h, w = hr_image.shape[:2]
                if h < self.patch_size or w < self.patch_size:
                    hr_image = cv2.resize(hr_image, (self.patch_size, self.patch_size))
                else:
                    # Random crop
                    top = np.random.randint(0, h - self.patch_size + 1)
                    left = np.random.randint(0, w - self.patch_size + 1)
                    hr_image = hr_image[top:top+self.patch_size, left:left+self.patch_size]
                
                # Crear versión degradada (baja calidad)
                lr_size = self.patch_size // self.scale
                lr_image = cv2.resize(hr_image, (lr_size, lr_size), interpolation=cv2.INTER_CUBIC)
                
                # Aplicar ruido y desenfoque para simular degradación
                if np.random.rand() > 0.5:
                    # Añadir ruido gaussiano
                    noise = np.random.normal(0, 5, lr_image.shape)
                    lr_image = np.clip(lr_image + noise, 0, 255).astype(np.uint8)
                
                if np.random.rand() > 0.5:
                    # Añadir blur
                    kernel_size = np.random.choice([3, 5])
                    lr_image = cv2.GaussianBlur(lr_image, (kernel_size, kernel_size), 0)
                
                # Volver a escalar LR a tamaño original (bicubic interpolation)
                lr_image = cv2.resize(lr_image, (self.patch_size, self.patch_size), 
                                    interpolation=cv2.INTER_CUBIC)
                
                # Convertir a tensor
                hr_image = torch.from_numpy(hr_image).permute(2, 0, 1).float() / 255.0
                lr_image = torch.from_numpy(lr_image).permute(2, 0, 1).float() / 255.0
                
                if self.transform:
                    hr_image = self.transform(hr_image)
                    lr_image = self.transform(lr_image)
                
                # Si llegamos aquí, la imagen se cargó exitosamente
                break
                
            except (OSError, IOError, ValueError) as e:
                # Si falla, intentar con otra imagen
                print(f"\n⚠️  Error loading {img_path.name}: {e}")
                if retry < max_retries - 1:
                    # Intentar con la siguiente imagen
                    idx = (idx + 1) % len(self.image_files)
                    print(f"    Retrying with image {idx}...")
                else:
                    # Última opción: crear una imagen sintética
                    print(f"    Creating synthetic image as fallback...")
                    hr_image = torch.rand(3, self.patch_size, self.patch_size)
                    lr_image = torch.rand(3, self.patch_size, self.patch_size)
        
        return lr_image, hr_image


class EnhancementNet(nn.Module):
    """
    Red neuronal simple para mejora de imágenes
    Basada en arquitectura SRCNN mejorada
    """
    
    def __init__(self, num_channels=3):
        super(EnhancementNet, self).__init__()
        
        # Feature extraction
        self.conv1 = nn.Conv2d(num_channels, 64, kernel_size=9, padding=4)
        self.relu1 = nn.ReLU(inplace=True)
        
        # Non-linear mapping
        self.conv2 = nn.Conv2d(64, 32, kernel_size=5, padding=2)
        self.relu2 = nn.ReLU(inplace=True)
        
        self.conv3 = nn.Conv2d(32, 32, kernel_size=5, padding=2)
        self.relu3 = nn.ReLU(inplace=True)
        
        # Reconstruction
        self.conv4 = nn.Conv2d(32, num_channels, kernel_size=5, padding=2)
        
        # Residual connection
        self.use_residual = True
    
    def forward(self, x):
        identity = x
        
        out = self.relu1(self.conv1(x))
        out = self.relu2(self.conv2(out))
        out = self.relu3(self.conv3(out))
        out = self.conv4(out)
        
        if self.use_residual:
            out = out + identity
        
        return torch.clamp(out, 0, 1)


class Trainer:
    """
    Clase para entrenar el modelo de mejora de imágenes
    """
    
    def __init__(self, config):
        self.config = config
        self.device = self._setup_device()
        
        # Crear modelo
        self.model = EnhancementNet(num_channels=3).to(self.device)
        
        # Optimizador
        self.optimizer = optim.Adam(
            self.model.parameters(),
            lr=config['learning_rate'],
            betas=(0.9, 0.999)
        )
        
        # Scheduler para ajustar learning rate
        self.scheduler = optim.lr_scheduler.StepLR(
            self.optimizer,
            step_size=config['lr_decay_step'],
            gamma=config['lr_decay_gamma']
        )
        
        # Loss function
        self.criterion = nn.L1Loss()  # L1 loss es mejor que MSE para imágenes
        
        # Métricas adicionales
        self.psnr_criterion = self.calculate_psnr
        
        # Checkpoint directory
        self.checkpoint_dir = Path(config['checkpoint_dir'])
        self.checkpoint_dir.mkdir(parents=True, exist_ok=True)
        
        # Logging
        self.log_file = self.checkpoint_dir / 'training_log.txt'
        self.best_psnr = 0
    
    def _setup_device(self):
        """Configurar dispositivo de entrenamiento"""
        if torch.cuda.is_available():
            device = 'cuda'
            print(f"Using GPU: {torch.cuda.get_device_name(0)}")
        else:
            device = 'cpu'
            print("Using CPU")
        return device
    
    def calculate_psnr(self, img1, img2):
        """Calcular PSNR entre dos imágenes"""
        mse = torch.mean((img1 - img2) ** 2)
        if mse == 0:
            return float('inf')
        return 20 * torch.log10(1.0 / torch.sqrt(mse))
    
    def train_epoch(self, train_loader, epoch):
        """Entrenar una época"""
        self.model.train()
        epoch_loss = 0
        epoch_psnr = 0
        
        pbar = tqdm(train_loader, desc=f'Epoch {epoch}')
        for batch_idx, (lr_images, hr_images) in enumerate(pbar):
            lr_images = lr_images.to(self.device)
            hr_images = hr_images.to(self.device)
            
            # Forward pass
            self.optimizer.zero_grad()
            sr_images = self.model(lr_images)
            
            # Calcular loss
            loss = self.criterion(sr_images, hr_images)
            
            # Backward pass
            loss.backward()
            self.optimizer.step()
            
            # Métricas
            epoch_loss += loss.item()
            psnr = self.calculate_psnr(sr_images, hr_images)
            epoch_psnr += psnr.item()
            
            # Actualizar progress bar
            pbar.set_postfix({
                'loss': f'{loss.item():.4f}',
                'PSNR': f'{psnr.item():.2f} dB'
            })
        
        avg_loss = epoch_loss / len(train_loader)
        avg_psnr = epoch_psnr / len(train_loader)
        
        return avg_loss, avg_psnr
    
    def validate(self, val_loader):
        """Validar el modelo"""
        self.model.eval()
        val_loss = 0
        val_psnr = 0
        
        with torch.no_grad():
            for lr_images, hr_images in val_loader:
                lr_images = lr_images.to(self.device)
                hr_images = hr_images.to(self.device)
                
                sr_images = self.model(lr_images)
                loss = self.criterion(sr_images, hr_images)
                psnr = self.calculate_psnr(sr_images, hr_images)
                
                val_loss += loss.item()
                val_psnr += psnr.item()
        
        avg_loss = val_loss / len(val_loader)
        avg_psnr = val_psnr / len(val_loader)
        
        return avg_loss, avg_psnr
    
    def save_checkpoint(self, epoch, loss, psnr, is_best=False):
        """Guardar checkpoint del modelo"""
        checkpoint = {
            'epoch': epoch,
            'model_state_dict': self.model.state_dict(),
            'optimizer_state_dict': self.optimizer.state_dict(),
            'loss': loss,
            'psnr': psnr,
            'config': self.config
        }
        
        # Guardar checkpoint regular
        checkpoint_path = self.checkpoint_dir / f'checkpoint_epoch_{epoch}.pth'
        torch.save(checkpoint, checkpoint_path)
        print(f"Checkpoint saved: {checkpoint_path}")
        
        # Guardar mejor modelo
        if is_best:
            best_path = self.checkpoint_dir / 'best_model.pth'
            torch.save(checkpoint, best_path)
            print(f"Best model saved: {best_path}")
    
    def log_metrics(self, epoch, train_loss, train_psnr, val_loss=None, val_psnr=None):
        """Registrar métricas de entrenamiento"""
        log_message = f"Epoch {epoch}: Train Loss={train_loss:.4f}, Train PSNR={train_psnr:.2f}dB"
        if val_loss is not None:
            log_message += f", Val Loss={val_loss:.4f}, Val PSNR={val_psnr:.2f}dB"
        
        print(log_message)
        
        with open(self.log_file, 'a') as f:
            f.write(log_message + '\n')
    
    def train(self, train_loader, val_loader=None):
        """Ciclo completo de entrenamiento"""
        print("=" * 50)
        print("Starting Training")
        print(f"Device: {self.device}")
        print(f"Epochs: {self.config['num_epochs']}")
        print(f"Batch Size: {self.config['batch_size']}")
        print(f"Learning Rate: {self.config['learning_rate']}")
        print("=" * 50)
        
        for epoch in range(1, self.config['num_epochs'] + 1):
            # Entrenar
            train_loss, train_psnr = self.train_epoch(train_loader, epoch)
            
            # Validar
            if val_loader is not None:
                val_loss, val_psnr = self.validate(val_loader)
                self.log_metrics(epoch, train_loss, train_psnr, val_loss, val_psnr)
                
                # Guardar mejor modelo
                is_best = val_psnr > self.best_psnr
                if is_best:
                    self.best_psnr = val_psnr
            else:
                self.log_metrics(epoch, train_loss, train_psnr)
                val_psnr = train_psnr
                is_best = val_psnr > self.best_psnr
                if is_best:
                    self.best_psnr = val_psnr
            
            # Guardar checkpoint
            if epoch % self.config['save_freq'] == 0 or is_best:
                self.save_checkpoint(epoch, train_loss, val_psnr, is_best)
            
            # Ajustar learning rate
            self.scheduler.step()
        
        print("=" * 50)
        print("Training Completed!")
        print(f"Best PSNR: {self.best_psnr:.2f} dB")
        print("=" * 50)


def load_config(config_path='config/config.yaml'):
    """Cargar configuración desde archivo YAML"""
    if os.path.exists(config_path):
        with open(config_path, 'r') as f:
            config = yaml.safe_load(f)
        return config.get('training', {})
    else:
        # Configuración por defecto
        return {
            'data_dir': 'data/input',
            'checkpoint_dir': 'checkpoints',
            'num_epochs': 100,
            'batch_size': 16,
            'learning_rate': 1e-4,
            'lr_decay_step': 30,
            'lr_decay_gamma': 0.5,
            'patch_size': 128,
            'scale': 2,
            'save_freq': 5,
            'val_split': 0.1
        }


def main():
    """Función principal de entrenamiento"""
    # Cargar configuración
    config = load_config()
    
    print("\n" + "=" * 50)
    print("IMAGE ENHANCEMENT MODEL TRAINING")
    print("=" * 50 + "\n")
    
    # Crear dataset
    full_dataset = ImageEnhancementDataset(
        image_dir=config['data_dir'],
        patch_size=config['patch_size'],
        scale=config['scale']
    )
    
    # Dividir en train y validation
    val_size = int(len(full_dataset) * config['val_split'])
    train_size = len(full_dataset) - val_size
    
    train_dataset, val_dataset = torch.utils.data.random_split(
        full_dataset, [train_size, val_size]
    )
    
    print(f"\nDataset Split:")
    print(f"  Training samples: {len(train_dataset)}")
    print(f"  Validation samples: {len(val_dataset)}")
    
    # Crear dataloaders
    train_loader = DataLoader(
        train_dataset,
        batch_size=config['batch_size'],
        shuffle=True,
        num_workers=0,  # Windows compatibility
        pin_memory=torch.cuda.is_available()
    )
    
    val_loader = DataLoader(
        val_dataset,
        batch_size=config['batch_size'],
        shuffle=False,
        num_workers=0,
        pin_memory=torch.cuda.is_available()
    )
    
    # Crear trainer y entrenar
    trainer = Trainer(config)
    trainer.train(train_loader, val_loader)


if __name__ == '__main__':
    main()
