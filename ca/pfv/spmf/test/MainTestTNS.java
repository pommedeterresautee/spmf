package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.AlgoTNS;
import ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns.Rule;
import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;
import ca.pfv.spmf.input.sequence_database_array_integers.SequenceDatabase;


/**
 * Example of how to use the TNS algorithm in source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestTNS {

	public static void main(String [] arg) throws Exception{
		// Load database into memory
		SequenceDatabase database = new SequenceDatabase(); 
		database.loadFile(fileToPath("contextPrefixSpan.txt"));

		int k = 30; 
		double minConf = 0.5; 
		int delta =  2;
		
		AlgoTNS algo = new AlgoTNS();
		RedBlackTree<Rule> kRules = algo.runAlgorithm(k, database, minConf,   delta );
		algo.writeResultTofile(".//output.txt");   // to save results to file
		
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTNS.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
