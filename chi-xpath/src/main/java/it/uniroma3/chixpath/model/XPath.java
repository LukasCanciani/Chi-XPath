package it.uniroma3.chixpath.model;

import java.util.Set;

public class XPath implements Comparable<XPath> {
	public XPath(String rule, Set<Page> pages) {
		this.rule = rule;
		this.pages = pages;
	}
	String rule;
	Set<Page> pages;
	public String getRule() {
		return rule;
	}
	public Set<Page> getPages() {
		return pages;
	}
	
	@Override
	public int compareTo(XPath arg0) {
		return this.rule.compareTo(arg0.getRule());
				
	}
	@Override
	public boolean equals(Object obj) {
		XPath xp = (XPath) obj;
		return this.getRule().equals(xp.getRule());
	}
	
	@Override
	public int hashCode() {
		return this.getRule().hashCode();
	}
	
	
	
}
