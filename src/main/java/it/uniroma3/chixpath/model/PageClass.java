package it.uniroma3.chixpath.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

public class PageClass implements Comparable<PageClass> {
	private Set<String> xPaths = new HashSet<>();
	
	private Set<Page> pages= new HashSet<>();
	
	private String id;
	
	private Set<String> uniqueXPaths;
	
	private String characteristicXPath;
	
    private static int progId=0;

	public PageClass(){
		this.setId(Integer.toString(progId++));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getxPaths() {
		return xPaths;
	}

	public void setxPaths(Set<String> xPaths) {
		this.xPaths = xPaths;
	}

	public Set<Page> getPages() {
		return pages;
	}

	public void setPages(Set<Page> pages) {
		this.pages = pages;
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
	    
	    
	    public void print() {
	    	System.out.print("Classe di pagine "+this.getId()+" ha "+this.getxPaths().size()+" xpaths che matchano con le pagine");
			for(Page page : this.getPages()) {
				System.out.print("  ID:"+page.getId()+" ");
			}	    	
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
			return containsId;
	    }
	    
	    public boolean containsXpathsSet(ArrayList<PageClass> classi) {
	    	for(int i=0;i<classi.size();i++) {
	    		if(this.getxPaths().equals(classi.get(i).getxPaths())) return true;
	    	}
	    	return false;
	    }

		public Set<String> getUniqueXPaths() {
			return uniqueXPaths;
		}

		public void setUniqueXPaths(Set<String> uniqueXPaths) {
			this.uniqueXPaths = uniqueXPaths;
		}

		public String getCharacteristicXPath() {
			return characteristicXPath;
		}

		public void setCharacteristicXPath(String characteristicXPath) {
			this.characteristicXPath = characteristicXPath;
		}
		public static void createUniqueXPaths(ArrayList<PageClass> pageClasses,int max_p) throws XPathExpressionException {
			for(PageClass classe : pageClasses) {
				final Set<String> senzaEquivalenti = classe.deleteEquivalentXpaths(max_p);
				classe.setUniqueXPaths(senzaEquivalenti);
				//System.out.println("Classe di pagine "+classe.getId()+" matcha con "+senzaEquivalenti.size()+" xpaths");
			}
			System.out.println("");
		}

		public Set<String> deleteEquivalentXpaths(int max_P) throws XPathExpressionException{
				VectorRepository container = new VectorRepository(this,max_P,this.getId());
				int index=1;
				for (String rule : this.getxPaths()) {
						System.out.println("controllando xpath "+index+ " di "+this.getxPaths().size());
						container.addUnique(rule);
						index++;
				}
				return container.getXPaths();
		}
		public static void selectCharacteristicXPath(ArrayList<PageClass> pageClasses) {
			for(PageClass pageClass : pageClasses) {
				final Set<String> rules = pageClass.getUniqueXPaths();
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
					System.out.println("Nessun xPath caratteristico trovato per la classe di pagine"+ pageClass.getId());
				pageClass.setCharacteristicXPath(bestXPath);
			}
			
		}
	    
	
}
