package it.uniroma3.fragment;

import static it.uniroma3.fragment.test.CollectionUtils.setOf;
import static it.uniroma3.fragment.test.DOMUtils.getUniqueByXPath;
import static it.uniroma3.fragment.test.DOMUtils.makeDocument;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.TEXTS_FRAGMENT;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import it.uniroma3.fragment.BackwardTreeExplorer;
import it.uniroma3.fragment.Path;
import it.uniroma3.fragment.XPathFragment;
import it.uniroma3.fragment.XPathFragmentSpecification;
import it.uniroma3.fragment.step.Down;
import it.uniroma3.fragment.step.RightText;
import it.uniroma3.fragment.step.XPathStepFactory;
/**
 * 
 *
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
@RunWith(Parameterized.class)
public class BackwardTreeExplorerTest {

    private BackwardTreeExplorer explorer;
    
    private XPathFragmentSpecification specification;

    private XPathFragment fragment;
    
    private Document document;
    
    private Node pivot;
    
    private Node target;

    private Set<PathSpecification> expectedPaths;

    public BackwardTreeExplorerTest(
            XPathFragmentSpecification spec,
            String documentBody, 
            String xpathToTargetNode,
            Set<PathSpecification> expectedPaths) {
        this.specification = spec;
        this.document = makeDocument(documentBody);
        this.fragment = new XPathFragment(specification, this.document);
        this.target = getUniqueByXPath(this.document, xpathToTargetNode);
        this.expectedPaths = expectedPaths;
        this.explorer = new BackwardTreeExplorer(this.fragment);
    }
        
    @Parameters(name = "{index}: doc {1} target node {2} paths = {3}")
    static public Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                TEXTS_FRAGMENT(new Down()).setRange(1), // XPathFragmentSpecification
                "<DIV>target</DIV>", // Document
                "//text()",          // XPath to find unique target node
                setOf(
                    path("//DIV","D")  // specification of an expected path in the DOM tree
                )
            },

            {
                TEXTS_FRAGMENT(new Down(), new RightText()).setRange(1), // XPathFragmentSpecification
                "<DIV><SPAN>label</SPAN>target</DIV>", // Document
                "//DIV/text()",          // XPath to find unique target node
                setOf(
                    path("//DIV","D"), // specification of an expected path in the DOM tree
                    path("//SPAN","RT")  // specification of an expected path in the DOM tree
                )
            },
        });

    }
    
    @Test
    public void testExploreFromTarget() {
        for(PathSpecification pathSpec : this.expectedPaths) {
            this.pivot = getUniqueByXPath(this.document, pathSpec.pivotXPath);
            assertPaths(pathSpec.stepFactories, this.explorer.exploreFromTarget(this.target));            
        }
    }

    private void assertPaths(String[] expectedStepFactoriesShortnames, Set<Path> actualPaths) {
        boolean found = false;
        for(Path actual : actualPaths) {
            if (actual.getStart()==this.pivot && assertStepShortNames(expectedStepFactoriesShortnames, actual)) {
                found = true;
            }
        }
        if (!found)
            fail("path " + expectedStepFactoriesShortnames + " not found within:\n" +
                    actualPaths.stream()
                        .map( p -> Objects.toString(p) )
                        .collect(joining("\n"))
                );
    }


    static private boolean assertStepShortNames(String[] expected, Path actual) {
        return Arrays.asList(expected).equals(
                actual.getSteps().stream().map( step -> step.getShortName()).collect(toList())
            );
    }

    static private PathSpecification path(String pivotXPath, String...stepFactoriesShortNames) {
        return PathSpecification.path(pivotXPath, stepFactoriesShortNames);
    }

    /**
     * An helper class to specify a path in the DOM tree as produced by the {@link BackwardTreeExplorer}.
     * <BR/>
     * It consists of an XPath uniquely specifying the pivot node plus the list of
     * step factories short-names (see {@link XPathStepFactory#getShortName()}) 
     * to specify a path in the DOM tree from the pivot to the target value
     * 
     * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
     * @author Wrapidity team
     * @author Fairhair.ai team
     */
    static private class PathSpecification {
        
        static public PathSpecification path(String pivotXPath, String...factoriesShortNames) {
            return new PathSpecification(pivotXPath, factoriesShortNames);
        }

        private String pivotXPath;
        private String[] stepFactories;

        private PathSpecification(String pivotXPath, String[] stepFactories) {
            this.pivotXPath = pivotXPath;
            this.stepFactories = stepFactories;
        }
                
    }
    
}
