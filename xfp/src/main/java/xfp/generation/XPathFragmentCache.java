package xfp.generation;

import java.util.Collection;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import xfp.model.Webpage;

/**
 * A cache implemented by using DOM user-data attached to DOM {@link Node}.
 * <BR/>
 * The cache is meant to work for partial artifacts strictly related to
 * specific {@link XPathFragment}, for instance a fragment-specific partial computation
 * such a the set of generated rules over the same target node.
 * <BR/>
 * So different fragments will lead to different cache values associated
 * with the same user-data-key.
 * <BR/>
 * Optionally, the cache can give sample-specific cache values, where a sample
 * is a collection of {@link Webpage}s whose {@link #hashCode()} is considered
 * sufficient for differentiating all the samples met.
 */
public class XPathFragmentCache<T> {
    
    private XPathFragment fragment;
    
    private int sampleUDK; /* hashcode of sample */
    
    public XPathFragmentCache(XPathFragment f) {
        this(f,null);
    }
    /* MGC */
    public XPathFragmentCache(XPathFragment fragment, Collection<Webpage> sample) {
        this.fragment  = fragment;
        this.sampleUDK = Objects.hashCode(sample);
    }

    @SuppressWarnings("unchecked")
    public T get(Node node, final String userDataKey) {
        synchronized ( getLockingNode(node) ) {
            return (T) node.getUserData(getSpecificUserDataKey(userDataKey));            
        }
    }

    final private Node getLockingNode(Node node) {
        Objects.requireNonNull(node);
        return ( node.getOwnerDocument()!=null ? node.getOwnerDocument() : node );
    }

    /**
     * 
     * @param node
     * @param userDataKey
     * @param userDataValue value to cache; null not permitted
     * @return
     */
    @SuppressWarnings("unchecked")
    public T set(Node node, final String userDataKey, T userDataValue) {
        synchronized ( getLockingNode(node) ) {
            return (T) node.setUserData(getSpecificUserDataKey(userDataKey), userDataValue, null);            
        }
    }
    
    /**
     * Generate a fragment-specific user-data key so that every {@link XPathFragment}
     * has a different user-data key on the same {@link Document} object.
     * @param dataKey
     * @return
     */
    final private String getSpecificUserDataKey(final String dataKey) {
        return dataKey+"-"+getFragmentUDKcomponent()+"-"+getSampleUDKcomponent();
    }
    private String getFragmentUDKcomponent() {
        return this.fragment.getClass().getName();
    }
    
    private int getSampleUDKcomponent() {
        return this.sampleUDK;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" over "+this.fragment+" and sample "+this.sampleUDK;
    }

}
