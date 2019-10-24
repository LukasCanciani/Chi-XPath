package xfp.model;

import java.util.Objects;
import java.util.Vector;

import org.w3c.dom.Node;

/**
 * Abstract base class for different kind of values 
 * in a {@link Vector}.
 * 
 * Originally, these values come from a nodes extracted
 * from {@link Webpage} in the form of objects. 
 * 
 * Further processing can occur ({@see Normalizer})
 * to clean up the extracted values. 
 *  
 * These values can then be interpreted by means of a {Type}
 * and seen as a subtype of {Object}, e.g., {Integer}s,
 * {String}, {URL}.
 * 
 */
public class Value<T> implements Comparable<Value<T>> {
	
	/** the value after its interpretation by means of 
	  a Type, e.g., as a NUMBER, URL etc., etc... 
	 */
	private T value; 
	/**
	 * the {@link Webpage} from which this value has been extracted
	 */
    private Webpage page;

    private Node node;
    
	public Value(Webpage page, T value) {
		this.page  = page;
		this.value = value;
	}
	
	public Value(T value) {	
		this.value = value;
	}
	
	/**
	 * The object interpreting the value according to a Type
	 * @return the value as a generic 
	 *         Object interpreted by Type.
	 */
	public T getValue() {
		return this.value;
	}
	
	/**
	 * @return the {@link Webpage} from which this value comes from
	 */
    public Webpage getPage() {
        return this.page;
    }
    
	public void setPage(Webpage page) {
		this.page = page;
	}
    
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	final public boolean isNull() { return getValue()==null; }
	
	@Override
	final public int hashCode() {
		return Objects.hashCode(getValue())+Objects.hashCode(this.getPage());
	}

	@Override
	final public boolean equals(Object o) {
		if (o==null || this.getClass()!=o.getClass()) return false;
		
		@SuppressWarnings("unchecked")
		Value<T> that = (Value<T>)o;
		
		return Objects.equals(this.getValue(),  that.getValue()) && 
		       Objects.equals( this.getPage(),  that.getPage() );
	}
	
	final public String toLowerCase() {
		if (this.isNull()) return null;
		return this.getValue().toString().toLowerCase().trim();	
	}

	@Override
	public int compareTo(Value<T> that) {
		int cmp = this.getPage().getURI().compareTo(that.getPage().getURI());
		if (cmp!=0) return cmp;
		if (this.isNull() && that.isNull()) return 0;
		if (that.isNull()) return +1;
		if (this.isNull()) return -1;
		return this.toString().compareTo(that.toString());
	}

	/**
	 * 
	 * @return either null if this value {@link #isNull()}, or
	 *         its String representation
	 */
	@Override
	final public String toString() {
		if (this.isNull()) return null;
		return String.valueOf(this.getValue());
	}
}
