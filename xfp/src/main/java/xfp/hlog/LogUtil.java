package xfp.hlog;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;

import java.util.Collection;
import java.util.logging.Level;
import java.util.stream.Stream;

import xfp.util.Constants;
import xfp.util.XFPConfig;

public class LogUtil {
    
    static final public boolean PARALLEL_STREAMS_ENABLED = XFPConfig.getBoolean(Constants.PARALLEL_STREAMS_ENABLED);

    // PARALLEL <-> LOG toGather !?
    static final public <T> Stream<T> streamOf(Collection<T> collection) {
        if (PARALLEL_STREAMS_ENABLED) {
            /* N.B. hypertextual-logging does not support multi-thread logging */
            setFragmentLoggingLevel(Level.OFF); /* that's for up-front XPath fragment generation */
                                                /* for fragment searching during rule generation */
            setRuleLoggingLevel(Level.OFF);     /* for XPath extraction rule generation          */
                                                /* for Fixed Point generation / validation       */
            return collection.stream().parallel();
        }
        else return collection.stream().sequential();        
    }

    static public void setRuleLoggingLevel(Level level) {
        getLogger(xfp.fixpoint.RuleInference.class).setLevel(level);
        getLogger(xfp.fixpoint.FPGenerator.class).setLevel(level);
    }
    
    static public void setFragmentLoggingLevel(Level level) {
        getLogger(xfp.generation.XPathFragment.class).setLevel(level);
        getLogger(xfp.generation.XPathStepFactory.class).setLevel(level);
        getLogger(xfp.template.TemplateAnalyzer.class).setLevel(level);
        getLogger(xfp.generation.BackwardTreeExplorer.class).setLevel(level);
    }

}
