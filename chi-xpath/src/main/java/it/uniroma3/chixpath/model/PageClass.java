package it.uniroma3.chixpath.model;


import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import xfp.ExperimentRunner;
import xfp.model.Experiment;
import xfp.model.Webpage;
import xfp.util.XFPConfig;

public class PageClass implements Comparable<PageClass> {
	private Set<XPath> xpaths = new HashSet<>();

	private Set<Page> pages= new HashSet<>();

	private String id;

	private VectorRepository vr;

	public VectorRepository getVr() {
		return vr;
	}

	private void setId(String id) {
		this.id = id;
	}

	private Set<XPath> uniqueXPaths;

	private XPath characteristicXPath;

	private static int progId=0;

	private int fp; //fixep points

	public int getFp() {
		return fp;
	}

	public void setFp(int fp) {
		this.fp = fp;
	}

	public PageClass(Set<Page> pages, Set<XPath> xpaths){
		this.id = (Integer.toString(progId++));
		this.pages = pages;
		this.xpaths = xpaths;
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
		String out = "Classe di pagine "+this.getId()+" ha "+this.getUniqueXPaths().size()+" xpaths univoci che matchano con le pagine";
		for(Page page : this.getPages()) {
			out.concat("  ID:"+page.getId()+" ");
		}	
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


}
