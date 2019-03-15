package it.uniroma3.fragment;

import static it.uniroma3.fragment.test.CollectionUtils.setOf;
import static it.uniroma3.fragment.test.DOMUtils.getUniqueByXPath;
import static it.uniroma3.fragment.test.DOMUtils.makeDocument;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.ALL_FRAGMENT;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.FRAGMENT_PIVOTING_ON_CLASS;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.uniroma3.fragment.XPathFragment;
import it.uniroma3.fragment.XPathFragmentSpecification;
import it.uniroma3.fragment.step.Down;
import it.uniroma3.fragment.step.NamedAttribute;
import it.uniroma3.fragment.step.RightText;
import it.uniroma3.fragment.step.Up;
import it.uniroma3.fragment.step.XPathStep;

/**
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
@RunWith(Parameterized.class)
public class XPathFragmentTest {

    private XPathFragment fragment;
    
    private Document document;
    
    private String startNodeXPath;
    
    private Set<String> expectedSetOfXPathStepExpressionsFrom;

    private Set<String> expectedSetOfXPathStepExpressionsTo;

    public XPathFragmentTest(
            XPathFragmentSpecification specification,
            String documentContent, 
            String startNodeXPath, 
            Set<String> expectedFrom,
            Set<String> expectedTo) {
        this.document = makeDocument(documentContent);
        this.fragment = new XPathFragment(specification, this.document);   
        this.startNodeXPath = startNodeXPath;
        this.expectedSetOfXPathStepExpressionsFrom = expectedFrom;
        this.expectedSetOfXPathStepExpressionsTo   = expectedTo;
    }
    
    @Parameters(name = "{index}: {0} on doc {1} node {2} ")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            
            { ALL_FRAGMENT(new Up()),   // XPathFragmentSpecification with just ../ as a step
                "<DIV/>",  // Document body, just a DIV element
                "//DIV",          // An XPath to select a unique node on which we check...
                setOf("U"),       // ... the set of expected XPath-steps >from< the node
                setOf(/* none */) // ... the set of expected XPath-steps  >to<  the node
            },

            { ALL_FRAGMENT(new NamedAttribute()),   // XPathFragmentSpecification with LH, NA steps
                "<A href=\"v\">anchor</A>",  // Document body
                "//A",            // An XPath to select a unique node on which we check...
                setOf("NA"),      // ... the set of expected XPath-steps >from< the node
                setOf(/* none */) // ... the set of expected XPath-steps  >to<  the node
            },

            { FRAGMENT_PIVOTING_ON_CLASS,     // XPathFragmentSpecification with LH, NA steps
                "<A class=\"c\" href=\"v\">anchor</A>",  // Document body
                "//A",       // An XPath to select a unique node on which we check...
                setOf("NA"), // ... the set of expected XPath-steps >from< the node
                setOf("LH")  // ... the set of expected XPath-steps  >to<  the node
            },
            
            { ALL_FRAGMENT(new RightText()),
                "<BR/>value",
                "//BR",
                setOf("RT"),
                setOf()
            },
            
            { ALL_FRAGMENT(new Down(true, false, true, false)), // target text with index
                "<DIV>value1<HR/>value2</DIV>",
                "//DIV",
                setOf("D"),
                setOf("D")
            },
            
            { ALL_FRAGMENT(new Up(), new Down()),
                "<DIV>value</DIV>",
                "//DIV/text()", 
                setOf("U"), // ... expected XPath-steps >from< the node
                setOf("D")  // ... expected XPath-steps  >to<  the node
            },

            { ALL_FRAGMENT(new Up(), new Down(), new RightText()),
                "<DIV><SPAN>label:</SPAN> value </DIV>",
                "//DIV/SPAN", 
                setOf("U","D","RT"), // ... expected XPath-steps >from< the node
                setOf("U","D")  // ... expected XPath-steps  >to<  the node
            },
              
          });
    }

    @Test
    public void testFragment() {
        assertXPathStepExpressions(
                this.fragment,
                this.startNodeXPath,
                this.expectedSetOfXPathStepExpressionsFrom,
                this.expectedSetOfXPathStepExpressionsTo
        );
    }

    public void assertXPathStepExpressions(
            XPathFragment fragment, 
            String nodeXPath, 
            Set<String> expectedFromExpressions,
            Set<String> expectedToExpressions   
        ) {
        final Node node = getUniqueByXPath(document, nodeXPath);
        final Set<String> expectedFromSet = expectedFromExpressions;
        final Set<String> expectedToSet = expectedToExpressions;
        assertEquals("Set of step expressions created From node", 
                expectedFromSet, shortnames(this.fragment.availableFrom(node)));
        assertEquals("Set of step expressions created To node", 
                expectedToSet, shortnames(this.fragment.availableTo(node)));
    }

    static private Set<String> shortnames(Set<XPathStep> steps) {
        return steps.stream()
                .map( step -> step.getShortName() )
                .collect(toSet());
    }

}
