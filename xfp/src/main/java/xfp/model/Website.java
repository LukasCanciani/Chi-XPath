package xfp.model;

import java.util.*;
/**
 * 
 * Simple implementation to support local
 * crawling of websites by means of a
 * naming mechanism to map remote {@link Webpage} 
 * to local files.
 * <BR/>
 * Every website has got to have at least an entry point {@link #getAccessPage()}.
 */
public class Website {

	private Domain domain;
	
	private List<Webpage> accessPages; // entry points to this website

	// all available pages from this website, made accessible by page index
	private List<Webpage> pages;
	
	// page (local) name -> Webpage object, to access by page name
	private Map<String, Webpage> name2page;
	
	private String name;
	
	public Website(String name) {
        this.name = name;
        this.pages = new ArrayList<>();
        this.accessPages = new ArrayList<>();
		this.name2page = new HashMap<>();
	}

	public Set<Webpage> getAccessPages()	{
		return Collections.unmodifiableSet(new LinkedHashSet<>(this.accessPages));
	}
	
	public void addAccessPage(Webpage entryPoint) {
	    this.addPage(entryPoint); // no dups
	    this.accessPages.add(entryPoint);
	}
	
	public String getName() {
		return this.name;
	}

	public Domain getDomain() {
		return this.domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	/**
	 * Add a page to this website
	 * @param page
	 * @return true is the added page is not a duplicate
	 */
	public boolean addPage(Webpage page) {
        // do not allow duplicates (by page name)
        if (!this.name2page.containsKey(page.getName())) {
            page.setWebsite(this, this.pages.size());
            this.pages.add(page);
            this.name2page.put(page.getName(), page);
            return true;
        }
        return false;
	}
	
	public Webpage getPage(String pagename) {
	    return this.name2page.get(pagename);
	}

	/**
	 * 
	 * @return all the pages in this website
	 */
	public List<Webpage> getWebpages() {		
		return Collections.unmodifiableList(this.pages);
	}

	public void clear() {
	    this.pages.clear();
	    this.name2page.clear();
	}
	
	public void releaseDOMs() {		
		for (Webpage page : this.pages) {
			page.releaseDocument();
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getName())+Objects.hashCode(this.getDomain());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==null || this.getClass()!=o.getClass()) return false;
		
		final Website that = (Website)o;
		return Objects.equals(this.getName(),   that.getName()  ) &&
			   Objects.equals(this.getDomain(), that.getDomain());
	}

    public String toHTMLstring() {
        return this.getName();
    }

    @Override
    public String toString() {
        return toHTMLstring();
    }

}
