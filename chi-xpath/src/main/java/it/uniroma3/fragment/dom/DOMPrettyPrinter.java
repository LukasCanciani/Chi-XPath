package it.uniroma3.fragment.dom;


import org.w3c.dom.*;

import static org.w3c.dom.Document.*;

public class DOMPrettyPrinter {

    static public String print(Node node) {
        switch (node.getNodeType()) {
        case DOCUMENT_NODE:
            return print((Document)node);
        case DOCUMENT_FRAGMENT_NODE:
            return print((DocumentFragment)node);
        case ELEMENT_NODE:
            return print((Element)node);
        case TEXT_NODE:
            return print((Text)node);
        case COMMENT_NODE:
            return print((Comment)node);
        case ATTRIBUTE_NODE:
            return print((Attr)node);
        }
        return "";
    }

    static public String print(Document doc) {
        return doc.getClass().getSimpleName();        
    }

    static public String print(DocumentFragment df) {
        return df.getClass().getSimpleName();        
    }
    
    static public String print(Element element) {
        return "<" + element.getNodeName()+ print(element.getAttributes()) +">";
    }
    
    static public String print(NamedNodeMap attributes) {
        final StringBuilder result = new StringBuilder();
        for(int i=0; i<attributes.getLength(); i++) {
            result.append(" ");
            result.append(print((Attr)attributes.item(i)));
        }
        return result.toString();
    }

    static public String print(Text text) {
        return text.getNodeValue();
    }

    static public String print(Comment comment) {
        return comment.getNodeValue();
    }

    static public String print(Attr attribute) {
        return attribute.getName() + ( attribute.getValue()!=null ? "=\""+attribute.getValue()+"\"" : "" );
    }

}
