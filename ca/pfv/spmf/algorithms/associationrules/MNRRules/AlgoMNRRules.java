package ca.pfv.spmf.algorithms.associationrules.MNRRules;


/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.rule_itemset_array_integer_with_count.Rule;
import ca.pfv.spmf.patterns.rule_itemset_array_integer_with_count.Rules;
/**
 * This is an implementation of an algorithm for finding the set of Minimum Non Redundant rules (MNR)
 * from a transaction database.  
 * <br/><br/>
 * 
 * This set is defined as the set of association rules 
 * of the form P1 ==> P2 / P1, where P1 is a generator of P2,
 * P2 is a closed itemset, and the rule has a support and confidence 
 * respectively no less than minsup and minconf. 
 * <br/><br/>
 * 
 * See the following publication for more details:
 * <br/><br/>
 * 
 * M. Kryszkiewicz. Representative Association Rules. In PAKDD '98: Proceedings
 * of the Second Pacic-Asia Conference on Research and Development in Knowledge 
 * Discovery and Data Mining, pages 198209, London, UK, 1998. Springer-Verlag.
 * <br/><br/>
 * 
 * Here, the implementation is based on the description in  Szathmary's thesis (2006). 
 * The algorithm proceed by exploiting the generators and closed itemset found 
 * by the Zart algorithm.
 *<br/><br/>
 *
 * This algorithm can save the result to a file or keep it into memory
 * if the user gives a null output path to the runAlgorithm() method.
 * <br/><br/>
 *
 * @author Philippe Fournier-Viger
 * 
 */
public class AlgoMNRRules {
	// parameters
	private TZTableClosed closedPatternsAndGenerators; //closed itemsets and their generators
	private double minconf; // minimum confidence threshold
	private int databaseSize; //
	
	// for statistics
	long startTimestamp = 0; // last execution start time
	long endTimeStamp = 0;   // last execution end time
	private int ruleCount;  // number of rule found
	
	// the rules found (if we save to memory)
	private Rules rules;
	
	// object to write the output file if the user wish to write to a file
	BufferedWriter writer = null;
	
	/**
	 * Default constructor
	 */
	public AlgoMNRRules(){
		
	}

	/**
	 * Run the algorithm.
	 * @param closedPatternsAndGenerators  Closed itemsets and their associated generators.
	 * @param databaseSize the number of transactions in the transaction database.
	 * @param minconf minimum confidence threshold
	 * @param outputFilePath the output file path, if the results should be saved to a file.
	 *                       if null, the result are saved in memory and returned by this method.
	 *                       
	 * @return if the user chose to save to memory, this methods return the set of IGB association rules,
	 *         otherwise, the result is saved to the output file chosen by the user.
	 * @throws IOException
	 */
	public Rules runAlgorithm(String outputFilePath, double minconf, TZTableClosed closedPatternsAndGenerators, int databaseSize) throws IOException {
		
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			rules = new Rules("MNR association rules");
	    }else{ 
	    	// if the user want to save the result to a file
	    	rules = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		// save the parameters received by the user
		this.closedPatternsAndGenerators = closedPatternsAndGenerators;
		this.minconf = minconf; 
		this.databaseSize = databaseSize;
		
		// reset the number of rule found
		ruleCount = 0;
		// record start time
		startTimestamp = System.currentTimeMillis();
		

		
		// 1 - for each equivalence class
		for(Map.Entry<Itemset,List<Itemset>> entryEquivalenceClass : closedPatternsAndGenerators.mapGenerators.entrySet()){
			// get the list of generators
			List<Itemset> listGenerators = entryEquivalenceClass.getValue();
			// if the equivalence class has no generator, then its closed itemset is a generator...
			if(listGenerators.size() == 0  && entryEquivalenceClass.getKey().size() !=0){
				listGenerators.add(entryEquivalenceClass.getKey());
			}
			
			// loop over the generators g of the equivalence class
			for(Itemset generatorG : listGenerators){
				// 3 - find proper supersets of G among the frequent closed itemsets
				Set<Itemset> supersets = new HashSet<Itemset>();
				for(Itemset closedItemset : closedPatternsAndGenerators.mapGenerators.keySet()){
					if(generatorG.size() < closedItemset.size() 
							&& closedItemset.containsAll(generatorG)){
						supersets.add(closedItemset);
					}
				}
				
				// 6 - loop over the supersets found
				for(Itemset closedItemset : supersets){
					Itemset leftSide = generatorG;
					Itemset rightSide = closedItemset.cloneItemSetMinusAnItemset(generatorG);
					calculateSupport(rightSide);
					// left.support = g.support;
					
					double conf = ((double)closedItemset.getAbsoluteSupport()) / ((double)generatorG.getAbsoluteSupport());
//										
					if(conf >= minconf){
						saveRule(leftSide, rightSide, closedItemset.getAbsoluteSupport(), conf);
					}
				}
			}
		}
		endTimeStamp = System.currentTimeMillis();
		
		// if we saved to a file, we need to close it.
		if(writer != null){
			writer.close();
		}
		
		return rules;
	}

	/**
	 * Save a rule to file or to memory (if no output file path is provided)
	 * @param itemset1 the left side of the rule
	 * @param itemset2 the right side of the rule
	 * @param absoluteSupport the support of the rule
	 * @param confidence the confidence of the rule
	 * @throws IOException exception if there is an error writing the output file.
	 */
	private void saveRule(Itemset itemset1, Itemset itemset2,
			int absoluteSupport, double confidence) throws IOException {
		// increase the number of rules found
		ruleCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			StringBuffer buffer = new StringBuffer();
			
			// WRITE LEFT SIDE OF THE RULE
			// If the left side is empty we write ____
			if(itemset1.size() == 0){
				buffer.append("__");
			}
			else{
				// if the left side is not empty, a loop will save
				// each item one by one, separated by spaces
				for (int i = 0; i < itemset1.size(); i++) {
					buffer.append(itemset1.get(i));
					if (i != itemset1.size() - 1) {
						buffer.append(" ");
					}
				}
			}
			
			// write separator
			buffer.append(" ==> ");
			
			// write the right side of the rule
			for (int i = 0; i < itemset2.size(); i++) {
				buffer.append(itemset2.get(i));
				if (i != itemset2.size() - 1) {
					buffer.append(" ");
				}
			}
			
			// write the support
			buffer.append(" #SUP: ");
			// write the support as an integer
			buffer.append(absoluteSupport);
			// write the confidence
			buffer.append(" #CONF: ");
			buffer.append(doubleToString(confidence));
			
			writer.write(buffer.toString());
			writer.newLine(); // write new line
		}
		else{ // otherwise the result is kept into memory
			rules.addRule(new Rule(itemset1.getItems(), itemset2.getItems(), itemset1.getAbsoluteSupport(), 
					absoluteSupport, confidence));
		}
	}

	/**
	 * Calculate the support of a given itemset.
	 * @param itemsetToTest a given itemset (Itemset).
	 */
	private void calculateSupport(Itemset itemsetToTest) {  // THIS WAS CHANGED
		// check if closed
		for(List<Itemset> list : closedPatternsAndGenerators.levels){
			if(list.size() == 0  || list.get(0).size() < itemsetToTest.size()){
				continue; // it is not useful to consider itemsets that are smaller  
				          // than itemsetToTest.size
			}
			for(Itemset itemset : list){
				if(itemset.containsAll(itemsetToTest)){
					itemsetToTest.setAbsoluteSupport(itemset.getAbsoluteSupport());
					return;
				}
			}
		}
	}
	
	/**
	 * Convert a double value to a string with only five decimal
	 * @param value  a double value
	 * @return a string
	 */
	private String doubleToString(double value) {
		// convert it to a string with two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(value);
	}
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStatistics() {
		System.out
				.println("============= IGB ASSOCIATION RULE GENERATION - STATS =============");
		System.out.println(" Number of association rules generated : "
				+ ruleCount);
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}
}
