package it.uniroma3.fragment.dom;

import org.w3c.dom.*;

public interface DOMVisitorListener {

    public void setDOMVisitor(DOMVisitor visitor);

    /* root nodes */
    public void startDocument(Document doc);
    public void endDocument(Document doc);
    public void startFragment(DocumentFragment fragment);
    public void endFragment(DocumentFragment fragment);

    /* intermediate nodes */
    public void startElement(Element element);
    public void endElement(Element element);

    /* leaf nodes & attributes */
    public void text(Text text);
    public void comment(Comment comment);

    public void attribute(Attr attribute);

}
