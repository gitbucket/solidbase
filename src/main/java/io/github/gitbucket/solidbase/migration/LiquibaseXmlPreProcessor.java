package io.github.gitbucket.solidbase.migration;

import liquibase.database.Database;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LiquibaseXmlPreProcessor implements LiquibasePreProcessor {

    @Override
    public String preProcess(String moduleId, String version, String source) throws Exception {
        // add required attributes: id and author
        Document doc = parseXml(source);
        Element root = doc.getDocumentElement();
        if(!root.hasAttribute("id")) {
            root.setAttribute("id", version);
        }
        if(!root.hasAttribute("author")) {
            root.setAttribute("author", moduleId);
        }

        // Move constraint attributes defined in columns to child constraints element
        NodeList columns = root.getElementsByTagName("column");
        for(int i = 0; i < columns.getLength(); i++){
            Element column = (Element) columns.item(i);
            Map<String, String> constraintsMap = new HashMap<>();
            for(String constraintAttributeName: CONSTRAINT_PROPERTIES){
                if(column.hasAttribute(constraintAttributeName)){
                    constraintsMap.put(constraintAttributeName, column.getAttribute(constraintAttributeName));
                    column.removeAttribute(constraintAttributeName);
                }
            }
            if(!constraintsMap.isEmpty()){
                NodeList nodes = column.getElementsByTagName("constraints");
                Element constraints = null;
                if(nodes.getLength() == 0){
                    constraints = doc.createElement("constraints");
                    column.appendChild(constraints);
                } else {
                    constraints = (Element) nodes.item(0);
                }
                for(Map.Entry<String, String> entry: constraintsMap.entrySet()){
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if(!constraints.hasAttribute(key)){
                        constraints.setAttribute(key, value);
                    }
                }
            }
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<databaseChangeLog\n" +
                "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\"\n" +
                "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.0.xsd\n" +
                "        http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\">\n" +
                "\n" +
                printXml(doc) + "\n" +
                "</databaseChangeLog>\n";
    }

    private static Document parseXml(String xml) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        return doc;
    }

    private static String printXml(Document doc) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(out));

        return new String(out.toByteArray(), "UTF-8");
    }
}
