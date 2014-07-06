package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoFSGP;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.SequentialPattern;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;


/**
 * Example of how to use the FSGP algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestFSGP_saveToMemory {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		// print the database to console
		sequenceDatabase.print();
		
		// Create an instance of the algorithm 
		AlgoFSGP algo = new AlgoFSGP(); 
//		algo.setMaximumPatternLength(3);
		
		// execute the algorithm with minsup = 50 %
		boolean performPruning = true;// to activate pruning of search space
		List<SequentialPattern> patterns = algo.runAlgorithm(sequenceDatabase, 0.5, null, performPruning);    
		algo.printStatistics(sequenceDatabase.size());
		System.out.println(" == PATTERNS ==");
		for(SequentialPattern pattern : patterns){
			System.out.println(pattern + " support : " + pattern.getAbsoluteSupport());
		}
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFSGP_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}