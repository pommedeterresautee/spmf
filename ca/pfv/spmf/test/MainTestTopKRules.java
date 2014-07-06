package ca.pfv.spmf.test;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database;

/**
 * Example of how to use the TOPKRULES algorithm in source code.
 * @author Philippe Fournier-Viger (Copyright 2010)
 */
public class MainTestTopKRules {

	public static void main(String [] arg) throws Exception{
		// Load database into memory
		Database database = new Database(); 
		database.loadFile(fileToPath("contextIGB.txt")); 
		
		int k = 10; 
		double minConf = 0.8; //
		
		AlgoTopKRules algo = new AlgoTopKRules();
		algo.runAlgorithm(k, minConf, database);

		algo.printStats();
		algo.writeResultTofile(".//output.txt");   // to save results to file

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTopKRules.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
