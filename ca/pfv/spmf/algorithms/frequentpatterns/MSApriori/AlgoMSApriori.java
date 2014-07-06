package ca.pfv.spmf.algorithms.frequentpatterns.MSApriori;
/* This file is copyright (c) 2008-2013 Azadeh Soltani, Philippe Fournier-Viger
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the MSApriori algorithm as described by :<br/><br/>
 * 
 * Bing Liu et al. (1999). Mining Association Rules with Multiple Minimum Supports, Proceedings of KDD 1999.
 * <br/><br/>
 * 
 * This implementation was made by AZADEH SOLTANI based on the Apriori implementation by Philippe Fournier-Viger
 * 
 * @see Itemset
 * @author Azadeh Soltani, Philippe Fournier-Viger
 */
public class AlgoMSApriori {

	// the current level in the apriori generation (itemsets of size k)
	protected int k;
	
	// the array of MIS value where the position i indicate the MIS of item with ID i.
	int MIS[];

	// for statistics
	protected long startTimestamp;  // start time of latest execution
	protected long endTimestamp;   // end time of latest execution
	private int itemsetCount;  // number of frequent itemsets generated

	// the LS value as an integer
	private int LSRelative;

	// an in-memory representation of the transaction database
	// where position i represents the ith transaction as an integer array
	private List<Integer[]> database = null;
	
	// the  comparator that is used to sort items by MIS values
	final Comparator<Integer> itemComparator;

	// object to write the output file
	BufferedWriter writer = null;

	/**
	 * Constructor
	 */
	public AlgoMSApriori() {
		itemComparator = new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				// first compare by MIS values
				int compare  = MIS[o1] - MIS[o2];
				//if the same MIS, we check the lexical ordering!
				if(compare ==0){ // 
					return (o1 - o2);
				}
				// otherwise use MIS value
				return compare;
			}
		};
	}

	/**
	 * Run the algorithm
	 * @param input an input file containing a transaction database
	 * @param output an output file path for writing the result
	 * @param beta  the parameter Beta for generating MIS values for all items (see paper)
	 * @param LS    the parameter LS for generating MIS values for all items (see paper)
	 * @throws IOException exception if error while writing the output file
	 */
	public void runAlgorithm(String input, String output,
			double beta, double LS) throws IOException { 
		// record the start time
		startTimestamp = System.currentTimeMillis();
		// Prepare for writing the output file
		writer = new BufferedWriter(new FileWriter(output));
		// Reset the number of itemset found to 0
		itemsetCount = 0;
		// reseter the utility for recording the max memory usage
		MemoryLogger.getInstance().reset();
		
		// variable to store the maximum item id in the database
		int maxItemID = -1; // pfv

		// the number of transaction in the database
		int transactionCount = 0;
		// map to count the support of each item
		// key: item  value: support of the item
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>(); 
		
		 // the database in memory (intially empty)
		database = new ArrayList<Integer[]>();

		// scan the database to load it into memory and count the support of
		// each single item at the same time
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of the file
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
			// create an array of integer to store the transaction in memory
			Integer transaction[] = new Integer[lineSplited.length];

			// for each item in the transaction
			for (int i = 0; i < lineSplited.length; i++) { 
				// convert the item to integer
				Integer item = Integer.parseInt(lineSplited[i]);
				// add it to the in memory transaction
				transaction[i] = item;
				// increase the support count of the item
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
					// if this is the largest item ID encountered, then remember it
					if (item > maxItemID) {
						maxItemID = item;
					}
				} else {
					mapItemCount.put(item, ++count);
				}
			}
			// add the transaction to the in memory database
			database.add(transaction);
			// increase the transaction count
			transactionCount++;
		}
		// close the input file
		reader.close(); 

		// initialize array for storing the MIS values for each item
		MIS = new int[maxItemID + 1];

		// transform the LS value to a relative value by multiplying by
		// the number of transactions
		this.LSRelative = (int) Math.ceil(LS * transactionCount);   // pfv

		// Start generating frequent itemsets of size 1
		k = 1;

		// Create a set M to store all items
		List<Integer> M = new ArrayList<Integer>();
		// for each item
		for (Entry<Integer, Integer> entry : mapItemCount.entrySet()) {
			// add the item to M
			M.add(entry.getKey());
			// calculate the MIS value for that item by using the formula described in the paper
			MIS[entry.getKey()] = (int) (beta * entry.getValue());
			// if the MIS value for that item is lower than LS, then set it to LS 
			if (MIS[entry.getKey()] < LSRelative){
				MIS[entry.getKey()] = LSRelative;  	
			}
			//if the support of the item is higher than its MIS value
			if (entry.getValue() >= MIS[entry.getKey()]) {
				// save the item to the output file with its support
				saveItemsetToFile(entry.getKey(), entry.getValue());
			}
		}
		// sort the list of items by MIS order
		Collections.sort(M, itemComparator); //pfv

		// if no frequent item was found, we stop there!
		if (itemsetCount == 0) {
			return;
		}

		// create the set F (as described in the paper)
		List<Integer> F = new ArrayList<Integer>();
		// this variable will be used to store the smallest MIS value higher 
		// such that the corresponding item has a support no less than it
		double minMIS = -1;                                     
		int i;
		// for each item
		for (i = 0; i < M.size(); i++) {
			Integer item = M.get(i);
			//if its support is higher or = to its MIS value
			if (mapItemCount.get(item) >= MIS[item]) {
				// add it to F
				F.add(item);
				// set the min MIS to this value 
				minMIS = MIS[item];
				break;  // break
			}// if
		}// for
		// for each folowing item in M
		for (i++; i < M.size(); i++) {
			Integer item = M.get(i);
			// if it has a support higher or equal to his MIS value
			if (mapItemCount.get(item) >= minMIS){
				// add it to F
				F.add(item);
			}
		}// forj
		
		// sort the database  by  MIS order
		for (Integer[] transaction : database) {   
			Arrays.sort(transaction, itemComparator);  
		}
		
		// Now, the algorithm will discover itemset of size k > 1 starting from k=2
		List<Itemset> level = null;
		k = 2;
		// Generate candidates and test them for k>1 by inscreasing k at each iteration
		// until no candidates can be generated
		do {
			// check the memory usage
			MemoryLogger.getInstance().checkMemory();
			
			// Generate candidates of size K
			List<Itemset> candidatesK;

			// Generate candidates
			if (k == 2) {
				// if k=2 we use an optimization for candidate generation
				candidatesK = generateCandidate2(F, mapItemCount);
			} else {
				// otherwise, we use the general procedure for candidate generation
				candidatesK = generateCandidateSizeK(level);
			}

			// We scan the database one time to calculate the support
			// of each candidates and keep those with higher suport.
			
			// for each transaction
			for (Integer[] transaction : database) {
				// for each candidate
				loopCand: for (Itemset candidate : candidatesK) {
					// We will check if the candidate is contained in the transaction by
					// looking for each item one by one starting from pos =0.
					int pos = 0;
					// for each item in the transaction
					for (int item : transaction) {
						// if we have found the item at position pos
						if (item == candidate.get(pos)) {
							// search the next item from the candidate
							pos++;
							// if all items have been found
							if (pos == candidate.size()) {
								// then increase the support count of the candidate
								candidate.increaseTransactionCount();
								continue loopCand;
							}
						// because of the total order, if the item at position pos is larger 
						// than the current item in the transaction we can stop checking
						// this candidate because it will not be contained in the transaction.
						} else if (itemComparator.compare(item, candidate.get(pos)) > 0){   // pfv 
							continue loopCand;
						}
					}
				}
			}

			// We build the level k+1 with all the candidates that have
			// a support higher than MIS[0]
			level = new ArrayList<Itemset>();
			// for each candidate
			for (Itemset candidate : candidatesK) {
				// if its support is higher than the MIS of the first item 
				// (because they are sorted by MIS order)
				if (candidate.getAbsoluteSupport() >= MIS[candidate.get(0)]) {
					// add it to the next level of candidate
					level.add(candidate);
					// save the itemset to the file
					saveItemsetToFile(candidate);
				}
			}
			k++;
		} while (level.isEmpty() == false);

		// record the end time
		endTimestamp = System.currentTimeMillis();
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// close the output file
		writer.close();
	}

	/**
	 * Generate candidates of size 2 by using frequent itemsets of size 1.
	 * @param frequent1  frequent itemsets of size 1 (single items)
	 * @param mapItemCount a map indicating the support of each item (key: item, value: support)
	 * @return  the set of candidates of size 2
	 */
	private List<Itemset> generateCandidate2(List<Integer> frequent1, Map<Integer, Integer> mapItemCount) {
		// list to store the candidates
		List<Itemset> candidates = new ArrayList<Itemset>();

		// For each itemset I1 and I2 of level k-1
		for (int i = 0; i < frequent1.size(); i++) {
			Integer item1 = frequent1.get(i);		
			for (int j = i + 1; j < frequent1.size(); j++) {
				Integer item2 = frequent1.get(j);

				// Create a new candidate by combining itemset1 and itemset2
				candidates.add(new Itemset(new int[] { item1, item2 }));
			}
		}
		// return the set of candidates
		return candidates;
	}

	/**
	 * Generate candidates of size K by using frequent itemsets of size K-1.
	 * @return  the set of candidates of size K
	 */	
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1) {
		// list to store the candidates generated
		List<Itemset> candidates = new ArrayList<Itemset>();

		// For each itemset I1 and I2 of level k-1
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			int[] itemset1 = levelK_1.get(i).getItems();
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				int[] itemset2 = levelK_1.get(j).getItems();

				// we compare items of itemset1 and itemset2.
				// If they have all the same k-1 items and the last item of
				// itemset1 is smaller than the last item of itemset2, we will combine them to generate a
				// candidate

				// for each item in itemset1 and itemset2 at position k
				for (int k = 0; k < itemset1.length; k++) {
					// if it is the last position 
					if (k == itemset1.length - 1) {
						// the item  from itemset1 should be smaller (lexical
						// order)
						// and different from the one of itemset2
						if(itemComparator.compare(itemset1[k], itemset2[k]) > 0){ 
							// if not then continue the loop
							continue loop1;
						}
					}
					// if they are not the last items, and
					else if (itemset1[k] != itemset2[k]){
						// the item from itemset 1 is smaller than the one from itemset2
						if(itemComparator.compare(itemset1[k], itemset2[k]) < 0){  // pfv
							continue loop2; // we continue searching
						} // the item from itemset 1 is larger than the one from itemset2
						else if (itemComparator.compare(itemset1[k], itemset2[k]) > 0) { // pfv
							continue loop1; // we stop searching: because of MIS order
						}
					}
				}
				// Create a new candidate by combining itemset1 and itemset2
				int newItemset[] = new int[itemset1.length + 1];
				System.arraycopy(itemset1, 0, newItemset, 0, itemset1.length);
				newItemset[itemset1.length] = itemset2[itemset2.length - 1];

				// The candidate is tested to see if its subsets of size k-1 are
				// included in level k-1 (to check if they are frequent).
				if (allSubsetsOfSizeK_1AreFrequent(newItemset, levelK_1)) {
					// if yes, then add to candidates
					candidates.add(new Itemset(newItemset));
				}
			}
		}
		// return candidates
		return candidates;
	}

	// --------------------------------------------------------------------------------------------
	protected boolean allSubsetsOfSizeK_1AreFrequent(int[] c, List<Itemset> levelK_1) {
		// generate all subsets by always each item from the candidate, one by
		// one
		for (int posRemoved = 0; posRemoved < c.length; posRemoved++) {
			// az ******************************
			// if it does not contain first item of candidate and
			// MIS(c[0])!=MIS(c[1]) there is no need to check
			
			if ((posRemoved == 0) && MIS[c[0]] != MIS[c[1]]) {
				continue;
			}
			// end az******************************
			
			// the binary search
			// perform a binary search to check if the subset appears in level
			// k-1.
			int first = 0;
			int last = levelK_1.size() - 1;
			
			boolean found = false;
			
			 // the binary search
	        while( first <= last )
	        {
	        	int middle = ( first + last ) >>> 1; // divide by 2

	            if(sameAs(levelK_1.get(middle), c, posRemoved)  < 0 ){
	            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
	            }
	            else if(sameAs(levelK_1.get(middle), c, posRemoved)  > 0 ){
	            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
	            }
	            else{
	            	found = true; //  we have found it so we stop
	                break;
	            }
	        }

	        if (found == false) { // if we did not find it, that means that
									// candidate is not a frequent itemset
									// because
				// at least one of its subsets does not appear in level k-1.
				return false;
			}
		}
		return true;
	}

	/**
	 * Method to check if two itemsets are equals
	 * @param itemset the first itemset
	 * @param candidate the second itemset
	 * @param postRemoved a position that should be ignored from itemset "candidate" for the comparison
	 * @return 0 if they are the same, <0 if "itemset" is smaller than candidate according to the MIS order, otherwise >0
	 */
	private int sameAs(Itemset itemset, int[] candidate, int posRemoved) {
		// j will be position of the item that we are searching from "candidate"
		// in "itemset"
		int j = 0;
		// for each item in "itemset"
		for (int i = 0; i < itemset.size(); i++) {
			// if it is the position that should be ignored, then skip it
			if (j == posRemoved) {
				j++;
			}
			// if we have found the item at position j
			if (itemset.get(i) == candidate[j]) {
				// then we will search for the next one
				j++;
			}else{
				// they are different so use the comparator to compare the two
				// items according to the MIS order
				return itemComparator.compare(itemset.get(i), candidate[j]);  // pfv
			}
		}
		// they are the same!
		return 0;
	}

	/**
	 * 	Save an itemset to the output file
	 */
	private void saveItemsetToFile(Itemset itemset) throws IOException {
		// write the itemset with its support count
		writer.write(itemset.toString() + " #SUP: "	+ itemset.getAbsoluteSupport());
		writer.newLine();
		// increase the number of itemsets found
		itemsetCount++;
	}

	/**
	 * 	Save a frequent item to the output file
	 */
	private void saveItemsetToFile(Integer item, Integer support)
			throws IOException {
		// write the item with its support
		writer.write(item + " #SUP: " + support);
		writer.newLine();
		// increase the number of itemsets found
		itemsetCount++;
	}

	/**
	 * Print statistics about the latest execution of the algorithm to System.out.
	 */
	public void printStats() {
	 
		System.out.println("=============  MSAPRIORI - STATS =============");
		System.out.println(" The algorithm stopped at level " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}
}
