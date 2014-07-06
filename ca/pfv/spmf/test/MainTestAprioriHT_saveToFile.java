package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.apriori_HT.AlgoAprioriHT;


/**
 * Example of how to use the APRIORI algorithm (hash-tree version),
 * from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAprioriHT_saveToFile {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("contextPasquier99.txt");
		String output = "frequent_itemsets.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// This version of apriori use hash-tree. We need to set the number of child nodes
		// that each node in the hash-tree has. By default, it is set to 30.
		// Changing this value higher or lower can influence the performance.
		int branch_count_in_hash_tree = 30;  
				
		// Applying the Apriori algorithm
		AlgoAprioriHT apriori = new AlgoAprioriHT();
		apriori.runAlgorithm(minsup, input, output, branch_count_in_hash_tree);
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriHT_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
