package app;


public class UmlAttribute {
    private String name;
    private String type;
    private Visibility visibility;

    public UmlAttribute(String name, String type, Visibility visibility) {
        this.name = name;
        this.type = type;
        this.visibility = visibility;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    // Método toString para representación en formato de texto
    @Override
    public String toString() {
        return visibility.toString().toLowerCase() + " " + type + " " + name;
    }
}
