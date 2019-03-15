package it.uniroma3.fragment;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.uniroma3.fragment.cache.RulesCache;
import lombok.Getter;

/**
 * This class is in charge of generating a set of extraction rules
 * each pivoted on a pivot node and targeting either a value or a link
 * node to extract.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class RuleInference {

    static final private Logger log = LoggerFactory.getLogger(RuleInference.class);

    @Getter
    private XPathFragmentSpecification specification; /* XPath Fragment specification */

    private XPathFragment xpathFragment;
    
    private RulesCache rulesCache;

    public RuleInference(XPathFragmentSpecification specification) {
        this.specification = specification;
        /* create a fragment + sample specific cache */
        this.rulesCache = new RulesCache(specification);
        this.xpathFragment = null;
    }

    public XPathFragment getXPathFragment() {
        return this.xpathFragment;
    }
    
    public XPathFragmentSpecification getXPathFragmentSpecification() {
        return this.specification;
    }

    public Set<String> inferRules(Document page) {
        Objects.requireNonNull(page);
        /* cache rules generated for a page in its root Document node */
        final Set<String> cached = this.rulesCache.getRules(page);
        if (cached!=null) return cached;
        
        this.xpathFragment = new XPathFragment(this.specification,page);
        
        final Set<String> rules = newConcurrentHashSet();

        final AtomicInteger samplesCounter = new AtomicInteger(1);

        log.trace(" rules generation over input sample(s) " + page);
        final Set<Node> targets = xpathFragment.getTargetNodes();
        log.trace(targets.size()    + " target(s) found",  targets);
        final Set<Node> unsuitables = xpathFragment.getUnsuitableTargetNodes();
        log.trace(unsuitables.size() + " unsuitable node(s) found", unsuitables);

        final AtomicLong targetsCounter = new AtomicLong(1);
        final AtomicLong sampleNewRules = new AtomicLong(0);
        for(Node target : targets) {
            log.trace("Rules generation on: ");
            log.trace(target.toString());
            if (!this.specification.isSuitableTarget(target)) {
                log.warn("Not a suitable target for this fragment; skipping it");
            } else {
                log.trace("Processing ("+targetsCounter+" out of "+targets.size()+" available)");
                final Set<String> rulesFromThisTarget = generateRulesToTargetNode(xpathFragment, target);

                long target_new_rules = countNewRules(rules, rulesFromThisTarget);
                sampleNewRules.addAndGet(target_new_rules);
                rules.addAll(rulesFromThisTarget);
            }

            targetsCounter.incrementAndGet();  
        }

        samplesCounter.incrementAndGet();

        this.rulesCache.setRules(page, rules);
        return rules;
    }

    static final private Set<String> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    public long countNewRules(final Set<String> knownRules, final Set<String> newRules) {
        /* log generated rules details found vs ( new and old ) */
        final long num_rules = newRules.size();
        log.trace("generated "+num_rules+" rule(s) from this node");
        // count rules already known
        final long new_rules = newRules.stream().filter( r -> !knownRules.contains(r) ).count();
        final long old_rules = num_rules-new_rules;
        log.trace("generated "+new_rules+" new rule(s) from this node ("+old_rules+" old)");
        return new_rules;
    }

    private Set<String> generateRulesToTargetNode(XPathFragment fragment, final Node target) {
        final Set<String> rules = newConcurrentHashSet();

        log.trace("Looking for paths leading to pivot nodes from:");
        log.trace(target.toString());

        final BackwardTreeExplorer treeExplorer = new BackwardTreeExplorer(fragment);
        final Set<Path> found = treeExplorer.exploreFromTarget(target);
        log.trace("Navigations target-to-pivot found on this occ.: "+found.size());
        for (Path value2pivot : found) {
            if (value2pivot.isEmpty()) continue;
            final Set<String> xpath = value2pivot.getXPathExpression();
            if (xpath!=null && rules.addAll(xpath)) {
                log.trace("new rules generated: "+xpath);
            }
        }

        if (rules.isEmpty()) 
            log.trace("None extraction rule generated for target nodes: "+target);
        else {
            log.trace("Set of rules generated:");
            log.trace(rules.toString());
        }
        /* cache result */
        this.rulesCache.setRules(target, Collections.unmodifiableSet(rules));
        return rules;
    }

    public Set<String> getRules(final Node target) {
        Objects.requireNonNull(this.rulesCache,"Must provide a sample before generating the rules from a target node");
        if (this.getXPathFragment().getDocument()!=target.getOwnerDocument()) {
            throw new IllegalStateException("Can only query a document for which the rules have been just inferred");
        }
        final Set<String> cached = this.rulesCache.getRules(target);
        if (cached!=null) {
            log.trace("Rules for node "+target+" already cached");
            if (!cached.isEmpty())
                log.trace(cached.toString());
            else
                log.trace("none");
            return cached;
        }
        return generateRulesToTargetNode(xpathFragment, target);
    }

    public String toString() {
        return this.getClass().getSimpleName()
                + "&LessLess;"+this.specification+"&GreaterGreater;";
    }

}
