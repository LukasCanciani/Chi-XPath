package it.uniroma3.fragment.test;

import static it.uniroma3.fragment.test.CyberNekoParser.parseHTML;

import static javax.xml.xpath.XPathConstants.NODESET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Objects;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.uniroma3.fragment.dom.DOMDumper;
import static it.uniroma3.fragment.test.FixtureUtils.makeTmpFileWithContent;

/**
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class DOMUtils {
    
    
    static final private XPathFactory XPATHFACTORY = XPathFactory.newInstance();

    static public String _HTML_(Object body) {
        return "<HTML><BODY>" + body + "</BODY></HTML>" ;
    }
    static public Document makeDocument(String bodyContent) {
        try {
            return parseHTML(new StringReader(_HTML_(bodyContent)));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    static public URI makeTmpPageWithContent(String htmlContent) throws IOException {
        final File local = makeTmpFileWithContent(_HTML_(htmlContent));
        return local.toURI();
    }
    
    static public Node getUniqueByXPath(Document document, String xpath) {
        Objects.requireNonNull(document);
        Objects.requireNonNull(xpath);
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
            System.err.println("Context: "+new DOMDumper().dump(context));
            throw new IllegalStateException(re);
        } catch (XPathExpressionException e) {
            System.err.println("XPath:   "+xpathExp);
            throw new RuntimeException(e);
        }
        return result;
    }
    
    static private XPathExpression compileXPath(String xpath) throws XPathExpressionException {
        Objects.requireNonNull(xpath, "Cannot compile null XPath expression");
        XPathExpression expr = XPATHFACTORY.newXPath().compile(xpath);
        return expr;
    }
    
}
