package xfp.model;

import static it.uniroma3.hlog.HypertextualUtils.linkTo;
import static xfp.util.Constants.DATASET_PATH;
import static xfp.util.Constants.EXPERIMENTS_PATH;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.util.XFPConfig;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * A class to model all the resources needed to run a complete 
 * experiment on a {@link Dataset} over a single {@link Domain} 
 * including several {@link Website}s, with the golden information 
 * needed to evaluate the results.
 */
public class Experiment implements Serializable {
	
	static final private long serialVersionUID = -7618991188797384863L;

	static final private HypertextualLogger log = HypertextualLogger.getLogger();
	
	/* dataset: swde vs weir   */
	/* domain:  book, auto ... */	
	/* site: amazon, yahoo ... */
	static final private String SITE    = "site";    

	static final private String CSV = ".csv";

	static final private String ID2URL_INDEX_NAME = "_id2name.txt";
	
	static private Experiment instance = null; /* singleton Experiment instance */
	
	static public synchronized Experiment makeExperiment(String datasetName, String domainName) {
		instance = new Experiment(datasetName, domainName);
		return instance;
	}

	static public synchronized Experiment getInstance() {
		if (instance!=null) return instance;
		throw new IllegalStateException("Experiment not ready yet!");
	}

	private Configuration configuration; // experiment's configuration

	private Domain domain;             /* the list of input websites   */
	
	/**
	 * 
	 * @param datasetName - e.g., swde/weir
	 * @param domainName  - e.g., nbaplayer
	 */
	private Experiment(String datasetName, String domainName) {
		this.domain = new Domain(domainName);
		final Dataset dataset = new Dataset(datasetName);
		dataset.addDomain(this.domain);
		this.domain.setDataset(dataset);
	}

	public File getExperimentSpecificationFolder() {
		return new File(XFPConfig.getString(EXPERIMENTS_PATH));
	}
	
	public File getExperimentSpecificationFile() {
		// e.g., /
		final String dataset = this.getDataset().getName();
		final String vertical = this.getDomain().getName();
		System.out.println(getExperimentSpecificationFolder());
		return new File(getExperimentSpecificationFolder(), dataset+"-"+vertical + ".xml") ;
	}
	
	/**
	 * Load the configuration of the experiment to run,
	 * @throws ConfigurationException
	 */
	public void load() throws ConfigurationException {	
		final File file = resolveExperimentSpecificationFile();

		this.configuration = new XMLConfiguration(file);
		
		// Load input websites associated with this experiment
		final DomainLoader loader = new DomainLoader(this);
		loader.loadWebsites(this.getWebsiteList());
	}

	private File resolveExperimentSpecificationFile() {
		final File file = this.getExperimentSpecificationFile();	

		/* load experiment specification */
		if (!file.exists()) {
			throw new RuntimeException("Experiment specification file not found: " + file.getAbsolutePath());
		}
		log.info("specification file resolved as: " + linkTo(file));
		return file;
	}
	
	public Domain getDomain() {
		return this.domain;
	}

	public Dataset getDataset() {
		return this.getDomain().getDataset();
	}

	public List<Website> getWebsites() {
		return this.getDomain().getSites();
	}
	

	/* --- */
	/**
	 * @return the folder containing all available datasets
	 */
	public File getDatasetsRootFolder() {
		// e.g.,
		return new File(XFPConfig.getString(DATASET_PATH));
	}
	
	/**
	 * @param domain
	 * @return the folder containing the resources associated with a domain
	 */
	public File getDomainFolder() {
		// e.g., dataset/swde/nbaplayer
		final String datasetName = getDomain().getDataset().getName();
		final File datasetFolder = new File(getDatasetsRootFolder(), datasetName);
		final String domainName = getDomain().getName();
		final File domainFolder = new File(datasetFolder, domainName);
		return domainFolder;
	}

	/**
	 * @param sitename  - name of a site in the domain 
	 *                    covered by this experiment
	 * @return the folder containing the resources associated with the site
	 */
	public File getWebsiteFolder(String sitename) {
		// e.g., dataset/swde/nbaplayer/nbaplayer-espn/
		return new File(getDomainFolder(), sitename);
	}	
	
	/**
	 * @param sitename  - name of a site in the domain 
	 *                    covered by this experiment
	 * @return the file containing the index by id of the URL of the 
	 *         HTML pages from the site passed as a parameter.
	 */
	public File getWebsitePageIndex(String sitename) {
		// e.g., dataset/swde/nbaplayer/nbaplayer-espn/_id2name.txt
		return new File(getWebsiteFolder(sitename), ID2URL_INDEX_NAME);
	}

	public File getWebsiteGoldenCSVFile(Website website) {
		if (this.getDomain()!=website.getDomain())
			throw new IllegalArgumentException("Can query only sites in the same domain!");
		final File domainFolder = getDomainFolder();
		final File siteFolder = new File(domainFolder, website.getName());			
		final File csvFile =  new File(siteFolder,website.getName() + CSV);
		return csvFile;
	}

	public List<String> getWebsiteList() {
		return new ArrayList<>(Arrays.asList(this.configuration.getStringArray(SITE)));
	}

	@Override
	public String toString() {
		return getDataset().getName()+" "+getDomain().getName();
	}

}
