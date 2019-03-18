package it.uniroma3.chixpath.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

public class VectorContainer {
	private Set<String> xPaths = new HashSet<>();
	private Set<ValuesVector> vectors = new HashSet<>();
	private PageClass pClass;
	private String id;
	private int n_Pag;
	
	public VectorContainer(PageClass pClass, int pag , String id) {
		this.pClass = pClass;
		this.n_Pag = pag;
		this.id = id;
	}
	
	private boolean containsXpaths(String rule) {
		boolean found = false;
		for (String rule2 : this.getxPaths()) {
			if (rule2.compareTo(rule)==0) {
				found = true;
			}
		}
		return found;
	}
	
	private boolean containsVector(ValuesVector vector) {
		boolean found = false;
		for (ValuesVector v2 : this.getVectors()) {
			if (v2.equals(vector)) {
				found = true;
				break; //Consentito??
			}
		}
		return found;
	}
	
	void addUnique(String rule) throws XPathExpressionException {
		if (!this.containsXpaths(rule)) {
			ValuesVector vector = new ValuesVector(rule,this.getpClass(),this.getN_Pag());
			if(!this.containsVector(vector)) {
				Set<String> newxPaths = this.getxPaths();
				Set<ValuesVector> newVectors = this.getVectors();
				newxPaths.add(rule);
				newVectors.add(vector);
				this.setVectors(newVectors);
				this.setxPaths(newxPaths);
			}
		}
	}

	public Set<String> getxPaths() {
		return xPaths;
	}

	public void setxPaths(Set<String> xPaths) {
		this.xPaths = xPaths;
	}

	public Set<ValuesVector> getVectors() {
		return vectors;
	}

	public void setVectors(Set<ValuesVector> vectors) {
		this.vectors = vectors;
	}

	public PageClass getpClass() {
		return pClass;
	}

	public int getN_Pag() {
		return n_Pag;
	}
}
