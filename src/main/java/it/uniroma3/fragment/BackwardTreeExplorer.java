package it.uniroma3.fragment;


import static it.uniroma3.fragment.dom.DOMPrettyPrinter.print;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import it.uniroma3.fragment.step.XPathStep;
import it.uniroma3.fragment.util.Indenter;
import lombok.Getter;

/**
 * An algorithm exploring all paths leading to a target 
 * {@link Node} starting from a pivot {@link Node}.
 * <BR/>
 * Notice that the search is backward: starting from a target node, a
 * pivot node is searched, so that a {@link Path} need to be traversed
 * in reverse order to generate a correct XPath expression leading
 * to the target node starting from the pivot.
 * <BR/>
 * See method {@link #exploreFromTarget(Node)}
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class BackwardTreeExplorer {

    static final private Logger log = LoggerFactory.getLogger(BackwardTreeExplorer.class);

    final private Set<Path> paths; // set of paths accumulated so far

    @Getter
    private XPathFragment fragment; 

    private int pathCounter; // how many path have been evaluated so far?
    
    private Indenter ind;

    public BackwardTreeExplorer(XPathFragment fragment) {
        this.fragment = fragment;
        this.paths = new LinkedHashSet<>();
        this.ind = Indenter.createIndenter().setIndentationUnit(" ");
        this.pathCounter = 1; // N.B. EMPTY_PATH is number 0
    }

    public XPathFragmentSpecification getXPathFragmentSpecification() {
        return this.fragment.getSpecification();
    }

    /**
     * 
     * @param target - starting node from which to explore looking for a path to a pivot
     * @return the set of {@link Path} found in this tree
     */
    public Set<Path> exploreFromTarget(Node target) {
        if (!getXPathFragmentSpecification().isSuitableTarget(target))
            throw new IllegalArgumentException("An unsuitable target node has been provided: "+print(target));
        this.paths.clear(); /* accumulate new paths */
        int range = this.getXPathFragmentSpecification().getRange();
        logtrace("Looking for pivots at max distance {} from target node: {}",range,target);

        this.explore(target, Path.EMPTY_PATH, range);
        logtrace("Overall, evaluated {} paths",this.pathCounter);
        return this.paths;
    }

    private void explore(
            Node current,   // current node, exploring away from the start node
            Path path,      // path that leads here
            int distance) { // #steps still available
        logtrace("current path={}",path);

        if (this.getXPathFragmentSpecification().isSuitablePivot(current)) {
            logtrace("Found suitable pivot: {}",current);
            logtrace("Saving path {}", path.getXPathExpression());
            this.paths.add(path);
            return;
        }

        if (!this.getXPathFragmentSpecification().isCrossable(current)) {
            logtrace("Cannot cross node: {}",current);
            return;
        }

        /* steps still available? */
        if (distance > 0) {
            logtrace("Expanding path... (current distance={})",distance);

            /* move one step farther away to reach other pivots */
            final Set<XPathStep> available = fragment.availableTo(current);
            logSteps(available);
            for (XPathStep step : available) {
                ind.inc();

                logtrace("Adding {}",step);
                /* move backward looking for a suitable pivot */
                final Node from = step.getFrom();

                /* check it is not chasing its own footsteps */
                // e.g., for excluding a LeftElement step when we just moved to Right
                if (path.hasKnot()) {
                    logtrace("Path cycle - giving up adding: {}+{}",path,step);
                } else 
                /* wherever we're going, there's a shorter path to start from */
                if (this.getXPathFragmentSpecification().isSuitableTarget(from)) {
                    logtrace("Met another suitable target node: {} - Giving up as shorter path are preferrable",from);
                } else
                    this.explore(from, path.addFirst(step), distance - deltaDistance(from,current));
                if ( (this.pathCounter++ % 10 == 0)) // log every 10 paths, not always: too many
                    logtrace("Evaluated {} paths",this.pathCounter);
                ind.dec();
            }
        }
    }

    final private void logSteps(final Collection<XPathStep> available) {
        final int size = available.size();
        if (size>0) {
            logtrace("Available steps ({}):",size);
            final Iterator<XPathStep> it = available.iterator();
            IntStream.range(0, size).forEach(
                    i -> logtrace("{}",it.next())
                  );
        } else {
            logtrace("No other steps available");
        }
    }

    private int deltaDistance(Node from, Node to) {
        /* Up or Down? it counts 1 for not-only sons */
        return ( isOnlyElementSon(from, to) ? 0 : +1 ); 
        /* LeftElement or Right always count +1 */
    }

    /* do *not* count for distances vertical steps into an only-son element */
    private boolean isOnlyElementSon(Node from, Node to) {
        return (   from==to.getParentNode() && hasJustOneChild(from) 
                || to==from.getParentNode() && hasJustOneChild(to) );
    }

    public boolean hasJustOneChild(Node node) {
        return node.getChildNodes().getLength()==1;
    }
    
    private void logtrace(String msg, Object... objs) {
        log.trace("{}"+msg,ind,objs); // log indented msg
    }

}