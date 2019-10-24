package xfp.generation;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import static xfp.template.TemplateMarker.INVARIANT_MARKER;
import static xfp.template.TemplateMarker.VARIANT_MARKER;
import static xfp.test.TestUtils.getUniqueByXPath;
import static xfp.test.TestUtils.makeDocument;
import static xfp.generation.XPathFragmentFactory.*;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import xfp.AbstractXFPtest;

public class BackwardTreeExplorerTest extends AbstractXFPtest {

    private Document document;
	private Node _temp_righttext_node;
	private Node _COFFEE_node;
	private Node _A_node;

    @Before
    public void setUp() {
        this.document = makeDocument("<h2>drinks person1 likes</h2>"
        		+ "lefttext"
        		+ "<ul class=\"pivot\">"
        		+ "<li>Coffee</li>"
        		+ "<li>Tea</li>"
        		+ "</ul>"
        		+ "righttext"
        		+ "<a href=\"page.html\">anchor</a>");
        this._temp_righttext_node = getUniqueByXPath(this.document, "//*[text()[contains(.,'righttext')]]/text()[1]");
        this._COFFEE_node = getUniqueByXPath(document, "//*[text()[contains(.,'Coffee')]]/text()");
        this._A_node = getUniqueByXPath(document, "//A");
    }    
    
    // TODO split in 2 separate test classes: tree explorer vs XPath generation
    @Test
    public void testTreeExplorerAndXPathBuilder_data() {
        final BackwardTreeExplorer explorer = new BackwardTreeExplorer(DFP_FRAGMENT);
        DFP_FRAGMENT.build(document);

        //mark a node as invariant
        final Node invariant = pickRighttextLasAtemplateNode(document);
        invariant.setUserData(INVARIANT_MARKER, "INVARIANT", null);

        // mark a node as variant
        final Node variant = pickUpCoffeeAsAvariantTargetNode(invariant);
        variant.setUserData(VARIANT_MARKER, variant.getNodeValue().trim(), null);

        // check whether we find the path
        final Set<Path> nav = explorer.exploreFromTarget(variant);
        assertNavigationsConsistency(nav, variant, invariant);
        assertEquals(2, nav.size());

        final List<String> rules = buildXPathExpressions(nav);
        assertThat(rules, containsInAnyOrder(
                "//UL[@class='pivot']/LI/.//text()[1]/self::text()",
                "//UL[@class='pivot']/LI/text()/self::text()"));
    }

    static private void assertNavigationsConsistency(final Set<Path> nav, final Node from, final Node to) {
        for (Path n : nav)	{
            // check their are consistent
            assertEquals(from.toString(), from, n.getEnd());
            assertEquals(to.toString(), to, n.getStart());
        }
    }

    @Test
    public void testTreeExplorerAndXPathBuilder_navigation() {
        final BackwardTreeExplorer ft = new BackwardTreeExplorer(NFP_FRAGMENT);
        NFP_FRAGMENT.build(document);
        
        final Node invariant = pickRighttextLasAtemplateNode(document);

        final Node variant = pickUpLinkAsAvariantTargetNode(invariant);

        // check whether we find the path
        final Set<Path> nav = ft.exploreFromTarget(variant);
        assertNavigationsConsistency(nav, variant, invariant);
        assertEquals(1, nav.size());

        //test rule generation
        final List<String> rules = buildXPathExpressions(nav);
        assertThat(rules, hasItem("//UL[@class='pivot']/child::*/@href"));
    }

    public List<String> buildXPathExpressions(final Set<Path> navigations) {
        return navigations.stream()
                .map( nav -> nav.getXPathExpression() )
                .collect(toList());
    }

    private Node pickRighttextLasAtemplateNode(Document document) {
        //mark a node as invariant
        Node invariant = this._temp_righttext_node;
        assumeNotNull(invariant);
        assumeTrue("UL".equals(invariant.getNodeName())); // this will be the pivot
        return invariant;
    }

    private Node pickUpCoffeeAsAvariantTargetNode(Node invariant) {
        // mark a node as variant
        Node variant = this._COFFEE_node;
        assumeNotNull(variant);
        assumeTrue("Coffee".equals(variant.getNodeValue())); // this will be the target value
        return variant;
    }
    
    private Node pickUpLinkAsAvariantTargetNode(Node invariant) {
        // mark a node as variant
        Node variant = this._A_node;
        assumeNotNull(variant);
        assumeTrue("A".equals(variant.getNodeName())); // this will be the target value
        return variant;
    }
}
