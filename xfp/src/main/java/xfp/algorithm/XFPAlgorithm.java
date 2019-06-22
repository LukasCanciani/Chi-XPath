package xfp.algorithm;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import static java.util.stream.Collectors.toCollection;
import static xfp.algorithm.FPAlgorithm.dfp;
import static xfp.algorithm.FPAlgorithm.nfp;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.eval.Evaluation;
import xfp.fixpoint.FixedPoint;
import xfp.fixpoint.Lattice;
import xfp.fixpoint.PageClass;
import xfp.io.OutputHandlerFactory;
import xfp.model.ExtractedVector;
import xfp.model.Value;
import xfp.model.Webpage;
import xfp.model.Website;
import xfp.util.Constants;
import xfp.util.XFPConfig;

public class XFPAlgorithm {

	static final private HypertextualLogger log = getLogger();

	static final private int DATA_THRESHOLD = 3;

	static private boolean STORE_LINK_FP_ENABLED = XFPConfig.getBoolean(Constants.LINK_FIXEDPOINTS_STORING_ENABLED);

	static private boolean STORE_DATA_FP_ENABLED = XFPConfig.getBoolean(Constants.DATA_FIXEDPOINTS_STORING_ENABLED);

	static private boolean EVALUATE_DATA_FP_ENABLED = XFPConfig.getBoolean(Constants.DATA_FIXPOINTS_EVALUATION_ENABLED);

	private Website site;

	private Set<Set<Webpage>> alreadyProcessed;

	private Evaluation evaluation;

	public XFPAlgorithm(Website site, File websitedir) throws Exception	{
		this.site = site;
		this.alreadyProcessed = new HashSet<>();
		this.evaluation = new Evaluation(websitedir, site);
		if (!this.evaluation.isGoldenAvailable()) EVALUATE_DATA_FP_ENABLED = false;
	}

	/** new version of the algorithm: 
	 * lfp (ap) -> lc
	 *   for each l : lc
	 *     dfp -> data
	 *     	if (data > t)
	 *     		// this is a list (e.g. links to detail pages)
	 *      else
	 *        // use each link as new access page
	 * @throws Exception 
	 * */
	public void xfp(Set<Webpage> sample) throws Exception	{
		if (!this.alreadyProcessed.add(sample)) {
			log.trace("This sample has been already processed by XFP "+sample);
			return;
		}
		log.trace();
		log.trace("Input samples: " +sample);
		log.newPage("Applying NFP");

		//final Set<FixedPoint<URI>> navFixedPoint = nfp().computeFixedPoints(sample);
		final PageClass<URI> navFixedPoint = nfp().computeFixedPoints(sample);
		storeNavFixedPoints(navFixedPoint);
		final Set<Lattice<URI>> lattices = Lattice.create(navFixedPoint.getVariant());
		final NavigableSet<FixedPoint<URI>> lcs = // -> RequestCollection
				lattices.stream()
				.map( l -> l.top() ) // start crawling the lattices from the top
				.collect(Collectors.toCollection(TreeSet::new)); 

		if (lcs.isEmpty())
			log.trace("None link collection left to crawl");

		while (!lcs.isEmpty())	{ 
			log.trace("<HR><BR/>");

			log.trace("currently  "+lcs.size()+" link collection(s) are still available");
			final FixedPoint<URI> lc = ( lcs.pollLast() ); /* <-- get and remove next lc */
			log.trace(lc.getExtracted());
			log.newPage("Applying DFP on this link collection");
			log.trace("Link collection extracted by "+lc.getExtractionRule());

			final Set<Webpage> pages = download(getLinks(lc.getExtracted()));

			//final Set<FixedPoint<String>> data = dfp().computeFixedPoints(pages);
			final PageClass<String> data = dfp().computeFixedPoints(pages);
			storeDataFixedPoints(data);
			evaluateDataFixedPoints(data);

			if (isValidPageClass(data))	{
				log.trace("That's a valid page class (current threshold="+DATA_THRESHOLD+" f.p.)! Go deeper.");
				xfp(pages);
				log.endPage();
				log.trace("That was a <EM>valid</EM> page class.");
				log.newPage(data.getVariant().size()+ " fixed data fixed points found.");
				log.trace(data.getVariant());
				log.endPage();
			} else {
				final Set<FixedPoint<URI>> lc_glb = lc.getLattice().getGLBs(lc); // refine lc
				lcs.addAll(lc_glb);
				log.endPage();
				log.trace("That was an <EM>invalid</EM> page class");
				log.page("replacing link collection with its g.l.b. (size: "+lc_glb.size()+").",lc_glb);
			}
			log.trace("<BR/>");
		}

		log.endPage();
	}

	public Map<Set<String>, int[]> xfpNav(Set<Webpage> sample) throws Exception {

		Map<Set<String>, int[]>  fp = new HashMap<>();

		if (!this.alreadyProcessed.add(sample)) {
			log.trace("This sample has been already processed by XFP "+sample);
			return fp;
		}
		log.trace();
		log.trace("Input samples: " +sample);
		log.newPage("Applying NFP");

		//final Set<FixedPoint<URI>> navFixedPoint = nfp().computeFixedPoints(sample);
		final PageClass<URI> navFixedPoint = nfp().computeFixedPoints(sample);
		storeNavFixedPoints(navFixedPoint);
		

		Set<String> fpPages = new HashSet<>();
		for(Webpage wp : navFixedPoint.getPages()) {
			fpPages.add(wp.getName());
		}
		int[] fps = new int[2];
		fps[0] = navFixedPoint.getVariant().size();
		fps[1] = navFixedPoint.getConstant().size();
		fp.put(fpPages, fps);



		log.endPage();
		return fp;
	}

	public Set<FixedPoint<String>> xfpData(Set<Webpage> sample) throws Exception {
		Set<FixedPoint<String>> fp = new HashSet<>();
		//Set<String> rules = new HashSet<>();
		if (!this.alreadyProcessed.add(sample)) {
			log.trace("This sample has been already processed by XFP "+sample);
			return fp;
		}
		log.trace();
		log.trace("Input samples: " +sample);
		log.newPage("Applying DFP on this link collection");

		//final Set<FixedPoint<String>> data = dfp().computeFixedPoints(pages);
		final PageClass<String> data = dfp().computeFixedPointsData(sample);

		fp.addAll(data.getVariant());
		fp.addAll(data.getConstant());

		/*for(FixedPoint<String> constant : data.getConstant()) {
			rules.addAll(constant.getRules()); //Il problema è che cosi mi mette troppe xpath!!!! Anche se quasi non ho PF
		}
		for(FixedPoint<String> variant : data.getVariant()){
			rules.addAll(variant.getRules());
		}*/
		storeDataFixedPoints(data);
		evaluateDataFixedPoints(data);
		log.endPage();
		return fp;
	}

	private void storeNavFixedPoints(final PageClass<URI> navFixedPoint) throws Exception  {
		if (STORE_LINK_FP_ENABLED) OutputHandlerFactory.getNFPOutputHandler().storePageclass(navFixedPoint);
	}

	private void storeDataFixedPoints(final PageClass<String> data) throws Exception {
		if (STORE_DATA_FP_ENABLED) OutputHandlerFactory.getDFPOutputHandler().storePageclass(data); 
	}

	private void evaluateDataFixedPoints(final PageClass<String> data) throws Exception {
		if (EVALUATE_DATA_FP_ENABLED) evaluation.evaluateFixedPoints(data.getVariant()); 
	}

	/* TODO find a meaningful definition for this predicate */
	private boolean isValidPageClass(PageClass<String> data) {
		return ( data.getVariant().size()>DATA_THRESHOLD ) ;
	}

	private Collection<Value<URI>> getLinks(ExtractedVector<URI> extracted) {
		return extracted.getValues().stream()
				.flatMap( lv -> lv.stream() )
				.collect(toCollection(LinkedHashSet::new)); 
		// use an LinkedHashSet to preserve original link order
	}

	private Set<Webpage> download(Collection<Value<URI>> requests) {
		final Set<Webpage> accumulator = new LinkedHashSet<>(); // save order afap removing duplicates

		for (Value<URI> link : requests)	{
			final Webpage downloaded = this.site.getPage(Webpage.getLocalName(link.getValue()));
			if (downloaded==null)
				log.warn("Cannot download target page by URI: "+link);
			else {
				accumulator.add(downloaded);
				RedundancySeeker.instance.link(link.getPage(),downloaded);
			}
		}

		if (accumulator.size()!=requests.size()) {
			log.trace("Downloaded "+(requests.size()-accumulator.size())+" fewer pages than requested links (duplicates)");
		}
		return accumulator;
	}





}
