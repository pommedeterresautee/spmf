package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharm_Bitset;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets;


/**
 * Example of how to use CHARM-Bitset algorithm from the source code
 * and keep the result in memory.
 * @author Philippe Fournier-Viger 2014
 */
public class MainTestCharm_bitset_saveToMemory {

	public static void main(String [] arg) throws IOException{

		// the database
		String input = fileToPath("contextPasquier99.txt");  
		// the minsup threshold
		// Note : 0.4 means a minsup of 2 transaction (we used a relative support)
		double minsup = 0.4; 

		// Read the input file
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Applying the CHARM algorithm
		AlgoCharm_Bitset algo = new AlgoCharm_Bitset();
		Itemsets itemsets = algo.runAlgorithm(null, database, minsup, true, 10000);
		// NOTE 1: if you  use "true" in the line above, CHARM will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		
		// Print the itemsets found
		itemsets.printItemsets(database.size());

		// Print statistics about the algorithm execution.
		algo.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCharm_bitset_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
