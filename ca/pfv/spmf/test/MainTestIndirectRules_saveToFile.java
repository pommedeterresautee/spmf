package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.Indirect.AlgoINDIRECT;

/**
 * Example of how to mine indirect association rules, in source code.
 * @author Philippe Fournier-Viger 
 */
public class MainTestIndirectRules_saveToFile {

	public static void main(String [] arg) throws NumberFormatException, IOException{
		// Loading the binary context
		String input = fileToPath("contextIndirect.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found

		double minsup = 0.6; // we use a minimum support of 60 %
		double ts = 0.5; // we use a ts of 50 %
		double minconf = 0.1;  // we use a minimum confidence threshold of 20 % (we could modify the algorithm to use some other measures).
		
		// Applying the algorithm
		AlgoINDIRECT indirect = new AlgoINDIRECT();
		indirect.runAlgorithm(input, output, minsup, ts, minconf);
		indirect.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestIndirectRules_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
