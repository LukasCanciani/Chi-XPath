package it.uniroma3.chixpath.model;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ClasseDiPagine implements Comparable<ClasseDiPagine> {
	private Set<String> xPaths;
	
	private Set<Page> pages= new HashSet<>();
	
	private String id;
	
    private static int progId=0;

	public ClasseDiPagine(){
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
	public int compareTo(ClasseDiPagine that) {
        return this.getId().compareTo(that.getId());
	}
	
	 @Override
	    public int hashCode() {
	        return this.id.hashCode();
	    }
	    
	    @Override
	    public boolean equals(Object o) {
	        final ClasseDiPagine that = (ClasseDiPagine)o;
	        return this.getxPaths().equals(that.getxPaths());
	    }
	    
	    
	    public void stampa() {
	    	System.out.print("Classe di pagine "+this.getId()+" ha "+this.getxPaths().size()+" xpaths che matchano con le pagine");
			for(Page page : this.getPages()) {
				System.out.print("  ID:"+page.getId()+" ");
			}	    	
	    }
	    
	    public boolean haPagineUgualiA(Set<ClasseDiPagine> toAdd) {
	    	boolean contieneId=false;
			final Set<String> idsToCheck = new HashSet<>();
			//creo l'insieme di id da controllare che NON SIANO nel mio insieme di insiemi di pagine
			for(Page page : this.getPages()) {
				idsToCheck.add(page.getId());
			}

			//per ogni singolo insieme che fa parte dell'insieme di regole creo un insieme di id
			for(ClasseDiPagine test : toAdd){
				for(Page page : test.getPages()) {
					Set<String> ids = new HashSet<>();
						ids.add(page.getId());
						
					//controllo che nessun id del primo set sia nel set da controllare
					for(String id : idsToCheck ) {
						if(ids.contains(id)) {
							contieneId=true;
							return contieneId;
						}
					}
				}
			}
			return contieneId;
	    }
	    
	    public boolean contieneInsiemeDiXpaths(ArrayList<ClasseDiPagine> classi) {
	    	for(int i=0;i<classi.size();i++) {
	    		if(this.getxPaths().equals(classi.get(i).getxPaths())) return true;
	    	}
	    	return false;
	    }
	    
	
}
