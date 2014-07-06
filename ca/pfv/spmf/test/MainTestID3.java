package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.algorithms.classifiers.decisiontree.id3.AlgoID3;
import ca.pfv.spmf.algorithms.classifiers.decisiontree.id3.DecisionTree;

/**
 * Example of how to use ID3 from the source code.
 * @author Philippe Fournier-Viger (Copyright 2011)
 */
public class MainTestID3 {

	public static void main(String [] arg) throws IOException{
		// Read input file and run algorithm to create a decision tree
		AlgoID3 algo = new AlgoID3();
		// There is three parameters:
		// - a file path
		// - the "target attribute that should be used to create the decision tree
		// - the separator that was used in the file to separate values (by default it is a space)
		DecisionTree tree = algo.runAlgorithm(fileToPath("tennis.txt"), "play", " ");
		algo.printStatistics();
		
		// print the decision tree:
		tree.print();
		
		// Use the decision tree to make predictions
		// For example, we want to predict the class of an instance:
		String [] instance = {null, "sunny", "hot", "normal", "weak"};
		String prediction = tree.predictTargetAttributeValue(instance);
		System.out.println("The class that is predicted is: " + prediction);
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestID3.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
