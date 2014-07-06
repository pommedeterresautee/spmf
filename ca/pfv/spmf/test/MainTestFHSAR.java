package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.fhsar.AlgoFHSAR;

/**
 * Example of how to use FHSAR algorithm from the source code.
 * @author Philippe Fournier-Viger, 2011
 */
public class MainTestFHSAR {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextIGB.txt");  // the database
		String inputSAR = fileToPath("sar.txt");  // the sensitive association rules that we want to hide
		String output = ".//output.txt";  // the path for saving the transformed database
		double minsup = 0.5;
		double  minconf = 0.60;
		
		// STEP 1: Applying the FHSAR algorithm to hide association rules
		AlgoFHSAR algorithm = new AlgoFHSAR();
		algorithm.runAlgorithm(input, inputSAR, output, minsup, minconf);
		algorithm.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFHSAR.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
