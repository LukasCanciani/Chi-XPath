package xfp.generation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static xfp.generation.XPathFragmentFactory.DFP_FRAGMENT;
import static xfp.generation.XPathFragmentFactory.NFP_FRAGMENT;
import static xfp.test.TestUtils.getUniqueByXPath;
import static xfp.test.TestUtils.makeDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xfp.AbstractXFPtest;
import xfp.generation.XPathStepFactory.*;

public class XPathFragmentTest extends AbstractXFPtest {

	private Document document;
	private Node _HTML_node;

    @Before
    public void setUp() {
    	this.document = makeDocument("<h2>drinks person1 likes</h2>"
        		+ "<ul class=\"pivot\">"
        		+ "<li>Coffee</li>"
        		+ "<li>Tea</li>"
        		+ "</ul>"
        		+ "<a href=\"page.html\">anchor</a>");
        this._HTML_node = getUniqueByXPath(this.document, "//HTML");
    }
	
	@Test
	public void testNFPFragmentStepsAvailable()	{
		List<String> names = DFP_FRAGMENT.getStepFactories().stream().map(l -> l.getClass().getName()).collect(Collectors.toList());
		assertEquals(names.size(),3);
		assertThat(names, containsInAnyOrder(Down.class.getName(),Up.class.getName(),LinkHunter.class.getName()));
	}
	
	@Test
	public void testDFPfragmentStepsAvailable()	{
		List<String> names = DFP_FRAGMENT.getStepFactories().stream().map(l -> l.getClass().getName()).collect(Collectors.toList());
		assertEquals(4, names.size());
		assertThat(names, containsInAnyOrder(Down.class.getName(),Up.class.getName(),Sniper.class.getName(),RightText.class.getName()));
	}
	
	@Test
	public void testNFPFrom()	{
		NFP_FRAGMENT.build(document);
		// create ground truth
		NodeList nl = this._HTML_node.getChildNodes();
		List<String> gt = new ArrayList<>();
		for (int i=0; i<nl.getLength(); i++) gt.add(nl.item(i).toString());
		gt.add("[A: null]");
		
		Set<XPathStep> steps = NFP_FRAGMENT.availableFrom(this._HTML_node);
		List<String> to = new ArrayList<>();
		for (XPathStep s : steps) to.add(s.getTo().toString());
		for (String s : to) {
			assertThat(gt, hasItem(s));
		}
	}
	
	@Test
	public void testDFPFrom()	{
        NFP_FRAGMENT.build(document);

		NodeList nl = this._HTML_node.getChildNodes();
		List<String> gt = new ArrayList<>();
		for (int i=0; i<nl.getLength(); i++) gt.add(nl.item(i).toString());
		gt.add("[A: null]");
		
		Set<XPathStep> steps = NFP_FRAGMENT.availableFrom(this._HTML_node);
		List<String> to = new ArrayList<>();
		for (XPathStep s : steps) to.add(s.getTo().toString());
		for (String s : to) {
			assertThat(gt, hasItem(s));
		}
	}

}
