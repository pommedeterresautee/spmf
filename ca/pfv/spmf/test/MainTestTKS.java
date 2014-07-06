package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.PriorityQueue;

import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoTKS;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.Pattern;


/**
 * Example of how to use the TKS algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestTKS {

	public static void main(String [] arg) throws IOException{ 

		// Load a sequence database
		String input = fileToPath("contextPrefixSpan.txt");
		String output = "top_k_sequential_patterns___.txt";
		
		int k=5;
		
		// Create an instance of the algorithm 
		AlgoTKS algo = new AlgoTKS(); 
		
		// execute the algorithm
		PriorityQueue<Pattern> patterns = algo.runAlgorithm(input, output, k);    
		algo.writeResultTofile(output);   // to save results to file
		algo.printStatistics();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTKS.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}