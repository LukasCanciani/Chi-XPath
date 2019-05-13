package xfp.io;

import java.net.URI;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.util.Constants;
import xfp.util.XFPConfig;

public class OutputHandlerFactory {
	@SuppressWarnings("unused")
	static final private HypertextualLogger log = HypertextualLogger.getLogger();
	
	public static String FILE_OUTPUT = "FILE";
	public static String DB_OUTPUT = "DB";
	public static String DFP = "dfp";
	public static String NFP = "nfp";
	
	public static OutputHandler<String> getDFPOutputHandler()	{
		String output = XFPConfig.getString(Constants.OUTPUT_HANDLER_TYPE); //read from config 
		
		if (output.equals(FILE_OUTPUT))	{
			return PageClassFixedpointToFileOutputHandler.getDFPPageClassOutputHandler();
		}
		else if (output.equals(DB_OUTPUT))	{
			return PageClassToDatabaseOutputHandler.getDFPPageClassOutputHandler();
		} 
		return null;
	}
	
	public static OutputHandler<URI> getNFPOutputHandler()	{
		String output = XFPConfig.getString(Constants.OUTPUT_HANDLER_TYPE); //read from config
		if (output.equals(FILE_OUTPUT))	{
			return PageClassFixedpointToFileOutputHandler.getNFPPageClassOutputHandler();
		} else if (output.equals(DB_OUTPUT))	{
			return PageClassToDatabaseOutputHandler.getNFPPageClassOutputHandler();
		} 
		return null;
	}
}
