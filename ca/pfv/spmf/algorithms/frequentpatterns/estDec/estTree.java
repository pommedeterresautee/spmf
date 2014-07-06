package ca.pfv.spmf.algorithms.frequentpatterns.estDec;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * This is an implementation of a estTree. <br/>
 * <br/>
 * 
 * This implementation was made by Azadeh Soltani <br/>
 * <br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf). <br/>
 * <br/>
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. <br/>
 * <br/>
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details. <br/>
 * <br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see Algo_estDec
 * @see estNode
 * @author Azadeh Soltani
 */
public class estTree {

	double N;  // |Dk|
	double d; // decay rate
	int k; // current tid
	
	// itemset count
	int patternCount =0;
	
	// Hashtable for storing frequent patterns into memory
	Hashtable<List<Integer>, Double> patterns;
	
	// writer used if result is saved to file
	BufferedWriter writer;
	
	double minsup;
	double minsig;
	
	estNode root; // tree root

	/**
	 * Constructor
	 */
	estTree(double mins) {
		// default decay rate
		setDecayRate(2, 10000);
		
		N = 0;
		k = 0;
		minsup = mins / 100;
		minsig = 0.1 * minsup;
		
		root = new estNode(); // null node
	}
	
	/**
	 * Set the decay rate
	 * @param b  decay base 
	 * @param h decay-base life
	 */
	void setDecayRate(double b, double h) {
		d = Math.pow(b, -1.0 / h);
	}

	/**
	 * Method for updating parameters (Phase 1: parameter updating phase)
	 * @param transaction
	 */
	void updateParams(int[] transaction) {
		// |Dk| = |Dk| x d + 1
		N = N * d + 1;  
		k++;
		updateNodes(root, transaction, 0);
	}

	/********************************************************************
	 * Recursive method for updating the counters of itemsets that 
	 * belong to a given transaction  (Phase 2: count updating phase).
	 * 
	 * @param currentNode a tree node
	 * @param transaction the transaction for updating
	 * @param ind depth of the branch ending at the current node 
	 ********************************************************************/
	void updateNodes(estNode currentNode, int[] transaction, int ind) {
		// stop recursion
		if (ind >= transaction.length)
			return;

		// get item at position "ind" in the transaction
		Integer item = transaction[ind];
		
		// look if there is a node for this item in the est-Tree
		estNode child = currentNode.getChildWithID(item);
		if (child != null) {
			// update count of the node
			child.update(k, 1, d);
			// if the support is enough
			if (child.computeSupport(N) >= minsig)
				updateNodes(child, transaction, ind + 1);
			else {
				/*  PFV:  WHY NOT REMOVE?
				 * currentNode.children.remove(child); else
				 */
			}

		}
		updateNodes(currentNode, transaction, ind + 1);
	}

	/********************************************************************
	 * Method for inserting a new item to the tree (itemset of size 1).
	 * 
	 * @param it  the item
	 ********************************************************************/
	void insertItem(Integer it) {
		// create the node with a count of 0
		double c = 0;// (getN(k-1)*minsig)*d+1;
		root.children.add(new estNode(it, c, k));
	}

	/********************************************************************
	 * Method for inserting new possible frequent itemsets to the tree based on
	 * the new transaction (Phase 3 : Delayed Insertion Phase).
	 * 
	 * @param transaction the new transaction
	 ********************************************************************/

	void insertItemset(int[] transaction) {
		 // create a new transaction
		List<Integer> transaction2 = new ArrayList<Integer>();
		// add each item from the given transaction that has enough support to the new transaction
		for (int it : transaction) {
			estNode child = root.getChildWithID(it);
			if (child == null)
				insertItem(it);
			else if (child.computeSupport(N) >= minsig)
				transaction2.add(it);
		}
		// insert the new transaction
		insert_n_itemsets(root, transaction2, 0, new int[0]);
	}

	/********************************************************************
	 * Method for calculating |D|k
	 * 
	 * @param n
	 ********************************************************************/
	double getN(int k) {
		return (1 - Math.pow(d, k)) / (1 - d);
	}

	/********************************************************************
	 * Method for obtaining the count of an itemset while ignoring
	 * an item at a given position.
	 * 
	 * @param itemset the itemset
	 * @param pos the index of the item to be ignored in the itemset
	 ********************************************************************/
	double getCountOfItemsetWithoutItemAtPosition(int[] itemset, int pos) {
		// stop recursion
		estNode currentNode = root;
		for (int i=0; i< itemset.length; i++) {
			if(i != pos) {
				int item = itemset[i];
				estNode child = currentNode.getChildWithID(item);
				if (child == null)
					return 0;
				currentNode = child;
			}
		}
		return currentNode.counter;
	}

	/********************************************************************
	 * Method for estimating the count of n-itemset from its n-1 subsets
	 * 
	 * @param currentNode
	 *            , transaction, index
	 ********************************************************************/
	double estimateCount(int[] itemset) {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < itemset.length; ++i) {
			double c = getCountOfItemsetWithoutItemAtPosition(itemset, i);
			if (c < min)
				min = c;
		}
		int n = itemset.length;
		double C_upper = minsig * getN(k - (n - 1)) * Math.pow(d, n - 1)
				+ (1 - Math.pow(d, n - 1)) / (1 - d);
		if (min > C_upper)
			min = C_upper;
		return min;
	}

	/********************************************************************
	 * Recursive method for inserting all itemsets corresponding to the a transaction
	 * 
	 * @param currentNode a tree node
	 * @param transaction the transaction
	 * @param the depth of the current node with respect to the root of the tree
	 * @param itemset
	 ********************************************************************/

	public void insert_n_itemsets(estNode currentNode,
			List<Integer> transaction, int ind, int[] itemset) {

		// stop recursion
		if (ind >= transaction.size())
			return;

		Integer item = transaction.get(ind);
		// look if there is a node already in the est-Tree 
		estNode child = currentNode.getChildWithID(item);
		
		// Itemset2 := itemset U item
		int[] itemset2 = new int[itemset.length+1];
		System.arraycopy(itemset, 0, itemset2, 0, itemset.length);
		itemset2[itemset2.length-1] = item;
		
		if (child == null) {
			double c = estimateCount(itemset2);
			// if its estimated support is greater than minsig insert a new node
			// with itemId=item counter=c, tid=k
			if (c / N >= minsig) {
				child = new estNode(item, c, k);
				currentNode.children.add(child);
			}
		}// if child
		else {
			if (child.counter / N < minsig) {
				// if its support is less than minsig delete the node
				if (currentNode.itemID != -1)
					currentNode.children.remove(currentNode
							.getChildIndexWithID(item));
			} else {
				// if its support is greater than minsig continue the recursion
				// with this subtree
				insert_n_itemsets(child, transaction, ind + 1, itemset2);
			}
		}

		insert_n_itemsets(currentNode, transaction, ind + 1, itemset);
	}

	/********************************************************************
	 * Method for force pruning
	 * 
	 * @param root  t
	 ********************************************************************/
	void forcePruning(estNode root) {
		for (int i = 0; i < root.children.size(); ++i) {
			estNode node = root.children.get(i);
			node.update(k, 0, d);
			if (node.computeSupport(N) < minsig && root.itemID != -1)
				root.children.remove(i--);
			else
				forcePruning(node);
		}
	}

	/********************************************************************
	 * Recursive method for finding frequent patterns.
	 * @param root root of the current subtree
	 * @param pattern current pattern
	 * @throws IOException 
	 ********************************************************************/
	void patternMining(estNode root, List<Integer> pattern) throws IOException {
		for (estNode node : root.children) {
			List<Integer> patt2 = new ArrayList<Integer>(pattern);
			patt2.add(node.itemID);
			node.update(k, 0, d);
			double s = node.computeSupport(N);
			if (s > minsup) {
				patternCount++;
				// if store into file
				if(patterns == null) {
					writeItemset(patt2, s);
				}else { 
					// else, store into memory
					patterns.put(new ArrayList<Integer>(patt2), s);
				}
				patternMining(node, patt2);
			}
		}
	}
	
	/********************************************************************
	 * Method for finding frequent patterns and save them into memory
	 * @param root root of the current subtree
	 ********************************************************************/
	Hashtable<List<Integer>, Double> patternMining_saveToMemory() throws IOException {
		// Initialize hashtable for storing frequent patterns into memory
		patterns = new Hashtable<List<Integer>, Double>(); 
		patternCount = 0;
		
		// recursive method for pattern mining
		patternMining(root, new ArrayList<Integer>());
		
		return patterns; // return patterns found
	}
	
	/********************************************************************
	 * Method for finding frequent patterns and save them into file
	 * @param root the root of the curent subtree
	 * @param outputPath the output file path
	 * @throws IOException 
	 ********************************************************************/
	void patternMining_saveToFile(String outputPath) throws IOException {
		patterns = null; // because we will not save into memory
		writer = new BufferedWriter(new FileWriter(outputPath));
		patternCount = 0;

		// recursive method for pattern mining
		patternMining(root, new ArrayList<Integer>());
		
		writer.close();
	}

	/********************************************************************
	 * Method for writing frequent patterns in output file
	 * @param itemset the pattern to be saved
	 * @param support a double value
	 ********************************************************************/
	void writeItemset(List<Integer> itemset, double support) throws IOException {
		StringBuffer buffer = new StringBuffer();
		
		// for each item
		for (Integer item : itemset) {
			
			// write the item
			buffer.append(item);
			buffer.append(" ");
		}
		// write the support
		buffer.append("#SUP: ");
		buffer.append(support);
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Get the last transaction id
	 * @return the transaction id (integer)
	 */
	int getK() {
		return k;
	}


}// class
