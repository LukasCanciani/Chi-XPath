package it.uniroma3.chixpath.model;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;

public class Page {
    
    private String id;
    
    private Set<String> xpaths= new HashSet<>();
    
    private String url;
    
    private Document document;

    public Page(Document document) {
        this.document = document ;
    }
    
    public String getId() {
        return this.id;
    }

    public Page setId(String id) {
        this.id = id;
        return this;
    }

    public String getUrl() {
        return this.url;
    }

    public Page setUrl(String url) {
        this.url = url;
        return this;
    }

    public Document getDocument() {
        return this.document;
    }

    public Page setDocument(Document pageDom) {
        this.document = pageDom;
        return this;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" "+this.getUrl();
    }

	public Set<String> getXPaths() {
		return xpaths;
	}

	public void setXPaths(Set<String> xpaths) {
		this.xpaths = xpaths;
	}

}
