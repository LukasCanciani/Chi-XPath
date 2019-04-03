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
	public PageClass pageClass1;
	public PageClass pageClass2;
	public PageClass pageClass3;
	@Before
	public void setUp() throws Exception {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/test3.html";
		
        String content1 = loadPageContent(url1);
        page1 = createPage(content1,url1,"0");
        
        //creazione pagina2
        String url2 = "file:./src/test/resources/basic/test1.html";
        String content2 = loadPageContent(url2);
        page2 = createPage(content2,url2,"1");
		Set<Page> set = new HashSet<>();
		Set<Page> set2 = new HashSet<>();
		Set<Page> set3 = new HashSet<>();
		set.add(page1);
		set2.add(page1);
		set2.add(page2);
		set3.add(page2);
		pageClass1 = new PageClass(set,null);
		pageClass2 = new PageClass(set2,null);
		pageClass3 = new PageClass(set3,null);
		
	}
	
	@Test
	public void SamePartition() {
		
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		Partition p1 = new Partition(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass1);
		Partition p2 = new Partition(set2);
		assertTrue(p1.samePartition(p2));
	}
	
	@Test
	public void SamePartitionFalse() {
		
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		Partition p1 = new Partition(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass3);
		Partition p2 = new Partition(set2);
		assertFalse(p1.samePartition(p2));
	}
	

	@Test
	public void SamePartitionPartialSame() {
		
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		Partition p1 = new Partition(set1);
		Set<PageClass> set2 = new HashSet<PageClass>(set1);
		set2.add(pageClass2);
		
		Partition p2 = new Partition(set2);
		assertFalse(p1.samePartition(p2));
	}
	@Test
	public void DifferentPartition() {
		
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		Partition p1 = new Partition(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass3);
		Partition p2 = new Partition(set2);
		assertFalse(p1.samePartition(p2));
	}
	
	@Test
	public void DeleteDuplicates() {
		
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		Partition p1 = new Partition(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass1);
		Partition p2 = new Partition(set2);
		assertTrue(p1.samePartition(p2));
		Set<Partition> partitions = new HashSet<>();
		partitions.add(p2);
		partitions.add(p1);
		assertSame(2,partitions.size());
		Set<Partition> noDuplicates = Partition.deleteDuplicates(partitions);
		assertSame(1, noDuplicates.size());
	}
	
	@Test
	public void DeleteDuplicatesPartialSame() {
		
		Set<PageClass> set1 = new HashSet<PageClass>();
		set1.add(pageClass1);
		Partition p1 = new Partition(set1);
		Set<PageClass> set2 = new HashSet<PageClass>();
		set2.add(pageClass1);
		set2.add(pageClass2);
		Partition p2 = new Partition(set2);
		Set<Partition> partitions = new HashSet<>();
		assertFalse(p1.samePartition(p2));
		partitions.add(p2);
		partitions.add(p1);
		assertSame(2,partitions.size());
		Set<Partition> noDuplicates = Partition.deleteDuplicates(partitions);
		assertSame(2, noDuplicates.size());
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
