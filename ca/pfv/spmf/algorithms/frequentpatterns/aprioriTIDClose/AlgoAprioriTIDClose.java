package ca.pfv.spmf.algorithms.frequentpatterns.aprioriTIDClose;
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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the AprioriTID algorithm transformed to mine
 * only frequent closed itemsets as proposed by Pasquier (1999), rather than all
 * frequent itemsets.<br/><br/>
 * 
 * AprioriTID was originally proposed in :<br/><br/>
 * 
 * Agrawal R, Srikant R. "Fast Algorithms for Mining Association Rules", VLDB.
 * Sep 12-15 1994, Chile, 487-99,<br/><br/>
 * 
 * Modifying Apriori to mine closed itemsets was proposed in: <br/><br/>
 * 
 * Pasquier, N., Bastide, Y., Taouil, R., & Lakhal, L. (1999). 
 * Discovering frequent closed itemsets for association rules. 
 * In Database Theory—ICDT’99 (pp. 398-416). Springer Berlin Heidelberg.<br/><br/>
 * 
 * This implementation can save the result to a file or keep
 * it into memory if no output path is provided to the runAlgorithm() method.
 * 
 * @see Itemset
 * @see Itemsets
 * @author Philippe Fournier-Viger
 */
public class AlgoAprioriTIDClose {

	// object for writing to file if the user choose to write to a file
	BufferedWriter writer = null;
	
	// variable to store the result if the user choose to save to memory instead of a file
	protected Itemsets patterns = null;

	// the number of transactions
	private int databaseSize = 0;
	
	// the current level
	protected int k; 

	// variables for counting support of items
	Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();

	// the minimum support threshold
	int minSuppRelative;

	// Special parameter to set the maximum size of itemsets to be discovered
	int maxItemsetSize = Integer.MAX_VALUE;

	long startTimestamp = 0; // start time of latest execution
	long endTimestamp = 0; // end time of latest execution
	
	int itemsetCount = 0; // number of closed itemset found

	/**
	 * Default constructor
	 */
	public AlgoAprioriTIDClose() {
		
	}

	/**
	 * Run the algorithm
	 * @param minsupp the minsup threshold
	 * @param outputFile an output file path, if the result should be saved otherwise
	 *    leave it null and this method will keep the result into memory and return it.
	 * @return the set of itemsets found if the user chose to save the result to memory
	 * @throws IOException  exception if error writing the output file
	 */
	public Itemsets runAlgorithm(TransactionDatabase database, double minsupp, String outputFile) throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// reset number of itemsets found
		itemsetCount = 0;
		
		// if the user want to keep the result into memory
		if(outputFile == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT CLOSED ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(outputFile)); 
		}

		this.minSuppRelative = (int) Math.ceil(minsupp * database.size());
		if (this.minSuppRelative == 0) { // protection
			this.minSuppRelative = 1;
		}

		// (1) count the tid set of each item in the database in one database
		// pass
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); 
		// key : item   value: tidset of the item 

		// for each transaction
		for (int j = 0; j < database.getTransactions().size(); j++) {
			List<Integer> transaction = database.getTransactions().get(j);
			// for each item in the transaction
			for (int i = 0; i < transaction.size(); i++) {
				// update the tidset of the item
				Set<Integer> ids = mapItemTIDS.get(transaction.get(i));
				if (ids == null) {
					ids = new HashSet<Integer>();
					mapItemTIDS.put(transaction.get(i), ids);
				}
				ids.add(j);
			}
		}
		
		// save the database size
		databaseSize = database.getTransactions().size();

		// To build level 1, we keep only the frequent items.
		// We scan the database one time to calculate the support of each
		// candidate.
		k = 1;
		List<Itemset> level = new ArrayList<Itemset>();
		// For each item
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS
				.entrySet().iterator();
		while (iterator.hasNext()) {
			// check memory usage
			MemoryLogger.getInstance().checkMemory();
			
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator
					.next();
			if (entry.getValue().size() >= minSuppRelative) { // if the item is
																// frequent
				Integer item = entry.getKey();
				Itemset itemset = new Itemset(item);
				itemset.setTIDs(mapItemTIDS.get(item));
				level.add(itemset);
			} else {
				iterator.remove(); // if the item is not frequent we don't
				// need to keep it into memory.
			}
		}

		// sort itemsets of size 1 according to lexicographical order.
		Collections.sort(level, new Comparator<Itemset>() {
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		
		// Generate candidates with size k = 1 (all itemsets of size 1)
		k = 2;
		// While the level is not empty
		while (!level.isEmpty() && k <= maxItemsetSize) {

			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			List<Itemset> levelK = generateCandidateSizeK(level);

			// We check all sets of level k-1 for closure
			checkIfItemsetsK_1AreClosed(level, levelK);

			level = levelK; // We keep only the last level...
			k++;
		}

		// save end time
		endTimestamp = System.currentTimeMillis();
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns; // Return all frequent itemsets found!
	}

	/**
	 * Remove items that at not frequent from the transaction database
	 * @param database
	 * @return a map indicating the tidset of each item (key: item  value: tidset)
	 */
	private Map<Integer, Set<Integer>> removeItemsThatAreNotFrequent(
			TransactionDatabase database) {
		// (1) count the support of each item in the database in one database
		// pass
		// Map with (key: item  value: tidset)
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); 

		// for each transaction
		for (int j = 0; j < database.getTransactions().size(); j++) {
			List<Integer> transaction = database.getTransactions().get(j);
			// for each item
			for (int i = 0; i < transaction.size(); i++) {
				// update the support count of the item
				Set<Integer> ids = mapItemTIDS.get(transaction.get(i));
				if (ids == null) {
					ids = new HashSet<Integer>();
					mapItemTIDS.put(transaction.get(i), ids);
				}
				ids.add(j);
			}
		}
		System.out.println("NUMBER OF DIFFERENT ITEMS : " + mapItemTIDS.size());
		// (2) remove all items that are not frequent from the database

		// for each transaction
		for (int j = 0; j < database.getTransactions().size(); j++) {
			List<Integer> transaction = database.getTransactions().get(j);

			// for each item in the transaction
			Iterator<Integer> iter = transaction.iterator();
			while (iter.hasNext()) {
				Integer nextItem = iter.next();
				// if the item is not frequent
				Set<Integer> ids = mapItemTIDS.get(nextItem);
				if (ids.size() < minSuppRelative) {
					// remove it!
					iter.remove();
				}
			}

		}
		return mapItemTIDS;
	}

	/**
	 * Checks if all the itemsets of size K-1 are closed by comparing
	 * them with itemsets of size K.
	 * @param levelKm1 itemsets of size k-1
	 * @param levelK itemsets of size k
	 * @throws IOException exception if error writing output file
	 */
	private void checkIfItemsetsK_1AreClosed(Collection<Itemset> levelKm1,
			List<Itemset> levelK) throws IOException {
		// for each itemset of size k-1
		for (Itemset itemset : levelKm1) {
			// consider it is closed
			boolean isClosed = true;
			// compare this itemset with all itemsets of size k
			for (Itemset itemsetK : levelK) {
				// if an itemset has the same support and contain the itemset of size k-1 ,
				// then the itemset of size k-1 is not closed
				if (itemsetK.getAbsoluteSupport() == itemset
						.getAbsoluteSupport() && itemsetK.containsAll(itemset)) {
					isClosed = false;
					break;
				}
			}
			// if itemset of size k-1 is closed
			if (isClosed) {
				// save the itemset of of size k-1  to file
				saveItemset(itemset);
			}
		}
	}
	
	/**
	 * Save a frequent itemset to the output file or memory,
	 * depending on what the user chose.
	 * @param itemset the itemset
	 * @throws IOException exception if error writing the output file.
	 */
	void saveItemset(Itemset itemset) throws IOException {
		itemsetCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			writer.write(itemset.toString() + " #SUP: "
					+ itemset.getTransactionsIds().size() );
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			patterns.addItemset(itemset, itemset.size());
		}
	}
	
	/**
	 * Method to generate itemsets of size k from frequent itemsets of size K-1.
	 * @param levelK_1  frequent itemsets of size k-1
	 * @return itemsets of size k
	 */
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1) {
		// create a variable to store candidates
		List<Itemset> candidates = new ArrayList<Itemset>();

		// For each itemset I1 and I2 of level k-1
		loop1: for (int i = 0; i < levelK_1.size(); i++) {
			Itemset itemset1 = levelK_1.get(i);
			loop2: for (int j = i + 1; j < levelK_1.size(); j++) {
				Itemset itemset2 = levelK_1.get(j);

				// we compare items of itemset1 and itemset2.
				// If they have all the same k-1 items and the last item of
				// itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a
				// candidate
				for (int k = 0; k < itemset1.size(); k++) {
					// if they are the last items
					if (k == itemset1.size() - 1) {
						// the one from itemset1 should be smaller (lexical
						// order)
						// and different from the one of itemset2
						if (itemset1.getItems()[k] >= itemset2.get(k)) {
							continue loop1;
						}
					}
					// if they are not the last items, and
					else if (itemset1.getItems()[k] < itemset2.get(k)) {
						continue loop2; // we continue searching
					} else if (itemset1.getItems()[k] > itemset2.get(k)) {
						continue loop1; // we stop searching: because of lexical
										// order
					}
				}
				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				// create list of common tids
				Set<Integer> list = new HashSet<Integer>();
				for (Integer val1 : itemset1.getTransactionsIds()) {
					if (itemset2.getTransactionsIds().contains(val1)) {
						list.add(val1);
					}
				}
				
				// if the combination of itemset1 and itemset2 is frequent
				if (list.size() >= minSuppRelative) {
					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					candidate.setTIDs(list);
					
					candidates.add(candidate);
//					frequentItemsets.addItemset(candidate, k);
				}
			}
		}
		return candidates;
	}


	/**
	 * Get the frequent closed itemsets found by the latest execution.
	 * @return Itemsets
	 */
	public Itemsets getFrequentClosed() {
		return patterns;
	}

	/**
	 * Set the maximum itemset size of itemsets to be found
	 * @param maxItemsetSize maximum itemset size.
	 */
	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  APRIORI-CLOSE - STATS =============");
		long temps = endTimestamp - startTimestamp;
		// System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Transactions count from database : "
				+ databaseSize);
		System.out.println(" The algorithm stopped at size " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Frequent closed itemsets count : "
				+ itemsetCount);
		System.out.println(" Maximum memory usage : " + 
				MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
