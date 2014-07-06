package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
/**
 * Example of how to mine all association rules with FPGROWTH with the lift,
 * from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAllAssociationRules_FPGrowth_saveToMemory_with_lift {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// Loading the binary context
		String input = fileToPath("contextIGB.txt");
		
		// STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
		double minsupp = 0.5;
		AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
		Itemsets patterns = fpgrowth.runAlgorithm(input, null, minsupp);
		int databaseSize = fpgrowth.getDatabaseSize();
		patterns.printItemsets(databaseSize);
		
		// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double  minlift = 0;
		double  minconf = 0.90;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		// the next line run the algorithm.
		// Note: we pass null as output file path, because we don't want
		// to save the result to a file, but keep it into memory.
		AssocRules rules = algoAgrawal.runAlgorithm(patterns,null, databaseSize, minconf, minlift);
		rules.printRulesWithLift(databaseSize);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules_FPGrowth_saveToMemory_with_lift.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
