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

public class VectorTest {
	
	private Page page1;
	private Page page2;

	@Before
	public void setUp() throws Exception {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/test3.html";
        final String content1 = loadPageContent(url1);
        page1 = createPage(content1,url1,"0");
        
        //creazione pagina2
        String url2 = "file:./src/test/resources/basic/test1.html";
        final String content2 = loadPageContent(url2);
        page2 = createPage(content2,url2,"1");
	}
   /*in questo test creo una ClasseDiPagine con dentro due pagine e un xPath "a" qualsiasi. Mi aspetto che
    *  l'xPath "/html" estragga gli stessi valori su entrambe le pagine e quindi il metodo equals() abbia esito positivo.
    *  NOTA:E' DATO PER SCONTATO che l'xPath inserito nel costruttore del vettore MATCHA con le pagine della ClasseDiPagine inserita anch'essa nel costruttore!!
    */

	
	
	@Test
	public void SameVectorTrue() throws XPathExpressionException {
		
        
        //insieme di xPath qualsiasi della ClasseDiPagine
        final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("a"));
       
        //insieme di pagine della ClasseDiPagine
        final Set<Page> set1 = new HashSet<>();
        set1.add(page1);
        set1.add(page2);
        
        //creazione ClasseDiPagine
        
        PageClass c1 = new PageClass(set1,xPaths1);
        
        //creazine e test vettori
		Vector vett1 = new Vector(new XPath("/HTML"),c1,2);
		//Vector vett2 = new Vector("/HTML",c1,2);
		Vector vett2 = new Vector(new XPath("HTML"),c1,2);
		assertTrue(vett1.equals(vett2));
		
		
		
	}
	@Test
	public void sameVectorFalse() throws XPathExpressionException {
		
        
        //insieme di xPath qualsiasi della ClasseDiPagine
        final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("a"));
       
        //insieme di pagine della ClasseDiPagine
        final Set<Page> set1 = new HashSet<>();
        set1.add(page1);
        set1.add(page2);
        
        //creazione ClasseDiPagine
        
        PageClass c1 = new PageClass(set1,xPaths1);
        
        //creazine e test vettori
		Vector vett1 = new Vector(new XPath("/HTML"),c1,2);
		//Vector vett2 = new Vector("/HTML",c1,2);
		Vector vett2 = new Vector(new XPath("H2"),c1,2);
		assertFalse(vett1.equals(vett2));
		
		
		
	}
	@Test
	public void sameVectorSameString() throws XPathExpressionException {
		
        
        //insieme di xPath qualsiasi della ClasseDiPagine
        final Set<XPath> xPaths1 = new HashSet<>();
        xPaths1.add(new XPath("a"));
       
        //insieme di pagine della ClasseDiPagine
        final Set<Page> set1 = new HashSet<>();
        set1.add(page1);
        set1.add(page2);
        
        //creazione ClasseDiPagine
        
        PageClass c1 = new PageClass(set1,xPaths1);
        
        //creazine e test vettori
		Vector vett1 = new Vector(new XPath("/HTML"),c1,2);
		//Vector vett2 = new Vector("/HTML",c1,2);
		Vector vett2 = new Vector(new XPath("/HTML"),c1,2);
		assertTrue(vett1.equals(vett2));
		
		
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
