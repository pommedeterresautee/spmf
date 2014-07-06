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
 * This is an implementation of the AprioriTID algorithm.<br/><br/>
 * 
 * The AprioriTID algorithm finds all the frequents itemsets and their support
 * in a binary context.<br/><br/>
 * 
 * AprioriTID can be faster than Apriori and produce the same result.
 * <br/><br/>
 * 
 * AprioriTID was originally proposed in :<br/><br/>
 * 
 * Agrawal R, Srikant R. "Fast Algorithms for Mining Association Rules", VLDB.
 * Sep 12-15 1994, Chile, 487-99,<br/><br/>
 * 
 * This implementation can save the result to a file or keep
 * it into memory if no output path is provided to the runAlgorithm() method.
 * <br/><br/>
 * 
 * @see Itemset
 * @see Itemsets
 * @author Philippe Fournier-Viger
 */
public class AlgoAprioriTID {

	// the current level
	protected int k; 

	// variables for counting support of items
	Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();

	// the minimum support threshold
	int minSuppRelative;

	// Special parameter to set the maximum size of itemsets to be discovered
	int maxItemsetSize = Integer.MAX_VALUE;

	long startTimestamp = 0; // start time of latest execution
	long endTimeStamp = 0; // end time of latest execution
	
	// object for writing to file if the user choose to write to a file
	BufferedWriter writer = null;
	
	// variable to store the result if the user choose to save to memory instead of a file
	protected Itemsets patterns = null;

	// the number of frequent itemsets found
	private int itemsetCount = 0;
	
	// the number of transactions
	private int databaseSize = 0;
	
	// the current transaction database, if the user has provided one
	// instead of an input file.
	private TransactionDatabase database = null;

	// indicate if the empty set should be added to the results
	private boolean emptySetIsRequired = false;

	/**
	 * Default constructor
	 */
	public AlgoAprioriTID() {
	}
	
	
	/**
	 * This method run the algorithm on a transaction database already in memory.
	 * @param database  the transaction database
	 * @param minsup the minimum support threshold as a percentage (double)
	 * @return the method returns frequent itemsets
	 * @throws IOException  exception if error reading/writing the file
	 */
	public Itemsets runAlgorithm(TransactionDatabase database, double minsup)
			throws NumberFormatException, IOException {
		// remember the transaction database received as parameter
		this.database = database;
		// call the real "runAlgorithm() method
		Itemsets result = runAlgorithm(null, null, minsup);
		
		// forget the database
		this.database = null;
		
		// return the result
		return result;
	}
	
	/**
	 * This method run the algorithm.
	 * @param input  the file path of an input file.  if null, the result is returned by the method.
	 * @param output  the output file path
	 * @param minsup the minimum support threshold as a percentage (double)
	 * @return if no output file path is provided, the method return frequent itemsets, otherwise null
	 * @throws IOException  exception if error reading/writing the file
	 */
	public Itemsets runAlgorithm(String input, String output, double minsup)
			throws NumberFormatException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// reset number of itemsets found
		itemsetCount = 0;
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		
		// (1) count the tid set of each item in the database in one database
		// pass
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); // id item, count

		// read the input file line by line until the end of the file
		// (each line is a transaction)
		
		databaseSize = 0; 
		// if the database is in memory
		if(database != null){
			// for each transaction
			for(List<Integer> transaction : database.getTransactions()){ // for each transaction
				// for each token (item)
				for (int item : transaction) {
					// get the set of tids for this item until now
					Set<Integer> tids = mapItemTIDS.get(item);
					// if null, create a new set
					if (tids == null) {
						tids = new HashSet<Integer>();
						mapItemTIDS.put(item, tids);
					}
					// add the current transaction id (tid) to the set of the current item
					tids.add(databaseSize);
				}
				databaseSize++; // increment the tid number
			}
		}else{
			BufferedReader reader = new BufferedReader(new FileReader(input));
			
			String line;
			while (((line = reader.readLine()) != null)) { // for each transaction
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (line.isEmpty() == true ||
						line.charAt(0) == '#' || line.charAt(0) == '%'
								|| line.charAt(0) == '@') {
					continue;
				}
				
				// split the line into tokens according to spaces
				String[] lineSplited = line.split(" ");
				// for each token (item)
				for (String token : lineSplited) {
					// convert from string item to integer
					int item = Integer.parseInt(token);
					// get the set of tids for this item until now
					Set<Integer> tids = mapItemTIDS.get(item);
					// if null, create a new set
					if (tids == null) {
						tids = new HashSet<Integer>();
						mapItemTIDS.put(item, tids);
					}
					// add the current transaction id (tid) to the set of the current item
					tids.add(databaseSize);
				}
				databaseSize++; // increment the tid number
			}
			reader.close(); // close the input file
		}
		
		
		// if the user want the empty set
		if(emptySetIsRequired ){
			// add the empty set to the set of patterns
			patterns.addItemset(new Itemset(new int[]{}), 0);
		}
		

		// convert the support from a relative minimum support (%) to an 
		// absolute minimum support
		this.minSuppRelative = (int) Math.ceil(minsup * databaseSize);

		// To build level 1, we keep only the frequent items.
		// We scan the database one time to calculate the support of each
		// candidate.
		k = 1;
		List<Itemset> level = new ArrayList<Itemset>();
		// For each item
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS.entrySet().iterator();
		while (iterator.hasNext()) {
			// check memory usage
			MemoryLogger.getInstance().checkMemory();
			
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator
					.next();
			// if the item is frequent
			if (entry.getValue().size() >= minSuppRelative) { 
				Integer item = entry.getKey();
				Itemset itemset = new Itemset(item);
				itemset.setTIDs(mapItemTIDS.get(item));
				level.add(itemset);
				// save the itemset
				saveItemset(itemset);
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
			k++;
		}


		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		// save the end time
		endTimeStamp = System.currentTimeMillis();
		// return frequent itemsets
		return patterns;
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
					// if the k-th items is smalle rinn itemset1
					else if (itemset1.getItems()[k] < itemset2.getItems()[k]) {
						continue loop2; // we continue searching
					} else if (itemset1.getItems()[k] > itemset2.getItems()[k]) {
						continue loop1; // we stop searching: because of lexical
										// order
					}
				}

				// create list of common tids 
				Set<Integer> list = new HashSet<Integer>();
				// for each tid from the tidset of itemset1
				for (Integer val1 : itemset1.getTransactionsIds()) {
					// if it appears also in the tidset of itemset2
					if (itemset2.getTransactionsIds().contains(val1)) {
						// add it to common tids
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
					// add it to the list of candidates
					candidates.add(candidate);
					// save it 
					saveItemset(candidate);
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
	 * Method to indicate if the empty set should be included in results
	 * or not.
	 * @param emptySetIsRequired  if true the empty set will be included.
	 */
	public void setEmptySetIsRequired(boolean emptySetIsRequired) {
		this.emptySetIsRequired = emptySetIsRequired;
	}

	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  APRIORI - STATS =============");
		System.out.println(" Transactions count from database : " + databaseSize);
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Maximum memory usage : " + 
				MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}

	/**
	 * Get the number of transactions in the last database read.
	 * @return number of transactions.
	 */
	public int getDatabaseSize() {
		return databaseSize;
	}
}
