package hu.kornel.server.application.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import hu.kornel.server.application.dto.validation.XPathValidationResultDto;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XPathValidationService {
    
    private final XPathFactory xPathFactory;
    private final DocumentBuilderFactory documentBuilderFactory;

    public XPathValidationService() {
        this.xPathFactory = XPathFactory.newInstance();
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        configureDocumentBuilderSecurity();
    }


    private void configureDocumentBuilderSecurity() {
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            log.debug("Document builder security features configured successfully");
        } catch (Exception e) {
            log.warn("Could not set all security features on DocumentBuilderFactory: {}", e.getMessage());
        }
    }

    public XPathValidationResultDto validateExpressionSyntax(String expression) {
        log.debug("Validating XPath expression syntax: {}", expression);
        
        if (expression == null) {
            log.debug("Expression is null");
            return XPathValidationResultDto.invalid("Az XPath kifejezés nem lehet üres");
        }

        String trimmedExpression = expression.trim();
        if (trimmedExpression.isEmpty()) {
            log.debug("Expression is empty after trimming");
            return XPathValidationResultDto.invalid("Az XPath kifejezés nem lehet üres");
        }

        if (trimmedExpression.equals("/")) {
            log.debug("Expression is just root selector");
            return XPathValidationResultDto.invalid("A '/' önmagában érvénytelen kifejezés. Próbálj meg egy konkrét elemet kiválasztani.");
        }

        try {
            XPath xpath = xPathFactory.newXPath();
            XPathExpression compiledExpression = xpath.compile(trimmedExpression);
            log.debug("XPath expression compiled successfully: '{}'", trimmedExpression);
            return XPathValidationResultDto.valid(compiledExpression);
        } catch (XPathExpressionException e) {
            log.debug("XPath syntax error: '{}' - {}", trimmedExpression, e.getMessage());
            String userFriendlyMessage = getUserFriendlyErrorMessage(e.getMessage(), trimmedExpression);
            return XPathValidationResultDto.invalid(userFriendlyMessage);
        } catch (Exception e) {
            log.error("Unexpected error validating XPath expression: '{}'", trimmedExpression, e);
            return XPathValidationResultDto.invalid("Hiba történt az XPath kifejezés feldolgozása során: " + e.getMessage());
        }
    }

    private String getUserFriendlyErrorMessage(String technicalMessage, String expression) {
        String lowerMessage = technicalMessage.toLowerCase();
        
        if (lowerMessage.contains("unexpected token") || lowerMessage.contains("expected")) {
            return "Szintaktikai hiba az XPath kifejezésben. Ellenőrizd a zárójeleket és az operátorokat.";
        }
        
        if (lowerMessage.contains("unclosed") || lowerMessage.contains("bracket")) {
            return "Hiányzó vagy rossz helyen lévő zárójel az XPath kifejezésben.";
        }
        
        if (lowerMessage.contains("quote") || lowerMessage.contains("string")) {
            return "Hiányzó vagy rossz helyen lévő idézőjel az XPath kifejezésben.";
        }
        
        if (expression.contains("..") && lowerMessage.contains("axis")) {
            return "A '..' (szülő tengely) használata hibás. Próbáld meg: 'parent::*' helyette.";
        }
        
        return "Érvénytelen XPath szintaxis. Ellenőrizd a kifejezést.";
    }

    public List<String> evaluateXPath(XPathExpression expression, String documentContent) {
        log.debug("Evaluating XPath expression against document");
        
        if (documentContent == null || documentContent.trim().isEmpty()) {
            log.error("Document content is null or empty");
            throw new IllegalArgumentException("A dokumentum tartalma nem lehet üres");
        }
        
        try {
            Document document = parseDocument(documentContent);
            NodeList nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            
            List<String> nodePaths = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String nodePath = getAbsoluteNodePath(node);
                nodePaths.add(nodePath);
                log.debug("Matched node {}: {}", i + 1, nodePath);
            }
            
            log.debug("XPath evaluation complete: {} node(s) matched", nodePaths.size());
            return nodePaths;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error evaluating XPath expression: {}", e.getMessage(), e);
            throw new RuntimeException("Hiba történt az XPath kifejezés kiértékelése során: " + e.getMessage(), e);
        }
    }

    private Document parseDocument(String documentContent) throws Exception {
        log.debug("Parsing document content ({} characters)", documentContent.length());
        
        try {
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                @Override
                public void warning(org.xml.sax.SAXParseException e) {
                    log.debug("XML parsing warning (ignored): {}", e.getMessage());
                }
                
                @Override
                public void error(org.xml.sax.SAXParseException e) {
                    log.debug("XML parsing error (ignored): {}", e.getMessage());
                }
                
                @Override
                public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
                    throw e;
                }
            });
            
            InputSource inputSource = new InputSource(new StringReader(documentContent));
            Document document = builder.parse(inputSource);
            document.getDocumentElement().normalize();
            log.debug("Document parsed successfully");
            return document;
        } catch (Exception e) {
            log.error("Failed to parse document: {}", e.getMessage());
            throw new Exception("A dokumentum feldolgozása sikertelen. Ellenőrizd a HTML/XML szintaxist.", e);
        }
    }

    private String getAbsoluteNodePath(Node node) {
        if (node == null || node.getNodeType() == Node.DOCUMENT_NODE) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        Node current = node;

        while (current != null && current.getNodeType() != Node.DOCUMENT_NODE) {
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = current.getNodeName();
                int position = getNodePosition(current);
                path.insert(0, "/" + nodeName + "[" + position + "]");
            }
            current = current.getParentNode();
        }

        String result = path.toString();
        log.trace("Generated node path: {}", result);
        return result;
    }

    private int getNodePosition(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return 1;
        }
        
        int position = 1;
        Node sibling = node.getPreviousSibling();
        
        while (sibling != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE && 
                sibling.getNodeName().equals(node.getNodeName())) {
                position++;
            }
            sibling = sibling.getPreviousSibling();
        }
        
        return position;
    }

    public boolean validateXPathMatch(List<String> userNodes, List<String> expectedNodes) {
        log.debug("Comparing user nodes ({}) with expected nodes ({})", userNodes.size(), expectedNodes.size());
        
        if (userNodes == null || expectedNodes == null) {
            log.warn("Null node list provided for comparison");
            return false;
        }
        
        if (userNodes.isEmpty() && expectedNodes.isEmpty()) {
            log.debug("Both node lists are empty - this is a match");
            return true;
        }
        
        if (userNodes.size() != expectedNodes.size()) {
            log.debug("Node count mismatch: user={}, expected={}", userNodes.size(), expectedNodes.size());
            return false;
        }

        boolean matches = userNodes.equals(expectedNodes);
        
        if (matches) {
            log.debug("XPath validation: MATCH - user selected exactly the expected nodes");
        } else {
            log.debug("XPath validation: NO MATCH");
            logNodeDifferences(userNodes, expectedNodes);
        }
        
        return matches;
    }

    private void logNodeDifferences(List<String> userNodes, List<String> expectedNodes) {
        log.debug("=== Node Comparison Details ===");
        
        List<String> missing = new ArrayList<>(expectedNodes);
        missing.removeAll(userNodes);
        if (!missing.isEmpty()) {
            log.debug("Missing nodes (expected but not selected): {}", missing);
        }
        
        List<String> extra = new ArrayList<>(userNodes);
        extra.removeAll(expectedNodes);
        if (!extra.isEmpty()) {
            log.debug("Extra nodes (selected but not expected): {}", extra);
        }
        
        if (missing.isEmpty() && extra.isEmpty()) {
            log.debug("Same nodes selected but in different order");
            log.debug("User order: {}", userNodes);
            log.debug("Expected order: {}", expectedNodes);
        }
    }

    public boolean isValidDocument(String documentContent) {
        if (documentContent == null || documentContent.trim().isEmpty()) {
            return false;
        }
        
        try {
            parseDocument(documentContent);
            return true;
        } catch (Exception e) {
            log.error("Invalid sample document: {}", e.getMessage());
            return false;
        }
    }
}