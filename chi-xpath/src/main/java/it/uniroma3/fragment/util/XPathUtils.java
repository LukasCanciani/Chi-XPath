package it.uniroma3.fragment.util;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XPathUtils {

    /**
     * Evaluate an XPath expression on a Document.
     *
     * @param document an org.w3c.dom Document
     * @param xpathExpression the XPath expression to evaluate on the Document
     * @return a NodeList of the resulting fragments
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    static public NodeList evaluateXPath(Document document, String xpathExpression) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
    }

}
