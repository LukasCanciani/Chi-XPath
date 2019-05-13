package xfp.model;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import static it.uniroma3.util.CollectionsCSVUtils.collection2csv;
import it.uniroma3.hlog.HypertextualLogger;

import java.util.ArrayList;
import java.util.List;

public class DomainLoader {

	static final private HypertextualLogger log = getLogger();

	private final Experiment experiment;
	
	public DomainLoader(Experiment experiment) {
		this.experiment = experiment;
	}

	public Domain loadWebsites(List<String> sitenames) {
		log.trace("loading websites:" + collection2csv(sitenames, "\n\t", "\n\t", "\n"));
		final Domain domain = this.experiment.getDomain();
		final ExperimentLoader loader = new ExperimentLoader(this.experiment);
		final List<Website> websites = new ArrayList<>();
		int pageCount = 0;
		for (final String name : sitenames) {
			final Website site = loader.loadWebsite(name);
			websites.add(site);
			pageCount += site.getWebpages().size();
			domain.addSite(site);
			log.trace();
			if (site.getAccessPages().isEmpty())	{
			    log.warn("No access page provided to "+site);
			} else {
			    log.trace("Access pages of "+site);
			    log.trace(site.getAccessPages());
			}
		}
        log.trace("Total number of pages loaded for "+domain+" domain: " + pageCount);
		return domain;
	}

}
