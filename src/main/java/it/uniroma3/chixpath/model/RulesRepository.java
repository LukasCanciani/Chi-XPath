package it.uniroma3.chixpath.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.fragment.RuleInference;


public class RulesRepository {
	public RulesRepository(Set<Page> pages,int max) {
		this.setPages(pages);
		this.rulesGeneration();
		this.generateDifferentXpaths();
		this.createXpath2Pages();
		this.max_P=max;
	}

	private Set<String> differentXpaths  = new HashSet<>();
	private Map<String, Set<Page>> xPath2Pages  = new HashMap<>();
	private Map<Page,Set<String>> pages2xpath  = new HashMap<>();
	//private Map<String,Set<String>> ids2xpath  = new HashMap<>();
	private Set<Page> pages;
	private Map<PageClass, Set<String>> class2UniqueXpaths;
	private Map<PageClass, String> characteristicXpath = new HashMap<>();
	private int max_P;

	public void rulesGeneration() {
		//chifragment specification senza argomenti usa l'HTML_STANDARD_CASEHANDLER
		final RuleInference engine = new RuleInference(new ChiFragmentSpecification());
		final Map<Page,Set<String>> p2x= new HashMap<>();
		final Map<String,Set<String>> i2x = new HashMap<>();

		for(Page sample : this.pages) {
			final Set<String> rules = engine.inferRules(sample.getDocument());
			System.out.println("Generando xPaths sulla pagina"+sample.getUrl()+" con id: "+sample.getId());
			p2x.put(sample,rules);
			i2x.put(sample.getId(), rules);
			//this.setIds2xpath(i2x);
			this.setPages2xpath(p2x);
		}
	}

	private void generateDifferentXpaths() {
		final Set<String> diffXpaths = new HashSet<>();

		for(Page sample : this.pages2xpath.keySet()) {
			Set<String> toCheck = this.pages2xpath.get(sample);
			for(String str : toCheck) {
				if(!diffXpaths.contains(str))   diffXpaths.add(str);
			}
		}
		this.setDifferentXpaths(diffXpaths);
	}

	private void createXpath2Pages() {
		final Map<String, Set<Page>> x2p= new HashMap<>();

		for(String xpath : differentXpaths) {
			final Set<Page> pagesMatching = new HashSet<>();
			for(Page sample : pages2xpath.keySet()) {
				if(pages2xpath.get(sample).contains(xpath)) {
					pagesMatching.add(sample);
				}
			}
			x2p.put(xpath, pagesMatching);
		}

		this.setxPath2Pages(x2p);
	}

	public void createUniqueXPaths(ArrayList<PageClass> classiDiPagine) throws XPathExpressionException {
		final Map<PageClass, Set<String>> c2ux = new HashMap<>();
		for(PageClass classe : classiDiPagine) {
			final Set<String> senzaEquivalenti = deleteEquivalentXpaths(classe,this.max_P);
			c2ux.put(classe, senzaEquivalenti);
			//System.out.println("Classe di pagine "+classe.getId()+" matcha con "+senzaEquivalenti.size()+" xpaths");
		}
		System.out.println("");
		this.setClass2UniqueXpaths(c2ux);
	}
	
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
	
	
	
	private static Set<String> OldDeleteEquivalentXpaths(PageClass classe, int MAX_PAGES) throws XPathExpressionException{
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
	}
	//VECCHIA 10,39 MIN NUOVA 15s sul test Messaggero
	private static Set<String> deleteEquivalentXpaths(PageClass pClass, int MAX_PAGES) throws XPathExpressionException{
		VectorContainer container = new VectorContainer(pClass,MAX_PAGES,pClass.getId());
		int index=1;
		for (String rule : pClass.getxPaths()) {
				System.out.println("controllando xpath "+index+ " di "+pClass.getxPaths().size());
				container.addUnique(rule);
				index++;
		}
		return container.getxPaths();
	}
	
	public  void selectCharacteristicXPath() {
		final Map<PageClass, String> result = new HashMap<>();
		for(PageClass classe : this.getClass2UniqueXpaths().keySet()) {
			final Set<String> rules = this.getClass2UniqueXpaths().get(classe);
			String bestXPath = null;

			if (!rules.isEmpty()) {
				final LinkedList<String> ranking = new LinkedList<String>(rules);
				/* order by XPath expression length */
				ranking.sort(Comparator.<String>comparingInt( xpath -> xpath.length() )
						.thenComparing( xpath -> xpath.toString() )
						);
				bestXPath = ranking.getFirst();
			}
			else
				System.out.println("Nessun xPath caratteristico trovato per la classe di pagine"+ classe.getId());
			result.put(classe, bestXPath);
		}
		this.setCharacteristicXpath(result);
	}
	
	
	public void setDifferentXpaths(Set<String> differentXpaths) {
		this.differentXpaths = differentXpaths;
	}
	
	public Map<String, Set<Page>> getxPath2Pages() {
		return xPath2Pages;
	}

	public void setxPath2Pages(Map<String, Set<Page>> xPath2Pages) {
		this.xPath2Pages = xPath2Pages;
	}

	public void setPages2xpath(Map<Page, Set<String>> pages2xpath) {
		this.pages2xpath = pages2xpath;
	}
	
			/*public void setIds2xpath(Map<String, Set<String>> ids2xpath) {
				this.ids2xpath = ids2xpath;
			}*/
	
	public void setPages(Set<Page> pages) {
		this.pages = pages;
	}
	
	public Map<PageClass, Set<String>> getClass2UniqueXpaths() {
		return class2UniqueXpaths;
	}

	public void setClass2UniqueXpaths(Map<PageClass, Set<String>> class2UniqueXpaths) {
		this.class2UniqueXpaths = class2UniqueXpaths;
	}
	
	public Map<PageClass, String> getCharacteristicXpath() {
		return characteristicXpath;
	}
	public void setCharacteristicXpath(Map<PageClass,String> charact) {
		this.characteristicXpath=charact;
	}
}
