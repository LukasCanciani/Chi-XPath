package it.uniroma3.chixpath.model;

import static it.uniroma3.fragment.test.Fixtures.createPage;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;

public class PageClassTest {
	public Page page1;
	public Page page2;
	@Before
	public void setUp() throws Exception {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/test3.html";
		
        String content1 = loadPageContent(url1);
        page1 = createPage(content1, url1,"0");
        
        //creazione pagina2
        String url2 = "file:./src/test/resources/basic/test1.html";
        String content2 = loadPageContent(url2);
        page2 = createPage(content2,url2,"1");
	}

	@Test
	public void samePages() {
		
		Set<Page> set = new HashSet<>();
		Set<Page> set2 = new HashSet<>();
		set.add(page1);
		set2.add(page1);
		set2.add(page2);
		PageClass pageClass1 = new PageClass(set,null);
		PageClass pageClass2 = new PageClass(set2,null);
		Set<PageClass> pcSet = new HashSet<>();
		pcSet.add(pageClass2);
		assertTrue(pageClass1.hasSamePagesAs(pcSet));
		pageClass2 = new PageClass(null,null);
		pcSet.clear();
		pcSet.add(pageClass2);
		assertFalse(pageClass1.hasSamePagesAs(pcSet));
	}
	
	@Test
	public void containsXPathTrue()  throws XPathExpressionException {
		final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("/HTML",null));
        xPaths1.add(new XPath("HTML",null));
        page1.addXPath(new XPath("HTML",null));
        page1.addXPath(new XPath("/HTML",null));
		Set<Page> set = new HashSet<>();
		set.add(page1);
		PageClass pageClass1 = new PageClass(set,xPaths1);
		Set<PageClass> classes = new HashSet<>();
		classes.add(pageClass1);
		Set<XPath> x2 = new HashSet<>();
		x2.add(new XPath("/HTML",null));
		x2.add(new XPath("HTML",null));
		page2.addXPath(new XPath("/HTML",null));
		page2.addXPath(new XPath("HTML",null));
		Set<Page> set2 = new HashSet<>();
		set2.add(page2);
		PageClass pageClass2 = new PageClass(set2,x2);
		assertTrue(pageClass2.containsXpathsSet(classes));
		
		
	}
	
	@Test
	public void containsXPathFalse()  throws XPathExpressionException {
		final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("/HTML",null));
        xPaths1.add(new XPath("HTML",null));
        page1.addXPath(new XPath("HTML",null));
        page1.addXPath(new XPath("/HTML",null));
		Set<Page> set = new HashSet<>();
		set.add(page1);
		PageClass pageClass1 = new PageClass(set,xPaths1);
		Set<PageClass> classes = new HashSet<>();
		classes.add(pageClass1);
		Set<XPath> x2 = new HashSet<>();
		x2.add(new XPath("a",null));
		x2.add(new XPath("b",null));
		page2.addXPath(new XPath("a",null));
		page2.addXPath(new XPath("b",null));
		Set<Page> set2 = new HashSet<>();
		set2.add(page2);
		PageClass pageClass2 = new PageClass(set2,x2);
		assertFalse(pageClass2.containsXpathsSet(classes));
		
		
	}
	
	@Test
	public void createUniqueXPathsSAME()  throws XPathExpressionException {
		final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("/HTML",null));
        xPaths1.add(new XPath("HTML",null));
        page1.addXPath(new XPath("HTML",null));
        page1.addXPath(new XPath("/HTML",null));
		Set<Page> set = new HashSet<>();
		set.add(page1);
		PageClass pageClass1 = new PageClass(set,xPaths1);
		Set<PageClass> classes = new HashSet<>();
		classes.add(pageClass1);
		assertEquals(2,pageClass1.getxPaths().size());
		PageClass.createUniqueXPaths(classes, 1);
		assertEquals(1,pageClass1.getUniqueXPaths().size());
		
		
	}
	@Test
	public void createUniqueXPathsNOTSAME()  throws XPathExpressionException {
		final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("/HTML",null));
        xPaths1.add(new XPath("a",null));
        page1.addXPath(new XPath("/HTML",null));
        page1.addXPath(new XPath("a",null));
		Set<Page> set = new HashSet<>();
		set.add(page1);
		PageClass pageClass1 = new PageClass(set,xPaths1);
		Set<PageClass> classes = new HashSet<>();
		classes.add(pageClass1);
		assertEquals(2,pageClass1.getxPaths().size());
		PageClass.createUniqueXPaths(classes, 1);
		assertEquals(2,pageClass1.getUniqueXPaths().size());
		
		
	}
	
	@Test
	public void selectCharacteristicXPathOnePage()  throws XPathExpressionException {
		final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("/HTML",null));
        xPaths1.add(new XPath("a",null));
        page1.addXPath(new XPath("/HTML",null));
        page1.addXPath(new XPath("a",null));
        Set<Page> set = new HashSet<>();
		set.add(page1);
		PageClass pageClass1 = new PageClass(set,xPaths1);
		Set<PageClass> classes = new HashSet<>();
		classes.add(pageClass1);
		PageClass.createUniqueXPaths(classes, 1);
		PageClass.selectCharacteristicXPath(classes);
		assertEquals("a",pageClass1.getCharacteristicXPath().getRule());
		
	}
	@Test
	public void selectCharacteristicXPathTwoPage()  throws XPathExpressionException {
		final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("/HTML",null));
        xPaths1.add(new XPath("a",null));
        page1.addXPath(new XPath("/HTML",null));
        page1.addXPath(new XPath("a",null));
        page2.addXPath(new XPath("/HTML",null));
        page2.addXPath(new XPath("a",null));
        Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		PageClass pageClass1 = new PageClass(set,xPaths1);
		Set<PageClass> classes = new HashSet<>();
		classes.add(pageClass1);
		PageClass.createUniqueXPaths(classes, 2);
		PageClass.selectCharacteristicXPath(classes);
		assertEquals("a",pageClass1.getCharacteristicXPath().getRule());
		
	}
	
	@Test
	public void selectCharacteristicXPathTwoClasses()  throws XPathExpressionException {
		final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("/HTML",null));
        xPaths1.add(new XPath("a",null));
        page1.addXPath(new XPath("/HTML",null));
        page1.addXPath(new XPath("a",null));
        
        final Set<XPath> xPaths2 = new HashSet<>();
        xPaths2.add(new XPath("H2",null));
        Set<Page> set = new HashSet<>();
		set.add(page1);
		PageClass pageClass1 = new PageClass(set,xPaths1);
        Set<Page> set2 = new HashSet<>();
		set2.add(page2);
		PageClass pageClass2 = new PageClass(set2,xPaths2);
		Set<PageClass> classes = new HashSet<>();
		classes.add(pageClass1);
		classes.add(pageClass2);
		PageClass.createUniqueXPaths(classes, 2);
		PageClass.selectCharacteristicXPath(classes);
		assertEquals("a",pageClass1.getCharacteristicXPath().getRule());
		
	}
	
	
	static private String loadPageContent(String anURL) {
        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out);
        try {
            final URLConnection conn = new URL(anURL).openConnection();

            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                    );

            String line;
            while ( (line=reader.readLine())!=null) {
                writer.println(line);
            }

            writer.close();
            out.close();
        } catch (Exception e) { 
            throw new RuntimeException(e);
        }

        return out.toString();
    }
}
