package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth_with_strings.AlgoFPGrowth_Strings;

/**
 * Example of how to use FPGrowth from the source code and saves
 * the result to a file.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestFPGrowth_strings_saveToFile {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// Loading the binary context
		String input =  fileToPath("tennis.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Applying the FPGROWTH algorithmMainTestFPGrowth.java
		AlgoFPGrowth_Strings algo = new AlgoFPGrowth_Strings();
		algo.runAlgorithm(input, output, minsup);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPGrowth_strings_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
