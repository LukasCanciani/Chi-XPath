package it.uniroma3.chixpath.model;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.LinkedHashSet;

import it.uniroma3.chixpath.ChiFinder;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * 
 * This class models an instance of the <EM>Characteristic XPath Problem</EM>:
 * given a partition of pages from the same site, find one XPath per class of
 * pages (as modeled by {@link TemplateSample}) such that it matches with
 * all the {@link Page}s in the class and it does not with any  {@link Page}
 * from another class.
 * <BR>
 * There is a special class of pages (the <I>others</I>) that needs not to
 * be associated with any XPath and models the pages that should not be 
 * associated with any (other) class. 
 * <BR>
 * The <I>others</I> class contributes to the definition of the problem,
 * as its pages must not match with any XPath expressions associated with 
 * any (other) class.
 * <P>
 * Why chi-abbrevations? check
 * <A href="https://en.wikipedia.org/wiki/Chi_(letter)">https://en.wikipedia.org/wiki/Chi_(letter)</A>
 * </P>
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
@Builder
public class ChiProblem {

    /**
     * The collection of {@link TemplateSample}s, one per template
     */
    @Singular
    @Getter
    private Collection<TemplateSample> templateSamples;
    
    /**
     * These pages have been classified as neither article nor section
     */
    @Singular
    @Getter
    private Collection<Page> otherPages;

    @Getter
    private String siteUrl; 

    public void setTemplateSamples(Collection<TemplateSample> samples) {
        this.templateSamples = new LinkedHashSet<>(samples);
    }

    public void setOtherPages(Collection<Page> samples) {
        this.otherPages = new LinkedHashSet<>(samples);
    }
    
    public TemplateSample getTemplateSample(String templateId) {
        return this.templateSamples.stream().filter( ts -> ts.getId().equals(templateId)).findFirst().orElse(null);
    }
    
    public ChiSolution findSolution() {
        final ChiFinder finder = new ChiFinder();
        return finder.find(this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
               "\nTemplateSamples=\n"+
               this.getTemplateSamples().stream().sorted()
                .map( s -> s.toString() )
                .collect(joining(",\n\t","\t","\n")) +
                "otherPages=" +
                this.getOtherPages();
    }


	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}


}
