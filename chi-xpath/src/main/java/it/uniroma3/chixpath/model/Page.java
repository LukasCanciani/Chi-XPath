package it.uniroma3.chixpath.model;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

public class Page {
    
    private String id;
    
    
    private Set<XPath> xpaths = new HashSet<>();
    

	private String url;
    
    private Document document;
    
    public Set<String> dataRules;

    public Page(Document document, String url, String id) {
        this.document = document ;
        this.url = url;
        this.id = id;
        
        this.dataRules = new HashSet<>();
        
    }
    
    public Page(Document document, String url) {
        this.document = document ;
        this.url = url;
    }
    
    public Page(Document document) {
        this.document = document ;
    }
    
    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }


    public Document getDocument() {
        return this.document;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" "+this.getUrl();
    }
	
	public void addXPath(XPath x) {
		this.xpaths.add(x);
	}
	
	public Set<XPath> getXPaths() {
		return this.xpaths;
	}

	public Set<String> getDataRules() {
		return dataRules;
	}

	public void setDataRules(Set<String> dataRules) {
		this.dataRules = dataRules;
		System.out.println("settata");
	}

	public Set<XPath> getXpaths() {
		return xpaths;
	}
	@Override
	public boolean equals(Object obj) {
		Page that = (Page) obj;
		return this.id.equals(that.id);
	}


}
