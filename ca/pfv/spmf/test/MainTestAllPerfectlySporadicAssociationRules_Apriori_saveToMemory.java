package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.frequentpatterns.apriori_inverse.AlgoAprioriInverse;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;


/**
 * Example of how to mine perfectly sporadic association rules with AprioriInverse,
 * from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAllPerfectlySporadicAssociationRules_Apriori_saveToMemory {

	public static void main(String [] arg) throws IOException{
		// Loading the binary context
		String input = fileToPath("contextInverse.txt");
		String output = null; // null because we want to keep the result into memory instead of save it to a file in this example
		
		// STEP 1: Applying the APRIORI-Inverse algorithm to find sporadic itemsets
		// the thresholds that we will use:
		double minsup = 0.1;
		double maxsup = 0.6;
		
		AlgoAprioriInverse apriori = new AlgoAprioriInverse();
		Itemsets patterns = apriori.runAlgorithm(minsup, maxsup, input, null);
		int databaseSize = apriori.getDatabaseSize();
		patterns.printItemsets(databaseSize);
		
		// STEP 2: Generating all rules from the set of sporadic itemsets (based on Agrawal & Srikant, 94)
		double  minconf = 0.60;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
		AssocRules rules = algoAgrawal.runAlgorithm(patterns, output,databaseSize, minconf);
		rules.printRules(databaseSize);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllPerfectlySporadicAssociationRules_Apriori_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
