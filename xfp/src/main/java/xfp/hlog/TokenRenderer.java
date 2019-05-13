package xfp.hlog;

import org.apache.commons.lang.StringEscapeUtils;

import it.uniroma3.hlog.render.ObjectRenderer;
import it.uniroma3.token.dom.Token;

public class TokenRenderer implements ObjectRenderer<Token> {

    @Override
    public String toHTMLstring(Token token) {
        return toString(token);
    }

    @Override
    public Class<? extends Token> getRenderedObjectClass() {
        return Token.class;
    }

    static final private String toString(Token token) {
        StringBuilder result = new StringBuilder();
        if (token.isText()) result.append(escape(token.getValue()));
        else if (token.isTag()) result.append(escape("<" + token.getTag() + ">"));      
        
        return result.toString();
    }

    
    static final private String escape(String s) {
        return StringEscapeUtils.escapeHtml(s);
    }
    
    
}
