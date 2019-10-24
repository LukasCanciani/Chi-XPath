package xfp.fixpoint;

import java.util.*;
import java.util.stream.IntStream;

import xfp.extraction.ExtractionRule;
import xfp.model.ExtractedVector;
import xfp.model.Value;

/**
 * We will need to rewrite the extraction vector and value class as well:
 * the lattice implements only menu and list i.e. top and bottom
 * both are created at object instantiation level 
 * 
 * TODO generalize current 3-levels hacks to a real lattice based on what we extracted
 * 
 */
public class Lattice<T> {
	
    static public <T> Set<Lattice<T>> create(Set<FixedPoint<T>> fixedPoints) {
        final Set<Lattice<T>> result = new HashSet<>();
        final Iterator<FixedPoint<T>> it = fixedPoints.iterator();
        while (it.hasNext())    {
            final FixedPoint<T> fp = it.next();
            result.add(new Lattice<>(fp));
        }
        return result;
    }

	private FixedPoint<T> top;
	
	// top (list) -> menu -> bot (empty)
	private boolean atBot; // true iff at bot
	
	private boolean atTop; // true iff at top
	
	public Lattice(FixedPoint<T> top) {
		this.top = top;
		this.top.setLattice(this);
		this.atBot = false;
		this.atTop = true; 
	}

	public FixedPoint<T> top()	{
		return this.top;
	}
	
    /* available state transitions: top=list -> menu -> bot=empty-set */
	public Set<FixedPoint<T>> getGLBs(FixedPoint<T> fp)	{
	    if (this.atBot) return Collections.emptySet();
		if (this.atTop)	{
		    this.atTop = false;
		    return makeMenu(fp); // list -> menu (for costant number of menu entries)
		} else {
	        this.atBot = true;   // menu -> bot
	        return Collections.emptySet();
		}
	}

	private Set<FixedPoint<T>> makeMenu(FixedPoint<T> fp) {
        final ExtractedVector<T> vector = fp.getExtracted();
	    if ( vector.getValues().stream()
	            .map(l -> l.size()).distinct().count() > 1 ) {
	        // variable number of entries
	        // no menu to hope: just go bottom
            this.atBot = true;          
	        return Collections.emptySet();
	    }
	    
	    int n = vector.getValues().stream().findFirst().get().size();
	    
	    if (n < 2) {
	        // single column menu == single column list 
	        // just collapse menu and bottom levels
	        this.atBot = true;	        
	        return Collections.emptySet(); 
	    }
	    
	    final Set<FixedPoint<T>> result = new HashSet<>();

        IntStream.range(0, n).forEach( i -> {
            final ExtractedVector<T> menu = new ExtractedVector<>(new ExtractionRule<>(".",null));
            vector.getPages().stream().forEach( page -> {
    	        final List<Value<T>> values = vector.getValues(page);
    	        menu.add(page, Collections.singletonList(values.get(i)));
    	    });
            final FixedPoint<T> mfp = new FixedPoint<>(menu, null);
            mfp.setLattice(this);
            result.add(mfp);
        });
        
        return result;
    }

    public boolean isAtBottom() {
		return this.atBot;
	}

    public boolean isAtTop() {
        return this.atTop;
    }

}
