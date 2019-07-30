package it.uniroma3.chixpath;

import static it.uniroma3.chixpath.ChiFinder.*;
import static it.uniroma3.fragment.step.CaseHandler.*;
import static it.uniroma3.fragment.test.Fixtures.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import it.uniroma3.chixpath.ChiFinder;
import it.uniroma3.chixpath.fragment.ChiFragmentSpecification;
import it.uniroma3.chixpath.model.ChiProblem;
import it.uniroma3.chixpath.model.ChiSolution;
import it.uniroma3.chixpath.model.Page;
import it.uniroma3.chixpath.model.TemplateSample;

public class ChiFinderMain {

    static public void main(String args[]) {
        /* e.g., an example of a input args to this command line program:
           file:./src/test/resources/basic/section.html
           file:./src/test/resources/basic/article1.html
           file:./src/test/resources/basic/article2.html
           file:./src/test/resources/basic/article3.html 
           1 
           3
         */
        if (args.length<2) {
            System.err.println("Usage: java "+ChiFinderMain.class.getName()+" <url_1>...<url_n> <size_1>...<size_m> ");
            System.err.println("...where size_1+...+size_m<=n ");
            System.err.println("Apply Characteristic XPath Finder to input urls grouped into m templates;");
            System.err.println("the remainder of the provided urls, if any, are used as 'other' pages.");
            System.exit(-1);
        }
        else System.out.println("Received " + args.length + " args: "+Arrays.toString(args));

        System.setProperty("http.agent", "Chrome"); // some sites want you to cheat

        final LinkedList<String> pageUrls = new LinkedList<>();
        final LinkedList<Integer> templateSizes = new LinkedList<>();

        /* parse and partition the cmd lines args into URL and numbers (sizes) */
        for(int index=0; index<args.length; index++) {
            if (isAnumber(args[index])) 
                templateSizes.add(Integer.parseInt(args[index]));
            else if (isAnUrl(args[index]))
                pageUrls.add(args[index]);
            else {
                System.err.println("Cannot parse arg: "+args[index]);
                System.exit(-1);
            }
        }

        System.out.println(pageUrls.size()+" urls to be grouped into " + ( templateSizes.size() ) + " templates");

        System.out.println();

        assertTrue("No input page to process!", !pageUrls.isEmpty());

        final String siteUrl = URI.create(pageUrls.getFirst()).getHost(); // use host as siteUrl                

        /* load the template samples */
        final List<TemplateSample> samples = new LinkedList<>();

        int templateCounter = 0;
        while (!templateSizes.isEmpty()) {
            /* consume listed urls into one template sample */
            final int size = templateSizes.removeFirst();
            System.out.println("Loading " + size + " pages for template " + templateCounter);
            final TemplateSample sample = consumeIntoTemplateSample(pageUrls, size);
            samples.add(sample);
            templateCounter++;
        }

        if (!pageUrls.isEmpty()) {
            System.out.println(pageUrls.size()+" page(s) left as 'other' pages");
        }
        final TemplateSample other = consumeIntoTemplateSample(pageUrls, pageUrls.size());

        assertTrue("No pages should be left out the templates!?", pageUrls.isEmpty());

        /* Solve the Characteristic XPath Problem and print its solution */        
        final ChiProblem problem = ChiProblem.builder()
                .templateSamples(samples)
                .otherPages(other.getPages())
                .siteUrl(siteUrl)
                .build();
        System.out.println("--- Input  ---");
        System.out.println(problem);
        System.out.println();
        System.out.println("--- Output ---");
        final ChiFinder finder = new ChiFinder(new ChiFragmentSpecification(LEAVE_CASE_AS_IT_IS));
        final ChiSolution solution = finder.find(problem);
        System.out.println(solution);
        System.out.println();

        doubleCheckChiSolution(solution);
        
    }

    static private TemplateSample consumeIntoTemplateSample(LinkedList<String> pageUrls, int templateSize) {
        if (pageUrls.size()<templateSize)
            throw new IllegalArgumentException(templateSize+" pages are NOT available to populating the sample");

        final TemplateSample sample = new TemplateSample();
        final List<Page> pages = new LinkedList<>();
        System.out.println(String.join("", Collections.nCopies(templateSize, "-")));

        for(int i=0; i<templateSize; i++) {
            final String url = pageUrls.removeFirst();
            final String content = loadPageContent(url);
            final Page page = createPage(content, url);
            //page.setUrl(url);
            pages.add(page);
            System.out.print(".");
        }
        System.out.println();
        sample.setPages(pages);
        return sample;
    }

    static private boolean isAnumber(String s) {
        try {
            return ( Integer.parseInt(s)!=0 );
        } catch (RuntimeException e) {
            return false;
        }
    }

    static private boolean isAnUrl(String s) {
        try {
            new URL(s);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }

    static private String loadPageContent(String anURL) {
        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out);
        try {
            final URLConnection conn = new URL(anURL).openConnection();

            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
                    );

            String line;
            while ( (line=reader.readLine())!=null) {
                writer.println(line);
            }

            writer.close();
            out.close();
        } catch (Exception e) { 
            throw new RuntimeException(e);
        }

        return out.toString();
    }

    public static void doubleCheckChiSolution(final ChiSolution solution) {
        System.out.println("Double checking Characteristic XPaths");
        final ChiProblem problem = solution.getCharacteristicProblem();
        for(TemplateSample ts1 : problem.getTemplateSamples()) {
            final String chiXPath = solution.getCharacteristicXPathForTemplate(ts1.getId());
            if (chiXPath==null) {
                System.out.println("Template "+ts1.getId()+" does not have a Characteristic XPath");
                continue;
            } else {
                System.out.print("Template "+ts1.getId()+" has a Characteristic XPath: "+chiXPath);
            }

            for(TemplateSample ts2 : problem.getTemplateSamples()) {
                for(Page page : ts2.getPages()) {
                    if (ts1.equals(ts2)) {
                        assertTrue("\nA Characteristic XPath should match with its own sample pages\n"+chiXPath+"\n"+page+"\n"+ts1, 
                                isMatchingXPathExpression(chiXPath, page));
                    } else {
                        assertFalse("\nA Characteristic XPath should NOT match with other templates' pages\n"+chiXPath+"\n"+page+"\n"+ts2,
                                isMatchingXPathExpression(chiXPath, page));
                    }
                }
            }
            System.out.println("...OK!");
        }
        if (solution.hasBeenFound()) {
            /* print only if we has chi-XPath for every template */
            System.out.println("Characteristic XPath solution found!");
        }
        System.out.println("Characteristic XPaths validated!");
    }
    
}
