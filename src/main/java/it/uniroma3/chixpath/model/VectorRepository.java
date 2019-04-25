package it.uniroma3.chixpath.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

public class VectorRepository {
	private Set<XPath> xpaths = new HashSet<>();
	private Set<Vector> vectors = new HashSet<>();
	private PageClass pageClass;
	private String id;
	private int pagNum;
	
	public VectorRepository(PageClass pageClass, int pag , String id) {
		this.pageClass = pageClass;
		this.pagNum = pag;
		this.id = id;
	}
	
	private boolean containsXPaths(XPath rule) {
		boolean found = false;
		for (XPath rule2 : this.getXPaths()) {
			if (rule2.getRule().compareTo(rule.getRule())==0) {
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
	
	void addUnique(XPath rule) throws XPathExpressionException {
		if (!this.containsXPaths(rule)) {
			Vector vector = new Vector(rule,this.getpageClass(),this.getPagNum());
			if(!this.containsVector(vector)) {
				Set<XPath> newxPaths = this.getXPaths();
				Set<Vector> newVectors = this.getVectors();
				newxPaths.add(rule);
				newVectors.add(vector);
				this.vectors = (newVectors);
				this.xpaths = (newxPaths);
			}
		}
	}

	public Set<XPath> getXPaths() {
		return xpaths;
	}

	

	public Set<Vector> getVectors() {
		return vectors;
	}


	public PageClass getpageClass() {
		return pageClass;
	}

	public int getPagNum() {
		return pagNum;
	}
}
