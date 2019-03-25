package it.uniroma3.chixpath.model;

import static it.uniroma3.fragment.test.Fixtures.createPage;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
		String url1 = "file:./src/test/resources/basic/section.html";
		
        String content1 = loadPageContent(url1);
        page1 = createPage(content1);
        page1.setUrl(url1);
        page1.setId("0");
        
        //creazione pagina2
        String url2 = "file:./src/test/resources/basic/article1.html";
        String content2 = loadPageContent(url2);
        page2 = createPage(content2);
        page2.setUrl(url2);
        page2.setId("1");
	}

	@Test
	public void samePages() {
		PageClass pageClass1 = new PageClass();
		PageClass pageClass2 = new PageClass();
		Set<Page> set = new HashSet<>();
		Set<Page> set2 = new HashSet<>();
		set.add(page1);
		set2.add(page1);
		set2.add(page2);
		pageClass1.setPages(set);
		pageClass2.setPages(set2);
		Set<PageClass> pcSet = new HashSet<>();
		pcSet.add(pageClass2);
		assertTrue(pageClass1.hasSamePagesAs(pcSet));
		pageClass2.setPages(new HashSet<>());
		assertFalse(pageClass1.hasSamePagesAs(pcSet));
	}
	@Test
	public void createUniqueXPaths()  throws XPathExpressionException {
		final Set<String> xPaths1 = new HashSet<>();
        xPaths1.add("a");
        xPaths1.add("a");
        page1.setXPaths(xPaths1);
		PageClass pageClass1 = new PageClass();
		pageClass1.setxPaths(xPaths1);
		Set<Page> set = new HashSet<>();
		set.add(page1);
		pageClass1.setPages(set);
		ArrayList<PageClass> classes = new ArrayList<>();
		classes.add(pageClass1);
		assertEquals(1,pageClass1.getxPaths().size());
		PageClass.createUniqueXPaths(classes, 1);
		assertEquals(1,pageClass1.getUniqueXPaths().size());
		
		
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
