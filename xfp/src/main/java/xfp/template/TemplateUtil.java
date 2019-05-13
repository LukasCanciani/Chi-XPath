package xfp.template;


import static xfp.template.TemplateMarker.*;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.uniroma3.token.dom.DOMToken;

/**
 * The template analysis algorithm classifies documents nodes as 
 * suitable pivot and/or suitable extracted values.
 * <br/>
 * It classifies each node occurrence as either a template (invariant) node
 * or non-template (variant) node, after an ExAlg-like ECGM analysis 
 * (see <a href=http://ilpubs.stanford.edu:8090/548/1/2002-40.pdf"> 
 *    Extracting Structured Data from Web Pages</a>
 * )
 * <br/>
 * It marks the template token occurrences directly on the DOMs.
 * <br/>
 *  It is not coupled with the details of the ECGM analysis and
 * LFEQ representation, but it knows both classes {@link DOMToken} and [DOM]Node.
 */
public class TemplateUtil {

    /**
     * @param node
     * @return true iff node is text containing at least a variant to extract
     */
    static public boolean isPCDATAwithVariantContent(Node node) {
        /** look at the left && right hand side */
        return ( TemplateUtil.isVariantNode(node) );
    }

    @SuppressWarnings("unchecked")
    static public String extractLongestInvariant(Text pcdata) {
        // choose longest invariant belonging to this PCDATA, if any
        // and only in case the template analysis has been performed
        String result = null;
        final Object invariants = pcdata.getUserData(INVARIANT_MARKER);
        
        if (invariants==null) // template analysis not performed ?
            return pcdata.getNodeValue(); //just use the whole PCDATA
        
        if (invariants instanceof String) 
            return (String)invariants;
        
        for(Object inv : (List<Object>)invariants) { //null pointer - invariants are null
            if (inv==null) continue;
    
            final String candidate = inv.toString();
            if (result==null || inv.toString().length()>result.length()) {
                result = candidate;
            }
        }
        return result;
    }

    // N.B. this works only for extensional nodes (occurrences in the DOM tree)
    static public boolean isVariantNode(Node occurrence) {
    	return ( occurrence.getUserData(VARIANT_MARKER)!=null ) ;
    }

    // N.B. this works only for extensional nodes (occurrences in the DOM tree)
    static public boolean isTemplateNode(Node occurrence) {
    	return ( occurrence.getUserData(INVARIANT_MARKER)!=null ) ;
    }
	
}
