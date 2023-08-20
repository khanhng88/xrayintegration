import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        RewriteXML();
    }

    static void RewriteXML() {
        try {
            // 1. Read xml file
            File file = new File("target/surefire-reports/testng-results.xml");

            if (!file.exists()) {
                throw new Error("Xml file doesn't exist");
            }

            // 2. Parse xml
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);

            // 3. Update xml content
            NodeList nodeList = document.getElementsByTagName("test-method");
            List<Element> elementList = new ArrayList<>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element node = (Element) nodeList.item(i);
                NodeList attributes = node.getElementsByTagName("attribute");

                for (int j = 0; j < attributes.getLength(); j++) {
                    String attributeName = ((Element) attributes.item(j)).getAttribute("name");
                    String attributeContent = attributes.item(j).getTextContent().trim();

                    if (!attributeName.equals("test") || attributeContent.split(",").length < 1) {
                        continue;
                    }

                    String[] issueTypes = attributeContent.split(",");

                    for (String issueType : issueTypes) {
                        Element newNode = (Element) node.cloneNode(true);
                        newNode.getElementsByTagName("attribute").item(0).setTextContent(issueType);
                        elementList.add(newNode);
                    }
                }
            }

            // Replace node
            Node parent = nodeList.item(0).getParentNode();

            for (Node child; (child = parent.getFirstChild()) != null; parent.removeChild(child));

            for (Element element : elementList) {
                parent.appendChild(element);
            }

            // 4. Write xml
            DOMSource source = new DOMSource(document);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StreamResult result = new StreamResult("new-testng-results.xml");
            transformer.transform(source, result);
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (TransformerException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}