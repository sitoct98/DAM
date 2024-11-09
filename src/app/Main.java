package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/vista/Vista.fxml"));
        primaryStage.setTitle("Diagramas UML");
        primaryStage.setScene(new Scene(root, 1000, 1000));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

















/* 

Resumen de Clases hola
Main: Clase principal que inicia la aplicación JavaFX.
UmlClass: Representa una clase UML con atributos y métodos.
UmlAttribute: Define un atributo en una clase UML, con nombre, tipo y visibilidad.
UmlMethod: Representa un método en una clase UML, incluyendo parámetros y visibilidad.
UmlParameter: Define un parámetro en un método UML.
UmlRelation: Representa una relación entre dos clases UML.
UmlRelationType: Enumeración para los tipos de relaciones UML (Asociación, Agregación, Composición).
Visibility: Enumeración para los niveles de visibilidad (Público, Privado, Protegido).
UmlEditorController: Controlador de la interfaz de usuario, maneja la interacción con la vista.


al elegir 2 clases, quitar la primera de la eleccion
corregir scrollable. no llega hasta abajo
utilizar notacion estandar de las flechas


*/
