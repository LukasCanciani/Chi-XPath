package xfp.generation;

import java.util.Objects;

import org.w3c.dom.Node;

public class XPathStep {
    
    private XPathStepFactory generatedBy;
    
    private String xpath;   /* an XPath step expression */
    
    private Node from;      /* the (context) node  this  XPath step is evaluated from */

    private Node to;        /* a single node reached by this step  */
    
    
    public XPathStep(String xpath, Node from, Node to) {
        this.xpath = xpath;
        this.from = from;
        this.to = to;
    }
    
    public String getXPathStepExpression() {
        return this.xpath;
    }
    
    public Node getFrom() {
        return this.from;
    }

    public Node getTo() {
        return this.to;
    }
    
    public void setGeneratedBy(XPathStepFactory factory) {
        this.generatedBy = factory;
    }

    public String getShortName() {
        return this.generatedBy.toString();
    }
    
    @Override
    public int hashCode() {
        return this.getXPathStepExpression().hashCode()+ this.getFrom().hashCode() + this.getTo().hashCode();                
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof XPathStep))   
            return false;
        
        final XPathStep that = (XPathStep)o;
        return ( Objects.equals(this.getXPathStepExpression(), that.getXPathStepExpression()) &&
                 Objects.equals(this.getFrom(), that.getFrom()) &&
                 Objects.equals(this.getTo(), that.getTo())                
                );
    }
        
    @Override
    public String toString() {
        return  "Step: "+
                this.getFrom().toString()
                +'\u2192'+this.getXPathStepExpression()+"<SUP>"+this.getShortName()+"</SUP>"+'\u2192'
                +this.getTo().toString();
    }
    
}
