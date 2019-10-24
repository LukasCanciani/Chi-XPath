package it.uniroma3.fragment.step;

import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.uniroma3.fragment.XPathFragmentSpecification;

/**
 * This is by far the most complex {@link XPathStepFactory} as it has 
 * to move down in a tree structure targeting children by using several
 * available XPath steps: it generates (see {@link Down#from(Node)}
 * availables steps by element name, by position, by both, targeting 
 * both text and element children.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class Down extends XPathStepFactory {
    
    private boolean text; // also target text children; (element children are always a target)
    private boolean name; // target element children by name
    private boolean pos;  // target children by pos
    private boolean pan;  // target element children both by pos and name
    
    public Down() {
        this(true,false,false,true); // generates step expressions like text()[1] and DIV[3]
    }
    
    /**
     * @param textTarget   true iff generates XPath steps to target textual leaves, e.g., /text()[2]
     * @param elementName   "    "      "       "     "    " reach elements by their name e.g., /DIV
     * @param position      "    "      "       "     "    "   "       "    by their position e.g., /*[2]
     * @param posAndName    "    "      "       "     "    "   "       "    by their position and name, /DIV[3]
     */
    public Down(boolean textTarget, boolean elementName, boolean position, boolean posAndName) {
        this.text = textTarget;
        this.name = elementName;
        this.pos  = position;
        this.pan  = posAndName;
    }

    public boolean isTargetingTextsEnabled() {
        return this.text;
    }

    public boolean isTargetingByPositionEnabled() {
        return this.pos;
    }
    
    public boolean isTargetingByNameEnabled() {
        return this.name;
    }

    public boolean isTargetingByBothNameAndPositionEnabled() {
        return this.pan;
    }

    @Override
    public Set<String> makeStepExpressions(Node current) {
        if (isTargetingByNameEnabled() || isTargetingByBothNameAndPositionEnabled())
            Objects.requireNonNull(this.getXPathFragmentSpecification(), 
                    "before using "+this.getClass().getName()+" you must set its "+XPathFragmentSpecification.class+
                    "\nit needs it to choose the case of generated XPath element predicates, e.g., /DIV");
        final NodeList children = current.getChildNodes();
        if (children.getLength()==0) return Collections.emptySet();

        final Map<String,Node> result = new LinkedHashMap<>();

        /* N.B. DOM could be not normalized, i.e., a single PCDATA
            could be split into several contiguous sibling text nodes */
        boolean textIsSeparated = true; // next text node is well separated by previous one? 

        /* Reminder: XPath positions are 1-based */
        int positionAsElem = 1; // XPath position() for child::*[pos]-like expr.
        int positionAsText = 1; // XPath position() for   text()[pos]-like expr.
        //XPath position() for every element name to build  NAME[pos]-like expr.

        final Map<String, Integer> name2pos = new HashMap<>(); 

        for (int i=0; i<children.getLength(); i++) { 
            final Node child = children.item(i);
            if (child==null) continue;
            final short type = child.getNodeType();
            switch (type) {
            case TEXT_NODE:// ----- It's a text
                if (textIsSeparated) {
                    if (isTargetingTextsEnabled()) {
                        final String step = xpathStepDownToText(child,positionAsText);
                        result.put(step,child);
                    }
                    positionAsText++;
                }
                textIsSeparated = false;
                break;
            case ELEMENT_NODE:// -- It's an element 
                // we want to use the name for the step
                final String name = child.getNodeName().toUpperCase();
                int positionByName = name2pos.getOrDefault(name, 1);
                name2pos.put(name, positionByName);
                if (isTargetingByNameEnabled()) result.put(xpathStepDownToElementOfName(child), child);
                if (isTargetingByPositionEnabled())  result.put(xpathStepDownToElementOfPos(child, positionAsElem), child);
                if (isTargetingByBothNameAndPositionEnabled())  result.put(xpathStepDownToElementOfNameAndPos(child, positionByName),child);           
                positionByName++;
                positionAsElem++;

                // N.B. an element that separates texts: fall-through case
            default: // ----------- It's something else
                // e.g., an HTML comment <!-- -->;
                // whatever it is, it just separates otherwise contiguous text nodes
                textIsSeparated = true;
            }
        }
        return result.keySet();
    }

    protected String xpathStepDownToText(Node text, int pos) {
        return "text()"+ ( isTargetingByPositionEnabled() ? "["+pos+"]" : "" );
    }

    protected String xpathStepDownToElementOfNameAndPos(Node element, int pos) {            
        return xpathStepDownToElementOfName(element)+"["+pos+"]";
    }

    protected String xpathStepDownToElementOfName(Node element) {            
        /* to comply with DOM implementations using lower-case for element names */
        return elementCase(element.getNodeName());
    }

    protected String xpathStepDownToElementOfPos(Node element, int pos) {
        return "*["+pos+"]";
    }

}