/**
 * @author Valter Crescenzi
 */
package it.uniroma3.fragment.dom;


import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class DOMVisitorListenerForbidAdapter extends DOMVisitorListenerAdapter implements DOMVisitorListener {

    @Override
    public void startDocument(Document doc) { forbid(); }
    @Override
    public void endDocument(Document doc)   { forbid(); }

    @Override
    public void startFragment(DocumentFragment fragment) { forbid(); }
    @Override
    public void endFragment(DocumentFragment fragment)   { forbid(); }

    @Override
    public void startElement(Element element)  { forbid(); }
    @Override
    public void endElement(Element element)    { forbid(); }

    @Override
    public void text(Text text)   { forbid(); }
    @Override
    public void comment(Comment comment) { forbid(); }

    private void forbid() {
        throw new IllegalStateException("This method should never be called!");
    }
    @Override
    public String toString() {
        return this.getClass().getName();
    }

}
