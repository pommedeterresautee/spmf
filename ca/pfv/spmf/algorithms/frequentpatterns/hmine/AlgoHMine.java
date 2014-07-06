package ca.pfv.spmf.algorithms.frequentpatterns.hmine;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.tools.MemoryLogger;

/**
 * An implementation of the HMine algorithm for mining frequent itemsets from a
 * transaction database.<br/><br/>
 * 
 * It is based on the description in:<br/><br/>
 * 
 * Pei et al. (2007) H-Mine: Fast and space-preserving frequent pattern mining
 * in large databases. IIE Transactions, 39, 593-605.<br/><br/>
 * 
 * I tried to follow as much as possible the description in the article for
 * HMine(mem). One observation is that the links for an item in the header table
 * are simply what is called a "tid set" in some other algorithms, because links
 * always point to the first element of a transaction.  So actually, the algorithm
 * was more simple than I first thought.
 * 
 * @author Philippe Fournier-Viger
 */

public class AlgoHMine {

	// the minimum support threshold chosen by the user
	private int minsup;
	// object to write the output file
	BufferedWriter writer = null;
	// the number of frequent itemsets found (for
	// statistics)
	private int frequentCount; 

	// the start time and end time of the last algorithm execution
	long startTimestamp;
	long endTimestamp;

	// in-memory database where 
	// each position in the main list is a transaction represented as a list of integers
	List<List<Integer>> database; 

	/**
	 * Default constructor
	 */
	public AlgoHMine() {
	}

	/** 
	 * Run the algorithm.
	 * @param input the path of the input file  (a transaction database)
	 * @param output the output file path for writing the result
	 * @param minsup the minimum support threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, int minsup)
			throws IOException {
		// record the start time
		startTimestamp = System.currentTimeMillis();

		// create object for writing the output file
		writer = new BufferedWriter(new FileWriter(output));
		
		// reset the number of itemset found
		frequentCount = 0;
		
		// reset the memory usage checking utility
		MemoryLogger.getInstance().reset();
		
		// remember the minimum support threshold set by the user
		this.minsup = minsup;

		// (1) Scan the database and count the support of each item.
		// The support of items is stored in map where
		//  key = item      value = support count
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
		// scan the database
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
			// for each item in the transaction
			for (String itemString : lineSplited) { 
				// increase the support count of the item by 1
				Integer item = Integer.parseInt(itemString);
				Integer count = mapItemCount.get(item);
				if (count == null) {
					mapItemCount.put(item, 1);
				} else {
					mapItemCount.put(item, ++count);
				}
			}
		}
		// close the input file
		reader.close();

		// (2) Scan the database again to construct in-memory database without
		// infrequent items and to record the tidset of each item.
		
		// Create the structure of the in-memory database
		database = new ArrayList<List<Integer>>();
		
		// Create a map for recording the tidset of each item
		// Key: item    Value: tidset as a list of integers
		Map<Integer, List<Integer>> mapItemTidset = new HashMap<Integer, List<Integer>>();
		// TODO: For optimization, we could use a treemap sorted by descending
		// order of support.

		// Read the file again
		BufferedReader reader2 = new BufferedReader(new FileReader(input));
		String line2;
		int tid = 0;
		// for each line (transaction) until the end of the file
		while (((line2 = reader2.readLine()) != null)) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction into items
			String[] lineSplited = line2.split(" ");
			// Create a transaction object for storing the items as integers
			List<Integer> transaction = new ArrayList<Integer>();
			
			// for each item in the transaction
			for (String itemString : lineSplited) {
				// convert the item to an integer
				Integer item = Integer.parseInt(itemString);
				// if the item is frequent
				if (mapItemCount.get(item) >= minsup) {
					// add the item to this transaction
					transaction.add(item);
					// update the tidset of the item by adding
					// the tid of the current transaction
					List<Integer> tidset = mapItemTidset.get(item);
					if (tidset == null) {
						tidset = new ArrayList<Integer>();
						mapItemTidset.put(item, tidset);
					}
					tidset.add(tid);
				}
			}
			// add the transaction to the in-memory database
			database.add(transaction);
			// increase the id of the current transaction by 1 to get the id
			// of the next transction that will be read
			tid++;
		}
		// close the input file
		reader2.close();

		// (3)For each frequent item, save it to file, and then
		// call the HMINE recursive method.
		for (Entry<Integer, List<Integer>> entry : mapItemTidset.entrySet()) {
			// Create an empty prefix with that item
			int[] prefix = new int[1];
			prefix[0] = entry.getKey();
			// save that prefix to the output file
			writeOut(prefix, entry.getValue().size());
			//  make a recursive call to grow that prefix to find
			// larger frequent itemsets
			hmine(prefix, entry.getKey(), entry.getValue());
		}
		// record the end time
		endTimestamp = System.currentTimeMillis();
		//close the output file
		writer.close();
	}

	/**
	 * This is the recursive procedure for growing a prefix to find larger frequent itemsets
	 * @param prefix the prefix 
	 * @param itemProjection the last item added to the prefix 
	 * @param links a set of tids
	 * @throws IOException exception if error while writing the output file
	 */
	private void hmine(int[] prefix, Integer itemProjection, List<Integer> links)
			throws IOException {
		// scan the projected database and calculate the links (tids) for each item
		// appearing after the item "itemprojection" 
		
		// We will store these tidsets in a map
		// Key: item    value: tidset as a list of integers
		Map<Integer, List<Integer>> mapItemTidset = new HashMap<Integer, List<Integer>>();

		 // for each transaction containing the item to perform the projection
		for (Integer tid : links) {
			boolean seen = false;
			// for each item in that transaction
			for (Integer item : database.get(tid)) {
				// if we have seen "itemprojection" already
				if (seen) {
					// get the tidset of the current item and add
					// the tid of the current transction to its tidset
					List<Integer> tidset = mapItemTidset.get(item);
					if (tidset == null) {
						tidset = new ArrayList<Integer>();
						mapItemTidset.put(item, tidset);
					}
					tidset.add(tid);
				}
				// if this is the item for the projection, we remember it
				if (itemProjection.equals(item)) {
					seen = true;
				}
			}
		}

		// For each item having the minimum support in the projected database,
		// we will save to file, and then recursively call H-Mine.
		
		// for each item appearing in the projected database
		for (Entry<Integer, List<Integer>> entry : mapItemTidset.entrySet()) {
			// if the item is frequent
			if (entry.getValue().size() >= minsup) {
				// create a new prefix by appending the current item
				int[] newPrefix = new int[prefix.length + 1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = entry.getKey();
				// save the new prefix to the output file with its support (size of tidset)
				writeOut(newPrefix, entry.getValue().size());
				// call hmine procedure to recursively grow this prefix
				// to try to find larger frequent itemsets starting with the same prefix
				hmine(newPrefix, entry.getKey(), entry.getValue());
			}
		}
		// check the memory usage for statistics
		MemoryLogger.getInstance().checkMemory();
	}


	/**
	 * Write a frequent itemset to the output file.
	 * @param itemset the itemset
	 * @param support the support of the itemset
	 */
	private void writeOut(int[] itemset, int support) throws IOException {
		// increase the number of frequent itemsets found until now
		frequentCount++;
		// create a stringuffer
		StringBuffer buffer = new StringBuffer();
		// append items from the itemset to the stringbuffer
		for (int i = 0; i < itemset.length; i++) {
			buffer.append(itemset[i]);
			if (i != itemset.length - 1) {
				buffer.append(' ');
			}
		}
		// append the support of the itemset
		buffer.append(" #SUP: ");
		buffer.append(support);
		// write the strinbuffer to file and create a new line
		// so that we are ready for writing the next itemset.
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Print statistics about the latest execution of the algorithm
	 * to System.out.
	 */
	public void printStatistics() {
		System.out.println("========== HMINE - STATS ============");
		System.out.println(" Number of frequent  itemsets: " + frequentCount);
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("=====================================");
	}
}
