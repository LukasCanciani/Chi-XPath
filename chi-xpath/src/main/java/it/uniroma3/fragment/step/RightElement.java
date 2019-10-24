package it.uniroma3.fragment.step;

import org.w3c.dom.Node;

/**
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class RightElement extends AbstractSingletonStepFactory {
    @Override
    protected Node query(Node context) {
        return getContiguousElement(context, Node::getNextSibling);
    }
    @Override
    protected String makeStepExpression(Node from) {
        return "following-sibling::*";
    }
}