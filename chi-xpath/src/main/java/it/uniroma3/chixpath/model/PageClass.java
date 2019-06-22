package it.uniroma3.chixpath.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;


import xfp.fixpoint.FixedPoint;


public class PageClass implements Comparable<PageClass> {
	private Set<XPath> xpaths = new HashSet<>();

	private Set<Page> pages= new HashSet<>();

	private String id;
	private Set<String> pagesNames;


	private VectorRepository vr;
	private int variableFP;
	private int constantFP;
	private Map<PageClass,int[]> NFP;


	public Map<PageClass, int[]> getNFP() {
		return NFP;
	}

	public Set<String> getPagesNames() {
		return pagesNames;
	}

	public int getConstantFP() {
		return constantFP;
	}

	public int getVariableFP() {
		return variableFP;
	}




	public VectorRepository getVr() {
		return vr;
	}

	private void setId(String id) {
		this.id = id;
	}

	private Set<XPath> uniqueXPaths;

	private XPath characteristicXPath;

	private static int progId=0;

	public PageClass(Set<Page> pages, Set<XPath> xpaths){
		this.id = (Integer.toString(progId++));
		this.pages = pages;
		this.xpaths = xpaths;
		pagesNames = new HashSet<>();
		for (Page p : pages) {
			pagesNames.add(p.getUrl().split("/")[5]);
		}
		this.constantFP=0;
		this.variableFP=0;
		this.NFP = new HashMap<>();
	}

	public String getId() {
		return id;
	}



	public Set<XPath> getxPaths() {
		return xpaths;
	}



	public Set<Page> getPages() {
		return pages;
	}


	public float tfIdf() {
		float totaltf = 0;
		for(Vector v : this.getVr().getVectors()) {
			float tf = 0;
			for(Page p : this.getPages()) {
				tf = tf + ((float) v.getExtractedNodes()[Integer.parseInt(p.getId())].getLength() )/ (float)this.getVr().getTotalNodes()[Integer.parseInt(p.getId())];
			}
			tf = tf/this.getPages().size();
			totaltf =totaltf+ tf;
		}
		totaltf= totaltf/this.getVr().getVectors().size();
		float totalIdf = (float) (Math.log(this.getVr().getPagNum()/this.getPages().size())/Math.log(10));
		return totaltf * totalIdf;
	}

	@Override
	public int compareTo(PageClass that) {
		return this.getId().compareTo(that.getId());
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		final PageClass that = (PageClass)o;
		return this.getxPaths().equals(that.getxPaths());
	}


	public String toString() {
		String out = "PageClass "+this.getId()+ " Pages: \n";
		for(Page page : this.getPages()) {
			out=out.concat("  ID:"+page.getId()+" ");
		}	
		out=out.concat("\nXPaths: "+this.getUniqueXPaths().size()+"\nDFP: Constant:"+this.getConstantFP() + " Variable: "+this.getVariableFP());
		out=out.concat("NFP: ");
		for(PageClass pc : this.getNFP().keySet()) {
			out=out.concat("\nTo Pageclass: "+pc.getId() );//+ " NFP: Constant:"+this.getNFP().get(pc)[1] + " Variable: "+this.getNFP().get(pc)[0]);
		}
		out=out.concat("\n");
		return out;
	}

	public boolean hasSamePagesAs(Set<PageClass> toAdd) {
		boolean containsId=false;
		final Set<String> idsToCheck = new HashSet<>();
		//creo l'insieme di id da controllare che NON SIANO nel mio insieme di insiemi di pagine
		for(Page page : this.getPages()) {
			idsToCheck.add(page.getId());
		}

		//per ogni singolo insieme che fa parte dell'insieme di regole creo un insieme di id

		for(PageClass test : toAdd){
			if (test.getPages() != null) {
				for(Page page : test.getPages()) {
					Set<String> ids = new HashSet<>();
					ids.add(page.getId());
					//controllo che nessun id del primo set sia nel set da controllare
					for(String id : idsToCheck ) {
						if(ids.contains(id)) {
							containsId=true;
							return containsId;
						}
					}
				}
			}
		}

		return containsId;
	}



	public boolean containsXpathsSet(Set<PageClass> classes) {
		for(PageClass pageClass : classes) {
			if(this.getxPaths().equals(pageClass.getxPaths())) return true;
		}
		return false;
	}

	public Set<XPath> getUniqueXPaths() {
		return uniqueXPaths;
	}

	public Set<String> getUniqueXPathsAsString(){
		Set<String> xpaths = new HashSet<>();
		for ( XPath x : this.getUniqueXPaths()) {
			xpaths.add(x.getRule());
		}
		return xpaths;
	}

	private  void setUniqueXPaths(Set<XPath> uniqueXPaths) {
		this.uniqueXPaths = uniqueXPaths;
	}

	public XPath getCharacteristicXPath() {
		return characteristicXPath;
	}

	public static void createUniqueXPaths(Set<PageClass> pageClasses,int max_p) throws XPathExpressionException {
		for(PageClass classe : pageClasses) {
			final Set<XPath> senzaEquivalenti = classe.deleteEquivalentXpaths(max_p);
			classe.setUniqueXPaths(senzaEquivalenti);
		}
		System.out.println("");
	}

	public Set<XPath> deleteEquivalentXpaths(int max_P) throws XPathExpressionException{
		VectorRepository container = new VectorRepository(this,max_P,this.getId());
		//int index=1;
		for (XPath rule : this.getxPaths()) {
			//System.out.println("controllando xpath "+index+ " di "+this.getxPaths().size());
			container.addUnique(rule);
			//index++;
		}
		this.vr = container;
		return container.getXPaths();
	}
	public static void selectCharacteristicXPath(Set<PageClass> pageClasses) {
		for(PageClass pageClass : pageClasses) {
			final Set<XPath> rules = pageClass.getUniqueXPaths();
			XPath bestXPath = null;

			if (!rules.isEmpty()) {
				final LinkedList<XPath> ranking = new LinkedList<XPath>(rules);
				/* order by XPath expression length */
				ranking.sort(Comparator.<XPath>comparingInt( xpath -> xpath.getRule().length() )
						.thenComparing( xpath -> xpath.toString() )
						);
				bestXPath = ranking.getFirst();
			}
			else
				System.out.println("Nessun xPath caratteristico trovato per la classe di pagine"+ pageClass.getId());
			pageClass.setCharacteristicXPath(bestXPath);
		}

	}

	/*public static void dfp(Set<PageClass> pageClasses) {

		/*final Experiment experiment = Experiment.makeExperiment("dfp", "");
        //XFPConfig.getInstance().setCurrentExperiment(experiment);
		for (PageClass pClass : pageClasses) {
			Set<Webpage> pages = new LinkedHashSet<>();
			List<String> strings = new LinkedList<>();
			for ( Page p : pClass.getPages()) {
				Webpage wp = new Webpage(URI.create(p.getUrl()));
				wp.setDocument(p.getDocument());
				strings.add(p.getUrl());
			}

			final ExperimentRunner runner = new ExperimentRunner();
			runner.run("a", "b",strings );
			xfp.fixpoint.PageClass<String> data = xfp.algorithm.FPAlgorithm.dfp().computeFixedPoints(pages);
			int constant = data.getConstant().size();
			int variant = data.getVariant().size();
			pClass.setFp(2*variant+constant);
		}
		String[] str = new String[4];
		str[0]="-d";
		str[1]="basiccases";
		str[2]="-s";
		str[3]="test";
		try {
			xfp.Main.main(str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error");
		}
	}*/


	public static void reorderClasses ( Set<PageClass> pageClasses) {
		int i = 0;
		for (PageClass pClass : pageClasses) {
			pClass.setId(Integer.toString(i));
			i++;
		}
	}

	private void setCharacteristicXPath(XPath bestXPath) {
		// TODO Auto-generated method stub
		this.characteristicXPath = bestXPath;
	}

	public void setFP(int constant, int variable) {
		this.constantFP= constant;
		this.variableFP = variable;
	}

	public static void executeDFP(Set<PageClass> pageClasses, String[] XFParguments, Set<Page> pages) {
		Map<FixedPoint<String>, PageClass> FP2PC = new HashMap<>();
		for(PageClass pc : pageClasses) {
			Map<String,String> id2name = new HashMap<>();
			//System.out.println("Pagine: "+ pages.size());
			for(Page p : pages) {
				if(pc.getPages().contains(p)) {
					String pageName = p.getUrl().split("/")[5];	
					String id = "idAP"+pageName.split(".html")[0];
					id2name.put(id, pageName);
				}
				else {
					String pageName = p.getUrl().split("/")[5];
					String id = "id"+pageName;
					id2name.put(id, pageName);
				}
			}
			Set<FixedPoint<String>> FixedPoints = null;
			try {
				FixedPoints = xfp.Main.DataMain(XFParguments,id2name);
			} catch (Exception e) {
				System.out.println("DFP failure");
			}
			for(FixedPoint<String> fp : FixedPoints) {
				FP2PC = addFixedPoints(FP2PC, fp, pc);
			}
		}
		//removeDuplicates(FP2PC);		
		addToPageClass(FP2PC);
	}

	private static void addToPageClass(Map<FixedPoint<String>, PageClass> fP2PC) {
		for (FixedPoint<String> fp : fP2PC.keySet()) {
			PageClass pc = fP2PC.get(fp);
			if (fp.isConstant()) {
				pc.setFP(pc.getConstantFP()+1, pc.getVariableFP());
			}
			else if (fp.isVariant()) {
				pc.setFP(pc.getConstantFP(), pc.getVariableFP()+1);
			}
		}

	}

	private static Map<FixedPoint<String>, PageClass> addFixedPoints(Map<FixedPoint<String>, PageClass> FP2PC,
			FixedPoint<String> fp, PageClass pc) {
		if (FP2PC.containsKey(fp)) {
			System.out.println("Trovate due uguali");
			PageClass other = FP2PC.get(fp);
			boolean contained = true;
			for (Page page : other.getPages()) {
				contained = contained && pc.getPages().contains(page);
			}
			if (contained) {
				FP2PC.remove(fp);
				FP2PC.put(fp, pc);
			}
		}
		else {
			FP2PC.put(fp, pc);
		}
		return FP2PC;
	}

	public static void executeNFP(Set<PageClass> pageClasses, String[] XFParguments, Set<Page> pages) {
		// TODO Auto-generated method stub
		// x ogni clasdipag vedo quale sono le possibili classidipag raggiungibili. me lo segno nella CdP stessa, magari una bella lista e vedo come comportarmi
		//se do una sistemata alla stampa in console potrei usare direttamente quella da portare al proff, senza mettermi sempre a riscrivere tutto!!

		for(PageClass pc : pageClasses) {
			System.out.println("Prima");
			Map<String,String> id2name = new HashMap<>();
			//System.out.println("Pagine: "+ pages.size());
			for(Page p : pages) {
				if(pc.getPages().contains(p)) {
					String pageName = p.getUrl().split("/")[5];	
					String id = "idAP"+pageName.split(".html")[0];
					id2name.put(id, pageName);
				}
				else {
					String pageName = p.getUrl().split("/")[5];
					String id = "id"+pageName;
					id2name.put(id, pageName);
				}
			}
			Map<Set<String>, int[]> NFP = new HashMap<>();
			try {
				NFP = xfp.Main.NavMain(XFParguments, id2name);
			} catch (Exception e) {
				System.out.println("NFP failure");
			}
			for(Set<String> pcID : NFP.keySet()) {
				PageClass pageClass = PageClass.getPageClassFromIDs(pageClasses,pcID);
				pc.addNFP(pageClass,NFP.get(pcID));
			}



		}
	}

	private void addNFP(PageClass pageClass, int[] nfp) {
		if(this.NFP == null) {
			this.NFP = new HashMap<>();
		}
		if(pageClass == null) {
			System.out.println("Una nulla");
		}
		else {
			this.NFP.put(pageClass, nfp);
		}

	}

	private static PageClass getPageClassFromIDs(Set<PageClass> pageClasses, Set<String> pcID) {
		
		for(PageClass pc : pageClasses) {
			if(pc.getPages().size() == pcID.size()) {
				Boolean equals = true;
				for (Page p : pc.getPages()) {
					if (!pcID.contains(p.getId() +".html")) {
						equals= false;
					}
				}
				if(equals) {
					return pc;
				}
			}
		}
		return null;
	}

	/*private static Map<PageClass, Set<FixedPoint<String>>> removeDuplicates(Map<PageClass, Set<FixedPoint<String>>> PC2FP) {
		for(PageClass pc : PC2FP.keySet()) {
			for (FixedPoint fp : PC2FP.get(pc) ) {
				System.out.println(fp);
			}
		}
		return null;
	}

	private Set<String> getXPathsAsString() {
		Set<String> xpaths = new HashSet<>();
		for ( XPath x : this.getxPaths()) {
			xpaths.add(x.getRule());
		}
		return xpaths;
	}*/
}
