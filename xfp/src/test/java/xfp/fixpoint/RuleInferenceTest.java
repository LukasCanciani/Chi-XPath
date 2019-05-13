package xfp.fixpoint;


import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static xfp.generation.XPathFragmentFactory.DFP_FRAGMENT;
import static xfp.generation.XPathFragmentFactory.NFP_FRAGMENT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import xfp.AbstractXFPtest;
import xfp.model.Experiment;
import xfp.model.Webpage;
import xfp.model.Website;
import xfp.util.XFPConfig;

public class RuleInferenceTest extends AbstractXFPtest {

    private Website website;
    private Set<Webpage> apSample;
    private Set<Webpage> pcSample;
        
    @Before
    public void setUp() throws ConfigurationException {
        final Experiment experiment = Experiment.makeExperiment("test", "oxpath");
        XFPConfig.getInstance().setCurrentExperiment(experiment);
        experiment.load();
        this.website = experiment.getWebsites().get(0);

        this.apSample = new LinkedHashSet<>(this.website.getAccessPages());
        
        this.pcSample = getPageClassSample();
    }
    
    private Set<Webpage> getPageClassSample() {
        // that's a single pageclass website plus one access page
        final List<Webpage> pc_pages = this.website.getWebpages();
        return pc_pages.stream()
                .filter(p -> p.getName().startsWith("printer"))
                .collect(toSet()); // select printer pages (data-rich)
    }

    @Test
    public void testRuleInference_NavigationalRules() {
        assertEquals(13, new RuleInference(NFP_FRAGMENT).inferRules(this.apSample).size());
    }

    @Test
    public void testRuleInference_DataExtractionRules() throws Exception {
        assertEquals(7, new RuleInference(DFP_FRAGMENT).inferRules(this.pcSample).size());
    }

}
