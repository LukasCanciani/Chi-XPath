package xfp.fixpoint;

import static xfp.util.DocumentUtil.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xfp.extraction.ExtractionRule;
import xfp.model.Value;

/**
 * Class for building rules extracting a list of {@link Value}s starting from a
 * {@link NodeList} extracted (by means of an XPath expression)
 * from the DOM tree representation of a web pages.
 * <BR/>
 * You can select a subset of the {@link Node}s from the provided
 * {@link NodeList} by overwriting the method {@linkplain #allowedNode(Node)}.
 * <BR/>
 * 
 * @param <T> the type of the value wrapped in the extracted {@link Value}s 
 */

abstract public class RuleFactory<T> {    
    
	static public RuleFactory<String> DATA_RULE_FACTORY = new DataRuleFactory();

	static public RuleFactory<URI>    LINK_RULE_FACTORY = new LinkRuleFactory();
	
    abstract public ExtractionRule<T> create(String xPathExpression);

    public List<Value<T>> createValues(NodeList nodes) {
        final List<Value<T>> list = new ArrayList<>();
        for (int i=0; i<nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            if (allowedNode(node))  {
                final Value<T> value = createValue(node);
                value.setNode(node);
                list.add(value);
            }
        }
        return list;
    }

    abstract protected boolean allowedNode(Node node);

    abstract protected Value<T> createValue(Node node);

	
    static final private class DataRuleFactory extends RuleFactory<String>  {
    
        @Override
        public ExtractionRule<String> create(String xPathExpression) {
            return new ExtractionRule<>(xPathExpression, this);
        }

        /* A regexp that catches all whitespace characters unicode + traditional.
         * Please, check:
           http://stackoverflow.com/questions/1822772/java-regular-expression-to-match-all-whitespace-characters 
         */
        static final private Pattern ALL_WHITESPACES = Pattern.compile("[\\p{Z}\\s]");

        private String sanitize(final String text) {
            if (text==null) return null;
            return ALL_WHITESPACES.matcher(text).replaceAll(" ").trim();
        }

        @Override
        public Value<String> createValue(Node node) {
            return new Value<String>(sanitize(node.getNodeValue()));
        }

        @Override
        public boolean allowedNode(Node node) {
            return ( isText(node) ) ;
        }

    }

    
    static final private class LinkRuleFactory extends RuleFactory<URI>{

        @Override
        public ExtractionRule<URI> create(String xPathExpression) {
            return new ExtractionRule<>(xPathExpression, this);
        }

        @Override
        public Value<URI> createValue(Node node) {
            final URI uri = URI.create(getHrefValue(node));
            return new Value<URI>(uri);
        }

        @Override
        public boolean allowedNode(Node node) {
            return ( isAnchorNode(node) ) ;
        }

    }

}