package xfp.generation;


//import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import static xfp.generation.Path.EMPTY_PATH;

//import java.util.Collection;
//import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
//import java.util.stream.IntStream;

import org.w3c.dom.Node;

//import it.uniroma3.hlog.HypertextualLogger;
import it.uniroma3.util.Indenter;

//import static it.uniroma3.hlog.Level.TRACE;

/**
 * A simple algorithm to explore all paths leading to a <EM>target</EM> 
 * {@link Node} starting from a <EM>pivot</EM> {@link Node}.
 * <BR/>
 * Notice that the search is backward: starting from a target node, a
 * pivot node is searched, so that a {@link Path} need to be traversed
 * in reverse order to generate a correct XPath expression leading
 * to the target node starting from the pivot.
 * <BR/>
 * See method {@link #exploreFromTarget(Node)}
 * 
 */
public class BackwardTreeExplorer {

  //  static final private HypertextualLogger log = getLogger();

    final private Set<Path> paths; // set of paths accumulated so far

    final private int range; // exploration max range from starting node 	

    // factory for generating the steps used for navigating the tree
    private XPathFragment fragment; 

    private Indenter ind;

    @SuppressWarnings("unused")
	private int pathCounter; // how many path have been evaluated so far?
    
    public BackwardTreeExplorer(XPathFragment fragment) {
        this.range  = fragment.getRange();
        this.fragment = fragment;
        this.paths = new LinkedHashSet<>();
        this.ind = Indenter.createIndenter().setIndentationUnit("&emsp;&emsp;");
        this.pathCounter = 1; // N.B. EMPTY_PATH is number 0
    }

    /**
     * 
     * @param target - starting node from which to explore looking for a path to a pivot
     * @return the set of {@link Path} found in this tree
     */
    public Set<Path> exploreFromTarget(Node target) {
        if (!this.fragment.isSuitableTarget(target))
            throw new IllegalArgumentException("An unsuitable target node has been provided: "+target);
        this.paths.clear(); /* accumulate new paths */
/*        if (log.isLoggable(TRACE)) log.trace("Looking for pivots at max distance "+this.range+" from target node:");
        if (log.isLoggable(TRACE)) log.trace(target);
        if (log.isLoggable(TRACE)) log.trace("<BR/>");
 */       this.explore(target, EMPTY_PATH, this.range);
//        if (log.isLoggable(TRACE)) log.trace(ind+"<EM>Overall</EM>, evaluated "+ this.pathCounter + " paths");
        return this.paths;
    }

    private void explore(
            Node current,   // current node, exploring away from the start node
            Path path,      // path that leads here
            int distance) { // #steps still available
  //      if (log.isLoggable(TRACE)) log.trace(ind+"current path="+path);

       if (this.fragment.isSuitablePivot(current)) {
       // if(true) {
//            if (log.isLoggable(TRACE)) log.trace(ind+"Found a suitable pivot, "+current);
 //           if (log.isLoggable(TRACE)) log.trace(ind+"<EM>Saving this path</EM>", path.getXPathExpression());
            this.paths.add(path);
            return;
        }

        /* steps still available? */
        if (distance > 0) {
 //           if (log.isLoggable(TRACE)) log.trace(ind+"Expanding path... (current distance="+distance+")");

            /* move one step farther away to reach other pivots */			
            final Set<XPathStep> available = fragment.availableTo(current);
 //           logSteps(available);
            for (XPathStep step : available) {
                ind.inc();
  //              if (log.isLoggable(TRACE)) log.trace("<BR/>");
    //            if (log.isLoggable(TRACE)) log.trace(ind+"\nTry adding "+step);
                /* move backward looking for a suitable pivot */
                final Node from = step.getFrom();

                /* check it is not chasing its own footsteps */
                // e.g., for excluding a LeftElement step when we just moved to Right
                if (path.hasKnot()) {
      //              if (log.isLoggable(TRACE)) log.trace(ind+"<EM>Path cycle - giving up adding: "+path+"+"+step+"</EM>");
                } else 
                /* wherever we're going, there's a shorter path to start from */
                if (this.fragment.isSuitableTarget(from)) {
//                    if (log.isLoggable(TRACE)) log.trace(ind+"Met another suitable target\n"+from);
//                    if (log.isLoggable(TRACE)) log.trace(ind+"<EM>Giving up this as shorter paths are preferred</EM>");
                } else
                    this.explore(from, path.addFirst(step), distance - deltaDistance(from,current));
              
//                if ( (this.pathCounter++ % 10 == 0) && log.isLoggable(TRACE)) 
//                    log.trace(ind+"Evaluated "+ this.pathCounter + " paths");
                ind.dec();
            }
        }
    }

/*    final private void logSteps(final Collection<XPathStep> available) {
//        if (!log.isLoggable(TRACE)) return;
        final int size = available.size();
        if (size>0) {
            log.trace(ind+"Available steps ("+size+"):");
            final Iterator<XPathStep> it = available.iterator();
            IntStream.range(0, size).forEach(
                    i -> log.trace(ind.toString()+it.next())
                    );
        } else {
            log.trace(ind+"<EM>No other steps are available</EM>");
        }
    }*/

    private int deltaDistance(Node from, Node to) {
        /* Up or Down? it counts 1 for not-only sons */
        return ( isOnlyElementSon(from, to) ? 0 : +1 ); 
        /* LeftElement or Right always count +1 */
    }

    /* do *not* count for distances vertical steps into an only-son element */
    private boolean isOnlyElementSon(Node from, Node to) {
        return (   from==to.getParentNode() && from.getChildNodes().getLength()==1 
                || to==from.getParentNode() && to.getChildNodes().getLength()==1   );
    }

}