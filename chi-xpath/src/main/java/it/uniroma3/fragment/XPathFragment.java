package it.uniroma3.fragment;


import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import it.uniroma3.fragment.cache.StepsCache;
import it.uniroma3.fragment.dom.DOMVisitor;
import it.uniroma3.fragment.dom.DOMVisitorListener;
import it.uniroma3.fragment.dom.DOMVisitorListenerAdapter;
import it.uniroma3.fragment.step.XPathStep;
import it.uniroma3.fragment.step.XPathStepFactory;
import lombok.Getter;


/**
 * An XPath fragment is the set of XPath expressions
 * that can be build by following the constraints and 
 * by using the set of {@link XPathStepFactory} in a 
 * {@link XPathFragmentSpecification} object on
 * one page (or several pages, if they are analyzed
 * as a collection and the set of expressions that are
 * generated on a page depends on the collection that 
 * page belong to, i.e., template-analysis).
 * <BR/>
 * Build a multi-edge directed graph over the DOM tree of a web page.
 * Every edge is associated with a corresponding XPath-step
 * expression leading from the node from which it departs to the
 * node corresponding to the result of its evaluation in the context
 * of the starting node.
 * <BR/>
 * Every edge is also associated with an {@link XPathStepFactory} that
 * created it.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class XPathFragment {

    @SuppressWarnings("unused")
    static final private Logger log = LoggerFactory.getLogger(XPathFragment.class);
    
    @Getter
    private XPathFragmentSpecification specification;
    
    private Set<Node> targets;     // suitable target nodes

    private Set<Node> unsuitable;  // unsuitable target nodes
    
    /* these maps save the XPath steps generated from/to a given document node */
    private Map<Node,Set<XPathStep>> fromNode2steps;
    private Map<Node,Set<XPathStep>> toNode2steps;
        
    @Getter
    private Document document;

    public XPathFragment(XPathFragmentSpecification specification, Document document) {
        Objects.requireNonNull(specification);
        Objects.requireNonNull(document);
        this.specification = specification;
        this.document = document;
        this.fromNode2steps = new HashMap<>();
        this.toNode2steps   = new HashMap<>();
        this.targets = new LinkedHashSet<>();
        this.unsuitable = new LinkedHashSet<>();
        new XPathFragmentBuilder().build();
    }        

    public Set<Node> getTargetNodes() {
        return this.targets;
    }

    public Set<Node> getUnsuitableTargetNodes() {
        return this.unsuitable;
    }

   /**
     * Build a multi-edge directed graph over the DOM tree of a web page by
     * following the specification contains in a {@link XPathFragmentSpecification} 
     * object.
     * <BR/>
     * Every edge is associated with a corresponding {@link XPathStep} object,
     * i.e., an XPath step expression leading from the node from which it departs 
     * to the node corresponding to the result of its evaluation in the context
     * of the starting node.
     * 
     * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
     * @author Wrapidity team
     * @author Fairhair.ai team
     */
    class XPathFragmentBuilder {
        
        final private Logger log = LoggerFactory.getLogger(XPathFragmentBuilder.class);

        private StepsCache cache; // facade to cached maps

        XPathFragmentBuilder() {
            this.cache = new StepsCache(specification, document);
        }
        /**
         * Build (and cache its steps) an XPath fragment over a {@link Document}
         * @param document - the whole document
         */
        public void build() {
            log.trace("building all XPath steps over " + document.getDocumentURI());
            if (!cache.areStepsAlreadyCached(document)) {
                // never built the fragment on this document
                
                /* N.B. This visit find the steps all over the DOM tree nodes, and does
                 *      not take into account whethere a node can be a target or a pivot 
                 */

                final DOMVisitorListener listener = new DOMVisitorListenerAdapter() {
                    @Override
                    public void startElement(Element element)  {
                        process(element);
                    }            
                    @Override
                    public void text(Text text)   {
                        process(text);
                    }
                    @Override
                    public void attribute(Attr attr)   {
                        process(attr);
                    }

                    private void process(Node node) {
                        checkSuitablenessAsTarget(node);
                        generateAllXPathSteps(node);
                    }
                    private void checkSuitablenessAsTarget(Node n) {
                        if (specification.isSuitableTarget(n)) 
                            targets.add(n);
                        else unsuitable.add(n);
                    }
                    protected void generateAllXPathSteps(Node node) {
                        if (fromNode2steps.containsKey(node)) return; // already processed ?
                        final Set<XPathStep> steps = availableFrom(node);
                        for(XPathStep step : steps)
                            process(step.getTo());
                    }

                };
                final DOMVisitor visitor = new DOMVisitor(listener);
                listener.setDOMVisitor(visitor);
                log.trace("Generating all XPath steps of fragment "+this);
                visitor.visit(document.getDocumentElement());
                cache.setStepsFromNodes(document, fromNode2steps);
                cache.setStepsFromNodes(document, toNode2steps);
            } else { // The XPath fragment has been already built on this document
                log.trace("Fragment already built; recovering from cache.");
                fromNode2steps = cache.getStepsFromNodes(document);
                toNode2steps   = cache.getStepsToNodes(document);            
            }
        }

        /**
         * The list of all available {@link XPathStep}s <U>from</U> the given node
         * @param current - the reference node
         * @return the list of all available xpath steps from the reference node
         */
        public Set<XPathStep> availableFrom(Node current) {
            Objects.requireNonNull(current);

            /* check cache first */
            Set<XPathStep> result = fromNode2steps.get(current);

            if (result==null) {
                result = new HashSet<>();
                for (XPathStepFactory factory : specification.getStepFactories()) {
                    result.addAll(factory.from(current));
                }

                /* save by indexing  step:  from -> to */
                fromNode2steps.put(current, result);
                /* now index them backward: to <- from */
                this.indexStepsToNode(result);
            }
            return result;
        }

        private void indexStepsToNode(Set<XPathStep> result) {
            for(XPathStep step : result) {
                final Node to = step.getTo();
                Set<XPathStep> stepsLeadingTo = toNode2steps.get(to);
                if (stepsLeadingTo==null) {
                    stepsLeadingTo = new HashSet<>();
                    toNode2steps.put(to, stepsLeadingTo);
                }
                stepsLeadingTo.add(step);
            }
        }


    }
    
    
    /**
     * The list of all available {@link XPathStep}s <U>from</U> the given node.
     * 
     * @param current - the reference node
     * @return the list of all available xpath steps <U>from</U> the reference node
     */
    public Set<XPathStep> availableFrom(Node current) {
        Objects.requireNonNull(current);
        return fromNode2steps.get(current);
    }

    /**
     * The list of all available {@link XPathStep}s <U>to</U> the given node.
     * 
     * @param current - the reference node
     * @return the list of all available xpath steps <U>to</U> the reference node
     */
    public Set<XPathStep> availableTo(Node current) {
        Objects.requireNonNull(current);
        /* we do  NOT support generation of XPath leading TO a node */
        /* we just index by to-node the steps generated FROM a node */
        
        /* check index to -> {from} */
        return toNode2steps.getOrDefault(current,Collections.emptySet());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" "+this.getSpecification();
    }

	public XPathFragmentSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(XPathFragmentSpecification specification) {
		this.specification = specification;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
