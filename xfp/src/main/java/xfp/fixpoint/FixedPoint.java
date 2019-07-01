package xfp.fixpoint;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import xfp.extraction.ExtractionRule;
import xfp.model.ExtractedVector;
import xfp.model.Value;

public class FixedPoint<T> implements Comparable<FixedPoint<T>> {

	private ExtractedVector<T> extracted;
	
	private Lattice<T> lattice;
	
	@SuppressWarnings("unused")
	private Set<String> rules; // String -> ExtractionRules //TODO?
	
	public FixedPoint(ExtractedVector<T> ev, Set<String> intersection) {
	    this.extracted = ev;
	    this.rules = intersection;
	}
	
	public ExtractionRule<T> getExtractionRule() {
		return this.extracted.getExtractionRule();
	}
	
	public boolean isConstant() {
		return this.extracted.isConstant();
	}

	public boolean isVariant() {
		return !this.extracted.isConstant();
	}

	public ExtractedVector<T> getExtracted() {
		return this.extracted;
	}

	public void setExtracted(ExtractedVector<T> extracted) {
		this.extracted = extracted;
	}

	public Lattice<T> getLattice() {
		return this.lattice;
	}

	public void setLattice(Lattice<T> lattice) {
		this.lattice = lattice;
	}
	
	@Override
	public int hashCode() {
	    return Objects.hashCode(this.getExtracted());
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o==null || !(o instanceof FixedPoint))
	        return false;
	    /* only the extracted vector matters, no rule */
	    @SuppressWarnings("unchecked")
        final FixedPoint<T> that = (FixedPoint<T>)o;
	    for(String rule: that.getRules()) {
	    	if( this.getRules().contains(rule)) {
	    		return true;
	    	}
	    }
	    return Objects.equals(this.getExtracted(), that.getExtracted());
	}
	
	@Override
	public String toString() {
	    return this.getClass().getSimpleName()+" over "+this.getExtractionRule();
	}

    @Override
    public int compareTo(FixedPoint<T> that) {
        long cmp = this.extracted.size()-that.getExtracted().size();
        if (cmp==0) 
            cmp = this.getExtractionRule().getXPath().compareTo(that.getExtractionRule().getXPath());
        return (int)cmp;
    }

	public Set<String> getRules() {
		return this.rules;
	}


	public boolean isOptional() {
		for(List<Value<T>> list : this.getExtracted().getValues()) {
			if(list.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
}
