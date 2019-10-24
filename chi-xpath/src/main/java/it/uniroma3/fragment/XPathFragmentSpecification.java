package it.uniroma3.fragment;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.uniroma3.fragment.step.CaseHandler;
import it.uniroma3.fragment.step.XPathStepFactory;


/**
 * 
 * A <EM>XPath Fragment Specification</EM> establishes:
 * <UL>
 *   <LI> the set of possible target node (see {@link #isSuitableTarget(Node)}
 *   <LI> the set of possible pivot nodes (see {@link #isSuitablePivot(Node)})
 *   <LI> the set of possible nodes to cross (see {@link #isCrossable(Node)})
 *   <LI> the set of possible step factories (see {@link #getStepFactories()} 
 *   <LI> the range (see {@link #getRange()} i.e., max number of steps
 * </UL>
 * 
 * An {@link XPathFragmentSpecification} specifies the set of XPath expressions
 * that can be generated on a page {@link Document}.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public abstract class XPathFragmentSpecification {

    static final private int DEFAULT_RANGE_VALUE = 3;

    private Set<XPathStepFactory> factories;
    
    private int range;

    public XPathFragmentSpecification(String... stepFactories) {
        this(Arrays.asList(stepFactories));
    }

    public XPathFragmentSpecification(List<String> stepFactories) {
        this(XPathStepFactory.create(stepFactories));
    }

    public XPathFragmentSpecification(Set<XPathStepFactory> factories) {
        this(factories, DEFAULT_RANGE_VALUE);
    }
    
    public XPathFragmentSpecification(XPathStepFactory...factories) {
        this(new HashSet<>(Arrays.asList(factories)));
    }

    public XPathFragmentSpecification(Set<XPathStepFactory> factories, int range) {
        Objects.requireNonNull(factories);
        this.factories = factories;
        this.range = range;
        factories.forEach( factory -> factory.setXPathFragmentSpecification(this) );
    }

    /**
     * 
     * @return the range, i.e., max number of steps, for this fragment
     */
    public int getRange() {
        return this.range;
    }
    /**
     * @param range - the new range for this {@link BackwardTreeExplorer}
     */
    public XPathFragmentSpecification setRange(int range) {
        this.range = range;
        return this;
    }

    /**
     * @return the set of  {@link XPathStepFactory}s enable for this fragment
     */
    public Set<XPathStepFactory> getStepFactories() {
        return Collections.unmodifiableSet(this.factories);
    }

    /**
     * @param node
     * @return true iff the node can work as a <EM>pivot</EM> in this fragment
     */
    abstract public boolean isSuitablePivot(Node node);

    /**
     * @param node
     * @return true iff the node can work as a <EM>target</EM> in this fragment
     */
    abstract public boolean isSuitableTarget(Node node);

    /**
     * @param node
     * @return true iff the node can be crossed while generating expressions
     *         of this fragment
     */
    abstract public boolean isCrossable(Node node);

    /**
     * 
     * @return true iff this fragment can extract multi-valued attributes
     *              i.e., several values per page
     */
    public boolean canExtractMultiValued() { return true; }
    
    public CaseHandler getCaseHandler() {
        return CaseHandler.HTML_STANDARD_CASEHANDLER;
    }

    @Override
    public String toString() {
        return this.getClass().getTypeName()+" "+this.getStepFactories();
    }
    
}
