package it.uniroma3.fragment.cache;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.uniroma3.fragment.XPathFragment;
import it.uniroma3.fragment.XPathFragmentSpecification;

/**
 * A cache implemented by using DOM user-data attached to DOM {@link Node} objects.
 * <BR/>
 * The cache is meant to work for saving partial results obtained
 * by means of heavy computations, for instance an XPath Fragment-specific partial 
 * computation such as the set of generated rules over the same target node.
 * <BR/>
 * Different fragments (i.e., objects {@link XPathFragmentSpecification})
 * should lead to different cache values associated with the same user-data-key.
 * 
 * 
 * @author Valter Crescenzi <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class DOMCache<T> {
    
    private XPathFragmentSpecification specification;

    public DOMCache(XPathFragmentSpecification specification) {
        this.specification = specification;
    }

    @SuppressWarnings("unchecked")
    public T get(Node node, final String userDataKey) {
        synchronized ( getLockingNode(node) ) {
            return (T) node.getUserData(getFragmentSpecificUserDataKey(userDataKey));
        }
    }

    final private Node getLockingNode(Node node) {
        Objects.requireNonNull(node);
        return ( node.getOwnerDocument()!=null ? node.getOwnerDocument() : node );
    }

    /**
     * 
     * @param node - the node to set user data on
     * @param userDataKey - the user data key
     * @param userDataValue - value to cache; null not permitted
     * @return T - the previous value if any, null otherwise
     */
    @SuppressWarnings("unchecked")
    public T set(Node node, final String userDataKey, T userDataValue) {
        synchronized ( getLockingNode(node) ) {
            return (T) node.setUserData(getFragmentSpecificUserDataKey(userDataKey), userDataValue, null);            
        }
    }
    
    /**
     * Generate a fragment-specific user-data key so that every {@link XPathFragment}
     * has a different user-data key on the same {@link Document} object.
     * @param dataKey
     * @return a string working as a DOM-user-data-key that depends on the fragment
     */
    final private String getFragmentSpecificUserDataKey(final String dataKey) {
        return dataKey+"-"+getFragmentUDKcomponent();
    }    
    private String getFragmentUDKcomponent() {
        return Objects.toString(this.specification);
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
