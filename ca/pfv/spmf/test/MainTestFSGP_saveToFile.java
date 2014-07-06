package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoFSGP;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;


/**
 * Example of how to use the FSGP algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestFSGP_saveToFile {

	public static void main(String [] arg) throws IOException{    
		String outputPath = ".//output.txt";
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		// print the database to console
//		sequenceDatabase.print();
		
		// Create an instance of the algorithm with minsup = 50 %
		AlgoFSGP algo = new AlgoFSGP(); 
		
		int minsup = 2; // we use a minimum support of 2 sequences.
		
		// execute the algorithm
		boolean performPruning = true;// to activate pruning of search space
		algo.runAlgorithm(sequenceDatabase, outputPath, minsup, performPruning);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFSGP_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}