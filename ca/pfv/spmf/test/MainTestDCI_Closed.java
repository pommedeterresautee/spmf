package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.dci_closed.AlgoDCI_Closed;

/**
 * Example of how to use DCI_Closed algorithm from the source code.
 * @author Philippe Fournier-Viger 
 */
public class MainTestDCI_Closed {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextPasquier99.txt");
		String output = ".//output.txt";
		int minsup = 2;  // means 2 transactions (we used a relative support)
		
		// Applying the  algorithm
		AlgoDCI_Closed algorithm = new AlgoDCI_Closed();
		algorithm.runAlgorithm(input, output, minsup);
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestDCI_Closed.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
