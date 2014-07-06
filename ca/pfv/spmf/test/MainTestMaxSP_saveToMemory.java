package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoMaxSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.SequentialPatterns;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;

/**
 * Example of how to use the MaxSP algorithm, from the source code.
 * 
 * @author Philippe Fournier-Viger
 */
public class MainTestMaxSP_saveToMemory {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		sequenceDatabase.print();
		// Create an instance of the algorithm
		AlgoMaxSP algo  = new AlgoMaxSP();
		
		// execute the algorithm
		SequentialPatterns patterns = algo.runAlgorithm(sequenceDatabase, null, 2);    
		algo.printStatistics(sequenceDatabase.size());
		patterns.printFrequentPatterns(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMaxSP_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}