package it.uniroma3.fragment.test;

import static it.uniroma3.fragment.util.DocumentUtils.isElement;
import static it.uniroma3.fragment.util.DocumentUtils.isHrefAttribute;
import static it.uniroma3.fragment.util.DocumentUtils.isNodeWithClassAttribute;
import static it.uniroma3.fragment.util.DocumentUtils.isText;
import static java.util.Collections.emptyList;

import org.w3c.dom.Node;

import it.uniroma3.fragment.XPathFragmentSpecification;
import it.uniroma3.fragment.step.LinkHunter;
import it.uniroma3.fragment.step.NamedAttribute;
import it.uniroma3.fragment.step.XPathStepFactory;


public class XPathFragmentSpecifications {

    static public XPathFragmentSpecification EMPTY_FRAGMENT = new XPathFragmentSpecification(emptyList()) {

        @Override
        public boolean isSuitableTarget(Node node) { return false; }
        @Override
        public boolean isSuitablePivot(Node node)  { return false; }
        @Override
        public boolean isCrossable(Node node) { return false; }

    };

    static public XPathFragmentSpecification ALL_FRAGMENT(XPathStepFactory... stepFactories) {
        return new XPathFragmentSpecification(stepFactories) {
        
            @Override
            public boolean isSuitableTarget(Node node) { return true; }
            @Override
            public boolean isSuitablePivot(Node node)  { return true; }
            @Override
            public boolean isCrossable(Node node) { return true; }

        };
    };

    static public XPathFragmentSpecification TEXTS_FRAGMENT(XPathStepFactory... stepFactories) {
        return new XPathFragmentSpecification(stepFactories) {
        
            @Override
            public boolean isSuitableTarget(Node node) { return isText(node); }
            @Override
            public boolean isSuitablePivot(Node node)  { return !isText(node); }
            @Override
            public boolean isCrossable(Node node) { return true; }

        };
    };

    static public XPathFragmentSpecification LINKS_FRAGMENT = new XPathFragmentSpecification(
            new LinkHunter(),    // target //A
            new NamedAttribute() // target @href
        ) {

        @Override
        public boolean isSuitableTarget(Node node) { return isHrefAttribute(node); }
        @Override
        public boolean isSuitablePivot(Node node)  { return isElement(node); }
        @Override
        public boolean isCrossable(Node node) { return true; }

    };
    
    static public XPathFragmentSpecification FRAGMENT_PIVOTING_ON_CLASS = new XPathFragmentSpecification(
            new LinkHunter(),    // target //A
            new NamedAttribute() // target @href
        ) {
        
        @Override
        public boolean isSuitableTarget(Node node) { return isHrefAttribute(node); } 
        @Override
        public boolean isSuitablePivot(Node node) { return isNodeWithClassAttribute(node); }
        @Override
        public boolean isCrossable(Node node) { return true; }

    };
    
}
