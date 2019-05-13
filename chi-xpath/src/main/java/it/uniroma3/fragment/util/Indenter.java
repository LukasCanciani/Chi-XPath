package it.uniroma3.fragment.util;
/**
 * Indenter.java
 * 
 * Support nested indentation levels.

 * @author  Valter Crescenzi
 */
import java.util.Stack;

public class Indenter {

    static final public String INDENT_UNIT = "  "; /* DEFAULT INDENT_UNIT */

    static final public Indenter NO_INDENT = new Indenter(0,"","",false) {
        @Override
		public String toString() { return ""; }
    };

    public int indent;                 /* current level of indentation */
    
    private String unit = INDENT_UNIT; /* string used  for indentation */
    
    private String prefix;
    private String suffix;
    private Stack<Integer> stack;      /* to keep track of the nesting levels reached */
    private int index;
    private boolean numbering;

    static public Indenter createIndenter() {
        return new Indenter(0,"","",false);
    }
    static public Indenter createIndenterFromIndentPosition(int pos) {
        return new Indenter(pos,"","",false);
    }
    static public Indenter createIndenterWithPrefix(String prefix) {
        return new Indenter(0,prefix,"",false);
    }
    static public Indenter createNumberingIndenter() {
        return new Indenter(0,"","",true);
    }
    static public Indenter createNumberingIndenterWithPrefix(String prefix) {
        return new Indenter(0,prefix,"",true);
    }
    static public Indenter createNumberingIndenterWithPrefixAndSuffix(String prefix, String suffix) {
        return new Indenter(0,prefix,suffix,true);
    }

    private Indenter(int ind, String prefix, String suffix, boolean numbering) {
        this.indent = ind;
        this.index  = 0;
        this.stack  = new Stack<Integer>();
        this.prefix = prefix;
        this.suffix = suffix;
        this.numbering = numbering;        
    }
    
    public Indenter setIndentationUnit(String unit) {
        this.unit = unit;
        return this;
    }

    private String indentString(int i) {
        final StringBuilder result = new StringBuilder(i*this.unit.length()+this.prefix.length()+this.suffix.length());
        for(int k=0; k<i; k++) {
            result.append(this.unit);
        }
        return result.toString();
    }
    
    @Override
	public String toString() {
    	final StringBuilder result = new StringBuilder(this.prefix);
    	result.append(indentString(this.indent));
    	if (this.numbering) {
    		result.append((this.index++)+".");
    	}
    	result.append(this.suffix);
    	return result.toString();
    }

    public Indenter inc() {
        if (this.numbering) {
            this.stack.push(this.index);
            this.index = 0;
        }
        ++this.indent;
        return this;
    }

    public Indenter dec() {
        if (this.numbering) {
            this.index = this.stack.pop();
        }
        --this.indent;
        return this;
    }

    public int value() {
    	return this.indent;
    }

}



