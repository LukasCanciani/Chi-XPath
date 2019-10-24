package it.uniroma3.fragment.dom;

import static it.uniroma3.fragment.dom.DOMVisitorSkippingStrategies.*;

import java.util.Objects;
import java.util.Stack;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Visit a {@link Document} triggering events of a {@link DOMVisitorListener}.
 * 
 * During the visit, it selects some nodes, and whole subtrees, that should
 * be removed according to a strategy specified by means of a 
 * {@link SkippingStrategy}.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class DOMVisitor  {

    private DOMVisitorListener listener; // target event listener

    private boolean notifySkipped; // true iff skipped nodes should be notified

    private Node lastNodeNotified; // last node for which listeners have been notified

    private Stack<Boolean> skip;   // to keep track of subtrees to skip

    private boolean lastSkipped;   // true iff last notified node should be skipped

    private SkippingStrategy strategy; // the strategy to selects nodes to skip

    /** Creates a new instance of DOMVisitor */
    public DOMVisitor() {
        this(null, NOT_SKIPPING_ANYTHING_STRATEGY, true);
    }

    public DOMVisitor(DOMVisitorListener listener) {
        this(listener, NOT_SKIPPING_ANYTHING_STRATEGY, true);
    }

    public DOMVisitor(DOMVisitorListener listener, SkippingStrategy strategy) {
        this(listener, strategy, true);
    }

    /* MGC */
    public DOMVisitor(DOMVisitorListener listener, SkippingStrategy strategy, boolean notifySkipped) {    	
        this.listener = listener;
        this.strategy = strategy;
        this.notifySkipped = notifySkipped;
        this.skip = new Stack<>();
        this.skip.push(false);
    }


    public void setListener(DOMVisitorListener listener) {
        this.listener = listener;
    }

    public void visit(Node node) {
        Objects.requireNonNull(node,"Cannot visit a null node!");
        /* visit even subtrees to skip to preserve the work of other listeners */
        int type = node.getNodeType();

        switch ( type ) {
        case Node.DOCUMENT_FRAGMENT_NODE:
            DocumentFragment fragment = (DocumentFragment)node;
            startFragment(fragment);
            visitChildren(node);
            endFragment(fragment);
            break;

        case Node.DOCUMENT_NODE:

            Document document = (Document)node;
            startDocument(document);
            visitChildren(node);
            endDocument(document);
            break;

        case Node.ELEMENT_NODE:

            Element element = (Element)node;

            this.skip.push(this.skip.peek() || isSubtreeToSkip(element));

            startElement(element);
            visitAttributes(element);
            visitChildren(element);
            endElement(element);

            this.skip.pop();
            break;

        case Node.ATTRIBUTE_NODE:
            attribute((Attr)node);
            break;

        case Node.TEXT_NODE:
            text((Text)node);
            break;

        case Node.COMMENT_NODE:
            comment((Comment)node);
            break;

        default:
            /* skip all other 'unusual' DOM node types */
        }
    }

    private void visitAttributes(Node node) {
        final NamedNodeMap attributes = node.getAttributes();
        for(int i=0; i<attributes.getLength(); i++) {
            visit(attributes.item(i));
        }
    }

    private void visitChildren(Node node) {
        Node child = node.getFirstChild();
        while (child!=null) {
            Node next = child.getNextSibling();
            visit(child);
            child = next;
        }
    }

    public boolean isLastNotifiedTokenSkipped() {
        return this.lastSkipped;
    }

    public boolean isLastNotifiedTokenWithinAsubtreeToSkip() {
        return this.skip.peek();
    }

    public Node getLastNodeNotified() {
        return this.lastNodeNotified;
    }

    final private boolean checkIfToNotify(Node node) {
        this.lastNodeNotified = node;
        return this.listener!=null && ( this.notifySkipped || !this.lastSkipped );
    }

    final public void startDocument(Document doc) {
        this.lastSkipped = false;
        if (checkIfToNotify(doc)) this.listener.startDocument(doc);
    }
    final public void endDocument(Document doc)   {
        this.lastSkipped = false;
        if (checkIfToNotify(doc)) this.listener.endDocument(doc);
    }

    final public void startFragment(DocumentFragment fragment) {
        this.lastSkipped = false;
        if (checkIfToNotify(fragment)) this.listener.startFragment(fragment);
    }
    final public void endFragment(DocumentFragment fragment)   {
        this.lastSkipped = false;
        if (checkIfToNotify(fragment)) this.listener.endFragment(fragment);
    }

    final public void startElement(Element element)  {
        this.lastSkipped = this.skip.peek() || isTagToSkip(element);
        if (checkIfToNotify(element)) this.listener.startElement(element);
    }
    final public void endElement(Element element)    {
        this.lastSkipped = this.skip.peek() || isTagToSkip(element);
        if (checkIfToNotify(element)) this.listener.endElement(element);
    }

    final public void text(Text text)  {
        this.lastSkipped = this.skip.peek() || isTextToSkip(text);
        if (checkIfToNotify(text)) this.listener.text(text);
    }
    final public void comment(Comment comment) {
        this.lastSkipped = this.skip.peek();
        if (checkIfToNotify(comment)) this.listener.comment(comment);
    }
    final public void attribute(Attr attr)  {
        this.lastSkipped = this.skip.peek() || isAttrToSkip(attr);
        if (checkIfToNotify(attr)) this.listener.attribute(attr);
    }

    private boolean isTagToSkip(Element element) {
        return this.strategy.isTagToSkip(element);
    }

    private boolean isSubtreeToSkip(Element element) {
        return this.strategy.isSubtreeToSkip(element);
    }

    private boolean isTextToSkip(Text text) {
        return this.strategy.isTextToSkip(text);
    }

    private boolean isAttrToSkip(Attr attr) {
        return this.strategy.isAttributeToSkip(attr);
    }

    static public interface SkippingStrategy {
        public boolean isTagToSkip(Element element);
        public boolean isSubtreeToSkip(Element subtreeRoot);
        public boolean isTextToSkip(Text text);
        public boolean isAttributeToSkip(Attr attr);
    }

}
