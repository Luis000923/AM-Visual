"""
Test suite for utilities
"""

import pytest
import numpy as np
from src.utils.preprocessor import ImagePreprocessor
from src.utils.postprocessor import ImagePostprocessor


@pytest.fixture
def sample_image():
    """Crear imagen de prueba"""
    return np.random.randint(0, 255, (100, 100, 3), dtype=np.uint8)


def test_resize(sample_image):
    """Test para resize"""
    resized = ImagePreprocessor.resize(sample_image, target_size=(50, 50))
    assert resized.shape[:2] == (50, 50)


def test_crop(sample_image):
    """Test para crop"""
    cropped = ImagePreprocessor.crop(sample_image, 10, 10, 50, 50)
    assert cropped.shape[:2] == (50, 50)


def test_brightness_adjustment(sample_image):
    """Test para ajuste de brillo"""
    adjusted = ImagePostprocessor.adjust_brightness(sample_image, factor=1.5)
    assert adjusted.shape == sample_image.shape


def test_contrast_adjustment(sample_image):
    """Test para ajuste de contraste"""
    adjusted = ImagePostprocessor.adjust_contrast(sample_image, factor=1.2)
    assert adjusted.shape == sample_image.shape


if __name__ == '__main__':
    pytest.main([__file__])
