package xfp.model;


import static it.uniroma3.hlog.HypertextualUtils.linkTo;
import static xfp.util.DocumentUtil.removeEmptyWhitespaces;
import static xfp.algorithm.RedundancySeeker.normalizeText;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import it.uniroma3.dom.visitor.DOMVisitor;
import it.uniroma3.dom.visitor.DOMVisitorListenerAdapter;
import it.uniroma3.hlog.render.Renderable;
import xfp.template.DocumentNormalizer;

/**
 * 
 * A web page from a {@link Website} of a vertical {@link Domain} in a {@link Dataset}
 */
public class Webpage implements Renderable, Comparable<Webpage> {

    
    private Website website;

    private URI uri;

    /* This is the index of this page over the collection of all 
     * the pages collected from its source website
     */
    private int index;

    private Document doc; // full DOM-representation

    private boolean normalized;

    private Set<String> content;

    private String golden;
    
    public Webpage(URI uri) {
        Objects.requireNonNull(uri);
        this.uri = uri;
        this.normalized = false;
        this.content = null;
        this.golden = null;
    }

    public URI getURI() {
        return this.uri;
    }

    /**
     * 
     * @param website
     * @param pageIndex the index of this page within the list of pages in its site
     */
    public void setWebsite(Website website, int pageIndex) {
        this.website = website;
        this.index   = pageIndex;
    }

    public Website getWebsite() {
        return this.website;
    }

    public int getIndex() {
        return this.index;
    }

    public Document getDocument() {
        if (this.doc==null)
            throw new IllegalStateException("Load the page before requiring its DOM tree");
        return this.doc;
    }

    synchronized public void normalize() {        
        synchronized (this.doc) {
            if (!this.normalized) {
                // N.B. normalization is needed for applying XPath extraction rules
                //      without loosing DOM annotations used for template analysis
                final DocumentNormalizer normalizer = new DocumentNormalizer();
                normalizer.normalize(this.doc);
                this.normalized = true;
            }
        }
    }

    synchronized public void loadDocument() {
        if (this.doc==null) {
            this.doc = WebFetcher.getInstance().fetchDocument(this);
            synchronized (this.doc) {
                removeEmptyWhitespaces(this.doc);
            }
            this.normalized = false;
        }// ELSE already loaded
    }

    /**
     * Get rid of the heavy {@linkplain org.w3c.dom.Document} representation
     */
    synchronized public void releaseDocument() {
        this.doc = null;
    }

    /**
     * Returns the <EM>local</EM> file name associated with this page. 
     * That's useful for pages stored in the local file-system, 
     * i.e., for locally stored datasets.
     * 
     * @return the local file name
     */
    public String getName() {
        return getLocalName(this);
    }

    /**
     * Realizes a naming mechanism to map remote names (i.e., the
     * {@link URI} of {@link Webpage}) to a local names (i.e., the
     * relative name of a file contained in a local folder.
     * 
     * @param page - the page to give a local name to
     * @return the local name of the given page
     */
    static public String getLocalName(Webpage page) {
        return getLocalName(page.getURI());
    }

    static public String getLocalName(URI uri) {
        final String pathSteps[] = uri.getPath().split("/");
        return pathSteps[pathSteps.length-1]; //last step is the filename
    }

    public Set<String> getContent() {
        if (this.content==null) {
            this.content = new HashSet<>();
            new DOMVisitor(new DOMVisitorListenerAdapter() {
                @Override
                public void text(Text text) {
                        content.addAll(normalizeText(text));
                }
            }).visit(getDocument()); 
        }
        return this.content;
    }
    
    public String getGoldenPageClass() {
        return this.golden;
    }

    public void setGoldenPageClass(String pageclass) {
        this.golden = pageclass;
    }

    @Override
    public String toHTMLstring() {
        return linkTo(this.getURI()).withAnchor(this.getName()+getGoldenSuffix()).toString();
    }

    private String getGoldenSuffix() {
        final String golden = this.getGoldenPageClass();
        return ( golden!=null ? "<SUP>&pr;" + golden + "&sc;</SUP>" : "" ) ;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getName())+Objects.hashCode(this.getWebsite());
    }

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof Webpage)) return false;
        final Webpage that = (Webpage)o;
        return Objects.equals(this.getName(),   that.getName()   ) && 
               Objects.equals(this.getWebsite(),that.getWebsite());
    }

    @Override
    public String toString() {
        return this.getName().toString() + getGoldenSuffix();
    }

    @Override
    public int compareTo(Webpage that) {
        return this.getName().compareTo(that.getName());
    }
    
    //
    public void setDocument(Document doc) {
    	this.doc = doc;
    }
    //
}