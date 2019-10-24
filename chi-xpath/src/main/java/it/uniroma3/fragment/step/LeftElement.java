package it.uniroma3.fragment.step;

import org.w3c.dom.Node;

public class LeftElement extends AbstractSingletonStepFactory {
    @Override
    protected Node query(Node from) {
        return getContiguousElement(from, Node::getPreviousSibling);
    }
    @Override
    protected String makeStepExpression(Node from) {
        return "preceding-sibling::*";
        /* N.B. other options are easily available. Just do:
         *      return "preceding-sibling::text()"
         *      return "preceding-sibling::node()"
         */
    }
}