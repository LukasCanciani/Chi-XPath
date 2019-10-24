package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.util.DocumentUtils.isAttribute;
import static it.uniroma3.fragment.util.DocumentUtils.isElement;
import static it.uniroma3.fragment.util.DocumentUtils.isText;
import static java.util.Collections.emptyList;
import static javax.xml.xpath.XPathConstants.NODESET;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.uniroma3.fragment.XPathFragmentSpecification;
import it.uniroma3.fragment.dom.DOMDumper;

/**
 * This class is in charge of generating a set of available {@link XPathStep}s
 * for creating a {@link XPathFragmentSpecification}.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public abstract class XPathStepFactory {

    static final private Logger LOGGER = LoggerFactory.getLogger(XPathStepFactory.class);

    static final private XPathFactory XPATHFACTORY = XPathFactory.newInstance();

    @SuppressWarnings({ "rawtypes", "unchecked", "serial" })
    static final private Map<String,XPathStepFactory> shortname2stepFactoryInstance = new HashMap() {{
        final Class[] stepFactoryClasses = { 
                Up.class, 
                Down.class, // i.e., == DTFFT
                RightElement.class, 
                RightNamedElement.class, 
                RightText.class,
                LeftElement.class,
                Sniper.class,
                LinkHunter.class,
                NamedAttribute.class
        };
        Arrays.stream(stepFactoryClasses).forEach( c -> {
            Class<? extends XPathStepFactory> stepFactoryClass = c;
            XPathStepFactory instance = makeXPathStepFactoryInstance(stepFactoryClass);
            put(instance.getShortName(),instance);
        });
        /* Down has a rather complex constructor */
        put("DFFFF",new Down(false,false,false,false));
        put("DFFFT",new Down(false,false,false,true ));
        put("DFFTF",new Down(false,false,true ,false));
        put("DFFTT",new Down(false,false,true ,true ));
        
        put("DFTFF",new Down(false,true ,false,false));
        put("DFTFT",new Down(false,true ,false,true ));
        put("DFTTF",new Down(false,true ,true ,false));
        put("DFTTT",new Down(false,true ,true ,true ));
        
        put("DTFFF",new Down(true ,false,false,false));
        put("DTFFT",new Down(true ,false,false,true ));
        put("DTFTF",new Down(true ,false,true ,false));
        put("DTFTT",new Down(true ,false,true ,true ));
        
        put("DTTFF",new Down(true ,true ,false,false));
        put("DTTFT",new Down(true ,true ,false,true ));
        put("DTTTF",new Down(true ,true ,true ,false));
        put("DTTTT",new Down(true ,true ,true ,true ));
    }

    private XPathStepFactory makeXPathStepFactoryInstance(Class<? extends XPathStepFactory> stepFactoryClass) {
        try {
            return stepFactoryClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error while building XPath", e);
        }
    }};


    static public Set<XPathStepFactory> create(String... stepFactoriesShortNames) {
        return create(Arrays.asList(stepFactoriesShortNames));
    }

    static public Set<XPathStepFactory> create(List<String> stepFactoriesShortNames) {
        return stepFactoriesShortNames.stream()
            .map( shortName -> create(shortName))
            .collect(Collectors.toSet());
    }
    
    /**
     * Given a the short-name of a {@link XPathStepFactory} concrete subclass, it creates an instance, 
     * i.e., LH -> {@link LinkHunter} ;  
     * @param shortName - the short name of the {@link XPathStepFactory} to create
     * @return the instance of a {@link XPathStepFactory} of the given short name
     */
    static public XPathStepFactory create(String shortName) {
        final XPathStepFactory stepFactory = shortname2stepFactoryInstance.get(shortName);
        if (stepFactory==null) {
            final String simpleName = XPathStepFactory.class.getSimpleName();
            throw new RuntimeException(
                    "No known "+simpleName+" has short name "+shortName+"!"
                    +"\nAvailable "+simpleName+"(s):\n"+
                    shortname2stepFactoryInstance.keySet()+".");
        }
        return stepFactory;
    }

    private XPathFragmentSpecification xpathFragmentSpecification;
    
    protected XPathStepFactory() {
        this.xpathFragmentSpecification = null;
    }
    
    protected XPathStepFactory(XPathFragmentSpecification specification) {
        this.xpathFragmentSpecification = specification;
    }
    
    public XPathFragmentSpecification getXPathFragmentSpecification() {
        return this.xpathFragmentSpecification;
    }
    
    public XPathStepFactory setXPathFragmentSpecification(XPathFragmentSpecification spec) {
        this.xpathFragmentSpecification = spec;
        return this;
    }
    
    public CaseHandler getCaseHandler() {
        Objects.requireNonNull(getXPathFragmentSpecification(), "Must set an "+XPathFragmentSpecification.class.getSimpleName());
        return getXPathFragmentSpecification().getCaseHandler();
    }
    
    protected String elementCase(String element) {
        return getCaseHandler().elementCase(element);
    }

    protected String attributeCase(String attribute) {
        return getCaseHandler().attributeCase(attribute);
    }

    public List<XPathStep> from(final Node from) {
        Objects.requireNonNull(from, "Cannot generate steps from a null node");

        if (!isCrossable(from)) {
            LOGGER.warn("Cannot generate steps from nodes which are neither texts nor elements nor attributes: "+from);
            return emptyList();
        }

        return makeSteps(from);
    }


    static final private boolean isCrossable(Node node) {
        return ( isText(node) || isElement(node) || isAttribute(node) );
    }

    /**
     * Creates the {@link XPathStep}s from the context of the node
     * it starts from (see {@link XPathStep#getFrom()}) to get the
     * node it leads to  (see {@link XPathStep#getTo()}).
     * <HR/>
     * <BR/>
     * Override this method to make it fast.
     * <BR/>
     * Do not override this method to avoid re-implementing
     * the logics of the corresponding XPath expressions as
     * returned by {@link #makeStepExpressions(Node)} }
     * @param from - the context node from which to generated the steps
     * @return the {@link XPathStep}s for moving from the context node
     *         to each of the node-set returned by the evaluation of 
     *         expression
     */
    protected List<XPathStep> makeSteps(final Node from) {
        final List<XPathStep> result = new LinkedList<>();
        this.makeStepExpressions(from).forEach( exp -> {
            final NodeList evaluation = eval(from, exp);
            if (evaluation==null) {
                LOGGER.trace(exp, from, "<EM>XPath evaluation without results</EM>");
            } else {
                for(int index=0; index<evaluation.getLength(); index++) {
                    final Node to = evaluation.item(index);
                    final XPathStep step = new XPathStep(exp, from, to);
                    step.setGeneratedBy(this);
                    result.add(step);                    
                    LOGGER.trace("" + step + from + to + xpath(from) + xpath(to));
                }
            }
        });
        return result;
    }

    private String xpath(Node n) {
        return n.toString(); //TODO if we want to log precise info about the occurrence
    }

    /**
     * @param from - the starting node (context node)
     * @return a set of XPath step expressions generated to crawl
     *         the DOM tree starting from the context node
     */
    protected Set<String> makeStepExpressions(Node from) {
        throw new UnsupportedOperationException(
                  "Either you override this method, "
                + "or you override the makeSteps() method calling it ");
    }

    /**
     * Evaluate this XPath step from the context of the node
     * it starts from (see {@link XPathStep#getFrom()}) to get the
     * node it leads to  (see {@link XPathStep#getTo()}).
     * <BR/>
     * Override this method to make it fast.
     * <BR/>
     * Do not override this method to avoid re-implementing
     * the logics of the corresponding XPath expression.
     * @param context
     * @return the node-set returned by the evaluation of this step
     */
    protected NodeList eval(Node context, String xpathExp) {
        /* N.B.: this requires the input document to have been already normalized,
         *       i.e., contiguous text siblings should have been already merged
         */
        NodeList result = null; /* just one node */
        try {
            final XPathExpression xpath = compileXPath(xpathExp);
            result = (NodeList) xpath.evaluate(context, NODESET);
            if (result==null)
                LOGGER.warn("Erroneous XPath step expression \'"+xpathExp+"\' is not locating any node");
        }
        catch (RuntimeException re) {
            System.err.println("XPath:   "+xpathExp);
            System.err.println("Context: "+new DOMDumper().dump(context));
            throw new IllegalStateException(re);
        } catch (XPathExpressionException e) {
            System.err.println("XPath:   "+xpathExp);
            LOGGER.error("Malformed XPath expression "+xpathExp); // TODO fix, e.g.,  by means of a better escaping strategy
        }
        return result;
    }    

    
    private String shortname = null;
    
    @Override
    public String toString() {
        if (this.shortname==null) { 
            this.shortname = getShortName();
        }
        return this.shortname;
    }


    public String getShortName() {
        final StringBuilder builder = new StringBuilder();
        for(char c : this.getClass().getSimpleName().toCharArray())
            if (Character.isUpperCase(c))
                builder.append(c);
        return builder.toString();
    }
    
    static final private WeakHashMap<String, XPathExpression> cache = new WeakHashMap<>();

    private XPathExpression compileXPath(String xpath) throws XPathExpressionException {
        XPathExpression expr = cache.get(xpath);
        if (expr!=null) return expr;
        expr = XPATHFACTORY.newXPath().compile(xpath);
        cache.put(xpath, expr);
        return expr;
    }

    static final Node getContiguousElement(Node from, Function<Node, Node> direction) {
        Node current = from;
        do {
            current = direction.apply(current); // move in the given direction
        } while (current!=null && !isElement(current));                    
        return current;
    }

}
