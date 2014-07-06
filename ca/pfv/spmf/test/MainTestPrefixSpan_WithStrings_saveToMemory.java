package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings.AlgoPrefixSpan_with_Strings;
import ca.pfv.spmf.input.sequence_database_list_strings.SequenceDatabase;


/**
 * Example of how to use the Prefixspan algorithms with strings,
 * from source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestPrefixSpan_WithStrings_saveToMemory {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpanStrings.txt"));
		// print the database to console
		sequenceDatabase.printDatabase();
		
		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpan_with_Strings algo = new AlgoPrefixSpan_with_Strings(); 
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, null, 2);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrefixSpan_WithStrings_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}