package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth.AlgoCFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

/**
 * Example of how to use the CFPGrowth++ algorithm, from the source code and save the result to 
 * memory instead of into a file.
 */
public class MainTestCFPGrowth_saveToMemory {

	public static void main(String[] arg) throws FileNotFoundException,
			IOException {
		
		String database = fileToPath("contextCFPGrowth.txt");
		String output = null;  // because we want to indicate
		// that we want to keep the result into memory instead of 
		// saving it to a file
		String MISfile = fileToPath("MIS.txt");

		// Applying the CFPGROWTH algorithmMainTestFPGrowth.java
		AlgoCFPGrowth algo = new AlgoCFPGrowth();
		Itemsets result = algo.runAlgorithm(database, output, MISfile);
		algo.printStats();
		
		result.printItemsets(algo.getDatabaseSize());
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestCFPGrowth_saveToMemory.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
