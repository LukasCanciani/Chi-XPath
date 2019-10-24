package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.dom.DOMPrettyPrinter.*;

import java.util.Objects;

import org.w3c.dom.Node;

/**
 * Models one of the available XPath steps <EM>at the instance level</EM>, i.e.,
 * from a {@link Node} occurrence to another {@link Node} occurrence
 * of a DOM tree.
 * <BR/>
 * {@linkplain #getXPathStepExpression()} returns the XPath expression describing
 * the step. 
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
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
    
    public XPathStepFactory getGeneratingFactory() {
        return this.generatedBy;
    }
    
    public void setGeneratedBy(XPathStepFactory factory) {
        this.generatedBy = factory;
    }

    public String getShortName() {
        return Objects.toString(this.generatedBy);
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
        return  this.getShortName() + "-Step: " +
                print(this.getFrom()) +
                '\u2192' + this.getXPathStepExpression() + '\u2192' +
                print(this.getTo());
    }
    
}
