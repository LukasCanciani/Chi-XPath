package xfp.hlog;


import static it.uniroma3.hlog.HypertextualUtils.linkTo;
import static xfp.util.DocumentUtil.HREF_ATTRIBUTE;
import static xfp.util.DocumentUtil.isAnchorNode;

import org.w3c.dom.Node;

import it.uniroma3.hlog.render.ObjectRenderer;
import it.uniroma3.token.dom.node.DOMTagNode;

public class TagNodeRenderer implements ObjectRenderer<DOMTagNode> {

    @Override
    public String toHTMLstring(DOMTagNode node) {
        return isAnchorNode(node)  ? 
                linkValue(node) : 
                node.toString();
    }

    protected String linkValue(DOMTagNode node) {
        final String target = getTarget(node);
        // TODO find local name by starting from Experiment.getInstance();
        return "link to " +linkTo(target).withAnchor(getTarget(node));
    }

    @Override
    public Class<? extends DOMTagNode> getRenderedObjectClass() {
        return DOMTagNode.class;
    }

    private String getTarget(Node link) {
        final Node href = link.getAttributes().getNamedItem(HREF_ATTRIBUTE);        
        return ( href!=null ? href.getNodeValue() : null );        
    }
    
}
