package xfp.hlog;


import it.uniroma3.hlog.render.ObjectRenderer;
import xfp.fixpoint.FixedPoint;

import static it.uniroma3.hlog.HypertextualUtils.styled;

public class FixedPointRenderer<T> implements ObjectRenderer<FixedPoint<T>> {

    @Override
    public String toHTMLstring(FixedPoint<T> f) {
        return  styled(
                  "font-size:medium;"
                + "font-style:oblique;"
                + "background-color: #cccccc;",
                "Fixed Point")+
                new ExtractedVectorRenderer().toHTMLstring(f.getExtracted())+
                styled("display: inline-block; height: 6px;","&nbsp;");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends FixedPoint<T>> getRenderedObjectClass() {
        return (Class<? extends FixedPoint<T>>) FixedPoint.class.asSubclass(FixedPoint.class);
    }

}
