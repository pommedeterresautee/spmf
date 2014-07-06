package ca.pfv.spmf.algorithms.frequentpatterns.two_phase;
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.tools.MemoryLogger;


/**
 * This is an implementation of the "Two-Phase Algorithm" for High-Utility Itemsets Mining.
 * Two-Phase is described in the conference paper : <br/><br/>
 * 
 *  Liu, Y., Liao, W.-K., Choudhary, A. (2005) A Two-Phase Algorithm for Fast-Discovery of 
 *  High Utility Itemsets, Proceedings of PAKDD 2005, pp. 689-695.<br/><br/>
 *  
 *  This implementation uses the Apriori algorithm as it seems to be suggested by the article, even if
 *  the Apriori algorithm is not mentionned explicitly in the article.
 *
 * @see ItemsetsTP
 * @see ItemsetTP
 * @see TransactionTP
 * @see UtilityTransactionDatabase
 * @author Philippe Fournier-Viger
 */
public class AlgoTwoPhase {

	// the set of high utility itemsets found by the algorithm
	private ItemsetsTP highUtilityItemsets = null;
	// the database
	protected UtilityTransactionDatabaseTP database;
	
	// the min utility threshold
	int minUtility;

	// for statistics
	long startTimestamp = 0;  // start time
	long endTimestamp = 0; // end time
	private int candidatesCount; // the number of candidates generated
	
	/**
	 * Default constructor
	 */
	public AlgoTwoPhase() {
	}

	/**
	 * Run the Two-phase algorithm
	 * @param database  a transaction database containing utility information.
	 * @param minUtility the min utility threshold
	 * @return the set of high utility itemsets
	 */
	public ItemsetsTP runAlgorithm(UtilityTransactionDatabaseTP database, int minUtility) {
		// save the parameters
		this.database = database;
		this.minUtility = minUtility;
		
		// reset the utility to check the memory usage
		MemoryLogger.getInstance().reset();
		// record start time
		startTimestamp = System.currentTimeMillis();

		// initialize the set of HUIs (high utility itemsets)
		highUtilityItemsets = new ItemsetsTP("HIGH UTILITY ITEMSETS");
		// reset HUI count
		candidatesCount =0;
		
		// ===================  PHASE 1: GENERATE CANDIDATES  =================== 
		
		// First, we create the level of candidate itemsets of size 1
		List<ItemsetTP> candidatesSize1 = new ArrayList<ItemsetTP>();
		
		// Scan database one time to get the tidset of each item and its utility for the whole database
		// Map to store the tidset of each item
		// key: item   value: tidset as a set of integers
		Map<Integer, Set<Integer>> mapItemTidsets = new HashMap<Integer, Set<Integer>>();
		// Map to store the TWU of each item  (key: item , value: TWU)
		Map<Integer, Integer> mapItemTWU = new HashMap<Integer, Integer>();
		// variable to remember the maximum item ID
		int maxItem = Integer.MIN_VALUE;
		
		// for each line (transaction) in the database
		for(int i=0; i< database.size(); i++){
			// get the transaction
			TransactionTP transaction = database.getTransactions().get(i);
			
			// for each item in the current transaction
			for(Integer item : transaction.getItems()){
				// if this is the largest item until now, remember it
				if(item > maxItem){
					maxItem = item;
				}
				// Add the tid of this transaction to the tidset of the item
				Set<Integer> tidset = mapItemTidsets.get(item);
				if(tidset == null){
					tidset = new HashSet<Integer>();
					mapItemTidsets.put(item, tidset);
				}
				tidset.add(i);
				
				// Add transaction utility for this item to its TWU
				Integer sumUtility = mapItemTWU.get(item);
				if(sumUtility == null){  // if no utility yet
					sumUtility = 0;
				}
				sumUtility += transaction.getTransactionUtility(); // add the utility
				mapItemTWU.put(item, sumUtility);
			}
		}
		
		// Create a candidate itemset for each item having a TWU  >= minUtil
		// For each item
		for(int item=0; item<= maxItem; item++){
			// Get the twu of the item
			Integer estimatedUtility = mapItemTWU.get(item);
			// if it is a HWTUI itemset (see formal definition in paper)
			if(estimatedUtility != null && estimatedUtility >= minUtility){
				// Create the itemset with this item and set its tidset
				ItemsetTP itemset = new ItemsetTP();
				itemset.addItem(item);
				itemset.setTIDset(mapItemTidsets.get(item));
				// add it to candidates
				candidatesSize1.add(itemset);
				// add it to the set of HUIs
				highUtilityItemsets.addItemset(itemset, itemset.size());
			}
		}

		// From candidate of size 1, we recursively create candidates of greater size
		// until no candidates can be generated
		List<ItemsetTP> currentLevel = candidatesSize1;
		while (true) {
			// Generate candidates of size K+1
			int candidateCount = highUtilityItemsets.getItemsetsCount();
			currentLevel = generateCandidateSizeK(currentLevel, highUtilityItemsets);
			// if no new candidates are found, then we stop because no more candidates will be found.
			if(candidateCount == highUtilityItemsets.getItemsetsCount()){
				break;
			}
		}
		// the Phase 1 of the algorithm is now completed!

		// check memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// update the number of candidates generated until now
		candidatesCount = highUtilityItemsets.getItemsetsCount();
		
		// ========================  PHASE 2: Calculate exact utility of each candidate =============
		// for each level of HWTUIs found in phase 1
		for(List<ItemsetTP> level : highUtilityItemsets.getLevels()){
			// for each HWTUIs in that level
			Iterator<ItemsetTP> iterItemset = level.iterator();
			while(iterItemset.hasNext()){
				// this is the current HWTUI
				ItemsetTP candidate = iterItemset.next();
				
				// Calculate exact utility of that HTWUI by scanning transactions of its tidset
				// For each transaction
				for(TransactionTP transaction : database.getTransactions()){
					// variable to store the transaction utility of "candidate" for the current transaction
					int transactionUtility =0;
					// the number of items from "candidate" appearing in this transaction
					int matchesCount =0; 
					// for each item of the transaction
					for(int i=0; i< transaction.size(); i++){
						// if it appears in "candidate"
						if(candidate.getItems().contains(transaction.get(i))){
							// add the transaction utility
							transactionUtility += transaction.getItemsUtilities().get(i);
							matchesCount++; // increase the number of matches
						}
					}
					// if the numer of matches is the size of "candidate", it means
					// that it appears completely in the transaction,
					// so we add the transaction utility of "candidate" to its utility.
					if(matchesCount == candidate.size()){
						candidate.incrementUtility(transactionUtility);
					}
				}
				// finally, after scanning all transactions for "candidate", we have its
				// real utility value.
				// if lower than min-utility it is not a HUI so:
				if(candidate.getUtility() < minUtility){
					iterItemset.remove(); // delete it
					highUtilityItemsets.decreaseCount();  // decrease number of itemsets found
				}
				
			}
		}
		
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// record end time
		endTimestamp = System.currentTimeMillis();
		
		// Return all frequent itemsets found!
		return highUtilityItemsets; 
	}


	/**
	 * Generate candidate HWTUI of size K by using HWTUIs of size k-1
	 * @param levelK_1   HWTUIs of size k-1
	 * @param candidatesHTWUI  structure to store the HWTUIs
	 * @return  candidates of size K
	 */
	protected List<ItemsetTP> generateCandidateSizeK(List<ItemsetTP> levelK_1, ItemsetsTP candidatesHTWUI) {
		
	// For each itemset I1 and I2 of level k-1
	loop1:	for(int i=0; i< levelK_1.size(); i++){
				ItemsetTP itemset1 = levelK_1.get(i);
	loop2:		for(int j=i+1; j< levelK_1.size(); j++){
					ItemsetTP itemset2 = levelK_1.get(j);
			
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
				for(int k=0; k< itemset1.size(); k++){
					// if they are the last items
					if(k == itemset1.size()-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1.getItems().get(k) >= itemset2.get(k)){  
							continue loop1;
						}
					}
					// if they are not the last items, and 
					else if(itemset1.getItems().get(k) < itemset2.get(k)){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems().get(k) > itemset2.get(k)){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}
				
				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				Integer missing = itemset2.get(itemset2.size()-1);

				// create list of common tids
				Set<Integer> tidset = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTIDset()){
					if(itemset2.getTIDset().contains(val1)){
						tidset.add(val1);
					}
				}
				
				// Calculate TWU of itemset
				// it is defined as the sum of the transaction utility (TU) for the
				// tidset of the itemset
				int twu =0;
				for(Integer tid : tidset){
					twu += database.getTransactions().get(tid).getTransactionUtility();
				}
		
				// if the transaction weighted utility (TWU) is high enough
				if(twu >= minUtility){
					// Create a new candidate by combining itemset1 and itemset2
					ItemsetTP candidate = new ItemsetTP();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(missing);
					// set its tidset
					candidate.setTIDset(tidset);
					// add it to the set of HWTUI of size K
					candidatesHTWUI.addItemset(candidate, candidate.size());
				}
			}
		}
		// return candidates HTWUIs of size K
		return candidatesHTWUI.getLevels().get(candidatesHTWUI.getLevels().size()-1);
	}

	/**
	 * Print statistics about the latest algorithm execution to System out.
	 */
	public void printStats() {
		System.out
				.println("=============  TWO-PHASE ALGORITHM - STATS =============");
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Candidates count : " + candidatesCount); 
		System.out.println(" High-utility itemsets count : " + highUtilityItemsets.getItemsetsCount()); 
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out
				.println("===================================================");
	}
}