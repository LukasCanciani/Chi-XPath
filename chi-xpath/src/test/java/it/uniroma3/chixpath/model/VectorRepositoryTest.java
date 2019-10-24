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

public class VectorRepositoryTest {
	public Page page1;
	public Page page2;
	public PageClass pageClass;
	public VectorRepository vr;
	@Before
	public void setUp() throws Exception {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/Test/test3.html";
		
        String content1 = loadPageContent(url1);
        page1 = createPage(content1,url1,"0");
        
        //creazione pagina2
        String url2 = "file:./src/test/resources/basic/Test/test1.html";
        String content2 = loadPageContent(url2);
        page2 = createPage(content2,url2,"1");
        
        Set<Page> set = new HashSet<>();
        set.add(page1);
        set.add(page2);
        pageClass = new PageClass(set,null);
        vr = new VectorRepository(pageClass,2,pageClass.getId());
        
	}
	@Test
	public void SameString() throws XPathExpressionException {
		assertEquals(0,vr.getVectors().size());
		vr.addUnique(new XPath("HTML",null));
		assertEquals(1,vr.getVectors().size());
		vr.addUnique(new XPath("HTML",null));
		assertEquals(1,vr.getVectors().size());
	}
	
	@Test
	public void SameValues() throws XPathExpressionException {
		assertEquals(0,vr.getVectors().size());
		vr.addUnique(new XPath("HTML",null));
		assertEquals(1,vr.getVectors().size());
		vr.addUnique(new XPath("/HTML",null));
		assertEquals(1,vr.getVectors().size());
	}
	
	@Test
	public void DifferentString() throws XPathExpressionException {
		assertEquals(0,vr.getVectors().size());
		vr.addUnique(new XPath("HTML",null));
		assertEquals(1,vr.getVectors().size());
		vr.addUnique(new XPath("a",null));
		assertEquals(2,vr.getVectors().size());	
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
