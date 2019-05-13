package xfp.algorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Text;

import xfp.model.Webpage;
import xfp.util.Constants;
import xfp.util.XFPConfig;

public class RedundancySeeker {
    
    static final public int MIN_REDUNDANT_TEXT_LENGTH = XFPConfig.getInteger(Constants.MIN_REDUNDANT_TEXT_LENGTH);

    private Map<Webpage, Webpage>  to2from = new HashMap<>();
    
    public void link(Webpage from, Webpage to) {
        if (this.to2from.containsKey(to)) return; // at most 1 'from' for each 'to'
        this.to2from.put(to, from);
    }
    
    static final private Pattern BLANKS_REGEXP = Pattern.compile("\\s+");

    static final private Pattern NOALPHANUM_REGEXP = Pattern.compile("\\W");

    static public Set<String> normalizeText(Text text) {
        final String value = text.getNodeValue();
        final String normalized_chars = NOALPHANUM_REGEXP.matcher(value).replaceAll(" ");
        final String normalized_blanks = BLANKS_REGEXP.matcher(normalized_chars).replaceAll(" ");
        return Stream.of(normalized_blanks.trim().split(" "))
                .filter( s -> (s.length()>=MIN_REDUNDANT_TEXT_LENGTH) )
                .collect(Collectors.toSet()); 
    }

    public boolean isRedundant(Webpage to, Text text) {
        final Webpage from = this.to2from.get(to);
        if (from==null) return true;
        final Set<String> textWords = normalizeText(text);
        if (textWords.isEmpty()) return false;
        for(String word : textWords)
            if (from.getContent().contains(word)) return true;
        return false;
    }

    static final public RedundancySeeker instance = new RedundancySeeker();

    static public RedundancySeeker getInstance() {
        return instance;
    }

}
