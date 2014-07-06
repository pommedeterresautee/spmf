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
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * TNR is an algorithm for mining the TOP-K non redundant association rules 
 * with a pattern growth approach and several optimizations. This is the original
 * implementation as proposed in the following paper:
 * <br/><br/>
 * 
 * Fournier-Viger, P., Tseng, V.S. (2012). Mining Top-K Non-Redundant Association Rules. Proc. 20th International Symposium on Methodologies for Intelligent Systems (ISMIS 2012), Springer, LNCS 7661, pp. 31- 40. 
 * 
 * @author Philippe Fournier-Viger, 2012
 */
public class AlgoTNR {
	
	// for statistics
	long timeStart = 0;  // start time of last execution
	long timeEnd = 0;   // end time of last execution
	
	// the maximum number of candidates at the same time during the last execution
	int maxCandidateCount = 0;
	int notAdded = 0;  // rules eliminated by strategy 1
	int totalremovedCount = 0; // rules eliminated by strategy 2
	
	long totalCandidatesConsideredFromR = 0;  // the total number of candidates processed
	long totalRules11considered = 0;  // the total number of rules with only two items considered
	
	// Parameters
	double minConfidence;  // minimum confidence threshold
	int initialK = 0;     // the value of k set by the user
	Database database;    // the transaction database
	int delta = 0;        // the delta parameter
	
	// Internal variables
	RedBlackTree<RuleG> kRules;  // the top k rules found until now 
	RedBlackTree<RuleG> candidates;  // the candidates for expansion
	int k=0;              // will contain k + delta
	int minsuppRelative;   // minimum support threshold that will be raised dynamically
	
	// a vertical representation of the database
	BitSet[] tableItemTids; // [item], IDs of transaction containing the item
	// a table indicating the support of each item
	int[] tableItemCount; // [item], support
	
	/**
	 * Default constructor
	 */
	public AlgoTNR() {}

	/**
	 * Run the algorithm.
	 * @param k the value of k.
	 * @param minConfidence the minimum confidence threshold.
	 * @param database the database.
	 * @param delta the delta parameter
	 * @return a RedBlackTree containing approximately k rules.
	 */
	public RedBlackTree<RuleG> runAlgorithm(int k, double minConfidence, Database database, int delta) {


		// reset statistics
		totalremovedCount = 0;
		notAdded = 0;
		MemoryLogger.getInstance().reset(); // reset utility to check memory usage
		maxCandidateCount = 0;
		totalCandidatesConsideredFromR =0;
		totalRules11considered = 0;
		
		// save parameters
		this.delta =  delta;
		this.minConfidence = minConfidence;
		this.database = database;
		this. initialK = k;
		
		// calculate k
		this.k = k + delta;           // IMPORTANT

		// set the minimum support threshold that will be raised dynamically
		this.minsuppRelative = 1;
		
		// initialize data structures
		tableItemTids = new BitSet[database.maxItem+1]; // id item, count
		tableItemCount = new int[database.maxItem+1];
		kRules = new RedBlackTree<RuleG>();
		candidates = new RedBlackTree<RuleG>();

		// record the start time
		timeStart = System.currentTimeMillis(); 
		
		// perform the first database scan to generate vertical database representation
		scanDatabase(database);
		
		// start the generation of rules
		start();
		
		// record the end time
		timeEnd = System.currentTimeMillis(); 
		
		// if more than k rules because several of them have the same support,
		// we remove some to only return k to the user
		cleanResult();
		
		// return the result
		return kRules;
	}


	/**
	 * Start the rule generation.
	 */
	private void start() {

		// for each item I in the database
main:	for(int itemI=0; itemI<= database.maxItem; itemI++){
			// if the item is not frequent according to the current
			// minsup threshold, then skip it
			if(tableItemCount[itemI] < minsuppRelative){
				continue main;
			}
			// Get the bitset corresponding to item I
			BitSet tidsI = tableItemTids[itemI];
			
			// for each item J in the database
main2:		for(int itemJ=itemI+1; itemJ <= database.maxItem; itemJ++){
				// if the item is not frequent according to the current
				// minsup threshold, then skip it
				if(tableItemCount[itemJ] < minsuppRelative){
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
				
				totalRules11considered++; // for stats
				
				// If  rules I ==> J and J ==> I have enough support
				if(support >= minsuppRelative){
					// generate  rules I ==> J and J ==> I and remember these rules
					// for future possible expansions
					generateRuleSize11(itemI, tidsI, itemJ, tidsJ, commonTids, support);
				}
			}
		}
	
		// Now we have finished checking all the rules containing 1 item
		// in the left side and 1 in the right side,
		// the next step is to recursively expand rules in the set 
		// "candidates" to find more rules.
		while(candidates.size() >0){
			// We take the rule that has the highest support first
			RuleG rule = candidates.popMaximum();
			// if there is no more candidates with enough support, then we stop
			if(rule.getAbsoluteSupport() < minsuppRelative){
//				candidates.remove(rule);
				break;
			}
			// Otherwise, we try to expand the rule
			totalCandidatesConsideredFromR++;
			// If the rule should be expanded by both left and ride side
			if(rule.expandLR){
				// we do it
				expandLR(rule);
			}else{
				// If the rule should only be expanded by left side to
				// avoid generating redundant rules, then we 
				// only expand the left side.
				expandR(rule);
			}
//			candidates.remove(rule);
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
	private void generateRuleSize11(Integer itemI, BitSet tidI, Integer itemJ, BitSet tidJ, BitSet commonTids, int cardinality) {	
		// Create the rule I ==> J
		Integer[] itemsetI = new Integer[1];
		itemsetI[0] = itemI;
		Integer[] itemsetJ = new Integer[1];
		itemsetJ[0] = itemJ;
		RuleG ruleLR = new RuleG(itemsetI, itemsetJ, cardinality, tidI, commonTids, itemI, itemJ);
		 
		// calculate the confidence
		double confidenceIJ = ((double) cardinality) / (tableItemCount[itemI]);

		// if rule i->j has minimum confidence
		if(confidenceIJ >= minConfidence){
			// save the rule in current top-k rules
			save(ruleLR, cardinality); 
		}
		// register the rule as a candidate for future expansion
		registerAsCandidate(true, ruleLR);

		// Create the rule J ==> I
		double confidenceJI = ((double) cardinality) / (tableItemCount[itemJ]);
		RuleG ruleRL = new RuleG(itemsetJ, itemsetI, cardinality, tidJ, commonTids, itemJ, itemI);
		
		// if rule J->I has minimum confidence
		if(confidenceJI >= minConfidence){
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
		if(candidates.size() >= maxCandidateCount){
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
		
		for (int tid = ruleG.common.nextSetBit(0); tid >= 0; tid =  ruleG.common.nextSetBit(tid+1)) {
			Iterator<Integer> iter = database.getTransactions().get(tid).getItems().iterator();
			while(iter.hasNext()){
				Integer item = iter.next();
				// CAN DO THIS BECAUSE TRANSACTIONS ARE SORTED BY DESCENDING ITEM IDS (see Database.Java)
				if(item < ruleG.maxLeft && item < ruleG.maxRight){  // 
					break;
				}
				if(tableItemCount[item] < minsuppRelative){
					iter.remove();
					continue;
				}
				if(item > ruleG.maxLeft &&!ArraysAlgos.containsLEX(ruleG.getItemset2(),item, ruleG.maxRight)){
					BitSet tidsItem = mapCountLeft.get(item);
					if(tidsItem == null){
						tidsItem = new BitSet();
						mapCountLeft.put(item, tidsItem);
					}
					tidsItem.set(tid);	
				}
				if(item > ruleG.maxRight && !ArraysAlgos.containsLEX(ruleG.getItemset1(),item, ruleG.maxLeft)){
					BitSet tidsItem = mapCountRight.get(item);
					if(tidsItem == null){
						tidsItem = new BitSet();
						mapCountRight.put(item, tidsItem);
					}
					tidsItem.set(tid);	
				}
			}
		}
		
		// for each item c found in the previous step, we create a rule	
		// I  ==> J U {c} if the support is enough 	
    	for(Entry<Integer, BitSet> entry : mapCountRight.entrySet()){
    		BitSet tidsRule = entry.getValue();
    		int ruleSupport = tidsRule.cardinality();
    		
    		// if the support is enough
    		if(ruleSupport >= minsuppRelative){ 
        		Integer itemC = entry.getKey();
        		
				// create new right part of rule
				Integer[] newRightItemset = new Integer[ruleG.getItemset2().length+1];
				System.arraycopy(ruleG.getItemset2(), 0, newRightItemset, 0, ruleG.getItemset2().length );
				newRightItemset[ruleG.getItemset2().length] =  itemC;

				// recompute maxRight
				int maxRight = (itemC >= ruleG.maxRight) ? itemC : ruleG.maxRight;
				
				// calculate the confidence of the rule
				double confidence =  ((double)ruleSupport) / ruleG.tids1.cardinality();
				
				// create the rule
				RuleG candidate = new RuleG(ruleG.getItemset1(), newRightItemset, ruleSupport, ruleG.tids1, tidsRule, ruleG.maxLeft, maxRight);
				
				// if the confidence is enough
				if(confidence >= minConfidence){
					// save the rule in current top-k rules
					save(candidate, ruleSupport);
				}
				// register the rule as a candidate for future expansion
				registerAsCandidate(false, candidate);
    		}
    	}
    	
		// for each item c found in the previous step, we create a rule	
		// I  U {c} ==> J if the support is enough
    	for(Entry<Integer, BitSet> entry : mapCountLeft.entrySet()){
    		BitSet tidsRule = entry.getValue();
    		int ruleSupport = tidsRule.cardinality();
    		
    		// if the support is enough
    		if(ruleSupport >= minsuppRelative){ 
        		Integer itemC = entry.getKey();
        		
				// The tidset of the left itemset is calculated
				BitSet tidsLeft = (BitSet)ruleG.tids1.clone();
				tidsLeft.and(tableItemTids[itemC]);

				// create new left part of rule
				Integer[] newLeftItemset = new Integer[ruleG.getItemset1().length+1];
				System.arraycopy(ruleG.getItemset1(), 0, newLeftItemset, 0, ruleG.getItemset1().length );
				newLeftItemset[ruleG.getItemset1().length] =  itemC;

				// recompute maxLeft for the new rule
				int maxLeft = itemC >= ruleG.maxLeft ? itemC : ruleG.maxLeft;
				
				// calculate the confidence
				double confidence =  ((double)ruleSupport) / tidsLeft.cardinality();
				// create the rule
				RuleG candidate = new RuleG(newLeftItemset, ruleG.getItemset2(), ruleSupport, tidsLeft, tidsRule, maxLeft, ruleG.maxRight);
				
				// If the confidence is enough
				if(confidence >= minConfidence){
					// save the rule in current top-k rules
					save(candidate, ruleSupport);
				}
				// register the rule as a candidate for future expansion
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
		for (int tid = ruleG.common.nextSetBit(0); tid >= 0; tid =  ruleG.common.nextSetBit(tid+1)) {
			
			// iterate over the items in this transaction
			Iterator<Integer> iter = database.getTransactions().get(tid).getItems().iterator();
			while(iter.hasNext()){
				Integer item = iter.next();
				
				// if  that item is not frequent, then remove it from the transaction
				if(tableItemCount[item] < minsuppRelative){
					iter.remove();
					continue;
				}
				
				//If the item is smaller than the largest item in the right side
				// of the rule, we can stop this loop because items
				// are sorted in lexicographical order.
				if(item < ruleG.maxRight){
					break;
				}
				
				// if the item is larger than the maximum item in the right side
				// and is not contained in the left side of the rule
				if(item > ruleG.maxRight && !ArraysAlgos.containsLEX(ruleG.getItemset1(),item, ruleG.maxLeft)){
					
					// update the tidset of the item
					BitSet tidsItem = mapCountRight.get(item);
					if(tidsItem == null){
						tidsItem = new BitSet();
						mapCountRight.put(item, tidsItem);
					}
					tidsItem.set(tid);	
				}
			}
		}
		
		// for each item c found in the previous step, we create a rule	
		// I ==> J U {c} if the support is enough
    	for(Entry<Integer, BitSet> entry : mapCountRight.entrySet()){
    		BitSet tidsRule = entry.getValue();
    		int ruleSupport = tidsRule.cardinality();
    		
    		// if the support is enough
    		if(ruleSupport >= minsuppRelative){ 
        		Integer itemC = entry.getKey();
        		
				// create new right part of rule
				Integer[] newRightItemset = new Integer[ruleG.getItemset2().length+1];
				System.arraycopy(ruleG.getItemset2(), 0, newRightItemset, 0, ruleG.getItemset2().length );
				newRightItemset[ruleG.getItemset2().length] =  itemC;

				// update maxRight
				int maxRight = itemC >= ruleG.maxRight ? itemC : ruleG.maxRight;
				
				// calculate the confidence
				double confidence = ((double)ruleSupport) / ruleG.tids1.cardinality();
				
				// create the rule
				RuleG candidate = new RuleG(ruleG.getItemset1(), newRightItemset, ruleSupport, ruleG.tids1,tidsRule, ruleG.maxLeft, maxRight);
				
				// If the confidence is enough
				if(confidence >= minConfidence){
					// save the rule in current top-k rules
					save(candidate, ruleSupport);
				}
				// register the rule as a candidate for future expansion
				registerAsCandidate(false, candidate);  // IMPORTANT: WAS MISSING IN PREVIOUS VERSION !!!!
    		}
    	}	
	}

	/**
	 * Save a rule to the current set of top-k rules.
	 * @param rule the rule to be saved
	 * @param support the support of the rule
	 */
	private void save(RuleG rule, int support) {
		
		// We get a pointer to the node in the redblacktree for the
		// rule having a support just lower than support+1.
		RedBlackTree<RuleG>.Node lowerRuleNode = kRules.lowerNode(new RuleG(null, null, support+1, null, null, 0, 0));	
		
		// Applying Strategy 1 and Strategy 2
		Set<RuleG> rulesToDelete = new HashSet<RuleG>();
		// for each rule "lowerRuleNode" having the save support as the rule received as parameter
		while(lowerRuleNode != null &&
				lowerRuleNode.key != null 
				&& lowerRuleNode.key.getAbsoluteSupport() == support){
			// Strategy 1: 
			// if the confidence is the same and the rule "lowerRuleNode" subsume the new rule
			// then we don't add the new rule
			if(rule.getConfidence() == lowerRuleNode.key.getConfidence() && subsume(lowerRuleNode.key, rule)){
				notAdded++; // for stats
//				System.out.println("The rule  " + rule + " was not added because it is subsumed by : " + lowerRuleNode.key);
				return ;
			}
			// Strategy 2:
			// if the confidence is the same and the rule "lowerRuleNode" subsume the new rule
			// then we don't add the new rule
			if(rule.getConfidence() == lowerRuleNode.key.getConfidence() && subsume(rule, lowerRuleNode.key)){
				// add the rule to the set of rules to be deleted
				rulesToDelete.add(lowerRuleNode.key);
				totalremovedCount++;
			}
			// check the next rule
			lowerRuleNode = kRules.lowerNode(lowerRuleNode.key);
		}
		
		// delete the rules to be deleted
		for(RuleG ruleX : rulesToDelete){
//			System.out.println("REMOVED  " + ruleX + " because subsumed by : " + rule);
			kRules.remove(ruleX);
		}
				
		// Now the rule "rule" has passed the test of Strategy 1 already,
		// so we add it to the set of top-k rules
		kRules.add(rule);
		// if there is more than k rules
		if(kRules.size() > k ){
			// and if the support of the rule is higher than minsup
			if(support > this.minsuppRelative ){
				// recursively find the rule with the lowest support and remove it
				// until there is just k rules left
				RuleG lower;
				do{
					lower = kRules.lower(new RuleG(null, null, this.minsuppRelative+1, null, null, 0, 0));
					if(lower == null){
						break;  /// IMPORTANT
					}
					kRules.remove(lower);
				}while(kRules.size() > k);
			}
			// set the minimum support to the support of the rule having
			// the lowest suport.
			this.minsuppRelative = kRules.minimum().getAbsoluteSupport();
		}
//		System.out.println(this.minsuppRelative);
	}
	
//private boolean isRedundant(RuleG rule) {
//	if(rule.getItemset1().length > 1){
//		for(int i=0; i< rule.getItemset1().length; i++){
//			BitSet tids = null;
//			for(int j=0; j< rule.getItemset1().length; j++){
//				if( i != j){
//					if(tids == null){
//						tids = (BitSet) tableItemTids[rule.getItemset1()[j]].clone();
//					}else{
//						tids.and(tableItemTids[rule.getItemset1()[j]]);
//					}
//				}
//			}
//			// calculate support
//			int support =  tids.cardinality();
//			if(rule.getItem)
//			
//			= ;
//			if(tids.cardinality() == rule.getAbsoluteSupport()){
//				notAdded++;
//				return true;
//			}
//		}
//	}
//	
//	
//	return false;
//}

	/**
	 * Check if a rule subsumes another.
	 * @param rule1 a rule
	 * @param rule2 a second rule
	 * @return true if rule1 subsume rule2, otherwise false.
	 */
	private boolean subsume(RuleG rule1, RuleG rule2) {
//		if(rule1 == rule2 || rule1.getConfidence() != rule2.getConfidence() ||
//				   rule1.getAbsoluteSupport() != rule2.getAbsoluteSupport()){
//			return false;
//		}

		// We check first the size of the itemsets
		if(rule1.getItemset1().length <= rule2.getItemset1().length && rule1.getItemset2().length >=rule2.getItemset2().length){
			// After that we check the inclusion relationships between
			// the itemsets			
			boolean cond1 = ArraysAlgos.containsOrEquals(rule2.getItemset1(), rule1.getItemset1());
			boolean cond2 = ArraysAlgos.containsOrEquals(rule1.getItemset2(), rule2.getItemset2());
			// If all the conditions are met the method returns true.
			if(cond1 && cond2){
				return true;
			}
		}
		// otherwise, it returns false
		return false;	
	}
	
	/**
	 * This method remove exceeding rules so that only k are presented to the user
	 */
	private void cleanResult() {
		// for each rules in the set of top-k rules
		while(kRules.size() > initialK){
			// take out the minimum until the size is k
			kRules.popMinimum();
		}
		// set the minimum support to the minimum of the remaining rules 
		minsuppRelative = kRules.minimum().getAbsoluteSupport();
	}
	

	/**
	 * Method to scan the database to create the vertical database.
	 * @param database a database of type Database.
	 */
	private void scanDatabase(Database database) {
		//  for each transaction
		for(int j=0; j < database.getTransactions().size(); j++){
				Transaction transaction = database.getTransactions().get(j);
				
				// for each item in the current transaction
				for(Integer item : transaction.getItems()){
					// update the tidset of this item (represented by a bitset.
					BitSet ids = tableItemTids[item];
					if(ids == null){
						tableItemTids[item] = new BitSet(database.tidsCount);
					}
					tableItemTids[item].set(j);
					// update the support of this item
					tableItemCount[item] = tableItemCount[item] +1;
				}
		}
	}

	/**
	 * Write the rules found to an output file.
	 * @param path the path to the output file
	 * @throws IOException exception if an error while writing the file
	 */
	public void writeResultTofile(String path) throws IOException {
		// Prepare the file
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		
		// for each rule
		Iterator<RuleG> iter = kRules.iterator();
		while (iter.hasNext()) {
			// Write the rule
			RuleG rule = (RuleG) iter.next();
			StringBuffer buffer = new StringBuffer();
			buffer.append(rule.toString());
			// write separator
			buffer.append(" #SUP: ");
			// write support
			buffer.append(rule.getAbsoluteSupport());
			// write Confidence
			buffer.append(" #CONF: ");
			buffer.append(rule.getConfidence());
			writer.write(buffer.toString());
			writer.newLine();
		}
		// close the file
		writer.close();
	}
	
	/**
	 * Print statistics about the last algorithm execution.
	 */
	public void printStats() {
		System.out.println("=============  NR-TOP-K RULES - STATS =============");
		System.out.println("Minsup : " + minsuppRelative);
		System.out.println("Rules count: " + kRules.size());
		System.out.println("Total time : " + ((timeEnd - timeStart) / 1000) + " s");
		System.out.println("Memory : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
//		System.out.println("Candidates count : " + candidates.size());
		System.out.println("Rules eliminated by strategy 1: " + notAdded);
		System.out.println("Rules eliminated by strategy 2: " + totalremovedCount);	
		System.out.println("--------------------------------");
		System.out.println("===================================================");
	}
}
