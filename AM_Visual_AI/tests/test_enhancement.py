"""
Test suite for Image Enhancement module
"""

import pytest
import numpy as np
import cv2
from src.models.image_enhancement import (
    SuperResolution,
    Denoiser,
    Sharpener,
    ColorCorrector
)


@pytest.fixture
def sample_image():
    """Crear imagen de prueba"""
    return np.random.randint(0, 255, (100, 100, 3), dtype=np.uint8)


def test_denoiser(sample_image):
    """Test para denoiser"""
    denoiser = Denoiser(method='bilateral', strength=0.5, device='cpu')
    result = denoiser.enhance(sample_image)
    
    assert result.shape == sample_image.shape
    assert result.dtype == np.uint8


def test_sharpener(sample_image):
    """Test para sharpener"""
    sharpener = Sharpener(strength=0.3, device='cpu')
    result = sharpener.enhance(sample_image)
    
    assert result.shape == sample_image.shape
    assert result.dtype == np.uint8


def test_color_corrector(sample_image):
    """Test para color corrector"""
    corrector = ColorCorrector(auto_contrast=True, device='cpu')
    result = corrector.enhance(sample_image)
    
    assert result.shape == sample_image.shape
    assert result.dtype == np.uint8


if __name__ == '__main__':
    pytest.main([__file__])
