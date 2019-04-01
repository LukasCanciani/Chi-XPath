package it.uniroma3.chixpath.model;

import java.util.HashMap;
//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
//import java.util.Map;
import java.util.Set;

//import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.fragment.RuleInference;


public class RulesRepository {
	public RulesRepository(Set<Page> pages) {
		/*this.setPages(pages);
		this.rulesGeneration();
		this.generateDifferentXpaths();
		this.createXPath2Pages();*/
		
		this.pages = pages;
		rulesGeneration();
		
		
	}
	
	

	//private Set<String> differentXpaths  = new HashSet<>();
	private Set<Page> pages;
	//private Set<XPath> xpaths;
	
	private Set<XPath> xpaths;
	
	
	/*public void rulesGeneration() {
		//chifragment specification senza argomenti usa l'HTML_STANDARD_CASEHANDLER
		final RuleInference engine = new RuleInference(new ChiFragmentSpecification());

		for(Page sample : this.pages) {
			final Set<String> rules = engine.inferRules(sample.getDocument());
			System.out.println("Generando xPaths sulla pagina"+sample.getUrl()+" con id: "+sample.getId());
			sample.setXPaths(rules);
		}
	}*/
	
	public void rulesGeneration() {
		Map<Page,Set<String>> p2x = new HashMap<Page,Set<String>>();
		final RuleInference engine = new RuleInference(new ChiFragmentSpecification());
		Set<String> diffXPaths = new HashSet<String>();
		for(Page sample : this.pages) {
			System.out.println("Generando xPaths sulla pagina"+sample.getUrl()+" con id: "+sample.getId());
			final Set<String> rules = engine.inferRules(sample.getDocument());
			for (String str : rules) {
				if(!diffXPaths.contains(str))
					diffXPaths.add(str);
			}
			p2x.put(sample, rules);
		}
		Set<XPath> xpaths = new HashSet<XPath>();
		for (String rule : diffXPaths) {
			Set<Page> pages = new HashSet<Page>();
			for (Page page : p2x.keySet()) {
				if (p2x.get(page).contains(rule)) {
					pages.add(page);
				}
			}
			XPath xpath = new XPath(rule,pages);
			xpaths.add(xpath);
			for (Page page : pages) {
				page.addXPath(xpath);
			}
		}
		this.xpaths = xpaths;
		
	}

	/*private void generateDifferentXpaths() {
		final Set<String> diffXpaths = new HashSet<>();

		for(Page sample : this.pages) {
			Set<String> toCheck = sample.getXPaths();
			for(String str : toCheck) {
				if(!diffXpaths.contains(str))   diffXpaths.add(str);
			}
		}
		this.setDifferentXpaths(diffXpaths);
	}

	private void createXPath2Pages() {
		Set<XPath> x2pag = new HashSet<>();
		for(String xpath : differentXpaths) {
			XPath rule = new XPath(xpath);
			final Set<Page> pagesMatching = new HashSet<>();
			for(Page sample : this.pages) {
				if(sample.getXPaths().contains(xpath)) {
					pagesMatching.add(sample);
				}
			}
			rule.setPages(pagesMatching);
			x2pag.add(rule);
		}
		this.setXPaths(x2pag);
	}*/
	
	
	

	

	public Set<XPath> getXPaths() {
		return xpaths;
	}
	
}
