/**
 * @author Valter Crescenzi
 */
package it.uniroma3.fragment.dom;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class DOMVisitorListenerChain extends DOMVisitorListenerAdapter implements DOMVisitorListener {

    private DOMVisitorListener[] listeners;

    public static DOMVisitorListener chain(DOMVisitorListener...tobechained) {
        return new DOMVisitorListenerChain(tobechained);
    }

    private DOMVisitorListenerChain(DOMVisitorListener...tobechained) {
        this.listeners = tobechained;
    }

    public DOMVisitorListenerChain(List<DOMVisitorListener> dvl) {
        this(dvl.toArray(new DOMVisitorListener[dvl.size()]));
    }
    @Override
    public void setDOMVisitor(DOMVisitor visitor) {
        for(DOMVisitorListener listener : listeners)
            listener.setDOMVisitor(visitor);
    }

    @Override
    public void startDocument(Document doc) {
        for(DOMVisitorListener listener : listeners)
            listener.startDocument(doc);    	    	
    }
    @Override
    public void endDocument(Document doc)   {
        for(DOMVisitorListener listener : listeners)
            listener.endDocument(doc);
    }
    @Override
    public void startFragment(DocumentFragment fragment) {
        for(DOMVisitorListener listener : listeners)
            listener.startFragment(fragment);    	    	
    }
    @Override
    public void endFragment(DocumentFragment fragment)   {
        for(DOMVisitorListener listener : listeners)
            listener.endFragment(fragment);
    }
    @Override
    public void startElement(Element element)  {
        for(DOMVisitorListener listener : listeners)
            listener.startElement(element);
    }
    @Override
    public void endElement(Element element)    {
        for(DOMVisitorListener listener : listeners)
            listener.endElement(element);
    }
    @Override
    public void text(Text text)  {
        for(DOMVisitorListener listener : listeners)
            listener.text(text);
    }
    @Override
    public void comment(Comment comment) {
        for(DOMVisitorListener listener : listeners)
            listener.comment(comment);
    }    		
    @Override
    public String toString() {
        return Arrays.toString(this.listeners);
    }
}
