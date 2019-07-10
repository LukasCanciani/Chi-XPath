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
	private Map<Set<String>,int[]> FixedPoints;
	private int[] totalDFP;
	private int[] totalOptionalDFP;
	private int[] innerNFP;
	private int[] innerOptionalNFP;
	private float rank;
	

	//CouldBeChanged
	private float ranking() {
		this.totalDFP = countTotalDFP();
		this.totalOptionalDFP = countTotalOptionalDFP();
		return (float) (this.totalDFP[0]*2 + this.totalDFP[1])/this.pageClasses.size();
	}

	public int[] getInnerNFP() {
		return innerNFP;
	}

	public Map<Set<String>, int[]> getFixedPoints() {
		return FixedPoints;
	}

	public void setFixedPoints(Map<Set<String>, int[]> fixedPoints) {
		FixedPoints = fixedPoints;
	}

	public Partition(Set<PageClass> pc) {
		this.pageClasses = pc;
		this.id = (Integer.toString(progId++));
		this.avgXPaths = averageXPaths();
		
		int[] NFP = countInnerNFP();
		//this.innerNFP = countInnerNFP();
		int[] inNFP = new int[2];
		inNFP[0] = NFP[0];
		inNFP[1] = NFP[1];
		this.innerNFP = inNFP;
		int[] inOpNFP = new int[2];
		inOpNFP[0] = NFP[2];
		inOpNFP[1] = NFP[3];
		this.innerOptionalNFP = inOpNFP;
		this.rank=0;
		
		
	}


	private int[] countInnerNFP() {
		int[] total = new int[4];
		for (PageClass p: this.pageClasses) {
			for(PageClass p2 : p.getNFP().keySet()) {
				if (this.pageClasses.contains(p2)) {
					int[] current = p.getNFP().get(p2);
					total[0] += current[0];
					total[1] += current[1];
					total[2] += current[2];
					total[3] += current[3];
				}
			}
		}
		return total;
	}

	private int[] countTotalDFP() {
		int[] total= new int[2];
		for(PageClass p : this.pageClasses) {
			total[0] = total[0] + p.getVariableFP();
			total[1] = total[1] + p.getConstantFP();
		}
		return total;
	}
	
	private int[] countTotalOptionalDFP() {
		int[] total= new int[2];
		for(PageClass p : this.pageClasses) {
			total[0] = total[0] + p.getVariableOptional();
			total[1] = total[1] + p.getConstantOptional();
		}
		return total;
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
		String str = "Partition "+this.getId() +"\nPage Classes: ";
		for(PageClass Pset : this.getPageClasses()) {
			str=str.concat(Pset.getId()+" ");
		}
		str=str.concat("Average XPaths: " + this.getAvgXPaths() 
						+ " \nNFP: Constant: " + this.getInnerNFP()[1]+ " Variable: " + this.getInnerNFP()[0] 
							+ "(total: " + (this.getInnerNFP()[0]+this.getInnerNFP()[1])+ " )")
				+ "\nOptionalNFP: OptionalConstant: " + this.getInnerOptionalNFP()[1]+ " OptionalVariable: " + this.getInnerOptionalNFP()[0] 
						+ "(OptionalTotal: " + (this.getInnerOptionalNFP()[1]+this.getInnerOptionalNFP()[0])+ " )";
		if(this.getRank() != 0 ) {
			str=str.concat(" Ranking: "+ this.getRank() +" \nDFP:  Constant: " + this.getTotalDFP()[1]+ " Variable: " + this.getTotalDFP()[0] 
					+ "(total: " + (this.getTotalDFP()[0]+this.getTotalDFP()[1])+ " )"
							+ " OptionalDFP:  OptConstant: " + this.getTotalOptionalDFP()[1]+ " OptVariable: " + this.getTotalOptionalDFP()[0] 
									+ "(OptTotal: " + (this.getTotalOptionalDFP()[0]+this.getTotalOptionalDFP()[1])+ " )");
		}
		return str;
	}

	/*public void executeXFP(String[] XFParguments, Set<Page> AP, Set<Page> pages) {
		Map<String,String> id2name = new HashMap<>();
		for(PageClass pc : this.getPageClasses()) {
			if(Collections.disjoint(pc.getPages(), AP)) {
				for(Page p : pc.getPages()) {
					String pageName = p.getUrl().split("/")[5];
					
					String id = "id"+pageName;
					id2name.put(id, pageName);
					
				}
			}else {
				for(Page p : pc.getPages()) {
					String pageName = p.getUrl().split("/")[5];
					
					String id = "idAP"+pageName.split(".html")[0];
					id2name.put(id, pageName);
					
				}
			}
		}

		Map<Set<String>,int[]> FixedPoints = null;
		try {
			FixedPoints = xfp.Main.NavMain(XFParguments,id2name);
		} catch (Exception e) {
			System.out.println("DFP failure");
		}
		this.setFixedPoints(FixedPoints);
		int tot = 0;
		for(Set<String> ss : FixedPoints.keySet()) {
			for(PageClass pc : this.getPageClasses()) {
				if (pc.getPagesNames().containsAll(ss) && ss.containsAll(pc.getPagesNames())) {
					pc.setFP(FixedPoints.get(ss)[0], FixedPoints.get(ss)[1]);
					tot = tot +FixedPoints.get(ss)[0]+FixedPoints.get(ss)[1];
				}
			}
		}
		this.totalFP = tot;
		

	}

	public int getTotalFP() {
		return totalFP;
	}*/

	/*public static void executeXFP(Set<Partition> partitions, Set<String> APIds) {
		for(Partition p : partitions) {
			Map<Set<String>,int[]> FixedPoints = null;
			String[] arguments = new String[6];
			arguments[0] = "-d";
			arguments[1] = "autoscout";
			arguments[2] = "-s";
			arguments[3] = "test";
			arguments[4] = "-w";
			arguments[5] = "1ap3car";
			try {
				FixedPoints = xfp.Main.chiMain(arguments);
			} catch (Exception e) {
				System.out.println("DFP failure");
			}
			p.setFixedPoints(FixedPoints);
		}
	}*/

	public int[] getTotalDFP() {
		return totalDFP;
	}

	public int[] getTotalOptionalDFP() {
		return totalOptionalDFP;
	}

	public int[] getInnerOptionalNFP() {
		return innerOptionalNFP;
	}

	public float getRank() {
		if(this.rank==0) 
			this.rank = ranking();
			
		return this.rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}
