package it.uniroma3.chixpath;

import static it.uniroma3.fragment.test.Fixtures.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import it.uniroma3.chixpath.ChiFinder;
import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.chixpath.model.ChiProblem;
import it.uniroma3.chixpath.model.ChiSolution;

/**
 * 
 *
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
@RunWith(Parameterized.class)
public class ChiFinderTest {

    private ChiProblem problem;
    
    private ChiFinder finder;

    private ChiSolution solution;

    private String[] expectedPaths;

    private boolean solutionExists;
    
    public ChiFinderTest(String testName,
                         String[][] templateSamples, 
                         String[] otherPages, 
                         String[] expectedPaths,
                         Boolean hasSolution) {
        this.problem = createChiProblem(
                templateSamples,
                otherPages
        );
        assertEquals("wrong number of expected characteristic-XPaths", 
                     templateSamples.length, 
                     expectedPaths.length);
        this.expectedPaths = expectedPaths;
        
        this.finder = new ChiFinder(new ChiFragmentSpecification());
        
        this.solutionExists = hasSolution;
    }
    
    @Parameters(name = "{index}: {0} ")
    static public Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {   "none solution",  // a msg for visual identification of parametric tests
                arrays(
                    array("<B>out of fragment</B>")  /* template 0 */
                ),
                array() ,                            // no other pages
                new String[] {null},                 // no characteristic XPath
                false
            },

            {   "basic singleton",  // this is the "testName" for this combination of params
                arrays(
                    array("<DIV class='0'>0</DIV>")  /* template 0 */
                ),
                array() ,                              // no other pages
                array("//DIV[contains(@class,'0')]/@class/self::node()"), // expected chi-XPath 0
                true
            },

            {   "basic doubleton",  // a msg for displaying parametric tests
                arrays(
                    array("<DIV class='0'>0</DIV>"), /* template 0 */
                    array("<DIV class='1'>1</DIV>") /* template 1 */
                ),
                    array("this an-other-page")      , /* other pages */
                    array(
                        "//DIV[contains(@class,'0')]/@class/self::node()", // chi-XPath 0
                        "//DIV[contains(@class,'1')]/@class/self::node()"  // chi-XPath 1
                    ),
                true
            },

            {   "problematic other page",
                arrays(
                    array("<DIV class='0'>0</DIV>"), /* template 0 */
                    array("<DIV class='1'>1</DIV>")  /* template 1 */
                ),
                array( 
                      "<DIV class='0'>1</DIV>"    /* other pages */
                ) ,
                array(
                    null,                                    // chi-XPath 0
                    "//DIV[contains(@class,'1')]/@class/self::node()"  // chi-XPath 1
                ) ,
                false // none solution
            },
            
            {   "not characteristic involved",
                arrays(
                    array("<DIV class='0'>0</DIV><DIV class='1'>1</DIV>"), /* template 1 */
                    array("<DIV class='2'>2</DIV><DIV class='0'>0</DIV>")  /* template 2 */
                ),
                array(), // no other page
                array(
                    "//DIV[contains(@class,'1')]/@class/self::node()",  // chi-XPath 1
                    "//DIV[contains(@class,'2')]/@class/self::node()"   // chi-XPath 2
                ) ,
                true // there is a solution
            },
            
            {   "prefer short XPaths",
                arrays(
                    array("<DIV class='short0'>0</DIV><DIV class='longxxxxx1'>1</DIV>"), /* template 1 */
                    array("<DIV class='short2'>2</DIV><DIV class='longxxxxx3'>3</DIV>")  /* template 2 */
                ),
                array(), // no other page
                array(
                    "//DIV[contains(@class,'short0')]/@class/self::node()", 
                    "//DIV[contains(@class,'short2')]/@class/self::node()" 
                ) ,
                true 
            },
           
            {   "many: 1-2-3-4-5-6 are characteric, X-Y are not",
                arrays(
                    array("<DIV class='1'>1</DIV><DIV class='X'/>"), /* template 1 */
                    array("<DIV class='2'>2</DIV><DIV class='Y'/>"), /* template 2 */
                    array("<DIV class='3'>3</DIV><DIV class='X'/>"), /* template 3 */
                    array("<DIV class='4'>4</DIV><DIV class='Y'/>"), /* template 4 */
                    array("<DIV class='5'>5</DIV><DIV class='X'/>"), /* template 5 */
                    array("<DIV class='6'>6</DIV><DIV class='Y'/>")  /* template 6 */
                ),
                array(), // no other page
                array(
                    "//DIV[contains(@class,'1')]/@class/self::node()",  // chi-XPath 1
                    "//DIV[contains(@class,'2')]/@class/self::node()",  // chi-XPath 2
                    "//DIV[contains(@class,'3')]/@class/self::node()",  // chi-XPath 3
                    "//DIV[contains(@class,'4')]/@class/self::node()",  // chi-XPath 4
                    "//DIV[contains(@class,'5')]/@class/self::node()",  // chi-XPath 5
                    "//DIV[contains(@class,'6')]/@class/self::node()"   // chi-XPath 6
                ) ,
                true // there is a solution
            },
            
            {   "doubleton with X-Y not characteristic",
                arrays(
                    array("<DIV class='1'>1</DIV><DIV class='X'/>" , /* template 1 */
                          "<DIV class='1'>1</DIV><DIV class='Y'/>"), /*  ...  ...  */
                    array("<DIV class='2'>2</DIV><DIV class='X'/>" , /* template 2 */
                          "<DIV class='2'>2</DIV><DIV class='Y'/>")  /*  ...  ...  */
                ),
                array(), // no other page
                array(
                    "//DIV[contains(@class,'1')]/@class/self::node()",  // chi-XPath 1
                    "//DIV[contains(@class,'2')]/@class/self::node()"   // chi-XPath 2
                ) ,
                true // there is a solution
            },
            
            {   "choice by other page ",
                arrays(
                    array("<DIV class='1'>1</DIV><DIV class='X'/>" , /* template 1 */
                          "<DIV class='1'>1</DIV><DIV class='X'/>"), /*  ...  ...  */
                    array("<DIV class='2'>2</DIV><DIV class='Y'/>" , /* template 2 */
                          "<DIV class='2'>2</DIV><DIV class='Y'/>")  /*  ...  ...  */
                ),
                array("<DIV class='1'/>","<DIV class='2'/>"), // others 
                array(
                    "//DIV[contains(@class,'X')]/@class/self::node()",  // chi-XPath 1
                    "//DIV[contains(@class,'Y')]/@class/self::node()"   // chi-XPath 2
                ) ,
                true // there is a solution
            },
            
            {   "choice by other page, prefer short ",
                arrays( /* template 1 */
                    array("<DIV class='A'/><DIV class='1'/><DIV class='XXX'/>" ,
                          "<DIV class='A'/><DIV class='1'/><DIV class='XXX'/>" ,
                          "<DIV class='A'/><DIV class='1'/><DIV class='XXX'/>"
                         ),
                        /* template 2 */
                    array("<DIV class='B'/><DIV class='2'/><DIV class='YYY'/>" ,
                          "<DIV class='B'/><DIV class='2'/><DIV class='YYY'/>" ,
                          "<DIV class='B'/><DIV class='2'/><DIV class='YYY'/>"
                         ) 
                ),
                array("<DIV class='1'/>","<DIV class='2'/>"), // others 
                array(
                    "//DIV[contains(@class,'A')]/@class/self::node()",  // short chi-XPath 1
                    "//DIV[contains(@class,'B')]/@class/self::node()"   // short chi-XPath 2
                ) ,
                true // there is a solution
            },        
        });

    }

    @Test
    public void testFind() {
        assertNotNull(this.problem);
        this.solution = this.finder.find(this.problem);
        assertNotNull(this.solution);
        for(int i=0; i<expectedPaths.length; i++) {
            assertEquals("wrong characteristic-XPath for template "+i, 
                         this.expectedPaths[i], 
                         this.solution.getCharacteristicXPathForTemplate(Integer.toString(i)));
        }
        assertEquals(this.solutionExists, this.solution.hasBeenFound());
    }

    static private String[] array(String...strings) { return strings; }

    static private String[][] arrays(String[]...strings) { return strings; }

}
