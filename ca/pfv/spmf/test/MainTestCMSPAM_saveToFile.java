package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoCMSPAM;


/**
 * Example of how to use the SPAM algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestCMSPAM_saveToFile {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		//String input = fileToPath("D1C20T20N0.5S6I5_SPMF.txt");
       String input = fileToPath("contextPrefixSpan.txt");
       String output = ".//output.txt";
            
		
		// Create an instance of the algorithm 
		AlgoCMSPAM algo = new AlgoCMSPAM(); 
//		algo.setMaximumPatternLength(3);
		
		// execute the algorithm with minsup = 2 sequences  (50 %)
		algo.runAlgorithm(input, output, 0.5);     // minsup = 106   k = 1000   BMS
		algo.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCMSPAM_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}