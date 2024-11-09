package app;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UmlMethod {
    private String name;
    private String returnType;
    private List<UmlParameter> parameters;
    private Visibility visibility;

    public UmlMethod(String name, String returnType, Visibility visibility) {
        this.name = name;
        this.returnType = returnType;
        this.visibility = visibility; 
        this.parameters = new ArrayList<>();
    }

    // Getters y setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<UmlParameter> getParameters() {
        return new ArrayList<>(parameters); // Devuelve una copia para encapsulamiento
    }

    public void setParameters(List<UmlParameter> parameters) {
        this.parameters = new ArrayList<>(parameters);
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    // Métodos para manejar parámetros
    public void addParameter(UmlParameter parameter) {
        parameters.add(parameter);
    }

    public void removeParameter(UmlParameter parameter) {
        parameters.remove(parameter);
    }

    @Override
    public String toString() {
        String params = parameters.stream()
                                  .map(UmlParameter::toString)
                                  .collect(Collectors.joining(", "));

        // Comprobar si visibility es null antes de llamar a toString()
        String visibilityString = (visibility == null) ? "default" : visibility.toString().toLowerCase();

        return visibilityString + " " + returnType + " " + name + "(" + params + ")";
    }

}