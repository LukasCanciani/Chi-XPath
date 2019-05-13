package xfp.hlog;

import java.util.LinkedList;

import org.apache.xerces.dom.NodeImpl;

import it.uniroma3.hlog.render.IterableRenderer;
import it.uniroma3.hlog.render.TableBuilder;
import it.uniroma3.token.dom.DOMToken;
import it.uniroma3.token.dom.node.DOMNode;
 
import static xfp.template.TemplateMarker.*;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class NodeListRenderer implements IterableRenderer<NodeImpl, LinkedList<NodeImpl>> {

    @Override
    public String toHTMLstring(LinkedList<NodeImpl> nodelist) {
        final TableBuilder builder = new TableBuilder().setBaseWidth(128)
                .addColumn(1)
                .addColumn(1)
                .addColumn(1)
                .addColumn(1)
                .addColumn(3);
        
        builder.table();
        builder
            .tr()
            .th("node")
            .th("depth")
            .th("var_mark")
            .th("invar_mark")
            .th("XPath (for dis.)")
            ._tr();
        nodelist.forEach( node -> {
            DOMNode n = (DOMNode)node; 
            DOMToken t = n.asDOMToken();
            Object canXpath = t.getAnnotation("xpath");
            builder.tr();
            builder.td(escapeHtml(n.toString()));
            builder.td(t.depth());
            builder.td(n.getUserData(VARIANT_MARKER));
            builder.td(n.getUserData(INVARIANT_MARKER));
            builder.td(canXpath);
            builder._tr();
        });
        builder._table();
        return builder.toString();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Class<LinkedList<NodeImpl>> getRenderedObjectClass() {
        return (Class<LinkedList<NodeImpl>>) LinkedList.class.asSubclass(LinkedList.class);
    }

    @Override
    public Class<? extends NodeImpl> getRenderedElementClass() {
        return NodeImpl.class;
    }

}
