package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.vme.AlgoVME;

/**
 * Example of how to use the VME Algorithm in source code.
 * @author Philippe Fournier-Viger 
 */
public class MainTestVME {

	public static void main(String [] arg) throws NumberFormatException, IOException{
		// Loading the binary context
		
		String input = fileToPath("contextVME.txt");  // the database
		String output = ".//output.txt";  // the path for saving the earasable itemsets found
		
		double threshold = 0.15; // a threshold of 15 %
		
		// Applying the  algorithm
		AlgoVME algo = new AlgoVME();
		algo.runAlgorithm(input, output, threshold);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestVME.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
