package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoMaxSP;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
/*
 * Example of how to use the BIDE+ algorithm, from the source code.
 */
public class MainTestMaxSP_saveToFile {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile("D:\\eclipse-java\\workspace\\" +
				"CMAPS_PROJECT_3october2013_adaptedByAntonioTwoCMAPS\\src\\FIFA.txt");
//		sequenceDatabase.print();
		
		int minsup = 3200; // we use a minsup of 2 sequences (50 % of the database size)
		
		AlgoMaxSP algo  = new AlgoMaxSP();  //
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, ".//output.txt", minsup);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMaxSP_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}