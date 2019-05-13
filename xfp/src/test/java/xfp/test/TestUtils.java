package xfp.test;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.uniroma3.util.MarkUpUtils;

import static it.uniroma3.util.MarkUpUtils.parseHTML;
import static javax.xml.xpath.XPathConstants.NODESET;
import static org.junit.Assert.*;
/**
 * Set of utilities methods to make assertions with collections less verbose.
 * 
 */
public class TestUtils {
	static final private XPathFactory XPATHFACTORY = XPathFactory.newInstance();

    static public String _HTML_TEMPLATE_(Object body) {
        return "<HTML><BODY>"+body+"</BODY></HTML>";
    }

    static public Document makeDocument(String content) {
        try {
            return parseHTML(new StringReader(_HTML_TEMPLATE_(content)));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    static public Node getUniqueByXPath(Document document, String xpath) {
        final NodeList nodelist = getByXPath(document, xpath);
        assertNotNull(nodelist);
        assertEquals("not a singleton node-set returned by "+xpath, 1, nodelist.getLength());
        final Node result = nodelist.item(0);
        assertNotNull("null node returned by "+xpath, result);
        return result;
    }

    static public Node getFirstByXPath(Document document, String xpath) {
        final NodeList nodelist = getByXPath(document, xpath);
        assertNotNull(nodelist);
        assertTrue("empty node-set returned by "+xpath, nodelist.getLength()>0);
        final Node result = nodelist.item(0);
        assertNotNull("null node returned by "+xpath, result);
        return result;
    }

    static public NodeList getByXPath(Node context, String xpathExp) {
    	/* N.B.: this requires the input document to have been already normalized,
         *       i.e., contiguous text siblings should have been already merged
         */
        NodeList result = null; /* just one node */
        try {
            final XPathExpression xpath = compileXPath(xpathExp);
            result = (NodeList) xpath.evaluate(context, NODESET);
            if (result==null)
                throw new RuntimeException("Erroneous XPath step expression \'"+xpathExp+"\' is not locating any node");
        }
        catch (RuntimeException re) {
            System.err.println("XPath:   "+xpathExp);
            System.err.println("Context: "+MarkUpUtils.dumpTree(context));
            throw new IllegalStateException(re);
        } catch (XPathExpressionException e) {
            System.err.println("XPath:   "+xpathExp);
            throw new RuntimeException(e);
        }
        return result;
    }
    
    private static XPathExpression compileXPath(String xpath) throws XPathExpressionException {
        XPathExpression expr = XPATHFACTORY.newXPath().compile(xpath);
        return expr;
    }

    /**
     * An utility method to extract a singleton element from a collection.
     * It checks that a collection is not null, is a singleton-collection and its only element
     * is not null
     * @param <T> the formal type of the hosted elements
     * @param collection the collection to check
     * @return the only non-null element of the collection
     */
    static public <T> T unique(String msg, Collection<T> collection) {
        assertNotNull(msg+"\nCollection is null.", collection);
        assertEquals(msg+"\nCollection "+collection+" isn't a singleton-collection as expected. Size ", 1, collection.size());
        final T result = collection.iterator().next();
        assertNotNull(msg+"\nSingleton element is null",result);
        return result;
    }
    //CHECK sJUnit MethodRule che consenta il reporting di un msg in caso di errore
    static public <T> T unique(Collection<T> collection) {
        return unique("",collection);
    }

    /**
     * An utility method to convert an array of elements with the same type
     * to a {@link java.util.Set}
     * 
     * @param <T> formal type of hosted elements
     * @param elements the elements to insert in the set
     * @return a set with the given elements
     */
    @SafeVarargs
    static public <T> Set<T> set(T... elements) {
        assertNotNull(elements);
        return new HashSet<T>(Arrays.asList(elements));
    }

}
