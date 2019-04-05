package it.uniroma3.chixpath.model;

import static it.uniroma3.fragment.test.Fixtures.createPage;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;

public class PageTest {
	public Page page1;
	@Before
	public void setUp() throws Exception {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/Test/test1.html";
		
        String content1 = loadPageContent(url1);
        page1 = createPage(content1,url1,"0");
	}
	@Test
	public void addXPath() {
		assertSame(page1.getXPaths().size(),0);
		page1.addXPath(new XPath("a",null));
		assertSame(page1.getXPaths().size(),1);
	}
	@Test
	public void addXPathDifferent() {
		assertSame(page1.getXPaths().size(),0);
		page1.addXPath(new XPath("a",null));
		assertSame(page1.getXPaths().size(),1);
		page1.addXPath(new XPath("b",null));
		assertSame(page1.getXPaths().size(),2);
	}
	@Test
	public void addXPathSame() {
		assertSame(page1.getXPaths().size(),0);
		page1.addXPath(new XPath("a",null));
		assertSame(page1.getXPaths().size(),1);
		page1.addXPath(new XPath("a",null));
		assertSame(page1.getXPaths().size(),1);
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
