package ca.pfv.spmf.algorithms.frequentpatterns.itemsettree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
 * This class contains methods that are shared by the Itemset-Tree
 * and Memory-Efficient Itemset Tree Implementations.
 * 
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
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
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
abstract class AbstractItemsetTree {
	
	// root of the itemset tree
	ItemsetTreeNode root = null;

	// statistics about tree construction
	int nodeCount; // number of nodes in the tree (recalculated by printStatistics() )
	long totalItemCountInNodes;  // total number of items stored in nodes (recalculated by printStatistics()
	
	long startTimestamp;  // start time of tree construction (buildTree())
	long endTimestamp;   // end time  of tree contruction (buildTree())

	/**
	 * Method to calculate the largest common ancestor of two given itemsets
	 * (as defined in the paper).
	 * @param itemset1  the first itemset
	 * @param itemset2  the second itemset
	 * @return a new itemset which is the largest common ancestor or null if it is the empty set
	 */
	protected int[] getLargestCommonAncestor(int[] itemset1, int[] itemset2) {
		// if one of the itemsets is null,
		// return null.
		if(itemset2 == null || itemset1 == null){
			return null;
		}
	
		// find the minimum length of the itemsets
		int minI = itemset1.length < itemset2.length ? itemset1.length : itemset2.length;
		
		int count = 0;  // to count the size of the common ancestor
		
		// for each position in the itemsets from 0 to the maximum length -1
		// Note that we use maxI-1 because we don't want that
		// the maximum ancestor to be equal to itemset1 or itemset2
		for(int i=0; i < minI; i++){   
			// if the two items are different, we stop because
			// of the lexical ordering
			if(itemset1[i] != itemset2[i]){
				break;
			}else{
				// otherwise we inscrease the counter indicating the number of common
				// items in the prefix
				count++;
			}
		}
		// if there is a common ancestor of size >0
		// (we don,t want the empty set!)
		if(count >0 && count < minI){
			// create the itemset by copying the first "count" elements of
			// itemset1 and return it
			int[] common = new int[count];
			System.arraycopy(itemset1, 0, common, 0, count);
			return common;
		}
		else{
			// otherwise, return null because the common ancestor is the empty set
			return null;
		}
	}
	


	/**
	 * Check if a first itemset is the ancestor of the second itemset
	 * @param itemset1  the first itemset
	 * @param itemset2 the second itemset
	 * @return true, if yes, otherwise, false.
	 */
	protected boolean ancestorOf(int[] itemset1, int[] itemset2) {
		// if the second itemset is null (empty set), return false
		if(itemset2 == null){
			return false;
		}
		// if the first itemset is null (empty set), return true
		if(itemset1 == null){
			return true;
		}
		// if the length of itemset 1 is greater than the one of
		// itemset2, it cannot be the ancestor, so return false
		if(itemset1.length >= itemset2.length){
			return false;
		}
		// otherwise, loop on items from itemset1
		// and check if they are the same as itemset 2
		for(int i=0; i< itemset1.length; i++){
			// if one item is different, itemset1 is not the ancestor
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		// otherwise itemset1 is an ancestor of itemset2
		return true;
	}
	

	/**
	 * Method to check if two itemsets are equals
	 * @param itemset1 the first itemset
	 * @param itemset2 the second itemset
	 * @param prefix 
	 * @return true if they are the same or false otherwise
	 */
	protected boolean same(int[] itemset1, int[] itemset2) {
		// if one is null, then returns false
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		// if they don't have the same size, then they cannot
		// be equal
		if(itemset1.length != itemset2.length){
			return false;
		}
		// otherwise, loop on items from itemset1
		// and check if they are the same as itemset 2
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				// if one is different then they are not the same
				return false;
			}
		}
		// otherwise they are the same
		return true;
	}
	

	/**
	 * Get the frequent itemsets subsuming a given itemset for a given minimum support value.
	 * @param is  the itemset
	 * @param minsup the minimum support threshold (integer)
	 * @return an hashtable containing the frequent itemsets
	 */
	public HashTableIT getFrequentItemsetSubsuming(int[] is, int minsup) {
		// call the recursive method 
		HashTableIT hashTable = getFrequentItemsetSubsuming(is);
		// after finding the itemsets we do a loop to remove those with a support lower than minsup,
		// This does not seems efficient but that is how the authors of the paper do it.
		
		// for each position in the internal array of the hash table
		for(List<Itemset> list : hashTable.table){
			// if that position is not empty
			if(list != null){
				// loop over the itemsets stored at that position
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					// if the itemset is infrequent, remove it
					Itemset itemset = (Itemset) it.next();
					if(itemset.support < minsup){
						it.remove();
					}
				}
			}
		}
		// then we return the hash table
		return hashTable;
	}
	
	/**
	 * This method pass through the itemset tree to get all itemsets
	 * that are subsuming a given itemset "s" and their support. Note that
	 * this method may also return infrequent itemsets that can be filtered by 
	 * additional processing after.
	 * @param s the itemset
	 * @return  an hashtable countaining itemsets and their support.
	 */
	abstract protected HashTableIT getFrequentItemsetSubsuming(int[] s);
	

	/**
	 * Generate all association rules with a given itemset as antecedent.
	 * @param s  the itemset to be used as antecedent
	 * @param minsup  the minsup threshold to be used
	 * @param minconf the minconf threshold to be used
	 * @return a list of association rules
	 */
	public List<AssociationRuleIT> generateRules(int[] s, int minsup, double minconf) {
		// create a list of association rules for storing the result
		List<AssociationRuleIT> rules = new ArrayList<AssociationRuleIT>();
		
		// put the items from the itemset in a hashset
		// for quick item inclusion checking
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}

		// calculate the support of the itemset
		// (it will be used for calculating the confidence)
		int suppS = getSupportOfItemset(s);
		
		// get all frequent itemsets
		 HashTableIT frequentItemsets = getFrequentItemsetSubsuming(s, minsup);
		 // for each position in the hash table
		 for(List<Itemset> list : frequentItemsets.table){
			 // if the position is not empty
			if(list != null){
				// iterate over all itemsets in the same bucket in the hash table
				for(Itemset c : list){
					 // if we have found an itemset having the same size as S,
					// we continue because we want to find an itemset C to generate
					// rules by doing  C - S and that would result in the empty set.
					if(c.size() == s.length){ 
						continue;
					}
					//Try to generate a rule by 
					// creating a new itemset l for the consequent as  C - S.
					int[] l = new int[c.itemset.length - s.length];
					int pos =0;
					// we copy to l the items from c that are not in S.
					for(Integer item : c.itemset){
						if(!seti.contains(item)){
							l[pos++] = item;
						}
					}
					// calculate confidence of S -->  C - S
					int suppC = getSupportOfItemset(c.itemset);
					
					// Note: the formula for calculating the confidence is wrong in the paper.
					// It is not g(l) / g(c) but it should be g(c) / g(s).
					double conf = (double)suppC / suppS;  
					// if the confidence is no less than minconf
					if(conf >= minconf){
						// create a new rule  S --> L
						AssociationRuleIT rule = new AssociationRuleIT();
						rule.itemset1 = s;
						rule.itemset2 = l;
						rule.support = suppC;
						rule.confidence = conf;
						// add it to the list of rules found.
						rules.add(rule);
					}
					
				}
			}
		}
		 // return the result
		return rules;
	}
	
	/**
	 * Get the support of a given itemset s.
	 * @param s the itemset
	 * @return the support as an integer.
	 */
	public abstract int getSupportOfItemset(int[] s);

}
