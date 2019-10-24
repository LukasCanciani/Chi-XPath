package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.util.DocumentUtils.*;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.Set;

import org.w3c.dom.Node;

public class NamedAttribute extends XPathStepFactory {
    
    private String name; /* name of the target attribute, e.g., @href */

    public NamedAttribute() {
        this(HREF_ATTRIBUTE);
    }
    
    public NamedAttribute(String targetAttributeName) {
        this.name = targetAttributeName;                     
    }
    
    public String getAttributeName() {
        return this.name;
    }
    
    @Override
    protected Set<String> makeStepExpressions(Node from) {
        final String attributeExp = attributeCase("@"+this.name);
        if (isElement(from) && eval(from, attributeExp).getLength()==1)
            return singleton(attributeExp);
        else return emptySet();
    }

}