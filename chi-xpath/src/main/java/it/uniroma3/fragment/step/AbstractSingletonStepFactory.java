package it.uniroma3.fragment.step;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractSingletonStepFactory extends XPathStepFactory {
    /**
     * <BR/>
     * Override this method to make it fast.
     * <BR/>
     * Do not override this method to avoid re-implementing
     * the logics of the corresponding XPath expression.
     * @param context - the starting node
     * @return the node-set returned by the evaluation of this step
     */
    protected Node query(Node context) {
        final String expr = makeStepExpression(context);
        final NodeList nodeset = super.eval(context, expr);
        if (nodeset==null) return null;
        if (nodeset.getLength()>1) {
            throw new IllegalStateException("A "+AbstractSingletonStepFactory.class.getSimpleName()
                    +" cannot return multiple nodes. From "+context+" with xpath: "+expr);
        }
        return nodeset.item(0);
    }
    @Override
    protected List<XPathStep> makeSteps(Node from) {
        final Node to = query(from);
        if (to==null) return emptyList();
        final String exp = makeStepExpression(from);
        if (exp==null) return emptyList();        
        final XPathStep step = new XPathStep(exp,from,to);
        step.setGeneratedBy(this);
        return singletonList(step);
    }
    
    abstract protected String makeStepExpression(Node context);
    
}