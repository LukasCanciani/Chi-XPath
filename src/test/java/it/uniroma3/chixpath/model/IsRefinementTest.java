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

import org.junit.Before;
import org.junit.Test;

public class IsRefinementTest {

	@Before
	public void setUp() throws Exception {
	}
	/*creo due partizioni una raffinamento dell'altra; con tutte le pagine, insiemi di pagine, insiemi di xPath, e insiemi di classi di pagine e verifico
	il corretto funzionamento del metodo isRaffinamentoDi()
	*/
	@Test
    public void testSampePages() {
    	final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("a",null));
        
        final Set<XPath> xPaths2 = new HashSet<>();
        xPaths2.add(new XPath("b",null));
        
        final Set<XPath> xPaths3 = new HashSet<>();
        xPaths3.add(new XPath("c",null));
        
        final Set<XPath> xPaths4 = new HashSet<>();
        xPaths4.add(new XPath("d",null));
        
        final Set<XPath> xPaths5 = new HashSet<>();
        xPaths5.add(new XPath("e",null));
        
        final Set<XPath> xPaths6 = new HashSet<>();
        xPaths6.add(new XPath("e",null));
    	
    	String url1 = "file:./src/test/resources/basic/section.html";
        final String content1 = loadPageContent(url1);
        final Page page1 = createPage(content1, url1, "1");
        
        
        String url2 = "file:./src/test/resources/basic/section.html";
        final String content2 = loadPageContent(url2);
        final Page page2 = createPage(content2, url2, "2");
        
        String url3 = "file:./src/test/resources/basic/section.html";
        final String content3 = loadPageContent(url3);
        final Page page3 = createPage(content3, url3, "3");
        
        
        String url4 = "file:./src/test/resources/basic/section.html";
        final String content4 = loadPageContent(url4);
        final Page page4 = createPage(content4, url4,"4");
        
        String url5 = "file:./src/test/resources/basic/section.html";
        final String content5 = loadPageContent(url5);
        final Page page5 = createPage(content5, url5, "5");
        
        final Set<Page> setTest = new HashSet<>();
        setTest.add(page5);
        
    	final Set<Page> set1 = new HashSet<>();
        set1.add(page1);
        set1.add(page2);
        
        final Set<Page> set2 = new HashSet<>();
        set2.add(page3);
        set2.add(page4);
        
        final Set<Page> set3 = new HashSet<>();
        set3.add(page1);
        set3.add(page2);
        
        final Set<Page> set4 = new HashSet<>();
        set4.add(page3);
        
        final Set<Page> set5 = new HashSet<>();
        set5.add(page4);
        
      // ----------------------------------------------------------------------------------------------------------------- 
        PageClass c1 = new PageClass(set1,xPaths1);
        
        PageClass c2 = new PageClass(set2,xPaths2);
        
        
        PageClass c3 = new PageClass(set3,xPaths3);
      
        
        PageClass c4 = new PageClass(set4,xPaths4);
        
        
        PageClass c5 = new PageClass(set5,xPaths5);
        
        
      // ---------------------------------------------------------------------------------------------------------------------  
            
        Set<PageClass> p1 = new HashSet<>();
        p1.add(c1);
        p1.add(c2);
        
        
        Partition i1 = new Partition(p1);
      // -----------------------------------------------------------------------------------------------------------------------------------  
        Set<PageClass> p2 = new HashSet<>();
        p2.add(c3);
        p2.add(c4);
        p2.add(c5);
        
        
        Partition i2 = new Partition(p2);
      // --------------------------------------------------------------------------------------------------------------------------------  
       // assertTrue(i1.equals(i2)==true);
        
        assertTrue(i2.isRefinementOf(i1));
        assertFalse(i1.isRefinementOf(i2));
        
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
