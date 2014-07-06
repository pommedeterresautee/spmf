package ca.pfv.spmf.algorithms.sequential_rules.cmrules;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;

/**
 * This is an implementation of the AprioriTID algorithm that is
 * modified to be used with the CMRules algorithm. AprioriTID was
 * proposed in:
 * <br/><br/>

 *   Agrawal R, Srikant R. "Fast Algorithms for Mining Association Rules", VLDB. Sep 12-15 1994, Chile, 487-99,
 * <br/><br/>
 * 
 * The Apriori algorithm finds all the frequents itemsets and their support
 * in a binary context.
 * 
 * @author Philippe Fournier-Viger
 */
public class AlgoAprioriTID_forCMRules {

	// frequent itemsets found by the algorithm
	protected Itemsets frequentItemsets = new Itemsets("FREQUENT ITEMSETS");
	// transaction database
	protected TransactionDatabase database;

	// the current level
	protected int k; 

//	 a triangular matrix for efficiently counting the support of pairs of items
	// (received as parameter and optional)
	TriangularMatrix matrix;

	// the minimum support threshold
	int minSuppRelative;
	
	// Special parameter to set the maximum size of itemsets to be discovered
	int maxItemsetSize = Integer.MAX_VALUE;

	/**
	 * Constructor
	 * @param database  the transaction database
	 * @param matrix    the triangular matrix
	 */
	public AlgoAprioriTID_forCMRules(TransactionDatabase database, TriangularMatrix matrix) {
		this.database = database;
		this.matrix = matrix;
	}

	/**
	 * Run the algorithm.
	 * @param minsuppRelative    the minimum support threshold
	 * @param listFrequentsSize1  the list of frequent itemsets of size 1
	 * @param mapItemCount  a map of items (key) and their tidset (value).
	 * @return the frequent itemsets
	 */
	public Itemsets runAlgorithm(int minsuppRelative, List<Integer> listFrequentsSize1, 
			Map<Integer, Set<Integer>> mapItemCount) {
		
		// save the minimum suppor threshold
		this.minSuppRelative = minsuppRelative;

		// To build level 1, we keep only the frequent candidates.
		// We scan the database one time to calculate the support of each candidate.
		k=1;
		List<Itemset> level = createLevelWithFrequentItemsetsSize1(listFrequentsSize1, mapItemCount);

		// Generate candidates with size k = 1 (all itemsets of size 1)
		k = 2;
		// While the level is not empty
		while (!level.isEmpty()  && k <= maxItemsetSize) {
			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			level = generateCandidateSizeK(level);; 
			k++;
		}
		// return frequent itemsets
		return frequentItemsets; // Return all frequent itemsets found!
	}


	/**
	 * Generate frequents itemsets of size 1
	 * @param listFrequentsSize1  list of frequent items of size 1
	 * @param mapItemCount a map indicating the tidset (value) of each item (key)
	 * @return the itemsets of size1
	 */
	protected List<Itemset> createLevelWithFrequentItemsetsSize1(List<Integer> listFrequentsSize1, Map<Integer, Set<Integer>> mapItemCount) {
		// create the structure to store itemsets of size 1
		List<Itemset> levelK = new ArrayList<Itemset>();

		// for each item in the list of frequent items
		for(Integer item : listFrequentsSize1){
			// create an itemset
			Itemset itemset = new Itemset(item);
			itemset.setTIDs(mapItemCount.get(item));
			// add it to the level k that will be used for generating k+1 later on...
			levelK.add(itemset);
			// add the itemset to frequent itemsets
			frequentItemsets.addItemset(itemset, k);
		}
		// return itemsets of size k
		return levelK;
	}

	
	/**
	 * Generate candidate itemsets of size K by using itemsets of size k-1
	 * @param levelK_1   itemsets of size k-1
	 * @return  candidates of size K
	 */
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1) {
		// a set to store candidates of size K
		List<Itemset> candidates = new ArrayList<Itemset>();

		// For each itemset I1 and I2 of level k-1
loop1:	for(int i=0; i< levelK_1.size(); i++){
			Itemset itemset1 = levelK_1.get(i);
loop2:		for(int j=i+1; j< levelK_1.size(); j++){
				Itemset itemset2 = levelK_1.get(j);
				
				// we compare items of itemset1 and itemset2.
				// If they have all the same k-1 items and the last item of
				// itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a
				// candidate
				for(int k=0; k< itemset1.size(); k++){
					// if they are the last items
					if(k == itemset1.size()-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1.getItems()[k] >= itemset2.get(k)){  
							continue loop1;
						}
					}
					// if the k-th items is smalle rinn itemset1
					else if(itemset1.getItems()[k] < itemset2.get(k)){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems()[k]> itemset2.get(k)){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}

				// create list of common tids
				Set<Integer> list = new HashSet<Integer>();
				// for each tid from the tidset of itemset1
				for(Integer val1 : itemset1.getTransactionsIds()){
					// if it appears also in the tidset of itemset2
					if(itemset2.getTransactionsIds().contains(val1)){
						// add it to common tids
						list.add(val1);
					}
				}

				// if the combination of itemset1 and itemset2 is frequent
				if(list.size() >= minSuppRelative){
					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					candidate.setTIDs(list);
					
					// add it to the list of candidates
					candidates.add(candidate);
					// add it to the list of frequent itemsets
					frequentItemsets.addItemset(candidate, k);
				}
			}
		}
		// return the list of candidates
		return candidates;
	}

	/**
	 * Get the frequent itemsets.
	 * @return an object Itemsets containing the frequent itemsets.
	 */
	public Itemsets getItemsets() {
		return frequentItemsets;
	}

	/**
	 * Set the maximum frequent itemset size to be discovered.
	 * @param maxItemsetSize the maximum size as a int.
	 */
	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}
}
