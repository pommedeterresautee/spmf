package ca.pfv.spmf.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.spam.AlgoVGEN;
import ca.pfv.spmf.algorithms.sequentialpatterns.spam.PatternVGEN;


/**
 * Example of how to use the VGEN algorithm in source code.
 * @author Philippe Fournier-Viger
 */
public class MainTestVGEN_saveToMemory {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		String input = fileToPath("contextPrefixSpan.txt");
		String output = ".//output.txt";
		
		// Create an instance of the algorithm 
		AlgoVGEN algo = new AlgoVGEN(); 
//		algo.setMaximumPatternLength(3);
		
		// execute the algorithm with minsup = 2 sequences  (50 %)
		List<Map<Integer, List<PatternVGEN>>> generatorPatterns = algo.runAlgorithm(input, output, 0.1);    
		algo.printStatistics();
		
	    // PRINT THE PATTTERNS FOUND
		 for(Map<Integer, List<PatternVGEN>> map : generatorPatterns) {
	    	  if(map == null) {
	    		  continue;
	    	  }
	    	  // for each pattern
	    	  for(List<PatternVGEN> patterns : map.values()) {
	    		  for(PatternVGEN pattern : patterns) {
	    			  System.out.println(" " + pattern.getPrefix() + "  support : " + pattern.bitmap.getSupport());
	    		  }
	    	}
		}
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestVGEN_saveToMemory.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}