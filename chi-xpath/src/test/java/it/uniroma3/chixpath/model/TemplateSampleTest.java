package it.uniroma3.chixpath.model;

import static org.junit.Assert.*;

import org.junit.Test;

import it.uniroma3.chixpath.model.TemplateSample;

public class TemplateSampleTest {
    
    @Test
    public void testEquals_false() {
        assertFalse(new TemplateSample().equals(new TemplateSample()));
    }

    @Test
    public void testEquals_true() {
        assertEquals(new TemplateSample().setId("0"),new TemplateSample().setId("0"));
    }

}
