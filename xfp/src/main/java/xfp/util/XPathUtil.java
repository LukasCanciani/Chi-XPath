package xfp.util;

import org.w3c.dom.Text;

import xfp.template.TemplateUtil;

public class XPathUtil {

    static public String containsPredicate(Text textualPivot) {
        final String label = getMeaningfulPivotLabel(textualPivot);
        return "[contains(.,'" + label + "')]";     
    }

    static public String getMeaningfulPivotLabel(Text pivot) {
        return sanitize(TemplateUtil.extractLongestInvariant(pivot));
    }

    static public String sanitize(String label) {
        return label.replaceAll("'", "\"").replace("\u00a0","\\u00a0").trim();
    }

}
