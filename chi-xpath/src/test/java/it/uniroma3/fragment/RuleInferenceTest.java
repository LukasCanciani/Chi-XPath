package it.uniroma3.fragment;

import static it.uniroma3.fragment.test.CollectionUtils.setOf;
import static it.uniroma3.fragment.test.DOMUtils.*;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.*;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;

import it.uniroma3.fragment.RuleInference;
import it.uniroma3.fragment.XPathFragmentSpecification;
import it.uniroma3.fragment.step.Down;

@RunWith(Parameterized.class)
public class RuleInferenceTest {

    private RuleInference ruleInference;

    private Document document;

    private Set<String> expectedRules;

    public RuleInferenceTest(
            String documentBody,
            XPathFragmentSpecification specification,
            Set<String> expectedSetOfRules) {
        this.document = makeDocument(documentBody);
        this.ruleInference = new RuleInference(specification);
        this.expectedRules = expectedSetOfRules;
    }

    @Parameters(name = "{index}: fragment {1} on doc {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                "<A href=\"anurl.html\">anchor</A>", // a document body
                LINKS_FRAGMENT.setRange(2),          // the fragment specification to use
                singleton("//A/@href/self::node()")  // the set of expected rules that should be generated
            },

            {
                "value", // doc body
                TEXTS_FRAGMENT(new Down(true,false,true,false)).setRange(1),
                singleton("//BODY/text()[1]/self::text()")
            },

            {
                "<DIV class='c'>"+
                    "<A href=\"anurl1.html\">anchor1</A>"+
                    "<A href=\"anurl2.html\">anchor2</A>"+
                    "<A href=\"anurl3.html\">anchor3</A>"+
                "</DIV>",
                FRAGMENT_PIVOTING_ON_CLASS.setRange(2),          
                singleton("//DIV[contains(@class,'c')]/.//A/@href/self::node()") 
            },

            {
                "<DIV class='c1 c2'>"+ // double value on class attribute
                    "<A href=\"anurl1.html\">anchor1</A>"+
                "</DIV>",
                FRAGMENT_PIVOTING_ON_CLASS.setRange(2),
                setOf("//DIV[contains(@class,'c1')]/.//A/@href/self::node()",
                      "//DIV[contains(@class,'c2')]/.//A/@href/self::node()"
                     )
            },
            {
                "<DIV class='c1;c2'>"+ // double value on class attribute separated by ;
                    "<A href=\"anurl1.html\">anchor1</A>"+
                "</DIV>",
                FRAGMENT_PIVOTING_ON_CLASS.setRange(2),
                setOf("//DIV[contains(@class,'c1')]/.//A/@href/self::node()",
                      "//DIV[contains(@class,'c2')]/.//A/@href/self::node()"
                     )
            },
        });
    }

    @Test
    public void testInferRules() {
        /* assert the collection of rules generated */
        assertEquals(this.expectedRules, this.ruleInference.inferRules(this.document));

        /* assert that all generated ones are matching rules */
        for(String xpath : this.expectedRules) {
            assertNotNull(xpath+" does not match", getFirstByXPath(this.document, xpath));
        }
        
    }

}
