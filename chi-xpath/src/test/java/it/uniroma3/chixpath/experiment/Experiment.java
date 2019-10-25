package it.uniroma3.chixpath.experiment;

import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;


import it.uniroma3.chixpath.PartitionerXFP;
import it.uniroma3.chixpath.model.PageClass;
import it.uniroma3.chixpath.model.Partition;

public class Experiment {
	public static void main(String args[]) throws XPathExpressionException{
		/* dataset - domain - website */
		//long startTime = System.currentTimeMillis();

		//Espressività del frammento XPath e "k" punteggi restituiti
		int range = 6;
		int nBest = 3;

		long startTime = System.currentTimeMillis();
		System.out.println("Starting simulation");
		//Salva i path per i file e l'ottimo
		String expDir = "./dataset/" + args[0] + "/" + args[1] + "/" + args[2] ;
		String fileDir = expDir.concat( "/_local.txt");
		String goldenDir = expDir.concat("/_golden.txt");
		List<String> pages = load(fileDir,expDir);

		//esegue l'algoritmo
		List<Partition> solution = PartitionerXFP.execution(pages.toArray(new String[pages.size()]),range, nBest);
		long startGolden= System.currentTimeMillis();
		FileWriter fw   = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter("./experiments/"+ goldenDir.split("/")[3]+".txt");
			bw = new BufferedWriter(fw);
			bw.write("Website: " + goldenDir.split("/")[3] + " Range: " + range + " Best: "+ nBest + "\n"   );


		} catch (IOException e) {
			System.out.println("IO error");
		}
		//Effettua il confronto con il golden
		for (Partition p : solution) {
			float precision = compareToGolden(goldenDir, p, bw);
			System.out.println("Partizione: "+p.getId() + " Fmedio: " +precision);
			
			
		}

		long stopGolden= System.currentTimeMillis();

		long endTime = System.currentTimeMillis();

		System.out.println("************SOLUTION************\n");
		System.out.println("Total pages: " + pages.size());
		System.out.println("Golden compare : " + (stopGolden-startGolden)/1000 + " seconds");
		System.out.println("TOTAL TIME : " + (endTime-startTime)/1000 + " seconds");
		System.out.println("********************************\n");
		if (bw !=null) {

			try {
				bw.write("EXECUTION TIME: " + ((endTime-startTime)/1000) + " seconds");
				bw.flush();
				bw.close();
				fw.close();
			} catch (IOException e) {
				System.out.println("IO error");
			}
		}
		Toolkit.getDefaultToolkit().beep();
		Toolkit.getDefaultToolkit().beep();
		Toolkit.getDefaultToolkit().beep();
		Toolkit.getDefaultToolkit().beep();
		Toolkit.getDefaultToolkit().beep();
		Toolkit.getDefaultToolkit().beep();



	}
	
	//Effettua il confornto tra una partizione in output ed una "golden" in termini di precision&recall restituitendo una f1score
	private static float compareToGolden(String goldenDir, Partition p, BufferedWriter bw) {

		Set<Set<String>> golden = loadGolden(goldenDir);
		Map<Set<String>,Set<String>> golden2IDs = new HashMap<>();
		Set<PageClass> alreadyToken = new HashSet<>();
		float totalP = 0;
		float totalR = 0;
		int j = 0;
		for(Set<String> l : golden) {
			PageClass current = null;
			float MaxF1 = -1;
			float MaxPrecision = 0;
			float MaxRecall = 0;
			int MaxSamePages = 0;
			for (PageClass pc : p.getPageClasses()) {
				if(!alreadyToken.contains(pc)) {	
					int samePages = 0;
					for(String id : pc.getPagesID()) {
						if (l.contains(id)){
							samePages++;
						}
					}
					float currentP = (float)samePages/pc.getPages().size();
					float currentR = (float)samePages/l.size();
					float currentF1;
					if(currentP + currentR == 0) {
						currentF1 = 0;
					} else {
						currentF1 = (float)2*currentP * currentR/(currentP + currentR);
					}
					if (currentF1 > MaxF1) {
						current = pc;
						MaxF1 = currentF1;
						MaxPrecision = currentP;
						MaxRecall = currentR;
						MaxSamePages = samePages;
					}
					else {
					}
				}

			}
			if(MaxF1 != -1) {
				totalR += MaxRecall;
				totalP += MaxPrecision;
				alreadyToken.add(current);
				golden2IDs.put(l, current.getPagesID());
				System.out.println("*****************************" + p.getId() + "**************************************************************");
				System.out.println(j + ") " + l + " -> " + current.getPagesID() );
				System.out.println("Precisione : " + MaxPrecision + " Pagine uguali: " + MaxSamePages + "Dimensione : " + current.getPages().size());
				System.out.println("Recall :" + MaxRecall + " Pagine uguali: " + MaxSamePages + "Dimensione : " + l.size());
				System.out.println("F1 :" + MaxF1 + "\n");

				System.out.println("*******************************************************************************************/n");
				
			} else {
				golden2IDs.put(l,Collections.emptySet());
			}
			j++;
		}


		int i = 0;
		for (Set<String> goldenS : golden) {
			System.out.println(i + ") " + goldenS + " -> " + golden2IDs.get(goldenS) + "\n");
			i++;
		}
		try {
			bw.write("****************************************\n");
			for (Set<String> g : golden) {
				bw.write(g + " -> ");
				if (!golden2IDs.containsKey(g)) {
					bw.write("[]\n");
				}else {
					bw.write(golden2IDs.get(g) + "\n");
				}
			}
		}
		catch (IOException e) {
			System.out.println("IO error");
		}
		float avgP = totalP/golden.size();
		float avgR = totalR/golden.size();
		float avgF = 2*avgP*avgR/(avgP+avgR);
		System.out.println("Average: Precision: " + avgP + "   Recall: "+ avgR);
		if(bw != null) {
			try {
				bw.write("Average: Precision: " + avgP  + "   Recall: "+ avgR + "\n" );
				bw.write("Average F1: " + avgF+ "\n" );
				bw.write("RANKING PARTIZIONE: " + p.getRank() + "\n");
				bw.write("****************************************\n");
			} catch (IOException e) {
				System.out.println("IO error");
			}

		}


		return avgF;

	}

	
	private static Set<Set<String>> loadGolden(String goldenDir) {
		Set<Set<String>> golden = new HashSet<>();
		Reader fr   = null;
		try {
			fr = new FileReader(goldenDir);
		} catch (FileNotFoundException e) {
			System.out.println("IO error");
			return null;
		}
		Scanner s = new Scanner(fr);
		Set<String> pClass = null;
		while (s.hasNextLine()) {
			final String line = s.nextLine();
			if(line.equals("-")) {
				if(pClass!=null) {
					golden.add(pClass);
				}
				pClass = new HashSet<>();
			}else {
				if(!line.isEmpty())
					pClass.add(line);
			}
		}
		if(pClass!=null) {
			golden.add(pClass);
		}
		pClass = new HashSet<>();
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
