package ca.pfv.spmf.algorithms.sequential_rules.rulegen;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.SequentialPattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan.SequentialPatterns;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the RuleGen algorithm proposed by Zaki et al to generate sequential rules where
 * the antecedent and consequent are sequential patterns.  The RuleGen algorithm is described in: 
 * <br/><br/>
 * 
 *    M. J. Zaki, “SPADE: An Efficient Algorithm for Mining Frequent Se-quences,”Machine Learning, vol. 42, no.1-2, pp. 31-60, 2001.
 * <br/><br/>
 * 
 * However, note that instead of using the SPADE algorithm,  we use the PrefixSpan algorithm because at the time
 * that this algorithm was implemented there was no implementation of SPADE in SPMF and PrefixSpan is a fast
 * algorithm. 
 * 
 * @see AlgoPrefixSpan
 * @see SequentialPattern
 * @see SequentialPatterns
 * @author Philippe Fournier-Viger
 */
public class AlgoRuleGen {
	
	// start time of the latest execution
	private long startTime; 
	
	// end time of the latest execution
	private long endTime;   
	
	private int patternCount; // the numer of rules found
	
	// object to write the output file path
	BufferedWriter writer = null;

	/**
	 * Default constructor.
	 */
	public AlgoRuleGen() {
	}
	
	/**
	 * Run the algorithm
	 * @param minsup  the minimum support threshold
	 * @param minconf the minimum confidence threshold
	 * @param input   the input file path
	 * @param output  the output file path for saving the result
	 * @throws IOException exception if there is an error reading/writing files
	 */
	public void runAlgorithm(int minsup, double minconf, String input, String output) throws IOException {
		// Prepare object for writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		// record the start time
		startTime = System.currentTimeMillis();
		
		// Load the sequence database taken as input
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(input);
				
		// STEP 1: Apply the PrefixSpan algorithm to generate frequent sequential patterns
		AlgoPrefixSpan algo = new AlgoPrefixSpan(); 
		SequentialPatterns patternsLists = algo.runAlgorithm(sequenceDatabase, null, minsup);    

		// STEP 2: Generate  rules of the form    a ==> b, 
		// where a and b are sequential patterns 
		// such that a is a subsequence of b.
		// For each rule
		
		// for each seq. pattern a (pattern1)  of size i
		for (int i = 0; i < patternsLists.getLevels().size(); i++) {
			for(int j=0; j < patternsLists.getLevel(i).size(); j++){
				SequentialPattern pattern1 = patternsLists.getLevel(i).get(j);
				
				//for each seq. pattern  b (pattern2) of SIZE k > i
				for (int k = i+1; k < patternsLists.getLevels().size(); k++) {
					for(int m =0; m < patternsLists.getLevel(k).size(); m++){
						SequentialPattern pattern2 = patternsLists.getLevel(k).get(m);
						
						// try to generate a rule   a ==> b
						tryToGenerateRule(pattern1, pattern2, minconf);
						// try to generate a rule   b ==> a
						tryToGenerateRule(pattern2, pattern1, minconf);
					}
				}
			}
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		// record the end time
		endTime = System.currentTimeMillis();
		// close the output file
		writer.close();	
	}
	
	
	
	/**
	 *  Try to generate a rule between two sequential patterns. The rule is generated if the
	 *  pattern1 is included in pattern2 and if the confidence is high enough.
	 * @param pattern1 a sequential pattern
	 * @param pattern2 another sequential pattern
	 * @throws IOException 
	 */
	private void tryToGenerateRule(SequentialPattern pattern1, SequentialPattern pattern2, double minconf) throws IOException {
		// if pattern1 is not contained in pattern2, we stop
		// because we want that pattern1 is strictly included in pattern2
		if(strictlyContains(pattern2, pattern1) == false){
			return;
		}
		// calculate the confidence of:     pattern1 ==>  pattern2  / pattern1
		double conf = ((double) pattern2.getAbsoluteSupport()) / pattern1.getAbsoluteSupport();
		
		// if not enough confidence, then the rule is not valid
		if(conf < minconf){
			return;
		}
		
		// otherwise it is a valid rule so
		// increase pattern count
		patternCount++;
		// then save it to file.
		
		// Create a string buffer
		StringBuffer buffer = new StringBuffer();
//		
		// write the rule
		buffer.append(pattern1.itemsetsToString());
		buffer.append(" ==> ");
		buffer.append(pattern2.itemsetsToString());
//
		// write support
		buffer.append(" #SUP: ");
		buffer.append(pattern2.getAbsoluteSupport());
		
		// write confidence
		buffer.append(" #CONF: ");
		buffer.append(conf);
		
		writer.write(buffer.toString());  // write to file 
		writer.newLine(); // write a new line
		
	}
	
	/**
	 * This methods checks if a seq. pattern "pattern2" is strictly contained in a seq. pattern "pattern1".
     * @param pattern1 a sequential pattern
     * @param pattern2 another sequential pattern
	 * @return true if the pattern1 contains pattern2.
	 */
	boolean strictlyContains(SequentialPattern pattern1, SequentialPattern pattern2) {
		// if pattern2 is larger or equal in size, then it cannot be contained in pattern1
		if(pattern1.size() <= pattern2.size()){
			return false;
		}
		
		// To see if pattern2 is strictly contained in pattern1,
		// we will search for each itemset i of pattern2 in pattern1 by advancing
		// in pattern 1 one itemset at a time.
		
		int i =0; // position in pattern2
		int j= 0; // position in pattern1
		while(true){
			//if the itemset at current position in pattern1 contains the itemset
			// at current position in pattern2
			if(pattern1.getItemsets().get(j).containsAll(pattern2.get(i))){
				// go to next itemset in pattern2
				i++;
				// if we reached the end of pattern2, then return true
				if(i == pattern2.size()){
					return true;
				}
			}
			// go to next itemset in pattern1
			j++;
			// if we reached the end of pattern1, then pattern2 is not strictly included
			// in it, and return false
			if(j >= pattern1.size()){
				return false;
			}
			// lastly, for optimization, we check how many itemsets are left to be matched.
			// if there is less itemsets left in pattern1 than in pattern2, then it will
			// be impossible to get a total match, and so we return false.
			if((pattern1.size() - j)< pattern2.size()  - i){
				return false;
			}
		}
		
	}

	/**
	 * Print statistics to System.out about the latest execution of the algorithm.
	 */
	public void printStats() {
		System.out
				.println("=============  SEQUENTIAL RULES - STATS =============");
		System.out.println("Sequential rules count: " + patternCount);
		System.out.println("Total time : " + (endTime - startTime) + " ms");		
		System.out.println("Max memory: " + MemoryLogger.getInstance().getMaxMemory());
		System.out
				.println("===================================================");
	}
}
