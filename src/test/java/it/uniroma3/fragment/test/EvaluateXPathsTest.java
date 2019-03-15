package it.uniroma3.fragment.test;

import static it.uniroma3.fragment.step.CaseHandler.LOWER_CASEHANDLER;
import static it.uniroma3.fragment.test.Fixtures.createPage;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import it.uniroma3.chixpath.ChiFinderMain;
import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.chixpath.model.ClasseDiPagine;
import it.uniroma3.chixpath.model.Page;
import it.uniroma3.chixpath.model.VettoreDiValori;
import it.uniroma3.fragment.RuleInference;

import static it.uniroma3.fragment.util.XPathUtils.evaluateXPath;

public class EvaluateXPathsTest {


	@Test
	public void test() throws XPathExpressionException {
		final String url = "file:./src/test/resources/basic/Articolo1.html";
		final String content = loadPageContent(url);
		final Page page = createPage(content);
		page.setUrl(url);
		page.setId("0");
		Document doc = page.getDocument();
		final RuleInference engine = new RuleInference(new ChiFragmentSpecification());

		boolean uguale=false;

		final Set<Node> nodi = new HashSet<>();

		final Set<String> rules = engine.inferRules(page.getDocument());

		final Set<Page> pages = new HashSet<>();
		pages.add(page);
		ClasseDiPagine classe = new ClasseDiPagine();
		classe.setPages(pages);
		classe.setxPaths(rules);

		//VettoreDiValori vett1 = new VettoreDiValori("//DIV[contains(@class,'title')]/A[1]/text()[contains(.,'Fotocamere')]/self::text()", classe,1);
		//VettoreDiValori vett2 = new VettoreDiValori("//TD[contains(@class,'schedaprodotto_schedatecnica_testo')]/text()[contains(.,'77')]/self::text()", classe,1);

		//if(vett1.stessoVettore(vett2)) System.out.println("stesso vettore");

		NodeList node = evaluateXPath(page.getDocument(), "//DIV[contains(@class,'title')]/A[1]/text()[contains(.,'Fotocamere')]/self::text()");
		NodeList node1 = evaluateXPath(page.getDocument(), "//TD[contains(@class,'schedaprodotto_schedatecnica_testo')]/text()[contains(.,'77')]/self::text()");

		for(String rule : rules) {
			VettoreDiValori vett1 = new VettoreDiValori(rule, classe,1);
			for(String rule1 : rules) {
				VettoreDiValori vett2 = new VettoreDiValori(rule1, classe,1);
				if(!rule.equals(rule1) && vett1.stessoVettore(vett2)) {
					System.out.println("L'xpath" +rule+" e l'xPath "+rule1+" estraggono lo stesso nodo");
				}
			}
		}
	}

	public boolean stessiNodi(NodeList n1, NodeList n2) {
		boolean stessi=false;
		String[] same = new String[n1.getLength()];
		if(n1.getLength()==n2.getLength()) {
			for(int i=0;i<n1.getLength();i++) {
				Node nod1 = n1.item(i);
				System.out.println(""+nod1.getNodeValue());
				for(int j=0; j<n2.getLength();j++) {
					Node nod2 = n2.item(j);
					System.out.println(""+nod2.getNodeValue());
					if(nod1.getNodeValue().equals(nod2.getNodeValue())) {
						same[i]="1";
					}
				}
				for(int k=0;k<same.length;k++) {
					if(same[k]==null) {
						stessi=false;
					}  

					else if(same[k]=="1" && k==same.length-1) return stessi=true;
				}
			}
		}
		return stessi;
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
