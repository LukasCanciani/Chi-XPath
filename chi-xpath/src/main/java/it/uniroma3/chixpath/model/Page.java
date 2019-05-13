package it.uniroma3.chixpath.model;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

public class Page {
    
    private String id;
    
    
    private Set<XPath> xpaths = new HashSet<>();
    

	private String url;
    
    private Document document;

    public Page(Document document, String url, String id) {
        this.document = document ;
        this.url = url;
        this.id = id;
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


}
