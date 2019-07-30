package xfp.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//import it.uniroma3.hlog.HypertextualLogger;
import xfp.fixpoint.FixedPoint;
import xfp.fixpoint.PageClass;
import xfp.model.Webpage;

public class PageClassFixedpointToFileOutputHandler<T> implements OutputHandler<T> {

 //   static final private HypertextualLogger log = HypertextualLogger.getLogger();

    static public String PC_FILE_SUFFIX = "_pc";

    static public String CONSTANT_FP_FILE_SUFFIX = "_cfp_";

    static public String VARIANT_FP_FILE_SUFFIX = "_vfp_";

    static public String CSV_SEPARATOR=",";

    static public String CSV_VALUE_QUOTE="\"";

    static String FIXEDPOINT_HEADER = "header";

    static private String BASE_DIR = "output";

    static private String DIR_NAME = "tmp";

    static private PageClassFixedpointToFileOutputHandler<URI> nfpToFile = null;

    static private PageClassFixedpointToFileOutputHandler<String> dfpToFile = null;

    static private Map<String,Set<String>> pc_ids_written = new HashMap<>();
    
    static public OutputHandler<URI> getNFPPageClassOutputHandler() {
        if (nfpToFile==null) 
            nfpToFile = new PageClassFixedpointToFileOutputHandler<URI>(OutputHandlerFactory.NFP);
        return nfpToFile;
    }

    static public OutputHandler<String> getDFPPageClassOutputHandler() {
        if (dfpToFile==null)
            dfpToFile = new PageClassFixedpointToFileOutputHandler<String>(OutputHandlerFactory.DFP);
        return dfpToFile;
    }

    static public void setDirName(String dirName)   {
        PageClassFixedpointToFileOutputHandler.DIR_NAME = dirName;
    }

    private File expDir = null;

    private String type;

    private PageClassFixedpointToFileOutputHandler(String type)	{	
        this.type = type;
        pc_ids_written.put(type, new HashSet<>());
    }
    
    private boolean createOutputDirectory()	{
    	if (this.expDir==null)	{
	    	this.expDir = new File(BASE_DIR,DIR_NAME);
		    if (!expDir.exists()) 
		    	return expDir.mkdir();
	        return true;
    	} else {
    		return true;
    	}
    }

	public void storePageclass(PageClass<T> pc) throws IOException {
		DIR_NAME = ((Webpage)pc.getPages().toArray()[0]).getWebsite().getName();
		this.createOutputDirectory();
        if (pc_ids_written.get(type).contains(pc.getId()))	{
 //           log.trace("Page class "+pc.getId()+" already stored ("+type+").");
            return;
        }
        final Set<Webpage> pages = pc.getPages();
        final Set<FixedPoint<T>> varFP = pc.getVariant();
        final Set<FixedPoint<T>> conFP = pc.getConstant();

        writePageClass(pc.getId(), pages, varFP, conFP);

        if (varFP.size()>0)
            writeFixedPoints(pc.getId(), pages, varFP, true);

        if (conFP.size()>0)
            writeFixedPoints(pc.getId(), pages, conFP, false);

  //      log.trace("Page class "+pc.getId()+" stored (pages "+pages.size()+") for type ("+type+"): "+varFP.size()+", "+conFP.size());
        pc_ids_written.get(type).add(pc.getId());
    }

    private void writeFixedPoints(String pc_id, Set<Webpage> pages, Set<FixedPoint<T>> fixedpoints, boolean variant) throws IOException {

        //fixedpoints to array
        final Map<String, List<String>> values = IOUtil.fixedpoints2Map(pages, fixedpoints);
        File file = getFixedPointFile(pc_id, variant);
        final FileWriter fw = new FileWriter(file);
        fw.write(values.get(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER).stream().map(v -> CSV_VALUE_QUOTE+v+CSV_VALUE_QUOTE).collect(Collectors.joining(PageClassFixedpointToFileOutputHandler.CSV_SEPARATOR))+"\n");

        Set<String> keys = values.keySet();
        keys.remove(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER);
        for (String key : keys)	{
            fw.write(values.get(key).stream().map(v -> CSV_VALUE_QUOTE+v+CSV_VALUE_QUOTE).collect(Collectors.joining(PageClassFixedpointToFileOutputHandler.CSV_SEPARATOR))+"\n");
        }
        fw.close();
    }

    private File getFixedPointFile(String pc_id, boolean variant) {
        return new File(expDir, pc_id + ( variant ? VARIANT_FP_FILE_SUFFIX : CONSTANT_FP_FILE_SUFFIX ) + type + ".csv");
    }

    private void writePageClass(String pc_id, Set<Webpage> pages, Set<FixedPoint<T>> var_fp, Set<FixedPoint<T>> con_fp) throws IOException {
        final File file = new File(expDir, getPageclassFilename(pc_id));
        final PrintWriter fw = new PrintWriter(file);
        fw.println("pages: "+pc_id);
        for (Webpage page : pages)	{
            fw.println(page.getName());
        }
        fw.println("nr_vfp: "+var_fp.size());
        fw.println("nr_cfp: "+con_fp.size());
        fw.close();
    }

    private String getPageclassFilename(String pc_id) {
        return pc_id+PC_FILE_SUFFIX+".txt";
    }
}
