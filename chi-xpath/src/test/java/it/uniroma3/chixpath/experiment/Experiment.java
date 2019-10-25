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

		int range = 6;
		int nBest = 3;

		long startTime = System.currentTimeMillis();
		System.out.println("Starting simulation");
		String expDir = "./dataset/" + args[0] + "/" + args[1] + "/" + args[2] ;
		String fileDir = expDir.concat( "/_local.txt");
		String goldenDir = expDir.concat("/_golden.txt");
		List<String> pages = load(fileDir,expDir);

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

	/*private static float OLDcompareToGolden(String goldenDir, Partition p, BufferedWriter bw) {

		Set<Set<String>> golden = loadGolden(goldenDir);
		Map<Set<String>,Set<String>> golden2Classes = mapGolden(golden,p.getPageClasses(),bw);
		float totalF = 0;
		float totalP = 0;
		float totalR = 0;
		Map<Set<String>,float[]> pAndR = new HashMap<>();
		int i = 0;
		for (Set<String> group : golden) {

			float[] pRF = new float[3];
			int samePages = 0;
			//Precision
			System.out.println("\n**********************"+i+"**********************\n");
			if(golden2Classes.containsKey(group)) {
				for(String id : group) {
					if (golden2Classes.get(group).contains(id)) {
						samePages ++;
					}
				}


				pRF[0] = (float) samePages / golden2Classes.get(group).size();
				System.out.println("Precisione : Pagine uguali: " + samePages + "Dimensione : " + golden2Classes.get(group).size());

				pRF[1] =(float) samePages / group.size();

				System.out.println("Recall : Pagine uguali: " + samePages + "Dimensione : " + group.size());
			}
			else {
				pRF[0] = 0;
				pRF[1] = 0;
			}
			if(pRF[0] != 0 || pRF[1] != 0) {
				pRF[2] = 2*pRF[0] * pRF[1]/(pRF[0] + pRF[1]);
			}else {
				pRF[2] = 0;
			}
			totalF = totalF + pRF[2];
			totalP = totalP + pRF[0];
			totalR = totalR + pRF[1];
			pAndR.put(group, pRF);
			System.out.println( "TOTAL:  Precision: " + pRF[0] + "  Recal: " + pRF[1] + "  F1: " + pRF[2]);
			i++;
		}
		System.out.println("Average: Precision: " + totalP/golden.size() + "   Recall: "+ totalR/golden.size());
		if(bw != null) {
			try {
				bw.write("Average: Precision: " + totalP/golden.size() + "   Recall: "+ totalR/golden.size()+ "\n" );
				bw.write("Average F1: " + totalF / golden.size()+ "\n" );
			} catch (IOException e) {
				System.out.println("IO error");
			}

		}


		return totalF / golden.size();

	}*/


	/*private static Map<Set<String>,Set<String>> mapGolden(Set<Set<String>> golden , Set<PageClass> pageClasses, BufferedWriter bw){
		Map<Set<String>,Set<String>> golden2IDs = new HashMap<>();
		Map<Set<String>,Set<PageClass>> golden2Classes = new HashMap<>();
		for(Set<String> l : golden) {
			Set<PageClass> toRemove = new HashSet<>();
			for (PageClass pc : pageClasses) {
				if (belongsTo(l,pc)) {
					toRemove.add(pc);
					if(!golden2IDs.containsKey(l)) {
						Set<String> ids = new HashSet<>();
						Set<PageClass> classes = new HashSet<>();
						classes.add(pc);
						for(Page page : pc.getPages()) {
							ids.add( page.getUrl().split("/")[page.getUrl().split("/").length-1].split(".html")[0]);
						}
						golden2Classes.put(l,classes);
						golden2IDs.put(l, ids);
					}else {
						Set<String> ids = golden2IDs.get(l);
						Set<PageClass> classes = golden2Classes.get(l);
						classes.add(pc);
						for(Page page : pc.getPages()) {
							ids.add( page.getUrl().split("/")[page.getUrl().split("/").length-1].split(".html")[0]);
						}
						golden2Classes.put(l, classes);
						golden2IDs.put(l, ids);
					}
				}
			}
			pageClasses.removeAll(toRemove);
		}
		int i = 0;
		for (Set<String> goldenS : golden2Classes.keySet()) {
			String print = i + ") " + goldenS.toString() + " -> ";
			i ++;
			for (PageClass p : golden2Classes.get(goldenS)) {
				print = print.concat(" - ");

				for (Page page : p.getPages()) {
					String name = page.getUrl().split("/")[page.getUrl().split("/").length-1].split(".html")[0];
					print=print.concat(name + " ");
				}
			}
			System.out.println(print);
			print = new String();
		}
		try {
			for (Set<String> g : golden) {
				bw.write(g + " -> ");
				if (!golden2Classes.containsKey(g)) {
					bw.write("[]\n");
				}else {
					bw.write(" [");
					for (PageClass p : golden2Classes.get(g)) {
						bw.write(" - ");
						for (Page page : p.getPages()) {
							String name = page.getUrl().split("/")[page.getUrl().split("/").length-1].split(".html")[0];
							bw.write(name + " ");
						}
					}
					bw.write("]\n");
				}
			}
		} catch (IOException e) {
			System.out.println("IO error");
		}
		return golden2IDs;

	}




	private static boolean belongsTo(Set<String> l, PageClass pc) {
		int samePages = 0;
		for (String URL : pc.getPagesURL()) {
			String name = URL.split("/")[URL.split("/").length-1].split(".html")[0];

			if (l.contains(name)) {
				samePages++;
			}
		}
		return (samePages > (pc.getPagesURL().size()/2)/* || (samePages == (pc.getPagesID().size()/2))*//*);
	}*/

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
