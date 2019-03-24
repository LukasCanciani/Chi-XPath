package it.uniroma3.chixpath.model;

import java.util.Set;

public class XPath {
	public XPath(String rule) {
		this.rule = rule;
	}
	String rule;
	Set<Page> pages;
	public String getRule() {
		return rule;
	}
	public Set<Page> getPages() {
		return pages;
	}
	public void setPages(Set<Page> pages) {
		this.pages = pages;
	}
	
}
