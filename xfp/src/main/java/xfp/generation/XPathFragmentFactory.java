package xfp.generation;

import static xfp.template.TemplateUtil.isPCDATAwithVariantContent;
import static xfp.util.DocumentUtil.*;

import java.net.URI;
import java.util.List;

import org.w3c.dom.Node;

import xfp.fixpoint.RuleFactory;
import xfp.template.TemplateUtil;
import xfp.util.Constants;
import xfp.util.DocumentUtil;
import xfp.util.XFPConfig;
/**
 * 
 * A XPath fragment factory establishes the set of possible target node (see {@link #isSuitableTarget(Node)},
 * the set of possible pivot nodes (see {@link #isSuitableTarget(Node)}),
 * and the set of possible step to navigate the DOM tree {@link #availableFrom(Node)}.
 * 
 */
public interface XPathFragmentFactory {

    static final public List<String> DATA_STEP_FACTORIES = XFPConfig.getList(Constants.DATA_STEP_FACTORIES);
    static final public List<String> LINK_STEP_FACTORIES = XFPConfig.getList(Constants.LINK_STEP_FACTORIES);

    
    static final public int MAX_PIVOT_DISTANCE = XFPConfig.getInteger(Constants.MAX_PIVOT_DISTANCE);
    static final public int MIN_PIVOT_LENGTH   = XFPConfig.getInteger(Constants.MIN_PIVOT_LENGTH);
    static final public int MAX_PIVOT_LENGTH   = XFPConfig.getInteger(Constants.MAX_PIVOT_LENGTH);
    static final public int MAX_VALUE_LENGTH   = XFPConfig.getInteger(Constants.MAX_VALUE_LENGTH);

    static final public XPathFragment NFP_FRAGMENT = new NFPfragment();
    static final public XPathFragment DFP_FRAGMENT = new DFPfragment();
    
    abstract public boolean isSuitablePivot(Node node);

    abstract public boolean isSuitableTarget(Node node);

    final class DFPfragment extends XPathFragment implements XPathFragmentFactory {

        private DFPfragment()	{
            super(DATA_STEP_FACTORIES);
        }

        @Override
        public boolean isSuitablePivot(Node node) {
            // N.B. an element with an id attribute always plays as a pivot
            return  ( (isElement(node) && 
            		(isNodeWithIdAttribute(node) || TemplateUtil.isTemplateNode(node))// || isNodeWithClassAttribute(node))
                    || TemplateUtil.isTemplateNode(node) && DocumentUtil.isTextOfLength(node,  MIN_PIVOT_LENGTH, MAX_PIVOT_LENGTH)) ) ;
        }

        @Override
        public boolean isSuitableTarget(Node node) {
            return DocumentUtil.isTextOfLength(node, 1, MAX_VALUE_LENGTH);// && isPCDATAwithVariantContent(node);
        }

        @Override
        public boolean coverMultiValued() {
         // return false; // so that this fragment only covers detail rules
            return false;  // so that this fragment also covers result rules
        }

        @SuppressWarnings("unchecked")
        @Override
        public RuleFactory<String> getRuleFactory() {
            return RuleFactory.DATA_RULE_FACTORY;
        }

    }

    final class NFPfragment extends XPathFragment implements XPathFragmentFactory {

        private NFPfragment()	{
            super(LINK_STEP_FACTORIES);
        }

        @Override
        public boolean isSuitablePivot(Node node) {
            return  !isSuitableTarget(node) &&
                    ( TemplateUtil.isTemplateNode(node) || isNodeWithIdAttribute(node) || DocumentUtil.isTextOfLength(node,  MIN_PIVOT_LENGTH, MAX_PIVOT_LENGTH) );
        }

        @Override
        public boolean isSuitableTarget(Node node) {
            return isAnchorNode(node) ;
        }

        @Override
        public boolean coverMultiValued() {
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public RuleFactory<URI> getRuleFactory() {
            return RuleFactory.LINK_RULE_FACTORY;
        }

    }

}
