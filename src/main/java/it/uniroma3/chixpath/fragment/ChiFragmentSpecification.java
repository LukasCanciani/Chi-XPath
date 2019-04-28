package it.uniroma3.chixpath.fragment;

import static it.uniroma3.fragment.step.CaseHandler.*;
import static it.uniroma3.fragment.util.DocumentUtils.CLASS_ATTRIBUTE;
import static it.uniroma3.fragment.util.DocumentUtils.isAttribute;
import static it.uniroma3.fragment.util.DocumentUtils.isElement;
import static it.uniroma3.fragment.util.DocumentUtils.isNodeWithClassAttribute;
import static it.uniroma3.fragment.util.DocumentUtils.isNodeWithIdAttribute;
import static it.uniroma3.fragment.util.DocumentUtils.isText;
import static it.uniroma3.fragment.util.DocumentUtils.isTextOfLength;
import static it.uniroma3.fragment.util.TemplateUtils.extractLongestInvariant;
import static it.uniroma3.fragment.util.XPathExpressionBuilderUtils.containsPredicate;

import org.w3c.dom.Node;
import org.w3c.dom.Text;

import it.uniroma3.fragment.XPathFragment;
import it.uniroma3.fragment.XPathFragmentSpecification;
import it.uniroma3.fragment.step.CaseHandler;
import it.uniroma3.fragment.step.Down;
import it.uniroma3.fragment.step.NamedAttribute;
import it.uniroma3.fragment.step.RightNamedElement;
/**
 * The {@link XPathFragmentSpecification} of the {@link XPathFragment}
 * used to generate the <EM>Characteristic XPath</EM> over template
 * sample pages in the News domain.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class ChiFragmentSpecification extends XPathFragmentSpecification {

    static final private int TARGET_TEXT_MIN_LENGTH = 3;
    static final private int TARGET_TEXT_MAX_LENGTH = 32;

    static private class DownTextValue extends Down {
        public DownTextValue() {
            super(true,false,false,true);
            /* Targeting texts, move down by using steps by name and pos, 
             * e.g., SPAN[4] 
             */
        }
        @Override
        public String xpathStepDownToText(Node node, int pos) {
            final Text text = (Text)node;
            if (!extractLongestInvariant(text).trim().isEmpty())
                // Add a predicate explicitly stating a word from the text value
                return super.xpathStepDownToText(text, pos) + containsPredicate(text);
            else
                return super.xpathStepDownToText(text, pos) ;
        }        
    }
    
    final private CaseHandler caseHandler;
    
    public ChiFragmentSpecification() {
        this(HTML_STANDARD_CASEHANDLER);
    }
    
    public ChiFragmentSpecification(CaseHandler ch) {
        super(new DownTextValue(), new RightNamedElement(), new NamedAttribute(CLASS_ATTRIBUTE));
        this.setRange(3); // TODO Make it configurable 
        this.caseHandler = ch;
    }
    
    public ChiFragmentSpecification(CaseHandler ch, int range) {
        super(new DownTextValue(), new RightNamedElement(), new NamedAttribute(CLASS_ATTRIBUTE));
        this.setRange(range); 
        this.caseHandler = ch;
    }
    
    
    
    @Override
    public boolean isSuitablePivot(Node node) {
        return isNodeWithClassAttribute(node) || isNodeWithIdAttribute(node);
    }

    @Override
    public boolean isSuitableTarget(Node node) {
        return isTextOfLength(node, TARGET_TEXT_MIN_LENGTH, TARGET_TEXT_MAX_LENGTH) || isAttribute(node);
    }

    @Override
    public boolean isCrossable(Node node) {       
        return isElement(node) || isAttribute(node) || isText(node);
    }
    
    /**
     * 
     * @return true iff this fragment can extract multi-valued attributes
     *              i.e., several values per page
     */
    @Override
    public boolean canExtractMultiValued() { return true; }
    
    @Override
    public CaseHandler getCaseHandler() {
        return this.caseHandler;
    }

    @Override
    public String toString() {
        return this.getClass().getTypeName()+" "+this.getStepFactories()+" "+this.getCaseHandler();
    }

}
