package it.uniroma3.fragment.util;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;

import org.w3c.dom.Text;

public class XPathExpressionBuilderUtils {

    static public String containsPredicate(Text textualPivot) {
        final String label = getMeaningfulPivotLabel(textualPivot);
        return containsPredicate(".", label);
    }

    static public String containsPredicate(String xpathSubExp, String value) {
        return "[contains("+xpathSubExp+",'" + value + "')]";     
    }

    static public String equalsPredicate(String xpathSubExp, String value) {
        return "["+xpathSubExp+"='" + value + "']";     
    }

    static public String getMeaningfulPivotLabel(Text pivot) {
        return sanitize(TemplateUtils.extractLongestInvariant(pivot));
    }

    static public String sanitize(String label) {
        return label.replaceAll("'", "\"").replace("\u00a0","\\u00a0").trim();
    }

    static public String[] extractWords(String string) {
        return Arrays.stream(string.split("\\W+")) // A non-word character: [^\w]
                .filter( w -> w.matches("\\w+"))   // A word character: [a-zA-Z_0-9]
                .collect(toList())
                .toArray(new String[0]);
    }

}
