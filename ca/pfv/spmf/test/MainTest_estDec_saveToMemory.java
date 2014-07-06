package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.frequentpatterns.estDec.Algo_estDec;

/**
 * Class to test the estDec algorithm and save the result to memory.
 */
public class MainTest_estDec_saveToMemory {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
        
        double mins = 0.1;

		Algo_estDec algo = new Algo_estDec(mins);
		// To set the decay rate manually:
		algo.setDecayRate(2, 10000); 
		// To process transactions from memory:
		algo.processTransaction(new int[] {1,2,4, 5});
		algo.processTransaction(new int[] {2,3,5});
		algo.processTransaction(new int[] {1,2,4, 5});
		algo.processTransaction(new int[] {1,2,3,5});
		algo.processTransaction(new int[] {1,2,3,4,5});
		algo.processTransaction(new int[] {2,3,5});
		// To perform mining and save the result to memory:
		Hashtable<List<Integer>, Double> result = algo.performMining_saveResultToMemory();
		// print statistics
		algo.printStats();
		// Print the results to the console
		System.out.println("RESULTS:");
		for(Entry<List<Integer>, Double> itemset : result.entrySet()) {
			System.out.println("" + itemset.getKey() + " #SUP: " + itemset.getValue());
		}

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTest_estDec_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
