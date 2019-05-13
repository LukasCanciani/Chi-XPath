package it.uniroma3.fragment;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.uniroma3.fragment.step.CaseHandler;

import static it.uniroma3.fragment.util.DocumentUtils.*;
import static java.util.Collections.singleton;

import static it.uniroma3.fragment.util.XPathExpressionBuilderUtils.*;
import static it.uniroma3.fragment.step.CaseHandler.HTML_STANDARD_CASEHANDLER;
/**
 * Builds well-formed and executable XPath expressions starting from {@link Path} objects.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class XPathExpressionBuilder {

    private CaseHandler caseHandler;

    /* attribute names whose values will be in a [@id='text-123'] -like predicate */
    private Set<String> equalsPredicateAttributeNames;

    /* attribute names whose values will be in several 
     * [contains(@class,'123')] [contains(@class,'text')] - like predicates */
    private Set<String> containsPredicateAttributeNames;

    public XPathExpressionBuilder() {
        this(HTML_STANDARD_CASEHANDLER);
    }

    public XPathExpressionBuilder(CaseHandler caseHandler) {
        this.caseHandler = caseHandler;
        this.equalsPredicateAttributeNames = new HashSet<>();
        this.addAttributeNameForEqualsPredicate(ID_ATTRIBUTE);
        this.containsPredicateAttributeNames = new HashSet<>();
        this.addAttributeNameForContainsPredicate(CLASS_ATTRIBUTE);
    }

    public void addAttributeNameForEqualsPredicate(String attributeName) {
        this.equalsPredicateAttributeNames.add(this.caseHandler.attributeCase(attributeName));
    }

    public void addAttributeNameForContainsPredicate(String attributeName) {
        this.containsPredicateAttributeNames.add(this.caseHandler.attributeCase(attributeName));
    }

    public Set<String> makeXPaths(final Path pivot2target) {
        Objects.requireNonNull(pivot2target, "No tree-path provided");
        final Node pivot = pivot2target.getStart();
        final Node target = pivot2target.getEnd();

        /* consistency checks */
        if (target==null || pivot==null) 
            return null;

        if (!isText(target) && !isAnchorNode(target) && !isAttribute(target)) {
            throw new IllegalArgumentException("Unknown target node type: "+target);		
        }

        synchronized (pivot.getOwnerDocument()) {
            /* the starting node must be a template node such
             * that: it is either a node with an id attribute,
             * or an element that is the parent of a 
             * PCDATA containing an invariant text
             */
            final String pivot2targetXPath = pivot2targetXPath(pivot2target);
            final String targetXPath = getTargetXPath(target);
            final Set<String> result = new HashSet<>();
            for(String pivotXPath : getPivotXPaths(pivot)) {
                final String xpath = pivotXPath + '/' + pivot2targetXPath + '/' + targetXPath ;
                result.add(xpath);
            }
            return ( result );
        }
    }

    /**
     * @param pivot occurrence
     * @return an XPath expression to locate only the pivot
     */
    protected Set<String> getPivotXPaths(Node pivot) {
        if ( isText(pivot) ) {
            // a text with an invariant label as content
            final Text text = (Text)pivot;
            return getPivotXPath(text) ;
        } else if ( isElement(pivot) ) {
            // an element (with an id attribute)
            final Element element = (Element)pivot;
            return getPivotXPaths(element);
        } else throw new IllegalStateException("Unknown XPath translation of "+pivot+" as a pivot");
    }

    protected Set<String> getPivotXPath(final Text text) {
        return singleton("//text()" + containsPredicate(text));
    }

    static final private String _VALUE_PLACEHOLDER_ = "_$_";

    protected Set<String> getPivotXPaths(final Element pivot) {
        //TODO Make it configurable the details of which attributes are used in equals() and contains()
        final Set<String> result = new LinkedHashSet<>();
        // N.B. an element with an id attribute always plays as pivot
        final String elementName = this.caseHandler.elementCase(pivot.getNodeName()); // e.g., DIV

        /* generate [@id='text-123']-like XPath predicates */
        for(String attributeName : this.equalsPredicateAttributeNames) {
            if (isElementWithAttribute(pivot, attributeName)) {
                final String attrSubExp = this.caseHandler.attributeCase("@"+attributeName);
                result.addAll(templatedXPaths("//" + elementName + equalsPredicate(attrSubExp, _VALUE_PLACEHOLDER_), getIdValue(pivot)) );
            } 
        }
        /* generate [contains(@class,'text')]-like XPath predicates */
        for(String attributeName : this.containsPredicateAttributeNames) {
            if (isElementWithAttribute(pivot, attributeName)) {
                final String attrSubExp = this.caseHandler.attributeCase("@"+attributeName);
                result.addAll(templatedXPaths("//" + elementName + containsPredicate(attrSubExp,_VALUE_PLACEHOLDER_), extractWords(getClassValue(pivot))) );
            }
        }

        /* pivot on just the element name as last resort choice */
        if (result.isEmpty() && isElement(pivot)) {
            result.addAll( singleton ( "//" + elementName ) ); 
        }

        if (result.isEmpty())
            throw new IllegalStateException("Unknown XPath translation of "+pivot+" as a pivot");
        
        return result;
    }

    private Set<String> templatedXPaths(String template, String...strings) {
        final Set<String> result = new HashSet<>();
        for(String actual : strings) {
            if (actual.trim().isEmpty()) continue; // skip all white-spaces strings
            result.add(template.replace(_VALUE_PLACEHOLDER_,actual));
        }
        return result;
    }

    protected String pivot2targetXPath(Path pivot2value) {
        if (pivot2value.isEmpty()) return "";
        final StringJoiner joiner = new StringJoiner("/");
        pivot2value.getSteps().forEach( s -> 
            joiner.add(s.getXPathStepExpression())
        );

        return joiner.toString();
    }

    protected String getTargetXPath(Node target) {
        if (isText(target)) 
            return "self::text()" ;  // only texts, please...
        else if (isAnchorNode(target)) 
            return "self::*["+this.caseHandler.attributeCase("@href")+"]"; // only anchor nodes, please...
        else if (isAttribute(target))
            return "self::node()";   // already on target, stay there!
        throw new IllegalStateException("Unknown XPath translation of "+target+" as a target");
        // N.B. we need XPath 3.1 to support attribute() node test
        // see https://www.w3.org/TR/2017/REC-xpath-31-20170321/#id-attribute-test
    }

}