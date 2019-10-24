package xfp.generation;

import static xfp.util.DocumentUtil.*;
import static xfp.util.XPathUtil.containsPredicate;

import java.util.Objects;
import java.util.StringJoiner;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class XPathBuilder {

    public String makeXPath(final Path pivot2target) {
        Objects.requireNonNull(pivot2target, "No tree-path provided");
        final Node pivot = pivot2target.getStart();
        final Node target = pivot2target.getEnd();

        /* consistency checks */
        if (target==null || pivot==null) 
            return null;

        if (!isText(target) && !isAnchorNode(target)) {
            throw new IllegalArgumentException("Unknown target node type: "+target);		
        }

        synchronized (pivot.getOwnerDocument()) {
            /* the starting node must be a template node such that: 
             * it is either a node with an id attribute, 
             * or an element that is the parent of a 
             * PCDATA containing an invariant text  */
            return ( getPivotXPath(pivot) + '/' + pivot2targetXPath(pivot2target) + '/' + getTargetXPath(target));
        }
    }

    /**
     * @param pivot occurrence
     * @return an XPath expression to locate only the pivot
     */
    private String getPivotXPath(Node pivot) {
        if ( isText(pivot) ) { 
            // a text with an invariant label as content
            final Text text = (Text)pivot;
            return getPivotXPath(text);            
        } else {      
            // an element (with an id attribute)
            final Element element = (Element)pivot;            
            return getPivotXPath(element);
        }
    }

    protected String getPivotXPath(final Text text) {
        return "//text()" + containsPredicate(text);
    }

    private String getPivotXPath(final Element pivot) {
        // N.B. an element with an id attribute always plays as pivot
        final String elementName = pivot.getNodeName().toUpperCase(); // e.g., DIV
        if (isNodeWithIdAttribute(pivot))   {
            return "//" + elementName + "[@id='" + getIdValue(pivot) + "']";
        } else if (isNodeWithClassAttribute(pivot)) { /* !? */
            return "//" + elementName + "[@class='" + getClassValue(pivot) + "']";
        } else if (isElement(pivot)){ //CHECK what about other template attributes ???
            return "//" + elementName;          
        }       
        throw new IllegalStateException("Unknown XPath translation of "+pivot+" as a pivot");        
    }

    /*
        private Node getParentNode(Text pivot) {
            Node parentNode = pivot.getParentNode();

            if (parentNode==null) {
                Node original = getMergedNode(pivot);
                if (original==null)
                    throw new RuntimeException("This node is not the result of normalization");
                parentNode = original.getParentNode();
                Objects.requireNonNull(parentNode, "Cannot recover the parent of this node: "+pivot);
            }
            return parentNode;
        }
     */

    private String pivot2targetXPath(Path pivot2value) {
        if (pivot2value.isEmpty()) return "";
        final StringJoiner joiner = new StringJoiner("/");
        pivot2value.getSteps().forEach( s -> 
            joiner.add(s.getXPathStepExpression())
        );

        return joiner.toString();
    }

    private String getTargetXPath(/*Path pivot2target, */Node target) {
        if (isText(target)) 
            return "self::text()" ;  // only texts, please...
        else if (isAnchorNode(target)) 
            return "self::*[@href]"; // only anchor nodes, please...
        throw new IllegalStateException("Unknown XPath translation of "+target+" as a target");        
    }

}