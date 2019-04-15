package it.uniroma3.chixpath.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Lattice {
	

	private Set<Partition> partitions;
	private Map<Partition,Set<Partition>> isRefinementOf;
	private Map<Partition,Set<Partition>> isRefinedBy;
	
	public Lattice(Set<Partition> partitions) {
		this.partitions=partitions;
		this.isRefinedBy = new HashMap<Partition,Set<Partition>>();
		this.isRefinementOf=new HashMap<Partition,Set<Partition>>();
		this.GenerateRefinements();
		
	}
	
	private void GenerateRefinements() {
		for(Partition p1 : this.partitions) {
			for(Partition p2 : this.partitions) {
				if(p1.isRefinementOf(p2)) {
					this.addRefinement(p1,p2);
				}
			}
		}
	}
	
	
	
	private void addRefinement(Partition finer, Partition coarser) {
		if (this.isRefinementOf.containsKey(finer)) {
			Set<Partition> refined = this.isRefinementOf.get(finer);
			refined.add(coarser);
			this.isRefinementOf.put(finer, refined);
		}
		else {
			Set<Partition> refined = new HashSet<Partition>();
			refined.add(coarser);
			this.isRefinementOf.put(finer, refined);
		}
		if (this.isRefinedBy.containsKey(coarser)) {
			Set<Partition> refinements = this.isRefinedBy.get(coarser);
			refinements.add(finer);
			this.getIsRefinedBy().put(coarser, refinements);
		}
		else {
			Set<Partition> refinements = new HashSet<Partition>();
			refinements.add(finer);
			this.getIsRefinedBy().put(coarser, refinements);
		}
		
	}
	
	@Override
	public String toString() {
		String out = "Il reticolo è composto da: \n";
		for (Partition p : this.partitions) {
			
			out = out.concat(p.toString());
			out = out.concat("\n");
			
		}
		for (Partition p : this.isRefinementOf.keySet()) {
			out = out.concat("La partizione " + p.getId() + " è raffinamento di: ");
			for (Partition p2 : this.isRefinementOf.get(p)) {
				out = out.concat(" "+p2.getId());
			}
			out = out.concat("\n");
		}
		return out;
	}

	public Set<Partition> getPartitions() {
		return partitions;
	}

	public Map<Partition, Set<Partition>> getIsRefinementOf() {
		return isRefinementOf;
	}

	public Map<Partition, Set<Partition>> getIsRefinedBy() {
		return isRefinedBy;
	}
}
