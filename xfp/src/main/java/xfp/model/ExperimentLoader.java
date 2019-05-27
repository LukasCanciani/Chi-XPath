package xfp.model;

import static it.uniroma3.hlog.HypertextualLogger.getLogger;
import static it.uniroma3.hlog.HypertextualUtils.linkTo;
import static xfp.hlog.XFPStyles.header;
import static xfp.util.Constants.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import it.uniroma3.hlog.HypertextualLogger;
import it.uniroma3.hlog.Logpage;
import xfp.XFPException;
import xfp.util.XFPConfig;

public class ExperimentLoader {

	static final private HypertextualLogger log = getLogger();

	final private Experiment experiment;

	// max number of pages to load from each source website
	private int maxNumberOfPages = XFPConfig.getInteger(MAX_PAGES_PER_SOURCE);

	final private Pattern idRegexp;

	public ExperimentLoader(Experiment experiment) {
		this.experiment = experiment;
		final String regexp = XFPConfig.getString(ID_FILTER);
		if (regexp!=null && !regexp.trim().isEmpty()) {
			this.idRegexp = Pattern.compile(regexp);
			log.trace("selecting only pages whose id matches with: "+this.idRegexp);
		} else this.idRegexp=null;
	}

	public Website loadWebsite(String sitename) {

		// e.g., dataset/swde/nbaplayer/nbaplayer-espn/
		final File siteFolder = this.experiment.getWebsiteFolder(sitename);

		// e.g., dataset/swde/nbaplayer/nbaplayer-espn/_id2name.txt
		final File pagesIndex = this.experiment.getWebsitePageIndex(sitename);

		try (final Reader reader = new FileReader(pagesIndex)) {
			final Website website = new Website(sitename);
			log.newPage();
			log.trace("loading website " + sitename);
			int filtered = this.loadPages(reader, website, siteFolder);
			log.endPage("loading website " + sitename 
					+ " ("+website.getWebpages().size()+" pages loaded,"
					+ filtered+" filtered out)");
			return website;
		}
		catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public Website loadWebsiteChi(String sitename, Map<String,String> id2names) {

		// e.g., dataset/swde/nbaplayer/nbaplayer-espn/
		final File siteFolder = this.experiment.getWebsiteFolder(sitename);

		// e.g., dataset/swde/nbaplayer/nbaplayer-espn/_id2name.txt
		final Website website = new Website(sitename);
		log.newPage();
		log.trace("loading website " + sitename);
		int filtered = this.loadPagesChi(website, siteFolder, id2names);
		log.endPage("loading website " + sitename 
				+ " ("+website.getWebpages().size()+" pages loaded,"
				+ filtered+" filtered out)");
		return website;
	}

	private int loadPages(Reader id2UrlReader, Website website, File siteFolder) {
		log.trace("loading from folder "+linkTo(siteFolder).withAnchor(website.getName()));
		log.newTable();
		log.trace(header("Id"),header("file"),header("loading"));
		int counter = 0;
		int filtered = 0;
		final AtomicInteger loading = new AtomicInteger(0);
		try (Scanner scanner = new Scanner(id2UrlReader)) {
			while (scanner.hasNextLine()) {
				/* a line per page */
				/* id-page page.html [pageclass] ... ignored every else */
				final String line = scanner.nextLine();
				final String[] split = line.split("\t");
				if (split.length>=2) {
					final String id       = split[0].trim();
					final String filename = split[1].trim();
					final File file = new File(siteFolder, filename);
					if (idMatchesFilterPattern(id)) {
						final Webpage page = new Webpage(file.toURI());
						log.newPage();
						loading.incrementAndGet();
						ForkJoinPool.commonPool().submit( () -> { 
							page.loadDocument();
							loading.decrementAndGet();
						} );
						final Logpage logpage = log.endPage();
						if (website.addPage(page)) {
							/* is there a golden pageclass specified for this page? */
							if (split.length>2) {
								final String pageclass = split[2].trim();
								page.setGoldenPageClass(pageclass);
							}

							if (isAccessPageId(id)) {
								website.addAccessPage(page);
								logLoadedPage(file, page, "access page", logpage);
							} else {
								logLoadedPage(file, page, "non-access page", logpage);
							}
						} else  logLoadedPage(file, page, "duplicate! page", logpage);
					} else {
						log.trace(id,"discarded");
						filtered++;						
					}
				} else {
					log.warn("@skipping malformed line " + line);
				}
				if (++counter == maxNumberOfPages) break;
			}
		}		
		log.trace(counter  + " pages loaded");
		log.trace(filtered + " pages discarded");
		log.endTable();
		/* wait that all pages loaded */
		int sec = 0;
		while (loading.get()>0) {            
			try {
				Thread.sleep(1000);
				log.trace("Waiting page loading to complete ("+sec+" secs)");
				sec++;
			} catch (InterruptedException e) {
				throw new XFPException(e);
			}
		}
		log.trace("Page loading now completed.");
		return filtered;
	}

	private int loadPagesChi(Website website, File siteFolder, Map<String,String> id2names) {
		log.trace("loading from folder "+linkTo(siteFolder).withAnchor(website.getName()));
		log.newTable();
		log.trace(header("Id"),header("file"),header("loading"));
		int counter = 0;
		int filtered = 0;
		final AtomicInteger loading = new AtomicInteger(0);
		/* a line per page */
		/* id-page page.html [pageclass] ... ignored every else */
		for(String id : id2names.keySet()) {
			final String filename = id2names.get(id);
			final File file = new File(siteFolder, filename);
			if (idMatchesFilterPattern(id)) {
				final Webpage page = new Webpage(file.toURI());
				log.newPage();
				loading.incrementAndGet();
				ForkJoinPool.commonPool().submit( () -> { 
					page.loadDocument();
					loading.decrementAndGet();
				} );
				final Logpage logpage = log.endPage();
				if (website.addPage(page)) {

					if (isAccessPageId(id)) {
						website.addAccessPage(page);
						logLoadedPage(file, page, "access page", logpage);
					} else {
						logLoadedPage(file, page, "non-access page", logpage);
					}
				} else  logLoadedPage(file, page, "duplicate! page", logpage);
			} else {
				log.trace(id,"discarded");
				filtered++;						
			}
			if (++counter == maxNumberOfPages) break;
		}

		log.trace(counter  + " pages loaded");
		log.trace(filtered + " pages discarded");
		log.endTable();
		/* wait that all pages loaded */
		int sec = 0;
		while (loading.get()>0) {            
			try {
				Thread.sleep(1000);
				log.trace("Waiting page loading to complete ("+sec+" secs)");
				sec++;
			} catch (InterruptedException e) {
				throw new XFPException(e);
			}
		}
		log.trace("Page loading now completed.");
		return filtered;
	}

	private void logLoadedPage(final File file, final Webpage page, String pageType, Logpage logpage) {
		log.trace(linkTo(file).withAnchor(page.getName()), pageType, linkTo(logpage).withAnchor("whitespaces"));
	}

	private boolean idMatchesFilterPattern(String id) {
		return ( idRegexp==null || this.idRegexp.matcher(id).matches() );
	}

	private boolean isAccessPageId(String id) {
		return ( id.startsWith(XFPConfig.getString(ID_AP_SELECTOR)) );
	}

}
