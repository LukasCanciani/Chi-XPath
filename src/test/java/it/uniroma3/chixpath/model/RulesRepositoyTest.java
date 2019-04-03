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
		String url1 = "file:./src/test/resources/basic/test1.html";
		
        String content1 = loadPageContent(url1);
        page1 = createPage(content1,url1,"0");
        
        //creazione pagina2
        String url2 = "file:./src/test/resources/basic/test2.html";
        String content2 = loadPageContent(url2);
        page2 = createPage(content2,url2,"1");
        
        String url3 = "file:./src/test/resources/basic/test3.html";
        String content3 = loadPageContent(url3);
        page3 = createPage(content3,url3,"2");
	}
	
	@Test
	public void RulesGenerationTest() {
		Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		RulesRepository rp = new RulesRepository(set);
		assertTrue(rp.getXPaths().contains(new XPath("//H2[contains(@class,'article')]/@class/self::node()",null)));
	}
	@Test
	public void RulesGenerationFalseTest() {
		Set<Page> set = new HashSet<>();
		set.add(page3);
		RulesRepository rp = new RulesRepository(set);
		assertFalse(rp.getXPaths().contains(new XPath("//H2[contains(@class,'article')]/@class/self::node()",null)));
	}
	@Test
	public void rulesGenerationPartialTest() { //Solo alcune classi hanno l'xpath cercato
		Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		set.add(page3);
		RulesRepository rp = new RulesRepository(set);
		XPath xp = new XPath("//H2[contains(@class,'folder')]/following-sibling::P/text()[contains(.,'folder')]/self::text()",null);
		boolean found = false;
		for (XPath x : rp.getXPaths()) {
			if (x.equals(xp)) {
				found = true;
				assertFalse(x.getPages().contains(page1));
				assertFalse(x.getPages().contains(page2));
				assertTrue(x.getPages().contains(page3));
			}
		}
		assertTrue(found);
	}
	@Test 
	public void rulesGenerationAllTest() { //tutte le classi hanno l'xpath cercato
		Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		RulesRepository rp = new RulesRepository(set);
		XPath xp = new XPath("//H2[contains(@class,'article')]/@class/self::node()",null);
		boolean found = false;
		for (XPath x : rp.getXPaths()) {
			if (x.equals(xp)) {
				found = true;
				assertTrue(x.getPages().contains(page1));
				assertTrue(x.getPages().contains(page2));
			}
		}
		assertTrue(found);
	}
	
	
	@Test
	public void rulesGenerationNoneTest() { //Nessuna classe ha l'xpath cercato
		Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		RulesRepository rp = new RulesRepository(set);
		XPath xp = new XPath("//H2[contains(@class,'folder')]/following-sibling::P/text()[contains(.,'folder')]/self::text()",null);
		boolean found = false;
		for (XPath x : rp.getXPaths()) {
			if (x.equals(xp)) {
				found = true;
			}
		}
		assertFalse(found);
	}
	
	@Test
	public void rulesGenerationOneOfTwo() { //una classe ha l'xpath cercato
		Set<Page> set = new HashSet<>();
		set.add(page1);
		set.add(page2);
		RulesRepository rp = new RulesRepository(set);
		XPath xp = new XPath("//H2[contains(@class,'article')]/following-sibling::P/text()[contains(.,'Test1')]/self::text()",null);
		boolean found = false;
		for (XPath x : rp.getXPaths()) {
			if (x.equals(xp)) {
				found = true;
				assertTrue(x.getPages().contains(page1));
				assertFalse(x.getPages().contains(page2));
			}
		}
		assertTrue(found);
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
