package it.uniroma3.fragment.util;


import static it.uniroma3.fragment.util.DocumentUtils.isElement;
import static it.uniroma3.fragment.util.DocumentUtils.isText;

import java.util.Arrays;
import java.util.Comparator;

import org.w3c.dom.Node;
import org.w3c.dom.Text;
/**
 * The template analysis algorithm aims at classifying documents nodes as 
 * suitable pivot and/or suitable extracted values.
 * <br/>
 * 
 * Since the pages are considered one at a time, a differential analysis between
 * pages sharing a common template cannot be easily adopted.
 * Simple rough heuristics are used.
 */
public class TemplateUtils {

    /**
     * @param node
     * @return true iff node is text containing at least a variant to extract
     */
    static public boolean isPCDATAwithVariantContent(Node node) {
        /** look at the left && right hand side */
        return ( TemplateUtils.isVariantNode(node) );
    }

    static public String extractLongestInvariant(Text pcdata) {        
        // return longest word
        return Arrays.stream(pcdata.getNodeValue().split("\\W+")).max( Comparator.comparing( s -> s.length() ) ).orElse("");
    }

    static public boolean isVariantNode(Node occurrence) {
        return isText(occurrence);
    }

    static public boolean isTemplateNode(Node occurrence) {
    	return isElement(occurrence);
    }
	
}
