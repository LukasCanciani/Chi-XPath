package xfp.generation;

import static java.util.Collections.*;
import static javax.xml.xpath.XPathConstants.NODESET;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;
import static xfp.util.DocumentUtil.isElement;
import static xfp.util.DocumentUtil.isText;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import it.uniroma3.hlog.HypertextualLogger;
import it.uniroma3.util.MarkUpUtils;
import xfp.XFPException;

public abstract class XPathStepFactory {

   // static final private HypertextualLogger log = HypertextualLogger.getLogger();

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
                LinkHunter.class
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
            throw new XFPException(e);
        }
    }};

    
    static public Set<XPathStepFactory> create(List<String> stepFactoriesShortNames) {
        return stepFactoriesShortNames.stream()
            .map( shortName -> createXPathStepFactory(shortName))
            .collect(Collectors.toSet());
    }
    
    
    static private XPathStepFactory createXPathStepFactory(String shortName) {
        final XPathStepFactory stepFactory = shortname2stepFactoryInstance.get(shortName);
        if (stepFactory==null) {
            final String simpleName = XPathStepFactory.class.getSimpleName();
            throw new XFPException(
                    "No known "+simpleName+" has short name "+shortName+"!"
                    +"\nAvailable "+simpleName+"(s):\n"+
                    shortname2stepFactoryInstance.keySet()+".");
        }
        return stepFactory;
    }


    public List<XPathStep> from(final Node from) {
        Objects.requireNonNull(from, "Cannot generate steps from a null node");

        if (!isTextOrElement(from)) {
//            log.warn("Cannot generate steps from nodes which are neither texts or elements: "+from);
            return emptyList();
        }

        return makeSteps(from);
    }


    static final private boolean isTextOrElement(Node node) {
        return ( isText(node) || isElement(node) );
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
//                log.trace(exp, from, "<EM>XPath evaluation without results</EM>");
            } else {
                for(int index=0; index<evaluation.getLength(); index++) {
                    final Node to = evaluation.item(index);
                    final XPathStep step = new XPathStep(exp, from, to);
                    step.setGeneratedBy(this);
                    result.add(step);                    
 //                   log.trace(step, from, to, xpath(from), xpath(to));
                }
            }
        });
        return result;
    }

    @SuppressWarnings("unused")
	private String xpath(Node n) {
        return "TODO"; //TODO if we want to log precise info about the occurrence
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
     * it starts from (see {@link #getFrom()}) to get the
     * node it leads to  (see {@link #getTo()}).
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
//            if (result==null)
  //              log.warn("Erroneous XPath step expression \'"+xpathExp+"\' is not locating any node");
        }
        catch (RuntimeException re) {
            System.err.println("XPath:   "+xpathExp);
            System.err.println("Context: "+MarkUpUtils.dumpTree(context));
            throw new IllegalStateException(re);
        } catch (XPathExpressionException e) {
            System.err.println("XPath:   "+xpathExp);
 //           log.warn("Malformed XPath expression "+xpathExp);
            throw new RuntimeException(e);
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

    static abstract class SingleStepFactory extends XPathStepFactory {
        /**
         * <BR/>
         * Override this method to make it fast.
         * <BR/>
         * Do not override this method to avoid re-implementing
         * the logics of the corresponding XPath expression.
         * @param context - the starting node
         * @return the node-set returned by the evaluation of this step
         */
        protected Node query(Node context) {
            final String expr = makeStepExpression(context);
            final NodeList nodeset = super.eval(context, expr);
            if (nodeset==null) return null;
            if (nodeset.getLength()!=1) {
                throw new IllegalStateException("A "+SingleStepFactory.class.getSimpleName()
                        +" cannot return multiple nodes. From "+context+" with xpath: "+expr);
            }
            return nodeset.item(0);
        }
        @Override
        protected List<XPathStep> makeSteps(Node from) {
            final Node to = query(from);
            if (to==null) return emptyList();
            final String exp = makeStepExpression(from);
            final XPathStep step = new XPathStep(exp,from,to);
            step.setGeneratedBy(this);
            return singletonList(step);
        }
        
        abstract protected String makeStepExpression(Node context);
        
    }
    
    static public class Up extends SingleStepFactory {
        @Override
        protected Node query(Node context) {
            return getContiguousElement(context, Node::getParentNode);
        }

        @Override
        protected String makeStepExpression(Node context) {
            return "..";
        }
    }

    static public class LeftElement extends SingleStepFactory {
        @Override
        protected Node query(Node from) {
            return getContiguousElement(from, Node::getPreviousSibling);
        }
        @Override
        protected String makeStepExpression(Node from) {
            return "preceding-sibling::*";
            /* N.B. other options are easily available. Just do:
             *      return "preceding-sibling::text()"
             *      return "preceding-sibling::node()"
             */
        }
    }
        
    static public class RightElement extends SingleStepFactory {
        @Override
        protected Node query(Node context) {
            return getContiguousElement(context, Node::getNextSibling);
        }
        @Override
        protected String makeStepExpression(Node from) {
            return "following-sibling::*";
        }
    }

    static public class RightNamedElement extends SingleStepFactory {
        @Override
        protected Node query(Node context) {
            Node current = context;
            do {
                current = current.getNextSibling(); // move in the given direction
            } while (current!=null && !isElement(current));                    
            return current;
        }
        @Override
        protected String makeStepExpression(Node from) {
            return "following-sibling::"+query(from).getNodeName();
        }
    }

    static public class RightText extends SingleStepFactory {
        @Override
        protected Node query(Node context) {
            Node current = context;
            do {
                current = current.getNextSibling(); // move in the given direction
            } while (current!=null && !isText(current));                    
            return current;
        }
        @Override
        protected String makeStepExpression(Node from) {
            return "following-sibling::text()[1]";
        }
    }

    static final private Node getContiguousElement(Node from, Function<Node, Node> direction) {
        Node current = from;
        do {
            current = direction.apply(current); // move in the given direction
        } while (current!=null && !isElement(current));                    
        return current;
    }

    static public class Down extends XPathStepFactory {
        
        private boolean text; // target text (by pos, implicit)
        private boolean name; // target element by name
        private boolean pos;  // target element by pos
        private boolean pan;  // target element by pos and name
        
        public Down() {
            this(true,false,false,true);
        }
        
        /**
         * @param textTarget   true iff generates XPath steps to target textual leaves
         * @param elementName   "    "      "       "     "    " reach elements by their name 
         * @param position      "    "      "       "     "    "   "       "    by their position
         * @param posAndName    "    "      "       "     "    "   "       "    by their position and name
         */
        public Down(boolean textTarget, boolean elementName, boolean position, boolean posAndName) {
            this.text = textTarget;
            this.name = elementName;
            this.pos  = position;
            this.pan  = posAndName;
        }

        @Override
        protected Set<String> makeStepExpressions(Node current) {
            final NodeList children = current.getChildNodes();
            if (children.getLength()==0) return Collections.emptySet();

            final Map<String,Node> result = new LinkedHashMap<>();

            /* N.B. DOM could be denormalized with a single PCDATA
                split into several contiguous sibling text nodes */
            boolean textIsSeparated = true; // next text is well separated? 

            int positionAsElem = 1; // XPath position() for child::*[pos] like expr.
            int positionAsText = 1; // XPath position() for   text()[pos] like expr.
            //XPath position() for every element name to build  NAME[pos] like expr.

            final Map<String, Integer> name2pos = new HashMap<>(); 

            for (int i=0; i<children.getLength(); i++) { // XPath positions are 1-based
                final Node child = children.item(i);
                if (child==null) continue;
                final short type = child.getNodeType();
                switch (type) {
                case TEXT_NODE:// ----- It's a text
                    if (textIsSeparated) {
                        if (this.text) {
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
                    if (this.name) result.put(xpathStepDownToElementOfName(child), child);
                    if (this.pos)  result.put(xpathStepDownToElementOfPos(child, positionAsElem), child);
                    if (this.pan)  result.put(xpathStepDownToElementOfNameAndPos(child, positionByName),child);           
                    positionByName++;
                    positionAsElem++;

                    // n.b. an element separates texts: fall-through the case
                default: // ----------- It's something else
                    // e.g., an HTML comment <!-- -->;
                    // we do not care what it is but beside that 
                    // it separates otherwise contiguous text nodes
                    textIsSeparated = true;
                }
            }
            return result.keySet();
        }


        private String xpathStepDownToText(Node text, int pos) {
            return "text()"+ ( this.pos ? "["+pos+"]" : "" );
        }

        private String xpathStepDownToElementOfPos(Node element, int pos) {
            return "*["+pos+"]";
        }

        private String xpathStepDownToElementOfName(Node element) {            
            return element.getNodeName().toUpperCase();
        }

        private String xpathStepDownToElementOfNameAndPos(Node element, int pos) {            
            return xpathStepDownToElementOfName(element)+"["+pos+"]";
        }

    }

    static public class LinkHunter extends XPathStepFactory {
        @Override
        protected Set<String> makeStepExpressions(Node from) {
            if (isElement(from))
                return singleton(".//A");
            else return emptySet();
        }
    }    

    static public class Sniper extends XPathStepFactory {
        @Override
        protected Set<String> makeStepExpressions(Node from) {
            if (isElement(from) && eval(from, ".//text()").getLength()==1)
                return singleton(".//text()[1]");
            else return emptySet();
        }
    }    

}
