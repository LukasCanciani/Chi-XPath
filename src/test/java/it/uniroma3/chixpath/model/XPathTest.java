package it.uniroma3.chixpath.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class XPathTest {

	@Test
	public void NotEquals() {
		XPath x1 = new XPath("a");
		XPath x2 = new XPath("b");
		assertFalse(x1.equals(x2));
	}
	@Test
	public void Equals() {
		XPath x1 = new XPath("a");
		XPath x2 = new XPath("a");
		assertTrue(x1.equals(x2));
	}
	@Test
	public void NullEquals() {
		XPath x1 = new XPath("a");
		XPath x2 = new XPath(null);
		assertFalse(x1.equals(x2));
	}

}
