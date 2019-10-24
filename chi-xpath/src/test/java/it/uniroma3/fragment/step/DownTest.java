package it.uniroma3.fragment.step;


import static it.uniroma3.fragment.test.DOMUtils.getUniqueByXPath;
import static it.uniroma3.fragment.test.DOMUtils.makeDocument;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.ALL_FRAGMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;
import org.w3c.dom.Node;

import it.uniroma3.fragment.step.Down;

public class DownTest {

    static private Down down(boolean textTarget, boolean byName, boolean byPos, boolean byPosAndName) {
        final Down down = new Down(textTarget,byName,byPos,byPosAndName);
        down.setXPathFragmentSpecification(ALL_FRAGMENT());
        return down;
    }
    
    @Test
    public void testGetShortName() {
        assertEquals("D", new Down().getShortName());
    }

    @Test
    public void testMakeStepExpressions_DownText() {
        final Node start = getUniqueByXPath(makeDocument("<DIV>value</DIV>"), "//DIV");
        assertDownExpressions(down(true,false,false,false).makeStepExpressions(start), "text()");
    }
    
    @Test
    public void testMakeStepExpressions_DownTextWithPos() {
        final Node start = getUniqueByXPath(makeDocument("<DIV>value</DIV>"), "//DIV");
        assertDownExpressions(down(true,false,true,false).makeStepExpressions(start), "text()[1]");
    }

    @Test
    public void testMakeStepExpressions_Down2TextsWithoutPos() {
        final Node start = getUniqueByXPath(makeDocument("<DIV>value1<BR/>value2</DIV>"), "//DIV");
        assertDownExpressions(down(true,false,false,false).makeStepExpressions(start), "text()");
    }

    @Test
    public void testMakeStepExpressions_Down2TextsWithPos() {
        final Node start = getUniqueByXPath(makeDocument("<DIV>value1<BR/>value2</DIV>"), "//DIV");
        assertDownExpressions(down(true,false,true,false).makeStepExpressions(start), "text()[1]", "text()[2]", "*[1]");
    }

    @Test
    public void testMakeStepExpressions_Down2TextsSeparatedByCommentsWithPos() {
        final Node start = getUniqueByXPath(makeDocument("<DIV>value1<!-- separting comment-->value2</DIV>"), "//DIV");
        assertDownExpressions(down(true,false,true,false).makeStepExpressions(start), "text()[1]", "text()[2]");
    }

    @Test
    public void testMakeStepExpressions_Down3TextsSeparatedByCommentsWithPos() {
        final Node start = getUniqueByXPath(makeDocument("<DIV>value1<!-- c12 -->value2<!-- c23 -->value3</DIV>"), "//DIV");
        assertDownExpressions(down(true,false,true,false).makeStepExpressions(start), "text()[1]", "text()[2]", "text()[3]");
    }
    
    
    @Test
    public void testMakeStepExpressions_DownElement_noTarget() {
        final Node start = getUniqueByXPath(makeDocument("<DIV><HR/></DIV>"), "//DIV");
        assertDownExpressions(down(false,false,false,false).makeStepExpressions(start) /* none */ );
    }

    @Test
    public void testMakeStepExpressions_DownElement_target() {
        final Node start = getUniqueByXPath(makeDocument("<DIV><HR/></DIV>"), "//DIV");
        assertDownExpressions(down(false, true, false, false)
                .makeStepExpressions(start), "HR");
    }
    
    @Test
    public void testMakeStepExpressions_DownElement_targetByPosition() {
        final Node start = getUniqueByXPath(makeDocument("<DIV><HR/></DIV>"), "//DIV");
        assertDownExpressions(down(false,false,true,false)
                .makeStepExpressions(start), "*[1]");
    }

    @Test
    public void testMakeStepExpressions_DownElement_targetByNameAndPos() {
        final Node start = getUniqueByXPath(makeDocument("<DIV><HR/></DIV>"), "//DIV");
        assertDownExpressions(down(false,false,false,true)
                .makeStepExpressions(start), "HR[1]");
    }

    @Test
    public void testMakeStepExpressions_DownElement_targetByNamePosNameAndPos() {
        final Node start = getUniqueByXPath(makeDocument("<DIV><HR/></DIV>"), "//DIV");
        assertDownExpressions(down(false,true,true,true)
                .makeStepExpressions(start), "*[1]", "HR", "HR[1]");
    }

    @Test
    public void testMakeStepExpressions_DownElement_textAndElement() {
        final Node start = getUniqueByXPath(makeDocument("<DIV>value1<HR/>value2</DIV>"), "//DIV");
        assertDownExpressions(down(true,true,true,true)
                .makeStepExpressions(start), "text()[1]", 
                                             "text()[2]", 
                                             "*[1]", 
                                             "HR", 
                                             "HR[1]");
    }

    static private void assertDownExpressions(Set<String> downExpressions, String... expectedExps) {
        final String failureMsg = "Wrong down-step expressions:\n" +
                "\tExpected:\t" + Arrays.toString(expectedExps) + "\n" +
                "\tActual:  \t" + downExpressions + "\n";
        for(String expectedExp : expectedExps)
            assertTrue( failureMsg, downExpressions.contains(expectedExp) );
        assertEquals(failureMsg, expectedExps.length, downExpressions.size());
    }

}
