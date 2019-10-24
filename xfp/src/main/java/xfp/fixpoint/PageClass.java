package xfp.fixpoint;

import static java.util.Collections.emptySet;

import java.util.Set;

import xfp.model.Webpage;

public class PageClass<T> {

    private Set<Webpage> pages;

    private Set<FixedPoint<T>> variant;

    private Set<FixedPoint<T>> constant;
    
    private final String id;

    static private int idCounter = 0;

    static public synchronized String createID() {
        return Integer.toString(idCounter++);
    }

    static public <T> PageClass<T> emptyPageClass(Set<Webpage> pages) {
        return new PageClass<>(pages, emptySet(), emptySet());
    }

    public PageClass(Set<Webpage> pages, Set<FixedPoint<T>> var, Set<FixedPoint<T>> constant) {
        this.id = createID();
        this.pages = pages;
        this.variant = var;
        this.constant = constant;
    }

    public String getId() {
        return this.id;
    }

    public Set<Webpage> getPages() {
        return this.pages;
    }

    public void setPages(Set<Webpage> pages) {
        this.pages = pages;
    }

    public Set<FixedPoint<T>> getVariant() {
        return this.variant;
    }

    public void setVariant(Set<FixedPoint<T>> variant) {
        this.variant = variant;
    }

    public Set<FixedPoint<T>> getConstant() {
        return this.constant;
    }

    public void setConstant(Set<FixedPoint<T>> constant) {
        this.constant = constant;
    }
 
}
