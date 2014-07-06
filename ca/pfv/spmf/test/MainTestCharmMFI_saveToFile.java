package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharmMFI;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharm_Bitset;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;

/**
 * Example of how to use the CHARM-MFI algorith, from the source code.
 * 
 * @author Philippe Fournier-Viger (Copyright 2009)
 */
public class MainTestCharmMFI_saveToFile {

	public static void main(String [] arg) throws IOException{

		// the file paths
		String input = fileToPath("contextPasquier99.txt");  // the database
		String output = ".//output.txt";  // the path for saving the frequent itemsets found
		
		// minimum support
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Loading the binary context
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		database.printDatabase();
		
		// Applying the Charm algorithm
		AlgoCharm_Bitset charm = new AlgoCharm_Bitset();
		charm.runAlgorithm(null, database, minsup, false, 10000);
		// NOTE 0: for CHARM, we use null as output path to keep the result
		// in memory instead of writing it to a file.
		
		// NOTE 1: if you  use "true" in the line above, CHARM will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		
		// NOTE 2:  1000000 is the hashtable size used by CHARM for
		// storing itemsets.  Most users don't
		// need to change this parameter.

		// Run CHARM MFI
		AlgoCharmMFI charmMFI = new AlgoCharmMFI();
		charmMFI.runAlgorithm(output, charm.getClosedItemsets());
		charmMFI.printStats(database.size()); 

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCharmMFI_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
