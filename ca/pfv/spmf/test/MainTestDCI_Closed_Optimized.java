package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.dci_closed_optimized.AlgoDCI_Closed_Optimized;

/**
 * Example of how to use DCI_Closed algorithm from the source code.
 * (optimized version)
 * @author Philippe Fournier-Viger 
 */
public class MainTestDCI_Closed_Optimized {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextPasquier99.txt");
		String output = ".//output.txt";
		int minsup = 2;  // means 2 transactions (we use a relative support)
		
		// Applying the  algorithm
		AlgoDCI_Closed_Optimized algorithm = new AlgoDCI_Closed_Optimized();
		algorithm.runAlgorithm(input, output, minsup);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestDCI_Closed_Optimized.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
