package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;


/**
 * Example of how to use FPGrowth from the source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestFPGrowth_saveToMemory {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// Loading the transaction database
		String input = fileToPath("contextPasquier99.txt");  // the database

		// the minimum support threshold
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Applying the FPGROWTH algorithmMainTestFPGrowth.java
		AlgoFPGrowth algo = new AlgoFPGrowth();
		// Run the algorithm
		// Note that here we use "null" as output file path because we want to keep the results into memory instead of saving to a file
		Itemsets patterns = algo.runAlgorithm(input, null, minsup);  
		// show the execution time and other statistics
		algo.printStats();
		// print the patterns to System.out
		patterns.printItemsets(algo.getDatabaseSize());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPGrowth_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
