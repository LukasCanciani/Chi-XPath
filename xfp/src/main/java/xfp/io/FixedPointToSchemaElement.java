package xfp.io;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import xfp.fixpoint.FixedPoint;

/** this class encapsulates different strategies to map
 * fixed points to schema elements, i.e. column names 
 * in the extracted schema
 * maybe: set of potential names? different heuristics? */
public class FixedPointToSchemaElement<T> {
	public static String useContainsOrClassOrSimpleXPath(FixedPoint<?> f)	{
		String xpath_o = f.getExtractionRule().getXPath();
		String xpath = xpath_o;
		if (StringUtils.countMatches(xpath_o, "contains")>1)
			xpath = xpath_o.substring(0, StringUtils.ordinalIndexOf(xpath_o, "/", 3)); 
		if (xpath.contains("contains"))	{
			return xpath.substring(xpath.indexOf("'")+1, xpath.lastIndexOf("'"));
		}
		// use class
		xpath = xpath_o;
		if (StringUtils.countMatches(xpath_o, "@class='")>1)
			xpath = xpath_o.substring(0, StringUtils.ordinalIndexOf(xpath_o, "/", 3));
		if (xpath.contains("@class='") && checkForClassContent(xpath))	{
			return xpath.substring(xpath.indexOf("@class='")+"@class='".length()+1, xpath.lastIndexOf("'"));
		}
		// return xpath without special chars
		xpath = xpath_o;
        xpath = cleanSingleHeaderElement(xpath);
        return xpath;
	}

	private static boolean checkForClassContent(String xpath)	{
		int i = xpath.indexOf("@class='");
		int li = xpath.lastIndexOf("'");
		if ((i + ("@class='".length()+1)) < li)	return true;
		else return false;
	}
	
	static private String prefix = "p_"; 
	public static List<String> cleanHeader(List<String> header)	{
		Set<String> unique_c_names = new HashSet<>();
		for (int i=0; i < header.size(); i++)	{
			String tmp_header = cleanSingleHeaderElement(header.get(i));
			if (unique_c_names.contains(tmp_header))	{
				int id = getNextFreeId(unique_c_names, tmp_header, 0);
				tmp_header = tmp_header+id;
				unique_c_names.add(tmp_header);
				header.set(i, tmp_header);
			} else {
				header.set(i, tmp_header);
				unique_c_names.add(tmp_header);
			}
		}
		header = header.stream().map(h -> prefix+h).collect(Collectors.toList());
		return header;
	}
	
	private static int getNextFreeId(Set<String> unique_c_names, String tmp_header, int id) {
		if (unique_c_names.contains(tmp_header+id))
			return getNextFreeId(unique_c_names, tmp_header, id+1);
		else
			return id;
	}

	private static String cleanSingleHeaderElement(String xpath) {
		Pattern pt = Pattern.compile("[^a-zA-Z0-9]");
        Matcher match= pt.matcher(xpath);
        while(match.find()) {
            String s= match.group();
            xpath=xpath.replaceAll("\\"+s, "");
        }
        xpath = xpath.trim().toLowerCase().replaceAll("\\s","");
        if (StringUtils.isNumeric(xpath)) xpath = "c"+xpath;
		return xpath;
	}
	
	public static String useXPathExpression(FixedPoint<?> f)	{
		return f.getExtractionRule().getXPath();
	}
}
