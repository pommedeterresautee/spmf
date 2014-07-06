package ca.pfv.spmf.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.estDec.Algo_estDec;

/**
 * Class to test the estDec algorithm and save the result to a file.
 */
public class MainTest_estDec_saveToFile {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		
		String database= "contextIGB.txt";
		String output = "output"; 

        if (arg.length!=0)
          database=arg[0];
        
        double mins = 0.1;

		Algo_estDec algo = new Algo_estDec(mins);
		// process a set of transactions from a file
		algo.processTransactionFromFile(database);
		// perform mining and save the result to a file
		algo.performMining_saveResultToFile(output);
		// print statistics
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTest_estDec_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
