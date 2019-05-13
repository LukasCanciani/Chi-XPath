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

import static it.uniroma3.fragment.step.CaseHandler.*;

public class RulesRepository {
	public RulesRepository(Set<Page> pages) {
		
		
		this.pages = pages;
		rulesGeneration();
		
		
	}
	
	private Set<Page> pages;
	
	private Set<XPath> xpaths;
	
	
	
	public void rulesGeneration() {
		Map<Page,Set<String>> p2x = new HashMap<Page,Set<String>>();
		//final RuleInference engine = new RuleInference(new ChiFragmentSpecification());
		final RuleInference engine = new RuleInference(new ChiFragmentSpecification(HTML_STANDARD_CASEHANDLER,6));
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
	
	
	

	

	public Set<XPath> getXPaths() {
		return xpaths;
	}
	
}
