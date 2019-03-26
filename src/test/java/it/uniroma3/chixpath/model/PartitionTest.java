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

public class PartitionTest {
	public Page page1;
	public Page page2;
	PageClass pageClass1;
	PageClass pageClass2;
	PageClass pageClass3;
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
        pageClass1 = new PageClass();
        pageClass2 = new PageClass();
        pageClass3 = new PageClass();
		Set<Page> set = new HashSet<>();
		Set<Page> set2 = new HashSet<>();
		Set<Page> set3 = new HashSet<>();
		set.add(page1);
		set2.add(page1);
		set2.add(page2);
		set3.add(page2);
		pageClass1.setPages(set);
		pageClass2.setPages(set2);
		pageClass3.setPages(set3);
	}
	
	@Test
	public void SamePartition() {
		Partition p1 = new Partition();
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		p1.setPageClasses(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass1);
		Partition p2 = new Partition();
		p2.setPageClasses(set2);
		assertTrue(p1.samePartition(p2));
	}
	@Test
	public void PartialSame() {
		Partition p1 = new Partition();
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		p1.setPageClasses(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass2);
		Partition p2 = new Partition();
		p2.setPageClasses(set2);
		assertFalse(p1.samePartition(p2));
	}
	@Test
	public void DifferentPartition() {
		Partition p1 = new Partition();
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		p1.setPageClasses(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass3);
		Partition p2 = new Partition();
		p2.setPageClasses(set2);
		assertFalse(p1.samePartition(p2));
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
