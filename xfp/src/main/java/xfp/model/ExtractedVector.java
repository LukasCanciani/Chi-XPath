package xfp.model;

import static java.util.stream.Collectors.toSet;

import java.util.*;

import xfp.extraction.ExtractionRule;

/**
 * A vector of {@link Value}s associated with a { @link Type} such that
 * every element in the vector is of that type. The values have been
 * extracted from {@link Webpage}s and then eventually normalized
 * 
 * @see Normalizer, {@link ExtractedVector}
 */
public class ExtractedVector<T> implements Iterable<List<Value<T>>> {

    private Map<Webpage, List<Value<T>>> page2values; // this is an indexed vector

    /* The first rule generating this vector, when it was created */
    private ExtractionRule<T> rule;
    
    /* These are all the rules generating this vector */
    private Set<ExtractionRule<T>> generating;

    public ExtractedVector(ExtractionRule<T> rule) {
        this.rule = rule;
        this.page2values = new HashMap<>();
        this.generating = new HashSet<>();
    }

    public List<Value<T>> getValues(Webpage page) {
        return this.page2values.get(page);
    }

    /**
     * 
     * @return the number of {@link Value}s in this vector
     */
    public long size() {
        return ( this.page2values.values().stream().flatMap(lv -> lv.stream()).count() );
    }

    public boolean isEmpty() {
        return ( this.size()==0 );
    }

    public ExtractionRule<T> getExtractionRule() {
        return this.rule; 
    }
    
    public void addGeneratingRules(Collection<ExtractionRule<T>> rules) {
        this.generating.addAll(rules);
    }
    
    public Set<ExtractionRule<T>> getGeneratingRules() {
        return this.generating;
    }

    public Set<String> getGeneratingRulesXPath() {
        return getGeneratingRules().stream()
                .map(er -> er.getXPath())
                .collect(toSet());
    }

    public void add(Webpage page, List<Value<T>> values) {
        this.page2values.put(page, values);
    }

    public Set<Webpage> getPages()	{
        return this.page2values.keySet();
    }

    @Override
    public Iterator<List<Value<T>>> iterator() {
        return this.page2values.values().iterator();
    }

    public Collection<List<Value<T>>> getValues()	{
        return this.page2values.values();
    }

    public boolean isConstant() {
        return ( this.getValues().stream()
                .flatMap(l -> l.stream())
                .map( v -> v.getValue().toString() )
                .distinct()
                .count() == 1 );
    }

    public boolean isSingleton() {
        return ( this.getValues().size()==1 && this.getValues().iterator().next().size()==1 ) ;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(this.page2values);
    }

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof ExtractedVector))
            return false;
        @SuppressWarnings("rawtypes")
        final ExtractedVector that = (ExtractedVector)o;
        return Objects.equals(this.page2values, that.page2values);
    }
    
    @Override
    public String toString() {
        return super.toString()+
                "["+this.size()+"]"+"\t"+this.page2values;
    }

}