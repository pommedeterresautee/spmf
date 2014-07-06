package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID.AlgoAprioriTID;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;

/**
 * Example of how to use the AprioriTID algorithm from the source code.
 * @author Philippe Fournier-Viger 
 */
public class MainTestAprioriTID {

	public static void main(String [] arg) throws NumberFormatException, IOException{
		// Loading the binary context
		String inputfile = fileToPath("contextPasquier99.txt");
		
		// Applying the AprioriTID algorithm
		AlgoAprioriTID apriori = new AlgoAprioriTID();
		
		// We run the algorithm.
		// Note: we pass a null value for the output file 
		//      because we want to keep the result into memory
		//      instead of writing it to an output file.
		Itemsets patterns = apriori.runAlgorithm(inputfile, null, 0.4);
		patterns.printItemsets(apriori.getDatabaseSize());
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriTID.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
