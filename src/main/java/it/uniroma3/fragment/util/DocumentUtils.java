package it.uniroma3.fragment.util;

import static org.w3c.dom.Node.ATTRIBUTE_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class DocumentUtils {

    static final public String ANCHOR_ELEMENT  = "A";
    static final public String ID_ATTRIBUTE    = "id";
    static final public String CLASS_ATTRIBUTE = "class";
    static final public String HREF_ATTRIBUTE  = "href";
     
    static public boolean isNodeWithIdAttribute(Node node) {
    	return isElementWithAttribute(node, ID_ATTRIBUTE) ;
    }

    static public boolean isNodeWithClassAttribute(Node node) {
    	return isElementWithAttribute(node, CLASS_ATTRIBUTE) ;
    }
    
    static public boolean isElementWithAttribute(Node node, String attributeName) {
        return isElement(node) && ((Element)node).hasAttribute(attributeName);
    }

    static public boolean isAnchorNode(Node node) {
        return isElementWithAttribute(node, HREF_ATTRIBUTE) &&
               node.getNodeName().equalsIgnoreCase(ANCHOR_ELEMENT) ;
    }

    static public boolean isHrefAttribute(Node node) {
        return isAttribute(node) && node.getNodeName().equalsIgnoreCase(HREF_ATTRIBUTE) ;
    }

    static public boolean isText(Node node) {
    	return node!=null && ( node.getNodeType()==TEXT_NODE );
    }

    static public boolean isElement(Node node) {
        return node!=null && ( node.getNodeType()==ELEMENT_NODE );
    }

    static public boolean isAttribute(Node node) {
        return node!=null && ( node.getNodeType()==ATTRIBUTE_NODE );
    }
    
    static public String getIdValue(Node node) {
        final Element element = (Element)node;
        return element.getAttribute(ID_ATTRIBUTE);
    }
    
    static public String getClassValue(Node node) {
        final Element element = (Element)node;
        return element.getAttribute(CLASS_ATTRIBUTE);
    }
    
    static public String getHrefValue(Node node) {
        final Element element = (Element)node;
        return element.getAttribute(HREF_ATTRIBUTE);
    }    

    static public boolean isTextOfLength(Node node, int minLen, int maxLen) {
    	return isText(node) && checkLength(node.getNodeValue(), minLen, maxLen);
    }

    static public boolean checkLength(String string, int min, int max) {
    	final int length = string.trim().length();
    	return ( min<=length && length<max );
    }
    
}
