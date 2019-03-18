package it.uniroma3.chixpath.fragment;


import static it.uniroma3.fragment.test.CollectionUtils.unique;
import static it.uniroma3.fragment.test.DOMUtils.makeDocument;
import static it.uniroma3.fragment.test.Fixtures.createPage;
import static it.uniroma3.fragment.util.XPathUtils.evaluateXPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import it.uniroma3.chixpath.model.PageClass;
import it.uniroma3.chixpath.model.Partition;
import it.uniroma3.chixpath.model.Page;
import it.uniroma3.chixpath.model.TemplateSample;
import it.uniroma3.chixpath.ChiFinder;
import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.fragment.RuleInference;
import it.uniroma3.fragment.XPathFragmentSpecification;

public class ChiFragmentTest {

    private XPathFragmentSpecification specification;
    
    private RuleInference engine;
    
    @Before
    public void setUp() throws Exception {
        this.specification = new ChiFragmentSpecification();
        this.engine = new RuleInference(this.specification);
        
        
        
        
    }
   
    
    @Test
    public void testRuleInference() throws XPathExpressionException {
        final Document doc = makeDocument("<DIV class='c'></DIV>");
        final String xpath = unique( this.engine.inferRules(doc) );
        assertEquals( "DIV[contains(@class,'c')]/@class/self::node()", xpath );
       
        assertTrue(xpath+" not working", evaluateXPath(doc, xpath).getLength()>0);
    }

}
