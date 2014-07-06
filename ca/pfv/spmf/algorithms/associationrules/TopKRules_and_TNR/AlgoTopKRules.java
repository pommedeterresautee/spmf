package ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR;

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
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * TopKRules is an algorithm for mining the TOP-K  association rules from a 
 * transaction database using
 * a pattern growth approach and several optimizations. This is the original
 * implementation as proposed in the following paper:
 * <br/><br/>
 * 
 * Fournier-Viger, P., Wu, C.-W., Tseng, V. S. (2012). Mining Top-K Association Rules. Proceedings of the 25th Canadian Conf. on Artificial Intelligence (AI 2012), Springer, LNAI 7310, pp. 61-73.
 * 
 * @author Philippe Fournier-Viger, 2012
 */
public class AlgoTopKRules {
	
	// for statistics
	long timeStart = 0;  // start time of last execution
	long timeEnd = 0;  // end time of last execution

	// parameters
	double minConfidence; // minconf threshold
	int k = 0;            // variable k
	Database database;   // a transaction database

	// minimum support that will be reased during the search
	int minsuppRelative;
	
	// a vertical representation of the database
	BitSet[] tableItemTids; // [item], IDs of transaction containing the item
	// a table indicating the support of each item
	int[] tableItemCount; // [item], support
	
	PriorityQueue<RuleG> kRules; // the top k rules found until now
	RedBlackTree<RuleG> candidates; // the candidates for expansion

	// the maximum number of candidates at the same time during the last execution
	int maxCandidateCount = 0;

	/**
	 * Default constructor
	 */
	public AlgoTopKRules() {
	}

	/**
	 * Run the algorithm.
	 * @param k the value of k.
	 * @param minConfidence the minimum confidence threshold.
	 * @param database the database.
	 */
	public void runAlgorithm(int k, double minConfidence, Database database) {
		// reset statistics
		MemoryLogger.getInstance().reset(); // reset utility to check memory usage
		maxCandidateCount = 0;
		
		// save parameters
		this.minConfidence = minConfidence;
		this.database = database;
		this.k = k;

		// prepare internal variables and structures
		this.minsuppRelative = 1;
		tableItemTids = new BitSet[database.maxItem + 1]; // id item, count
		tableItemCount = new int[database.maxItem + 1];
		kRules = new PriorityQueue<RuleG>();
		candidates = new RedBlackTree<RuleG>();

		// record the start time
		timeStart = System.currentTimeMillis(); 
		
		// perform the first database scan to generate vertical database representation
		scanDatabase(database);
		
		// start the generation of rules
		start();
		
		// record the end time
		timeEnd = System.currentTimeMillis(); 
	}

	/**
	 * Start the rule generation.
	 */
	private void start() {
		// We will now try to generate rules with one item in the
		// antecedent and one item in the consequent using
		// frequent items.
		
		// for each item I in the database
		main: for (int itemI = 0; itemI <= database.maxItem; itemI++) {
			// if the item is not frequent according to the current
			// minsup threshold, then skip it
			if (tableItemCount[itemI] < minsuppRelative) {
				continue main;
			}
			// Get the bitset corresponding to item I
			BitSet tidsI = tableItemTids[itemI];

			// for each item J in the database
			main2: for (int itemJ = itemI + 1; itemJ <= database.maxItem; itemJ++) {
				// if the item is not frequent according to the current
				// minsup threshold, then skip it
				if (tableItemCount[itemJ] < minsuppRelative) {
					continue main2;
				}
				// Get the bitset corresponding to item J
				BitSet tidsJ = tableItemTids[itemJ];

				// Calculate the list of transaction IDs shared
				// by I and J.
				// To do that with a bitset, we just do a logical AND.
				BitSet commonTids = (BitSet) tidsI.clone();
				commonTids.and(tidsJ);
				// We keep the cardinality of the new bitset because in java
				// the cardinality() method is expensive, and we will need it again later.
				int support = commonTids.cardinality();
				
				// If the rules I ==> J and J ==> I have enough support
				if (support >= minsuppRelative) {
					// generate  rules I ==> J and J ==> I and remember these rules
					// for future possible expansions
					generateRuleSize11(itemI, tidsI, itemJ, tidsJ, commonTids,
							support);
				}
			}
		}
	
		// Now we have finished checking all the rules containing 1 item
		// in the left side and 1 in the right side,
		// the next step is to recursively expand rules in the set 
		// "candidates" to find more rules.
		while (candidates.size() > 0) {
			// We take the rule that has the highest support first
			RuleG rule = candidates.popMaximum();
			// if there is no more candidates with enough support, then we stop
			if (rule.getAbsoluteSupport() < minsuppRelative) {
				// candidates.remove(rule);
				break;
			}
			// Otherwise, we try to expand the rule
			if (rule.expandLR) {
				// we do it
				expandLR(rule);
			} else {
				// If the rule should only be expanded by left side to
				// avoid generating redundant rules, then we 
				// only expand the left side.
				expandR(rule);
			}
			// candidates.remove(rule);
		}
	}

	/**
	 * This method test the rules I ==> J and J ==> I  for their confidence
	 * and record them for future expansions.
	 * @param itemI an item I
	 * @param tidI  the set of IDs of transaction containing  item I (BitSet)
	 * @param itemJ an item J
	 * @param tidJ  the set of IDs of transaction containing  item J (BitSet)
	 * @param commonTids  the set of IDs of transaction containing I and J (BitSet)
	 * @param cardinality  the cardinality of "commonTids"
	 */
	private void generateRuleSize11(Integer item1, BitSet tid1, Integer item2,
			BitSet tid2, BitSet commonTids, int cardinality) {
		// Create the rule I ==> J
		Integer[] itemset1 = new Integer[1];
		itemset1[0] = item1;
		Integer[] itemset2 = new Integer[1];
		itemset2[0] = item2;
		RuleG ruleLR = new RuleG(itemset1, itemset2, cardinality, tid1,
				commonTids, item1, item2);
		
		// calculate the confidence
		double confidenceIJ = ((double) cardinality) / (tableItemCount[item1]);
		
		// if rule i->j has minimum confidence
		if (confidenceIJ >= minConfidence) {
			// save the rule in current top-k rules
			save(ruleLR, cardinality);
		}
		// register the rule as a candidate for future expansion
		registerAsCandidate(true, ruleLR);

		// calculate the confidence
		double confidenceJI = ((double) cardinality) / (tableItemCount[item2]);
		
		// Create the rule J ==> I
		RuleG ruleRL = new RuleG(itemset2, itemset1, cardinality, tid2,
				commonTids, item2, item1);
		// if rule J->I has minimum confidence
		if (confidenceJI >= minConfidence) {
			// save the rule in current top-k rules
			save(ruleRL, cardinality);
		}
		// register the rule as a candidate for future expansion
		registerAsCandidate(true, ruleRL);

	}

	/**
	 * Register a given rule in the set of candidates for future expansions
	 * @param expandLR  if true the rule will be considered for left/right 
	 * expansions otherwise only right.
	 * @param rule the given rule
	 */
	private void registerAsCandidate(boolean expandLR, RuleG rule) {
		// add the rule to candidates
		rule.expandLR = expandLR;
		candidates.add(rule);

		// record the maximum number of candidates for statistics
		if (candidates.size() >= maxCandidateCount) {
			maxCandidateCount = candidates.size();
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Try to expand a rule by left and right expansions.
	 * @param ruleG the rule
	 */
	private void expandLR(RuleG ruleG) {
		// Maps to record the potential item to expand the left/right sides of the rule
		// Key: item   Value: bitset indicating the IDs of the transaction containing the item
		// from the transactions containing the rule.
		Map<Integer, BitSet> mapCountLeft = new HashMap<Integer, BitSet>();
		Map<Integer, BitSet> mapCountRight = new HashMap<Integer, BitSet>();

		
		for (int tid = ruleG.common.nextSetBit(0); tid >= 0; tid = ruleG.common
				.nextSetBit(tid + 1)) {
			Iterator<Integer> iter = database.getTransactions().get(tid)
					.getItems().iterator();
			while (iter.hasNext()) {
				Integer item = iter.next();
				// CAN DO THIS BECAUSE TRANSACTIONS ARE SORTED BY DESCENDING
				// ITEM IDS (see Database.Java)
				if (item < ruleG.maxLeft && item < ruleG.maxRight) { //
					break;
				}
				if (tableItemCount[item] < minsuppRelative) {
					iter.remove();
					continue;
				}
				if (item > ruleG.maxLeft
						&& !ArraysAlgos.containsLEX(ruleG.getItemset2(), item,
								ruleG.maxRight)) {
					BitSet tidsItem = mapCountLeft.get(item);
					if (tidsItem == null) {
						tidsItem = new BitSet();
						mapCountLeft.put(item, tidsItem);
					}
					tidsItem.set(tid);
				}
				if (item > ruleG.maxRight
						&& !ArraysAlgos.containsLEX(ruleG.getItemset1(), item,
								ruleG.maxLeft)) {
					BitSet tidsItem = mapCountRight.get(item);
					if (tidsItem == null) {
						tidsItem = new BitSet();
						mapCountRight.put(item, tidsItem);
					}
					tidsItem.set(tid);
				}
			}
		}

		// for each item c found in the previous step, we create a rule	
		// I  ==> J U {c} if the support is enough 	
		for (Entry<Integer, BitSet> entry : mapCountRight.entrySet()) {
			BitSet tidsRule = entry.getValue();
			int ruleSupport = tidsRule.cardinality();

			// if the support is enough
			if (ruleSupport >= minsuppRelative) {
				Integer itemC = entry.getKey();

				// create new right part of rule
				Integer[] newRightItemset = new Integer[ruleG.getItemset2().length + 1];
				System.arraycopy(ruleG.getItemset2(), 0, newRightItemset, 0,
						ruleG.getItemset2().length);
				newRightItemset[ruleG.getItemset2().length] = itemC;

				// recompute maxRight
				int maxRight = (itemC >= ruleG.maxRight) ? itemC
						: ruleG.maxRight;

				// calculate the confidence of the rule
				double confidence = ((double) ruleSupport)
						/ ruleG.tids1.cardinality();
				
				// create the rule
				RuleG candidate = new RuleG(ruleG.getItemset1(),
						newRightItemset, ruleSupport, ruleG.tids1, tidsRule,
						ruleG.maxLeft, maxRight);
				
				// if the confidence is enough
				if (confidence >= minConfidence) {
					// save the rule in current top-k rules
					save(candidate, ruleSupport);
				}
				// register the rule as a candidate for future expansion
				registerAsCandidate(false, candidate);
			}
		}

		// for each item c found in the previous step, we create a rule	
		// I  U {c} ==> J if the support is enough
		for (Entry<Integer, BitSet> entry : mapCountLeft.entrySet()) {
			BitSet tidsRule = entry.getValue();
			int ruleSupport = tidsRule.cardinality();

			// if the support is enough
			if (ruleSupport >= minsuppRelative) {
				Integer itemC = entry.getKey();

				// The tidset of the left itemset is calculated
				BitSet tidsLeft = (BitSet) ruleG.tids1.clone();
				tidsLeft.and(tableItemTids[itemC]);

				// create new left part of rule
				Integer[] newLeftItemset = new Integer[ruleG.getItemset1().length + 1];
				System.arraycopy(ruleG.getItemset1(), 0, newLeftItemset, 0,
						ruleG.getItemset1().length);
				newLeftItemset[ruleG.getItemset1().length] = itemC;

				// recompute maxLeft
				int maxLeft = itemC >= ruleG.maxLeft ? itemC : ruleG.maxLeft;

				// calculate the confidence of the rule
				double confidence = ((double) ruleSupport)
						/ tidsLeft.cardinality();
				
				// create the rule
				RuleG candidate = new RuleG(newLeftItemset,
						ruleG.getItemset2(), ruleSupport, tidsLeft, tidsRule,
						maxLeft, ruleG.maxRight);

				// if the confidence is high enough
				if (confidence >= minConfidence) {
					// save the rule to the top-k rules
					save(candidate, ruleSupport);
				}
				// register the rule as a candidate for further expansions
				registerAsCandidate(true, candidate);
			}
		}
	}
	
	/**
	 * Try to expand a rule by right expansion only.
	 * @param ruleG the rule
	 */
	private void expandR(RuleG ruleG) {
		// map to record the potential item to expand the right side of the rule
		// Key: item   Value: bitset indicating the IDs of the transaction containing the item
		// from the transactions containing the rule.
		Map<Integer, BitSet> mapCountRight = new HashMap<Integer, BitSet>();
		
		// for each transaction containing the rule
		for (int tid = ruleG.common.nextSetBit(0); tid >= 0; tid = ruleG.common
				.nextSetBit(tid + 1)) {
			
			// iterate over the items in this transaction
			Iterator<Integer> iter = database.getTransactions().get(tid)
					.getItems().iterator();
			while (iter.hasNext()) {
				Integer item = iter.next();
				
				// if  that item is not frequent, then remove it from the transaction
				if (tableItemCount[item] < minsuppRelative) {
					iter.remove();
					continue;
				}
				
				//If the item is smaller than the largest item in the right side
				// of the rule, we can stop this loop because items
				// are sorted in lexicographical order.
				if (item < ruleG.maxRight) {
					break;
				}
				
				// if the item is larger than the maximum item in the right side
				// and is not contained in the left side of the rule
				if (item > ruleG.maxRight
						&& !ArraysAlgos.containsLEX(ruleG.getItemset1(), item,
								ruleG.maxLeft)) {
					
					// update the tidset of the item
					BitSet tidsItem = mapCountRight.get(item);
					if (tidsItem == null) {
						tidsItem = new BitSet();
						mapCountRight.put(item, tidsItem);
					}
					tidsItem.set(tid);
				}
			}
		}

		// for each item c found in the previous step, we create a rule	
		// I ==> J U {c} if the support is enough
    	for (Entry<Integer, BitSet> entry : mapCountRight.entrySet()) {
			BitSet tidsRule = entry.getValue();
			int ruleSupport = tidsRule.cardinality();
			
			// if the support is enough
			if (ruleSupport >= minsuppRelative) {
				Integer itemC = entry.getKey();

				// create new right part of rule
				Integer[] newRightItemset = new Integer[ruleG.getItemset2().length + 1];
				System.arraycopy(ruleG.getItemset2(), 0, newRightItemset, 0,
						ruleG.getItemset2().length);
				newRightItemset[ruleG.getItemset2().length] = itemC;

				//recompute maxRight
				int maxRight = itemC >= ruleG.maxRight ? itemC : ruleG.maxRight;

				// calculate confidence
				double confidence = ((double) ruleSupport)
						/ ruleG.tids1.cardinality();
				
				// create the rule
				RuleG candidate = new RuleG(ruleG.getItemset1(),
						newRightItemset, ruleSupport, ruleG.tids1, tidsRule,
						ruleG.maxLeft, maxRight);
				
				// if the confidence is enough
				if (confidence >= minConfidence) {
					// save the rule to the current top-k rules
					save(candidate, ruleSupport);
				}
				// register the rule as a candidate for future expansion(s)
				registerAsCandidate(false, candidate); 
			}
		}
	}
	
	/**
	 * Save a rule to the current set of top-k rules.
	 * @param rule the rule to be saved
	 * @param support the support of the rule
	 */
	private void save(RuleG rule, int support) {
		// We add the rule to the set of top-k rules
		kRules.add(rule);
		// if the size becomes larger than k
		if (kRules.size() > k) {
			// if the support of the rule that we haved added is higher than
			// the minimum support, we will need to take out at least one rule
			if (support > this.minsuppRelative) {
				// we recursively remove the rule having the lowest support,
				// until only k rules are left
				do {
					kRules.poll();
				} while (kRules.size() > k);
			}
			// we raise the minimum support to the lowest support in the 
			// set of top-k rules
			this.minsuppRelative = kRules.peek().getAbsoluteSupport();
		}
	}

	/**
	 * Method to scan the database to create the vertical database.
	 * @param database a database of type Database.
	 */
	private void scanDatabase(Database database) {
		// for each transaction
		for (int j = 0; j < database.getTransactions().size(); j++) {
			Transaction transaction = database.getTransactions().get(j);
			// for each item in the current transaction
			for (Integer item : transaction.getItems()) {
				// update the tidset of this item (represented by a bitset.
				BitSet ids = tableItemTids[item];
				if (ids == null) {
					tableItemTids[item] = new BitSet(database.tidsCount);
				}
				tableItemTids[item].set(j);
				// update the support of this item
				tableItemCount[item] = tableItemCount[item] + 1;
			}
		}
	}
	
	/**
	 * Print statistics about the last algorithm execution.
	 */
	public void printStats() {
		System.out.println("=============  TOP-K RULES - STATS =============");
		System.out.println("Minsup : " + minsuppRelative);
		System.out.println("Rules count: " + kRules.size());
		System.out.println("Memory : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("Total time : " + (timeEnd - timeStart) + " ms");
		System.out
				.println("===================================================");
	}
	
	/**
	 * Write the rules found to an output file.
	 * @param path the path to the output file
	 * @throws IOException exception if an error while writing the file
	 */
	public void writeResultTofile(String path) throws IOException {
		// Prepare the file
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		
		// sort the rules in sorted order before printing them
		// because the Iterator from Java on a priority queue do not
		// show the rules in priority order unfortunately (even though
		// they are sorted in the priority queue. 
		Object[] rules = kRules.toArray();
		Arrays.sort(rules);  
		
		// for each rule
		for(Object ruleObj : rules){
			RuleG rule = (RuleG) ruleObj;
			
			// Write the rule
			StringBuffer buffer = new StringBuffer();
			buffer.append(rule.toString());
			// write separator
			buffer.append(" #SUP: ");
			// write support
			buffer.append(rule.getAbsoluteSupport());
			// write separator
			buffer.append(" #CONF: ");
			// write confidence
			buffer.append(rule.getConfidence());
			writer.write(buffer.toString());
			writer.newLine();
		}
		// close the file
		writer.close();
	}
}
