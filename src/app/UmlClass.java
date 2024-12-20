package app;

import java.util.ArrayList;
import java.util.List;

public class UmlClass {
    private String name;
    private List<UmlAttribute> attributes;
    private List<UmlMethod> methods;
    private double posX;
    private double posY;
    private String borderColor = "black"; // Color por defecto
    private double borderWidth = 1.0; // Ancho del borde por defecto

    public UmlClass(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public double getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(double borderWidth) {
        this.borderWidth = borderWidth;
    }

    public List<UmlAttribute> getAttributes() {
        return new ArrayList<>(attributes); // Devuelve una copia para encapsulamiento
    }

    public List<UmlMethod> getMethods() {
        return new ArrayList<>(methods); // Devuelve una copia para encapsulamiento
    }

    public void addAttribute(UmlAttribute attribute) {
        attributes.add(attribute);
    }

    public void removeAttribute(UmlAttribute attribute) {
        attributes.remove(attribute);
    }

    public void addMethod(UmlMethod method) {
        methods.add(method);
    }

    public void removeMethod(UmlMethod method) {
        methods.remove(method);
    }

    @Override
    public String toString() {
        return "UmlClass{" +
               "name='" + name + '\'' +
               ", attributes=" + attributes +
               ", methods=" + methods +
               '}';
    }
}