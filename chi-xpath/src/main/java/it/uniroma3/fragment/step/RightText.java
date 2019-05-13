package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.util.DocumentUtils.isText;

import org.w3c.dom.Node;

/**
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class RightText extends AbstractSingletonStepFactory {
    @Override
    protected Node query(Node context) {
        Node current = context;
        do {
            current = current.getNextSibling(); // move in the given direction
        } while (current!=null && !isText(current));                    
        return current;
    }
    @Override
    protected String makeStepExpression(Node from) {
        return "following-sibling::text()[1]";
    }
}