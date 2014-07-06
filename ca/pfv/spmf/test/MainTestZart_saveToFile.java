package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TFTableFrequent;
import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
/**
 * Example of how to use the Zart Algorithm in source code.
 * @author Philippe Fournier-Viger, 2008
 *
 */
public class MainTestZart_saveToFile {

	public static void main(String[] args) throws IOException {

		String input = fileToPath("contextZart.txt");  // the database
		String output = ".//zart_output.txt";  // the path for saving the frequent itemsets found
		
		// Load a binary context
		TransactionDatabase context = new TransactionDatabase();
		context.loadFile(input);

		// Apply the Zart algorithm
		double minsup = 0.4;
		AlgoZart zart = new AlgoZart();
		TZTableClosed results = zart.runAlgorithm(context, minsup);
		TFTableFrequent frequents = zart.getTableFrequent();
		zart.printStatistics();
		zart.saveResultsToFile(output);
			
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestZart_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
