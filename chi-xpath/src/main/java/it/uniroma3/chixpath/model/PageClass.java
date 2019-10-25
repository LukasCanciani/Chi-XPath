package it.uniroma3.chixpath.model;

import java.util.Collections;
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
	private Set<String> pagesID;
	private Set<String> pagesURL;


	private VectorRepository vr;
	private int variableFP;
	private int constantFP;
	private int variableOptional;
	private int constantOptional;
	private Map<Set<PageClass>,int[]> NFP;

	public Set<String> rules;

	public Map<Set<PageClass>, int[]> getNFP() {
		return NFP;
	}

	public Set<String> getPagesURL(){
		return this.pagesURL;
	}

	public Set<String> getPagesNames() {
		return pagesNames;
	}

	public Set<String> getPagesID(){
		return this.pagesID;
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
		this.rules = new HashSet<>();
		this.pagesID = new HashSet<>();
		this.pagesURL = new HashSet<>();
		if (pages!=null) {
			for (Page p : pages) {
				pagesNames.add(p.getUrl().split("/")[5]);
				rules.addAll(p.dataRules);
				pagesID.add(p.getId());
				pagesURL.add(p.getUrl());
			}
		}
		this.uniqueXPaths = xpaths;
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
		if(that != null) {
			if (this.getPages().size() == that.getPages().size()){
				for(Page p : this.getPages()) {
					if(!that.getPages().contains(p)) 
						return false;
				}
				return true;
			}else {
				return false;
			}
		}else
			return false;
	}


	public String toString() {
		String out = "PageClass "+this.getId()+ " Pages: \n";
		for(Page page : this.getPages()) {
			out=out.concat("ID:"+page.getId()+" ");
		}	
		out=out.concat("\nXPaths: "+this.getUniqueXPaths().size());//+"\nDFP: Constant:"+this.getConstantFP() + " Variable: "+this.getVariableFP());
		//out=out.concat("\nOptionalDFP: Constant:"+this.getConstantOptional() + " Variable: "+this.getVariableOptional());
		out=out.concat("\nNFP: ");
		if(this.getNFP().keySet().isEmpty()) {
			out = out.concat(" N/A");
		}
		else {
			for(Set<PageClass> pcs : this.getNFP().keySet()) {
				out=out.concat("\nTo Pageclass: ");
				for(PageClass pc : pcs) {
					out = out.concat(pc.getId() + " ");
				}
				out = out.concat(" NFP: Constant: "+this.getNFP().get(pcs)[1] + " Variable: "+this.getNFP().get(pcs)[0]) +
						" (OptionalConstant: "+this.getNFP().get(pcs)[3] +" OptionalVariable: "+this.getNFP().get(pcs)[2] +")";
			}
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
		for(PageClass pClass : pageClasses) {
			final Set<XPath> senzaEquivalenti = pClass.deleteEquivalentXpaths(max_p);
			pClass.setUniqueXPaths(senzaEquivalenti);		

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
		//Se non riserve, evito memoria!!
		//this.vr = container;
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



	/*public static Set<PageClass> removeNFP(Set<PageClass> pClasses) {
		Set<PageClass> toKeep = new HashSet<>();
		for (PageClass pc : pClasses) {
			if(pc.getPages().size() != 1) {
			toKeep.add(pc);
			}
		}
		return toKeep;
	}	*/

	private void setCharacteristicXPath(XPath bestXPath) {
		// TODO Auto-generated method stub
		this.characteristicXPath = bestXPath;
	}

	public void setFP(int constant, int variable) {
		this.constantFP= constant;
		this.variableFP = variable;
	}
	public void setOptionalFP(int constant, int variable) {
		this.constantOptional = constant;
		this.variableOptional = variable;
	}

	public static void executeDFP(Set<PageClass> pageClasses, String[] XFParguments, Set<Page> pages, Set<FixedPoint<String>> siteDFP, int range) {

		Map<FixedPoint<String>, PageClass> FP2PC = new HashMap<>();
		for(PageClass pc : pageClasses) {

			Set<FixedPoint<String>> FixedPoints = null;
			try {
				FixedPoints = DFP(XFParguments,pc,pages,range);
			} catch (Exception e) {
				System.out.println("DFP failure");
			}
			for(FixedPoint<String> fp : FixedPoints) {
				if(!siteDFP.contains(fp)) {
					FP2PC = addFixedPoints(FP2PC, fp, pc);
				}
			}
		}
		//removeDuplicates(FP2PC);		
		addToPageClass(FP2PC);
	}

	public static Set<FixedPoint<String>> DFP(String[] XFParguments, PageClass pageClass, Set<Page> pages, int range) throws Exception{
		Map<String,String> id2name = new HashMap<>();
		//System.out.println("Pagine: "+ pages.size());
		for(Page p : pages) {
			if(pageClass.getPages().contains(p)) {
				String pageName = p.getUrl().split("/")[5];	
				String id = "idAP"+pageName.split(".html")[0];
				id2name.put(id, pageName);
			}
		}
		return xfp.Main.DataMain(XFParguments,id2name,pageClass.rules,range);
	}

	public static Set<FixedPoint<String>> executeSiteDFP(Set<PageClass> pageClasses, String[] XFParguments, Set<Page> pages){
		Set<FixedPoint<String>> FixedPoints = new HashSet<>();
		for (PageClass pc : pageClasses) {
			if (pc.getPages().size()==pages.size()) {
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

				try {
					FixedPoints = xfp.Main.DataMain(XFParguments,id2name);
				} catch (Exception e) {
					System.out.println("DFP failure");
				}
			}
		}
		Set<FixedPoint<String>> results = new HashSet<>();
		for(FixedPoint<String> fp : FixedPoints) {
			if(fp.isConstant()) {
				results.add(fp);
			}
		}
		return results;
	}

	private static void addToPageClass(Map<FixedPoint<String>, PageClass> fP2PC) {
		for (FixedPoint<String> fp : fP2PC.keySet()) {
			PageClass pc = fP2PC.get(fp);
			if(fp.isOptional()) {
				if (fp.isConstant()) {
					pc.setOptionalFP(pc.getConstantOptional()+1, pc.getVariableOptional());
				}
				else if (fp.isVariant()) {
					pc.setOptionalFP(pc.getConstantOptional(), pc.getVariableOptional()+1);
				}
			}
			else {
				if (fp.isConstant()) {
					pc.setFP(pc.getConstantFP()+1, pc.getVariableFP());
				}
				else if (fp.isVariant()) {
					pc.setFP(pc.getConstantFP(), pc.getVariableFP()+1);
				}
			}
		}

	}

	private static Map<FixedPoint<String>, PageClass> addFixedPoints(Map<FixedPoint<String>, PageClass> FP2PC,
			FixedPoint<String> fp, PageClass pc) {
		if (FP2PC.containsKey(fp) ) {
			System.out.println("TROVATI DUE UGUALI");
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
			Map<Set<String>, int[]> NFP = null;

			try {
				NFP = NFP(XFParguments,pc,pages);
			} catch (Exception e) {
				System.out.println("Error : " + e);
			}
			if (NFP != null) {
				for(Set<String> pcIDpre : NFP.keySet()) {
					Set<String> pcID = new HashSet<>();
					Set<String> allPagesID = new HashSet<>();
					for (PageClass pc2: pageClasses) {
						allPagesID.addAll(pc2.getPagesId());
					}
					for(String s : pcIDpre) {
						if (!(s.isEmpty() || s==null) && allPagesID.contains(s.split(".html")[0])){
							pcID.add(s);
						}
					}
					Set<PageClass> pClasses = PageClass.getPageClassFromIDs(pageClasses,pcID);
					if(pClasses != null) {
						if(!pClasses.isEmpty()) {
							pClasses.remove(pc);
							int[] nfpCurrent = NFP.get(pcIDpre);
							for(int i = 0; i< 4; i++) {
								int temp = nfpCurrent[i];
								if(temp>0) {
									nfpCurrent[i] = (temp * pcID.size()) ;
								}
							}
							pc.addNFP(pClasses,nfpCurrent);
						}
					}
				}
			}
		}
	}


	private static Map<Set<String>, int[]> NFP(String[] XFParguments, PageClass pageClass, Set<Page> pages) throws Exception{
		Map<String,String> id2name = new HashMap<>();
		//System.out.println("Pagine: "+ pages.size());
		for(Page p : pages) {
			if(pageClass.getPages().contains(p)) {
				String pageName = p.getUrl().split("/")[5];	
				String id = "idAP"+pageName.split(".html")[0];
				id2name.put(id, pageName);
			}
		}
		return xfp.Main.NavMain(XFParguments, id2name);
	}

	private void addNFP(Set<PageClass> pageClass, int[] nfp ) {
		if(this.NFP == null) {
			this.NFP = new HashMap<>();
		}
		if(pageClass != null) 
		{
			this.NFP.put(pageClass, nfp);

		}

	}

	public static Set<PageClass> getPageClassFromIDs(Set<PageClass> pageClasses, Set<String> pcID) {
		Set<PageClass> result = new HashSet<>();

		for(PageClass pc : pageClasses) {
			if(pc.getPages().size() >= pcID.size()) {
				Boolean equals = true;
				for (String s : pcID) {
					if (!pc.getPagesId().contains(s.split(".html")[0])) {
						equals = false;
					}
				}
				if(equals) {
					result.add(pc);
				}
			}
		}
		if (result.isEmpty()) {
			return null;
		}
		return result;
	}




	private Set<String> getPagesId() {
		return this.pagesID;
	}

	public int getVariableOptional() {
		return variableOptional;
	}

	public int getConstantOptional() {
		return constantOptional;
	}

	public Set<String> getRules() {
		return rules;
	}

	public static Set<PageClass> addMissingSingleton(Set<PageClass> pClasses, Set<Page> allPages) {
		for(Page p : allPages) {
			boolean found = false;
			for(PageClass pc : pClasses) {
				if(pc.getPages().size() == 1) {
					if(pc.getPages().contains(p)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				Set<Page> pages = new HashSet<>();
				pages.add(p);
				PageClass pageClass = new PageClass(pages, Collections.emptySet());
				pClasses.add(pageClass);
			}
		}
		return pClasses;
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
