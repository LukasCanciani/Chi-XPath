package xfp.util;

import it.uniroma3.preferences.Constant;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

public enum Constants implements Constant {
	
//### GENERAL PROPERTIES
	EXPERIMENTS_PATH("./src/main/resources/experiments"),
	DATASET_PATH("./dataset"),
	
	MAX_PAGES_PER_SOURCE("0"),
	
	ID_FILTER(".+"),
	
	ID_AP_SELECTOR("idAP"),
	
	REDUNDANCY_CHECK_ENABLED("false"),

//### FORCE THE USE OF PARALLEL STREAM WHEN CODED	
	PARALLEL_STREAMS_ENABLED("false"),
	
//### EXTRACTION PROPERTIES
	MIN_EXTRACTION_SAMPLES("3"),
	MAX_EXTRACTION_SAMPLES("3"),
	
	DATA_STEP_FACTORIES("U,RNE,RT,D,S"),
    LINK_STEP_FACTORIES("U,LH,RNE,DFTFT"),
                
	MAX_PIVOT_DISTANCE("3"),
	MIN_PIVOT_LENGTH("2"),
	MAX_PIVOT_LENGTH("32"),
	MAX_VALUE_LENGTH("96"), 
	
	MIN_REDUNDANT_TEXT_LENGTH("2"),
	
	LINK_FIXEDPOINTS_STORING_ENABLED("false"),
	DATA_FIXEDPOINTS_STORING_ENABLED("false"),
	DATA_FIXPOINTS_EVALUATION_ENABLED("true"),
	
//### OUTPUT HANDLER
	OUTPUT_HANDLER_TYPE("FILE"),
	DB_CONNECTION_STRING("jdbc:postgresql://localhost/xfp"),
	DB_SCHEMA("xfpsource"),
	DB_USERNAME("xfp"),
	DB_PASSWORD("xfp"),
	
//### LOGGING PROPERTIES
    FRAGMENT_LOGGING_ENABLED("true"),
    
    RULE_LOGGING_ENABLED("true");
    
	// STEPS PROPERTIES (TODO)
    
	private final String defaultValue;
															      
    public String defaultValue() {
		return defaultValue;
	}

	private Constants(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	
	/**
	 * Produces the key associated with this {@link Constants}
	 * to use it as key of a {@link Configuration}
	 * @return
	 */
	public String key() {
		return name().toLowerCase();
	}
        
	static final public Configuration defaultConfiguration() {
		Configuration defaultConfiguration = new BaseConfiguration();
		for(Constants c : values())
			defaultConfiguration.setProperty(c.name().toLowerCase(), c.defaultValue());
		return defaultConfiguration;
	}    
    
}
