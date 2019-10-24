package xfp.model;

import static it.uniroma3.dom.visitor.DOMVisitorListenerChain.chain;
import static it.uniroma3.ecgm.Constants.USE_XPATH_ROLES;
import static it.uniroma3.token.Constants.*;
import it.uniroma3.dom.visitor.DOMVisitor;
import it.uniroma3.dom.visitor.DOMVisitorListener;
import it.uniroma3.dom.visitor.DOMVisitorSkippingStrategies.SkippingByNameStrategy;
import it.uniroma3.ecgm.loader.XPathAnnotator;
import it.uniroma3.hlog.HypertextualLogger;
import it.uniroma3.token.dom.TagFactory;
import it.uniroma3.token.dom.TagFactoryBuilder;
import it.uniroma3.token.dom.node.DOMNodeFactory;
import it.uniroma3.token.loader.DOMLoader;
import it.uniroma3.token.loader.PCDATATokenizer;
import it.uniroma3.token.loader.Skipper;
import xfp.util.XFPConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
/**
 * 
 * Load customized DOM representations of HTML documents
 * that supports a tokenization process useful for finding
 * template nodes, eventually by annotating each node with a 
 * disambiguation positional XPath rule that leads to the
 * node itself.
 */ 
class WebFetcher {

	@SuppressWarnings("unused")
	static final private HypertextualLogger log = HypertextualLogger.getLogger();

	static private WebFetcher instance;

	static public WebFetcher getInstance() {
		if (instance==null) {
			instance = new WebFetcher();
		}
		return instance;
	}

	final private Configuration prefs;

	final private Set<String> skipTrees;

	final private Set<String> skipTags;

	final private String separators;
	
	final private int maxPCDATAlength;

	private WebFetcher() {
		this.prefs = XFPConfig.getConfiguration();
		this.skipTags = new HashSet<>(XFPConfig.getList(IGNORE_TAGS));
		this.skipTrees = new HashSet<>(XFPConfig.getList(IGNORE_TREES));
		this.separators = XFPConfig.getString(SEPARATOR_CHARS);
		this.maxPCDATAlength = XFPConfig.getInteger(MAX_PCDATA_LENGTH);
	}

	public DOMNodeFactory fetchDocument(Webpage page) {
		try (final InputStreamReader reader = new InputStreamReader(page.getURI().toURL().openStream())) {			
			/* load a customized DOM tree of the HTML page to support
			 * a tokenization functional to our template analysis */
			return loadAndPreProcessDocument(reader);
		} catch (SAXException | IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private DOMNodeFactory loadAndPreProcessDocument(final InputStreamReader reader) 
			throws SAXException, IOException {
		final DOMLoader loader = new DOMLoader();			
		final DOMNodeFactory document = loader.loadDOM(reader);
		// N.B. xerces DOM impl. requires clients to serialize 
		//      accesses because of lazy inits in the DOM trees
		// https://xerces.apache.org/xerces2-j/faq-dom.html#faq-1
		synchronized (document) {
			return preprocess(document);
		}
	}

	/**
	 * This method digests the input DOM tree by means of two visits during
	 * which it performs a tokenization functional to a following analysis
	 * of the underlying template. This analysis is needed, for example, to 
	 * generate relative extraction rules.
	 * <BR/>
	 * It makes uses of several {@link DOMVisitorListener}s:
	 * <ul>
	 * <li> {@link PCDATATokenizer}: split textual DOM leaf nodes containing 
	 *                 whole PCDATA into several textual DOM leaves with a 
	 *                 token (word) each;
	 *                 
	 * <li>{@link XPathAnnotator}: annotate, if required, each node with 
	 * 				   its positional XPath as a 'role' to distinguish 
	 * 				   several occurrences of the same token;
	 * 
	 * <li>{@link Skipper}: skip tags and entire DOM subtrees according to
	 * 				   a {@link SkippingStrategy}. Note that it also removes 
	 * 				   DOM comment nodes.
	 * </ul>
	 * @param document - the DOM document to preprocess
	 * @return a preprocessed version of the input DOM document
	 */
	// TODO design an uniform tokenization strategy throughout the project:
	// generalize xfp.extraction.wrapper.pcdata.InvariantsFinder.tokenize(String)
	// into a TokenizationStrategy based on a regexp (to inject also into
	// it.uniroma3.token.loader.PCDATATokenizer)
	private DOMNodeFactory preprocess(final DOMNodeFactory document) {
		// split pcdata
		if (this.separators!=null) {
			final PCDATATokenizer splitter = new PCDATATokenizer(this.separators);
			splitter.splitTextNodesOfDocument(document); // first visit
		}
		DOMVisitorListener listener = null;

		// annotate with xpaths; skip unwanted input portions; skip comments
		if (xpathRolesEnabled())
			listener = chain(new XPathAnnotator(), new Skipper());
		else
			listener = new Skipper();

		/* N.B.: ECGM module wouldn't process DOM comment nodes, 
		 * however, they separates otherwise indistinguishable texts
		 */
		final SkippingByNameStrategy strategy = new SkippingByNameStrategy(skipTags, skipTrees) {
			@Override
			public boolean isTextToSkip(Text text) {
				// too long texts cannot be redundant, and cannot be pivots
				return text.getNodeValue().length()>maxPCDATAlength;
			}
		};

		final DOMVisitor visitor = new DOMVisitor(listener,strategy);
		listener.setDOMVisitor(visitor);
		
		/* Inject into the doc the right tag factory for tokens */
		final TagFactory factory = TagFactoryBuilder.getFactory(prefs); 
		document.setTagFactory(factory);

		visitor.visit(document); // second and final visit

		return document;
	}

	private boolean xpathRolesEnabled() {
		return this.prefs.getBoolean(USE_XPATH_ROLES.key());
	}

}
