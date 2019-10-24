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


	private Set<Page> pages;

	private Set<XPath> xpaths;
	public RulesRepository(Set<Page> pages ,String[] XFParguments) {


		this.pages = pages;
		//rulesGeneration();
		rulesGeneration(XFParguments);


	}

	public RulesRepository(Set<Page> pages, String[] XFParguments, int range) {
		this.pages = pages;
		//rulesGeneration(spec);
		rulesGeneration(range,XFParguments);
	}
	
	public RulesRepository(Set<Page> pages) {
		this.pages = pages;
		rulesGeneration(new ChiFragmentSpecification());
	}
	
	public RulesRepository(Set<Page> pages, int range) {
		this.pages = pages;
		ChiFragmentSpecification spec = new ChiFragmentSpecification();
		spec.setRange(range/2);
		rulesGeneration(spec);
	}


	private void rulesGeneration(ChiFragmentSpecification spec) {
		Map<Page,Set<String>> p2x = new HashMap<Page,Set<String>>();
		//final RuleInference engine = new RuleInference(new ChiFragmentSpecification());
		final RuleInference engine = new RuleInference(spec);
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
	public void rulesGeneration() {
		rulesGeneration(new ChiFragmentSpecification(HTML_STANDARD_CASEHANDLER));
	}


	private void rulesGeneration(int range, String[] XFParguments) {
		Map<Page,Set<String>> p2x = new HashMap<Page,Set<String>>();
		//final RuleInference engine = new RuleInference(new ChiFragmentSpecification());
		//final RuleInference engine = new RuleInference(spec);

		Set<String> diffXPaths = new HashSet<String>();
		for(Page sample : this.pages) {
			System.out.println("Generando xPaths sulla pagina"+sample.getUrl()+" con id: "+sample.getId());
			Map<String,String> id2name = new HashMap<>();
			//System.out.println("Pagine: "+ pages.size());
			for(Page p : this.pages) {
				if(p.equals(sample)) {
					String pageName = p.getUrl().split("/")[5];	
					String id = "idAP"+pageName.split(".html")[0];
					id2name.put(id, pageName);
				}
				else {
					/*String pageName = p.getUrl().split("/")[5];
					String id = "id"+pageName;
					id2name.put(id, pageName);*/
				}
			}
			Set<String> rules = null;
			try {
				rules = xfp.Main.inferRules(XFParguments,id2name,range);
			} catch (Exception e) {
				System.out.println("Rule generation failure");
				rules = new HashSet<>();
			} 
			sample.setDataRules(rules);
			
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



	public void rulesGeneration(String[] XFParguments) {
		rulesGeneration(3,XFParguments);
	}






	public Set<XPath> getXPaths() {
		return xpaths;
	}

}
