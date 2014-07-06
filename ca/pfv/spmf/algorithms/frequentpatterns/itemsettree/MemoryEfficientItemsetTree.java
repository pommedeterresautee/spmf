package ca.pfv.spmf.algorithms.frequentpatterns.itemsettree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is the original implementation of the Memory Efficient Itemset-tree as
 * proposed in:
 * 
 *   Fournier-Viger, P., Mwamikazi, E., Gueniche, T., Faghihi, U. (2013). Memory 
 *   Efficient Itemset Tree for Targeted Association Rule Mining. 
 *   Proc. 9th International Conference on Advanced Data Mining and Applications 
 *   (ADMA 2013) Part II, Springer LNAI 8347, pp. 95-106.
 *   
 * Copyright (c) 2013 Philippe Fournier-Viger
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

public class MemoryEfficientItemsetTree extends AbstractItemsetTree implements Serializable {

	long sumBranchesLength; // sum of branches length
	int totalNumberOfBranches; // total number of branches

	// This variable is commented and was only used
	// for testing the performance of random queries
//	HashSet<Integer> items = new HashSet<Integer>(); 
	
	/**
	 * Default constructor
	 */
	public MemoryEfficientItemsetTree() {	
		super();
	}

	/**
	 * Build the itemset-tree based on an input file containing transactions
	 * @param input an input file
	 * @throws IOException exception if error while reading the file
	 */
	public void buildTree(String input)
			throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// reset memory usage statistics
		MemoryLogger.getInstance().reset();
		
		// create an empty root for the tree
		root = new ItemsetTreeNode(null, 0);

		// Scan the database to read the transactions
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		 // for each line (transaction) until the end of file
		while (((line = reader.readLine()) != null)) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction into items
			String[] lineSplited = line.split(" ");
			// create a structure for storing the transaction
			int[] itemset = new int[lineSplited.length];
			 // for each item in the transaction
			for (int i=0; i< lineSplited.length; i++) {
				// convert the item to integer and add it to the structure
				itemset[i] = Integer.parseInt(lineSplited[i]);
				
				// The next line is commented and was only used
				// for testing the performance of random queries
				//items.add(itemset[i]);
			}
//			printTree();
			// call the method "construct" to add the transaction to the tree
			construct(null, root, itemset, null);
//			System.out.println(".");
		}
		// close the input file
		reader.close();
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		// close the file
		endTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * Add a transaction to the itemset tree.
	 * @param transaction the transaction to be added (array of ints)
	 */
	public void addTransaction(int[] transaction){
		// call the "construct" algorithm to add it
		construct(null, root, transaction, null);
	}


	/**
	 * Given the root of a sub-tree, add an itemset at the proper position in that tree
	 * @param r  the root of the sub-tree
	 * @param s  the itemset to be inserted
	 * @param prefix the current item(s) explored in this branch of the tree until the current node r.
	 */
	private void construct(ItemsetTreeNode parentOfR, ItemsetTreeNode r, int[] s, int[] prefix) {
				
		// if the itemset in root node is the same as the one to be inserted,
		// we just increase the support, and return.
 		if(same(s, prefix, r.itemset)){
			r.support++;
			return;
		}
		
 		int[] rprefix = append(prefix, r.itemset);
		
 		
		// if the node to be inserted is an ancestor of the itemset of the root node
		// then insert the itemset between r and its parent
		// Before:   parent_of_r --> r
		// After:    parent_of_r --> s --> r
		// e.g.   for a regular itemset tree
		//          {2}:4 --> {2,3,4,5,6}:6
		//  		 we insert {2,3}
		//          {2}�4 --> {2,3}:7 --> {2,3,4,5,6}:6
		// e.g.  for a compact itemset tree 
		//           r_parent    r
		//          {2}:4 --> {3,4,5,6}:6
		// 			 we insert s={2,3}
		//           r_parent    s'        r'
		//          {2}:4 --> {3}:7 --> {4,5,6}:6
		if(ancestorOf(s, rprefix)){
			//  Calculate  s' and r'  by using the prefix
			int[] sprime = copyItemsetWithoutItemsFrom(s, prefix);
			int[] rprime = copyItemsetWithoutItemsFrom(rprefix, sprime);
			
			// create a new node for the itemset to be inserted with the support of
			// the subtree root node + 1
			ItemsetTreeNode newNodeS = new ItemsetTreeNode(sprime, r.support +1);
			// set the childs and parent pointers.
			newNodeS.childs.add(r);
			parentOfR.childs.remove(r);
			parentOfR.childs.add(newNodeS);
//			r.parent = newNodeS;
			r.itemset = rprime;
			return;  // return
		}
		
		// Otherwise, calculate the largest common ancestor
		// of the itemset to be inserted and the root of the sutree
		int[] l = getLargestCommonAncestor(s, rprefix);
		if(l != null){ // if there is one largest common ancestor
			int[] sprime = copyItemsetWithoutItemsFrom(s, l);
			int[] rprime = copyItemsetWithoutItemsFrom(r.itemset, l);
			
			// create a new node with that ancestor and the support of
			// the root +1.
			ItemsetTreeNode newNode = new ItemsetTreeNode(l, r.support +1);
			// set the node childs and parent pointers
			newNode.childs.add(r);
			parentOfR.childs.remove(r);
			parentOfR.childs.add(newNode);
//			parentOfR = newNode;
			r.itemset = rprime;
			// append second children which is the itemset to be added with a 
			// support of 1
			ItemsetTreeNode newNode2 = new ItemsetTreeNode(sprime, 1);
			// update pointers for the new node
			newNode.childs.add(newNode2);
//			newNode2.parent = newNode;
			return;
		}
		
		// else  get the length of the root itemset
		int indexLastItemOfR = (rprefix == null)? 0 : rprefix.length;
		// increase the support of the root
		r.support++;
		// for each child of the root
		for(ItemsetTreeNode ci : r.childs){
			int[] ciprefix = append(rprefix, ci.itemset);
			
			// if one children of the root is the itemset to be inserted s,
			// then increase its support and stop
			if(same(s, ciprefix)){ // case 2
				ci.support++;
				return;
			}
			
			// if   the itemset to be inserted is an ancestor of the child ci
			if(ancestorOf(s, ciprefix)){ // case 3
				int[] sprime = copyItemsetWithoutItemsFrom(s, rprefix); 
				int[] ciprime = copyItemsetWithoutItemsFrom(ci.itemset, s); 
				
				// create a new node between ci and r in the tree
				// and update child /parents pointers
				ItemsetTreeNode newNode = new ItemsetTreeNode(sprime, ci.support+ 1);
				newNode.childs.add(ci);
//				newNode.parent = r;
				r.childs.remove(ci);
				r.childs.add(newNode);
//				ci.parent = newNode;
				ci.itemset = ciprime;
				return;
			}
			
			// if the child ci is an ancestor of s
			if(ancestorOf(ciprefix, s)){ // case 4
				
				// then make a recursive call to construct to handle this case.
				construct(r, ci, s, rprefix);
				return;
			}

			// case 5
			// if ci and s have a common ancestor that is larger than r:
			if(ciprefix[indexLastItemOfR] == s[indexLastItemOfR]){
				// find the largest common ancestor
				int[] ancestor = getLargestCommonAncestor(s, ciprefix);
				// create a new node for the ancestor itemset just found with the support
				// of ci + 1
				
				int[] ancestorprime = copyItemsetWithoutItemsFrom(ancestor, rprefix);
				
				ItemsetTreeNode newNode = new ItemsetTreeNode(ancestorprime, ci.support+ 1);
				// set r as parent
//				newNode.parent = r;
				r.childs.add(newNode);
				// add ci as a children of the new node
				ci.itemset = copyItemsetWithoutItemsFrom(ci.itemset, ancestorprime);
				newNode.childs.add(ci);
//				ci.parent = newNode;
				r.childs.remove(ci);
				// create another new node for s with a support of 1, which
				// will be the child of the first new node
				int[] sprime = copyItemsetWithoutItemsFromArrays(s, ancestorprime, rprefix);
				ItemsetTreeNode newNode2 = new ItemsetTreeNode(sprime, 1);
//				newNode2.parent = newNode;
				newNode.childs.add(newNode2);
				// end
				return;
			}
			
		}
		
		// Otherwise, case 1:
		// A new node is created for s with a support of 1 and is added
		// below the node r.
		int[] sprime = copyItemsetWithoutItemsFrom(s, rprefix);
		ItemsetTreeNode newNode = new ItemsetTreeNode(sprime, 1);
//		newNode.parent = r;
		r.childs.add(newNode);
		
	}

	/**
	 * Make a copy of an itemset while removing items that appears in
	 * two itemsets named "prefix" and "s".
	 * @param r  the itemset
	 * @param prefix  the other itemset named "prefix"
	 * @param s  the other itemset named "s"
	 * @return the itemset
	 */
	private int[] copyItemsetWithoutItemsFromArrays(int[] r,
			int[] prefix, int[] s) {
		
		// create an empty itemset
		List<Integer> rprime = new ArrayList<Integer>(r.length);
		
		// for each item in r
loop1:	for(Integer rvalue : r){
			// if the other itemset prefix is not null
			if(prefix != null){
				// for each item from the prefix
				for(int pvalue : prefix){
					// if it is the current item in r
					if(pvalue == rvalue){
						// skip this item from r
						continue loop1;
					// if the current item from prefix is larger
					// than the current item from r,
				    // then break because itemsets are lexically ordered
					// so there will be no match.
					}else if(pvalue > rvalue){
						break;
					}
				}
			}
			
			// if s is not null
			if(s != null){
				// for each item in s
				for(int svalue : s){
					// if this item in s is the current item in r
					if(rvalue == svalue){
						// skip it (don't add it to the new itemset)
						continue loop1;
					// if the current item from s is larger
					// than the current item from r,
				    // then break because itemsets are lexically ordered
					// so there will be no match.
					}else if(svalue > rvalue){
						break;
					}
				}
			}
			rprime.add(rvalue);
		}
		// transform the new itemset "rprime" from ArrayList 
		// to an array.
		int[] rprimeArray = new int[rprime.size()];
		for(int i=0; i< rprime.size(); i++){
			rprimeArray[i] = rprime.get(i);
		}
		// return the array
		return rprimeArray;
	}

	/**
	 * Make a copy of an itemset without items from a second itemset.
	 * @param itemset1 the first itemset
	 * @param itemset2 the second itemset
	 * @return the new itemset
	 */
	private int[] copyItemsetWithoutItemsFrom(int[] itemset1, int[] itemset2) {
		// if the second itemset is null, just return the first itemset
		if(itemset2 == null){
			return itemset1;
		}
		
		// create a new itemset
		List<Integer> itemset1prime = new ArrayList<Integer>(itemset1.length);
		// for each item in the first itemset
loop1:	for(int i1value : itemset1){
			// for each it in the second itemset
			for(int i2value : itemset2){
				// if the items match, don't add the current item 
				// from itemset1 to the new itemset
				if(i2value == i1value){
					continue loop1;
				// otherwise, if the current item from "itemset2"
				// is larger than the current item from "itemset1"
				// there will be no match because itemsets are 
				// lexically ordered.
				}else if(i2value > i1value){
					break;
				}
			}
			// if the current item from itemset1 was not in itemset2,
			// then add it to the new itemset
			itemset1prime.add(i1value);
		}
		// convert the new itemset from an ArrayList to an array
		int[] itemset1primeArray = new int[itemset1prime.size()];
		for(int i=0; i< itemset1prime.size(); i++){
			itemset1primeArray[i] = itemset1prime.get(i);
		}
		// return the array
		return itemset1primeArray;
	}


	/**
	 * Check if itemset1 is the same as the concatenation of prefix and itemset2
	 * @param itemset1  the first itemset
	 * @param prefix  a prefix
	 * @param itemset2 another itemset
	 * @return true if the same otherwise false
	 */
	private boolean same(int[] itemset1, int[] prefix, int[] itemset2) {
		if(prefix == null) {
			return same(itemset1, itemset2);
		}
		// if one is null, then returns false
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		// if they don't have the same size, then they cannot
		// be equal
		if(itemset1.length != itemset2.length + prefix.length){
			return false;
		}
		// otherwise, loop on items from itemset1
		// and check if they are the same as itemset 2
		int i = 0;
		while(i < prefix.length){
			if(itemset1[i] != prefix[i]){
				// if one is different then they are not the same
				return false;
			}
			i++;
		}
		int j = 0;
		while(j< itemset2.length){
			if(itemset1[j++] != itemset2[i++]){
				// if one is different then they are not the same
				return false;
			}
		}
		
		// otherwise they are the same
		return true;
	}
	
	/**
	 * Method that append two itemsets to create a larger one
	 * @param a1  the first itemset
	 * @param a2  the second itemset
	 * @return  the new itemset
	 */
	public int[] append(int[] a1, int[] a2){
		//if the first itemset is null, return the second one
		if(a1 == null){
			return a2;
		}
		//if the second itemset is null, return the first one
		if(a2 == null){
			return a1;
		}
		// create the new itemset
		int[] newArray = new int[a1.length + a2.length];
		
		// copy the first itemset in the new itemset
		int i=0;
		for(; i< a1.length; i++){
			newArray[i] = a1[i];
		}
		// copy the second itemset in the new itemset
		for(int j =0; j< a2.length; j++){
			newArray[i++] = a2[j];
		}
		// return the new itemset
		return newArray;
	}
	
	/**
	 * Print statistics about the time and maximum memory usage for the construction
	 * of the itemset tree. 
	 */
	public void printStatistics() {
		System.gc();
		System.out.println("========== MEMORY EFFICIENT ITEMSET TREE CONSTRUCTION - STATS ============");
		System.out.println(" Tree construction time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory());
		nodeCount = 0;
		totalItemCountInNodes = 0;
		sumBranchesLength = 0;
		totalNumberOfBranches = 0;
		recursiveStats(root, 1);
		System.out.println(" Node count: " + nodeCount);
		System.out.println(" Sum of items in all node: " + totalItemCountInNodes + " avg per node :" + totalItemCountInNodes / ((double)nodeCount));
		System.out.println("=====================================");
	}

	/**
	 * Recursive method to calculate statistics about the itemset tree
	 * @param root  the root node of the current subtree  
	 * @param length the cummulative sum of length of itemsets
	 */
	private void recursiveStats(ItemsetTreeNode root, int length) {
		// if the root is not null or the empty set
		if(root != null && root.itemset!=null){
			// increase node count
			nodeCount++;
			// increase the total number of items
			totalItemCountInNodes += root.itemset.length;
		}
		// for each child node, make a recursive call
		for(ItemsetTreeNode node : root.childs){
			recursiveStats(node, ++length);
		}
		// if no child, this node is a leaf, so
		// add the cummulative length of this branch to the sum
		// and add 1 to the total number of branches.
		if(root.childs.size() == 0) {
			sumBranchesLength += length;
			totalNumberOfBranches += 1;
		}
	}

	/**
	 * Print the tree to System.out.
	 */
	public void printTree() {
		System.out.println(root.toString(new StringBuffer(),""));
	}
	
	/**
	 * Return a string representation of the tree.
	 */
	public String toString() {
		return root.toString(new StringBuffer(), "");
	}

	/**
	 * Get the support of a given itemset s.
	 * @param s the itemset
	 * @return the support as an integer.
	 */
	public int getSupportOfItemset(int[] s) {
		return count(s, root, new int[0]);  // call the method count.
	}

	/**
	 * This method calculate the support of an itemset by using a subtree
	 * defined by its root.
	 * 
	 * Note: this is implemented based on the algorithm "count" of Table 2 in the paper by Kubat et al.
	// Note that there was a few problem in the algorithm in the paper.
	// I had to change > by < in :  ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
	// also the count was not correct so i had to change the way it counted the support a little bit
	// by using += instead of return.
	 * 
	 * @param s  the itemset
	 * @param root  the root of the subtree
	 * @param startFrom  the items to match starting from position j in s
	 * @return  the support as an integer
	 */
	private int count(int[] s, ItemsetTreeNode root, int[] prefix) {
		// the variable count will be used to count the support
		int count =0;
		// for each child of the root
		for(ItemsetTreeNode ci : root.childs){
			// if the first item of the itemset that we are looking for
			// is smaller than the first item of the child, we need to look
			// further in that tree.
			int[] ciprefix = append(prefix, ci.itemset);
			
			if(ciprefix[0]  <= s[0]){
				
				// if s is included in ci, add the support of ci to the current count.
				if(ArraysAlgos.includedIn(s, ciprefix)){
					count += ci.support;
				}else if(ciprefix[ciprefix.length -1] < s[s.length -1]){  
					// otherwise, if the last item of ci is smaller than
					// the last item of s, then  make a recursive call to explore
					// the subtree where ci is the root
					count += count(s, ci, ciprefix);
				}
			}
		}
		// return the total count
		return count;
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
	public HashTableIT getFrequentItemsetSubsuming(int[] s){
		// create a hash table to contain the itemsets to be more efficient
		// we set the default size of the internal array to 1000
		HashTableIT hash = new HashTableIT(1000);
		
		// create an hashset to store the items of the itemset
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}
		// call the method selective mining for finding the sets subsuming s
		selectiveMining(s, seti, root, hash, null);
		return hash;
	}

	/**
	 * This method finds itemsets subsuming a given itemset. It is a recursive method that
	 * scan a subtree of the itemset-tree. It stores itemsets found in an hashtable together
	 * with their support. 
	 * @param s  the itemset s
	 * @param seti  the items from the itemset s stored in a HashSet<Integer> for more efficiency for inclusion checking
	 * @param t  the root of the subtree
	 * @param hash  the hashtable for storing the result
	 */
	private void selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNode t, HashTableIT hash, int[] prefix) {
		// for all child nodes of the given root of the subtree
		for(ItemsetTreeNode ci : t.childs){
			int[] ciprefix = append(prefix, ci.itemset);
			
			// if the first item of s is smaller or equal to the
			// first item of the child
			if(ciprefix[0]  <= s[0]){
				// Check if s is included in ci
				if(ArraysAlgos.includedIn(s, ciprefix)){
					// if ci has not child, put s in the hashtable with 
					// the support of ci, and then
					// call recursive add.
					// Note: This part is not explained correctly in the paper, 
					// i had to figure it out by myself and fix it.
					if(ci.childs.size() ==0){
						hash.put(s, ci.support);
						recursiveAdd(s, seti, ciprefix, ci.support, hash, 0);
					}else{
						// otherwise recursively explore subtree with ci as root.
						selectiveMining(s, seti, ci, hash, ciprefix);
					}
					
				}
				else if(ciprefix[ciprefix.length -1] < s[s.length -1]){ 
					// else if the last item of ci is smaller than the last
					// item of s, we also need to recursively explore subtree 
					// with ci as root.
					selectiveMining(s, seti, ci, hash, ciprefix);
				}
			}
		}
	}

	/**
	 * Perform a recursive add (as based on the procedure presented in the paper by Kubat et al.)
	 * @param s  an itemset s
	 * @param seti   the items from the itemset s in a HashSet of integers
	 * @param ci     an itemset tree node ci
	 * @param cisupport  the support of the itemset associated to ci
	 * @param hash   an hashtable used to store itemset and their support
	 * @param pos   the current position in the itemset ci
	 */
	private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTableIT hash, int pos) {
		// if we have reached the end of ci, then stop
		if(pos >= ci.length){
			return;
		}
		// if the itemset i contain the item as position pos in ci
		if(!seti.contains(ci[pos])){
			// create a new itemset "newS"  by concatening the
			// item as position pos inn ci with the itemset s.
			
			// Note that the resulting itemset must be lexicographically ordered
			// so we copy the item one by one and check where the item at
			// position pos should be inserted.
			int[] newS = new int[s.length+1]; // create the new itemset
			int j=0;  // current position
			boolean added = false;  //indicate if we have added the item at pos already
			// for each item in s
			for(Integer item : s){
				// if added already or the current item is smaller than the one at pos
				if(added || item < ci[pos]){
					// we add the item from s
					newS[j++] = item;
				}else{
					// otherwise, we insert the item at position pos
					newS[j++] = ci[pos];
					newS[j++] = item;
					added = true;  // we set that variable to true to not insert it twice!
				}
			}
			// if the item at position pos was not yet added, that means
			// that he should be inserted in the last position because he his
			// greater than all other items
			if(j < s.length+1){
				newS[j++] = ci[pos];
			}
			// add the new itemset to the hashtable with the support of ci
			hash.put(newS, cisupport);
			
			// make a recursive call with the next position in ci with the new itemset
			recursiveAdd(newS, seti, ci, cisupport, hash, pos+1);
		}
		// make a recursive call with the next position in ci with itemset "S"
		recursiveAdd(s, seti, ci, cisupport, hash, pos+1);
	}

}
