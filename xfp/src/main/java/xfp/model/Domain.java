package xfp.model;


import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * e.g., real-estate, cruises ...
 *
 * A list of {@link Website}s in the same vertical domain
 * (and belonging to the same {@link Dataset}
 */
public class Domain {
	private Dataset dataset;

	final private List<Website> sites;

	private String name;
	
	public Domain(String name) {
		this.name = name;
		this.sites = new LinkedList<>();
	}
		
	public String getName() {
		return this.name;
	}
	
	public void addSite(Website site) {
		checkIsFromSameDomain(site);
		site.setDomain(this);
		this.sites.add(site);
	}
	
	private void checkIsFromSameDomain(Website site) {
		final Domain domain = site.getDomain();
		if (domain!=null && domain!=this)
			throw new IllegalArgumentException(site+" is from another domain ("+domain+")");
	}

	public List<Website> getSites() {
		return this.sites;
	}

	public Dataset getDataset() {
		return this.dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public int size() {
		return this.getSites().size();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getName())+Objects.hashCode(this.getDataset());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==null || o.getClass()!=getClass()) return false;
		
		final Domain that = (Domain)o;
		return Objects.equals(this.getName(),    that.getName()) &&
			   Objects.equals(this.getDataset(), that.getDataset());
	}

	@Override
	public String toString() {
		return this.getDataset()+"-"+getName();
	}

}
