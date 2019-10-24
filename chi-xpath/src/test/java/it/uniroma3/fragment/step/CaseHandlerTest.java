package it.uniroma3.fragment.step;

import static it.uniroma3.fragment.step.CaseHandler.HTML_STANDARD_CASEHANDLER;
import static it.uniroma3.fragment.step.CaseHandler.LEAVE_CASE_AS_IT_IS;
import static it.uniroma3.fragment.step.CaseHandler.LOWER_CASEHANDLER;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CaseHandlerTest {

    @Test
    public void testCase_HTMLStandard() {
        assertEquals("A", HTML_STANDARD_CASEHANDLER.elementCase("a"));
        assertEquals("A", HTML_STANDARD_CASEHANDLER.elementCase("A"));
        assertEquals("id", HTML_STANDARD_CASEHANDLER.attributeCase("id"));
        assertEquals("id", HTML_STANDARD_CASEHANDLER.attributeCase("ID"));
    }

    @Test
    public void testCase_LowerCase() {
        assertEquals("a", LOWER_CASEHANDLER.elementCase("a"));
        assertEquals("a", LOWER_CASEHANDLER.elementCase("A"));
        assertEquals("id", LOWER_CASEHANDLER.attributeCase("id"));
        assertEquals("id", LOWER_CASEHANDLER.attributeCase("ID"));
    }

    @Test
    public void testCase_LeaveAsItis() {
        assertEquals("a", LEAVE_CASE_AS_IT_IS.elementCase("a"));
        assertEquals("A", LEAVE_CASE_AS_IT_IS.elementCase("A"));
        assertEquals("id", LEAVE_CASE_AS_IT_IS.attributeCase("id"));
        assertEquals("ID", LEAVE_CASE_AS_IT_IS.attributeCase("ID"));
    }
}
