package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.test.CollectionUtils.setOf;
import static it.uniroma3.fragment.test.DOMUtils.getUniqueByXPath;
import static it.uniroma3.fragment.test.DOMUtils.makeDocument;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.EMPTY_FRAGMENT;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Node;

import it.uniroma3.fragment.step.XPathStepFactory;

@RunWith(Parameterized.class)
public class XPathStepFactoryTest {

    private XPathStepFactory stepFactory;
    
    private String document;

    private String startNodeXPath;
    
    private String[] expectedSetOfXPathStepExpression;

    public XPathStepFactoryTest(
            String factoryShortName, 
            String documentContent, 
            String startNodeXPath, 
            String... expectedStepExpressions) {
        this.stepFactory = XPathStepFactory.create(factoryShortName);
        this.stepFactory.setXPathFragmentSpecification(EMPTY_FRAGMENT);
        this.document = documentContent;
        this.startNodeXPath = startNodeXPath;
        this.expectedSetOfXPathStepExpression = expectedStepExpressions;
    }

    @Parameters(name = "{index}: {0} on doc {1} from node {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            { "LH", "<DIV><A href=\".\">anchor</A></DIV>",  "//DIV", new String[] {".//A"} },
            { "LH", "<A href=\".\"><DIV>anchor</DIV></A>",  "//DIV", new String[] {/*none*/}},
        });
    }

    @Test
    public void testMakeStepExpressions() {
        assertXPathStepExpressions(this.document, this.startNodeXPath, this.expectedSetOfXPathStepExpression);
    }

    public void assertXPathStepExpressions(String document, String xpathToStartingNode, String... expectedExpressions) {
        final Node from = getUniqueByXPath(makeDocument(document),xpathToStartingNode);
        final Set<String> actualSet = this.stepFactory.makeStepExpressions(from);
        final Set<String> expectedSet = setOf(expectedExpressions);
        assertEquals("Unexpected set of step expressions created", expectedSet, actualSet);
    }

}
