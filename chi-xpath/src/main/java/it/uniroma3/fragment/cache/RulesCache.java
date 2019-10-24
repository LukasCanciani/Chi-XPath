package it.uniroma3.fragment.cache;

import java.util.Set;

import org.w3c.dom.Node;

import it.uniroma3.fragment.XPathFragmentSpecification;

public class RulesCache {

    static final private String GENERATED_RULES_USER_DATA_KEY = "_GENERATED_RULES_";

    private DOMCache<Set<String>> cache;

    public RulesCache(XPathFragmentSpecification specification) {
        this.cache = new DOMCache<>(specification);
    }

    public Set<String> getRules(Node node) {
        return cache.get(node, GENERATED_RULES_USER_DATA_KEY);
    }

    public void setRules(Node node, Set<String> rules) {
        this.cache.set(node, GENERATED_RULES_USER_DATA_KEY, rules);
    }

}
