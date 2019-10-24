package xfp.util;

import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.*;

import it.uniroma3.preferences.Constant;
import xfp.model.Experiment;
/**
 * An immutable class that uses a singleton paradigm to model a
 * configuration: these are a set of generic system-wide configuration
 * properties,  i.e., they should not be related to an experiment.
 * <br/>
 * It checks that's loaded only once for each execution.
 *
 */
public class XFPConfig {

	static private XFPConfig instance;
	
	private Configuration configuration;
	
	private Experiment current; // the experiment currently executed

	private XFPConfig(Configuration xfp_project_config) {
		/* default values in the bundled configuration file     *
		 * with higher priority than hard-coded default values. */
        /* command-line set system config with higher priority  *
         * than the configuration bundled with the project.     */
		CompositeConfiguration cc = new CompositeConfiguration();
        cc.addConfiguration(new SystemConfiguration());
		cc.addConfiguration(xfp_project_config);
		cc.addConfiguration(Constants.defaultConfiguration());
		this.configuration = cc;
	}

	static public XFPConfig load(URL config) throws ConfigurationException {
		return instance = new XFPConfig(new PropertiesConfiguration(config));
	}

	synchronized static public XFPConfig getInstance() {
		if (instance==null)
			throw new IllegalStateException("Configuration not loaded yet!");
		return instance;
	}

	static public Experiment getCurrentExperiment() {
		return getInstance().current;
	}

	public void setCurrentExperiment(Experiment current) {
		this.current = current;
	}

	static public Configuration getConfiguration() {
		return getInstance().configuration;
	}
	
	static public String getString(Constant name) {
		return getConfiguration().getString(name.key());		
	}

	static public int getInteger(Constant name) {
		return getConfiguration().getInt(name.key());		
	}
		
	static public boolean getBoolean(Constant name) {
		return getConfiguration().getBoolean(name.key());		
	}
	
	static public double getDouble(Constant name) {
		return getConfiguration().getDouble(name.key());		
	}
	
	static public List<String> getList(Constant name) {
		return Arrays.asList(getConfiguration().getStringArray(name.key()));
	}
	
	static public <E extends Enum<E>> List<E> getEnumList(Class<E> cls, Constant name) {
		List<E> result = new LinkedList<>();
		for (String ename : getList(name)) {
			result.add(Enum.valueOf(cls,ename.toUpperCase()));
		}
		return result;
	}

}
