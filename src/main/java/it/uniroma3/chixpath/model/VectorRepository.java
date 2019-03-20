package it.uniroma3.chixpath.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

public class VectorRepository {
	private Set<String> xpaths = new HashSet<>();
	private Set<Vector> vectors = new HashSet<>();
	private PageClass pageClass;
	private String id;
	private int n_Pag;
	
	public VectorRepository(PageClass pageClass, int pag , String id) {
		this.pageClass = pageClass;
		this.n_Pag = pag;
		this.id = id;
	}
	
	private boolean containsXPaths(String rule) {
		boolean found = false;
		for (String rule2 : this.getXPaths()) {
			if (rule2.compareTo(rule)==0) {
				found = true;
			}
		}
		return found;
	}
	
	private boolean containsVector(Vector vector) {
		boolean found = false;
		for (Vector v2 : this.getVectors()) {
			if (v2.equals(vector)) {
				found = true;
				break; //Consentito??
			}
		}
		return found;
	}
	
	void addUnique(String rule) throws XPathExpressionException {
		if (!this.containsXPaths(rule)) {
			Vector vector = new Vector(rule,this.getpageClass(),this.getN_Pag());
			if(!this.containsVector(vector)) {
				Set<String> newxPaths = this.getXPaths();
				Set<Vector> newVectors = this.getVectors();
				newxPaths.add(rule);
				newVectors.add(vector);
				this.setVectors(newVectors);
				this.setXPaths(newxPaths);
			}
		}
	}

	public Set<String> getXPaths() {
		return xpaths;
	}

	public void setXPaths(Set<String> xpaths) {
		this.xpaths = xpaths;
	}

	public Set<Vector> getVectors() {
		return vectors;
	}

	public void setVectors(Set<Vector> vectors) {
		this.vectors = vectors;
	}

	public PageClass getpageClass() {
		return pageClass;
	}

	public int getN_Pag() {
		return n_Pag;
	}
}
