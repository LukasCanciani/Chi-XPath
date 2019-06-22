package xfp;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.*;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.fixpoint.FixedPoint;

public class Main {

	static {
		/* otherwise one of the old LFEQ comparators that implements
		 * a partial-but-not-total ordering would rise exceptions 
		 * when executed by new java versions. */
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		// set java.util.logging.manager to inject hypertextual logger
		System.setProperty("java.util.logging.manager", "it.uniroma3.hlog.HypertextualLogManager");     
	}

	static final protected HypertextualLogger log = getLogger(); /* this *after* previous static block */

	protected String datasetName;
	protected String domainName;
	protected List<String> websites = new ArrayList<>();

	protected void parseArgs(String args[])	throws Exception {
		final Options cmdOptions = new Options();
		/* available command-line parameters */
		cmdOptions.addOption("s", "dataset",  true,  "The name of the dataset.");
		cmdOptions.addOption("d", "domain",   true,  "The name of the domain.");
		cmdOptions.addOption("w", "websites", true,  "The list of websites to be executed, \"site1,site2\".");
		cmdOptions.addOption("h", "help",     false, "Print help message.");

		final CommandLineParser cmdParser = new DefaultParser();
		final CommandLine cmd = cmdParser.parse(cmdOptions, args);

		if (cmd.hasOption("h") || (!cmd.hasOption("s") && !cmd.hasOption("d")))	{
			final HelpFormatter help = new HelpFormatter();
			help.printHelp("\n", cmdOptions);
			System.exit(0);
		}
		if (cmd.hasOption("s") && (cmd.hasOption("d")))	{
			datasetName = cmd.getOptionValue("s").trim();
			domainName = cmd.getOptionValue("d").trim();
		}
		if (cmd.hasOption("w"))	{
			websites = Arrays.asList(cmd.getOptionValue("w").split(","));
		}

	}

	protected void run(ExperimentRunner runner) {
		System.out.println("Starting experiments"
				+ " on "
				+ " dataset "  + datasetName + ","
				+ " domain "   + domainName + " and"
				+ " websites " + websites);
		runner.run(this.datasetName, this.domainName, this.websites);
	}

	static public void main(String[] args) throws Exception {

		final Main main = new Main();
		final ExperimentRunner runner = new ExperimentRunner();
		main.parseArgs(args);
		main.run(runner);
	}

	public static Map<Set<String>, int[]> NavMain(String[] arguments, Map<String, String> id2name) throws Exception {
		
		final Main main = new Main();
		Map<Set<String>, int[]> FixedPoints = new HashMap<>();
		final ExperimentRunner runner = new ExperimentRunner();
		System.out.println("DOPO");
		main.parseArgs(arguments);
		FixedPoints = main.runNav(runner,id2name);

		return FixedPoints;
	}

	private Map<Set<String>, int[]> runNav(ExperimentRunner runner, Map<String, String> id2name) {
		/*System.out.println("Starting experiments"
				+ " on "
				+ " dataset "  + datasetName + ","
				+ " domain "   + domainName + " and"
				+ " websites " + websites);*/
		return runner.runNav(this.datasetName, this.domainName, this.websites, id2name);
		
	}

	public static Set<FixedPoint<String>> DataMain(String[] arguments, Map<String, String> id2name) throws Exception {
		final Main main = new Main();
		Set<FixedPoint<String>> FixedPoints;
		final ExperimentRunner runner = new ExperimentRunner();
		main.parseArgs(arguments);
		FixedPoints = main.runData(runner,id2name);

		return FixedPoints;
	}

	private Set<FixedPoint<String>> runData(ExperimentRunner runner, Map<String, String> id2name) {
		return runner.runData(this.datasetName, this.domainName, this.websites, id2name);
	}

}
