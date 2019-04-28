package it.uniroma3.chixpath.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Partition implements Comparable<Partition> {

	private static int progId=0;
	private Set<PageClass>  pageClasses;
	private String id;
	private float avgXPaths;
	private float avgTfIdf;

	

	public Partition(Set<PageClass> pc) {
		this.pageClasses = pc;
		this.id = (Integer.toString(progId++));
		this.avgXPaths = averageXPaths();
		this.avgTfIdf = averageTfIdf();
	}

	private float averageTfIdf() {
		float sum = 0;
		for (PageClass pc : this.getPageClasses()) {
			sum = sum+pc.tfIdf();
		}
		return ( sum/this.pageClasses.size());
	}

	public String getId() {
		return id;
	}


	public Set<PageClass> getPageClasses() {
		return pageClasses;
	}

	private float averageXPaths() {
		float sum = 0;
		for (PageClass page : this.pageClasses) {
			sum=sum+page.getUniqueXPaths().size();
		}
		return (sum/this.pageClasses.size());
	}

	public boolean samePartition(Partition partition) {
		boolean stesse= false;
		int index=0;
		if(this.getPageClasses().size()==partition.getPageClasses().size()) {
			String[] samePartitions = new String[this.getPageClasses().size()];
			for(PageClass classe : this.getPageClasses()) {
				for(PageClass toCheck : partition.getPageClasses()) {
					if(classe.getId().equals(toCheck.getId()))
						samePartitions[index]="1";
				}
				index++;
			}

			for(int j=0;j<samePartitions.length;j++) {
				if(samePartitions[j]==null)  stesse=false;

				else if(samePartitions[j]=="1" && j==samePartitions.length-1) return stesse=true;
			}
		}

		return stesse;
	}

	@Override
	public int compareTo(Partition that) {
		return this.getId().compareTo(that.getId());
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		final Partition that = (Partition)o;
		return this.samePartition(that);
	}


	//ciclo for, se i=indicedaeliminare vai avanti sennò aggiungi al nuovo array list
	public static Set<Partition> deleteDuplicates(Set<Partition> partitions){

		final Set<Partition> alreadyChecked = new HashSet<>();
		final Set<String> deleteIds = new HashSet<>();

		for(Partition i : partitions) {
			for(Partition j :partitions) {
				if (!i.getId().equals(j.getId())) {
					if(i.samePartition(j) && !(alreadyChecked.contains(j))) {
						deleteIds.add(j.getId());
					}
				}
			}
			alreadyChecked.add(i);
		}

		final Set<Partition> noDuplicate = new HashSet<>();
		for(Partition toDelete : partitions) {
			if(!deleteIds.contains(toDelete.getId())) noDuplicate.add(toDelete);
		}

		return noDuplicate;

	}



	public boolean isRefinementOf(Partition i) {
		boolean isRefinement=true;
		final Map<Set<Page>,String> p1 = new HashMap<>();

		final Map<Set<Page>,String> p2 = new HashMap<>();

		//creo 2 mappe che contengono le pagine divise come sono divise nelle partizioni in input
		for(PageClass classe : this.getPageClasses()) {
			final Set<Page> pages = new HashSet<>();
			pages.addAll(classe.getPages());
			p1.put(pages, classe.getId());
		}

		for(PageClass classe2 : i.getPageClasses()) {
			final Set<Page> pages2 = new HashSet<>();
			pages2.addAll(classe2.getPages());
			p2.put(pages2, classe2.getId());
		}

		if(!this.samePartition(i)) {
			//itero su tutti i raggruppamenti di pagine della partizione "principale" e per ognuno di questi creo un insieme di IDPAGINA per effettuare i controlli
			for(Set<Page> pages2 : p2.keySet()) {
				final Set<String> idsToCheck2 = new HashSet<>();
				for(Page temp : pages2) {
					idsToCheck2.add(temp.getId());
				}
				//itero su tutti i raggruppamenti della partizione che vogliamo sapere se è raffinamento della principale e creo un insieme di IDPAGINA
				for(Set<Page> pages1 : p1.keySet()) {
					final Set<String> idsToCheck1 = new HashSet<>();
					for(Page temp : pages1) {
						idsToCheck1.add(temp.getId());
					}

					//per ogni IDPAGINA del raggruppamento principale, il corrispettivo IDPAGINA della partizione da controllare e il raggruppamento a cui appartiene devono rispettare una condizione
					for(String id : idsToCheck2) {
						/*se un raggruppamento della partizione da controllare contiene un id della partizione principale,
						 *  quest'ultima deve contenere TUTTI gli id del raggruppamento della partizione da controllare
						//in modo da essere un raffinamento, se ciò non fosse(il raggruppamento della partiz. principale non contiene tutti gli IDPAGINA della partiz. da controllare)
						 *  allora NON è un raffinamento.
						 */
						if(idsToCheck1.contains(id) && !idsToCheck2.containsAll(idsToCheck1)) {
							return isRefinement=false;
						}
					}
				}

			}
		}
		else isRefinement=false;

		return isRefinement;

	}

	public static void reorderPartitions ( Set<Partition> partitions) {
		int i = 0;
		for (Partition partition : partitions) {
			partition.setId(Integer.toString(i));
			i++;
		}
	}

	private void setId(String id) {
		this.id  = id;
		
	}
	public float getAvgXPaths() {
		return avgXPaths;
	}

	@Override
	public String toString() {
		String str = "La partizione "+this.getId()+" matcha con "+this.getAvgXPaths() +" xpaths in media e ha un TfIdf medio di "+ this.averageTfIdf() +  " ed e' divisa in:\n";
		for(PageClass Pset : this.getPageClasses()) {
			str=str.concat("Classe di pagine "+Pset.getId()+":\n");
			for(Page page : Pset.getPages()) {
				str=str.concat("id:"+page.getId()+"\n");
			}
		}
		return str;
	}
}
