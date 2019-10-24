package it.uniroma3.fragment.step;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.uniroma3.fragment.step.Down;
import it.uniroma3.fragment.step.LeftElement;
import it.uniroma3.fragment.step.RightElement;
import it.uniroma3.fragment.step.Up;

import static it.uniroma3.fragment.test.DOMUtils.*;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.ALL_FRAGMENT;

/**
 * Unit-tests up/down/left/right
 *
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class WindRoseXPathStepFactoryTest {

    static final private String VALUE = "value";

    private Document document;

    private Node divNode;

    @Before
    public void setUp() {
        this.document = makeDocument("<DIV>"+VALUE+"</DIV>");
        this.divNode = getUniqueByXPath(this.document, "//DIV");
    }

    @Test
    public void testUp() {
        assertEquals(this.divNode.getParentNode(), new Up().query(this.divNode));
    }
    
    @Test
    public void testLeft()      {
        assertEquals(this.divNode.getPreviousSibling(), new LeftElement().query(this.divNode));
    }

    @Test
    public void testRight()     {
        assertEquals(this.divNode.getNextSibling(), new RightElement().query(this.divNode));
    }

    @Test
    public void testRightToText()  {       
        assertEquals(this.divNode.getNextSibling(), new RightElement().query(this.divNode));
    }

    @Test
    public void testDown()	{
        final Down dn = new Down(false, false, false, true);
        dn.setXPathFragmentSpecification(ALL_FRAGMENT());
        final NodeList nl = dn.eval(this.divNode.getParentNode(), 
                                    dn.makeStepExpressions(this.divNode.getParentNode()).stream().findFirst().get());
        assertEquals(this.divNode, nl.item(0));
    }

    @Test
    public void testDownToText()	{
        final Down dn = new Down(true, false, false, false);
        dn.setXPathFragmentSpecification(ALL_FRAGMENT());
        final NodeList nl = dn.eval(this.divNode, 
                                    dn.makeStepExpressions(this.divNode).stream().findFirst().get());
        assertEquals(this.divNode.getFirstChild(), nl.item(0));
    }

}
