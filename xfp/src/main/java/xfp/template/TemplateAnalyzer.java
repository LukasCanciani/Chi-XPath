package xfp.template;

import static xfp.hlog.XFPStyles.header;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.uniroma3.hlog.HypertextualLogger;
import it.uniroma3.hlog.Level;
import it.uniroma3.token.dom.DOMToken;
import it.uniroma3.token.dom.node.DOMNode;
import xfp.model.Webpage;

/**
 * It classifies each node occurrence as either a template (invariant) node
 * or non-template (variant) node, after an ExAlg-like ECGM analysis 
 * (see <a href=http://ilpubs.stanford.edu:8090/548/1/2002-40.pdf"> 
 *    Extracting Structured Data from Web Pages</a>
 * )
 * <br/>
 * It marks the template token occurrences directly on the DOMs.
 * <br/>
 * It is not coupled with the details of the ECGM analysis and
 * LFEQ representation, but it knows both {@link DOMToken} and [DOM]Node.
 */
public class TemplateAnalyzer {
    
    static final private HypertextualLogger log = HypertextualLogger.getLogger();
	
	static private final ECGMFacade facade = new ECGMFacade();
	
	public TemplateAnalyzer() {
	}

	public void findTemplateTokens(Collection<Webpage> input) {
	    log.newPage("Template analysis");
        
        final List<Webpage> samples = new ArrayList<>(input);
		

		/* find template nodes */
		facade.analyze(samples);
		
		/* mark template/invariant nodes vs value/variant nodes */
		final TemplateMarker marker = new TemplateMarker(this);
		marker.markTokens(samples);
		log.endPage();
        logTemplateTokens(this.getTemplateTokens());
	}

	public List<DOMToken> getIntensionalTemplateTokens() {
		return facade.getTokensOfBinaryLFEQs();
	}
	
	public List<DOMToken> getExtensionalTemplateTokens() {
		return facade.getOccurrencesOfBinaryLFEQs();
	}

	public List<DOMToken> getTemplateTokens() {
		return facade.getTokensOfBinaryLFEQs(); // get all the intensional template tokens
	}
	
	public List<DOMNode> getOccurrences(DOMToken templateToken) {
		return facade.getOccurrences(templateToken);
	}

    // TODO refactor with NodeListRenderer
    private void logTemplateTokens(List<DOMToken> templateTokens) {
        if (!log.isLoggable(Level.TRACE)) return;
        
        log.newPage("Found "+templateTokens.size()+" template tokens ");
        log.newTable();
        log.trace(header("depth"), header("token"), header("canonical xpath"));
        templateTokens.forEach(t -> 
            log.trace(
                    t.depth(), 
                    t,
                    t.getAnnotation("xpath")
            )
        );
        log.endTable();
        log.endPage();
    }

}
