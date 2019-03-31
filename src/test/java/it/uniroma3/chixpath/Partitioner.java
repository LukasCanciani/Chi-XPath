package it.uniroma3.chixpath;

import static it.uniroma3.fragment.test.Fixtures.createPage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.model.PageClass;
import it.uniroma3.chixpath.model.RulesRepository;
import it.uniroma3.chixpath.model.XPath;
import it.uniroma3.chixpath.model.Partition;
import it.uniroma3.chixpath.model.Page;

public class Partitioner {
	public static void main(String args[]) throws XPathExpressionException {
		long startTime = System.currentTimeMillis();
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

		//final String siteUrl = URI.create(pageUrls.getFirst()).getHost(); // use host as siteUrl                

		System.out.println("Caricamento di " + pageUrls.size() + " pagine ");
		final Set<Page> pages = createPages(pageUrls);


		final RulesRepository rulesRep = new RulesRepository(pages);		


		//raggruppamento di TUTTE LE PAGINE che condividono GLI STESSI xPath
		
		final ArrayList<PageClass> pageClasses = groupPagesByXPaths(rulesRep.getXPaths());
		
		
		
		System.out.println("classiDiPagine ha una size di "+pageClasses.size());

		//stampa delle informazioni di ogni Classe di Pagine
		for(int i=0;i<pageClasses.size();i++) {
			PageClass temp = pageClasses.get(i);
			temp.print();
			System.out.print("\n");
		}
		System.out.println("");

		PageClass.createUniqueXPaths(pageClasses, MAX_PAGES);
		///DATOGLIERE
		/*rulesRep.OLDcreateUniqueXPaths(pageClasses);
		for(PageClass pc: pageClasses) {
			if (pc.getUniqueXPaths().size() != rulesRep.OLDclass2UniqueXpaths.get(pc).size()) {
				System.out.println("DIMENSIONI DIFFERENTI SU PAGINEA " + pc.getId());
			}
			else {
				boolean uguali = true;
				for (String str: pc.getUniqueXPaths()) {
					if(!rulesRep.OLDclass2UniqueXpaths.get(pc).contains(str)) {
						uguali = false;
						System.out.println("Trovato elemento diverso su pagina "+pc.getId());
					}
				}
				if (uguali)
						System.out.println("TUTTI ELEMENTI UGUALI SU PAGINA "+pc.getId());
			}
		}
		//FINO A QUI*/

		//selezione dell'Xpath caratteristico per ogni classe di pagine
		PageClass.selectCharacteristicXPath(pageClasses);
		for(PageClass pageClass : pageClasses) {
			System.out.println("Classe di pagine "+pageClass.getId()+" ha xPath caratteristico "+pageClass.getCharacteristicXPath());
		}

		//generazione partizioni
		final Set<Partition> partizioni = new HashSet<>();
		for(int i=0;i<pageClasses.size();i++) {
			//creazione di un Set<> di Classi di pagine per innescare il metodo ricorsivo che genera le partizioni
			final Set<PageClass> classi = new HashSet<>();
			classi.add(pageClasses.get(i));
			//metodo ricorsivo che a partire da una classe di pagine trova tutte le combinazioni di queste che formano una partizione 
			partizioni.addAll(allPossiblePartitions(classi,pageClasses,pageClasses.get(i).getPages().size(),MAX_PAGES));
		}

		//eliminazione partizioni equivalenti
		Set<Partition> senzaDuplicati = deleteDuplicates(partizioni);

		//per ogni partizione stampa il suo id, gli id delle classi di pagine al suo interno, e gl id delle pagine di ogni ClasseDiPagine
		System.out.println("\n");
		System.out.println("\n");
		for(Partition i : senzaDuplicati) {
			System.out.println(i);
			System.out.println("\n");
		}

		//controllo relazione di raffiamento
		/*for(int i=0; i<senzaDuplicati.size();i++) {
			ClassContainer ins = senzaDuplicati.get(i);
			for(int j=0; j<senzaDuplicati.size();j++) {
				if(ins.isRefinementOf(senzaDuplicati.get(j), MAX_PAGES))
					System.out.println("La partizione "+ins.getId()+ " e' raffinamento di "+senzaDuplicati.get(j).getId());
			}

		}*/
		for(Partition c1 : senzaDuplicati) {

			for(Partition c2 : senzaDuplicati) {
				if(c1.isRefinementOf(c2, MAX_PAGES))
					System.out.println("La partizione "+c1.getId()+ " e' raffinamento di "+c2.getId());
			}

		}
		long endTime = System.currentTimeMillis();
		System.out.println("It took " + (endTime - startTime)/1000 + " seconds");

	}




	//ciclo for, se i è un indice da eliminare vai avanti sennò aggiungi al nuovo array list
	private static Set<Partition> deleteDuplicates(Set<Partition> partizioni){
		final Set<String> alreadyChecked = new HashSet<>();
		final Set<String> idDaEliminare = new HashSet<>();

		for(Partition i : partizioni) {
			for(Partition j :partizioni) {
				if(!i.getId().equals(j.getId()) && i.samePartition(j) && !(alreadyChecked.contains(j.getId()))) {
					idDaEliminare.add(j.getId());
				}
			}
			alreadyChecked.add(i.getId());
		}

		final Set<Partition> senzaDuplicati = new HashSet<>();
		for(Partition toAdd : partizioni) {
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
	private static ArrayList<Partition> allPossiblePartitions(Set<PageClass> toAdd,ArrayList<PageClass> classiDiPagine, int n,int max){
		final ArrayList<Partition> partizioni = new ArrayList<>();
		//gestisce i casi in cui viene inserita un'unica ClasseDiPagine che contiene tutte le pagine (che è una partizione da sè)
		if(n==max) {
			final Partition partizione= new Partition(toAdd);
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
					partizioni.addAll(allPossiblePartitions(setClassi,classiDiPagine,n_for,max));
				}
				//le pagine FORMANO UNA PARTIZIONE
				else if(n_for==max) {
					final Partition partizione= new Partition(setClassi);
					partizioni.add(partizione);
				}
			}
			else {}
		}
		return partizioni;
	}


	

	private static ArrayList<PageClass> groupPagesByXPaths(Set<XPath> xpaths) {
		final ArrayList<PageClass> pClasses = new ArrayList<>();

		for(XPath xpath1 : xpaths) {
			String toCheck1 = xpath1.getRule();
			final Set<XPath> XPaths = new HashSet<>();
			final Set<Page> pages = new HashSet<>();
			XPaths.add(xpath1);
			pages.addAll(xpath1.getPages());

			for( XPath xpath2 : xpaths) {
				String toCheck2=xpath2.getRule();
				if((!toCheck1.equals(toCheck2)) && (checkSamePages(xpath1.getPages(),xpath2.getPages()))){
					XPaths.add(xpath2);
					//pages.addAll(xPath2Pages.get(toCheck2));
				}
			}
			final PageClass pageClasses = new PageClass(pages,XPaths);
			if(!pageClasses.containsXpathsSet(pClasses)) {
				pClasses.add(pageClasses);
			}
		}
		//Sistema gli id
		/*for(int i=1;i<pClasses.size()+1;i++) {
			pClasses.get(i-1).setId(Integer.toString(i));
		}*/
		return pClasses;
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

/*
rulesRep.OLDcreateUniqueXPaths(pageClasses);
for(PageClass pc: pageClasses) {
	if (pc.getUniqueXPaths().size() != rulesRep.OLDclass2UniqueXpaths.get(pc).size()) {
		System.out.println("DIMENSIONI DIFFERENTI SU PAGINEA " + pc.getId());
	}
	else {
		boolean uguali = true;
		for (String str: pc.getUniqueXPaths()) {
			if(!rulesRep.OLDclass2UniqueXpaths.get(pc).contains(str)) {
				uguali = false;
				System.out.println("Trovato elemento diverso su pagina "+pc.getId());
			}
		}
		if (uguali)
				System.out.println("TUTTI ELEMENTI UGUALI SU PAGINA "+pc.getId());
	}
}*/
