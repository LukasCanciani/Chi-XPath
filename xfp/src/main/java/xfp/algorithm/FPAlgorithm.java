package xfp.algorithm;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import static xfp.generation.XPathFragmentFactory.*;
import static xfp.fixpoint.PageClass.emptyPageClass;

import java.net.URI;
import java.util.*;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.extraction.ExtractionRule;
import xfp.extraction.ParallelExtractor;
import xfp.fixpoint.FPGenerator;
import xfp.fixpoint.PageClass;
import xfp.fixpoint.RuleFactory;
import xfp.fixpoint.RuleInference;
import xfp.generation.XPathFragment;
import xfp.model.ExtractedVector;
import xfp.model.Webpage;

public class FPAlgorithm<T> {
    
	static final private HypertextualLogger log = getLogger();
	    
	static final private FPAlgorithm<String> DFP = new FPAlgorithm<String>(DFP_FRAGMENT);

	static final private FPAlgorithm<URI>    NFP = new FPAlgorithm<URI>(NFP_FRAGMENT);

	static public FPAlgorithm<String> dfp() {
		return DFP;
	}

	static public FPAlgorithm<URI> nfp() {
		return NFP;
	}

	private Map<Set<Webpage>, PageClass<T>> cache;

    private RuleFactory<T> ruleFactory;

    private XPathFragment fragment;
	
    public FPAlgorithm(XPathFragment fragment)	{
        this.fragment = fragment;
        this.ruleFactory = fragment.getRuleFactory();
		this.cache = Collections.synchronizedMap(new WeakHashMap<>());
	}
	
	/** It is assumed that every Page in the sample belongs to the same page class */
	public PageClass<T> computeFixedPoints(Set<Webpage> sample) {
	    if (this.cache.containsKey(sample)) {
	        log.trace("Result cached:");
	        final PageClass<T> result = this.cache.get(sample);
	        log.trace(result.getVariant());
	        log.trace(result.getConstant());
	        return result;
	    }
	    
		log.trace("There are "+sample.size()+" available sample(s)");
		log.trace(sample);
		log.trace("<BR/>");
		
		final Set<String> rules = inferRules(sample);		
		
		if (!rules.isEmpty()) {
		    log.trace("Extraction rules generated ("+rules.size()+"): ");
		    log.trace(rules);

            final ParallelExtractor<T> extractor = new ParallelExtractor<>(this.ruleFactory);
		    
		    final List<ExtractedVector<T>> extracted = extractor.extract(sample, rules);
		    
		    final Set<ExtractedVector<T>> vectors = groupRulesByVector(extracted);
            
            log.trace(vectors.size()+" vector(s) extracted.");
            
            final FPGenerator<T> fpg = new FPGenerator<T>(fragment);
		    fpg.generate(vectors);
            
		    logFixedPoints(fpg);
		    
		    final PageClass<T> pc = new PageClass<>(sample, fpg.getVariant(), fpg.getConstant());
		    this.cache.put(sample,pc);
		    return pc;
		    
		} else {
		    log.trace("No extraction rules generated in the fragment");
		    log.trace(this.fragment);
		    this.cache.put(sample, emptyPageClass(sample));
            return emptyPageClass(sample);
		}
	}

    public Set<ExtractedVector<T>> groupRulesByVector(final List<ExtractedVector<T>> extracted) {
        /* de-duplicate extracted vectors by saving */
        /* for each the generating extraction rules */
        final Map<ExtractedVector<T>, List<ExtractionRule<T>>> rulesGroupedByVector =  new HashMap<>();
        for(ExtractedVector<T> ev : extracted) {
            List<ExtractionRule<T>> extracting = rulesGroupedByVector.get(ev);
            if (extracting==null) {
                extracting = new LinkedList<>();
                rulesGroupedByVector.put(ev, extracting);
            }
            extracting.add(ev.getExtractionRule());            
        }
        /* save and merge all extraction rules */
        rulesGroupedByVector.forEach( (vector, rules) -> 
                    vector.addGeneratingRules(rules)
                );
        return rulesGroupedByVector.keySet();
    }

	private Set<String> inferRules(Set<Webpage> samples) {
		log.newPage("Fixed point rules generation.");
		log.trace(this.fragment);

		final RuleInference ruleInference = new RuleInference(this.fragment);
		final Set<String> rules = ruleInference.inferRules(samples);

		log.trace("Generated rules: ");
		log.trace(rules);
		log.endPage();
		return rules;
	}
	
    private void logFixedPoints(FPGenerator<T> fpg) {
        log.newPage("Summary of fixed points ("+fpg.getVariant().size()+" v.+"+fpg.getConstant().size()+" c.)");
        log.trace("<BR/><HR/><EM>Summary of fixed points</EM><HR/>");
        if (fpg.getVariant().size()>0) {
            log.trace("List of <EM>variant</EM> fixed point(s) found (" + fpg.getVariant().size() + "):");
            log.trace(fpg.getVariant());
        } else log.trace("<EM>No variant fixed point found</EM>");

        log.trace("<BR/>");

        if (fpg.getConstant().size()>0) {
            log.trace("List of <EM>constant</EM> fixed point(s) found (" + fpg.getConstant().size() + "):");
            log.trace(fpg.getConstant());
        } else log.trace("<EM>None costant fixed point found</EM>");
        
        if (fpg.getExcluded().size()>0) {
            log.trace("<BR/>");
            log.page("Excluded vectors (" + fpg.getExcluded().size() + ")",fpg.getExcluded());
        }

        log.endPage();
    }


}
