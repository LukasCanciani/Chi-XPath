package xfp.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import xfp.fixpoint.FixedPoint;
import xfp.model.ExtractedVector;
import xfp.model.Value;
import xfp.model.Webpage;

public class IOUtil<T> {

	public static <T> Map<String, List<String>> fixedpoints2Map(Set<Webpage> pages, Set<FixedPoint<T>> fixedpoints) {
        Map<String, List<String>> twfv = new HashMap<>();
        pages.forEach(p -> twfv.put(p.getName(), new ArrayList<>()));
        twfv.put(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER, new ArrayList<>());
        twfv.get(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER).add("pages");

        boolean first = true;

        for (FixedPoint<T> fp : fixedpoints)	{
            ExtractedVector<T> ev = fp.getExtracted();
            final List<Webpage> ordered = new ArrayList<>(ev.getPages());
            Collections.sort(ordered);

            if (first) {
                for (Webpage p : ordered)	{	        	
                    if (twfv.containsKey(p.getName())) {
                        twfv.get(p.getName()).add(p.getName());
                    }
                }
                first = false;
            }

            // max values from a page
            int max_values = -1;
            for (Webpage p : ordered)	{
            	if (ev.getValues(p).size()>max_values) max_values = ev.getValues(p).size();
            }

            //header
            List<String> header = twfv.get(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER);
            header.add(FixedPointToSchemaElement.useContainsOrClassOrSimpleXPath(fp));
            //header = FixedPointToSchemaElement.cleanHeader(header);
            for (int i=1; i<max_values; i++) header.add(new String());
            for (Webpage p : ordered)	{	        	
                if (twfv.containsKey(p.getName())) {
                    List<String> p_values = twfv.get(p.getName());

                    List<Value<T>> values = ev.getValues(p);
                    for (Value<T> val : values) p_values.add(val.toString());
                    for (int i=values.size(); i<max_values; i++)	{
                    	p_values.add(new String());
                    }
                }
            }

        }
        return twfv;
    }
	
	public static <T> Map<String, List<String>> fixedpoints2MapSingle(Set<Webpage> pages, Set<FixedPoint<T>> fixedpoints) {
        Map<String, List<String>> twfv = new HashMap<>();
        pages.forEach(p -> twfv.put(p.getName(), new ArrayList<>()));
        twfv.put(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER, new ArrayList<>());
        twfv.get(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER).add("pages");

        boolean first = true;
        
        for (FixedPoint<T> fp : fixedpoints)	{
            ExtractedVector<T> ev = fp.getExtracted();
            final List<Webpage> ordered = new ArrayList<>(ev.getPages());
            Collections.sort(ordered);

            if (first) {
                for (Webpage p : ordered)	{	        	
                    if (twfv.containsKey(p.getName())) {
                        twfv.get(p.getName()).add(p.getName());
                    }
                }
                first = false;
            }
            
            //header
            List<String> header = twfv.get(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER);
            try {
            	FixedPointToSchemaElement.useContainsOrClassOrSimpleXPath(fp);
            } catch (Exception e)	{
            	System.out.println(fp);
            	System.out.println(fp.getExtractionRule().getXPath());
            	e.printStackTrace();
            }
            header.add(FixedPointToSchemaElement.useContainsOrClassOrSimpleXPath(fp));
            //header = FixedPointToSchemaElement.cleanHeader(header);
            
            for (Webpage p : ordered)	{	        	
                if (twfv.containsKey(p.getName())) {
                    List<String> p_values = twfv.get(p.getName());
                    
                    p_values.add(combine(ev.getValues(p)));
                }
            }

        }
        return twfv;
    }

	private static <T> String combine(List<Value<T>> values) {
		StringJoiner sj = new StringJoiner(",","\"","\"");
		for (Value<T> v : values)	{
			sj.add(v.toString());
		}
		return sj.toString();
	}
}
