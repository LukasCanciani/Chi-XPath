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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.rmi.CORBA.Util;
import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.model.PageClass;
import it.uniroma3.chixpath.model.RulesRepository;
import it.uniroma3.chixpath.model.XPath;
import xfp.fixpoint.FixedPoint;
import it.uniroma3.chixpath.model.Partition;
import it.uniroma3.chixpath.model.Lattice;
import it.uniroma3.chixpath.model.Page;


public class PartitionerXFP {
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
		execution(args);
	}

	public static List<Partition> execution(String args[]) throws XPathExpressionException {
		return execution(args,6,1);
	}

	public static List<Partition> execution(String[] args, int selectedRange, int nBest) {
		System.out.println("Starting Chi-XFP\n");
		//final int MAX_PAGES = args.length;

		/* e.g., an example of a input args to this command line program:
           file:./src/test/resources/basic/section.html
           file:./src/test/resources/basic/article1.html
           file:./src/test/resources/basic/article2.html
           file:./src/test/resources/basic/article3.html
         	file:./dataset/test/autoscout/1ap3car/0.html
           3
		 */
		if (args.length<2) {
			System.err.println("Inutile provare a generare partizioni con una pagina");
			System.exit(-1);
		}
		else System.out.println("Ricevuti " + args.length + " args: "+Arrays.toString(args));
		System.setProperty("http.agent", "Chrome"); // some sites want you to cheat

		final LinkedList<String> pageUrls = new LinkedList<>();
		final LinkedList<String> apURIS= new LinkedList<>();
		/* parse and partition the cmd lines args into URLs */
		int pagesNum=0;
		//Genero una lista di id e nomi file
		for(int index=0; index<args.length; index++) {
			pagesNum++;
			if (isAnUrl(args[index])) {
				pageUrls.add(args[index]);
				if (index < args.length-1) {
					if (args[index+1].trim().equals("AP")) {
						apURIS.add(args[index]);
						index++;
					}
				}
			}
			else {
				System.err.println("Cannot parse arg: "+args[index]);
				System.exit(-1);
			}
		}
		final int MAX_PAGES = pagesNum;
		//Calcolo gli attributi necessari per l'esecuzione dell'algoritmo XFP
		String[] XFParguments = new String[6];
		XFParguments[0] = "-d";
		XFParguments[1] = pageUrls.getFirst().split("/")[3];
		XFParguments[2] = "-s";
		XFParguments[3] = pageUrls.getFirst().split("/")[2];
		XFParguments[4] = "-w";
		XFParguments[5] = pageUrls.getFirst().split("/")[4];

		//final String siteUrl = URI.create(pageUrls.getFirst()).getHost(); // use host as siteUrl
		System.out.println("Caricamento di " + pageUrls.size() + " pagine ");
		final Set<Page> pages = createPages(pageUrls);
		//ChiFragmentSpecification spec = new ChiFragmentSpecification(HTML_STANDARD_CASEHANDLER,3);
		int range = selectedRange;
		long rulesGenerationStart = System.currentTimeMillis();
		//final RulesRepository rulesRep = new RulesRepository(pages,spec);
		//Estraggo le regola
		final RulesRepository rulesRep = new RulesRepository(pages, XFParguments,range);
		long rulesGenerationStop = System.currentTimeMillis();


		//raggruppamento di TUTTE LE PAGINE che condividono GLI STESSI xPath
		//Creazione classi di pagine
		Set<PageClass> pageClasses = groupPagesByXPaths(rulesRep);



		System.out.println("classiDiPagine ha una size di "+pageClasses.size());

		/*//stampa delle informazioni di ogni Classe di Pagine
		for(PageClass temp : pageClasses) {
			System.out.println(temp);
			System.out.print("\n");
		}
		System.out.println("");*/
		/*System.out.println("InizioUnique");
		long rulesControlStart = System.currentTimeMillis();
		//PageClass.createUniqueXPaths(pageClasses, MAX_PAGES);										//Rimozione di regole che generano gli stessi vettori, per ora trascurata
		long rulesControlStop = System.currentTimeMillis();
		System.out.println("InizioCharacteristic");*/
		
		//selezione dell'Xpath caratteristico per ogni classe di pagine
		System.out.println("Selezione XPath caratteristico");
		
		//Selezione degli xpath caratteristici
		PageClass.selectCharacteristicXPath(pageClasses);
		System.out.println("DFP di sito");
		
		//Esecuzione DFP di sito
		Set<FixedPoint<String>> siteDFP = PageClass.executeSiteDFP(pageClasses, XFParguments, pages);


		long NFPStart = System.currentTimeMillis();
		//Calcolo NFP per ogni classe
		System.out.println("InizioNFP");
		PageClass.executeNFP(pageClasses,XFParguments,pages);
		long NFPStop = System.currentTimeMillis();

		long partitionStart = System.currentTimeMillis();
		System.out.println("Genero le partizioni");
		//Generazione partizioni
		Lattice lattice = generatePartitions(pageClasses, MAX_PAGES);
		
		
		
		
		
		long partitionStop = System.currentTimeMillis();

		for(PageClass pc : pageClasses) {
			if (pc.rules.isEmpty())
				System.out.println("vuota");
		}

		PrintResults(lattice,pageClasses);


		String siteName = pageUrls.getFirst().split("/")[3];
		String samplesName = pageUrls.getFirst().split("/")[4];
		
		//Generazione del grafo- servono foto pagine e funziona solo con insieme molto piccolo 7-8 pagine max
		//generateGraph(siteName,samplesName,pages,lattice);



		long bestStart = System.currentTimeMillis();
		System.out.println("Calcolo soluzione ottima e DFP");
		
		//Calcolo del DFP e delle migliori n punteggi
		List<Partition> best = findBestSolution(lattice,XFParguments,pages,siteDFP,range, nBest);
		long bestStop = System.currentTimeMillis();

		System.out.println("************SOLUTION************\n");
		if (best.size()==1) {
			System.out.println("SOLUZIONE UNIVOCA");
		}else if(best.size() == 0) {
			System.out.println("SOLUZIONE NON TROVATA");
		}
		else {
			System.out.println("SOLUZIONE NON UNIVOCA");
		}
		for(Partition p : best) {
			System.out.println("/n***********************************************");
			System.out.println(p);
			System.out.println("***********************************************");
		}

		System.out.println("********************************\n");
		

		System.out.println("Rules Generation : " + (rulesGenerationStop-rulesGenerationStart)/1000 + " seconds");
		//System.out.println("Rules Control  : " + (rulesControlStop-rulesControlStart)/1000 + " seconds");
		System.out.println("Lattice creation  : " + (partitionStop-partitionStart)/1000 + " seconds");
		System.out.println("NFP  : " + (NFPStop-NFPStart)/1000 + " seconds");
		System.out.println("Best find and DFP : " + (bestStop-bestStart)/1000 + " seconds");
		/********************************************************/
		int totalFixedPoints= 0;
		for(PageClass pc: pageClasses) {
			totalFixedPoints = totalFixedPoints + pc.getConstantFP() + pc.getVariableFP() + pc.getConstantOptional() + pc.getVariableOptional();
			for(Set<PageClass> set : pc.getNFP().keySet()) {
				int[] NFP = pc.getNFP().get(set);
				totalFixedPoints = totalFixedPoints + NFP[0] + NFP[1] + NFP[2] + NFP[3];
			}
		}
		System.out.println("TOTAL FIXED POINTS = " + totalFixedPoints);
		/********************************************************/
		return best;
	}


	//Calcola DFP, rimuovere quelli di sito e restituisce le partizioni con i migliori n punteggi
	private static List<Partition> findBestSolution(Lattice lattice, String[] XFParguments, Set<Page> pages, Set<FixedPoint<String>> siteDFP, int range, int nBest)  {
		//Trovo il massimo valore di NFP, poi seleziono tutte le partizioni che si trovano nel "quarto" più alto
		List<Partition> bestNFP = new ArrayList<>();


		int i = 0;
		do {
			bestNFP = new ArrayList<>();
			int maxNFP = 0;
			for(Partition p : lattice.getPartitions()) {
				int NFP = (p.getInnerNFP()[0] + p.getInnerNFP()[1]) / p.getPageClasses().size(); //Con o senza divisione??
				if (NFP > maxNFP) {
					maxNFP = NFP;
				}
			}
			for(Partition p : lattice.getPartitions()) {
				int NFP = (p.getInnerNFP()[0] + p.getInnerNFP()[1]) / p.getPageClasses().size(); //Con o senza divisione??
				if (NFP >= maxNFP/4*(3-i)) {
					bestNFP.add(p);
				}
			}
			i++;
		}
		while (bestNFP.size()<nBest && i<=3);
		Set<PageClass> classes = new HashSet<>();
		for(Partition p : bestNFP) {
			classes.addAll(p.getPageClasses());
		}
		PageClass.executeDFP(classes,XFParguments,pages,siteDFP,range);
		System.out.println("BEST NFP***************************");
		for(Partition p: bestNFP) {
			System.out.println(p + "\n");
		}
		System.out.println("**********************************");
		List<Partition> solution = new ArrayList<>();
		SortedSet<Float> ranks = new TreeSet<>(new Comparator<Float>() {
			public int compare(Float o1, Float o2) {
				if(o1<o2)
					return -1;
				else if(o1.equals(o2))
					return 0;
				else 
					return 1;
			};
		});
		for (Partition p : bestNFP) {
			if(!ranks.contains(p.getRank()))
				ranks.add((Float)p.getRank());
		}
		Set<Float> bestNRanks = new HashSet<Float>();
		System.out.println("RANKS: " + ranks);
		for (int j = 0; j<nBest; j++) {
			if(!ranks.isEmpty()) {
				bestNRanks.add(ranks.last());
				ranks.remove(ranks.last());
			}
		}
		for(Partition p : bestNFP) {
			if(bestNRanks.contains(p.getRank())) {
				solution.add(p);
			}
		}
		
		return solution;
	}



	private static void PrintResults(Lattice lattice, Set<PageClass> pageClasses) {
		//per ogni partizione stampa il suo id, gli id delle classi di pagine al suo interno, e gl id delle pagine di ogni ClasseDiPagine
		System.out.println("\n");
		System.out.println("********Lattice********");

		System.out.println("***Page Classes***");
		for(PageClass pc: pageClasses) {
			System.out.println(pc);
		}

		System.out.println("***Partizioni***");
		for(Partition p : lattice.getPartitions()) {
			System.out.println(p);
			System.out.println("\n");
		}

		

	}




	@SuppressWarnings("unused")
	//Generazione grafica del grafo
	private static void generateGraph(String siteName, String samplesName, Set<Page> pages, Lattice lattice) {


		Map<Page,String> p2i = new HashMap<Page,String>();
		for (Page p : pages) {
			String name = p.getUrl().split("/")[5].split("[.]")[0] ;
			String image = "./dataset/old/"+siteName+"/"+samplesName+"/images/"+
					name;
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
							+ "SRC=\"C:\\Users\\Lukas\\Documents\\TirocinioGit-Finale\\ChiXPath-FinalV\\chi-xpath"+p2i.get(pag).split("[.]")[1]+".PNG\"/></TD></TR>");
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





	
	/* generazione delle partizioni. in modo ricorsivo*/
	private static Lattice generatePartitions(Set<PageClass> pageClasses, int MAX_PAGES) {
		final Set<Partition> partitions = new HashSet<>();
		Set<PageClass> toCheck = new HashSet<>();
		Set<PageClass> singles = new HashSet<>();
		for(PageClass pageClass : pageClasses) {
			if(pageClass.getPages().size() == 1) {
				singles.add(pageClass);
			}else {
				toCheck.add(pageClass);
			}
		}
		for (PageClass pageClass : pageClasses) {
			if(pageClass.getPages().size() != 1) {
				toCheck.remove(pageClass);
				Set<PageClass> classes = new HashSet<>();
				classes.add(pageClass);
				partitions.addAll(allPossiblePartitions(classes,toCheck,singles,pageClass.getPages().size(),MAX_PAGES));
			}
		}
		Partition.reorderPartitions(partitions);
		Lattice lattice = new Lattice(partitions);
		return lattice;
	}

	/*Generazione di tutte le poss. partizioni. per ridurre i tempi non utilizzare le partizioni "singleton"
	 * ma considerate solamente alla fine, se non è possibile aggiungere altre partizioni
	 */
	private static Set<Partition> allPossiblePartitions(Set<PageClass> currentClasses,Set<PageClass> toCheck,Set<PageClass> singles, int pageCount,int max){
		final Set<Partition> partitions = new HashSet<>();
		boolean last = true;
		if (pageCount == max) {
			final Partition partition = new Partition(currentClasses);
			partitions.add(partition);
			last = false;
		}
		else if(!toCheck.isEmpty()) {
			Set<PageClass> newToCheck = new  HashSet<>();
			newToCheck.addAll(toCheck);
			for (PageClass PClass : toCheck) {
				newToCheck.remove(PClass);
				if ((pageCount+PClass.getPages().size()<=max)&&(!PClass.hasSamePagesAs(currentClasses))){
					Set<PageClass> newClasses = new HashSet<>();
					newClasses.addAll(currentClasses);
					newClasses.add(PClass);
					Set<Partition> partials = allPossiblePartitions(newClasses,newToCheck,singles,(pageCount+PClass.getPages().size()),max);
					if(!partials.isEmpty()) {
						last = false;
					}
					partitions.addAll(partials);
				}
			}
		} 
		if(last && singles!=null) {
			for(PageClass single : singles) {
				if(!single.hasSamePagesAs(currentClasses)) {
					currentClasses.add(single);
					pageCount++;
				}
			}
			if(pageCount == max) {
				final Partition partition = new Partition(currentClasses);
				partitions.add(partition);
			}
		}
		return partitions;
	}



	//Generazione classi di pagine
	private static Set<PageClass> groupPagesByXPaths(RulesRepository rulesRepo) {
		/*final*/ Set<PageClass> pClasses = new HashSet<>();
		Set<XPath> xpaths = rulesRepo.getXPaths();
		Set<Page> allPages = rulesRepo.getPages();

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
			final PageClass pageClass = new PageClass(pages,XPaths);
			if(!pageClass.containsXpathsSet(pClasses) /*&& pageClasses.getPages().size() != 1 /*&& pageClasses.getPages().size() != 2*/) {
				pClasses.add(pageClass);	
			}
		}
		pClasses = PageClass.addMissingSingleton(pClasses,allPages);
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
			int currID;
			try {
				currID = Integer.parseInt(url.split("/")[url.split("/").length-1].split(".html")[0]);
			} catch(Exception e){
				currID = id;
			}
			Page page = createPage(content, url, ""+currID);
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
