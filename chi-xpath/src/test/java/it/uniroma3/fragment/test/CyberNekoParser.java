package it.uniroma3.fragment.test;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.filters.Purifier;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A factory to create {@link Document} over HTML pages.
 * <BR/>
 * It is a facade over 
 * <A href="http://nekohtml.sourceforge.net/index.html"> NekoHTML parser </A>
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class CyberNekoParser {

    static public Document parseHTML(Reader in) throws SAXException, IOException {
        return parseHTML(new InputSource(in));
    }

    static public Document parseHTML(InputSource in) throws SAXException, IOException {
        DOMParser parser = createTagBalancingParser();
        parser.parse(in);
        return parser.getDocument();
    }

    static public DOMParser createTagBalancingParser() throws SAXException {
        final DOMParser parser = createParser();
        parser.setFeature("http://cyberneko.org/html/features/balance-tags", true);
        XMLDocumentFilter[] filters = { new Purifier() };
        parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
        return parser;
    }

    static public DOMParser createParser() throws SAXException {        
        /* N.B. Check http://nekohtml.sourceforge.net/faq.html#uppercase
            Starting from newer version of nekohtml, HTML element names 
            should always be uppercase and attribute names should be lower case:
            Section 1.2.1 of the HTML 4.01 specification states:
             Element names are written in uppercase letters (e.g., BODY). 
           Attribute names are written in lowercase letters (e.g., lang, onsubmit).
         */
        final DOMParser parser = new DOMParser();
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper");
        parser.setProperty("http://cyberneko.org/html/properties/names/attrs", "no-change");
        parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset", false);
        parser.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", false);
        parser.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", false);
        parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", false);
        parser.setFeature("http://xml.org/sax/features/namespaces",false);
        parser.setEntityResolver(getDumbEntityResolver());
        return parser;
    }

    static private EntityResolver getDumbEntityResolver() {
        return new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) {
                if (systemId.endsWith(".dtd")) {
                    // this deactivates all DTDs by giving empty XML docs
                    final String encoding = System.getProperty("file.encoding");
                    final String emptyDoc = "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>";
                    return new InputSource(new ByteArrayInputStream(emptyDoc.getBytes()));
                }
                return null;
            }
        };
    }


}