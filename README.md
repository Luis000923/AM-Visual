# AM Visual - Aplicación de Marcas de Agua

## Descripción

AM Visual es una aplicación de escritorio que permite añadir marcas de agua personalizadas a múltiples imágenes de forma sencilla y rápida. Ideal para fotografía profesional, portafolios, galerías en línea o cualquier caso donde se necesite proteger o firmar imágenes digitales.

## Características principales

- ✅ Procesamiento por lotes: Aplica marcas de agua a carpetas completas de imágenes
- ✅ Múltiples marcas de agua: Coloca varias marcas de agua en una misma imagen
- ✅ Control total de cada marca de agua:
  - Posicionamiento con arrastrar y soltar
  - Ajuste de opacidad (transparencia)
  - Redimensionamiento interactivo
- ✅ Vista previa en tiempo real
- ✅ Interfaz gráfica intuitiva y moderna
- ✅ Organización de marcas de agua con control de capas

## Requisitos

- Python 3.7+
- Dependencias principales:
  - CustomTkinter
  - Pillow (PIL)
  - Tkinter (incluido en la mayoría de instalaciones de Python)


## Guía de uso

### 1. Seleccionar carpeta de imágenes

1. Haz clic en el botón "Seleccionar Carpeta"
2. Navega y elige la carpeta que contiene las imágenes a procesar
3. La primera imagen se mostrará automáticamente en la vista previa

### 2. Añadir marcas de agua

1. Haz clic en "Añadir Marca de Agua"
2. Selecciona un archivo PNG o JPG para usar como marca de agua (se recomienda PNG con transparencia)
3. La marca de agua aparecerá en la vista previa

### 3. Personalizar marcas de agua

Para cada marca de agua puedes:

- **Posicionar**: Arrastra la marca de agua directamente en la vista previa
- **Cambiar tamaño**: 
  - Utiliza los botones "+" y "-"
  - O la rueda del ratón mientras estás sobre la marca de agua
- **Ajustar opacidad**: Utiliza el deslizador de opacidad para hacer la marca más o menos transparente

### 4. Gestionar múltiples marcas de agua

- Selecciona una marca de agua de la lista desplegable para editarla
- Usa los botones:
  - **Eliminar**: Para quitar una marca de agua
  - **▲** y **▼**: Para cambiar el orden de superposición de las marcas de agua

### 5. Procesar imágenes

1. Especifica el nombre de la carpeta de salida (por defecto: "imagenes_con_marca")
2. Haz clic en "Procesar Imágenes"
3. Espera a que se complete el proceso (la barra de progreso mostrará el avance)
4. Las imágenes procesadas se guardarán en una subcarpeta dentro de la carpeta de origen

## Funcionamiento interno

La aplicación está estructurada en dos clases principales:

- **ImagenFlotante**: Maneja la lógica de cada marca de agua individual
- **AplicacionMarcaAgua**: Implementa la interfaz gráfica y coordina el proceso completo

### Clase `ImagenFlotante`

Esta clase gestiona cada marca de agua y proporciona métodos para:
- Escalar la imagen
- Controlar la transparencia
- Calcular posiciones
- Manejar eventos de arrastrar y soltar

### Clase `AplicacionMarcaAgua`

Controla la interfaz de usuario y proporciona funcionalidades como:
- Selección y procesamiento de archivos
- Vista previa en tiempo real
- Gestión de múltiples marcas de agua
- Aplicación de marcas de agua por lotes

## Limitaciones conocidas

- Soporta archivos de imagen PNG, JPG y JPEG
- El procesamiento de imágenes de muy alta resolución puede ralentizar la previsualización

## Contribuciones

Las contribuciones son bienvenidas. Si deseas mejorar este proyecto:

1. Haz un fork del repositorio
2. Crea una rama para tu funcionalidad (`git checkout -b feature/amazing-feature`)
3. Haz commit de tus cambios (`git commit -m 'Add some amazing feature'`)
4. Push a la rama (`git push origin feature/amazing-feature`)
5. Abre un Pull Request

## Licencia

Este proyecto está licenciado bajo [LICENSE] - ver el archivo LICENSE para más detalles.

## Autor

Vides_2GA © 2025

---

*AM Visual - La forma más sencilla de poner marcas de agua a tus imagenes*
