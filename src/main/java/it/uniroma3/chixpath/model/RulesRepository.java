package it.uniroma3.chixpath.model;

//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashSet;
//import java.util.Map;
import java.util.Set;

//import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.fragment.RuleInference;


public class RulesRepository {
	public RulesRepository(Set<Page> pages) {
		this.setPages(pages);
		this.rulesGeneration();
		this.generateDifferentXpaths();
		this.createXpath2Pages();
		
	}

	private Set<String> differentXpaths  = new HashSet<>();
	//private Map<String, Set<Page>> xPath2Pages  = new HashMap<>();
	private Set<Page> pages;
	private Set<XPath> xpaths;
	//public Map<PageClass, Set<String>> OLDclass2UniqueXpaths;

	/*public void controllo() {
		boolean trovatoErrore=false;
		for (XPath xp : this.xpaths) {
			if (xp.getPages().size() == this.xPath2Pages.get(xp.getRule()).size()) {
				for (Page s : xp.getPages()) {
					if (!this.xPath2Pages.get(xp.rule).contains(s)) {
						System.out.println("RULE NON TROVATA");
						trovatoErrore = true;
						break;
					}
				}
			}
			else {
				System.out.println("Lunghezza non torna");
			}
		}
		if (!trovatoErrore) {
			System.out.println("TUTTO OK!!!!");
		}else
		{
			System.out.println("ERRRRRRRORE");
		}
	}*/
	
	public void rulesGeneration() {
		//chifragment specification senza argomenti usa l'HTML_STANDARD_CASEHANDLER
		final RuleInference engine = new RuleInference(new ChiFragmentSpecification());

		for(Page sample : this.pages) {
			final Set<String> rules = engine.inferRules(sample.getDocument());
			System.out.println("Generando xPaths sulla pagina"+sample.getUrl()+" con id: "+sample.getId());
			sample.setXPaths(rules);
		}
	}

	private void generateDifferentXpaths() {
		final Set<String> diffXpaths = new HashSet<>();

		for(Page sample : this.pages) {
			Set<String> toCheck = sample.getXPaths();
			for(String str : toCheck) {
				if(!diffXpaths.contains(str))   diffXpaths.add(str);
			}
		}
		this.setDifferentXpaths(diffXpaths);
	}

	private void createXpath2Pages() {
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
	}
	//Da togliere
	/*public void OLDcreateUniqueXPaths(ArrayList<PageClass> classiDiPagine) throws XPathExpressionException {
		final Map<PageClass, Set<String>> c2ux = new HashMap<>();
		for(PageClass classe : classiDiPagine) {
			final Set<String> senzaEquivalenti = OLDdeleteEquivalentXpaths(classe,4);
			c2ux.put(classe, senzaEquivalenti);
			//System.out.println("Classe di pagine "+classe.getId()+" matcha con "+senzaEquivalenti.size()+" xpaths");
		}
		System.out.println("");
		this.OLDclass2UniqueXpaths=c2ux;
	}*/

	
	/*public void createUniqueXPaths(ArrayList<PageClass> classiDiPagine) throws XPathExpressionException {
		final Map<PageClass, Set<String>> c2ux = new HashMap<>();
		for(PageClass classe : classiDiPagine) {
			final Set<String> senzaEquivalenti = OldDeleteEquivalentXpaths(classe,this.max_P);
			c2ux.put(classe, senzaEquivalenti);
			//System.out.println("Classe di pagine "+classe.getId()+" matcha con "+senzaEquivalenti.size()+" xpaths");
		}
		System.out.println("");
		this.setClass2UniqueXpaths(c2ux);
	}*/
	
	/*public void createUniqueXPaths(ArrayList<PageClass> classiDiPagine) throws XPathExpressionException {
		final Map<PageClass, Set<String>> c2ux = new HashMap<>();
		Set<String> senzaEquivalenti = new HashSet<>();
		Set<String> senzaEquivalenti2 = new HashSet<>();
		for(PageClass classe : classiDiPagine) {
			senzaEquivalenti = deleteEquivalentXpaths(classe,this.max_P);
			senzaEquivalenti2 = OldDeleteEquivalentXpaths(classe,this.max_P);
			c2ux.put(classe, senzaEquivalenti);
			if (senzaEquivalenti.size() == senzaEquivalenti2.size()) {
				boolean uguali = true;
				System.out.println("Stessa Lunghezza!!!\n");
				for (String s1 : senzaEquivalenti) {
					if (!senzaEquivalenti2.contains(s1)) {
						System.out.println("Uno diverso\n");
						uguali = false;
					}
				}
				for (String s2 : senzaEquivalenti2) {
					if (!senzaEquivalenti.contains(s2)) {
						System.out.println("Uno diverso\n");
						uguali = false;
					}
				}
				if (uguali) {
					System.out.println("Tutti UGUALI!!!!!!!!!!!!!!!!!\n");
				}
			}
			else {
				System.out.println("LUNGHEZZA DIVERSA!!!!\n");
				System.out.println("Nuova: "+senzaEquivalenti.size()+" Veccha: "+senzaEquivalenti2.size());
			}
			//System.out.println("Classe di pagine "+classe.getId()+" matcha con "+senzaEquivalenti.size()+" xpaths");
		}
		
		System.out.println("");
		this.setClass2UniqueXpaths(c2ux);
	}*/
	
	
	
	/*private static Set<String> OldDeleteEquivalentXpaths(PageClass classe, int MAX_PAGES) throws XPathExpressionException{
		final Set<String> alreadyChecked = new HashSet<>();
		final Set<String> xPathsDaEliminare = new HashSet<>();
		int index=1;
		//int stessiCounter=0;

		for(String rule : classe.getxPaths()) {
			System.out.println("controllando xpath "+index+ " di "+classe.getxPaths().size());
			ValuesVector v1 = new ValuesVector(rule,classe,MAX_PAGES);
			for(String rule1 : classe.getxPaths()) {
				ValuesVector v2 = new ValuesVector(rule1,classe,MAX_PAGES);
				if(!rule.equals(rule1) ) {
					if(!(alreadyChecked.contains(rule1))) {
						if((v1.equals(v2))) {
							xPathsDaEliminare.add(rule1);
							alreadyChecked.add(rule1);
							//stessiCounter++;
						}
					}
				}
			}
			alreadyChecked.add(rule);
			index++;
		}


		final Set<String> senzaEquivalenti = new HashSet<>();
		for(String rule : classe.getxPaths()) {
			if(!xPathsDaEliminare.contains(rule)) senzaEquivalenti.add(rule);
		}
		return senzaEquivalenti;
	}*/
	//VECCHIA 10,39 MIN NUOVA 15s sul test Messaggero
	//Datogliere!!!
	/*private static Set<String> OLDdeleteEquivalentXpaths(PageClass pClass, int MAX_PAGES) throws XPathExpressionException{
		VectorRepository container = new VectorRepository(pClass,MAX_PAGES,pClass.getId());
		int index=1;
		for (String rule : pClass.getxPaths()) {
				System.out.println("controllando xpath "+index+ " di "+pClass.getxPaths().size());
				container.addUnique(rule);
				index++;
		}
		return container.getXPaths();
	}*/
	
	
	
	
	public void setDifferentXpaths(Set<String> differentXpaths) {
		this.differentXpaths = differentXpaths;
	}
	
	public void setXPaths(Set<XPath> xpaths) {
		this.xpaths = xpaths;
	}

	public Set<XPath> getXPaths() {
		return xpaths;
	}
	
	public void setPages(Set<Page> pages) {
		this.pages = pages;
	}
	
}
