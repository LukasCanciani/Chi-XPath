package it.uniroma3.fragment.dom;

import java.util.Collections;
import java.util.Set;

import org.w3c.dom.*;

import it.uniroma3.fragment.dom.DOMVisitor.SkippingStrategy;


public class DOMVisitorSkippingStrategies {

    public static SkippingStrategy NOT_SKIPPING_ANYTHING_STRATEGY = new SkippingStrategy() {
        @Override
        public boolean isTextToSkip(Text text) { return false; }
        @Override
        public boolean isTagToSkip(Element element) { return false; }
        @Override
        public boolean isSubtreeToSkip(Element subtreeRoot) { return false; }
        @Override
        public boolean isAttributeToSkip(Attr attribute) { return false; }

    };

    public static class SkippingByNameStrategy implements SkippingStrategy {

        private Set<String> tagsToSkip;
        private Set<String> treesToSkip;
        private Set<String> attrsToSkip;
        
        public SkippingByNameStrategy(Set<String> tagsToSkip, Set<String> treesToSkip) {
            this(tagsToSkip,treesToSkip,Collections.emptySet());
        }

        public SkippingByNameStrategy(Set<String> tagsToSkip, Set<String> treesToSkip, Set<String> attrsToSkip) {
            this.tagsToSkip = tagsToSkip;
            this.treesToSkip = treesToSkip;
            this.attrsToSkip = attrsToSkip;
        }

        @Override
        public boolean isTagToSkip(Element element) {
            return ( this.tagsToSkip.contains(element.getNodeName().toLowerCase()) );
        }

        @Override
        public boolean isSubtreeToSkip(Element subtreeRoot) {
            return ( this.treesToSkip.contains(subtreeRoot.getNodeName().toLowerCase()) );
        }

        @Override
        public boolean isAttributeToSkip(Attr attribute) { 
            return this.attrsToSkip.contains(attribute.getName());
        }

        @Override
        public boolean isTextToSkip(Text text) {
            return false;
        }

    }

}
