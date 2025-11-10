// Este archivo es el punto de entrada de la aplicación JavaFX.
// Se encarga de cargar la vista principal (MainView.fxml), configurar la escena
// y mostrar la ventana inicial.
// Se conecta con:
// - /com/amvisual/viewfx/MainView.fxml: La definición de la interfaz de usuario.
// - /com/amvisual/viewfx/css/light.css: La hoja de estilos para la apariencia.
// - com.amvisual.controllerfx.MainViewController: El controlador asociado al FXML.
package com.amvisual;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class AMVisualFXApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/amvisual/viewfx/MainView.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        // Estilo por defecto (claro)
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/amvisual/viewfx/css/light.css")).toExternalForm());
        primaryStage.setTitle("AM Visual - Marcas de Agua (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
