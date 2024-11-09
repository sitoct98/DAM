package app;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;

public class UmlRelation {
    private UmlClass fromClass;
    private UmlClass toClass;
    private UmlRelationType type;
    private String cardinality;
    private Line line;
    private Path arrowHead;
    private Label cardinalityLabel;

    public UmlRelation(UmlClass fromClass, UmlClass toClass, UmlRelationType type, String cardinality) {
        this.fromClass = fromClass;
        this.toClass = toClass;
        this.type = type;
        this.cardinality = cardinality;
        this.cardinalityLabel = new Label(cardinality); // Inicializa la etiqueta de cardinalidad con el valor proporcionado
        configureCardinalityLabel(); // Configura el aspecto de la etiqueta de cardinalidad
    }

    private void configureCardinalityLabel() {
        this.cardinalityLabel.setTextFill(Color.BLACK); // Establece el color del texto de la etiqueta de cardinalidad
        
    }

    // Getters y setters para fromClass, toClass, type, cardinality
    public UmlClass getFromClass() {
        return fromClass;
    }

    public void setFromClass(UmlClass fromClass) {
        this.fromClass = fromClass;
    }

    public UmlClass getToClass() {
        return toClass;
    }

    public void setToClass(UmlClass toClass) {
        this.toClass = toClass;
    }

    public UmlRelationType getType() {
        return type;
    }

    public void setType(UmlRelationType type) {
        this.type = type;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
        this.cardinalityLabel.setText(cardinality); // Actualiza el texto de la etiqueta de cardinalidad
    }

    // Getters y setters para los objetos gráficos
    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public Path getArrowHead() {
        return arrowHead;
    }

    public void setArrowHead(Path arrowHead) {
        this.arrowHead = arrowHead;
    }

    public Label getCardinalityLabel() {
        return cardinalityLabel;
    }

    public void setCardinalityLabel(Label cardinalityLabel) {
        this.cardinalityLabel = cardinalityLabel;
    }
    
    // Método adicional para actualizar la etiqueta de cardinalidad
    public void updateCardinalityLabelPosition(double x, double y) {
        if (cardinalityLabel != null) {
            cardinalityLabel.setLayoutX(x);
            cardinalityLabel.setLayoutY(y);
        }
    }

    @Override
    public String toString() {
        return "UmlRelation{" +
               "fromClass=" + (fromClass != null ? fromClass.getName() : "null") +
               ", toClass=" + (toClass != null ? toClass.getName() : "null") +
               ", type=" + type +
               ", cardinality='" + cardinality + '\'' +
               '}';
    }
}
