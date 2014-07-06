package ca.pfv.spmf.algorithms.associationrules.IGB;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.algorithms.frequentpatterns.zart.TZTableClosed;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.rule_itemset_array_integer_with_count.Rule;
import ca.pfv.spmf.patterns.rule_itemset_array_integer_with_count.Rules;

/**
 * This is an implementation of the GEN-IGB-FERMES algorithm for mining
 * the IGB basis of association rules. 
 * <br/><br/>
 * 
 * This algorithm is described in the article (in French) : 
 * "IGB : une nouvelle base g�n�rique informative des r�gles d�association" 
 * dans Information-Interaction-Intelligence (Revue I3), vol. 6, n� 1, C�padu�s-�ditions, pp. 31-67, octobre 2006 
 * <br/><br/>
 * 
 * This algorithm generates the IGB basis of association rules from the set of frequent closed itemsets,
 * their support and their associated minimal generators.
 * <br/><br/>
 * 
 *  This algorithm can save the result to a file or keep it into memory
 * if the user provides a null output file path to the runAlgorithm() method.
 *
 * @author Philippe Fournier-Viger
 */

public class AlgoIGB {
	// closed itemsets and their corresponding generators
	private TZTableClosed closedPatternsAndGenerators;
	
	private double minconf; // minimum confidence
	
	private int databaseSize; // number of transactions in the original database
	
	// the rules found by IGB
	private Rules rules;
	
	// for statistics
	long startTimestamp = 0; // last execution start time
	long endTimeStamp = 0;   // last execution end time
	private int ruleCount; // the number of rules found
	
	// object to write the output file if the user wish to write to a file
	BufferedWriter writer = null;
	
	public AlgoIGB(){
		
	}

	/**
	 * Run the algorithm.
	 * @param closedPatternsAndGenerators  Closed itemsets and their associated generators.
	 * @param databaseSize the number of transactions in the transaction database.
	 * @param outputFilePath the output file path, if the results should be saved to a file.
	 *                       if null, the result are saved in memory and returned by this method.
	 *                       
	 * @return if the user chose to save to memory, this methods return the set of IGB association rules,
	 *         otherwise, the result is saved to the output file chosen by the user.
	 * @throws IOException
	 */
	public Rules runAlgorithm(TZTableClosed closedPatternsAndGenerators, int databaseSize, double minconf, String outputFilePath) throws IOException {
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			rules =  new Rules("IGB ASSOCIATION RULES");
	    }else{ 
	    	// if the user want to save the result to a file
	    	rules = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		startTimestamp = System.currentTimeMillis();
		
		// parameters
		this.minconf = minconf;
		this.closedPatternsAndGenerators = closedPatternsAndGenerators;
		this.databaseSize = databaseSize;
		
		// initialize variable to count the number of rules found
		ruleCount = 0;
		
		// line 3 of the pseudo code in the IGB paper:
		// For each closed frequent itemset t.
		for(List<Itemset> level : closedPatternsAndGenerators.levels){
			// for each itemset
			for(Itemset itemset : level){
				// if it is not the empty set
				if(itemset.size() != 0){
					// we will process this itemset
					processItemset(itemset);
				}
			}
		}
		
		// record the end time
		endTimeStamp = System.currentTimeMillis();
		
		// if the user chose to save to a file, we close the file.
		if(writer != null){
			writer.close();
		}
		
		return rules;
	}

	/**
	 * Process an itemset to generate rules.
	 * @param i an itemset.
	 * @throws IOException  exception if error while writing output file
	 */
	private void processItemset(Itemset i) throws IOException {
		// If the itemset has enough confidence
		if(i.getRelativeSupport(databaseSize) >= minconf){  // line 3
			// we generate a rule with an empty antecedent
//			Rule rule = new Rule(, i, i.getAbsoluteSupport(), i.getRelativeSupport(databaseSize)); // 4,5,6
			save(new Itemset(), i, i.getAbsoluteSupport(), i.getRelativeSupport(databaseSize));
			return;
		}
	
		// Line 9 of the paper
		// Create an empty set to store the smallest premises
		Set<Itemset> lSmallestPremise = new HashSet<Itemset>(); 
		
		// line 10  of the pseudo code in the IGB paper
		
		// For each closed itemsets, starting from size j=0 to the maximum size
		for(int j=0; j < i.size(); j++){
			for(Itemset i1 : closedPatternsAndGenerators.levels.get(j)){
				
				// if the confidence of I1 ==> I / I1 is higher than minconf
				// and that   I1 \included_in I then:
				if(((double)i.getAbsoluteSupport() / (double)i1.getAbsoluteSupport()) >= minconf 
						&& i.containsAll(i1)){ 
					
					// line 11  of the pseudo code in the IGB paper
					
					// For each generator genI1  of  I1:
					for(Itemset genI1 : closedPatternsAndGenerators.mapGenerators.get(i1)){
						
						// line 12  of the pseudo code in the IGB paper
						
						// check if there is a premise smaller than gen1 already found
						boolean thereIsSmaller = false;
						// for each premise:
						for(Itemset l : lSmallestPremise){
							// If strictly genI1 contains L.
							if(genI1.containsAll(l) && genI1.size() != l.size()){ 
								// remember that genI1 is not the smallest
								thereIsSmaller = true; 
								break;
							}
						}
						
						// if genI1 has no smaller premise
						if(thereIsSmaller ==  false){
							// Add genI1 to the set of smallest premises
							lSmallestPremise.add(genI1);//13
						}
					}
				}
			}
		}
		// line 14 of the pseudo code
		// For each smallest premise found in the previous step
		for(Itemset gs : lSmallestPremise){
			// lines 15, 16, 17  of the pseudo code in the IGB paper
			
			// Finds all items from I that are not in GS
			List<Integer> list_i_gs = new ArrayList<Integer>();
			for(Integer item : i.itemset){
				if(!gs.contains(item)){
					list_i_gs.add(item);
				}
			}
			// Ugly conversion  (we have to do that because Java cannot convert
			// from List<Integer> to int[]...
			int[] temp = new int[list_i_gs.size()];
			for(int k=0; k< list_i_gs.size(); k++){
				temp[k] = list_i_gs.get(k);
			}
			
			// We create the corresponding rule   gs ==> I / gs
			Itemset i_gs = new Itemset(temp);
			// We save the rule
			save(gs, i_gs, i.getAbsoluteSupport(), (double)i.getAbsoluteSupport() / (double)gs.getAbsoluteSupport()); 
		}
	}
	
	/**
	 * Save a rule to memory or file depending on what the user chose
	 * @param itemset1 the left side of the rule
	 * @param itemset2  the right side of the rule
	 * @param absoluteSupport the rule support
	 * @param confidence  the confidence of the rule
	 * @throws IOException if error occurs while writing output to file.
	 */
	private void save(Itemset itemset1, Itemset itemset2, int absoluteSupport, double confidence) throws IOException {
		// increase the number of rule found
		ruleCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			StringBuffer buffer = new StringBuffer();
			// write itemset 1
			if(itemset1.size() == 0){
				buffer.append("__");
			}
			else{
				for (int i = 0; i < itemset1.size(); i++) {
					buffer.append(itemset1.get(i));
					if (i != itemset1.size() - 1) {
						buffer.append(" ");
					}
				}
			}
			// write separator
			buffer.append(" ==> ");
			// write itemset 2
			for (int i = 0; i < itemset2.size(); i++) {
				buffer.append(itemset2.get(i));
				if (i != itemset2.size() - 1) {
					buffer.append(" ");
				}
			}
			// write separator
			buffer.append(" #SUP: ");
			// write support
			buffer.append(absoluteSupport);
			// write separator
			buffer.append(" #CONF: ");
			// write confidence
			buffer.append(doubleToString(confidence));
			writer.write(buffer.toString());
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			Rule rule = new Rule(itemset1.getItems(), itemset2.getItems(), itemset1.support, absoluteSupport, confidence);
			rules.addRule(rule);
		}
	}
	
	/**
	 * Convert a double value to a string with only five decimals
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
