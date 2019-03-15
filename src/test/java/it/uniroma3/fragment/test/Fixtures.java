package it.uniroma3.fragment.test;

import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Document;

import it.uniroma3.chixpath.model.ChiProblem;
import it.uniroma3.chixpath.model.ChiProblem.ChiProblemBuilder;
import it.uniroma3.chixpath.model.Page;
import it.uniroma3.chixpath.model.TemplateSample;

public class Fixtures {
    
    static public TemplateSample createTemplateSample(String...pageContents) {
        final TemplateSample result = new TemplateSample();
        final Collection<Page> pages = new LinkedList<>();
        for(String content : pageContents) {
            pages.add(createPage(content));
        }
        result.setPages(pages);
        return result;
    }
    
    static public ChiProblem createChiProblem(String[][] samples, String...others) {
        final ChiProblemBuilder result = ChiProblem.builder();
        int templateCounter = 0;
        for(String[] sample : samples) {
            final TemplateSample templateSample = createTemplateSample(sample);
            result.templateSample(templateSample);
            templateSample.setId(Integer.toString(templateCounter++));
        }
        for(String content : others) {
            result.otherPage(createPage(content));
        }
       
        return result.build();
    }
    
    static public Page createPage(String content) {
        final Document document = DOMUtils.makeDocument(content);
        return new Page(document);
    }
    
    
}
