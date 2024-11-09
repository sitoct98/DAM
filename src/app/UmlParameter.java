package app;

public class UmlParameter {
    private String name;
    private String type;

    public UmlParameter(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Método toString para representación en formato de texto
    @Override
    public String toString() {
        return type + " " + name;
    }
}