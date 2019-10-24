package it.uniroma3.chixpath.model;

import java.util.HashMap;

import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * A solution to the {@link ChiProblem}.
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class ChiSolution {

    private ChiProblem problem; // The problem instance this solution is for...

    /**
     * The characteristic XPath expressions associated with each template
     */
    private Map<String, String> templateId2chiXPath;
    
    public ChiSolution(ChiProblem problem) {
        this.problem = problem;
        this.templateId2chiXPath = new HashMap<>();
    }

    public ChiProblem getCharacteristicProblem() {
        return this.problem;
    }
    
    /**
     * @return true iff a Chi-XPath has been found for every input template
     */
    public boolean hasBeenFound() {
        return this.templateId2chiXPath.values().stream()
                   .allMatch( exps -> exps!=null && !exps.isEmpty() );
    }
    
    /**
     * @return true iff a Chi-XPath has been found for any of input templates
     */
    public boolean hasBeenPartiallyFound() {
        return this.templateId2chiXPath.values().stream()
                   .anyMatch( exps -> exps!=null && !exps.isEmpty() );
    }
    
    public void setCharacteristicXPaths(Map<String, String> id2chiXPath) {
        Objects.requireNonNull(id2chiXPath);
        this.templateId2chiXPath.putAll(id2chiXPath);
    }
    
    public void setCharacteristicXPathForTemplate(String templateSampleId, String chixpath) {
        Objects.requireNonNull(templateSampleId);
        this.templateId2chiXPath.put(templateSampleId, chixpath);
    }

    public String getCharacteristicXPathForTemplate(String templateSampleId) {
        Objects.requireNonNull(templateSampleId);
        return this.templateId2chiXPath.get(templateSampleId);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"\n"+
                this.templateId2chiXPath.keySet().stream().sorted()
                    .map(id -> id + " -> " + templateId2chiXPath.get(id))
                    .collect(joining("\n\t", "\t", ""));
    }
    
}
