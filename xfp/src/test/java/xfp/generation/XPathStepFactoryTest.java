package xfp.generation;

import static org.junit.Assert.assertEquals;
import static xfp.test.TestUtils.getUniqueByXPath;
import static xfp.test.TestUtils.makeDocument;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xfp.AbstractXFPtest;
import xfp.generation.XPathStepFactory.Down;
import xfp.generation.XPathStepFactory.LeftElement;
import xfp.generation.XPathStepFactory.RightElement;
import xfp.generation.XPathStepFactory.Up;

public class XPathStepFactoryTest extends AbstractXFPtest {
    
    private static final String VALUE = "value";

    private Document document;
    
    private Node _P_node;
    
    @Before
    public void setUp() {
        this.document = makeDocument("<P>"+VALUE+"</P>");
        this._P_node = getUniqueByXPath(this.document, "//P");
    }
    

	@Test
	public void testDownFactory()	{
		Down dn = new Down(false, false, false, true);
		NodeList nl = dn.eval(this._P_node.getParentNode(), dn.makeStepExpressions(this._P_node.getParentNode()).stream().findFirst().get());
		assertEquals(this._P_node, nl.item(0));
	}
	
	@Test
	public void testDownFactoryText()	{
		Down dn = new Down(true, false, false, false);
		NodeList nl = dn.eval(this._P_node, dn.makeStepExpressions(this._P_node).stream().findFirst().get());
		assertEquals(this._P_node.getFirstChild(), nl.item(0));
	}
	
	@Test
	public void testUpFactory()	{
		assertEquals(this._P_node.getParentNode(), new Up().query(this._P_node));
	}
	
	@Test
	public void testLeftFactory()	{
		assertEquals(this._P_node.getPreviousSibling(), new LeftElement().query(this._P_node));
	}
	
	@Test
	public void testRightFactory()	{
		assertEquals(this._P_node.getNextSibling(), new RightElement().query(this._P_node));
	}
	
	@Test
	public void testRightFactoryText()	{	
		assertEquals(this._P_node.getNextSibling(), new RightElement().query(this._P_node));
	}

}
