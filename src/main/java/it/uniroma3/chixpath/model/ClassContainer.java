package it.uniroma3.chixpath.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassContainer implements Comparable<ClassContainer> {

	private static int progId=0;
	private Set<PageClass>  pClasses;
	private String id;

	public ClassContainer() {
		this.setId(Integer.toString(progId++));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<PageClass> getpClasses() {
		return pClasses;
	}

	public void setpClasses(Set<PageClass> classes) {
		this.pClasses = classes;
	}

	public boolean samePartition(ClassContainer partition) {
		boolean stesse= false;
		int index=0;
		if(this.getpClasses().size()==partition.getpClasses().size()) {
			String[] samePartitions = new String[this.getpClasses().size()];
			for(PageClass classe : this.getpClasses()) {
				for(PageClass toCheck : partition.getpClasses()) {
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
	public int compareTo(ClassContainer that) {
		return this.getId().compareTo(that.getId());
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		final ClassContainer that = (ClassContainer)o;
		return this.samePartition(that);
	}


	//ciclo for, se i=indicedaeliminare vai avanti sennò aggiungi al nuovo array list
	public ArrayList<ClassContainer> deleteDuplicates(ArrayList<ClassContainer> partitions){
		final ArrayList<ClassContainer> deletePartitions = new ArrayList<>();

		final Set<ClassContainer> alreadyChecked = new HashSet<>();
		final Set<String> deleteIds = new HashSet<>();

		for(ClassContainer i : partitions) {
			for(ClassContainer j :partitions) {
				if(i.samePartition(j) && !(alreadyChecked.contains(j))) {
					deleteIds.add(j.getId());
				}
			}
			alreadyChecked.add(i);
		}

		final ArrayList<ClassContainer> noDuplicate = new ArrayList<>();
		for(ClassContainer toDelete : partitions) {
			if(!deleteIds.contains(toDelete.getId())) noDuplicate.add(toDelete);
		}

		return noDuplicate;

	}

	/*
	 * CONTROLLO SE LA PARTIZIONE SULLA QUALE CHIAMO IL METODO E' UN RAFFINAMENTO DELL'ARGOMENTO
	 */
	/*public boolean isRaffinamentoDi(InsiemeDiClassi i,int n_pagine) {
			final Set<String> differentClasses = new HashSet<>();

			final String[] id = new String[n_pagine];
			for(ClasseDiPagine classe : this.getClassi()) {
				differentClasses.add(classe.getId());
				for(Page p : classe.getPages()) {
					id[Integer.parseInt(p.getId())]=classe.getId();
				}
			}

			final String[] id1 = new String[n_pagine];

			for(ClasseDiPagine classe : i.getClassi()) {
				for(Page p : classe.getPages()) {
					id1[Integer.parseInt(p.getId())]=classe.getId();
				}
			}

			for(int j=0;j<id.length;j++) {

			}



			return false;
		}*/

	public boolean isRefinementOf(ClassContainer i,int n_pagine) {
		boolean isRefinement=true;
		final Map<Set<Page>,String> p1 = new HashMap<>();

		final Map<Set<Page>,String> p2 = new HashMap<>();

		//creo 2 mappe che contengono le pagine divise come sono divise nelle partizioni in input
		for(PageClass classe : this.getpClasses()) {
			final Set<Page> pages = new HashSet<>();
			pages.addAll(classe.getPages());
			p1.put(pages, classe.getId());
		}

		for(PageClass classe2 : i.getpClasses()) {
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

	public void print() {
		System.out.println("La partizione "+this.getId()+" e' divisa in: ");
		for(PageClass Pset : this.getpClasses()) {
			System.out.println("Classe di pagine "+Pset.getId()+":");
			for(Page page : Pset.getPages()) {
				System.out.println("id:"+page.getId()+" ");
			}
		}
	}
}
