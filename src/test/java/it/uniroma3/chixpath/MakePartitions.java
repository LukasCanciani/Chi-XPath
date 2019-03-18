package it.uniroma3.chixpath;

import static it.uniroma3.fragment.test.Fixtures.createPage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.chixpath.model.PageClass;
import it.uniroma3.chixpath.model.RulesRepository;
import it.uniroma3.chixpath.model.ClassContainer;
import it.uniroma3.chixpath.model.Page;
import it.uniroma3.chixpath.model.ValuesVector;
import it.uniroma3.fragment.RuleInference;

public class MakePartitions {
	public static void main(String args[]) throws XPathExpressionException {

		final int MAX_PAGES = args.length;

		/* e.g., an example of a input args to this command line program:
           file:./src/test/resources/basic/section.html
           file:./src/test/resources/basic/article1.html
           file:./src/test/resources/basic/article2.html
           file:./src/test/resources/basic/article3.html 
           1 
           3
		 */
		if (args.length<2) {
			System.err.println("Inutile provare a generare partizioni con una pagina");
			System.exit(-1);
		}
		else System.out.println("Ricevuti " + args.length + " args: "+Arrays.toString(args));

		System.setProperty("http.agent", "Chrome"); // some sites want you to cheat

		final LinkedList<String> pageUrls = new LinkedList<>();

		/* parse and partition the cmd lines args into URLs */
		for(int index=0; index<args.length; index++) {
			if (isAnUrl(args[index])) {
				pageUrls.add(args[index]);
			}
			else {
				System.err.println("Cannot parse arg: "+args[index]);
				System.exit(-1);
			}
		}      

		final String siteUrl = URI.create(pageUrls.getFirst()).getHost(); // use host as siteUrl                

		System.out.println("Caricamento di " + pageUrls.size() + " pagine ");
		final Set<Page> pages = createPages(pageUrls);

		/*//chifragment specification senza argomenti usa l'HTML_STANDARD_CASEHANDLER
		final RuleInference engine = new RuleInference(new ChiFragmentSpecification());


		final Map<Page,Set<String>> rules.getPages2xpath()= new HashMap<>();
		final Map<String,Set<String>> ids2xpath = new HashMap<>();
		
		
		//generazione regole xpath su tutte le pagine e inserimento nelle mappe
		for(Page sample : pages) {
			final Set<String> rules = engine.inferRules(sample.getDocument());
			System.out.println("Generando xPaths sulla pagina"+sample.getUrl()+" con id: "+sample.getId());
			pages2xpath.put(sample,rules);
			ids2xpath.put(sample.getId(), rules);
		}
		*/
		final RulesRepository rulesRep = new RulesRepository(pages,MAX_PAGES);

		/*//insieme di tutti gli xpath diversi
		final Set<String> differentXpaths = differentXpaths(rulesRep.getPages2xpath());

		//raggruppamento di tutte le paigne che condividono LO STESSO xPath
		final Map<String, Set<Page>> xPath2Pages=createXpath2Pages(rulesRep.getDifferentXpaths(),rulesRep.getPages2xpath());*/  

		//raggruppamento di TUTTE LE PAGINE che condividono GLI STESSI xPath
		final ArrayList<PageClass> classiDiPagine = groupPagesByXpaths(rulesRep.getxPath2Pages());
		System.out.println("classiDiPagine ha una size di "+classiDiPagine.size());

		//stampa delle informazioni di ogni Classe di Pagine
		for(int i=0;i<classiDiPagine.size();i++) {
			PageClass temp = classiDiPagine.get(i);
			temp.print();
			System.out.print("\n");
		}
		System.out.println("");

	/*	//eliminazione xPath equivalenti
		final Map<PageClass, Set<String>> class2UniqueXpaths = new HashMap<>();
		rulesRep.getClass2UniqueXpaths();
		for(PageClass classe : classiDiPagine) {
			final Set<String> senzaEquivalenti = deleteEquivalentXpaths(classe,MAX_PAGES);
			class2UniqueXpaths.put(classe, senzaEquivalenti);
			//System.out.println("Classe di pagine "+classe.getId()+" matcha con "+senzaEquivalenti.size()+" xpaths");
		}
		System.out.println("");*/
		
		rulesRep.createUniqueXPaths(classiDiPagine);
		
		//selezione dell'Xpath caratteristico per ogni classe di pagine
		rulesRep.selectCharacteristicXPath();
		for(PageClass classe : rulesRep.getCharacteristicXpath().keySet()) {
			System.out.println("Classe di pagine "+classe.getId()+" ha xPath caratteristico "+rulesRep.getCharacteristicXpath().get(classe));
		}

		//generazione partizioni
		final ArrayList<ClassContainer> partizioni = new ArrayList<>();
		for(int i=0;i<classiDiPagine.size();i++) {
			//creazione di un Set<> di Classi di pagine per innescare il metodo ricorsivo che genera le partizioni
			final Set<PageClass> classi = new HashSet<>();
			classi.add(classiDiPagine.get(i));
			//metodo ricorsivo che a partire da una classe di pagine trova tutte le combinazioni di queste che formano una partizione 
			partizioni.addAll(trovaTutteLePossibiliPartizioni(classi,classiDiPagine,classiDiPagine.get(i).getPages().size(),MAX_PAGES));
		}

		//eliminazione partizioni equivalenti
		ArrayList<ClassContainer> senzaDuplicati = deleteDuplicates(partizioni);

		//per ogni partizione stampa il suo id, gli id delle classi di pagine al suo interno, e gl id delle pagine di ogni ClasseDiPagine
		System.out.println("\n");
		System.out.println("\n");
		for(ClassContainer i : senzaDuplicati) {
			i.print();
			System.out.println("\n");
		}

		//controllo relazione di raffiamento
		for(int i=0; i<senzaDuplicati.size();i++) {
			ClassContainer ins = senzaDuplicati.get(i);
			for(int j=0; j<senzaDuplicati.size();j++) {
				if(ins.isRefinementOf(senzaDuplicati.get(j), MAX_PAGES))
					System.out.println("La partizione "+ins.getId()+ " e' raffinamento di "+senzaDuplicati.get(j).getId());
			}

		}
	}

	/*private static Map<PageClass, String> selectCharacteristicXPath(Map<PageClass, Set<String>> class2xpaths) {
		final Map<PageClass, String> result = new HashMap<>();
		for(PageClass classe : class2xpaths.keySet()) {
			final Set<String> rules = class2xpaths.get(classe);
			String bestXPath = null;

			if (!rules.isEmpty()) {
				final LinkedList<String> ranking = new LinkedList<String>(rules);
				/* order by XPath expression length *//*
				ranking.sort(Comparator.<String>comparingInt( xpath -> xpath.length() )
						.thenComparing( xpath -> xpath.toString() )
						);
				bestXPath = ranking.getFirst();
			}
			else
				System.out.println("Nessun xPath caratteristico trovato per la classe di pagine"+ classe.getId());
			result.put(classe, bestXPath);
		}
		return result;
	} */

	/*private static Set<String> deleteEquivalentXpaths(PageClass classe, int MAX_PAGES) throws XPathExpressionException{
		final Set<String> alreadyChecked = new HashSet<>();
		final Set<String> xPathsDaEliminare = new HashSet<>();
		int index=1;
		int stessiCounter=0;

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
							stessiCounter++;
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


	//ciclo for, se i è un indice da eliminare vai avanti sennò aggiungi al nuovo array list
	private static ArrayList<ClassContainer> deleteDuplicates(ArrayList<ClassContainer> partizioni){
		final Set<String> alreadyChecked = new HashSet<>();
		final Set<String> idDaEliminare = new HashSet<>();

		for(ClassContainer i : partizioni) {
			for(ClassContainer j :partizioni) {
				if(!i.getId().equals(j.getId()) && i.samePartition(j) && !(alreadyChecked.contains(j.getId()))) {
					idDaEliminare.add(j.getId());
				}
			}
			alreadyChecked.add(i.getId());
		}

		final ArrayList<ClassContainer> senzaDuplicati = new ArrayList<>();
		for(ClassContainer toAdd : partizioni) {
			if(!idDaEliminare.contains(toAdd.getId())) senzaDuplicati.add(toAdd);
		}

		return senzaDuplicati;

	}


	  /**
     * @param toAdd - attuale combinazione di classi di pagine da cui si vuole creare la partizione
     * @param classiDiPagine - TUTTE le classi di pagine create dalle quali si vogliono generare le partizioni
     * @param n - somma delle pagine dell'attuale combinazione di classi di pagine (una combinazione di classi di pagine è una partizione quando n==max)
     * @param max - numero di pagine inserite in INPUT
     * @return tutte le possibili partizioni a partire da una singola classe di pagine
     */
	private static ArrayList<ClassContainer> trovaTutteLePossibiliPartizioni(Set<PageClass> toAdd,ArrayList<PageClass> classiDiPagine, int n,int max){
		final ArrayList<ClassContainer> partizioni = new ArrayList<>();
		//gestisce i casi in cui viene inserita un'unica ClasseDiPagine che contiene tutte le pagine (che è una partizione da sè)
		if(n==max) {
			final ClassContainer partizione= new ClassContainer();
			partizione.setpClasses(toAdd);
			partizioni.add(partizione);
		}

		for(PageClass classe: classiDiPagine) {
			Set<PageClass> setClassi= new HashSet<>();
			setClassi.addAll(toAdd);
			//n_for è il valore che do a n nella chiamata ricorsiva
			int n_for=n; 
			//se sono tutte pagine distinte la cui somma è minore del nmax
			if(((n_for+classe.getPages().size()<=max) && (!classe.hasSamePagesAs(toAdd)))) {
				//aggiorno n sommando le pagine della classe di pagine e aggiungo la classe di pagine all'insieme 
				n_for+=classe.getPages().size();
				setClassi.add(classe);
				//RICORSIONE:se le pagine non creano ancora una partizione
				if( n_for<max) {
					partizioni.addAll(trovaTutteLePossibiliPartizioni(setClassi,classiDiPagine,n_for,max));
				}
				//le pagine FORMANO UNA PARTIZIONE
				else if(n_for==max) {
					final ClassContainer partizione= new ClassContainer();
					partizione.setpClasses(setClassi);
					partizioni.add(partizione);
				}
			}
			else {}
		}
		return partizioni;
	}


	//metodo che crea la mappa delle pagine che matchano con gli stessi xpath
	private static ArrayList<PageClass> groupPagesByXpaths(Map<String, Set<Page>> xPath2Pages) {
		final ArrayList<PageClass> classiDiPagine = new ArrayList<>();

		for(String toCheckl : xPath2Pages.keySet()) {
			final PageClass classeDiPagine = new PageClass();
			final Set<String> xPaths = new HashSet<>();
			final Set<Page> pages = new HashSet<>();
			xPaths.add(toCheckl);
			pages.addAll(xPath2Pages.get(toCheckl));

			for( String toCheck2 : xPath2Pages.keySet()) {
				if((!toCheckl.equals(toCheck2)) && (checkSamePages(xPath2Pages.get(toCheckl),xPath2Pages.get(toCheck2)))){
					xPaths.add(toCheck2);
					//pages.addAll(xPath2Pages.get(toCheck2));
				}
			}

			classeDiPagine.setPages(pages);
			classeDiPagine.setxPaths(xPaths);
			if(!classeDiPagine.containsXpathsSet(classiDiPagine)) {
				classiDiPagine.add(classeDiPagine);
			}
		}
		for(int i=1;i<classiDiPagine.size()+1;i++) {
			classiDiPagine.get(i-1).setId(Integer.toString(i));
		}
		return classiDiPagine;
	}

	/*
	 * RITORNA TRUE SE DUE SET<PAGE> CONTENGONO LE STESSE PAGINE
	 */
	private static boolean checkSamePages(Set<Page> set, Set<Page> set2) {
		boolean same = true;
		final Set<String> app1= new HashSet<>();
		final Set<String> app2= new HashSet<>();

		if(set.size()==set2.size()) {
			for(Page page : set) {
				app1.add(page.getId());
			}

			for(Page page : set2) {
				app2.add(page.getId());
			}

			for(String id : app1) {
				if(!app2.contains(id)) {
					same= false;
					return same;
				}
			}
		}
		else same=false;
		return same;
	}

	/*private static Map<String, Set<Page>> createXpath2Pages(Set<String> differentXpaths, Map<Page, Set<String>> pages2xpath) {
		final Map<String, Set<Page>> xPath2Pages= new HashMap<>();

		for(String xpath : differentXpaths) {
			final Set<Page> pagesMatching = new HashSet<>();
			for(Page sample : pages2xpath.keySet()) {
				if(pages2xpath.get(sample).contains(xpath)) {
					pagesMatching.add(sample);
				}
			}
			xPath2Pages.put(xpath, pagesMatching);
		}

		return xPath2Pages;
	}*/
	
	/*static private Set<String> differentXpaths(Map <Page,Set<String>> pages2xpath) {
		final Set<String> differentXpaths = new HashSet<>();

		for(Page sample : pages2xpath.keySet()) {
			Set<String> toCheck = pages2xpath.get(sample);
			for(String str : toCheck) {
				if(!differentXpaths.contains(str))   differentXpaths.add(str);
			}
		}
		return differentXpaths;
	}*/

	static private boolean isAnUrl(String s) {
		try {
			new URL(s);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	static private Set<Page> createPages(LinkedList<String> pageUrls) {
		final Set<Page> pages = new HashSet<>();

		int id=0;
		for(String url : pageUrls) {
			String content = loadPageContent(url);
			Page page = createPage(content);
			page.setUrl(url);
			page.setId(""+id);
			id++;
			pages.add(page);
		}
		System.out.println();
		return pages;
	}

	static private String loadPageContent(String anURL) {
		final StringWriter out = new StringWriter();
		final PrintWriter writer = new PrintWriter(out);
		try {
			final URLConnection conn = new URL(anURL).openConnection();

			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream())
					);

			String line;
			while ( (line=reader.readLine())!=null) {
				writer.println(line);
			}

			writer.close();
			out.close();
		} catch (Exception e) { 
			throw new RuntimeException(e);
		}

		return out.toString();
	}
}
