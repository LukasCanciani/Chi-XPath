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

public class RulesRepositoyTest {
	public Page page1;
	public Page page2;
	public Page page3;
	@Before
	public void setUp() throws Exception {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/article1.html";
		
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
        
        String url3 = "file:./src/test/resources/basic/section.html";
        String content3 = loadPageContent(url3);
        page3 = createPage(content3);
        page3.setUrl(url3);
        page3.setId("2");
	}
	
	@Test
	public void RulesGenerationTest() {
		Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		RulesRepository rp = new RulesRepository(set);
		assertTrue(rp.getXPaths().contains(new XPath("//H2[contains(@class,'article')]/text()[contains(.,'Article')]/self::text()")));
	}
	
	public void createXPath2PagesTest() {
		Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		set.add(page3);
		RulesRepository rp = new RulesRepository(set);
		XPath xp = new XPath("//H2[contains(@class,'article')]/text()[contains(.,'Article')]/self::text()");
		for (XPath x : rp.getXPaths()) {
			if (x.equals(xp)) {
				assertTrue(x.getPages().contains(page1));
				assertTrue(x.getPages().contains(page2));
				assertFalse(x.getPages().contains(page3));
			}
		}
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
