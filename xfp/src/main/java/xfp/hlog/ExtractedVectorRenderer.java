package xfp.hlog;

import static xfp.hlog.XFPStyles.nullValue;
import static xfp.hlog.XFPcssClasses.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.uniroma3.hlog.render.ObjectRenderer;
import it.uniroma3.hlog.render.TableBuilder;
import xfp.model.ExtractedVector;
import xfp.model.Webpage;

public class ExtractedVectorRenderer implements ObjectRenderer<ExtractedVector<?>> {


    private static final int MAX_VALUES_PER_PAGE  = 16;

    private static final int MAX_RULES_PER_VECTOR =  3;

    @Override
    public String toHTMLstring(ExtractedVector<?> ev) {
        final TableBuilder matrix = new TableBuilder()
                .setBaseWidth(64)
                /* N.B. With Fixed Layout the CSS properties of the header 
                 *      row are automatically used for every ->data row<- */                
                .setFixedLayout()
                .addColumn(1.5,0.1) // page id + small separator
                .addNColumns(MAX_VALUES_PER_PAGE, 1.5) // values
                .addColumn(0.2);    // closing '...' (if needed)
        matrix.table();
        
        /* header */
        matrix.tr();
        matrix
        .th(PAGE_ID_HEADER_CSS_CLASS,"page")
        .th("")
        .th(VALUES_HEADER_CSS_CLASS,"value(s):");
        for(int i=0; i<MAX_VALUES_PER_PAGE; i++)
            matrix.th("");
        matrix._tr();
        
        /* data */
        final List<Webpage> ordered = new ArrayList<>(ev.getPages());
        Collections.sort(ordered);
        ordered.stream().forEach( p -> {
            /* page related information */
            matrix.tr();
            matrix.td(PAGE_ID_CSS_CLASS, p.getName(), nullValue());
            matrix.td("border: none;","&nbsp;");
            final List<?> values = ev.getValues(p);
            values.stream().limit(MAX_VALUES_PER_PAGE).forEach( v ->
                matrix.td(VALUE_CSS_CLASS, v, nullValue()) );
            if (values.size()>MAX_VALUES_PER_PAGE) 
                matrix.td("font-size:x-small;","&hellip;");
            matrix._tr();
        });
        matrix._table();
        
        /* rules */
        final TableBuilder rules = new TableBuilder()
                .setAutoLayout()
                .addColumn(1);
        final String xpath = ev.getExtractionRule().getXPath();
        rules.table().tr().th(RULE_AS_HEADER_CSS_CLASS,xpath)._tr();
        ev.getGeneratingRules().stream() // do not repeat ev's rule twice
        .limit(MAX_RULES_PER_VECTOR).filter( r -> !r.getXPath().equals(xpath) )
        .forEach( r -> rules.tr().td(r.getXPath())._tr() );
        if (ev.getGeneratingRules().size()>MAX_RULES_PER_VECTOR)
            rules.tr().td("&hellip;")._tr();
        rules._table();

        return "<TABLE><TR><TD>"+rules+"</TD></TR>"+
                      "<TR><TD>"+matrix+"</TD></TR></TABLE>";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ExtractedVector<?>> getRenderedObjectClass() {
        return  (Class<? extends ExtractedVector<?>>) ExtractedVector.class.asSubclass(ExtractedVector.class);
    }

}
