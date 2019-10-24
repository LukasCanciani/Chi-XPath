package it.uniroma3.chixpath.model;

import java.util.Collection;

/**
 * A set of {@link Page}s associated with the same <EM>template</EM>
 * induced by the <I>Smart Generator</I>,
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class TemplateSample implements Comparable<TemplateSample>{

    private static int progId=0;
    
    private String id;
    
    private Collection<Page> pages;
    
    private ChiProblem container;

    public TemplateSample() {
        this.setId(Integer.toString(progId++));
    }
    
    public String getId() {
        return this.id;
    }

    public TemplateSample setId(String id) {
        this.id = id;
        return this;
    }

    public Collection<Page> getPages() {
        return pages;
    }

    public TemplateSample setPages(Collection<Page> pages) {
        this.pages = pages;
        return this;
    }

    /**
     * @return the {@link CharacteristicProblem} this template is part of
     */
    public ChiProblem getContainer() {
        return this.container;
    }

    public TemplateSample setContainer(ChiProblem container) {
        this.container = container;
        return this;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        final TemplateSample that = (TemplateSample)o;
        return this.getId().equals(that.getId());
    }
    
    @Override
    public int compareTo(TemplateSample that) {
        return this.getId().compareTo(that.getId());
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName()+getId()+" "+this.getPages();
    }

}
