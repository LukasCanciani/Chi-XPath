package it.uniroma3.chixpath.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InsiemeDiClassi implements Comparable<InsiemeDiClassi> {

	private static int progId=0;
	private Set<ClasseDiPagine>  classi;
	private String id;

	public InsiemeDiClassi() {
		this.setId(Integer.toString(progId++));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<ClasseDiPagine> getClassi() {
		return classi;
	}

	public void setClassi(Set<ClasseDiPagine> classi) {
		this.classi = classi;
	}

	public boolean stessaPartizione(InsiemeDiClassi partizione) {
		boolean stesse= false;
		int index=0;
		if(this.getClassi().size()==partizione.getClassi().size()) {
			String[] samePartitions = new String[this.getClassi().size()];
			for(ClasseDiPagine classe : this.getClassi()) {
				for(ClasseDiPagine toCheck : partizione.getClassi()) {
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
	public int compareTo(InsiemeDiClassi that) {
		return this.getId().compareTo(that.getId());
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		final InsiemeDiClassi that = (InsiemeDiClassi)o;
		return this.stessaPartizione(that);
	}


	//ciclo for, se i=indicedaeliminare vai avanti sennò aggiungi al nuovo array list
	public ArrayList<InsiemeDiClassi> deleteDuplicates(ArrayList<InsiemeDiClassi> partizioni){
		final ArrayList<InsiemeDiClassi> partizioniDaEliminare = new ArrayList<>();

		final Set<InsiemeDiClassi> alreadyChecked = new HashSet<>();
		final Set<String> idDaEliminare = new HashSet<>();

		for(InsiemeDiClassi i : partizioni) {
			for(InsiemeDiClassi j :partizioni) {
				if(i.stessaPartizione(j) && !(alreadyChecked.contains(j))) {
					idDaEliminare.add(j.getId());
				}
			}
			alreadyChecked.add(i);
		}

		final ArrayList<InsiemeDiClassi> senzaDuplicati = new ArrayList<>();
		for(InsiemeDiClassi toDelete : partizioni) {
			if(!idDaEliminare.contains(toDelete.getId())) senzaDuplicati.add(toDelete);
		}

		return senzaDuplicati;

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

	public boolean isRaffinamentoDi(InsiemeDiClassi i,int n_pagine) {
		boolean isRaffinamento=true;
		final Map<Set<Page>,String> p1 = new HashMap<>();

		final Map<Set<Page>,String> p2 = new HashMap<>();

		//creo 2 mappe che contengono le pagine divise come sono divise nelle partizioni in input
		for(ClasseDiPagine classe : this.getClassi()) {
			final Set<Page> pages = new HashSet<>();
			pages.addAll(classe.getPages());
			p1.put(pages, classe.getId());
		}

		for(ClasseDiPagine classe2 : i.getClassi()) {
			final Set<Page> pages2 = new HashSet<>();
			pages2.addAll(classe2.getPages());
			p2.put(pages2, classe2.getId());
		}

		if(!this.stessaPartizione(i)) {
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
							return isRaffinamento=false;
						}
					}
				}

			}
		}
		else isRaffinamento=false;

		return isRaffinamento;

	}

	public void stampa() {
		System.out.println("La partizione "+this.getId()+" e' divisa in: ");
		for(ClasseDiPagine Pset : this.getClassi()) {
			System.out.println("Classe di pagine "+Pset.getId()+":");
			for(Page page : Pset.getPages()) {
				System.out.println("id:"+page.getId()+" ");
			}
		}
	}
}
