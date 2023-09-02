import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String testExecutionId = args.length > 0 ? args[0] : "";
        RewriteJSON(testExecutionId);
//        RewriteXML(testExecutionId);
    }

    static void RewriteXML(String testExecutionId) {
        try {
            // 1. Read xml file
            File file = new File("testng-results.xml");

            if (!file.exists()) {
                throw new Error("Xml file doesn't exist");
            }

            // 2. Parse xml
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);

            // 3. Update xml content
            Map<Element, ArrayList<Element>> classDict = new HashMap<>();
            NodeList nodeTestList = document.getElementsByTagName("test");

            for (int i = 0; i < nodeTestList.getLength(); i++) {
                NodeList nodeClassList = ((Element) nodeTestList.item(i)).getElementsByTagName("class");

                for (int j = 0; j < nodeClassList.getLength(); j++) {
                    Element nodeClass = (Element) nodeClassList.item(j);
                    NodeList nodeTestMethodList = nodeClass.getElementsByTagName("test-method");
                    ArrayList<Element> newTestMethodList = new ArrayList<>();

                    for (int k = 0; k < nodeTestMethodList.getLength(); k++) {
                        Element nodeTestMethod = (Element) nodeTestMethodList.item(k);
                        NodeList nodeAttributeList = nodeTestMethod.getElementsByTagName("attribute");

                        for (int l = 0; l < nodeAttributeList.getLength(); l++) {
                            String attributeName = ((Element) nodeAttributeList.item(l)).getAttribute("name");
                            String attributeContent = nodeAttributeList.item(l).getTextContent().trim();

                            if (!attributeName.equals("test") || attributeContent.split(",").length < 1) {
                                continue;
                            }

                            String[] issueTypes = attributeContent.split(",");

                            for (String issueType : issueTypes) {
                                Element newNode = (Element) nodeTestMethod.cloneNode(true);
                                newNode.getElementsByTagName("attribute").item(0).setTextContent(issueType);
                                newTestMethodList.add(newNode);
                            }
                        }
                    }

                    classDict.put(nodeClass, newTestMethodList);
                }
            }

            // Replace node
            for (Map.Entry<Element, ArrayList<Element>> classNode : classDict.entrySet()) {
                Element parentNode = classNode.getKey();
                ArrayList<Element> newChildNode = classNode.getValue();

                // Remove all child node of class node
                for (Node child; (child = parentNode.getFirstChild()) != null; parentNode.removeChild(child));

                // Append new child node
                for (Element childNode : newChildNode) {
                    parentNode.appendChild(childNode);
                }
            }

            // 4. Write xml
            DOMSource source = new DOMSource(document);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StreamResult result = new StreamResult("server.xml");
            transformer.transform(source, result);
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (TransformerException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    static void RewriteJSON(String testExecutionId) {
        try {
            File allureTestCases = new File("allure-report/data/test-cases");
            File[] allureTestCasesFiles = allureTestCases.listFiles();
            JSONObject objWrite = new JSONObject();
            JSONArray tests = new JSONArray();
            JSONObject info = null;

            for (final File file : allureTestCasesFiles) {
                if (!file.isFile()) {
                    continue;
                }

                // 1. Read JSON file
                FileReader reader = new FileReader(file.getAbsoluteFile());

                // 2. Parse JSON
                JSONParser jsonParser = new JSONParser();
                JSONObject objReader = (JSONObject) jsonParser.parse(reader);

                // 3. Get tms links
                // 3.1 Add tests obj
                tests.addAll(buildTestCasesSection(objReader));

                // 3.2 Add info obj
                if (info == null) {
                    info = buildInfoSection(objReader);
                }
            }

            objWrite.put("tests", tests);
            objWrite.put("info", info);
            objWrite.put("testExecutionKey", testExecutionId);

            // 4. Write JSON
            FileWriter writeJsonFile = new FileWriter("new-xray-report.json");
            writeJsonFile.write(objWrite.toJSONString());

            System.out.println("Successfully write JSON object to file...");
            writeJsonFile.flush();;
            writeJsonFile.close();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    static String convertTimestampToString (String strTimestamp) {
        long timestamp = Long.parseLong(strTimestamp);

        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime zdt = instant.atZone(ZoneOffset.of("+07:00"));
        return zdt.truncatedTo(ChronoUnit.SECONDS).toOffsetDateTime().toString();
    }

    static JSONArray buildSteps (JSONArray originSteps) {
        JSONArray steps = new JSONArray();

        for (Object o : originSteps) {
            JSONObject originStep = (JSONObject) o;
            String status = originStep.get("status").toString().toUpperCase();
            String name = originStep.get("name").toString();

            JSONObject step = new JSONObject();
            step.put("status", status);
            step.put("action", name);

            steps.add(step);
        }

        return steps;
    }

    static JSONArray buildTestCasesSection (JSONObject originObj) {
        JSONArray testCases = new JSONArray();
        JSONArray links = (JSONArray) originObj.get("links");

        for (int i = 0; i < links.size(); i++) {
            JSONObject link = (JSONObject) links.get(i);

            if (!link.get("type").equals("tms")) {
                continue;
            }

            // Get origin steps
            JSONObject runTime = (JSONObject) originObj.get("time");
            JSONObject testStage = (JSONObject) originObj.get("testStage");
            JSONArray originSteps = (JSONArray) testStage.get("steps");
            String status = testStage.get("status").toString().toUpperCase();

            // Create step obj
            JSONObject testCase = new JSONObject();
            testCase.put("start", convertTimestampToString(runTime.get("start").toString()));
            testCase.put("finish", convertTimestampToString(runTime.get("stop").toString()));
            testCase.put("testKey", link.get("name"));
//            testCase.put("comment", originObj.get("description").toString());
            testCase.put("status", status);
            testCase.put("steps", buildSteps(originSteps));

            // Append to test cases
            testCases.add(testCase);
        }

        return testCases;
    }

    static JSONObject buildInfoSection (JSONObject originObj) {
        JSONObject runTime = (JSONObject) originObj.get("time");
        JSONObject info = new JSONObject();
        info.put("summary", originObj.get("name"));
        info.put("startDate", convertTimestampToString(runTime.get("start").toString()));
        info.put("finishDate", convertTimestampToString(runTime.get("stop").toString()));

        return info;
    }
}