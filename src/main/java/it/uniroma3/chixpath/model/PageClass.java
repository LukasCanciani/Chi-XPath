package it.uniroma3.chixpath.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

public class PageClass implements Comparable<PageClass> {
	private Set<XPath> xpaths = new HashSet<>();

	private Set<Page> pages= new HashSet<>();

	private String id;

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
		return this.getxPaths().equals(that.getxPaths());
	}


	public String toString() {
		String out = "Classe di pagine "+this.getId()+" ha "+this.getxPaths().size()+" xpaths che matchano con le pagine";
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
		int index=1;
		for (XPath rule : this.getxPaths()) {
			System.out.println("controllando xpath "+index+ " di "+this.getxPaths().size());
			container.addUnique(rule);
			index++;
		}
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
