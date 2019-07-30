package xfp.fixpoint;


/*import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import static it.uniroma3.hlog.HypertextualUtils.linkTo;*/
import static xfp.hlog.LogUtil.streamOf;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.uniroma3.dom.visitor.DOMVisitor;
import it.uniroma3.dom.visitor.DOMVisitorListener;
import it.uniroma3.dom.visitor.DOMVisitorListenerAdapter;
//import it.uniroma3.hlog.HypertextualLogger;
import xfp.algorithm.RedundancySeeker;
import xfp.extraction.ExtractionRule;
import xfp.generation.*;
import xfp.model.Webpage;
import xfp.util.Constants;
import xfp.util.XFPConfig;

/**
 * This class is in charge of generating a set of {@link ExtractionRule}s, 
 * each pivoted on an invariant template token and targeting either a value or a link
 * to extract as set by an instance of {@link XPathFragmentFactory}.
 */
public class RuleInference {

	//static final private HypertextualLogger log = getLogger();

	static final private String GENERATED_RULES_USER_DATA_KEY = "GENERATED_RULES";

	static final public int MAX_EXTRACTION_SAMPLES = XFPConfig.getInteger(Constants.MAX_EXTRACTION_SAMPLES);

	static final public boolean REDUNDANCY_CHECK_ENABLED = XFPConfig.getBoolean(Constants.REDUNDANCY_CHECK_ENABLED);


	private List<Node> targets;     // suitable target nodes

	private List<Node> unsuitable;  // unsuitable target nodes

	private XPathFragment fragment;

	private XPathFragmentCache<Set<String>> cache;

	private RedundancySeeker seeker;

	public RuleInference(XPathFragment fragment) {
		this.fragment = fragment;
		this.targets = new LinkedList<>();
		this.unsuitable = new LinkedList<>();
		this.seeker = RedundancySeeker.getInstance();
		this.cache = null; // wait for the sample please...
	}

	public XPathFragment getXPathFragment() {
		return this.fragment;
	}

	public void setInferenceSample(Set<Webpage> sample) {
		// create a fragment+sample specific cache 
		this.cache = new XPathFragmentCache<>(this.fragment,sample);                
	}

	public Set<String> inferRules(Set<Webpage> sample) {
		if (sample.isEmpty()) return Collections.emptySet();

		this.setInferenceSample(sample);
	/*	log.trace("Input samples:");
		log.trace(() -> sample);
		log.newPage("Fragment pre-processing over the input sample"); */
		this.fragment.preprocess(sample);
	//	log.endPage();

	//	log.trace("<HR/>");
		final Set<String> rules = newConcurrentHashSet();

		final AtomicInteger samplesCounter = new AtomicInteger(1);
	//	log.newTable();

		for (Webpage page : sample) {
			// N.B. collapse sibling text nodes before inferring rules
			// N.B. as a  side-effect the DOM tree (text) nodes change
			page.normalize(); 

	/*		log.trace(() -> " rules generation over"
					+ " ("+samplesCounter+" out of "+sample.size()+" available)"
					+ " input sample(s) " +	linkTo(page.getURI()).withAnchor(page.getName()));*/
			this.selectTargetNodes(page);
	//		log.page(this.targets.size()    + " target(s) found",  targets);
	//		log.page(this.unsuitable.size() + " unsuitable node(s) found", unsuitable);

	//		log.trace("<BR/>");

	//		log.newPage(() -> "building all XPath steps over " + linkTo(page.getURI()).withAnchor(page.getName()));
			this.fragment.build(page);
	//		log.endPage();

	//		log.trace("<BR/>");

			final AtomicLong targetsCounter = new AtomicLong(1);
			final AtomicLong sampleNewRules = new AtomicLong(0);
			streamOf(targets).forEach(target -> {
	//			log.trace("Rules generation on: ");
	//			log.trace(() -> target);
				if (!this.fragment.isSuitableTarget(target)) {
	//				log.warn("Not a suitable target for this fragment; skipping it");
				} else {
	//				log.newPage(() -> "Processing ("+targetsCounter+" out of "+targets.size()+" available)");
					final Set<String> rulesFromThisTarget = generateRules(target);
	//				log.endPage();
					long target_new_rules = countNewRules(rules, rulesFromThisTarget);
					sampleNewRules.addAndGet(target_new_rules);
					rules.addAll(rulesFromThisTarget);
				}
	//			log.trace("<BR/>");

				targetsCounter.incrementAndGet();  
			});
	//		log.trace("<HR/>");
	//		log.trace("<BR/>");

			if (samplesCounter.get()>=MAX_EXTRACTION_SAMPLES || sampleNewRules.get()==0) {
	//			log.trace("Max number of samples for rule inference reached");
				break;
			}
			samplesCounter.incrementAndGet();
		}
	//	log.endTable();
		return rules;
	}
	public Set<String> setRules(Set<Webpage> sample, Set<String> Xrules) {
		if (sample.isEmpty()) return Collections.emptySet();

		
		final Set<String> rules = Xrules;

		
		return rules;
	}

	static final private Set<String> newConcurrentHashSet() {
		return ConcurrentHashMap.newKeySet();
	}

	public long countNewRules(final Set<String> knownRules, final Set<String> newRules) {
		// log generated rules details found vs ( new and old ) 
	
	//	final long num_rules = newRules.size();
	//	log.trace(() -> "generated "+num_rules+" rule(s) from this node");
		// count rules already known
		final long new_rules = newRules.stream().filter( r -> !knownRules.contains(r) ).count();
	//	final long old_rules = num_rules-new_rules;
	//	log.trace(() -> "generated "+new_rules+" new rule(s) from this node ("+old_rules+" old)");
		return new_rules;
	}

	public Set<String> generateRules(final Node target) {
		Objects.requireNonNull(this.cache,"Must provide a sample before generating the rules from a target node");
		final Set<String> cached = this.cache.get(target, GENERATED_RULES_USER_DATA_KEY);

		if (cached!=null) {
	/*		log.trace(() -> "Rules for node "+target+" already cached");
			if (!cached.isEmpty())
				log.trace(() -> cached);
			else
				log.trace("none");	*/
			return cached;
		}

		final Set<String> rules = newConcurrentHashSet();

	/*	log.trace(() -> "Looking for paths leading to pivot nodes from:");
		log.trace(() -> target);

		log.newPage();	*/
		final BackwardTreeExplorer treeExplorer = new BackwardTreeExplorer(this.fragment);
		final Set<Path> found = treeExplorer.exploreFromTarget(target);
	//	log.endPage(() -> "Navigations text-to-pivot found on this occ.: "+found.size());
		for (Path value2pivot : found) {
			if (value2pivot.isEmpty()) continue;
			final String xpath = value2pivot.getXPathExpression();
			if (xpath!=null && rules.add(xpath)) {
	//			log.trace(()-> "new rule generated: "+xpath);
			}
		}

	/*	log.trace("<HR/>");
		if (rules.isEmpty()) 
			log.trace("None extraction rule generated");
		else {
			log.trace("Set of rules generated:");
			log.trace(()->rules);
		}*/
		// cache result 
		this.cache.set(target, GENERATED_RULES_USER_DATA_KEY, Collections.unmodifiableSet(rules));
		return rules;
	}

	private void selectTargetNodes(Webpage page) {
	//	log.newPage(() -> "selecting target nodes");
		final DOMVisitorListener listener = new DOMVisitorListenerAdapter() {
			@Override
			public void startElement(Element element)  {
				checkSuitablenessAsTarget(element);
			}            
			@Override
			public void text(Text text)   {
				if (!REDUNDANCY_CHECK_ENABLED || isRedundant(page, text)) {
					checkSuitablenessAsTarget(text);
				} else {
		//			log.trace(() -> text+" discarded as not considered redundant");
				}
			}
			private void checkSuitablenessAsTarget(Node n) {
				if (fragment.isSuitableTarget(n)) 
					targets.add(n);
				else unsuitable.add(n);
			}
		};
		final DOMVisitor visitor = new DOMVisitor(listener);
		listener.setDOMVisitor(visitor);
		visitor.visit(page.getDocument());
	//	log.endPage();
	}

	private final boolean isRedundant(Webpage page, Text text) {
		return ( this.seeker.isRedundant(page, text) );
	}    

	public String toString() {
		return this.getClass().getSimpleName()
				+ "&LessLess;"+this.fragment+"&GreaterGreater;";
	}

}
