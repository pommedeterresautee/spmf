package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import ca.pfv.spmf.algorithms.frequentpatterns.defme.AlgoDefMe;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets;


/**
 * Example of how to use DefMe algorithm from the source code.
 * @author Philippe Fournier-Viger - 2009
 */
public class MainTestDefMe_saveToMemory {

	public static void main(String [] arg) throws IOException{
		// Loading the binary context
		String input = fileToPath("contextZart.txt");  // the database
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)
		
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Applying the DefMe algorithm
		AlgoDefMe algo = new AlgoDefMe();
		Itemsets generators = algo.runAlgorithm(null, database, minsup);
		algo.printStats();
		for(List<Itemset> genSizeK : generators.getLevels()) {
			for(Itemset itemset : genSizeK) {
				System.out.println(Arrays.toString(itemset.getItems()) + " #SUP: " + itemset.getAbsoluteSupport());
			}
		}
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestDefMe_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
