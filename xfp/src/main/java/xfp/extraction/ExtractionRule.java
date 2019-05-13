package xfp.extraction;

import static javax.xml.xpath.XPathConstants.NODESET;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.fixpoint.RuleFactory;
import xfp.model.ExtractedVector;
import xfp.model.Value;
import xfp.model.Webpage;
import xfp.model.Website;

/**
 * An extraction rule to locate either textual leaf (i.e., a node {@link Text}) from a {@link Document},
 * or a link (i.e., a node {@link Element} containing a <CODE>href</CODE> attribute).
 * 
 * The rules are based on {@link XPathExpression}.
 * 
 * Several occurrences of the same text are made distinguishable by means of the method
 * {@link #getOccurrenceMark(Text)}
 *
 */
public class ExtractionRule<T> {
	
	static final private HypertextualLogger log = HypertextualLogger.getLogger();

	static final private XPathFactory XPATHFACTORY = XPathFactory.newInstance();
	
	private RuleFactory<T> valueFactory;
	
	private XPathExpression xpath;

	private Website website;

	final private String xpathString;

	public ExtractionRule(String xpath, RuleFactory<T> valFactory) {
		this.xpathString = xpath;
		this.xpath = compileXPath(xpath);
		this.valueFactory = valFactory;
	}
	
	public Website getWebsite() {
		return this.website;
	}

	public void setWebsite(Website site) {
		this.website = site;
	}

	
	private XPathExpression compileXPath(String exp) {
		try {
			return XPATHFACTORY.newXPath().compile(exp);
		} catch (XPathExpressionException e) {
			throw new IllegalStateException("error compiling XPath expression " + this.xpathString + "\n" + e);
		}
	}

	public String getXPath() {
		return this.xpathString;
	}
	
	public XPathExpression getXPathExpression() {
		return this.xpath;
	}
		
	public List<Value<T>> applyTo(Webpage page) {
        Objects.requireNonNull(page, "Webpage cannot be null");
        page.normalize(); // e.g., collapse sibling text nodes
	    final Document document = page.getDocument();

		synchronized (document) { 
			final NodeList nodeset = (NodeList)applyTo(document);
			final List<Value<T>> values = this.valueFactory.createValues(nodeset);
			// synch since ExtractedValue access document
			// to extract text value and occurrence mark
			values.forEach(v -> v.setPage(page));
			return values;	
		}
	}
	
	public ExtractedVector<T> applyTo(Collection<Webpage> pages) {
	    final ExtractedVector<T> result = new ExtractedVector<T>(this);
		applyTo(pages,result);
		return result;
	}
	
	public void applyTo(Collection<Webpage> pages, ExtractedVector<T> vector) {
		for(Webpage page : pages) {
			final List<Value<T>> values = this.applyTo(page);
			vector.add(page, values);
		}
	}
	
	public Object applyTo(Document document) {
		// N.B. xerces DOM impl. requires clients to serialize 
		//      accesses because of lazy inits in the DOM trees.
	    // If you do not believe it, before getting unpredictable
	    // errors, please check:
		// https://xerces.apache.org/xerces2-j/faq-dom.html#faq-1
		synchronized (document) {
			try {
				return evaluate(document);
			} catch (XPathExpressionException e) {
				log.warn(e.getMessage()+" during "+ this + " application");
				throw new ExtractionException(e);
			}		
		}
	}

	protected Object evaluate(Document document) throws XPathExpressionException {
		/* N.B.: this requires the input document to have been already normalized,
		 *       i.e., contiguous text siblings should have been already merged
		 */
		return (NodeList) this.xpath.evaluate(document, NODESET); /* several nodes */
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getXPath());
	}

	@Override
	public boolean equals(Object o) {
		if (o==null || !(o instanceof ExtractionRule)) return false;
		
		final ExtractionRule<?> that = (ExtractionRule<?>)o;
		return Objects.equals(this.getXPath(),that.getXPath());
	}

	@Override
	public String toString() { 
		return this.getXPath(); 
	}

}
