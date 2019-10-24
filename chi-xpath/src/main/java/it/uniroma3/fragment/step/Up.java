package it.uniroma3.fragment.step;

import org.w3c.dom.Node;

public class Up extends AbstractSingletonStepFactory {
    @Override
    protected Node query(Node context) {
        return getContiguousElement(context, Node::getParentNode);
    }

    @Override
    protected String makeStepExpression(Node context) {
        return "..";
    }
}