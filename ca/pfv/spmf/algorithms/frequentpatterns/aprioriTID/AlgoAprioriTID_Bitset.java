package ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID;
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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the AprioriTID algorithm. This version is very fast 
 * because it uses bit vector for representing TID SETS (transaction id sets).<br/><br/>
 * 
 * The AprioriTID algorithm finds all the frequents itemsets and their support
 * in a transaction database and save them to file.<br/><br/>
 * 
 * AprioriTID was originally proposed in :<br/><br/>
 * 
 * Agrawal R, Srikant R. "Fast Algorithms for Mining Association Rules", VLDB.
 * Sep 12-15 1994, Chile, 487-99,<br/><br/>
 * 
 * 
 * @see Itemset
 * @author Philippe Fournier-Viger
 */
public class AlgoAprioriTID_Bitset {

	// the current level
	protected int k; 

	// variables for counting support of items
	Map<Integer, BitSet> mapItemTIDS = new HashMap<Integer, BitSet>();

	// the minimum support threshold
	int minSuppRelative;

	// Special parameter to set the maximum size of itemsets to be discovered
	int maxItemsetSize = Integer.MAX_VALUE;

	long startTimestamp = 0; // start time of latest execution
	long endTimeStamp = 0; // end time of latest execution
	
	// object to write the output file
	BufferedWriter writer = null;

	// the number of frequent itemsets found
	private int itemsetCount;
	private int tidcount = 0;

	/**
	 * Default constructor
	 */
	public AlgoAprioriTID_Bitset() {
	}

	public void runAlgorithm(String input, String output, double minsup)
			throws NumberFormatException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		// reset number of itemsets found
		itemsetCount = 0;
		
		// create object for writing the output file
		writer = new BufferedWriter(new FileWriter(output));
		
		// initialize variable to count the number of transactions
		tidcount = 0;

		// read the input file line by line until the end of the file
		// (each line is a transaction)
		mapItemTIDS = new HashMap<Integer, BitSet>(); 
		// key : item   value: tidset of the item as a bitset
		
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of file
		while (((line = reader.readLine()) != null)) { 
			// check memory usage
			MemoryLogger.getInstance().checkMemory();
			
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split line into items according to spaces
			String[] lineSplited = line.split(" ");
			// for each item
			for (String stringItem : lineSplited) {
				// convert from string to integer
				int item = Integer.parseInt(stringItem);
				// update the tidset of the item
				BitSet tids = mapItemTIDS.get(item);
				if (tids == null) {
					tids = new BitSet();
					mapItemTIDS.put(item, tids);
				}
				tids.set(tidcount);
			}
			// increase the transaction count
			tidcount++;
		}
		reader.close(); // close the input file

		// convert the support from a relative minimum support (%) to an 
		// absolute minimum support
		this.minSuppRelative = (int) Math.ceil(minsup * tidcount);

		// To build level 1, we keep only the frequent items.
		// We scan the database one time to calculate the support of each
		// candidate.
		k = 1;
		List<Itemset> level = new ArrayList<Itemset>();
		// For each item
		Iterator<Entry<Integer, BitSet>> iterator = mapItemTIDS.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			// for the current item
			Map.Entry<Integer, BitSet> entry = (Map.Entry<Integer, BitSet>) iterator
					.next();
			// get the support count (cardinality of the tidset)
			int cardinality = entry.getValue().cardinality();
			// if the item is frequent
			if (cardinality >= minSuppRelative) { 
				// add the item to the set of frequent itemsets of size 1
				Integer item = entry.getKey();
				Itemset itemset = new Itemset(item);
				itemset.setTIDs(mapItemTIDS.get(item), cardinality);
				level.add(itemset);
				// save the itemset
				saveItemsetToFile(itemset);
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
			level = generateCandidateSizeK(level);
			; // We keep only the last level...
			k++;
		}

		// close the file
		writer.close();
		
		// save end time
		endTimeStamp = System.currentTimeMillis();
	}

	/**
	 * Method to generate itemsets of size k from frequent itemsets of size K-1.
	 * @param levelK_1  frequent itemsets of size k-1
	 * @return itemsets of size k
	 */
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1)
			throws IOException {
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
				Integer missing = itemset2.get(itemset2.size() - 1);

				// create list of common tids
				BitSet list = (BitSet) itemset1.getTransactionsIds().clone();
				list.and(itemset2.getTransactionsIds());
				int cardinality = list.cardinality();

				if (cardinality >= minSuppRelative) {
					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					candidate.setTIDs(list, cardinality);
					
					candidates.add(candidate);
					saveItemsetToFile(candidate);
				}
			}
		}
		return candidates;
	}

	/**
	 * Set the maximum itemset size of itemsets to be found
	 * @param maxItemsetSize maximum itemset size.
	 */
	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}

	/**
	 * Save an itemset to the output file.
	 * @param itemset the itemset to be saved
	 * @throws IOException an exception if error while writing the file.
	 */
	void saveItemsetToFile(Itemset itemset) throws IOException {
		writer.write(itemset.toString() + " #SUP: " + itemset.cardinality);
		writer.newLine();
		itemsetCount++; // increase frequent itemset count
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  APRIORI - STATS =============");
		System.out.println(" Transactions count from database : " + tidcount);
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + 
				MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}
}
