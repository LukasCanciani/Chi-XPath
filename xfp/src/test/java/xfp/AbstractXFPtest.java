package xfp;

import static it.uniroma3.hlog.HypertextualLogManager.loadConfiguration;

import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.BeforeClass;

import it.uniroma3.hlog.HypertextualLogManager;
import xfp.util.XFPConfig;

public class AbstractXFPtest {

    @BeforeClass
    static public void setUpXFPexecutions() {
    	String xfpConfigFilename = "./xfp-config.properties";
    	String xfpHlogConfigFilename = "/xfp-hlog.properties";
    	/* otherwise one of the old LFEQ comparators that implements
    	 * a partial-but-not-total ordering would rise exceptions 
    	 * when executed by new java versions. */
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    	System.setProperty("java.util.logging.manager", "it.uniroma3.hlog.HypertextualLogManager");
    	
    	// now automatically loaded by hlog
    	loadConfiguration(xfpHlogConfigFilename); 
    
    	URL configURL = ClassLoader.getSystemResource(xfpConfigFilename);	
    	try {
    		XFPConfig.getInstance();
    	} catch (IllegalStateException cfgEx) {
    		try {
    			XFPConfig.load(configURL);
    		} catch (ConfigurationException e) {
    			throw new RuntimeException("Cannot read XFP configuration: "+xfpConfigFilename,cfgEx);
    		}
    	}
    }
    
    @Before
    public void setUpLogManager() {
        HypertextualLogManager.getLogManager().reset();
    }


    @Before
    public void tearDownLogManager() {
        HypertextualLogManager.getLogManager().reset();
    }
}
