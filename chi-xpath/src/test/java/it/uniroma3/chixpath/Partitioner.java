package it.uniroma3.chixpath;

import static it.uniroma3.fragment.test.Fixtures.createPage;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.model.PageClass;
import it.uniroma3.chixpath.model.RulesRepository;
import it.uniroma3.chixpath.model.XPath;
import it.uniroma3.chixpath.model.Partition;
import it.uniroma3.chixpath.model.Lattice;
import it.uniroma3.chixpath.model.Page;


public class Partitioner {
	static {
        /* otherwise one of the old LFEQ comparators that implements
         * a partial-but-not-total ordering would rise exceptions 
         * when executed by new java versions. */
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        // set java.util.logging.manager to inject hypertextual logger
        System.setProperty("java.util.logging.manager", "it.uniroma3.hlog.HypertextualLogManager");     
    }

    static final protected it.uniroma3.hlog.HypertextualLogger log = it.uniroma3.hlog.HypertextualLogger.getLogger(); /* this *after* previous static block */
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

		long rulesGenerationStart = System.currentTimeMillis();
		final RulesRepository rulesRep = new RulesRepository(pages);
		long rulesGenerationStop = System.currentTimeMillis();


		//raggruppamento di TUTTE LE PAGINE che condividono GLI STESSI xPath

		final Set<PageClass> pageClasses = groupPagesByXPaths(rulesRep.getXPaths());



		System.out.println("classiDiPagine ha una size di "+pageClasses.size());

		/*//stampa delle informazioni di ogni Classe di Pagine
		for(PageClass temp : pageClasses) {
			System.out.println(temp);
			System.out.print("\n");
		}
		System.out.println("");*/
		long rulesControlStart = System.currentTimeMillis();
		PageClass.createUniqueXPaths(pageClasses, MAX_PAGES);
		long rulesControlStop = System.currentTimeMillis();
		
			
		for(PageClass temp : pageClasses) {
			System.out.println(temp);
			System.out.print("\n");
		}
		//selezione dell'Xpath caratteristico per ogni classe di pagine
		PageClass.selectCharacteristicXPath(pageClasses);
		for(PageClass pageClass : pageClasses) {
			System.out.println("Classe di pagine "+pageClass.getId()+" ha xPath caratteristico "+pageClass.getCharacteristicXPath().getRule());
		}
		long partitionStart = System.currentTimeMillis();
		System.out.println("Genero le partizioni");
		Lattice lattice = generatePartitions(pageClasses, MAX_PAGES);
		long partitionStop = System.currentTimeMillis();
		//per ogni partizione stampa il suo id, gli id delle classi di pagine al suo interno, e gl id delle pagine di ogni ClasseDiPagine
		System.out.println("\n");
		System.out.println("\n");

		System.out.println(lattice);
		String siteName = pageUrls.getFirst().split("/")[5];
		generateGraph(siteName,pages,lattice);
		long endTime = System.currentTimeMillis();
		System.out.println("It took " + (endTime - startTime)/1000 + " seconds");
		System.out.println("Rules Generation : " + (rulesGenerationStop-rulesGenerationStart)/1000 + " seconds");
		System.out.println("Rules Control  : " + (rulesControlStop-rulesControlStart)/1000 + " seconds");
		System.out.println("Lattice creation  : " + (partitionStart-partitionStop)/1000 + " seconds");

	}
	
	


	private static void generateGraph(String siteName, Set<Page> pages, Lattice lattice) {
		
		
		Map<Page,String> p2i = new HashMap<Page,String>();
		for (Page p : pages) {
			String name = p.getUrl().split("/")[6].split("[.]")[0] ;
			String image = "./src/test/resources/basic/"+siteName+"/images/"+name;
			p2i.put(p, image);
		}
		
		FileWriter fw = null;
		String fileName = siteName.concat(".dot");
		try {
			fw = new FileWriter(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println("graph "+siteName +" {");
		pw.println("graph [ratio=fill];");
		pw.println("node [label=\"\\N\", fontsize=20, shape=rect];");
		for (Partition p : lattice.getPartitions()) {
			pw.println(p.getId()+"[label=<<TABLE ALIGN=\"LEFT\" CELLBORDER= \"1\" BORDER=\"0\">");
			pw.println("<TR><TD BORDER=\"0\" ALIGN = \"CENTER\" COLSPAN=\"2\">"+p.getId()+"</TD></TR>");
			pw.println("<TR>");
			for(PageClass pc : p.getPageClasses()) {
				//Dividere le colonne, per non avere tutte le pagine una sotto l'altra
				/*int range = pc.getPages().size()/2;
				int rest = pc.getPages().size()%2;*/
				pw.println("<TD>");
				pw.println("<TABLE BORDER=\"0\" CELLBORDER = \"1\">");
				pw.println("<TR><TD BORDER=\"0\" COLSPAN=\"2\">"+pc.getId()+"</TD></TR>");
				for(Page pag : pc.getPages()) {
					pw.println("<TR><TD fixedsize=\"true\" width=\"100\" height=\"100\"><IMG SCALE=\"FALSE\" "
							+ "SRC=\"C:\\Users\\Lukas\\git\\Chi-Xpath\\chi-xpath"+p2i.get(pag).split("[.]")[1]+".PNG\"/></TD></TR>");
				}
				pw.println("</TABLE>");
				pw.println("</TD>");
			}
			pw.println("</TR>");
			pw.println("</TABLE>> ];");
		}
		pw.println();
		for(Partition p : lattice.getIsRefinedBy().keySet()) {
			for(Partition p1: lattice.getIsRefinedBy().get(p)) {
				pw.println(p.getId() + " -- "+p1.getId());
			}
		}
		pw.println("}");
		pw.close();
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/*private static void generateGraphOld(LinkedList<String> pageUrls, Set<Page> pages, Lattice lattice) {
		String siteName = pageUrls.getFirst().split("/")[5];
		FileWriter fw = null;
		String fileName = siteName.concat(".dot");
		try {
			fw = new FileWriter(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println("graph "+siteName +" {");
		pw.print("node [shape=circle]; ");
		for (Partition p : lattice.getPartitions()) {
			pw.print(" "+p.getId()+";");
		}
		pw.println();
		for(Partition p : lattice.getIsRefinedBy().keySet()) {
			for(Partition p1: lattice.getIsRefinedBy().get(p)) {
				pw.println(p.getId() + " -- "+p1.getId());
			}
		}
		pw.println("}");
		pw.close();
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/



	/*//ciclo for, se i è un indice da eliminare vai avanti sennò aggiungi al nuovo array list
	private static Set<Partition> deleteDuplicates(Set<Partition> partitions){
		final Set<String> alreadyChecked = new HashSet<>();
		final Set<String> deleteId = new HashSet<>();

		for(Partition i : partitions) {
			for(Partition j :partitions) {
				if(!i.getId().equals(j.getId()) && i.samePartition(j) && !(alreadyChecked.contains(j.getId()))) {
					deleteId.add(j.getId());
				}
			}
			alreadyChecked.add(i.getId());
		}

		final Set<Partition> uniques = new HashSet<>();
		for(Partition toAdd : partitions) {
			if(!deleteId.contains(toAdd.getId())) uniques.add(toAdd);
		}
		Partition.reorderPartitions(uniques);
		return uniques;

	}*/


	/**
	 * @param toAdd - attuale combinazione di classi di pagine da cui si vuole creare la partizione
	 * @param classiDiPagine - TUTTE le classi di pagine create dalle quali si vogliono generare le partizioni
	 * @param n - somma delle pagine dell'attuale combinazione di classi di pagine (una combinazione di classi di pagine è una partizione quando n==max)
	 * @param max - numero di pagine inserite in INPUT
	 * @return tutte le possibili partizioni a partire da una singola classe di pagine
	 */


	private static Lattice generatePartitions(Set<PageClass> pageClasses, int MAX_PAGES) {
			final Set<Partition> partitions = new HashSet<>();
			Set<PageClass> toCheck = new HashSet<>();
			toCheck.addAll(pageClasses);
			for (PageClass pageClass : pageClasses) {
				toCheck.remove(pageClass);
				Set<PageClass> classes = new HashSet<>();
				classes.add(pageClass);
				partitions.addAll(allPossiblePartitions(classes,toCheck,pageClass.getPages().size(),MAX_PAGES));
			}
			Partition.reorderPartitions(partitions);
			Lattice lattice = new Lattice(partitions);
			return lattice;
	}

	private static Set<Partition> allPossiblePartitions(Set<PageClass> currentClasses,Set<PageClass> toCheck, int pageCount,int max){
			final Set<Partition> partitions = new HashSet<>();
			if (pageCount == max) {
				final Partition partition = new Partition(currentClasses);
				partitions.add(partition);
			}
			else if(!toCheck.isEmpty()) {
				Set<PageClass> newToCheck = new  HashSet<>();
				newToCheck.addAll(toCheck);
				for (PageClass PClass : toCheck) {
					if ((pageCount+PClass.getPages().size()<=max)&&(!PClass.hasSamePagesAs(currentClasses))){
						Set<PageClass> newClasses = new HashSet<>();
						newClasses.addAll(currentClasses);
						newClasses.add(PClass);
						newToCheck.remove(PClass);
						partitions.addAll(allPossiblePartitions(newClasses,newToCheck,(pageCount+PClass.getPages().size()),max));
					}
				}
			}
			return partitions;
	}


	/*private static Lattice generatePartitionsOld(Set<PageClass> pageClasses, int MAX_PAGES) {
		//generazione partizioni
				final Set<Partition> partitions = new HashSet<>();
				for(PageClass pageClass : pageClasses) {
					//creazione di un Set<> di Classi di pagine per innescare il metodo ricorsivo che genera le partizioni
					final Set<PageClass> classi = new HashSet<>();
					classi.add(pageClass);
					//metodo ricorsivo che a partire da una classe di pagine trova tutte le combinazioni di queste che formano una partizione
					partitions.addAll(allPossiblePartitions(classi,pageClasses,pageClass.getPages().size(),MAX_PAGES));
				}
				System.out.println("Finito di generare ora elimino le ripetizioni");

				//eliminazione partizioni equivalenti
				Set<Partition> unique = deleteDuplicates(partitions);
				Lattice lattice = new Lattice(unique);
				return lattice;
	}

	private static ArrayList<Partition> allPossiblePartitionsOld(Set<PageClass> toAdd,Set<PageClass> classiDiPagine, int n,int max){
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
	}*/




	private static Set<PageClass> groupPagesByXPaths(Set<XPath> xpaths) {
		final Set<PageClass> pClasses = new HashSet<>();

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
		PageClass.reorderClasses(pClasses);
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
			Page page = createPage(content, url, ""+id);
			//page.setId(""+id);
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
