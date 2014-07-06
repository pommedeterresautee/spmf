package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
/**
 * Example of how to mine all association rules with CFPGROWTH,
 * from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2014)
 */
public class MainTestAllAssociationRules_CFPGrowth_saveToMemory {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		String input = fileToPath("contextIGB.txt");
		String MISfile = fileToPath("MIS.txt");
		
		// STEP 1: Applying the CFP-GROWTH algorithm to find frequent itemsets
		AlgoCFPGrowth cfpgrowth = new AlgoCFPGrowth();
		Itemsets patterns = cfpgrowth.runAlgorithm(input, null, MISfile);
//		patterns.printItemsets(database.size());
		int databaseSize = cfpgrowth.getDatabaseSize();
		cfpgrowth.printStats();
		
		// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double  minconf = 0.90; 
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		// the next line run the algorithm.
		// Note: we pass null as output file path, because we don't want
		// to save the result to a file, but keep it into memory.
		AssocRules rules = algoAgrawal.runAlgorithm(patterns, null, databaseSize, minconf);
		rules.printRules(databaseSize);
		
		//System.out.println("DATABASE SIZE :" + databaseSize);

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules_CFPGrowth_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
