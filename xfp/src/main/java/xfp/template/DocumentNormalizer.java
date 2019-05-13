package xfp.template;

import static xfp.template.TemplateMarker.INVARIANT_MARKER;
import static xfp.template.TemplateMarker.VARIANT_MARKER;

import static xfp.util.DocumentUtil.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.uniroma3.dom.visitor.DOMVisitor;
import it.uniroma3.dom.visitor.DOMVisitorListenerAdapter;

/**
 * The evaluation of XPath expression strictly requires as input normalized DOM
 * tree documents, (otherwise unpredictable behaviors occurs.
 * <BR/>
 * (Check this spike-code: {it.uniroma3.weir.extraction.wrapper.pcdata.XPathAssumesNormalizedDocumentsSpike})
 * <BR/>
 * The normalization procedure implemented by the method 
 * {@link Document.#normalize()} is not supposed to preserve the DOM user-data
 * annotations (see {@link Node.#getUserData(String)}.
 * <BR/>
 * DOM user-data are currently used in this project to conveniently save close
 * to the DOM nodes information on their classification as either
 * template/invariant node or variant/value node.
 * <BR/>
 * This classification is operated at token/word level, not at the level of
 * whole PCDATAs, because it may happen that just a few words of a long
 * PCDATA should be considered part of the HTML template. 
 * <BR/>
 * This is implemented by splitting a single text of normalized document
 * normally associated with a whole PCDATA into a sequence of contiguous 
 * sibling text leaves so breaking the document normalization.
 * <BR/>
 * This class is in charge of normalizing a DOM document without loosing
 * user-data spread across several continguous texts that will be merged
 * after the normalization.
 * <BR/>
 * <U>
 * We leverage and based on the standard distribution implementation of
 * the {@link Document#normalize()} method that preserves the user-data
 * of the first text node of each sequence before merging it with the
 * following textual siblings.
 * </U>
 * <BR/>
 * Also, it save a reference to the merged node on every mergee nodes
 * as user-data using the user-data-key {@linkplain #MERGED_NODE}.
 */
public class DocumentNormalizer  extends DOMVisitorListenerAdapter {

	static final public String MERGED_NODE = "_MERGED_NODE_";
	
	final private String[] userdataKeys;

	public DocumentNormalizer() {
		this(INVARIANT_MARKER,
		     VARIANT_MARKER,
			 MERGED_NODE
		);
	}
	
	/**
	 * @param keys the DOM user-data to merge.
	 */
	public DocumentNormalizer(String...keys) {
		this.visitor = new DOMVisitor();
		this.visitor.setListener(this);
		this.setDOMVisitor(this.visitor);
		this.userdataKeys = keys;
	}

	public void normalize(Document doc) {
	    synchronized (doc) {
	        this.visitor.visit(doc);
	        doc.normalize();            
        }
	}

	@Override
	public void text(Text text) {
		if (isFirstOfAseriesOfContiguousTextSiblings(text)) {
		    synchronized (text) {
	            mergeDOMuserDatas(text);                
            }
		}
	}

    private void mergeDOMuserDatas(Text first) {
        if (!isText(first.getNextSibling())) return;
        
	    /* starts by transforming first node's user-data values to *list* of values */
		Map<String,List<Object>> acc = mergeDOMuserDatas(null, first, first);
		Node current = first.getNextSibling();
        /* accumulate others' nodes user-data key/value pairs to merge */
		while (current!=null && isText(current) ) {
			acc = mergeDOMuserDatas(acc, current, first);
            /* keep a reference back to the merged version (i.e., the first node) */
			if (current!=first)
			    current.setUserData(MERGED_NODE, first, null);
			current = current.getNextSibling();
		}
		setUserdata(first, acc);
	}

	private Map<String, List<Object>> mergeDOMuserDatas(final Map<String, List<Object>> acc, Node current, Node first) {
		Map<String, List<Object>> result = acc;
		// for each user-data-key to preserve, it accumulates
		// several user-data-values into one list of values
		for(String key : this.userdataKeys) {
			final Object value = current.getUserData(key);
			if (value!=null) {
				/* remove user-data once merged */
				current.setUserData(key, null, null);
				
				/* accumulate same-key user-data-value into a list */
				if (result==null) result = new HashMap<>();

				List<Object> values = result.get(key);
				if (values==null) {
					values = new LinkedList<>();
					result.put(key, values);
				}
				
				// keep it flat: no list of lists, please!
				if (value instanceof List)
					values.addAll((List<?>)value);
				else values.add(value);
			}
		}
		return result;
	}

	private void setUserdata(Text text, final Map<String, List<Object>> acc) {
		if (acc==null) return;
		for(String key : this.userdataKeys) {
			final Object value = acc.get(key);
			if (value!=null)
				text.setUserData(key, value, null);
		}
	}

	public static Node getMergedNode(Node node) {
	    return (Node) node.getUserData(MERGED_NODE);
	}
	
}
