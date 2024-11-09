package app;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class UmlDiagramSerializer {

    public Document serializeToXML(List<UmlClass> classes, List<UmlRelation> relations) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Crear el elemento raíz
        Element rootElement = doc.createElement("UMLDiagram");
        doc.appendChild(rootElement);

        // Serializar las clases UML
        for (UmlClass umlClass : classes) {
            Element classElement = doc.createElement("UmlClass");
            classElement.setAttribute("name", umlClass.getName());
            rootElement.appendChild(classElement);

            // Serializar atributos
            for (UmlAttribute attribute : umlClass.getAttributes()) {
                Element attributeElement = doc.createElement("Attribute");
                attributeElement.setAttribute("name", attribute.getName());
                attributeElement.setAttribute("type", attribute.getType());
                attributeElement.setAttribute("visibility", attribute.getVisibility().toString());
                classElement.appendChild(attributeElement);
            }

            // Serializar métodos
            for (UmlMethod method : umlClass.getMethods()) {
                Element methodElement = doc.createElement("Method");
                methodElement.setAttribute("name", method.getName());
                methodElement.setAttribute("returnType", method.getReturnType());
                methodElement.setAttribute("visibility", method.getVisibility().toString());
                classElement.appendChild(methodElement);

                // Serializar parámetros
                for (UmlParameter param : method.getParameters()) {
                    Element paramElement = doc.createElement("Parameter");
                    paramElement.setAttribute("name", param.getName());
                    paramElement.setAttribute("type", param.getType());
                    methodElement.appendChild(paramElement);
                }
            }
        }

        // Serializar relaciones
        for (UmlRelation relation : relations) {
            Element relationElement = doc.createElement("UmlRelation");
            relationElement.setAttribute("fromClass", relation.getFromClass().getName());
            relationElement.setAttribute("toClass", relation.getToClass().getName());
            relationElement.setAttribute("type", relation.getType().toString());
            relationElement.setAttribute("cardinality", relation.getCardinality());
            rootElement.appendChild(relationElement);
        }

        return doc;
    }
}
