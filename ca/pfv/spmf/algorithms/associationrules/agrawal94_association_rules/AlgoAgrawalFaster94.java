package ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

/**
 * This is an implementation of the "faster algorithm" for generating association rules,
 * described in Agrawal &
 * al. 1994, IBM Research Report RJ9839, June 1994.
 * <br/><br/>
 * 
 * This implementation saves the result to a file
 * or can alternatively keep it into memory if no output 
 * path is provided by the user when the runAlgorithm()
 * method is called.
 * 
 *  @see   AssocRule
 *  @see   AssocRules
 *  @author Philippe Fournier-Viger
 **/

public class AlgoAgrawalFaster94 {
	
	// the frequent itemsets that will be used to generate the rules
	private Itemsets patterns;
	
	// variable used to store the result if the user choose to save
	// the result in memory rather than to an output file
	protected AssocRules rules;
	
	// object to write the output file if the user wish to write to a file
	protected BufferedWriter writer = null;
	
	// for statistics
	protected long startTimestamp = 0; // last execution start time
	protected long endTimeStamp = 0;   // last execution end time
	protected int ruleCount = 0;  // number of rules generated
	protected int databaseSize = 0; // number of transactions in database
	
	// parameters
	protected double minconf;
	protected double minlift;
	protected boolean usingLift = true;
	
	/**
	 * Default constructor
	 */
	public AlgoAgrawalFaster94(){
		
	}

	/**
	 * Run the algorithm
	 * @param patterns  a set of frequent itemsets
	 * @param output an output file path for writing the result or null if the user want this method to return the result
	 * @param databaseSize  the number of transactions in the database
	 * @param minconf  the minconf threshold
	 * @return  the set of association rules if the user wished to save them into memory
	 * @throws IOException exception if error writing to the output file
	 */
	public AssocRules runAlgorithm(Itemsets patterns, String output, int databaseSize, double minconf) throws IOException {
		// save the parameters
		this.minconf = minconf;
		this.minlift = 0;
		usingLift = false;
		
		// start the algorithm
		return runAlgorithm(patterns, output, databaseSize);
	}

	/**
	 * Run the algorithm
	 * @param patterns  a set of frequent itemsets
	 * @param output an output file path for writing the result or null if the user want this method to return the result
	 * @param databaseSize  the number of transactions in the database
	 * @param minconf  the minconf threshold
	 * @param minlift  the minlift threshold
	 * @return  the set of association rules if the user wished to save them into memory
	 * @throws IOException exception if error writing to the output file
	 */
	public AssocRules runAlgorithm(Itemsets patterns, String output, int databaseSize, double minconf,
			double minlift) throws IOException {
		// save the parameters
		this.minconf = minconf;
		this.minlift = minlift;
		usingLift = true;
		
		// start the algorithm
		return runAlgorithm(patterns, output, databaseSize);
	}

	/**
	 * Run the algorithm for generating association rules from a set of itemsets.
	 * @param patterns the set of itemsets
	 * @param output the output file path. If null the result is saved in memory and returned by the method.
	 * @param databaseSize  the number of transactions in the original database
	 * @return the set of rules found if the user chose to save the result to memory
	 * @throws IOException exception if error while writting to file
	 */
	private AssocRules runAlgorithm(Itemsets patterns, String output, int databaseSize)
			throws IOException {
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			rules =  new AssocRules("ASSOCIATION RULES");
	    }else{ 
	    	// if the user want to save the result to a file
	    	rules = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}

		this.databaseSize = databaseSize;
		
		// record the time when the algorithm starts
		startTimestamp = System.currentTimeMillis();
		// initialize variable to count the number of rules found
		ruleCount = 0;
		// save itemsets in a member variable
		this.patterns = patterns;
		
		// SORTING
		// First, we sort all itemsets having the same size by lexical order
		// We do this for optimization purposes. If the itemsets are sorted, it allows to
		// perform two optimizations:
		// 1) When we need to calculate the support of an itemset (in the method
		// "calculateSupport()") we can use a binary search instead of browsing the whole list.
		// 2) When combining itemsets to generate candidate, we can use the
		//    lexical order to avoid comparisons (in the method "generateCandidates()").
		
		// For itemsets of the same size
		for(List<Itemset> itemsetsSameSize : patterns.getLevels()){
			// Sort by lexicographical order using a Comparator
			Collections.sort(itemsetsSameSize, new Comparator<Itemset>() {
				@Override
				public int compare(Itemset o1, Itemset o2) {
					// The following code assume that itemsets are the same size
					return ArraysAlgos.comparatorItemsetSameSize.compare(o1.getItems(), o2.getItems());
				}
			});
		}
		// END OF SORTING
		
		// Now we will generate the rules.
		
		// For each frequent itemset of size >=2 that we will name "lk"
		for (int k = 2; k < patterns.getLevels().size(); k++) {
			for (Itemset lk : patterns.getLevels().get(k)) {
				
				// create a variable H1 for recursion
				List<int[]> H1_for_recursion = new ArrayList<int[]>();
				
				// For each itemset "itemsetSize1" of size 1 that is member of lk
				for(int item : lk.getItems()) {
					int itemsetHm_P_1[] = new int[] {item};
	
					// make a copy of  lk without items from  hm_P_1
					int[] itemset_Lk_minus_hm_P_1 = ArraysAlgos.cloneItemSetMinusOneItem(lk.getItems(), item);

					// Now we will calculate the support and confidence
					// of the rule: itemset_Lk_minus_hm_P_1 ==>  hm_P_1
					int support = calculateSupport(itemset_Lk_minus_hm_P_1); // THIS COULD BE
																// OPTIMIZED ?
					double supportAsDouble = (double) support;
					
					// calculate the confidence of the rule : itemset_Lk_minus_hm_P_1 ==>  hm_P_1
					double conf = lk.getAbsoluteSupport() / supportAsDouble;

					// if the confidence is lower than minconf
					if(conf < minconf || Double.isInfinite(conf)){
						continue;
					}
					
					double lift = 0;
					int supportHm_P_1 = 0;
					// if the user is using the minlift threshold, we will need
					// to also calculate the lift of the rule:  itemset_Lk_minus_hm_P_1 ==>  hm_P_1
					if(usingLift){
						// if we want to calculate the lift, we need the support of hm_P_1
						supportHm_P_1 = calculateSupport(itemsetHm_P_1);  // if we want to calculate the lift, we need to add this.
						// calculate the lift
						double term1 = ((double)lk.getAbsoluteSupport()) /databaseSize;
						double term2 = supportAsDouble /databaseSize;
						double term3 = ((double)supportHm_P_1 / databaseSize);
						lift = term1 / (term2 * term3);
						
						// if the lift is not enough
						if(lift < minlift){
							continue;
						}
					}
					
					// If we are here, it means that the rule respect the minconf and minlift parameters.
					// Therefore, we output the rule.
					saveRule(itemset_Lk_minus_hm_P_1, support, itemsetHm_P_1, supportHm_P_1, lk.getAbsoluteSupport(), conf, lift);
					
					// Then we keep the itemset  hm_P_1 to find more rules using this itemset and lk.
					H1_for_recursion.add(itemsetHm_P_1);
					// ================ END OF WHAT I HAVE ADDED
				}
				// Finally, we make a recursive call to continue explores rules that can be made with "lk"
				apGenrules(k, 1, lk, H1_for_recursion);
			}
			
		}

		// close the file if we saved the result to a file
		if(writer != null){
			writer.close();
		}
		// record the end time of the algorithm execution
		endTimeStamp = System.currentTimeMillis();
		
		// Return the rules found if the user chose to save the result to memory rather than a file.
		// Otherwise, null will be returned
		return rules;
	}

	/**
	 * The ApGenRules as described in p.14 of the paper by Agrawal.
	 * (see the Agrawal paper for more details).
	 * @param k the size of the first itemset used to generate rules
	 * @param m the recursive depth of the call to this method (first time 1, then 2...)
	 * @param lk the itemset that is used to generate rules
	 * @param Hm a set of itemsets that can be used with lk to generate rules
	 * @throws IOException exception if error while writing output file
	 */
	private void apGenrules(int k, int m, Itemset lk, List<int[]> Hm)
			throws IOException {
		
		// if the itemset "lk" that is used to generate rules is larger than the size of itemsets in "Hm"
		if (k > m + 1) {
			// Create a list that we will be used to store itemsets for the recursive call
			List<int[]> Hm_plus_1_for_recursion = new ArrayList<int[]>();
			
			// generate candidates using Hm
			List<int[]> Hm_plus_1 = generateCandidateSizeK(Hm);
			
			// for each such candidates
			for (int[] hm_P_1 : Hm_plus_1) {
				
				// We subtract the candidate from the itemset "lk"
				int[] itemset_Lk_minus_hm_P_1 =  ArraysAlgos.cloneItemSetMinusAnItemset(lk.getItems(), hm_P_1);

				// We will now calculate the support of the rule  Lk/(hm_P_1) ==> hm_P_1
				// we need it to calculate the confidence
				int support = calculateSupport(itemset_Lk_minus_hm_P_1); 
				
				double supportAsDouble = (double)support;
				
				// calculate the confidence of the rule Lk/(hm_P_1) ==> hm_P_1
				double conf = lk.getAbsoluteSupport() / supportAsDouble;

				// if the confidence is not enough than we don't need to consider
				// the rule  Lk/(hm_P_1) ==> hm_P_1 anymore so we continue 
				if(conf < minconf || Double.isInfinite(conf)){
					continue;
				}
				
				double lift = 0;
				int supportHm_P_1 = 0;
				// if the user is using the minlift threshold, then we will need to calculate the lift of the
				// rule as well and check if the lift is higher or equal to minlift.
				if(usingLift){
					// if we want to calculate the lift, we need the support of Hm+1
					supportHm_P_1 = calculateSupport(hm_P_1);  
					// calculate the lift of the rule:  Lk/(hm_P_1) ==> hm_P_1
					double term1 = ((double)lk.getAbsoluteSupport()) /databaseSize;
					double term2 = (supportAsDouble) /databaseSize;
					
					 lift = term1 / (term2 * ((double)supportHm_P_1 / databaseSize));

					// if the lift is not enough
					if(lift < minlift){
						continue;
					}
				}
				
				// The rule has passed the confidence and lift threshold requirements,
				// so we can output it
				saveRule(itemset_Lk_minus_hm_P_1, support, hm_P_1, supportHm_P_1, lk.getAbsoluteSupport(), conf, lift);
				
				// if k == m+1, then we cannot explore further rules using Lk since Lk will be too small.
				if(k != m+1) {
					Hm_plus_1_for_recursion.add(hm_P_1);
				}
			}
			// recursive call to apGenRules to find more rules using "lk"
			apGenrules(k, m + 1, lk, Hm_plus_1_for_recursion);
		}
	}
	
	/**
	 * Calculate the support of an itemset by looking at the frequent patterns
	 * of the same size.
	 * Because patterns are sorted by lexical order, we use a binary search.
	 * This is MUCH MORE efficient than just browsing the full list of patterns.
	 * 
	 * @param itemset the itemset.
	 * @return the support of the itemset
	 */
	private int calculateSupport(int[] itemset) {
		// We first get the list of patterns having the same size as "itemset"
		List<Itemset> patternsSameSize = patterns.getLevels().get(itemset.length);
//		
		// We perform a binary search to find the position of itemset in this list
        int first = 0;
        int last = patternsSameSize.size() - 1;
       
        while( first <= last )
        {
        	int middle = ( first + last ) >>1 ; // >>1 means to divide by 2
        	int[] itemsetMiddle = patternsSameSize.get(middle).getItems();

        	int comparison = ArraysAlgos.comparatorItemsetSameSize.compare(itemset, itemsetMiddle);
            if(comparison  > 0 ){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(comparison  < 0 ){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	// we have found the itemset, so we return its support.
            	return patternsSameSize.get(middle).getAbsoluteSupport();
            }
        }
        // The following line will not happen because in the context of this algorithm, we will
        // always search for itemsets that are frequent and thus will be in the list of patterns.
        // We just put the following line to avoid compilation error and detect if the error if this
        // case was ever to happen.
        throw new RuntimeException("INVALID SUPPORT - THIS SHOULD NOT HAPPEN BECAUSE ALL ITEMSETS HAVE TO BE FREQUENT");
	}

	/**
	 * Generating candidate itemsets of size k from frequent itemsets of size
	 * k-1. This is called "apriori-gen" in the paper by agrawal. This method is
	 * also used by the Apriori algorithm for generating candidates.
	 * Note that this method is very optimized. It assumed that the list of
	 * itemsets received as parameter are lexically ordered.
	 * 
	 * @param levelK_1  a set of itemsets of size k-1
	 * @return a set of candidates
	 */
	protected List<int[]> generateCandidateSizeK(List<int[]> levelK_1) {
		// create a variable to store candidates
		List<int[]> candidates = new ArrayList<int[]>();

		// For each itemset I1 and I2 of level k-1
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			int[] itemset1 = levelK_1.get(i);
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				int[] itemset2 = levelK_1.get(j);

				// we compare items of itemset1 and itemset2.
				// If they have all the same k-1 items and the last item of
				// itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a
				// candidate
				for (int k = 0; k < itemset1.length; k++) {
					// if they are the last items
					if (k == itemset1.length - 1) {
						// the one from itemset1 should be smaller (lexical
						// order)
						// and different from the one of itemset2
						if (itemset1[k] >= itemset2[k]) {
							continue loop1;
						}
					}
					// if they are not the last items, and
					else if (itemset1[k] < itemset2[k]) {
						continue loop2; // we continue searching
					} else if (itemset1[k] > itemset2[k]) {
						continue loop1; // we stop searching: because of lexical
										// order
					}
				}

				// Create a new candidate by combining itemset1 and itemset2
				int lastItem1 =  itemset1[itemset1.length -1];
				int lastItem2 =  itemset2[itemset2.length -1];
				int newItemset[];
				if(lastItem1 < lastItem2) {
					// Create a new candidate by combining itemset1 and itemset2
					newItemset = new int[itemset1.length+1];
					System.arraycopy(itemset1, 0, newItemset, 0, itemset1.length);
					newItemset[itemset1.length] = lastItem2;
					candidates.add(newItemset);
				}else {
					// Create a new candidate by combining itemset1 and itemset2
					newItemset  = new int[itemset1.length+1];
					System.arraycopy(itemset2, 0, newItemset, 0, itemset2.length);
					newItemset[itemset2.length] = lastItem1;
					candidates.add(newItemset);
				}

			}
		}
		// return the set of candidates
		return candidates; 
	}



	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  ASSOCIATION RULE GENERATION v0.96f- STATS =============");
		System.out.println(" Number of association rules generated : " + ruleCount);
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}
	
	/**
	 * Save a rule to the output file or in memory depending
	 * if the user has provided an output file path or not
	 * @param itemset1  left itemset of the rule
	 * @param supportItemset1 the support of itemset1 if known
	 * @param itemset2  right itemset of the rule
	 * @param supportItemset2 the support of itemset2 if known
	 * @param absoluteSupport support of the rule
	 * @param conf confidence of the rule
	 * @param lift lift of the rule
	 * @throws IOException exception if error writing the output file
	 */
	protected void saveRule(int[] itemset1, int supportItemset1, int[] itemset2, int supportItemset2,
			int absoluteSupport, double conf, double lift) throws IOException {
		ruleCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			StringBuffer buffer = new StringBuffer();
			// write itemset 1
			for (int i = 0; i < itemset1.length; i++) {
				buffer.append(itemset1[i]);
				if (i != itemset1.length - 1) {
					buffer.append(" ");
				}
			}
			// write separator
			buffer.append(" ==> ");
			// write itemset 2
			for (int i = 0; i < itemset2.length; i++) {
				buffer.append(itemset2[i]);
				if (i != itemset2.length - 1) {
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
			buffer.append(doubleToString(conf));
			if(usingLift){
				buffer.append(" #LIFT: ");
				buffer.append(doubleToString(lift));
			}
			
			writer.write(buffer.toString());
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			rules.addRule(new AssocRule(itemset1, itemset2, supportItemset1, absoluteSupport, conf, lift));
		}
	}

	
	/**
	 * Convert a double value to a string with only five decimal
	 * @param value  a double value
	 * @return a string
	 */
	String doubleToString(double value) {
		// convert it to a string with two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(value);
	}

}
