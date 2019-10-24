package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.util.DocumentUtils.isElement;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Set;

import org.w3c.dom.Node;

public class Sniper extends XPathStepFactory {
    @Override
    protected Set<String> makeStepExpressions(Node from) {
        if (isElement(from) && eval(from, ".//text()").getLength()==1)
            return singleton(".//text()[1]");
        else return emptySet();
    }
}