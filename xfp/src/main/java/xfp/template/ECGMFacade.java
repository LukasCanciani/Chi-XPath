package xfp.template;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import it.uniroma3.ecgm.ECGM;
import it.uniroma3.ecgm.LFEQ;
import it.uniroma3.ecgm.lfeqrepository.LFEQRepository;
import it.uniroma3.hlog.HypertextualLogger;
import it.uniroma3.preferences.Constant;
import it.uniroma3.token.Sample;
import it.uniroma3.token.Samples;
import it.uniroma3.token.dom.DOMToken;
import it.uniroma3.token.dom.node.DOMNode;
import it.uniroma3.token.dom.node.DOMNodeFactory;
import xfp.model.Webpage;
import xfp.util.XFPConfig;
import it.uniroma3.ecgm.Constants;
import static it.uniroma3.ecgm.Constants.defaultConfiguration;

import java.util.*;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;

public class ECGMFacade  {

	static final private HypertextualLogger log = getLogger();

	final private Configuration prefs;

	private LFEQRepository binary;

	private List<Webpage> samples = null;

	public ECGMFacade() {
		this.prefs   = initConfig();
	}

	private Configuration initConfig() {
		/* weir-specific ECGM configuration overrides generic ECGM defaults */		
		CompositeConfiguration ecgmConf = new CompositeConfiguration();
		ecgmConf.addConfiguration(XFPConfig.getConfiguration());
		ecgmConf.addConfiguration(defaultConfiguration()); // ECGM defaults
		return ecgmConf;
	}
	
	public void analyze(List<Webpage> samples) {
		this.samples = samples;
		this.binary  = createBinaryLFEQRepository();
	}

	public List<DOMToken> getOccurrencesOfBinaryLFEQs() { // extensional
		return getAllOccurrences(binary.getLFEQs());
	}

	public List<DOMToken> getTokensOfBinaryLFEQs() {      // intensional
		return getAllTokens(binary.getLFEQs());
	}

	private List<DOMToken> getAllOccurrences(Collection<LFEQ> lfeqs) {
		final List<DOMToken> result = new LinkedList<>();
		for(LFEQ lfeq : lfeqs) {
			result.addAll(lfeq.getAllTokenOccurrences());
		}
		return result;
	}

	private List<DOMToken> getAllTokens(Collection<LFEQ> lfeqs) { // intensional
		List<DOMToken> result = new LinkedList<>();
		for(LFEQ lfeq : lfeqs) {
			result.addAll(Arrays.asList(lfeq.getTokens()));
		}
		return result;
	}

	private LFEQRepository createBinaryLFEQRepository() {
		logPrefs(this.prefs);
		log.newPage("ECGM analysis");

		final LFEQRepository repository = new LFEQRepository(Collections.<LFEQ>emptySet());

		final Samples ecgmSamples = makeECGMSamples();

		/* to deal with old LFEQ comparators executed by new java versions. */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		final ECGM ecgm = new ECGM(prefs, ecgmSamples);

		for (LFEQ lfeq : ecgm.inferLFEQs()) {
			if (lfeq.isBinary()) {
				repository.addLFEQ(lfeq);
			} else {
				log.trace("Discarding non-binary LFEQ:",lfeq);
			}
		}

		log.endPage();
		return repository;
	}

	private Samples makeECGMSamples() {
		final List<Sample> ecgmSamples = new ArrayList<>(this.samples.size());
		final Configuration config = this.prefs; // used to configure the tokenization
		for(Webpage page : this.samples) {
			final DOMNodeFactory document = (DOMNodeFactory) page.getDocument();
			ecgmSamples.add(new Sample(config, document));
		}
		return new Samples(config, ecgmSamples.toArray(new Sample[0]));
	}

	public List<DOMNode> getOccurrences(DOMToken intensional) { // intensional -> extensional
		final LFEQ lfeq = this.binary.getLFEQByIntensionalToken(intensional);
		if (lfeq==null) 
			throw new IllegalArgumentException(intensional+"\n is not a known LFEQ Token");
		final int tokenIndex = lfeq.getTokenIndex(intensional);
		final List<List<DOMNode>> occurrencesByDocument = this.binary.getOccurrences(lfeq, tokenIndex);
		return flat(occurrencesByDocument);
	}

	private List<DOMNode> flat(List<List<DOMNode>> listOfLists) {
		final List<DOMNode> flat = new ArrayList<>();
		for (List<DOMNode> list : listOfLists) {
			flat.addAll(list);
		}
		return flat;
	}

	private void logPrefs(Configuration config) {
		log.newPage("ECGM configuration");
		logPrefs("ECGM Analysis preferences", config, Constants.values());
		logPrefs("Tokenization preferences", config, it.uniroma3.token.Constants.values());
		log.endPage();
	}

	private void logPrefs(String msg, Configuration config, Constant[] keys) {
		log.trace("<br/><i>"+msg+"</i>:");
		for(Constant constant : keys) {
			final String   key   = constant.key();
			final String[] values = config.getStringArray(key);
			log.trace(key+"="+Arrays.toString(values));		
		}
	}

}