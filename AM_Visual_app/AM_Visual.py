# -*- coding: utf-8 -*-
#licencia: MIT
# Desarrollado por: Vides_2GA
# Fecha: 07/05/2025
# Descripción: Aplicación para añadir marcas de agua a imágenes
# Requisitos: customtkinter, PIL (Pillow), tkinter
# Documentado por: Vides_2GA 
# Fecha de documentacion 07/05/2025



################################################################################################################
################################################################################################################
###############                                                                                   ##############
###############     █████╗ ███╗   ███╗    ██╗   ██╗██╗███████╗██╗   ██╗ █████╗ ██╗                ##############
###############    ██╔══██╗████╗ ████║    ██║   ██║██║██╔════╝██║   ██║██╔══██╗██║                ##############
###############    ███████║██╔████╔██║    ██║   ██║██║███████╗██║   ██║███████║██║                ##############
###############    ██╔══██║██║╚██╔╝██║    ╚██╗ ██╔╝██║╚════██║██║   ██║██╔══██║██║                ##############
###############    ██║  ██║██║ ╚═╝ ██║     ╚████╔╝ ██║███████║╚██████╔╝██║  ██║███████╗           ##############
###############    ╚═╝  ╚═╝╚═╝     ╚═╝      ╚═══╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝           ##############
###############                                                                                   ##############
################################################################################################################                                                                  
################################################################################################################



# Importar las librerías necesarias
# Importar las librerías necesarias para la interfaz gráfica y el procesamiento de imágenes
# Importar customtkinter para la interfaz gráfica
# Importar filedialog y messagebox de tkinter para la selección de archivos y mensajes emergentes
# Importar Image y ImageEnhance de PIL para el procesamiento de imágenes
# Importar os para la manipulación de rutas de archivos
# Importar Tuple y List de typing para la anotación de tipos


import customtkinter as ctk
from tkinter import filedialog, messagebox
from PIL import Image, ImageEnhance
import os
from typing import Tuple, List


# Definición de la clase ImagenFlotante para manejar la marca de agua
# Esta clase permite ajustar la posición, escala y opacidad de la imagen de marca de agua

class ImagenFlotante:

    # Definición de la clase ImagenFlotante para manejar la marca de agua
    # Esta clase permite ajustar la posición, escala y opacidad de la imagen de marca de agua

    def __init__(self, imagen):
        self.imagen_original = imagen.convert("RGBA")
        self.posicion_relativa = [0.5, 0.5]
        self.escala = 0.5
        self.opacidad = 0.7
        self.imagen_actual = None
        self.is_moving = False
        self.offset_x, self.offset_y = 0, 0
        self.imagen_base_size = None
        self.actualizar_imagen()
    
    # Método para actualizar la imagen de marca de agua con la escala y opacidad actuales
    # Devuelve la imagen redimensionada y con opacidad ajustada
    def actualizar_imagen(self) -> Image.Image:
        w, h = self.imagen_original.size
        img_redim = self.imagen_original.resize(
            (int(w * self.escala), int(h * self.escala)), 
            Image.LANCZOS
        )
        # Redimensionar la imagen de marca de agua según la escala
        # Aplicar opacidad a la imagen de marca de agua
        alpha = ImageEnhance.Brightness(img_redim.split()[3]).enhance(self.opacidad)
        img_redim.putalpha(alpha)
        
        self.imagen_actual = img_redim
        return img_redim
    # Método para calcular la posición absoluta de la marca de agua en la imagen base
    # Devuelve una tupla con las coordenadas (x, y) de la posición de la marca de agua
    def calcular_posicion_absoluta(self, base_imagen: Image.Image) -> Tuple[int, int]:
        if base_imagen is None:
            return (0, 0)
            
        self.imagen_base_size = base_imagen.size
        w_base, h_base = base_imagen.size
        w_marca, h_marca = self.imagen_actual.size
        
        x_center = int(w_base * self.posicion_relativa[0])
        y_center = int(h_base * self.posicion_relativa[1])
        
        x = x_center - (w_marca // 2)
        y = y_center - (h_marca // 2)
        
        return (x, y)
    
    # Método para dibujar la marca de agua en la imagen base
    # Devuelve la imagen base con la marca de agua aplicada
    # Si no hay imagen de marca de agua, devuelve la imagen base sin cambios
    # La imagen de marca de agua se coloca en la posición calculada
    def dibujar(self, base_imagen: Image.Image) -> Image.Image:
        if not self.imagen_actual:
            return base_imagen

        # Calcular la posición absoluta de la marca de agua en la imagen base
        # y dibujar la marca de agua en la imagen base 

        posicion_absoluta = self.calcular_posicion_absoluta(base_imagen)
        
        # Convertir la imagen base a RGBA para permitir la transparencia
        # Pegar la imagen de marca de agua en la imagen base en la posición calculada
        # y devolver la imagen resultante
        base = base_imagen.convert("RGBA")
        base.paste(self.imagen_actual, posicion_absoluta, self.imagen_actual)
        return base
        
        
        # Método para iniciar el movimiento de la marca de agua
        # Devuelve True si el movimiento se inicia correctamente, False en caso contrario
        # Calcula la posición absoluta de la marca de agua y verifica si el clic está dentro de la imagen


    def iniciar_movimiento(self, x: int, y: int, base_imagen_size: Tuple[int, int]) -> bool:
        if not self.imagen_actual:
            return False
       
       
       
        # Verifica si la imagen de marca de agua está dentro de la imagen base
        # y calcula la posición absoluta de la marca de agua
        # Si el clic está dentro de la imagen, inicia el movimiento
    

        self.imagen_base_size = base_imagen_size
        posicion_absoluta = self.calcular_posicion_absoluta(Image.new("RGB", base_imagen_size))
        ix, iy = posicion_absoluta
        iw, ih = self.imagen_actual.size
        

        # Verifica si el clic está dentro de la imagen de marca de agua
        # y guarda la posición del clic como offset

        if ix <= x <= ix + iw and iy <= y <= iy + ih:
            self.is_moving = True
            self.offset_x = x - ix
            self.offset_y = y - iy
            return True
        return False
    
    # Método para mover la marca de agua a una nueva posición
    # Devuelve True si el movimiento se realiza correctamente, False en caso contrario


    def mover(self, x: int, y: int, base_imagen_size: Tuple[int, int]) -> bool:
        if not self.is_moving or base_imagen_size is None:
            return False
        
        new_x = x - self.offset_x
        new_y = y - self.offset_y
        
        w_marca, h_marca = self.imagen_actual.size
        
        center_x = new_x + (w_marca // 2)
        center_y = new_y + (h_marca // 2)
        
        w_base, h_base = base_imagen_size
        self.posicion_relativa[0] = center_x / w_base
        self.posicion_relativa[1] = center_y / h_base
        
        return True
    
    # Método para finalizar el movimiento de la marca de agua

    def finalizar_movimiento(self):
        self.is_moving = False

    # Método para ajustar la escala de la marca de agua

    def ajustar_escala(self, factor: float):
        self.escala = max(0.1, min(2.0, self.escala * factor))
        self.actualizar_imagen()
    
    # Método para ajustar la opacidad de la marca de agua

    def ajustar_opacidad(self, valor: float):
        self.opacidad = max(0.1, min(1.0, valor))
        self.actualizar_imagen()

# Método para obtener la opacidad de la marca de agua

class AplicacionMarcaAgua(ctk.CTk):
   
   # Método para inicializar la aplicación
    # Configura la ventana principal y los widgets de la interfaz gráfica

    

    def __init__(self):
        super().__init__()
        
        self.carpeta_entrada = ""
        self.carpeta_salida = ""
        self.rutas_marcas_agua = []
        self.imagen_actual = None
        self.imagen_preview = None
        self.lista_imagenes = []
        self.imagenes_flotantes = []
        self.marca_agua_seleccionada = None
        self.indice_marca_seleccionada = -1
        
           
        self.title("AM visual")
        self.geometry("900x700")
        ctk.set_appearance_mode("dark")
        
  

        self._crear_widgets()
        self._configurar_eventos()
    
    # Método para crear los widgets de la interfaz gráfica

    def _crear_widgets(self):
        self.frame_principal = ctk.CTkFrame(self)
        self.frame_principal.pack(fill="both", expand=True, padx=5, pady=5)

    # Método para crear el marco principal de la aplicación

        self.frame_contenido = ctk.CTkFrame(self.frame_principal)
        self.frame_contenido.pack(fill="both", expand=True, padx=5, pady=5)
        
        self._crear_panel_controles()
        self._crear_area_preview()
        self._crear_barra_estado()


     # Método para crear el panel de controles de la aplicación
    # Contiene botones para seleccionar la carpeta de entrada, añadir marcas de agua y procesar imágenes

    def _crear_panel_controles(self):
        self.frame_controles = ctk.CTkFrame(self.frame_contenido, width=250)
        self.frame_controles.pack(side="left", fill="y", padx=5, pady=5)

        # Método para crear el panel de controles de la aplicación
        # Contiene botones para seleccionar la carpeta de entrada, añadir marcas de agua y procesar imágenes

    
        ctk.CTkLabel(
            self.frame_controles, 
            text="Configuración", 
            font=ctk.CTkFont(size=16, weight="bold")
        ).pack(pady=(10, 15))

##########################################################################################################################
#####################                                                                                #####################
#####################  Crear botones para seleccionar la carpeta de entrada y añadir marcas de agua  #####################
#####################                                                                                #####################
##########################################################################################################################
       
        botones = [
            ("Seleccionar Carpeta", self.seleccionar_carpeta_entrada),
            ("Añadir Marca de Agua", self.añadir_marca_agua),
        ]

        # Crear botones para cada acción
        for texto, comando in botones:
            btn = ctk.CTkButton(
                self.frame_controles, 
                text=texto, 
                command=comando, 
                height=35
            )
            btn.pack(pady=5, padx=5, fill="x")
        
        self._crear_lista_marcas_agua()
        self._crear_panel_ajustes_marca()
        
        # Método para crear el campo de entrada para la carpeta de salida

        ctk.CTkLabel(self.frame_controles, text="Carpeta de salida:").pack(anchor="w", padx=5, pady=(15, 0))
        self.entry_carpeta_salida = ctk.CTkEntry(self.frame_controles)
        self.entry_carpeta_salida.pack(fill="x", padx=5, pady=(0, 5))
        self.entry_carpeta_salida.insert(0, "imagenes_con_marca")
        
        self.btn_procesar = ctk.CTkButton(
            self.frame_controles, 
            text="Procesar Imágenes", 
            command=self.procesar_todas_imagenes,
            height=40
        )
        self.btn_procesar.pack(fill="x", padx=5, pady=10)


        # Método para crear la lista de marcas de agua
        # Contiene botones para eliminar, subir y bajar marcas de agua en la lista

    def _crear_lista_marcas_agua(self):
        frame_lista = ctk.CTkFrame(self.frame_controles)
        frame_lista.pack(fill="x", padx=5, pady=5)
        
        ctk.CTkLabel(
            frame_lista, 
            text="Marcas de Agua", 
            font=ctk.CTkFont(size=14, weight="bold")
        ).pack(pady=(5, 5))
        
        self.lista_marcas_var = ctk.StringVar()
        self.lista_marcas_indices = []
        self.lista_marcas = ctk.CTkComboBox(
            frame_lista,
            height=30,
            width=200,
            variable=self.lista_marcas_var,
            values=[],
            command=self.seleccionar_marca_de_lista
        )
        self.lista_marcas.pack(fill="x", padx=5, pady=5)
        
        frame_botones_lista = ctk.CTkFrame(frame_lista, fg_color="transparent")
        frame_botones_lista.pack(fill="x", padx=5, pady=5)


        ## Botones para eliminar, subir y bajar marcas de agua en la lista
        self.btn_eliminar_marca = ctk.CTkButton(
            frame_botones_lista,
            text="Eliminar",
            width=70,
            command=self.eliminar_marca_seleccionada
        )
        self.btn_eliminar_marca.pack(side="left", padx=2)
        
        self.btn_subir_marca = ctk.CTkButton(
            frame_botones_lista,
            text="▲",
            width=40,
            command=lambda: self.mover_marca_en_lista(-1)
        )
        self.btn_subir_marca.pack(side="left", padx=2)
        
        self.btn_bajar_marca = ctk.CTkButton(
            frame_botones_lista,
            text="▼",
            width=40,
            command=lambda: self.mover_marca_en_lista(1)
        )
        self.btn_bajar_marca.pack(side="left", padx=2)

        ## Método para crear el panel de ajustes de la marca de agua
        # Contiene controles para ajustar la opacidad y el tamaño de la marca de agua

    def _crear_panel_ajustes_marca(self):
        self.grupo_ajustes = ctk.CTkFrame(self.frame_controles)
        self.grupo_ajustes.pack(fill="x", padx=5, pady=5)
        
        ctk.CTkLabel(
            self.grupo_ajustes, 
            text="Ajustes de Marca de Agua", 
            font=ctk.CTkFont(size=14, weight="bold")
        ).pack(pady=(5, 5))
        
        ctk.CTkLabel(self.grupo_ajustes, text="Opacidad:").pack(anchor="w", padx=5)
        self.slider_opacidad = ctk.CTkSlider(
            self.grupo_ajustes, 
            from_=0.1, 
            to=1.0, 
            number_of_steps=90
        )
        self.slider_opacidad.set(0.7)
        self.slider_opacidad.pack(fill="x", padx=5, pady=(0, 5))
        self.slider_opacidad.configure(command=self.ajustar_opacidad_marca)
        
        ctk.CTkLabel(self.grupo_ajustes, text="Tamaño:").pack(anchor="w", padx=5)
        self.frame_escala = ctk.CTkFrame(self.grupo_ajustes, fg_color="transparent")
        self.frame_escala.pack(fill="x", padx=5, pady=(0, 5))
        
        ## Botones para aumentar y disminuir el tamaño de la marca de agua
        ## y una etiqueta para mostrar el porcentaje actual de la escala
        self.btn_menos = ctk.CTkButton(
            self.frame_escala, 
            text="-", 
            width=30, 
            command=lambda: self.ajustar_escala_marca(0.9)
        )
        self.btn_menos.pack(side="left", padx=(0, 2))
        
        self.label_escala = ctk.CTkLabel(self.frame_escala, text="100%")
        self.label_escala.pack(side="left", expand=True)
        
        self.btn_mas = ctk.CTkButton(
            self.frame_escala, 
            text="+", 
            width=30, 
            command=lambda: self.ajustar_escala_marca(1.1)
        )
        self.btn_mas.pack(side="right", padx=(2, 0))

        self.grupo_ajustes.pack_forget()

    def _crear_area_preview(self):
        self.frame_preview = ctk.CTkFrame(self.frame_contenido)
        self.frame_preview.pack(side="right", expand=True, fill="both", padx=5, pady=5)
        
        self.frame_imagen = ctk.CTkFrame(self.frame_preview, fg_color="transparent")
        self.frame_imagen.pack(expand=True, fill="both", padx=5, pady=5)
        
        self.label_imagen = ctk.CTkLabel(self.frame_imagen, text="Vista previa de la imagen")
        self.label_imagen.pack(expand=True, pady=10)
        
        self.label_info = ctk.CTkLabel(
            self.frame_preview, 
            text="Imágenes encontradas: 0"
        )
        self.label_info.pack(pady=5)
        

        #   barra de estado para mostrar mensajes y progreso
        # Método para crear la barra de estado
        # Contiene una etiqueta para mostrar el estado y una barra de progreso
        # La barra de progreso se utiliza para mostrar el progreso del procesamiento de imágenes

    def _crear_barra_estado(self):
        self.frame_estado = ctk.CTkFrame(self.frame_principal, height=25)
        self.frame_estado.pack(fill="x", padx=5, pady=(0, 5))
        
        self.label_estado = ctk.CTkLabel(self.frame_estado, text="Listo para comenzar")
        self.label_estado.pack(side="left", padx=10)

        self.label_firma = ctk.CTkLabel(
            self.frame_estado,
            text= "Vides_2GA 2025",
            font=ctk.CTkFont(size=10, slant="italic")
        )
        self.label_firma.pack(side="right", padx=10)
        
        self.barra_progreso = ctk.CTkProgressBar(self.frame_estado)
        self.barra_progreso.pack(side="right", padx=5, fill="x", expand=True)
        self.barra_progreso.set(0)

    def _configurar_eventos(self):
        self.label_imagen.bind("<Button-1>", self.iniciar_movimiento)
        self.label_imagen.bind("<B1-Motion>", self.mover_marca)
        self.label_imagen.bind("<ButtonRelease-1>", self.finalizar_movimiento)
        
        self.label_imagen.bind("<MouseWheel>", self.zoom_marca)
        self.label_imagen.bind("<Button-4>", lambda e: self.zoom_marca(e, 1))
        self.label_imagen.bind("<Button-5>", lambda e: self.zoom_marca(e, -1))

        ## Método para configurar los eventos de la interfaz gráfica
        # Asocia eventos de desplazamiento del mouse para hacer zoom en la marca de agua

    def seleccionar_carpeta_entrada(self):
        ruta = filedialog.askdirectory(title="Seleccione carpeta con imágenes")
        if not ruta:
            return
            
        self.carpeta_entrada = ruta
        self.lista_imagenes = self._obtener_imagenes_en_carpeta(ruta)
        
        if self.lista_imagenes:
            self.cargar_imagen_preview(self.lista_imagenes[0])
            self.label_info.configure(
                text=f"Imágenes encontradas: {len(self.lista_imagenes)}")
            self.actualizar_estado(
                f"Carpeta seleccionada: {os.path.basename(ruta)} - "
                f"{len(self.lista_imagenes)} imágenes encontradas"
            )
        else:
            self.actualizar_estado("No se encontraron imágenes en la carpeta seleccionada")

    # Método para añadir una marca de agua a la lista
    # Abre un cuadro de diálogo para seleccionar la imagen de la marca de agua
    def añadir_marca_agua(self):
        ruta = filedialog.askopenfilename(
            title="Seleccionar Marca de Agua", 
            filetypes=[("Archivos de imagen", "*.png;*.jpg;*.jpeg")]
        )
        if not ruta:
            return
            
        self.rutas_marcas_agua.append(ruta)
        nombre_archivo = os.path.basename(ruta)
        
        try:
            marca = Image.open(ruta)
            self.actualizar_estado(f"Marca de agua añadida: {nombre_archivo}")
            
            imagen_flotante = ImagenFlotante(marca)
            self.imagenes_flotantes.append(imagen_flotante)
            
            valores_actuales = list(self.lista_marcas._values) if hasattr(self.lista_marcas, '_values') else []
            valores_actuales.append(nombre_archivo)
            self.lista_marcas.configure(values=valores_actuales)
            self.lista_marcas_var.set(nombre_archivo)
            self.lista_marcas_indices.append(len(self.imagenes_flotantes) - 1)
            
            self.seleccionar_marca_de_lista(nombre_archivo)
            
            if self.imagen_actual:
                self.actualizar_preview()
        except Exception as e:
            self.actualizar_estado(f"Error al cargar la marca de agua: {e}")
            if ruta in self.rutas_marcas_agua:
                self.rutas_marcas_agua.pop()

       #selecionar la marca de agua de la lista
    def seleccionar_marca_de_lista(self, seleccion):
        if not self.imagenes_flotantes:
            self.marca_agua_seleccionada = None
            self.indice_marca_seleccionada = -1
            self.grupo_ajustes.pack_forget()
            return
            
        # Buscar el índice basado en el nombre del archivo
        nombre_seleccionado = self.lista_marcas_var.get()
        indice = -1
        
        for i, ruta in enumerate(self.rutas_marcas_agua):
            if os.path.basename(ruta) == nombre_seleccionado:
                indice = i
                break
                
        if indice < 0 or indice >= len(self.imagenes_flotantes):
            self.marca_agua_seleccionada = None
            self.indice_marca_seleccionada = -1
            self.grupo_ajustes.pack_forget()
            return
            
        self.indice_marca_seleccionada = indice
        self.marca_agua_seleccionada = self.imagenes_flotantes[indice]
        
        self.grupo_ajustes.pack(fill="x", padx=5, pady=5)
        
        self.slider_opacidad.set(self.marca_agua_seleccionada.opacidad)
        escala_porcentaje = int(self.marca_agua_seleccionada.escala * 100)
        self.label_escala.configure(text=f"{escala_porcentaje}%")
        
        self.actualizar_preview()
        self.actualizar_estado(f"Marca de agua seleccionada: {os.path.basename(self.rutas_marcas_agua[indice])}")


    # Método para eliminar la marca de agua seleccionada de la lista
    # Elimina la marca de agua de las listas internas y actualiza la lista desplegable
    def eliminar_marca_seleccionada(self):
        if self.indice_marca_seleccionada < 0:
            return
            
        indice = self.indice_marca_seleccionada
        nombre_a_eliminar = os.path.basename(self.rutas_marcas_agua[indice])
        
        # Eliminar de las listas internas
        self.rutas_marcas_agua.pop(indice)
        self.imagenes_flotantes.pop(indice)
        
        # Actualizar la lista desplegable
        valores_actuales = list(self.lista_marcas._values) if hasattr(self.lista_marcas, '_values') else []
        if nombre_a_eliminar in valores_actuales:
            valores_actuales.remove(nombre_a_eliminar)
            self.lista_marcas.configure(values=valores_actuales)
        
        # Actualizar la selección
        if self.imagenes_flotantes:
            nuevo_indice = min(indice, len(self.imagenes_flotantes) - 1)
            if nuevo_indice >= 0 and nuevo_indice < len(self.rutas_marcas_agua):
                nuevo_nombre = os.path.basename(self.rutas_marcas_agua[nuevo_indice])
                self.lista_marcas_var.set(nuevo_nombre)
                self.indice_marca_seleccionada = nuevo_indice
                self.marca_agua_seleccionada = self.imagenes_flotantes[nuevo_indice]
            else:
                self.marca_agua_seleccionada = None
                self.indice_marca_seleccionada = -1
                self.lista_marcas_var.set("")
        else:
            self.marca_agua_seleccionada = None
            self.indice_marca_seleccionada = -1
            self.lista_marcas_var.set("")
            self.grupo_ajustes.pack_forget()
            
        self.actualizar_preview()
        self.actualizar_estado(f"Marca de agua eliminada")



        ## Actualizar la lista de índices
    def mover_marca_en_lista(self, direccion):
        if self.indice_marca_seleccionada < 0:
            return
            
        indice_actual = self.indice_marca_seleccionada
        nuevo_indice = indice_actual + direccion
        
        if 0 <= nuevo_indice < len(self.imagenes_flotantes):
            # Intercambiar marcas de agua
            self.imagenes_flotantes[indice_actual], self.imagenes_flotantes[nuevo_indice] = \
                self.imagenes_flotantes[nuevo_indice], self.imagenes_flotantes[indice_actual]
            
            self.rutas_marcas_agua[indice_actual], self.rutas_marcas_agua[nuevo_indice] = \
                self.rutas_marcas_agua[nuevo_indice], self.rutas_marcas_agua[indice_actual]
            
            # Actualizar la lista visual
            nombre_actual = os.path.basename(self.rutas_marcas_agua[indice_actual])
            nombre_nuevo = os.path.basename(self.rutas_marcas_agua[nuevo_indice])
            
            self.lista_marcas.delete(indice_actual)
            self.lista_marcas.insert(indice_actual, nombre_actual)
            self.lista_marcas.delete(nuevo_indice)
            self.lista_marcas.insert(nuevo_indice, nombre_nuevo)
            
            # Seleccionar la marca movida
            self.lista_marcas.select(nuevo_indice)
            self.seleccionar_marca_de_lista(nuevo_indice)
            
            # Actualizar preview
            self.actualizar_preview()
            self.actualizar_estado(f"Marca de agua movida")


        # Eliminar la marca de agua seleccionada si no hay más marcas
    def _obtener_imagenes_en_carpeta(self, ruta: str) -> List[str]:
        extensiones = ('.png', '.jpg', '.jpeg')
        return [
            os.path.join(ruta, f) for f in os.listdir(ruta) 
            if os.path.isfile(os.path.join(ruta, f)) and 
            f.lower().endswith(extensiones)
        ]
        ## Método para cargar la imagen de vista previa
        # Abre la imagen seleccionada y actualiza la vista previa
        # Si hay marcas de agua flotantes, las dibuja en la imagen
        # Si ocurre un error al cargar la imagen, muestra un mensaje de error

    def cargar_imagen_preview(self, ruta_imagen: str):
        try:
            self.imagen_actual = Image.open(ruta_imagen)
            self.actualizar_preview()
            self.actualizar_estado(f"Imagen cargada: {os.path.basename(ruta_imagen)}")
        except Exception as e:
            self.actualizar_estado(f"Error al cargar la imagen: {e}")

    def actualizar_preview(self):
        if not self.imagen_actual:
            return
            
        img = self.imagen_actual.copy()
        
        for imagen_flotante in self.imagenes_flotantes:
            img = imagen_flotante.dibujar(img)
        
        img = img.convert("RGB")
        img.thumbnail((600, 400))
        self.imagen_preview = ctk.CTkImage(light_image=img, dark_image=img, size=img.size)
        self.label_imagen.configure(image=self.imagen_preview, text="")


    # Método para iniciar el movimiento de la marca de agua
    def iniciar_movimiento(self, event):
        if not self.marca_agua_seleccionada:
            return
            
        x, y = self.convertir_coordenadas_evento(event.x, event.y)
        if self.imagen_actual and self.marca_agua_seleccionada.iniciar_movimiento(x, y, self.imagen_actual.size):
            self.actualizar_estado("Moviendo marca de agua...")

    def mover_marca(self, event):
        if not (self.marca_agua_seleccionada and self.marca_agua_seleccionada.is_moving) or not self.imagen_actual:
            return
            
        x, y = self.convertir_coordenadas_evento(event.x, event.y)
        if self.marca_agua_seleccionada.mover(x, y, self.imagen_actual.size):
            self.actualizar_preview()
    
         ## Método para finalizar el movimiento de la marca de agua
        # Finaliza el movimiento de la marca de agua y actualiza el estado
    def finalizar_movimiento(self, event):
        if self.marca_agua_seleccionada:
            self.marca_agua_seleccionada.finalizar_movimiento()
            self.actualizar_estado("Posición de marca de agua actualizada")
        
        ## Método para hacer zoom en la marca de agua
        # Ajusta la escala de la marca de agua según el desplazamiento del mouse
        # Si no hay desplazamiento, utiliza el evento delta para determinar la dirección del zoom
        # Llama al método ajustar_escala_marca para aplicar el zoom
        # Actualiza la etiqueta de escala y la vista previa de la imagen
        # Si no hay marca de agua seleccionada, no hace nada

    def zoom_marca(self, event, delta=None):
        if not self.marca_agua_seleccionada:
            return
            
        if delta is None:
            delta = event.delta / 120
            
        factor = 1.1 if delta > 0 else 0.9
        self.ajustar_escala_marca(factor)

    def ajustar_escala_marca(self, factor: float):
        if not self.marca_agua_seleccionada:
            return
            
        self.marca_agua_seleccionada.ajustar_escala(factor)
        escala_porcentaje = int(self.marca_agua_seleccionada.escala * 100)
        self.label_escala.configure(text=f"{escala_porcentaje}%")
        self.actualizar_preview()

    def ajustar_opacidad_marca(self, valor: float):
        if self.marca_agua_seleccionada:
            self.marca_agua_seleccionada.ajustar_opacidad(float(valor))
            self.actualizar_preview()

    def convertir_coordenadas_evento(self, x: int, y: int) -> Tuple[int, int]:
        if not self.imagen_preview:
            return x, y
            
        label_width = self.label_imagen.winfo_width()
        label_height = self.label_imagen.winfo_height()
        preview_width, preview_height = self.imagen_preview._size
        
        offset_x = (label_width - preview_width) / 2
        offset_y = (label_height - preview_height) / 2
        
        x_ajustado = max(0, min(preview_width, x - offset_x))
        y_ajustado = max(0, min(preview_height, y - offset_y))
        
        if self.imagen_actual:
            w_original, h_original = self.imagen_actual.size
            factor_x = w_original / preview_width
            factor_y = h_original / preview_height
            return int(x_ajustado * factor_x), int(y_ajustado * factor_y)
        
        return int(x_ajustado), int(y_ajustado)
    

        # Método para procesar todas las imágenes en la carpeta de entrada
        # Aplica las marcas de agua flotantes a cada imagen y guarda el resultado en la carpeta de salida
        # Si no hay imágenes o marcas de agua, muestra un mensaje de advertencia
        # Si ocurre un error al procesar una imagen, lo imprime en la consola y continúa con la siguiente imagen

    def procesar_todas_imagenes(self):
        if not self.imagenes_flotantes:
            return messagebox.showwarning(
                "Configuración faltante", 
                "Debe añadir al menos una marca de agua primero"
            )
            
        if not self.lista_imagenes:
            return messagebox.showwarning(
                "Sin imágenes", 
                "No hay imágenes para procesar"
            )
            
        nombre_carpeta = self.entry_carpeta_salida.get().strip()
        if not nombre_carpeta:
            nombre_carpeta = "imagenes_con_marca"
            
            ## Crear la carpeta de salida si no existe


        carpeta_salida = os.path.join(self.carpeta_entrada, nombre_carpeta)
        if not os.path.exists(carpeta_salida):
            os.makedirs(carpeta_salida)
            
        total = len(self.lista_imagenes)
        for i, ruta_imagen in enumerate(self.lista_imagenes):
            try:
                # Actualizar la barra de progreso y el estado
                progreso = (i + 1) / total
                self.barra_progreso.set(progreso)
                self.actualizar_estado(f"Procesando imagen {i+1} de {total}: {os.path.basename(ruta_imagen)}")
                self.update_idletasks()
                
                img = Image.open(ruta_imagen)
                
                for imagen_flotante in self.imagenes_flotantes:
                    img = imagen_flotante.dibujar(img)
                
                img = img.convert("RGB")
                
                nombre_archivo = os.path.basename(ruta_imagen)
                ruta_salida = os.path.join(carpeta_salida, nombre_archivo)
                img.save(ruta_salida)
                
            except Exception as e:
                print(f"Error procesando {ruta_imagen}: {e}")
        
        self.barra_progreso.set(1.0)
        self.actualizar_estado(f"Proceso completado. {total} imágenes procesadas.")
        messagebox.showinfo(
            "Proceso Completado", 
            f"Se han procesado {total} imágenes.\nLas imágenes se guardaron en la carpeta:\n{carpeta_salida}"
        )

         # Método para actualizar la barra de estado con un mensaje
    def actualizar_estado(self, mensaje: str):
        self.label_estado.configure(text=mensaje)
        print(mensaje)

 # Método para cerrar la aplicación
if __name__ == "__main__":
    app = AplicacionMarcaAgua()
    app.mainloop()