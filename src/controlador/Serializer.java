package controlador;

import app.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Serializer {



    // Método para guardar el XML en un archivo
    public void saveXMLToFile(Document doc, String filePath) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }



    public Document serializeToXML(List<UmlClass> classes, List<UmlRelation> relations) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("UMLDiagram");
        doc.appendChild(rootElement);

        for (UmlClass umlClass : classes) {
            Element classElement = doc.createElement("UmlClass");
            classElement.setAttribute("name", umlClass.getName());
            classElement.setAttribute("posX", String.valueOf(umlClass.getPosX()));
            classElement.setAttribute("posY", String.valueOf(umlClass.getPosY()));
            rootElement.appendChild(classElement);

            for (UmlAttribute attribute : umlClass.getAttributes()) {
                Element attributeElement = doc.createElement("Attribute");
                attributeElement.setAttribute("name", attribute.getName());
                attributeElement.setAttribute("type", attribute.getType());
                attributeElement.setAttribute("visibility", attribute.getVisibility().toString());
                classElement.appendChild(attributeElement);
            }

            for (UmlMethod method : umlClass.getMethods()) {
                Element methodElement = doc.createElement("Method");
                methodElement.setAttribute("name", method.getName());
                methodElement.setAttribute("returnType", method.getReturnType());
                methodElement.setAttribute("visibility", method.getVisibility().toString());
                classElement.appendChild(methodElement);

                for (UmlParameter param : method.getParameters()) {
                    Element paramElement = doc.createElement("Parameter");
                    paramElement.setAttribute("name", param.getName());
                    paramElement.setAttribute("type", param.getType());
                    methodElement.appendChild(paramElement);
                }
            }
        }

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

    public List<UmlClass> deserializeFromXML(String filePath) throws Exception {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        List<UmlClass> classes = new ArrayList<>();
        NodeList classNodes = doc.getElementsByTagName("UmlClass");

        for (int i = 0; i < classNodes.getLength(); i++) {
            Node classNode = classNodes.item(i);
            if (classNode.getNodeType() == Node.ELEMENT_NODE) {
                Element classElement = (Element) classNode;
                String className = classElement.getAttribute("name");
                double posX = Double.parseDouble(classElement.getAttribute("posX"));
                double posY = Double.parseDouble(classElement.getAttribute("posY"));
                UmlClass umlClass = new UmlClass(className);
                umlClass.setPosX(posX);
                umlClass.setPosY(posY);

                NodeList attributeNodes = classElement.getElementsByTagName("Attribute");
                for (int j = 0; j < attributeNodes.getLength(); j++) {
                    Element attributeElement = (Element) attributeNodes.item(j);
                    String attributeName = attributeElement.getAttribute("name");
                    String attributeType = attributeElement.getAttribute("type");
                    Visibility visibility = Visibility.valueOf(attributeElement.getAttribute("visibility"));
                    UmlAttribute attribute = new UmlAttribute(attributeName, attributeType, visibility);
                    umlClass.addAttribute(attribute);
                }

                NodeList methodNodes = classElement.getElementsByTagName("Method");
                for (int j = 0; j < methodNodes.getLength(); j++) {
                    Element methodElement = (Element) methodNodes.item(j);
                    String methodName = methodElement.getAttribute("name");
                    String returnType = methodElement.getAttribute("returnType");
                    Visibility visibility = Visibility.valueOf(methodElement.getAttribute("visibility"));
                    UmlMethod method = new UmlMethod(methodName, returnType, visibility);

                    NodeList paramNodes = methodElement.getElementsByTagName("Parameter");
                    for (int k = 0; k < paramNodes.getLength(); k++) {
                        Element paramElement = (Element) paramNodes.item(k);
                        String paramName = paramElement.getAttribute("name");
                        String paramType = paramElement.getAttribute("type");
                        UmlParameter param = new UmlParameter(paramName, paramType);
                        method.addParameter(param);
                    }

                    umlClass.addMethod(method);
                }

                classes.add(umlClass);
            }
        }

        return classes;
    }


    public List<UmlRelation> deserializeRelationsFromXML(String filePath, List<UmlClass> classes) throws Exception {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        List<UmlRelation> relations = new ArrayList<>();
        NodeList relationNodes = doc.getElementsByTagName("UmlRelation");

        for (int i = 0; i < relationNodes.getLength(); i++) {
            Node relationNode = relationNodes.item(i);
            if (relationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element relationElement = (Element) relationNode;
                String fromClassName = relationElement.getAttribute("fromClass");
                String toClassName = relationElement.getAttribute("toClass");
                UmlRelationType type = UmlRelationType.valueOf(relationElement.getAttribute("type"));
                String cardinality = relationElement.getAttribute("cardinality");

                UmlClass fromClass = classes.stream().filter(c -> c.getName().equals(fromClassName)).findFirst().orElse(null);
                UmlClass toClass = classes.stream().filter(c -> c.getName().equals(toClassName)).findFirst().orElse(null);

                if (fromClass != null && toClass != null) {
                    UmlRelation relation = new UmlRelation(fromClass, toClass, type, cardinality);
                    relations.add(relation);
                }
            }
        }

        return relations;
    }
}