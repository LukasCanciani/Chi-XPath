package it.uniroma3.chixpath;

import static it.uniroma3.fragment.step.CaseHandler.*;
import static it.uniroma3.fragment.util.XPathUtils.evaluateXPath;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.chixpath.model.ChiProblem;
import it.uniroma3.chixpath.model.ChiSolution;
import it.uniroma3.chixpath.model.Page;
import it.uniroma3.chixpath.model.TemplateSample;
import it.uniroma3.fragment.RuleInference;
import it.uniroma3.fragment.XPathFragmentSpecification;
/**
 * 
 * Given an instance of the {@link ChiProblem}, this class
 * implements an algorithm to find out am exact solution of the problem,
 * as an instance of {@link ChiSolution}
 * 
 * 
 * @author Valter Crescenzi (Meltwater) <valter.crescenzi@meltwater.com>
 * @author Wrapidity team
 * @author Fairhair.ai team
 */
public class ChiFinder {

    static final private Logger log = LoggerFactory.getLogger(ChiFinder.class);

    static final private int MAX_NUMBER_OF_CHI_RULES_TO_LOG = 5;

    private Map<String,Set<String>> templateId2xpaths;

    private XPathFragmentSpecification xpathFragmentSpecification;

    private RuleInference engine;

    public ChiFinder() {
        this(new ChiFragmentSpecification(LOWER_CASEHANDLER));
        // Default set to handler required by the current version of browser service...
    }
    
    public ChiFinder(XPathFragmentSpecification fragmentSpecification) {
        this.templateId2xpaths = new HashMap<>();
        this.xpathFragmentSpecification = fragmentSpecification;
        this.engine = new RuleInference(this.xpathFragmentSpecification);
    }

    public ChiSolution find(ChiProblem problem) {
        log.trace("Finding an 'exact' solution for Characteristic XPath Problem:\n{}",problem);
        for(TemplateSample sample : problem.getTemplateSamples()) {
            final Set<String> rules = new HashSet<>(generateRulesMachingWith(sample));
            log.trace("There are {} XPath rules matching with template sample '{}': '{}'", rules.size(), sample.getId(), rules);
            this.templateId2xpaths.put(sample.getId(), rules);
        }
        /* syntax-level check: XPath expressions are not really evaluated */
        log.trace("Finding Characteristic XPath at syntax-level for {}",problem.getSiteUrl());
        removeNotCharacteristicXPathsAtSyntaxLevel(this.templateId2xpaths);
        
        /* semantic level check: XPath expressions are truly evaluated */
        log.trace("Finding Characteristic XPath at semantic-level for {}",problem.getSiteUrl());
        removeNotCharacteristicXPathAtSemanticLevel(problem, this.templateId2xpaths);

        final Map<String,String> chiMap = selectCharacteristicXPath(this.templateId2xpaths);
        
        final ChiSolution solution = new ChiSolution(problem);
        solution.setCharacteristicXPaths(chiMap);
        if (solution.hasBeenFound())
            log.trace("Found a complete solution to the Characteristic XPath Problem for site {}", problem.getSiteUrl());
        else if (solution.hasBeenPartiallyFound())
            log.trace("Found a partial solution to the Characteristic XPath Problem for site {}", problem.getSiteUrl());
        else
            log.trace("Cannot find any solution to the Characteristic XPath Problem for site {}", problem.getSiteUrl());
        return solution;
    }

    public void removeNotCharacteristicXPathAtSemanticLevel(ChiProblem problem, Map<String, Set<String>> templateId2xpaths) {

        for(TemplateSample sample : problem.getTemplateSamples()) {
            final Set<String> rules = templateId2xpaths.get(sample.getId());
            
            for(TemplateSample other : problem.getTemplateSamples()) {
                if (sample.equals(other)) continue;                
                log.trace("Checking rules for template sample {} vs {}",sample.getId(),other.getId());
                discardRulesMatchingWith(rules, other.getPages(), "template "+other.getId());
            }            
            log.trace("Checking rules for template sample {} vs 'other' pages",sample.getId());
            discardRulesMatchingWith(rules, problem.getOtherPages(), "others");
            
            /* Next is a redundant check to validate the generated */
            /* XPath expressions against their own sample pages    */
            log.trace("Validating rules for template {}",sample.getId());
            validateRuleSyntaxAndSemantics(rules, sample);
        }
    }

    private Set<String> generateRulesMachingWith(TemplateSample sample) {
        Objects.requireNonNull(sample);
        Objects.requireNonNull(sample.getPages());
        Set<String> rules = null;
        /*  keep only rules matching will *all* pages in the sample */
        for(Page page : sample.getPages()) {
            final Set<String> newRules = engine.inferRules(page.getDocument());
            log.trace("{} XPath rules have been generated on '{}'", newRules.size(), page.getUrl());
            if (rules==null) rules = newRules; // that's the first sample, start from it 
            else rules.retainAll(newRules);    // retain only rules matching with every pages
        }
        return rules;
    }

    /**
     * Remove from a set of rules those not characteristic, i.e., matching with "other" pages
     * @param rules - the set of rules to check
     * @param theOthers - the <I>other</I> pages
     */
    private void discardRulesMatchingWith(Set<String> rules, Collection<Page> theOthers, String name) {
        Objects.requireNonNull(rules);
        Objects.requireNonNull(theOthers);
        final Iterator<String> ruleIt = rules.iterator();
        while (ruleIt.hasNext()) {

            final String xpath = ruleIt.next();
            // in order of this XPath expression to be characteristic, 
            // it should NOT match with any page from any other template
            for(Page page : theOthers) {
                if (isMatchingXPathExpression(xpath, page)) {
                    log.trace("Discarding rule '{}' as it matches a page from {}: '{}'",xpath, name, page.getUrl());
                    ruleIt.remove(); // discard this page
                    break;
                }
            }

        }
    }
    
    private void validateRuleSyntaxAndSemantics(Set<String> rules, TemplateSample sample) {
        Objects.requireNonNull(rules);
        Objects.requireNonNull(sample);
        final Collection<Page> pages = sample.getPages();
        
        final Iterator<String> ruleIt = rules.iterator();
        while (ruleIt.hasNext()) {

            final String xpath = ruleIt.next();
            // In order of this XPath expression to be characteristic, it should match
            // with any page from the template sample it has been generated from 
            for(Page page : pages) {
                if (!isMatchingXPathExpression(xpath, page)) {
                    log.trace("Discarding rule '{}' as it does not match with its own sample page '{}'",xpath, page.getUrl());
                    ruleIt.remove(); // discard this page
                    break;
                }
            }

        }        
    }

    static public boolean isMatchingXPathExpression(String xpath, Page page) {
        try {
        	System.out.println("L'xpath "+xpath+" matcha con "+evaluateXPath(page.getDocument(), xpath).getLength()+ " nodi");
            return ( evaluateXPath(page.getDocument(), xpath).getLength()>0 );
        } catch (XPathExpressionException e) {
            log.error("detected XPath expression not properly working: {}",xpath,e);
//          throw new RuntimeException(e);
            return false;
        }
    }

    public void removeNotCharacteristicXPathsAtSyntaxLevel(Map<String, Set<String>> templateId2xpaths) {
        final String[] ids = templateId2xpaths.keySet().toArray(new String[0]);
        /* remove all xpaths which are not characteristics by means of a
         * quadratic check, first quick based on syntax, then based on true evaluation */
        for(int i=0; i<ids.length; i++) {
            /* check at syntax level */
            final Set<String> xpaths_i = templateId2xpaths.get(ids[i]);
            for(int j=i+1; j<ids.length; j++) {
                final Set<String> xpaths_j = templateId2xpaths.get(ids[j]);
                final Set<String> intersection = intersection(xpaths_i,xpaths_j);
                xpaths_i.removeAll(intersection);
                xpaths_j.removeAll(intersection);
                if (!intersection.isEmpty()) {
                    log.trace("These {} XPath(s) are not characteristic as they are generated "
                            + "from both from template {}'s and template {}'s sample pages:\n\t{}",
                            intersection.size(), ids[i],ids[j], String.join("\n\t", intersection) );
                    log.trace("Template sample {} left with {} rules",ids[i],xpaths_i.size());
                    log.trace("Template sample {} left with {} rules",ids[j],xpaths_j.size());
                }
            }            
        }
    }
    
    private Set<String> intersection(Set<String> setA, Set<String> setB) {
        final Set<String> result = new HashSet<String>(setA);
        result.retainAll(setB);
        return result;
    }

    private Map<String, String> selectCharacteristicXPath(Map<String, Set<String>> id2xpaths) {
        final Map<String, String> result = new HashMap<>();
        for(String id : id2xpaths.keySet()) {
            final Set<String> rules = id2xpaths.get(id);
            log.trace("Template sample {} has {} characteristic XPath(s) ", id, rules.size());
            String bestXPath = null;
            
            if (!rules.isEmpty()) {
                final LinkedList<String> ranking = new LinkedList<String>(rules);
                /* order by XPath expression length */
                ranking.sort(Comparator.<String>comparingInt( xpath -> xpath.length() )
                        .thenComparing( xpath -> xpath.toString() )
                );
                bestXPath = ranking.getFirst();
                log.trace("{} has been selected as characteristic XPath for template sample {}", bestXPath, id);
                final int howManyToLog = Math.min(MAX_NUMBER_OF_CHI_RULES_TO_LOG,  ranking.size());
                log.trace("First {} chi-rules found:\n\t{}", howManyToLog, String.join("\n\t", ranking.subList(0, howManyToLog)));
            }
            else
                log.trace("No characteristic XPath found for template sample {}", id);
            result.put(id, bestXPath);
        }
        return result;
    }

}
