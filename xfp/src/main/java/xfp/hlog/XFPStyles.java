package xfp.hlog;


import static it.uniroma3.hlog.HypertextualUtils.linkTo;
import static it.uniroma3.hlog.HypertextualUtils.styled;
import static xfp.hlog.XFPcssClasses.TABLE_HEADER_CSS_CLASS;

import it.uniroma3.hlog.render.ObjectRenderer;
import xfp.model.Webpage;


/**
 * That's just to centralize project-wide methods and constants
 * to facilitate developing {@link ObjectRenderer}s to adopt a uniform aspect.
 */
public class XFPStyles {

	static final public String header(Object o) {
		return styled(TABLE_HEADER_CSS_CLASS, o.toString()).toString();
	}
	
	static final public String nullValue() {
		return nullStyled("null");
	}

	static final public String nullStyled(Object o) {
		return "<span class=\"nullValue\">"+o.toString()+"</span>";
	}
	
	static final public String linkToPage(Webpage page) {
		return ( page!=null ? linkTo(page.getURI()).withAnchor(page.getURI()).toString() : nullValue() );
	}

}
