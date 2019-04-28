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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.uniroma3.chixpath.Partitioner;

public class VectorTest {

	private Page page1;
	private Page page2;

	@Before
	public void setUp() throws Exception {
		//creazione pagina1
		String url1 = "file:./src/test/resources/basic/Test/test3.html";
		final String content1 = loadPageContent(url1);
		page1 = createPage(content1,url1,"0");

		//creazione pagina2
		String url2 = "file:./src/test/resources/basic/Test/test1.html";
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
		xPaths1.add(new XPath("a",null));

		//insieme di pagine della ClasseDiPagine
		final Set<Page> set1 = new HashSet<>();
		set1.add(page1);
		set1.add(page2);

		//creazione ClasseDiPagine

		PageClass c1 = new PageClass(set1,xPaths1);

		//creazine e test vettori
		Vector vett1 = new Vector(new XPath("/HTML",null),c1,2);
		//Vector vett2 = new Vector("/HTML",c1,2);
		Vector vett2 = new Vector(new XPath("HTML",null),c1,2);
		assertTrue(vett1.equals(vett2));



	}
	@Test
	public void sameVectorFalse() throws XPathExpressionException {


		//insieme di xPath qualsiasi della ClasseDiPagine
		final Set<XPath> xPaths1 = new HashSet<>();
		xPaths1.add(new XPath("a",null));

		//insieme di pagine della ClasseDiPagine
		final Set<Page> set1 = new HashSet<>();
		set1.add(page1);
		set1.add(page2);

		//creazione ClasseDiPagine

		PageClass c1 = new PageClass(set1,xPaths1);

		//creazine e test vettori
		Vector vett1 = new Vector(new XPath("/HTML",null),c1,2);
		//Vector vett2 = new Vector("/HTML",c1,2);
		Vector vett2 = new Vector(new XPath("H2",null),c1,2);
		assertFalse(vett1.equals(vett2));



	}
	@Test
	public void sameVectorSameString() throws XPathExpressionException {


		//insieme di xPath qualsiasi della ClasseDiPagine
		final Set<XPath> xPaths1 = new HashSet<>();
		xPaths1.add(new XPath("a",null));

		//insieme di pagine della ClasseDiPagine
		final Set<Page> set1 = new HashSet<>();
		set1.add(page1);
		set1.add(page2);

		//creazione ClasseDiPagine

		PageClass c1 = new PageClass(set1,xPaths1);

		//creazine e test vettori
		Vector vett1 = new Vector(new XPath("/HTML",null),c1,2);
		//Vector vett2 = new Vector("/HTML",c1,2);
		Vector vett2 = new Vector(new XPath("/HTML",null),c1,2);
		assertTrue(vett1.equals(vett2));


	}
	@Test
	public void referencesTest() throws XPathExpressionException{
		String xp = "//UL";
		XPath x1 = new XPath(xp,null);
		final Set<XPath> xpaths = new HashSet<>();
		xpaths.add(x1);
		String url = "file:./src/test/resources/basic/Test/references.html";
		final String content = loadPageContent(url);
		Page page = createPage(content,url,"0");
		final Set<Page> set = new HashSet<>();
		set.add(page);
		PageClass pc = new PageClass(set,xpaths);
		Vector v = new Vector (x1,pc,1);
		for(NodeList nl : v.getExtractedNodes()) {
			System.out.println(nl.getLength());
			int ulNum =nl.getLength();
			for ( int i = 1 ; i<= ulNum; i ++ ) {
				System.out.println("UL " + i);
				String newXP = "//UL[" + i + "]//A";
				XPath x2 = new XPath(newXP,null);
				Vector v2 = new Vector(x2,pc,1);
				for(NodeList nl2 : v2.getExtractedNodes()) {
					for(int j = 0 ; j < nl2.getLength() ; j ++) {
						Node n = nl2.item(j);
						if ( n !=null) {
							NamedNodeMap nn = n.getAttributes();
							if (nn!= null) {
								if ( nn.getNamedItem("href")!=null)
									System.out.println(nn.getNamedItem("href").getNodeValue());
							}
						}
					}
				}
			}
		}

	}

	public void searchNodeList(NodeList nl) {
		for(int i = 0 ; i<nl.getLength(); i ++ ) {
			Node n1 = nl.item(i);
			if (n1 != null)
				searchNode(n1);

		}
	}

	public void searchNode(Node n) {
		NamedNodeMap nnm = n.getAttributes();
		if ( nnm != null) {
			if (nnm.getNamedItem("href")!=null) {
				nnm.getNamedItem("href").getNodeValue();
			}
			if (nnm.getNamedItem("class")!=null) {
				nnm.getNamedItem("class").getNodeValue();
			}
		}
		if (n.getNextSibling()!=null)
			searchNode(n.getNextSibling());
		if(n.getChildNodes() != null)
			searchNodeList(n.getChildNodes());
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
