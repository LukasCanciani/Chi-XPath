package xfp.extraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import it.uniroma3.hlog.HypertextualLogger;
import xfp.fixpoint.RuleFactory;
import xfp.model.ExtractedVector;
import xfp.model.Webpage;
import xfp.model.Website;

/**
 * Extract {@link ExtractedVector}s by parallelizing the
 * application of a set of {@link ExtractionRule}s over a
 * collection of {@link Webpage}s.
 */
public class ParallelExtractor<T> {

	static final private int SEQUENTIAL_THRESHOLD = 125;

	static final private HypertextualLogger log = HypertextualLogger.getLogger();

	static final private int NCPU = Runtime.getRuntime().availableProcessors();

	// switch to this to deal with Eclipse and jwpd bugs when 
	// using conditional breakpoint during concurrent executions

	final private CompletionService<List<ExtractedVector<T>>> ecs = 
			new ExecutorCompletionService<>(ForkJoinPool.commonPool());

	final private RuleFactory<T> ruleFactory;

	private Website website;

	private Collection<Webpage> webpages;

	public ParallelExtractor(RuleFactory<T> vf) {
		this.ruleFactory = vf;
	}

	public List<ExtractedVector<T>> extract(Collection<Webpage> webpages, Collection<String> rules) {
		try {
			log.newPage("Extraction over "+webpages.size()+" pages");
			this.website = webpages.iterator().next().getWebsite();
			this.webpages = webpages;
			List<ExtractedVector<T>> extracted = null;
			// do no waste time parallelizing small tasks
			/* sequential extraction */
			if (webpages.size() * rules.size() < SEQUENTIAL_THRESHOLD * NCPU) 
				extracted = new ApplyRulesTask(rules).call();
			else {
				/* parallel extraction */
				log.trace("Available processors: " + NCPU);
				extracted = new ArrayList<>();

				/* slice the xpaths in NCPU chunks */
				final int total = rules.size();
				log.trace("applying " + total+" extraction xpaths over "+webpages.size()+" pages");

				final Iterator<String> it = rules.iterator();
				int taskCounter=0;
				while (it.hasNext()) {
					ecs.submit(makeExtractionTask(it, total));
					taskCounter++;
				}

				int counter = 0;
				// n.b. initial \n should flush log msgs..
				log.trace("\nStarting parallel extraction ...");
				while (taskCounter-- > 0) {
					final Future<List<ExtractedVector<T>>> f = ecs.take();
					extracted.addAll(f.get());
					counter += f.get().size();
					log.trace("\napplied extraction xpaths (" + counter + "/" + total + ")");
				}
				log.trace("\n...parallel extraction finished.");
			}
			log.trace("Extracted values:");
			log.trace(extracted);
			log.endPage();
			return extracted;
		}
		catch (InterruptedException | ExecutionException e) {
			log.error("extraction failed");
			e.printStackTrace();
			log.trace(e);
			throw new IllegalStateException(e);
		}
	}

	private Callable<List<ExtractedVector<T>>> makeExtractionTask(Iterator<String> it, int totalSize) {
		final int chunkSize = totalSize / NCPU;
		final List<String> chunk = new ArrayList<String>(chunkSize+1);
		int count = 0;
		while (it.hasNext() && count++ < chunkSize) {
			chunk.add(it.next());
		}
		if (totalSize % NCPU > 0 && it.hasNext()) chunk.add(it.next()); // one extra to consume odd xpaths

		return new ApplyRulesTask(chunk);
	}

	public class ApplyRulesTask implements Callable<List<ExtractedVector<T>>> {


		final private Collection<String> xpaths;

		// a task processes incrementally the n pages starting from this offset to
		// reduce the probability that it will meet other threads on the same doc
		public ApplyRulesTask(Collection<String> rules) {
			this.xpaths  = rules;
		}

		@Override
		public List<ExtractedVector<T>> call() {
			final List<ExtractedVector<T>> result = new ArrayList<>(xpaths.size());

			for (String xpath : this.xpaths) {
				try {
					final ExtractionRule<T> rule = ruleFactory.create(xpath);
					rule.setWebsite(website);
					final ExtractedVector<T> vector = new ExtractedVector<>(rule);
					apply(rule, vector);
					result.add(vector);
				} catch (Exception e) {
					this.xpaths.remove(xpath);
				}
			}
			return result;
		}

		final private void apply(ExtractionRule<T> rule, ExtractedVector<T> vector) {
			rule.applyTo(webpages, vector);
		}

	}

}