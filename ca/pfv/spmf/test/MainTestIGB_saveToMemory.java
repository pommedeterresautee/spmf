package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.IGB.AlgoIGB;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.rule_itemset_array_integer_with_count.Rules;

/**
 * Examples of how to generate the IGB basis of association rules,
 * from the source code, and keep the result into memory.
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class MainTestIGB_saveToMemory {

	public static void main(String[] args) throws IOException {
		System.out.println("STEP 1 : EXECUTING THE ZART ALGORITHM TO FIND CLOSED ITEMSETS AND MINIMUM GENERATORS");
		String input = fileToPath("contextIGB.txt");
		String output = null;
		// Note : we set the output to null because we choose to keep 
		// the result into memory instead of saving to a file, for this
		// example.
		
		TransactionDatabase database = new TransactionDatabase();
		try {
			database.loadFile(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Applying the Zart algorithm
		AlgoZart zart = new AlgoZart();
		double minsup = 0.5;
		TZTableClosed results = zart.runAlgorithm(database, minsup);
		zart.printStatistics();
		
		System.out.println("STEP 2 : RUNNING THE IGB ALGORITHM");
		// Apply the IGB algorithm
		double minconf = 0.61; // minimum confidence
		AlgoIGB algoIGB = new AlgoIGB();
		Rules rules = algoIGB.runAlgorithm(results, database.getTransactions().size(), minconf,output);
		algoIGB.printStatistics();
		rules.printRules(database.getTransactions().size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestIGB_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
