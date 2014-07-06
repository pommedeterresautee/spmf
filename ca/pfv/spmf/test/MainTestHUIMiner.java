package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoHUIMiner;

/**
 * Example of how to use the HUIMiner algorithm 
 * from the source code.
 * @author Philippe Fournier-Viger, 2010
 */
public class MainTestHUIMiner {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_Utility.txt");
		String output = ".//output.txt";

		int min_utility = 30;  // 
		
		// Applying the HUIMiner algorithm
		AlgoHUIMiner huiminer = new AlgoHUIMiner();
		huiminer.runAlgorithm(input, output, min_utility);
		huiminer.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHUIMiner.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
