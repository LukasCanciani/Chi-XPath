package xfp;

import static it.uniroma3.hlog.HypertextualLogManager.loadConfiguration;
import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import static it.uniroma3.hlog.HypertextualUtils.linkTo;
import static it.uniroma3.hlog.Level.OFF;
import static xfp.generation.XPathFragmentFactory.DFP_FRAGMENT;
import static xfp.hlog.LogUtil.*;
import static xfp.util.Constants.FRAGMENT_LOGGING_ENABLED;
import static xfp.util.Constants.RULE_LOGGING_ENABLED;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.algorithm.XFPAlgorithm;
import xfp.fixpoint.FixedPoint;
import xfp.model.Experiment;
import xfp.model.Website;
import xfp.util.Constants;
import xfp.util.XFPConfig;

public class ExperimentRunner {

    static final private HypertextualLogger log = getLogger(); /* this *after* previous static block */
    
    static final public String XFP_CONFIG_FILENAME 	= "./xfp-config.properties";

    static final public String XFP_HLOG_PROPERTIES 	= "/xfp-hlog.properties";

    /* constructor using default XFP configuration files */
    public ExperimentRunner()	{
        this(XFP_CONFIG_FILENAME, XFP_HLOG_PROPERTIES);
    }

    public ExperimentRunner(String xfpConfigFilename, String xfpHlogConfigFilename)	{
        // now automatically loaded by hlog
        loadConfiguration(xfpHlogConfigFilename); 

        final URL configURL = ClassLoader.getSystemResource(xfpConfigFilename);	
        try {
            XFPConfig.load(configURL);
        } catch (ConfigurationException cfgEx) {
            log.error("Cannot read XFP configuration");
            log.trace(cfgEx);
            throw new RuntimeException("Cannot read XFP configuration: "+xfpConfigFilename,cfgEx);
        }
        log.info("system config file resolved as: " + linkTo(configURL));
    }

    public void run(String datasetName, String domainName, List<String> enabledSitesList) {
        try {
            log.newPage("Loading experiment on "+datasetName+" "+domainName);
            final Experiment experiment = Experiment.makeExperiment(datasetName, domainName);
            XFPConfig.getInstance().setCurrentExperiment(experiment);
            experiment.load(); // N.B. this loads all pages
            log.endPage();

            if (XFPConfig.getBoolean(Constants.PARALLEL_STREAMS_ENABLED)) {
                log.warn("Parallel streams enabled for XPath fragment and rules generation");
            }
            
            // FOR each website - run a separate experiment 
            for(Website site : experiment.getDomain().getSites()) {
                if (!isEnabled(enabledSitesList, site)) {
                    log.warn("Skipping "+site);
                    continue;
                }

                configureLoggingLevels();              
                executeExperimentOn(experiment.getWebsiteFolder(site.getName()),site);
            }
            log.trace("\n");
        } catch (Exception e) {
            /* print & log stack-trace; before letting it go */
            log.error(e.getMessage());
            log.throwing(e);
            log.trace("\n");
            log.flushHandlers();
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    }
    
    public Map<Set<String>, int[]> runNav(String datasetName, String domainName, List<String> enabledSitesList, Map<String, String> id2name) {
    	Map<Set<String>, int[]> fp = null;
    	try {
            log.newPage("Loading experiment on "+datasetName+" "+domainName);
            final Experiment experiment = Experiment.makeExperiment(datasetName, domainName);
            XFPConfig.getInstance().setCurrentExperiment(experiment);
            experiment.loadChi(id2name); // N.B. this loads all pages
            log.endPage();

            if (XFPConfig.getBoolean(Constants.PARALLEL_STREAMS_ENABLED)) {
                log.warn("Parallel streams enabled for XPath fragment and rules generation");
            }
            
            // FOR each website - run a separate experiment 
            for(Website site : experiment.getDomain().getSites()) {
                if (!isEnabled(enabledSitesList, site)) {
                    log.warn("Skipping "+site);
                    continue;
                }

                configureLoggingLevels();              
                fp=executeExperimentNav(experiment.getWebsiteFolder(site.getName()),site);
            }
            log.trace("\n");
        } catch (Exception e) {
            /* print & log stack-trace; before letting it go */
            log.error(e.getMessage());
            log.throwing(e);
            log.trace("\n");
            log.flushHandlers();
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    	return fp;
	}
    
    public Set<FixedPoint<String>> runData(String datasetName, String domainName, List<String> enabledSitesList, Map<String, String> id2name) {
    	Set<FixedPoint<String>> fp= null;
    	try {
            log.newPage("Loading experiment on "+datasetName+" "+domainName);
            final Experiment experiment = Experiment.makeExperiment(datasetName, domainName);
            XFPConfig.getInstance().setCurrentExperiment(experiment);
            experiment.loadChi(id2name); // N.B. this loads all pages
            log.endPage();

            if (XFPConfig.getBoolean(Constants.PARALLEL_STREAMS_ENABLED)) {
                log.warn("Parallel streams enabled for XPath fragment and rules generation");
            }
            
            // FOR each website - run a separate experiment 
            for(Website site : experiment.getDomain().getSites()) {
                if (!isEnabled(enabledSitesList, site)) {
                    log.warn("Skipping "+site);
                    continue;
                }

                configureLoggingLevels();              
                fp=executeExperimentOnData(experiment.getWebsiteFolder(site.getName()),site);
            }
            log.trace("\n");
        } catch (Exception e) {
            /* print & log stack-trace; before letting it go */
            log.error(e.getMessage());
            log.throwing(e);
            log.trace("\n");
            log.flushHandlers();
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        }
    	return fp;
	}

    private Set<FixedPoint<String>> executeExperimentOnData(File websitedir, Website site) throws Exception {
    	Set<FixedPoint<String>> fp;
    	// trigger recursive executions of xfp algorithm beginning from the access page(s)
        log.newPage("Running XFP on website: "+site);
        final XFPAlgorithm algorithm = new XFPAlgorithm(site, websitedir);
        
        /* N.B. UP-FRONT FRAGMENT GENERATION IS NOT SUPPORTED 
         *      WITH PAGECLASS-SPECIFIC TEMPLATE ANALYSIS     */
        /* upfrontXPathFragmentGeneration(site); */
        fp = algorithm.xfpData(site.getAccessPages());
        
        log.trace("\n"); // flush hyper-logs
        log.endPage();
        return fp;
	}

	private Map<Set<String>, int[]> executeExperimentNav(File websitedir, Website site) throws Exception {
    	Map<Set<String>, int[]> fp = null;
    	// trigger recursive executions of xfp algorithm beginning from the access page(s)
        log.newPage("Running XFP on website: "+site);
        final XFPAlgorithm algorithm = new XFPAlgorithm(site, websitedir);
        
        /* N.B. UP-FRONT FRAGMENT GENERATION IS NOT SUPPORTED 
         *      WITH PAGECLASS-SPECIFIC TEMPLATE ANALYSIS     */
        /* upfrontXPathFragmentGeneration(site); */
        fp = algorithm.xfpNav(site.getAccessPages());
        
        log.trace("\n"); // flush hyper-logs
        log.endPage();
        return fp;
	}

	protected void configureLoggingLevels() {
        if (!XFPConfig.getBoolean(FRAGMENT_LOGGING_ENABLED)) {                
            setFragmentLoggingLevel(OFF);
        }              
        if (!XFPConfig.getBoolean(RULE_LOGGING_ENABLED)) {                
            setRuleLoggingLevel(OFF);
        }
    }

    protected void executeExperimentOn(File websitedir, Website site) throws Exception {
        // trigger recursive executions of xfp algorithm beginning from the access page(s)
        log.newPage("Running XFP on website: "+site);
        final XFPAlgorithm algorithm = new XFPAlgorithm(site, websitedir);
        
        /* N.B. UP-FRONT FRAGMENT GENERATION IS NOT SUPPORTED 
         *      WITH PAGECLASS-SPECIFIC TEMPLATE ANALYSIS     */
        /* upfrontXPathFragmentGeneration(site); */
        algorithm.xfp(site.getAccessPages());
        
        log.trace("\n"); // flush hyper-logs
        log.endPage();
    }

    protected void upfrontXPathFragmentGeneration(Website site) {
        log.newPage("Computing XPath fragment up-front");
        /* hlog only offers a basic and very limited multi-thread support */
        /* do not open / close logging pages during parallel operations.  */
        /* Even better: try to not log at al!                             */
        streamOf(site.getWebpages())
            .forEach(p -> {
                p.loadDocument();                   
                DFP_FRAGMENT.build(p);
            });
        log.endPage();
    }

    private boolean isEnabled(List<String> enabled, Website site) {
        return ( enabled.isEmpty() || enabled.contains(site.getName()) );
    }


}
