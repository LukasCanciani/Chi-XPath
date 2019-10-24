package xfp.generation;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class describes a path for navigating the
 * DOM tree of a {@link Document} by means of an
 * XPath expression organized as a sequence of
 * XPath step (see {@link XPathStep}.
 * <BR/>
 * Implemented by means of a simple immutable
 * (persistent) linked representation to support
 * fast head insert.
 */
public class Path {

    static final public Path EMPTY_PATH = new Path() {
        @Override
        public boolean equals(Object o) {
            return ( this == o ) ;
        }
        @Override
        public int hashCode() {
            return 0;
        }
        @Override
        public boolean isEmpty() { return true; }
        @Override
        public String toString() { return "Empty Path"; }
        @Override
        public Node getStart()   { return null; }
        @Override
        public Node getEnd()     { return null; }
        
    };

	private Path next;

	private XPathStep step;
	
	private Path() {
		this.step = null;
		this.next = null;
	}	

    public Path(Path toCopy) {
        this.next = toCopy.next;
        this.step = toCopy.step;
    }
    
	public List<XPathStep> getSteps() {
	    if (this.isEmpty()) return Collections.emptyList();
	    final LinkedList<XPathStep> result = new LinkedList<>();
	    Path current = this;
	    while (current!=EMPTY_PATH) {
	        result.add(current.step);
	        current = current.next;
	    }	        
	    return result;
	}
	
	public Node getStart() {
		return this.getFirst().getFrom();
	}

	public Node getEnd() {
		return this.getLast().getTo();
	}
	
    public Path addFirst(XPathStep step) {
        final Path result = new Path();
        result.next = this;
        result.step = step;
        return result;
    }
	
	public XPathStep getFirst() {
		return this.step;
	}

	public XPathStep getLast() {
	    if (this.next==EMPTY_PATH)
	        return this.step;
	    else return this.next.getLast();
	}

	public boolean hasKnot() {
	    if (this.isEmpty()) return false;
	    
		final Map<Node,?> alreadySeen = new IdentityHashMap<Node,Void>();
		Node node = this.getStart();
		Path current = this;
		while (current!=EMPTY_PATH) {
		    final XPathStep step = current.step;
			node = step.getTo();
			if (alreadySeen.containsKey(node)) return true;
			alreadySeen.put(node,null); /* save it */
			current = current.next;
		}
		return false;
	}

	public boolean isEmpty() {
		return ( false );
	}
	
	public String getXPathExpression() {
	    final XPathBuilder builder = new XPathBuilder();	    
	    return builder.makeXPath(this);
	}

	
	private int hc = 0;
	
	@Override
	public int hashCode() {
	    if (hc==0) {
	        this.hc = this.step.hashCode() + this.next.hashCode();
	    }
	    return this.hc;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==null || !(o instanceof Path)) return false;
		
        Path currentThis = this;
        Path currentThat = (Path)o;
        while (currentThis!=EMPTY_PATH && currentThat!=EMPTY_PATH) {
            if (!currentThis.step.equals(currentThat.step))
                return false;
            currentThis = currentThis.next;
            currentThat = currentThat.next;
        }
        return true;
	}
	
	@Override
	public String toString() {
	    Path current = this;
        final StringBuilder result = new StringBuilder();
	    while (current!=EMPTY_PATH) {
	        final XPathStep step = current.step;
	        result.append(getStart().toString()+'\u2192'+"<SUP>"+step.getShortName()+"</SUP>");
	        current = current.next;
	    }
	    result.append(getEnd().toString());
	    return this.getClass().getSimpleName()+": "+result;
	}
	
}
