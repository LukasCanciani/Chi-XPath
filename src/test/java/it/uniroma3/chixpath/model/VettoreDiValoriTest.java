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

import it.uniroma3.chixpath.model.*;
import org.junit.Before;
import org.junit.Test;

public class VettoreDiValoriTest {

	@Before
	public void setUp() throws Exception {
	}
   /*in questo test creo una ClasseDiPagine con dentro due pagine e un xPath "a" qualsiasi. Mi aspetto che
    *  l'xPath "/html" estragga gli stessi valori su entrambe le pagine e quindi il metodo equals() abbia esito positivo.
    *  NOTA:E' DATO PER SCONTATO che l'xPath inserito nel costruttore del vettore MATCHA con le pagine della ClasseDiPagine inserita anch'essa nel costruttore!!
    */
	@Test
	public void stessoVettoretest() throws XPathExpressionException {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/section.html";
        final String content1 = loadPageContent(url1);
        final Page page1 = createPage(content1);
        page1.setUrl(url1);
        page1.setId("0");
        
        //creazione pagina2
        String url2 = "file:./src/test/resources/basic/article1.html";
        final String content2 = loadPageContent(url2);
        final Page page2 = createPage(content2);
        page2.setUrl(url2);
        page2.setId("1");
        
        //insieme di xPath qualsiasi della ClasseDiPagine
        final Set<String> xPaths1 = new HashSet<>();
        xPaths1.add("a");
       
        //insieme di pagine della ClasseDiPagine
        final Set<Page> set1 = new HashSet<>();
        set1.add(page1);
        set1.add(page2);
        
        //creazione ClasseDiPagine
        PageClass c1 = new PageClass();
        c1.setPages(set1);
        c1.setxPaths(xPaths1);
        
        //creazine e test vettori
		Vector vett1 = new Vector("/HTML",c1,2);
		Vector vett2 = new Vector("/HTML",c1,2);
		assertTrue(vett1.equals(vett2));
		
		
		Vector vett3 = new Vector("//H2",c1,2);
		Vector vett4 = new Vector("/HTML",c1,2);
		assertFalse(vett3.equals(vett4));
		
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
