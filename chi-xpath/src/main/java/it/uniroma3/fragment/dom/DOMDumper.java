/**
 * DOMDumper.java
 * @author Valter Crescenzi
 */
package it.uniroma3.fragment.dom;

import static it.uniroma3.fragment.dom.DOMPrettyPrinter.*;

import org.w3c.dom.*;

import it.uniroma3.fragment.util.Indenter;

public class DOMDumper extends DOMVisitorListenerAdapter implements DOMVisitorListener {

    private StringBuilder sb;
    private Indenter ind;

    @Override
    public void startElement(Element element) {
        sb.append(ind.inc()+print(element));
    }
    
//    private String link(Element element) {
//        if (element.hasAttribute(HREF_ATTRIBUTE))
//            return " "+HREF_ATTRIBUTE+"=\'"+element.getAttribute(HREF_ATTRIBUTE)+"\'";
//        return "";
//    }
    
    @Override
    public void endElement(Element element) {
        ind.dec();
    }
    @Override
    public void text(Text text) {
        sb.append(ind+print(text)+"\n");
    }
    @Override
    public void comment(Comment comment) {
        sb.append(ind+print(comment)+"\n");				
    }
    @Override
    public void attribute(Attr attribute) {
//        sb.append(ind+print(attribute)+"\n");
    }
    @Override
    public void startDocument(Document doc) {
        sb.append(ind.inc()+print(doc)+"\n");
    }
    @Override
    public void endDocument(Document doc) {
        ind.dec();
    }
    @Override
    public void startFragment(DocumentFragment fragment) {
        sb.append(ind.inc()+print(fragment)+"\n");
    }
    @Override
    public void endFragment(DocumentFragment fragment) {
        ind.dec();
    }    

    public String dump(Node node) {
        this.sb = new StringBuilder();
        this.ind = Indenter.createIndenter();
        new DOMVisitor(this).visit(node);
        return this.sb.toString();
    }

}
