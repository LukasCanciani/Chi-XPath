package xfp.fixpoint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.generation.XPathFragment;
import xfp.model.ExtractedVector;
import xfp.model.Value;

public class FPGenerator<T> {

    static final private HypertextualLogger log = HypertextualLogger.getLogger();

    private Set<FixedPoint<T>> variants;

    private Set<FixedPoint<T>> constants;

    private Set<FixedPoint<T>> excluded;

    private XPathFragment fragment;
    
    public FPGenerator(XPathFragment fragment) {
        this.fragment  = fragment;
        this.variants  = newConcurrentHashSet();
        this.constants = newConcurrentHashSet();
        this.excluded  = newConcurrentHashSet();
    }

    private Set<FixedPoint<T>> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    public void generate(Set<ExtractedVector<T>> vectors) {
        log.newPage("Looking for fixed points ("+vectors.size()+" to evaluate)");

        vectors.stream().forEach(vector -> {
            log.trace("<HR/>");
            log.trace("Evaluating extracted vector");
            log.trace(() -> vector);
            
            if (!this.mayBeAsuitableFixedPoint(vector)) {
                log.trace("this vector cannot be a fixed point of fragment "+this.fragment);
                return;
            }
            
            if (vector.isSingleton()) {
                log.trace("this vector is a singleton excluded from the evaluation");
                return;
            }
            
            if (vector.isConstant()) {
                log.trace("it is a constant vector");
            }
                        
            log.newPage("Evaluating as fixed point");
            log.trace(() -> vector);

            boolean fixedSoFar = true;

            /* all the rules always generated so far are in this local variable */
            final Set<String> intersection = new HashSet<>(vector.getGeneratingRulesXPath());
            
            final Iterator<List<Value<T>>> it = vector.iterator();

            while (fixedSoFar && it.hasNext()) {
                final List<Value<T>> values = it.next();
                
                if (values.isEmpty())
                    continue;

                log.trace(() -> "generating rules from extracted values: " + values);
                
                final Set<String> generated = new HashSet<>();
                for (List<Value<T>> extractedFromPage : vector) {
                    for (Value<T> value : extractedFromPage) {
                    final Node target = value.getNode();
                        if (!this.fragment.isSuitableTarget(target)) {
                            log.warn(target+" is not a suitable target for this fragment");
                            log.trace("skipping this extracted value");
                        } else {
                            final RuleInference inference = new RuleInference(fragment);
                            inference.setInferenceSample(vector.getPages());
                            final Set<String> generatedFromTarget = inference.generateRules(target);
                            generated.addAll(generatedFromTarget);
                        }
                    }
                }

                log.trace("Rules generated from extracted nodes:");
                log.trace(() -> generated);
                intersection.retainAll(generated);
                if (intersection.isEmpty()) {
                    log.trace("That's not a fixed point.");
                    log.trace("The set of generated rules does not contain any of the generating rules");
                    log.trace(() -> vector.getGeneratingRules());
                    fixedSoFar = false;
                } else {
                    log.trace("Generating rule(s) there: still looks like a fixed point...");
                    log.trace(() -> intersection);
                }
            }
            log.endPage();
            
            final FixedPoint<T> fp = new FixedPoint<>(vector,intersection);

            if (fixedSoFar) {
                log.trace("Found a fixed point.");
                if (vector.isConstant()) {
                    log.trace("It is a <EM>constant</EM> fixed point.");
                    this.constants.add(fp);
                }
                else {
                    log.trace("It is a <EM>variable</EM> fixed point.");
                    this.variants.add(fp); // fixed-points                    
                }                    
            } else {
                this.excluded.add(fp);
                log.trace("Not a fixed point.");
            }
        });
        log.endPage();
    }

    private boolean mayBeAsuitableFixedPoint(ExtractedVector<T> vector) {
       /* return  this.fragment.coverMultiValued() || // check whether the fragment can cover this vector 
                !vector.getValues().stream().filter(l -> ( l.size()>1) ).findAny().isPresent();*/
    	return true;
    }

    public Document getDocumentOf(final List<Value<T>> values) {
        return values.get(0).getPage().getDocument();
    }
    
    public Set<FixedPoint<T>> getAllFixedPoint() {
        final Set<FixedPoint<T>> all = new HashSet<>();
        all.addAll(this.getVariant());
        all.addAll(this.getConstant());
        return all;
    }

    public Set<FixedPoint<T>> getConstant() {
        return this.constants;
    }
    
    public Set<FixedPoint<T>> getVariant() {
        return this.variants;
    }

    public Set<FixedPoint<T>> getExcluded() {
        return this.excluded;
    }

}
