package xfp.generation;

import static xfp.hlog.XFPStyles.header;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.uniroma3.dom.visitor.DOMVisitor;
import it.uniroma3.dom.visitor.DOMVisitorListener;
import it.uniroma3.dom.visitor.DOMVisitorListenerAdapter;
import it.uniroma3.hlog.HypertextualLogger;
import xfp.fixpoint.RuleFactory;
import xfp.model.Webpage;
import xfp.template.TemplateAnalyzer;

/**
 * Build a multi-edge directed graph over the DOM tree of a web page.
 * Every edge is associated with a corresponding XPath (step)
 * expression leading from the vertex from which it departs to the
 * node corresponding to the result of its evaluation in the context
 * of the starting node.
 * <BR/>
 * Every edge is also associated with an {@link XPathStepFactory} that
 * made it and it is considered belonging to.
 */
public abstract class XPathFragment implements XPathFragmentFactory {

    static final private HypertextualLogger log = HypertextualLogger.getLogger();

    static final private String FROM_NODE_2_STEPS_USER_DATA_KEY = "FROM_NODE_2_STEPS_MAP_";
    static final private String __TO_NODE_2_STEPS_USER_DATA_KEY = "__TO_NODE_2_STEPS_MAP_";

    private Set<XPathStepFactory> factories;

    /* these caches save the XPath steps generated from/to a given document node */
    private Map<Node,Set<XPathStep>> fromNode2steps;
    private Map<Node,Set<XPathStep>> toNode2steps;

    private XPathFragmentCache<Map<Node, Set<XPathStep>>> cache; // facade to cached maps

    public XPathFragment(List<String> stepFactories) {
        this(XPathStepFactory.create(stepFactories));
    }

    public XPathFragment(Set<XPathStepFactory> factories) {
        Objects.requireNonNull(factories);
        this.factories = factories;
        this.fromNode2steps = null;
        this.toNode2steps   = null;
        this.cache = new XPathFragmentCache<>(this);
    }

    public int getRange() {
        return MAX_PIVOT_DISTANCE;
    }

    public Set<XPathStepFactory> getStepFactories() {
        return Collections.unmodifiableSet(this.factories);
    }

    abstract public <T> RuleFactory<T> getRuleFactory();

    /**
     * 
     * @return true iff this fragment can extract multi-valued vectors
     *              i.e., several values for every page
     */
    abstract public boolean coverMultiValued();

    /**
     * A method to pre-process input sample before performing actual
     * fragment generation.
     * <BR/>
     * For instance, the samples could be analyzed to classify their
     * DOM tree nodes template/invariant nodes or variant/data nodes.
     * 
     * @param sample - set of {@link Webpage}s to preprocess
     */
    public void preprocess(Set<Webpage> sample) {        
        final TemplateAnalyzer analyzer = new TemplateAnalyzer();
        analyzer.findTemplateTokens(sample);
    }

    public void build(Webpage page) {
        build(page.getDocument());
    }
    /**
     * Build (and cache its steps) an XPath fragment over a {@link Document}
     * @param document - the whole document
     */
    public void build(final Document document) {
        if (!isFragmentCached(document)) {
            // never built the fragment on this document            
            this.fromNode2steps = initIndexMap(document,FROM_NODE_2_STEPS_USER_DATA_KEY);
            this.toNode2steps   = initIndexMap(document,__TO_NODE_2_STEPS_USER_DATA_KEY);

            final DOMVisitorListener listener = new DOMVisitorListenerAdapter() {
                @Override
                public void startElement(Element element)  {
                    /* N.B. that saves paths but would */
                    /* prevent from  caching the steps */
                    //if (isSuitablePivot(element))
                    generateAllXPathSteps(element);
                }            
                @Override
                public void text(Text text)   {
                    //if (isSuitablePivot(text)) /* N.B. idem */
                    generateAllXPathSteps(text);
                }

                private void generateAllXPathSteps(Node node) {
                    if (fromNode2steps.containsKey(node)) return; // already processed ?
                    Set<XPathStep> steps = availableFrom(node);
                    for(XPathStep step : steps)
                        generateAllXPathSteps(step.getTo());
                }

            };
            final DOMVisitor visitor = new DOMVisitor(listener);
            listener.setDOMVisitor(visitor);
            log.trace("Generating all XPath steps of fragment "+this);
            log.newTable();
            log.trace(header("Step"),header("From"),header("To"),header("from-XPath"),header("to-XPath"));
            visitor.visit(document.getDocumentElement());
            log.endTable();
        } else { // The XPath fragment has been already built on this document
            log.trace("Fragment already built; recovering caches.");
            this.fromNode2steps = getCache(document, FROM_NODE_2_STEPS_USER_DATA_KEY);
            this.toNode2steps   = getCache(document, __TO_NODE_2_STEPS_USER_DATA_KEY);            
        }
    }

    private Map<Node, Set<XPathStep>> getCache(Document doc, String userDataKey) {
        return this.cache.get(doc, userDataKey);
    }

    private boolean isFragmentCached(Document doc) {
        return ( getCache(doc,FROM_NODE_2_STEPS_USER_DATA_KEY)!=null );
    }

    private void setCache(Document doc, String userDataKey, Map<Node, Set<XPathStep>> map) {
        this.cache.set(doc, userDataKey, map);
    }

    public void reset(Document document) {
        setCache(document, FROM_NODE_2_STEPS_USER_DATA_KEY, null);
        setCache(document, __TO_NODE_2_STEPS_USER_DATA_KEY, null);
    }

    private Map<Node, Set<XPathStep>> initIndexMap(Document doc, final String userDataKey) {
        Map<Node, Set<XPathStep>> cached = getCache(doc, userDataKey);
        if (cached==null)
            cached = new ConcurrentHashMap<>();
        this.setCache(doc, userDataKey, cached);
        return cached;
    }

    /**
     * The list of all available {@link XPathStep}s <U>from</U> the given node
     * @param current - the reference node
     * @return the list of all available xpath steps from the reference node
     */
    public Set<XPathStep> availableFrom(Node current) {
        Objects.requireNonNull(current);

        /* check cache first */
        Set<XPathStep> result = this.fromNode2steps.get(current);

        if (result==null) {
            result = new HashSet<>();
            for (XPathStepFactory factory : this.factories) {
                result.addAll(factory.from(current));
            }

            /* save by indexing  step:  from -> to */
            this.fromNode2steps.put(current, result);
            /* now index them backward: to <- from */
            indexStepsToNode(result);
        }
        return result;
    }

    private void indexStepsToNode(Set<XPathStep> result) {
        for(XPathStep step : result) {
            final Node to = step.getTo();
            Set<XPathStep> stepsLeadingTo = this.toNode2steps.get(to);
            if (stepsLeadingTo==null) {
                stepsLeadingTo = new HashSet<>();
                this.toNode2steps.put(to, stepsLeadingTo);
            }
            stepsLeadingTo.add(step);
        }
    }

    /**
     * The list of all available {@link XPathStep}s <U>to</U> the given node.
     * 
     * @param current - the reference node
     * @return the list of all available xpath steps to the reference node
     */
    public Set<XPathStep> availableTo(Node current) {
        Objects.requireNonNull(current);

        /* check index to -> {from} */
        final Set<XPathStep> result = this.toNode2steps.get(current);
        /* we do  NOT support generation of XPath leading TO a node */
        /* we just index by to-node the steps generated FROM a node */
        return ( result==null ? Collections.emptySet() : result ) ;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" "+this.getStepFactories()+"[range="+this.getRange()+"]";
    }

}
