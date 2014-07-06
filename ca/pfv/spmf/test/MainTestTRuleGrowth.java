package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth.AlgoTRuleGrowth;

/**
 * Example of how to use the TRULEGROWTH algorithm in source code.
 * @author Philippe Fournier-Viger (Copyright 2010)
 */
public class MainTestTRuleGrowth {
	
	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextPrefixSpan.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found

		//  Applying RuleGROWTH algorithm with minsup = 3 sequences and minconf = 0.5
		double minsup = 0.7;
		double minconf = 0.8;
		int windowSize = 3;

		AlgoTRuleGrowth algo = new AlgoTRuleGrowth();
		algo.runAlgorithm(minsup, minconf, input, output, windowSize);

		// print statistics
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTRuleGrowth.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
