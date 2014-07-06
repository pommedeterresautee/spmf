package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoTSP_nonClosed;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;


/**
 * Example of how to use the PrefixSpanWithSupportRising algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestTSP_nonClosed {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		long startTime = System.currentTimeMillis();
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		System.out.println(System.currentTimeMillis() - startTime + " ms (database load time)");
		// print the database to console
//		sequenceDatabase.print();
			
		AlgoTSP_nonClosed algo = new AlgoTSP_nonClosed(); 
		
		int k = 2; // we use a k of 2 sequences.
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, k);    
		algo.writeResultTofile("C://patterns//sequential_patterns.txt");
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTSP_nonClosed.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}