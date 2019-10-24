package it.uniroma3.fragment.cache;

import static it.uniroma3.fragment.test.DOMUtils.makeDocument;
import static it.uniroma3.fragment.test.XPathFragmentSpecifications.EMPTY_FRAGMENT;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import it.uniroma3.fragment.cache.RulesCache;

public class RulesCacheTest {

    private RulesCache cache;
    
    private Document doc;

    private Set<String> setOfRules = Collections.singleton("//HTML");

    @Before
    public void setUp() throws Exception {
        this.cache = new RulesCache(null); /* null is plausible XPathFragmentSpecification */
        this.doc = makeDocument("");
        this.cache.setRules(this.doc, setOfRules);
    }

    @Test
    public void testGetAndSetRules() {
        assertSame(setOfRules, this.cache.getRules(this.doc));
    }

    @Test
    public void testGetAndSetRules_anotherFragment() {
        assumeTrue(setOfRules==this.cache.getRules(this.doc));
        assertSame(setOfRules, new RulesCache(null).getRules(this.doc));
        assertNotSame(setOfRules, new RulesCache(EMPTY_FRAGMENT).getRules(this.doc));
    }
    
}
