package it.uniroma3.fragment.cache;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.uniroma3.fragment.XPathFragmentSpecification;
import it.uniroma3.fragment.step.XPathStep;
/**
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class StepsCache {
    
    static final private String FROM_NODE_2_STEPS_USER_DATA_KEY = "FROM_NODE_2_STEPS_MAP_";
    static final private String __TO_NODE_2_STEPS_USER_DATA_KEY = "__TO_NODE_2_STEPS_MAP_";

    private DOMCache<Map<Node, Set<XPathStep>>> cache;

    public StepsCache(XPathFragmentSpecification specification, Document document) {
        this.cache = new DOMCache<Map<Node, Set<XPathStep>>>(specification);
    }
    
    public void reset(Document document) {
        this.cache.set(document, FROM_NODE_2_STEPS_USER_DATA_KEY, null);
        this.cache.set(document, __TO_NODE_2_STEPS_USER_DATA_KEY, null);
    }

    public boolean areStepsAlreadyCached(Document doc) {
        return ( this.cache.get(doc, FROM_NODE_2_STEPS_USER_DATA_KEY)!=null );
    }

    public Map<Node, Set<XPathStep>> setStepsFromNodes(Document document, Map<Node, Set<XPathStep>> map) {
        return this.cache.set(document, FROM_NODE_2_STEPS_USER_DATA_KEY, map);
    }

    public Map<Node, Set<XPathStep>> setStepsToNodes(Document document, Map<Node, Set<XPathStep>> map) {
        return this.cache.set(document, __TO_NODE_2_STEPS_USER_DATA_KEY, map);
    }

    public Map<Node, Set<XPathStep>> getStepsFromNodes(Document document) {
        return this.cache.get(document, FROM_NODE_2_STEPS_USER_DATA_KEY);
    }

    public Map<Node, Set<XPathStep>> getStepsToNodes(Document document) {
        return this.cache.get(document, __TO_NODE_2_STEPS_USER_DATA_KEY);
    }

}
