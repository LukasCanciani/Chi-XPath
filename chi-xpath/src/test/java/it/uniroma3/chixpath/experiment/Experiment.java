package it.uniroma3.chixpath.experiment;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.xpath.XPathExpressionException;

import it.uniroma3.chixpath.PartitionerXFP;
import it.uniroma3.chixpath.model.Partition;

public class Experiment {
	public static void main(String args[]) throws XPathExpressionException{
		/* dataset - domain - website */
		//long startTime = System.currentTimeMillis();
		System.out.println("Starting simulation");
		String expDir = "./dataset/" + args[0] + "/" + args[1] + "/" + args[2] ;
		String fileDir = expDir.concat( "/_local.txt");
		String goldenDir = expDir.concat("/_golden.txt");
		List<String> pages = load(fileDir,expDir);
		
		List<Partition> solution = PartitionerXFP.execution(pages.toArray(new String[pages.size()]));
		for (Partition p : solution) {
			float precision = compareToGolden(goldenDir, p);
			System.out.println("Partizione: "+p.getId() + " precisione: " +precision*100);
		}





	}

	private static float compareToGolden(String goldenDir, Partition p) {
		List<List<String>> golden = loadGolden(goldenDir);
		for(List<String> l : golden) {
			System.out.println("-");
			for (String s : l) {
				System.out.println(s);
			}
		}
		return 0;
	}

	private static List<List<String>> loadGolden(String goldenDir) {
		List<List<String>> golden = new ArrayList<>();
		Reader fr   = null;
		try {
			fr = new FileReader(goldenDir);
		} catch (FileNotFoundException e) {
			System.out.println("IO error");
			return null;
		}
		Scanner s = new Scanner(fr);
		List<String> pClass = null;
		while (s.hasNextLine()) {
			final String line = s.nextLine();
			if(line.equals("-")) {
				if(pClass!=null) {
					golden.add(pClass);
				}
				pClass = new ArrayList<>();
			}else {
				if(!line.isEmpty())
					pClass.add(line);
			}
		}
		s.close();
		return golden;
	}

	private static List<String> load(String fileDir, String expDir) {
		Reader fr   = null;
		try {
			fr = new FileReader(fileDir);
		} catch (FileNotFoundException e) {
			System.out.println("IO error");
			return null;
		}
		Scanner s = new Scanner(fr);
		List<String> pages = new ArrayList<>();
		while (s.hasNextLine()) {
			final String page = s.nextLine();
			if(!page.isEmpty())
				pages.add("file:" + expDir.concat("/"+page));
		}
		s.close();
		return pages;
	}
}
