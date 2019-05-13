package xfp;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;
//import static xfp.template.TemplateUtil.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Node;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.algorithm.FPAlgorithm;
import xfp.fixpoint.FixedPoint;
import xfp.fixpoint.PageClass;
import xfp.fixpoint.RuleFactory;
import xfp.generation.XPathFragment;
import xfp.generation.XPathStepFactory.Down;
import xfp.generation.XPathStepFactory.RightNamedElement;
import xfp.generation.XPathStepFactory.RightText;
import xfp.generation.XPathStepFactory.Sniper;
import xfp.generation.XPathStepFactory.Up;
import xfp.io.OutputHandlerFactory;
import xfp.model.Webpage;
import xfp.model.Website;
import xfp.util.Constants;
import xfp.util.XFPConfig;

public class GoldenAnalyzer extends Main {
	
    static final private HypertextualLogger log = getLogger();

    public void analyze(Website site) {
        final Optional<Webpage> withoutPageclass = site.getWebpages().stream()
                .filter( p -> (p.getGoldenPageClass()==null) )
                .findAny();
        if (withoutPageclass.isPresent())
            throw new IllegalArgumentException("Page "+withoutPageclass+" in "+site+" has not a golden page class");
        
        final Map<String, List<Webpage>> class2pages = groupPagesByGoldenPageclass(site);
        logGoldenPageClasses(class2pages);
        
        class2pages.forEach( (pc, pages) -> {
            final Set<Webpage> sample = new LinkedHashSet<>(pages);
            /* use custom XPath fragment for this analysis */
            final FPAlgorithm<String> fpa = new FPAlgorithm<String>(new GoldenAnalyzerXPathFragment());
            final PageClass<String> pageclass = fpa.computeFixedPoints(sample);
            Set<FixedPoint<String>> data;
            data = pageclass.getVariant();
            log.page(data.size()+ " variant data fixed points found.", data);
            data = pageclass.getConstant();
            log.page(data.size()+ " costant data fixed points found.",data);
			storeDataFixedPoints(pageclass);
        });
    }

    private Map<String, List<Webpage>> groupPagesByGoldenPageclass(Website site) {
        /* group pages by golden pageclass */
        final Map<String, List<Webpage>> class2pages = site.getWebpages().stream()
                .collect(Collectors.groupingBy(Webpage::getGoldenPageClass));
        return class2pages;
    }

    private void storeDataFixedPoints(final PageClass<String> data) {
        if (XFPConfig.getBoolean(Constants.DATA_FIXEDPOINTS_STORING_ENABLED))
			try {
				OutputHandlerFactory.getDFPOutputHandler().storePageclass(data);
			} catch (Exception e) {
				e.printStackTrace();
			} 
    }
    
    private void logGoldenPageClasses(Map<String, List<Webpage>> class2pages) {
        log.newPage("Golden page classes");
        log.trace("Golden page classes:");
        class2pages.keySet().forEach( pc -> {
            log.trace("<B>"+pc+"</B>");
            log.trace(class2pages.get(pc));                
            log.trace("<HR/>");
        });
        log.endPage();
    }

    static private class AnalyzerRunner extends ExperimentRunner {
        @Override
        public void executeExperimentOn(File websitedir, Website site) {
            log.newPage("Running GoldenAnalyzer on website: "+site);
            final GoldenAnalyzer analyzer = new GoldenAnalyzer();
            upfrontXPathFragmentGeneration(site);
            analyzer.analyze(site);
            
            log.trace("\n"); // flush hyper-logs
            log.endPage();
        }

    }
    
    static final class GoldenAnalyzerXPathFragment extends XPathFragment {
        
        public GoldenAnalyzerXPathFragment() {
            super(new HashSet<>(Arrays.asList( 
//                    new LeftElement(),
                    new RightText(),
//                    new RightElement(),
                    new RightNamedElement(),
                    new Down(true,false,false,true),
                    new Up(),
                    new Sniper()
                    //U,RNE,RT,D,S
                  )));
            //"U,RNE,RT,D,S"
        }
        
        @Override
        public void preprocess(Set<Webpage> sample) {
          // N.B. Either we skip template analysis
          //      log.warn("None template analysis performed!");
          super.preprocess(sample); // do this to enable ...
          // Or we execute an 
          //up-front pageclass-based template analysis
        }

        @Override
        public int getRange() {
            return 3;
        }
        
        @Override
        public boolean isSuitablePivot(Node node) {
            return DFP_FRAGMENT.isSuitablePivot(node); // template analysis performed
//          return !isSuitableTarget(node);            // no template analysis before
        }

        @Override
        public boolean isSuitableTarget(Node node) {
            return DFP_FRAGMENT.isSuitableTarget(node);        // template analysis performed
//          return isTextOfLength(node, 1, Integer.MAX_VALUE); // no template analysis before
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> RuleFactory<T> getRuleFactory() {
            return (RuleFactory<T>) RuleFactory.DATA_RULE_FACTORY;
        }

        @Override
        public boolean coverMultiValued() {
            return DFP_FRAGMENT.coverMultiValued();
        }
        
    }

    static public void main(String[] args) throws Exception {
        final Main main = new Main();
        final ExperimentRunner gan = new AnalyzerRunner();
        main.parseArgs(args);
        main.run(gan);
    }

}
