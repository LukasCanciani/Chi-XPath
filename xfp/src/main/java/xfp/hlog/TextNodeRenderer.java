package xfp.hlog;


import static xfp.template.TemplateUtil.isTemplateNode;
import static xfp.template.TemplateUtil.isVariantNode;

import it.uniroma3.hlog.render.ObjectRenderer;
import it.uniroma3.token.dom.node.DOMTextNode;


public class TextNodeRenderer implements ObjectRenderer<DOMTextNode> {

    @Override
    public String toHTMLstring(DOMTextNode node) {
        final StringBuilder builder = new StringBuilder();
        if (isVariantNode(node))  builder.append("Value: ");
        if (isTemplateNode(node)) builder.append("Template: ");
        builder.append("'" +node.getNodeValue()+"'");
        return builder.toString();
    }

    @Override
    public Class<? extends DOMTextNode> getRenderedObjectClass() {
        return DOMTextNode.class;
    }
    
}
