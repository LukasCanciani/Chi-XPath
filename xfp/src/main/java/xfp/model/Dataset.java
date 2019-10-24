package xfp.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * E.g., swde, weir.
 * 
 * A list of {@link Domain}s (e.g., auto, book ...), 
 * each containing a list of {@link Website}s
 * over the same vertical (e.g., amazon, barnesandnoble, etc. etc.)
 * 
 */
public class Dataset {
	
	private List<Domain> domains;
	
	private String name;
	
	public Dataset(String name) {
		this.name = name;
		this.domains = new LinkedList<>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addDomain(Domain domain) {
		this.domains.add(domain);
		domain.setDataset(this);
	}

	public List<Domain> getDomains() {
		return this.domains;
	}

    @Override
    public int hashCode() {
        return Objects.hash(this.getName());
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof Dataset)) return false;
        
        final Dataset that = (Dataset)o;
        return Objects.equals(this.getName(), that.getName());
    }

    @Override
	public String toString() {
		return getName();
	}
	
}
