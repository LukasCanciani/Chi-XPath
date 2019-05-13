/**
 * @author Valter Crescenzi
 */
package it.uniroma3.fragment.dom;


import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Attr;

public class DOMVisitorListenerAdapter implements DOMVisitorListener {

    protected DOMVisitor visitor;

    public DOMVisitor getVisitor() {
        return visitor;
    }
    public void setDOMVisitor(DOMVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void startDocument(Document doc) { /* default behavior: do nothing */ }
    @Override
    public void endDocument(Document doc)   { /* default behavior: do nothing */ }

    @Override
    public void startFragment(DocumentFragment fragment) { /* default behavior: do nothing */ }
    @Override
    public void endFragment(DocumentFragment fragment)   { /* default behavior: do nothing */ }

    @Override
    public void startElement(Element element)  { /* default behavior: do nothing */ }
    @Override
    public void endElement(Element element)    { /* default behavior: do nothing */ }

    @Override
    public void text(Text text)   { /* default behavior: do nothing */ }
    @Override
    public void comment(Comment comment) { /* default behavior: do nothing */ }
    @Override
    public void attribute(Attr attr) { /* default behavior: do nothing */ }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}
