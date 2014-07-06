package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.uapriori.AlgoUApriori;
import ca.pfv.spmf.algorithms.frequentpatterns.uapriori.UncertainTransactionDatabase;

/**
 * Example of how to use the UApriori Algorithm in source code.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestUApriori_saveToFile {

	public static void main(String [] arg) throws IOException{

		// Loading the binary context
		UncertainTransactionDatabase context = new UncertainTransactionDatabase();
		try {
			context.loadFile(fileToPath("contextUncertain.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printDatabase();
		
		String output = ".//output.txt"; 
		
		// Applying the UApriori algorithm
		AlgoUApriori apriori = new AlgoUApriori(context);
		apriori.runAlgorithm(0.1, output);
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestUApriori_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
