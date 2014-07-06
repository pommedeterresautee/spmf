package ca.pfv.spmf.algorithms.frequentpatterns.apriori_HT;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.frequentpatterns.apriori_HT.ItemsetHashTree.LeafNode;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an  implementation of the Apriori algorithm that use an Hash-tree to
 * store candidates,  to calculate their support  and to generate candidates efficiently.
 * The other version (AlgoApriori) do not use a hash tree.
 *  <br/><br/>
 *  
 * The Apriori algorithm is described  in :
 * <br/><br/>
 *  
 * Agrawal R, Srikant R. "Fast Algorithms for Mining Association Rules", VLDB.
 * Sep 12-15 1994, Chile, 487-99,
 * <br/><br/>
 *  
 * The Apriori algorithm finds all the frequents itemsets and their support in a
 * transaction database.
 * <br/><br/>
 *  
 * Note that the performance of the Hash-Tree version of Apriori depends on the BRANCH COUNT value
 * in the class ItemsetHashTree.  In my test, I have used a value of 30 because it seems to provide
 * the best results. But other values could also be used (see Agrawal & Srikant for details).
 * To change the branch_count variable, see the ItemsetHashTree class (default is 30).
 * 
 * 
 * @see Itemset
 * @see AbstractOrderedItemsetsAdapter
 * @see ItemsetHashTree
 * @author Philippe Fournier-Viger
 */
public class AlgoAprioriHT {

	// the maximul level reached by Apriori 
	protected int k;

	// For statistics
	protected int totalCandidateCount = 0; // total number of candidates generated
	protected long startTimestamp;  // start time
	protected long endTimestamp;   // end time
	private int itemsetCount;   // number of itemsets found
	private int hash_tree_branch_count;  // the number of branches in the hash tree
	
	// the relative minimum support used to find itemsets
	private int minsupRelative;
	
	// an in-memory representation of the transaction database
	private List<int[]> database = null;

	// write to file
	BufferedWriter writer = null;

	/**
	 * Default constructor
	 */
	public AlgoAprioriHT() {
		
	}

	/**
	 * Run the Apriori-HT algorithm
	 * @param minsup the minimum support threshold
	 * @param input path to the input file
	 * @param output path to save the result to an output file
	 * @param hash_tree_branch_count  the number of child nodes for each node in the hash tree
	 * @throws IOException if an error while reading/writing files
	 */
	public void runAlgorithm(double minsup, String input, String output, int hash_tree_branch_count) throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// prepare object for writing the file
		writer = new BufferedWriter(new FileWriter(output));
		
		// reset statistics
		itemsetCount = 0;
		totalCandidateCount = 0;
		MemoryLogger.getInstance().reset();
		int transactionCount = 0;
		
		// save the parameter
		this.hash_tree_branch_count = hash_tree_branch_count;

		// structure to count the support of each item
		// Key: item   Value: support count
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>(); 
		
		// the database in memory (intially empty)
		database = new ArrayList<int[]>(); 
		
		// scan the database to load it into memory and count the support of each single item at the same time
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		 // for each line (transaction) of the input file until the end of file
		while (((line = reader.readLine()) != null)) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line into items
			String[] lineSplited = line.split(" ");
			
			// create an array to store the items
			int transaction[] = new int[lineSplited.length];
			
			// for each item in the current transaction
			for (int i=0; i< lineSplited.length; i++) {
				// convert to integer
				Integer item = Integer.parseInt(lineSplited[i]);
				// add the item to the transaction
				transaction[i] = item;
				
				// increase the support count of the item
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}
			// add transaction to the database
			database.add(transaction);
			// increase the transaction count
			transactionCount++;
		} 
		// close the input file
		reader.close();
		
		// convert absolute minimum support to a relative minimum support
		// by multiplying by the database size
		this.minsupRelative = (int) Math.ceil(minsup * transactionCount);
		
//		System.out.println("database size = " +database.size() + "  minsuprel = " + minsupRelative);
		
		// Set variable k=1 because we start with itemsets of size 1
		k = 1;
		
		// Create the list of all frequent items of size 1
		List<Integer> frequent1 = new ArrayList<Integer>();
		// For each item
		for(Entry<Integer, Integer> entry : mapItemCount.entrySet()){
			// if its support is higher than the support
			if(entry.getValue() >= minsupRelative){
				// keep the item into memory for generating itemsets of size 2
				frequent1.add(entry.getKey());
				// and also save it to the output file
				saveItemsetToFile(entry.getKey(), entry.getValue());
			}
		}
		mapItemCount = null;  // we don't need it anymore
		
		// Sort the list of frequent items of size 1 by lexical order because
		// Apriori need itemset sorted by a total order.
		Collections.sort(frequent1, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});
		
		// if no frequent item, we stop there!
		if(frequent1.size() == 0){
			return;
		}
		
		// increase the number of candidates
		totalCandidateCount += frequent1.size();
		
		// Now, the algorithm recursively generates frequent itemsets of size K
		// by using frequent itemsets of size K-1 until no more
		// candidates can be generated.
		k = 2;
		// While the level is not empty
		int previousItemsetCount = itemsetCount;
		
		// Create an hashtree for storing candidates for efficient support counting
		ItemsetHashTree candidatesK = null;
		do{
			//check the memory usage
			MemoryLogger.getInstance().checkMemory();
			
			// Generate candidates of size K
			if(k ==2){
				// if K=2, use an optimized version of candidate generation
				candidatesK = generateCandidate2(frequent1);
			}else{
				// Otherwise use the regular candidate generation procedure
				candidatesK = generateCandidateSizeK(candidatesK, k);
			}
			
			// if no candidates were generated, we stop the algorithm
			if(candidatesK.candidateCount ==0 ){
				break;
			}
			
			// we keep the total number of candidates generated until now
			// for statistics purposes
			totalCandidateCount += candidatesK.candidateCount;

			// We scan the database one time to calculate the support
			// of each candidates and keep those with higher support.
			// This is done efficiently because the candidates are stored in a hash-tree.
			for(int[] transaction: database){
				// NEW OPTIMIZATION 2013: Skip transactions shorter than k!
				if(transaction.length >= k) {
					candidatesK.updateSupportCount(transaction);
				}
				// END OF NEW OPTIMIZATION
			}

			// We next save to file all the candidates that have a support 
			// higher than the minsup threshold and remove those who does'nt.
			
			// for each leaf node in the hash-tree
			for(LeafNode node  = candidatesK.lastInsertedNode;node != null; node = node.nextLeafNode){
				// for each list of candidate itemsets stored in that node
				for(List<Itemset> listCandidate: node.candidates){
					// if the list is not null
					if(listCandidate != null){
						// for each candidate itemset
						for(int i=0; i<listCandidate.size(); i++){
							Itemset candidate = listCandidate.get(i);
							// if enough support, save the itemset
							if (candidate.getAbsoluteSupport() >= minsupRelative) {
								saveItemsetToFile(candidate);
							}else{
								// otherwise remove it
								listCandidate.remove(i);  
							}
						}
					}	
				}
				
			}
			// continue recursively if some new itemsets were generated
			// during the current iteration
			k++;
		}while(previousItemsetCount != itemsetCount);

		// save endtime
		endTimestamp = System.currentTimeMillis();
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// close the file
		writer.close();
	}
	
	/**
	 * Method to generate candidates of size k, where k > 2
	 * @param candidatesK_1 the candidates of size k-1
	 * @param k  k
	 * @return the candidates of size k, stored in an hash-tree
	 */
	private ItemsetHashTree generateCandidateSizeK(ItemsetHashTree candidatesK_1, int k) {
		// create the hash-tree to store the candidates of size K
		ItemsetHashTree newCandidates = new ItemsetHashTree(k, hash_tree_branch_count);
		
		// The generation will be done by comparing the leaves of the hash-tree
		// containing the itemsets of size k-1.
		// To generate an itemsets, we need to use two itemsets from the same leaf node.
		
		// For each leaf node
		for(LeafNode node  = candidatesK_1.lastInsertedNode; node != null; node = node.nextLeafNode){
			List<Itemset> subgroups [] = node.candidates;
			// For each sets of itemsets in this node
			for(int i=0; i< subgroups.length; i++){
				if(subgroups[i] == null){
					continue;
				}
				// For each sets of itemsets in this node
				for(int j=i; j< subgroups.length; j++){
					if(subgroups[j] == null){
						continue;
					}
					// try to use these list of itemsets to generate candidates.
					generate(subgroups[i], subgroups[j], candidatesK_1, newCandidates);
				}
			}
		}
		return newCandidates; 
	}

	/**
	 * Method to generate candidates of size k from two list of itemsets of size k-1
	 * @param list1 the first list
	 * @param list2 the second list (may be equal to the first list)
	 * @param candidatesK the hash-tree containing the candidates of size k-1
	 * @param newCandidates the hash-tree to store the candidates of size k
	 */
	private void generate(List<Itemset> list1, List<Itemset> list2,
			ItemsetHashTree candidatesK_1, ItemsetHashTree newCandidates) {
	// For each itemset I1 and I2 of lists
		loop1: for (int i = 0; i < list1.size(); i++) {
			int[] itemset1 = list1.get(i).itemset;
			
			// if the two lists are the same, we will start from i+1 in the second list
			// to avoid comparing pairs of itemsets twice.
			int j = (list1 == list2)?  i+1 : 0;
			// For each itemset in list 2
			loop2: for (; j < list2.size(); j++) {
				int[] itemset2 = list2.get(j).itemset;

				// we compare items of itemset1 and itemset2.
				// If they have all the same k-1 items and the last item of
				// itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a
				// candidate
				for (int k = 0; k < itemset1.length; k++) {
					// if k is not the last item
					if (k != itemset1.length - 1) {
						if (itemset2[k] > itemset1[k]) {  
							continue loop1; // we continue searching
						} 
						if (itemset1[k] > itemset2[k]) {  
							continue loop2; // we continue searching
						} 
					}
					
				}
				// If we are here, it is because the two itemsets share
				// the same k-1 first item. Therefore, we can generate
				// a new candidate.
				// There is two cases depending if the last item of itemset1 is smaller 
				// or greater than the last item of itemset2. We do this just to make
				// sure that we add items in the new candidate according to the lexicographical order
				int newItemset[] = new int[itemset1.length+1];
				if(itemset2[itemset2.length -1] < itemset1[itemset1.length -1]){
					// Create a new candidate by combining itemset1 and itemset2
					System.arraycopy(itemset2, 0, newItemset, 0, itemset2.length);
					newItemset[itemset1.length] = itemset1[itemset1.length -1];
				}else{
					// Create a new candidate by combining itemset1 and itemset2
					System.arraycopy(itemset1, 0, newItemset, 0, itemset1.length);
					newItemset[itemset1.length] = itemset2[itemset2.length -1];
				}
				
				// The candidate is tested to see if its subsets of size k-1 are
				// included in level k-1 (they are frequent).
				if (allSubsetsOfSizeK_1AreFrequent(newItemset, candidatesK_1)) {
					// If yes, we add the candidate to the hash-tree
					newCandidates.insertCandidateItemset(new Itemset(newItemset));
				}
			}
		}
	}

	/**
	 * Method for generating the candidate itemsets of size 2.
	 * @param frequent1 The frequent itemsets of size 1
	 * @return the candidate itemsets of size 2 stored in a hash-tree.
	 */
	private ItemsetHashTree generateCandidate2(List<Integer> frequent1) {
		// we create an hash-tree to store the candidates
		ItemsetHashTree candidates = new ItemsetHashTree(2, hash_tree_branch_count);
		
		// For each pair of frequent items 
		for (int i = 0; i < frequent1.size(); i++) {
			Integer item1 = frequent1.get(i);
			for (int j = i + 1; j < frequent1.size(); j++) {
				Integer item2 = frequent1.get(j);
				// Create a new candidate by combining the two items and insert
				// it in the hash-tree
				candidates.insertCandidateItemset(new Itemset(new int []{item1, item2}));
			}
		}
		return candidates; // return the hash-tree
	}

	/**
	 * This method checks if all the subsets of an items are frequent (i.e. if
	 * all the subsets are in the hash-tree of the previous level)
	 * @param itemset the itemset
	 * @param hashtreeCandidatesK_1  the hash-tree of the previous level
	 * @return
	 */
	protected boolean allSubsetsOfSizeK_1AreFrequent(int[] itemset, ItemsetHashTree hashtreeCandidatesK_1) {
		// generate all subsets by always each item from the candidate, one by one
		for(int posRemoved=0; posRemoved< itemset.length; posRemoved++){

			if(hashtreeCandidatesK_1.isInTheTree(itemset, posRemoved) == false){  // if we did not find it, that means that candidate is not a frequent itemset because
				// at least one of its subsets does not appear in level k-1.
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Method to save a frequent itemset to file
	 * @param itemset
	 * @throws IOException
	 */
	void saveItemsetToFile(Itemset itemset) throws IOException {
		writer.write(itemset.toString() + " #SUP: "
				+ itemset.getAbsoluteSupport());
		writer.newLine();
		itemsetCount++;
	}
	
	/**
	 * Method to save a frequent itemset of size 1 to file.
	 * @param item the item contained in the itemset.
	 * @param support the support of the item.
	 * @throws IOException if an error happens while writing to file.
	 */
	void saveItemsetToFile(Integer item, Integer support) throws IOException {
		writer.write(item + " #SUP: " + support);
		writer.newLine();
		itemsetCount++;
	}
	


	/**
	 * Method to print statistics about the execution of the algorithm.
	 */
	public void printStats() {
		System.out.println("=============  APRIORI-HT - STATS =============");
		System.out.println(" Candidates count : " + totalCandidateCount);
		System.out.println(" The algorithm stopped at size " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println("===================================================");
	}
	
	//
//	private String toString(int[] newItemset) {
//		StringBuffer temp = new StringBuffer();
//		for(Integer integer: newItemset){
//			temp.append(integer);
//			temp.append(" ");
//		}
//		return temp.toString();
//	}
}
