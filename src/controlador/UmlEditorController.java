package controlador;

import app.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UmlEditorController {

    @FXML
    private Pane drawingArea;
    @FXML
    private ListView<String> classListView;
    @FXML
    private ListView<String> methodListView;
    @FXML
    private ScrollPane scrollPane;

    private List<UmlClass> classes = new ArrayList<>();
    private List<UmlRelation> relations = new ArrayList<>();
    private ContextMenu currentContextMenu;

    @FXML
    public void initialize() {
        updateClassListView();

        // Hacer el drawing area scrolleable
        scrollPane.setContent(drawingArea);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Listener para cambios de selección en la ListView de clases
        classListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Buscar la clase seleccionada basándose en el nombre de la clase
            UmlClass selectedClass = classes.stream()
                    .filter(umlClass -> umlClass.getName().equals(newValue))
                    .findFirst()
                    .orElse(null);
            // Actualizar la ListView de métodos con los métodos de la clase seleccionada
            if (selectedClass != null) {
                updateMethodListView(selectedClass);
            } else {
                // Si no se encuentra la clase, limpiar la ListView de métodos
                methodListView.setItems(FXCollections.observableArrayList());
            }
        });
        // Manejador para cerrar el menú contextual al hacer clic en cualquier lugar del drawingArea
        drawingArea.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && currentContextMenu != null) {
                currentContextMenu.hide(); // Cierra el menú contextual
                currentContextMenu = null; // Elimina la referencia
            }
        });

    }

    // Boton añadir clase
    @FXML
    private void handleAddClass() {
        String className = "Clase" + (classes.size() + 1);
        UmlClass newClass = new UmlClass(className);
        classes.add(newClass);
        drawClassOnCanvas(newClass);
        updateClassListView();
    }

    // Boton añadir relacion
    @FXML
    private void handleAddRelation() {
        // Pedir al usuario que seleccione el tipo de relación primero
        ChoiceDialog<UmlRelationType> dialogType = new ChoiceDialog<>(UmlRelationType.ASSOCIATION, UmlRelationType.values());
        dialogType.setTitle("Seleccionar Tipo de Relación");
        dialogType.setHeaderText("Seleccione el tipo de relación que desea crear");
        dialogType.setContentText("Tipo de Relación:");
        Optional<UmlRelationType> resultType = dialogType.showAndWait();

        if (!resultType.isPresent()) {
            return; // Salir si no se selecciona un tipo de relación
        }

        UmlRelationType selectedType = resultType.get();

        // Pedir al usuario que seleccione las clases en función del tipo de relación seleccionado
        UmlClass fromClass = null;
        UmlClass toClass = null;

        // Switch para variar el dialogo segun la relación
        switch (selectedType) {
            case INHERITANCE:
                fromClass = chooseClass("Seleccionar Superclase", "Seleccione la superclase:");
                toClass = chooseClass("Seleccionar Subclase", "Seleccione la subclase:");
                break;
            case ASSOCIATION:
            case AGGREGATION:
            case COMPOSITION:
            case REALIZATION:
            case DEPENDENCY:
                // Para los otros tipos, el orden de las clases podría no ser importante
                fromClass = chooseClass("Seleccionar Clase Origen", "Seleccione la clase de origen:");
                toClass = chooseClass("Seleccionar Clase Destino", "Seleccione la clase de destino:");
                break;
        }

        // Verificar que las clases han sido seleccionadas y no son la misma
        if (fromClass != null && toClass != null && !fromClass.equals(toClass)) {
            // Solicitar la cardinalidad
            TextInputDialog cardinalityDialog = new TextInputDialog();
            cardinalityDialog.setTitle("Cardinalidad de la Relación");
            cardinalityDialog.setHeaderText("Introduzca la cardinalidad para la relación\nDeje en blanco para no especificar");
            cardinalityDialog.setContentText("Cardinalidad:");

            Optional<String> cardinalityResult = cardinalityDialog.showAndWait();
            String cardinality = cardinalityResult.orElse("");

            // Crear la relación con la cardinalidad especificada (o en blanco)
            UmlRelation newRelation = new UmlRelation(fromClass, toClass, selectedType, cardinality);
            relations.add(newRelation);
            drawRelation(newRelation);
        } else {
            showAlert("Error de Relación", "Debe seleccionar dos clases diferentes para crear una relación.");
        }
    }

    // Boton eliminar relacion
    @FXML
    private void handleShowRelations() {
        if (relations.isEmpty()) {
            // Si no hay relaciones, muestra un mensaje de alerta
            showAlert("Sin Relaciones", "No existen relaciones para mostrar.");
        } else {
            // Crear una lista de representaciones String de cada relación
            List<String> relationDescriptions = relations.stream()
                    .map(relation -> relation.getFromClass().getName() + " -> "
                    + relation.getToClass().getName() + " : "
                    + relation.getType())
                    .collect(Collectors.toList());

            ChoiceDialog<String> dialog = new ChoiceDialog<>(null, relationDescriptions);
            dialog.setTitle("Eliminar Relación");
            dialog.setHeaderText("Seleccione la relación que desea eliminar:");
            dialog.setContentText("Relaciones:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(selectedRelation -> {
                // Encontrar la relación basada en la descripción seleccionada y eliminarla
                for (UmlRelation relation : relations) {
                    String description = relation.getFromClass().getName() + " -> " + relation.getToClass().getName() + " : " + relation.getType();
                    if (description.equals(selectedRelation)) {
                        deleteRelation(relation);
                        break; // Salir del bucle una vez que se encuentra y elimina la relación
                    }
                }
            });
        }
    }

    // Eliminar linea de la relacion
    private void deleteRelation(UmlRelation relation) {
        // Remover la línea y la flecha del área de dibujo
        drawingArea.getChildren().removeAll(relation.getLine(), relation.getArrowHead(), relation.getCardinalityLabel());
        // Remover la relación de la lista
        relations.remove(relation);
    }

    // Metodo auxiliar para el dialogo de handleAddRelation
    private UmlClass chooseClass(String title, String header) {
        ChoiceDialog<UmlClass> dialog = new ChoiceDialog<>(null, classes);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Clase:");
        Optional<UmlClass> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // Menu click derecho
    private void showContextMenu(UmlClass umlClass, double screenX, double screenY) {
        // Si hay un menu abierto cierralo
        if (currentContextMenu != null) {
            currentContextMenu.hide();
        }
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                createMenuItem("Editar Clase", () -> editClass(umlClass), "edit_icon.png"),
                createMenuItem("Eliminar Clase", () -> deleteClass(umlClass), "delete_icon.png"),
                createMenuItem("Añadir Método", () -> addMethodToClass(umlClass), "add_method_icon.png"),
                createMenuItem("Eliminar Método", () -> deleteMethodFromClass(umlClass), "delete_method_icon.png"),
                createMenuItem("Añadir Atributo", () -> addAttributeToClass(umlClass), "add_atb.png"),
                createMenuItem("Eliminar Atributo", () -> deleteAttributeFromClass(umlClass), "delete_atb.png")
        );
        currentContextMenu = contextMenu; // Guarda la referencia al menú actual
        contextMenu.show(drawingArea, screenX, screenY);
    }

    private MenuItem createMenuItem(String text, Runnable action, String iconName) {
        MenuItem menuItem = new MenuItem(text);
        // Cargar el icono desde el archivo en la carpeta de recursos
        Image icon = new Image(getClass().getResourceAsStream("/vista/" + iconName));
        // Crear una vista de imagen con el icono
        ImageView iconView = new ImageView(icon);
        // Establecer el tamaño del icono
        iconView.setFitHeight(16); // Altura del icono 
        iconView.setFitWidth(16); // Anchura del icono 
        // Establecer el icono para el elemento del menú
        menuItem.setGraphic(iconView);
        menuItem.setOnAction(e -> action.run());
        return menuItem;
    }

    // Implementación de las acciones para cada opción del menú    
    private void editClass(UmlClass umlClass) {
        TextInputDialog dialog = new TextInputDialog(umlClass.getName());
        dialog.setTitle("Editar Clase UML");
        dialog.setHeaderText("Editar nombre de la clase UML");
        dialog.setContentText("Ingrese el nuevo nombre de la clase:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            umlClass.setName(newName);
            updateClassListView();
            updateClassInCanvas(umlClass);
        });
    }

    private void deleteClass(UmlClass umlClass) {
        try {
            // Eliminar las relaciones vinculadas a esta clase
            relations.removeIf(rel -> {
                boolean toRemove = rel.getFromClass().equals(umlClass) || rel.getToClass().equals(umlClass);
                if (toRemove) {
                    // Eliminar las líneas, flechas y etiquetas de cardinalidad de las relaciones del área de dibujo
                    drawingArea.getChildren().removeAll(rel.getLine(), rel.getArrowHead(), rel.getCardinalityLabel());
                }
                return toRemove;
            });

            // Eliminar la clase del área de dibujo
            drawingArea.getChildren().removeIf(node -> {
                if (node.getUserData() instanceof UmlClass) {
                    return node.getUserData().equals(umlClass);
                }
                return false;
            });

            // Eliminar la clase de la lista de clases
            classes.remove(umlClass);

            // Actualizar la lista de clases en la interfaz
            updateClassListView();
        } catch (Exception e) {
            showAlert("Error", "No se pudo eliminar la clase: " + e.getMessage());
        }
    }

    private void addMethodToClass(UmlClass umlClass) {
        Dialog<Pair<String, Visibility>> dialog = new Dialog<>();
        dialog.setTitle("Añadir Método");
        dialog.setHeaderText("Añadir un nuevo método a " + umlClass.getName());

        // Configuración de botones
        ButtonType addButton = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        // Crear los controles para ingresar datos
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField methodNameField = new TextField();
        methodNameField.setPromptText("Nombre del método");

        ChoiceBox<Visibility> visibilityChoiceBox = new ChoiceBox<>();
        visibilityChoiceBox.getItems().setAll(Visibility.values());
        visibilityChoiceBox.setValue(Visibility.PUBLIC); // Valor por defecto

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(methodNameField, 1, 0);
        grid.add(new Label("Visibilidad:"), 0, 1);
        grid.add(visibilityChoiceBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertir los resultados a un par Nombre-Visibilidad cuando el botón Añadir es clickeado.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return new Pair<>(methodNameField.getText(), visibilityChoiceBox.getValue());
            }
            return null;
        });

        Optional<Pair<String, Visibility>> result = dialog.showAndWait();

        result.ifPresent(methodNameVisibility -> {
            String name = methodNameVisibility.getKey();
            Visibility visibility = methodNameVisibility.getValue();
            UmlMethod newMethod = new UmlMethod(name, "void", visibility);
            umlClass.addMethod(newMethod);
            updateMethodListView(umlClass);
            updateClassInCanvas(umlClass);
        });
    }

    private void deleteMethodFromClass(UmlClass umlClass) {
        List<String> methodNames = umlClass.getMethods().stream().map(UmlMethod::getName).collect(Collectors.toList());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, methodNames);
        dialog.setTitle("Eliminar Método");
        dialog.setHeaderText("Seleccione el método a eliminar de " + umlClass.getName());
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            UmlMethod methodToRemove = umlClass.getMethods().stream()
                    .filter(method -> method.getName().equals(name))
                    .findFirst()
                    .orElse(null);
            if (methodToRemove != null) {
                umlClass.removeMethod(methodToRemove);
                updateMethodListView(umlClass);
                updateClassInCanvas(umlClass);
            }
        });
    }

    private void addAttributeToClass(UmlClass umlClass) {
        Dialog<Pair<String, Pair<String, Visibility>>> dialog = new Dialog<>();
        dialog.setTitle("Añadir Atributo");
        dialog.setHeaderText("Añadir un nuevo atributo a " + umlClass.getName());

        // Configuración de botones
        ButtonType addButton = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        // Crear los controles para ingresar datos
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField attributeNameField = new TextField();
        attributeNameField.setPromptText("Nombre del atributo");

        TextField attributeTypeField = new TextField();
        attributeTypeField.setPromptText("Tipo del atributo");

        ChoiceBox<Visibility> visibilityChoiceBox = new ChoiceBox<>();
        visibilityChoiceBox.getItems().setAll(Visibility.values());
        visibilityChoiceBox.setValue(Visibility.PUBLIC); // Valor por defecto

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(attributeNameField, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(attributeTypeField, 1, 1);
        grid.add(new Label("Visibilidad:"), 0, 2);
        grid.add(visibilityChoiceBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convertir los resultados a un par Nombre-(Tipo, Visibilidad) cuando el botón Añadir es clickeado.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return new Pair<>(attributeNameField.getText(), new Pair<>(attributeTypeField.getText(), visibilityChoiceBox.getValue()));
            }
            return null;
        });

        Optional<Pair<String, Pair<String, Visibility>>> result = dialog.showAndWait();

        result.ifPresent(attributeNameTypeVisibility -> {
            String name = attributeNameTypeVisibility.getKey();
            String type = attributeNameTypeVisibility.getValue().getKey();
            Visibility visibility = attributeNameTypeVisibility.getValue().getValue();
            UmlAttribute newAttribute = new UmlAttribute(name, type, visibility);
            umlClass.addAttribute(newAttribute);
            updateClassInCanvas(umlClass);
        });
    }

    private void deleteAttributeFromClass(UmlClass umlClass) {
        List<String> attributeNames = umlClass.getAttributes().stream()
                .map(UmlAttribute::getName)
                .collect(Collectors.toList());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, attributeNames);
        dialog.setTitle("Eliminar Atributo");
        dialog.setHeaderText("Seleccione el atributo a eliminar de " + umlClass.getName());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            UmlAttribute attributeToRemove = umlClass.getAttributes().stream()
                    .filter(attribute -> attribute.getName().equals(name))
                    .findFirst()
                    .orElse(null);

            if (attributeToRemove != null) {
                umlClass.removeAttribute(attributeToRemove);
                updateClassInCanvas(umlClass);
            }
        });
    }

    // Métodos auxiliares 
    private void updateClassListView() {
        classListView.setItems(FXCollections.observableArrayList(classes.stream().map(UmlClass::getName).collect(Collectors.toList())));
        classListView.refresh();
    }

    private void updateMethodListView(UmlClass umlClass) {
        if (umlClass != null) {
            // Convertir los métodos de la clase en una lista observable y asignarla a la ListView de métodos
            List<String> methodInfos = umlClass.getMethods().stream()
                    .map(method -> method.getVisibility().getSymbol() + " " + method.getName() + "()") // Incluye el símbolo de visibilidad
                    .collect(Collectors.toList());
            methodListView.setItems(FXCollections.observableArrayList(methodInfos));
        } else {
            // Si umlClass es null, limpiar la ListView de métodos
            methodListView.setItems(FXCollections.observableArrayList());
        }
        methodListView.refresh(); // Refrescar la vista para mostrar los cambios
    }

    private void updateClassInCanvas(UmlClass umlClass) {
        for (Node node : drawingArea.getChildren()) {
            if (node instanceof VBox && umlClass.equals(((VBox) node).getUserData())) {
                VBox classBox = (VBox) node;

                // Asegúrate de que tienes al menos 3 hijos en el VBox:
                // 0 - Label para el nombre de la clase
                // 1 - VBox para los atributos
                // 2 - VBox para los métodos
                if (classBox.getChildren().size() < 3) {
                    // Si no hay suficientes hijos, añade los VBoxes necesarios.
                    classBox.getChildren().addAll(new VBox(), new VBox());
                }

                // Actualizar el nombre de la clase
                Label classNameLabel = (Label) classBox.getChildren().get(0);
                classNameLabel.setText(umlClass.getName());

                // Actualizar VBox de atributos
                VBox attributesBox = (VBox) classBox.getChildren().get(1);
                attributesBox.getChildren().clear(); // Limpia los atributos existentes
                for (UmlAttribute attribute : umlClass.getAttributes()) {
                    // Incluye el símbolo de visibilidad junto al nombre y tipo del atributo
                    String attributeText = attribute.getVisibility().getSymbol() + " " + attribute.getName() + " : " + attribute.getType();
                    Label attributeLabel = new Label(attributeText);
                    attributesBox.getChildren().add(attributeLabel);
                }

                // Actualizar VBox de métodos
                VBox methodsBox = (VBox) classBox.getChildren().get(2);
                methodsBox.getChildren().clear();
                for (UmlMethod method : umlClass.getMethods()) {
                    // Incluye el símbolo de visibilidad junto al nombre del método
                    String methodText = method.getVisibility().getSymbol() + " " + method.getName() + "()";
                    Label methodLabel = new Label(methodText);
                    methodsBox.getChildren().add(methodLabel);
                }

                break; // Salir del bucle una vez actualizada la clase correspondiente
            }
        }
    }

    // Alerta
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Point2D dragAnchor = new Point2D(0, 0);

private void drawClassOnCanvas(UmlClass umlClass) {
    VBox classBox = new VBox();
    classBox.setStyle("-fx-border-color: black; -fx-background-color: lightgray;");
    classBox.setUserData(umlClass);

    // Usar la posición guardada en lugar de centrar la clase en el lienzo
    classBox.setLayoutX(umlClass.getPosX());
    classBox.setLayoutY(umlClass.getPosY());

    Label classNameLabel = new Label(umlClass.getName());
    classNameLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
    classBox.getChildren().add(classNameLabel);

    VBox methodBox = new VBox();
    methodBox.setStyle("-fx-padding: 5;");
    for (UmlMethod method : umlClass.getMethods()) {
        Label methodLabel = new Label(method.getName() + "()");
        methodBox.getChildren().add(methodLabel);
    }
    classBox.getChildren().add(methodBox);

    // Manejar el arrastre de clase solo con clic izquierdo
    classBox.setOnMousePressed(event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
            dragAnchor = new Point2D(event.getSceneX() - classBox.getLayoutX(), event.getSceneY() - classBox.getLayoutY());
            event.consume();
        }
    });

    classBox.setOnMouseDragged(event -> {
        try {
            if (event.getButton() == MouseButton.PRIMARY) {
                double newX = event.getSceneX() - dragAnchor.getX();
                double newY = event.getSceneY() - dragAnchor.getY();

                // Asegúrate de que el movimiento se mantenga dentro del área de dibujo
                newX = Math.max(newX, 0);
                newY = Math.max(newY, 0);
                newX = Math.min(newX, drawingArea.getWidth() - classBox.getWidth());
                newY = Math.min(newY, drawingArea.getHeight() - classBox.getHeight());

                // Actualiza la posición del VBox de la clase
                classBox.setLayoutX(newX);
                classBox.setLayoutY(newY);

                // Actualiza la posición en el objeto UmlClass
                umlClass.setPosX(newX);
                umlClass.setPosY(newY);

                // Actualiza las relaciones de esta clase
                updateRelationsForClass(umlClass);

                event.consume();
            }
        } catch (Exception e) {
            showAlert("Error al mover la clase", "No se puede mover la clase: " + e.getLocalizedMessage());
        }
    });

    // Asignar el menú contextual directamente al VBox para el clic derecho
    classBox.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.SECONDARY) {
            showContextMenu(umlClass, event.getScreenX(), event.getScreenY());
            event.consume();
        }
    });

    drawingArea.getChildren().add(classBox);
}

    // Apariencia de la línea
    private void drawRelation(UmlRelation relation) {
        VBox fromClassBox = getClassBox(relation.getFromClass());
        VBox toClassBox = getClassBox(relation.getToClass());

        if (fromClassBox != null && toClassBox != null) {
            Point2D startPos = getBorderPoint(fromClassBox, getClassCenter(toClassBox));
            Point2D endPos = getBorderPoint(toClassBox, getClassCenter(fromClassBox));

            Line line = new Line(startPos.getX(), startPos.getY(), endPos.getX(), endPos.getY());
            configureLineAppearance(line, relation.getType());
            drawingArea.getChildren().add(line);

            // Crear una nueva Path para la cabeza de la flecha usando los elementos devueltos por createArrowShape
            Path arrowHead = new Path();
            arrowHead.getElements().addAll(createArrowShape(startPos, endPos, relation.getType()));
            configureArrowHeadFill(arrowHead, relation.getType());
            drawingArea.getChildren().add(arrowHead);
            relation.setLine(line);
            relation.setArrowHead(arrowHead);

            // Añadir la cardinalidad
            Label cardinalityLabel = new Label(relation.getCardinality());
            relation.setCardinalityLabel(cardinalityLabel);
            placeCardinalityLabel(relation, startPos, endPos); // Coloca la etiqueta en su posición
        }
    }

    // Método para configurar el aspecto de la línea según el tipo de relación
    private void configureLineAppearance(Line line, UmlRelationType type) {
        switch (type) {
            case AGGREGATION:
                line.setStroke(Color.BLACK);
                line.setStrokeWidth(2);
                line.getStrokeDashArray().addAll(10d, 5d);
                break;
            case COMPOSITION:
                line.setStroke(Color.BLACK);
                line.setStrokeWidth(3);
                break;
            case INHERITANCE:
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(1);
                line.getStrokeDashArray().addAll(2d, 2d);
                break;
            case ASSOCIATION:
                line.setStroke(Color.BLUE);
                line.setStrokeWidth(2);
                break;
            case REALIZATION:
                line.setStroke(Color.GREEN);
                line.setStrokeWidth(2);
                line.getStrokeDashArray().addAll(25d, 20d);
                break;
            case DEPENDENCY:
                line.setStroke(Color.ORANGE);
                line.setStrokeWidth(2);
                line.getStrokeDashArray().addAll(10d, 5d);
                break;
        }
    }

    // Método para configurar el relleno de la cabeza de la flecha según el tipo de relación
    private void configureArrowHeadFill(Path arrowHead, UmlRelationType type) {
        if (type == UmlRelationType.COMPOSITION) {
            arrowHead.setFill(Color.BLACK); // Relleno negro para composición
        } else {
            arrowHead.setFill(null); // Sin relleno específico para otros tipos
        }
    }

    // Actualizar la linea de la flecha
    private void updateRelationsForClass(UmlClass umlClass) {
        // Encuentra todas las relaciones de esta clase y actualiza sus posiciones
        relations.stream()
                .filter(rel -> rel.getFromClass().equals(umlClass) || rel.getToClass().equals(umlClass))
                .forEach(this::updateRelationPosition);
    }

    // Actualizar la posición de la relación y la cardinalidad
    private void updateRelationPosition(UmlRelation relation) {
        try {
            VBox fromClassBox = getClassBox(relation.getFromClass());
            VBox toClassBox = getClassBox(relation.getToClass());

            if (fromClassBox != null && toClassBox != null) {
                Point2D startPos = getBorderPoint(fromClassBox, getClassCenter(toClassBox));
                Point2D endPos = getBorderPoint(toClassBox, getClassCenter(fromClassBox));

                Line line = relation.getLine();
                line.setStartX(startPos.getX());
                line.setStartY(startPos.getY());
                line.setEndX(endPos.getX());
                line.setEndY(endPos.getY());

                Path arrowHead = relation.getArrowHead();
                arrowHead.getElements().clear();
                arrowHead.getElements().addAll(createArrowShape(startPos, endPos, relation.getType()));

                // Coloca la etiqueta de cardinalidad adecuadamente
                placeCardinalityLabel(relation, startPos, endPos);
            }
        } catch (Exception e) {
            showAlert("Error de Actualización de Relación", "No se puede actualizar la relación: " + e.getLocalizedMessage());
        }
    }

    // Cabeza de la flecha
    private List<PathElement> createArrowShape(Point2D start, Point2D end, UmlRelationType type) {
        List<PathElement> elements = new ArrayList<>();

        // Tamaño de la cabeza de la flecha
        double arrowHeadSize = 10;

        // Calcular la punta de la flecha y los puntos izquierdo y derecho de la base
        double angle = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        Point2D tip = end;
        Point2D left;
        Point2D right;

        // Aquí se utiliza la variable 'type' para ajustar la forma de la flecha
        switch (type) {
            case INHERITANCE:
            case REALIZATION:
                // Flechas con punta vacía
                left = new Point2D(
                        tip.getX() - arrowHeadSize * cos - arrowHeadSize * sin,
                        tip.getY() - arrowHeadSize * sin + arrowHeadSize * cos
                );
                right = new Point2D(
                        tip.getX() - arrowHeadSize * cos + arrowHeadSize * sin,
                        tip.getY() - arrowHeadSize * sin - arrowHeadSize * cos
                );
                break;
            case AGGREGATION:
            case COMPOSITION:
            case ASSOCIATION:
            case DEPENDENCY:
                // Flechas con punta llena
                left = new Point2D(
                        tip.getX() - arrowHeadSize * cos + arrowHeadSize * sin,
                        tip.getY() - arrowHeadSize * sin - arrowHeadSize * cos
                );
                right = new Point2D(
                        tip.getX() - arrowHeadSize * cos - arrowHeadSize * sin,
                        tip.getY() - arrowHeadSize * sin + arrowHeadSize * cos
                );
                break;
            // Otras formas de flechas pueden ser añadidas aquí.
            default:
                // Forma por defecto de la flecha si no se especifica un tipo conocido.
                left = new Point2D(
                        tip.getX() - arrowHeadSize * cos + arrowHeadSize * sin,
                        tip.getY() - arrowHeadSize * sin - arrowHeadSize * cos
                );
                right = new Point2D(
                        tip.getX() - arrowHeadSize * cos - arrowHeadSize * sin,
                        tip.getY() - arrowHeadSize * sin + arrowHeadSize * cos
                );
                break;
        }

        // Agregar los elementos a la lista
        elements.add(new MoveTo(tip.getX(), tip.getY()));
        elements.add(new LineTo(left.getX(), left.getY()));
        elements.add(new LineTo(right.getX(), right.getY()));
        elements.add(new ClosePath()); // Cierra la forma si es necesario

        return elements;
    }

    private Point2D getBorderPoint(VBox classBox, Point2D target) {
        double centerX = classBox.getLayoutX() + classBox.getWidth() / 2.0;
        double centerY = classBox.getLayoutY() + classBox.getHeight() / 2.0;
        double dx = target.getX() - centerX;
        double dy = target.getY() - centerY;
        double theta = Math.atan2(dy, dx);
        double x, y;

        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        if (Math.abs(sinTheta) < Math.abs(cosTheta)) {
            // Intersecta con los lados izquierdo/derecho
            x = cosTheta > 0 ? classBox.getWidth() : 0;
            y = sinTheta * (x - classBox.getWidth() / 2.0) / cosTheta + classBox.getHeight() / 2.0;
        } else {
            // Intersecta con los lados superior/inferior
            y = sinTheta > 0 ? classBox.getHeight() : 0;
            x = cosTheta * (y - classBox.getHeight() / 2.0) / sinTheta + classBox.getWidth() / 2.0;
        }

        x += classBox.getLayoutX();
        y += classBox.getLayoutY();

        return new Point2D(x, y);
    }

    private VBox getClassBox(UmlClass umlClass) {
        for (Node node : drawingArea.getChildren()) {
            if (node instanceof VBox && umlClass.equals(node.getUserData())) {
                return (VBox) node;
            }
        }
        return null;
    }

    private Point2D getClassCenter(VBox classBox) {
        double centerX = classBox.getLayoutX() + classBox.getWidth() / 2.0;
        double centerY = classBox.getLayoutY() + classBox.getHeight() / 2.0;
        return new Point2D(centerX, centerY);
    }

    // Posicion de etiqueta de cardinalidad basándose en la dirección de la línea de la relación
    private void placeCardinalityLabel(UmlRelation relation, Point2D startPos, Point2D endPos) {
        Label cardinalityLabel = relation.getCardinalityLabel();
        double labelOffset = 20; // La distancia desde el final de la línea hasta donde quieres colocar la etiqueta

        // Calcular la posición de la etiqueta más cerca del final de la línea
        double angle = Math.atan2(endPos.getY() - startPos.getY(), endPos.getX() - startPos.getX());
        double labelPosX = endPos.getX() - Math.cos(angle) * labelOffset;
        double labelPosY = endPos.getY() - Math.sin(angle) * labelOffset;

        // Ajustar para que la etiqueta no se superponga con la línea
        double offsetX = Math.sin(angle) * labelOffset;
        double offsetY = Math.cos(angle) * labelOffset;

        // Posición final de la etiqueta
        double labelX = labelPosX + offsetX;
        double labelY = labelPosY - offsetY;

        // Ajusta el ancho y alto de la etiqueta
        if (cardinalityLabel.getWidth() == 0 || cardinalityLabel.getHeight() == 0) {
            cardinalityLabel.applyCss();
            cardinalityLabel.layout();
        }

        labelX -= cardinalityLabel.getWidth() / 2;
        labelY -= cardinalityLabel.getHeight() / 2;

        cardinalityLabel.setLayoutX(labelX);
        cardinalityLabel.setLayoutY(labelY);

        cardinalityLabel.setStyle("-fx-background-color: white; "
                + "-fx-padding: 3px; "
                + "-fx-background-radius: 5px; "
                + "-fx-border-radius: 5px; "
                + "-fx-alignment: center;");

        // Asegurarse de que la etiqueta se agregue al área de dibujo si aún no está presente
        if (!drawingArea.getChildren().contains(cardinalityLabel)) {
            drawingArea.getChildren().add(cardinalityLabel);
        }
    }


    // Método para actualizar la vista en el área de dibujo
    public void actualizarVista() {
        // Limpiar el área de dibujo
        drawingArea.getChildren().clear();

        // Dibujar cada clase UML en el área de dibujo
        for (UmlClass umlClass : classes) {
            drawClassOnCanvas(umlClass);
        }

        // Dibujar cada relación UML en el área de dibujo
        for (UmlRelation relation : relations) {
            drawRelation(relation);
        }
    }

    // Método para guardar el esquema
    @FXML
    public void handleSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar esquema UML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo XML", "*.xml"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                Serializer serializer = new Serializer();
                Document doc = serializer.serializeToXML(classes, relations);
                serializer.saveXMLToFile(doc, file.getAbsolutePath());
                System.out.println("Esquema guardado en " + file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error al guardar el archivo: " + e.getMessage());
            }
        }
    }

    // Método para cargar el esquema
    @FXML
    public void handleLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar esquema UML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo XML", "*.xml"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                Serializer serializer = new Serializer();
                // Deserializar clases y relaciones desde el archivo
                List<UmlClass> loadedClasses = serializer.deserializeFromXML(file.getAbsolutePath());
                List<UmlRelation> loadedRelations = serializer.deserializeRelationsFromXML(file.getAbsolutePath(), loadedClasses);

                // Limpiar datos actuales
                classes.clear();
                relations.clear();
                drawingArea.getChildren().clear();

                // Añadir clases al lienzo
                classes.addAll(loadedClasses);
                for (UmlClass umlClass : classes) {
                    drawClassOnCanvas(umlClass); // Método que asegura que las clases se posicionan gráficamente en el lienzo
                }

                // Añadir relaciones al lienzo
                relations.addAll(loadedRelations);
                for (UmlRelation relation : relations) {
                    drawRelation(relation); // Método que posiciona las relaciones gráficamente
                }

                // Forzar actualización de posiciones de relaciones
                for (UmlClass umlClass : classes) {
                    updateRelationsForClass(umlClass); // Este método actualiza las posiciones de las flechas
                }

                // Actualizar la vista
                actualizarVista();

                // Actualizar las ListViews de clases y métodos
                updateClassListView();
                if (!classes.isEmpty()) {
                    updateMethodListView(classes.get(0));
                }

                System.out.println("Esquema cargado desde " + file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error al cargar el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
