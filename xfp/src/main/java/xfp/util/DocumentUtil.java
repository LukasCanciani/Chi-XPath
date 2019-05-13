package xfp.util;

import static org.apache.commons.lang.StringEscapeUtils.escapeJava;
import static org.w3c.dom.Node.*;

import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.uniroma3.dom.visitor.DOMVisitor;
import it.uniroma3.dom.visitor.DOMVisitor.SkippingStrategy;
import it.uniroma3.hlog.HypertextualLogger;
import it.uniroma3.token.dom.node.DOMNodeFactory;
import it.uniroma3.token.loader.DOMLoader;
import it.uniroma3.token.loader.Skipper;

public class DocumentUtil {

    static final private HypertextualLogger log = HypertextualLogger.getLogger();

    static final public String ANCHOR_ELEMENT  = "A";
    static final public String ID_ATTRIBUTE    = "id";
    static final public String CLASS_ATTRIBUTE = "class";
    static final public String HREF_ATTRIBUTE  = "href";

    static public Document loadDocument(String filename) {
        DOMNodeFactory document = null;
        try {
            final InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            final DOMLoader loader = new DOMLoader();           
            document = loader.loadDOM(reader);
        } catch (Exception e)   {
            throw new RuntimeException(e);
        }
        return document;
    }
    
    static public boolean isNodeWithIdAttribute(Node node) {
    	return DocumentUtil.isElement(node) && ((Element)node).hasAttribute(ID_ATTRIBUTE) ;
    }

    static public boolean isNodeWithClassAttribute(Node node) {
    	return DocumentUtil.isElement(node) && ((Element)node).hasAttribute(CLASS_ATTRIBUTE) ;
    }

    static public boolean isAnchorNode(Node node) {
        return DocumentUtil.isElement(node) && ((Element)node).hasAttribute(HREF_ATTRIBUTE) &&
               node.getNodeName().equals(ANCHOR_ELEMENT) ;
    }

    static public boolean isHrefNode(Node node) {
        return DocumentUtil.isAttribute(node) && node.getNodeName().equals(HREF_ATTRIBUTE) ;
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

    static public void removeEmptyWhitespaces(Document document) {
        /* skip all whitespace PCDATA */
        final SkippingStrategy strategy = new SkippingStrategy() {
            @Override
            public boolean isTextToSkip(Text text) {
                final String value = text.getNodeValue();
                final boolean result = 
                           !isText(text.getPreviousSibling()) 
                        && !isText(text.getNextSibling())
                        && isAllWhitespaces(value);
                if (result)
                    log.trace(() -> "Skipping PCDATA: "+escapeJava(value));
                return result;
            }
            @Override
            public boolean isTagToSkip(Element element)     { return false; }
            @Override
            public boolean isSubtreeToSkip(Element subtree) { return false; }
        };
        final Skipper listener = new Skipper();
        final DOMVisitor visitor = new DOMVisitor(listener,strategy);
        listener.setDOMVisitor(visitor);
        visitor.visit(document);
    }
    
    static private boolean isAllWhitespaces(String s) {
        for (int i=0; i<s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    static public boolean isFirstOfAseriesOfContiguousTextSiblings(Node node) {
        if (!isText(node) || !isText(node.getNextSibling())) return false;
        
        final Node prev = node.getPreviousSibling();
        return ( prev==null || prev==node || !isText(prev) ); /* hack for DOM tree library bug! prev==node */
    }

    static public boolean isTextOfLength(Node node, int minLen, int maxLen) {
    	return isText(node) && DocumentUtil.checkLength(node.getNodeValue(), minLen, maxLen);
    }

    static public boolean checkLength(String string, int min, int max) {
    	final int length = string.trim().length();
    	return ( min<=length && length<max );
    }
    
}
