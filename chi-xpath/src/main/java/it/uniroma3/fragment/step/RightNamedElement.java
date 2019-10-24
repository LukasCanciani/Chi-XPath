package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.util.DocumentUtils.isElement;

import org.w3c.dom.Node;

public class RightNamedElement extends AbstractSingletonStepFactory {
    @Override
    protected Node query(Node context) {
        Node current = context;
        do {
            current = current.getNextSibling(); // move in the given direction
        } while (current!=null && !isElement(current));                    
        return current;
    }
    @Override
    protected String makeStepExpression(Node from) {
        return "following-sibling::"+this.elementCase(query(from).getNodeName());
    }
}