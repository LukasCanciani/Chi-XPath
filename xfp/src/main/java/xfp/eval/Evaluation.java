package xfp.eval;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;

import com.opencsv.CSVReader;

//import it.uniroma3.hlog.HypertextualLogger;
import xfp.ExperimentRunner;
import xfp.fixpoint.FixedPoint;
import xfp.model.Experiment;
import xfp.model.ExtractedVector;
import xfp.model.Value;
import xfp.model.Webpage;
import xfp.model.Website;
import xfp.util.XFPConfig;

public class Evaluation {
	
//	static final private HypertextualLogger log = HypertextualLogger.getLogger();
	
	private Map<String, List<String[]>> golden_fixpoints;
	private Map<Set<String>,String> pcPagesToName = new HashMap<>();
	private File websitedir;
	private boolean isGoldenAvailable = false;
	
	static {
        /* otherwise one of the old LFEQ comparators that implements
         * a partial-but-not-total ordering would rise exceptions 
         * when executed by new java versions. */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        // set java.util.logging.manager to inject hypertextual logger
		System.setProperty("java.util.logging.manager", "it.uniroma3.hlog.HypertextualLogManager");     
	}
	
	public Evaluation(File websitedir, Website site) throws IOException	{ //Experiment.getWebsitedir
		this.websitedir = websitedir;
		this.golden_fixpoints = this.readGoldStandardFixedpoints(site);
		if (this.golden_fixpoints!=null) this.isGoldenAvailable = true;
	}
	
	public int evaluateFixedPoint(FixedPoint<String> fp, String pc_name)	{
		ExtractedVector<String> vector = fp.getExtracted();
		Set<Webpage> pages = vector.getPages();

		//maptopageclass
		List<String[]> golden_pc = golden_fixpoints.get(pc_name);
		if (golden_pc!=null)	{ // is there a page class
			// for each page - compare value (equals)
			//System.out.println(pages.size()+" "+vector.size());
			/*for (Webpage page : pages)	{
				String v = vector.getValues(page).isEmpty() ? "" : vector.getValues(page).get(0).getValue();
				System.out.println(v);
			}*/
			Set<String> values = pages.stream().map(p -> {
					if (vector.getValues(p).isEmpty()) return "";
					Value<String> v = vector.getValues(p).get(0);
					return v.isNull() ? "" : v.getValue();
				}
				).collect(Collectors.toSet()); 
			for (int i=1; i< golden_pc.get(0).length; i++)	{
				final int index = i;
				Set<String> g_values = golden_pc.stream().map(v -> v[index]).collect(Collectors.toSet());
				if (values.equals(g_values)) {
					return i;
				}
			}
		}
		return -1;
	}
	
	private String findPageClass(Set<Webpage> pages) {
		Set<String> names = pages.stream().map(p -> p.getName()).collect(Collectors.toSet());
		return pcPagesToName.get(names);
	}

	private Map<String, List<String[]>> readGoldStandardFixedpoints(Website site) throws IOException	{
		Map<String, List<String[]>> golden_fixpoints = new HashMap<>();
		
		Map<String, Set<Webpage>> tmp_golden = null;
		try {
			tmp_golden = site.getWebpages().stream().collect(Collectors.groupingBy(Webpage::getGoldenPageClass, Collectors.toSet()));
		} catch (Exception e)	{
//			log.error("Couldn't collect golden page class from webpages in site. Evaluation will not be executed.");
		}
		
		if (tmp_golden == null)	return null;
		
		for (String pc_name : tmp_golden.keySet())	{
			Set<String> pages = new HashSet<>();
			try {
				File file = getFixedPointGoldenFile(pc_name);
				CSVReader reader = new CSVReader(new FileReader(file), ',');
				
				Iterator<String[]> i = reader.iterator();
				@SuppressWarnings("unused")
				String[] headers = i.next();
				List<String[]> fixpoints_golden = new ArrayList<>();
				while (i.hasNext())	{
					String[] tmp = i.next();
					fixpoints_golden.add(tmp);
					pages.add(tmp[0]);
				}
				reader.close();
				
				golden_fixpoints.put(pc_name, fixpoints_golden);
				//printGoldenFixpoint(pc_name, fixpoints_golden);
				//System.out.println(pages+" "+pc_name);
				pcPagesToName.put(pages,pc_name);
			} catch(Exception e)	{
//				log.error("Couldn't find expected golden fixed point file for page class: "+pc_name+"("+websitedir+File.separatorChar+"gfp_"+pc_name+".csv)");
				return null;
			}
		}
		return golden_fixpoints;
	}
	
	@SuppressWarnings("unused")
	private void printGoldenFixpoint(String pc_name, List<String[]> values)	{
		System.out.println(pc_name+": ");//+values);
		for (String[] v : values)	{
			System.out.println(Arrays.toString(v));
		}
	}
	
	private File getFixedPointGoldenFile(String pc_name) {
		return new File(websitedir, "gfp_"+pc_name + ".csv");
    }
	
	public static void main(String[] args) throws ConfigurationException, IOException	{
		new ExperimentRunner();
		Experiment experiment = Experiment.makeExperiment("test", "cruise");
        XFPConfig.getInstance().setCurrentExperiment(experiment);
        experiment.load(); // N.B. this loads all pages
        Website ws = experiment.getWebsites().get(0);
		Evaluation eval = new Evaluation(experiment.getWebsiteFolder(ws.getName()), ws);
		eval.evaluateFixedPoint(null,"");//FIXME
	}

	public void evaluateFixedPoints(Set<FixedPoint<String>> variant) {
		if (variant!=null && !variant.isEmpty())	{
			@SuppressWarnings("unchecked")
			String pc_name = findPageClass(((FixedPoint<String>)variant.toArray()[0]).getExtracted().getPages());
			if ((pc_name!=null)&&(pc_name.equals("cabin"))){
				System.out.println();
			}
			float TP = 0;
			float P = 0;
			@SuppressWarnings("unchecked")
			String page_names = ((FixedPoint<String>)variant.toArray()[0]).getExtracted().getPages().stream().map(p -> p.getName()).reduce("",(l,r) -> l+" "+r);
			if (golden_fixpoints.get(pc_name)!=null)	{
				P = golden_fixpoints.get(pc_name).get(0).length-1;
				for (FixedPoint<String> fp : variant) {
					int tmp = evaluateFixedPoint(fp, pc_name);
					if (tmp > -1) TP++;
				}
			}
			float FP = variant.size()-TP;
			float FN = P-TP;
			float precision = (TP/(TP+FP));
			float recall = (TP/(TP+FN));
			float f1 = 2*((precision*recall)/(precision+recall));
//			log.info("Page class ("+pc_name+"): P/R/F1: "+precision+" "+recall+" "+f1);
			System.out.println("Page class ("+pc_name+") with "+variant.size()+" fixpoints: P/R/F1: "+precision+" "+recall+" "+f1+" ("+page_names+")");
		} /*else {
			log.info("Page class (empty): P/R/F1: 0 0 0");
			System.out.println("Page class (empty): P/R/F1: 0 0 0");
		}*/
	}

	/**check if we golden fixed point files have been read successfully and evaluation can be executed*/
	public boolean isGoldenAvailable() {
		return isGoldenAvailable;
	}
}
