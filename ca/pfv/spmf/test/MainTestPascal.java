package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.pascal.AlgoPASCAL;

/**
 * Example of how to use the PASCAL algorithm (hash-tree version),
 * from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestPascal {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("contextZart.txt");
		String output = "output.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

				
		// Applying the PASCAL algorithm
		AlgoPASCAL algorithm = new AlgoPASCAL();
		algorithm.runAlgorithm(minsup, input, output);
		algorithm.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPascal.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
