package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.lapin.AlgoLAPIN_LCI;

/**
 * Example of how to use the LAPIN_LCI (a.k.a LAPIN-SPAM) algorithm in source code.
 * @author Philippe Fournier-Viger 2014
 */
public class MainTestLAPIN_saveToFile {

	public static void main(String [] arg) throws IOException{   
		String inputPath = fileToPath("contextPrefixSpan.txt");
		String outputPath = ".//output.txt";
		
		// Create an instance of the algorithm with minsup = 50 %
		AlgoLAPIN_LCI algo = new AlgoLAPIN_LCI(); 
		
		double minsup = 0.2; // we use a minimum support of 2 sequences.
		
		// execute the algorithm
		algo.runAlgorithm(inputPath, outputPath, minsup);    
		algo.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestLAPIN_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}