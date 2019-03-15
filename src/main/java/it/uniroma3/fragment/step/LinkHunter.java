package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.util.DocumentUtils.isElement;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Set;

import org.w3c.dom.Node;

public class LinkHunter extends XPathStepFactory {

    @Override
    protected Set<String> makeStepExpressions(Node from) {
        final String anchorExpr = elementCase(".//a");
        /* to comply with DOM implementations using lowercase for element names */
        if (isElement(from) && eval(from, anchorExpr).getLength()>0) {
            return singleton(anchorExpr);
        } else return emptySet();
    }
    
    
}