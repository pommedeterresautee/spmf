package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
/**
 * Example of how to mine all association rules with CFPGROWTH and save
 * the result to a file, from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2014)
 */
public class MainTestAllAssociationRules_CFPGrowth_saveToFile {

	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextCFPGrowth.txt");
		String output = ".//output.txt";
		String MISfile = fileToPath("MIS.txt");
		
		// STEP 1: Applying the CFP-GROWTH algorithm to find frequent itemsets
		AlgoCFPGrowth cfpgrowth = new AlgoCFPGrowth();
		Itemsets patterns = cfpgrowth.runAlgorithm(input, null, MISfile);
		patterns.printItemsets(20);
		int databaseSize = cfpgrowth.getDatabaseSize();
		cfpgrowth.printStats();
		
		// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double  minconf = 0.50; 
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		AssocRules rules = algoAgrawal.runAlgorithm(patterns, null, databaseSize, minconf);
		algoAgrawal.printStats();
		rules.printRules(20);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules_CFPGrowth_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
