package ca.pfv.spmf.algorithms.frequentpatterns.charm;
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;
 
/**
 * This is a new implementation of the CHARM algorithm (2014) that relies on bitsets to implement
 * tidsets.
 *  
 * Charm was proposed by ZAKI (2001).
 * <br/><br/>
 * 
 * See this article for details about CHARM:
 * <br/><br/>
 * 
 * Zaki, M. J., & Hsiao, C. J. (2002). CHARM: An Efficient Algorithm for Closed Itemset Mining. In SDM (Vol. 2, pp. 457-473).
 * <br/><br/>
 * 
 * This  version  saves the result to a file
 * or keep it into memory if no output path is provided
 * by the user to the runAlgorithm method().
 * 
 * @see TriangularMatrix
 * @see TransactionDatabase
 * @see Itemset
 * @see Itemsets
 * @author Philippe Fournier-Viger
 */
public class AlgoCharm_Bitset {

	/** relative minimum support **/
	private int minsupRelative;  
	/** the transaction database **/
	protected TransactionDatabase database; 

	/**  start time of the last execution */
	protected long startTimestamp;
	/** end  time of the last execution */
	protected long endTime; 
	
	/** 
	 The  patterns that are found 
	 (if the user want to keep them into memory) */
	protected Itemsets closedItemsets;
	/** object to write the output file */
	BufferedWriter writer = null; 
	/** the number of patterns found */
	protected int itemsetCount; 
	
	/** For optimization with a triangular matrix for counting 
	/ itemsets of size 2.  */
	private TriangularMatrix matrix; // the triangular matrix

	/** The hash table for storing itemsets for closeness checking (an optimization) */
	private HashTable hash;
	
	/**
	 * Default constructor
	 */
	public AlgoCharm_Bitset() {
		
	}


	/**
	 * Run the algorithm and save the output to a file or keep it into memory.
	 * @param database a transaction database
	 * @param output an output file path for writing the result or if null the result is saved into memory and returned
	 * @param minsup the minimum support
	 * @param useTriangularMatrixOptimization if true the triangular matrix optimization will be applied.
	 * @param hashTableSize the size of the hashtable (e.g. 10,000).
	 * @return the set of closed itemsets found if the result is kept into memory or null otherwise.
	 * @throws IOException exception if error while writing the file.
	 */
	public Itemsets runAlgorithm(String output, TransactionDatabase database, double minsup,
			boolean useTriangularMatrixOptimization, int hashTableSize) throws IOException {

		// Reset the tool to assess the maximum memory usage (for statistics)
		MemoryLogger.getInstance().reset();
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			closedItemsets =  new Itemsets("FREQUENT CLOSED ITEMSETS");
	    }else{ // if the user want to save the result to a file
	    	closedItemsets = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		
		// Create the hash table to store itemsets for closeness checking
		this.hash = new HashTable(hashTableSize);

		// reset the number of itemset found to 0
		itemsetCount = 0;

		this.database = database;
		
		// record the start time
		startTimestamp = System.currentTimeMillis();
		
		// convert from an absolute minsup to a relative minsup by multiplying
		// by the database size
		this.minsupRelative = (int) Math.ceil(minsup * database.size());

		// (1) First database pass : calculate tidsets of each item.
		// This map will contain the tidset of each item
		// Key: item   Value :  tidset
		final Map<Integer, BitSetSupport> mapItemTIDS = new HashMap<Integer, BitSetSupport>();
		// for each transaction
		int maxItemId = 0;
		maxItemId = calculateSupportSingleItems(database, mapItemTIDS,
				maxItemId);

		// If the user chose to use the triangular matrix optimization
		// for counting the support of itemsets of size 2.
		if (useTriangularMatrixOptimization) {
			// We create the triangular matrix.
			matrix = new TriangularMatrix(maxItemId + 1);
			// for each transaction, take each itemset of size 2,
			// and update the triangular matrix.
			for (List<Integer> itemset : database.getTransactions()) {
				Object[] array = itemset.toArray();
				// for each item i in the transaction
				for (int i = 0; i < itemset.size(); i++) {
					Integer itemI = (Integer) array[i];
					// compare with each other item j in the same transaction
					for (int j = i + 1; j < itemset.size(); j++) {
						Integer itemJ = (Integer) array[j];
						// update the matrix count by 1 for the pair i, j
						matrix.incrementCount(itemI, itemJ);
					}
				}
			}
		}

		// (2) create the list of single items
		List<Integer> frequentItems = new ArrayList<Integer>();
		
		// for each item
		for(Entry<Integer, BitSetSupport> entry : mapItemTIDS.entrySet()) {
			// get the support and tidset of that item
			BitSetSupport tidset = entry.getValue();
			int support = tidset.support;
			int item = entry.getKey();
			// if the item is frequent
			if(support >= minsupRelative) {
				// add the item to the list of frequent items
				frequentItems.add(item);
			}
		}
		
		// Sort the list of items by the total order of increasing support.
		// This total order is suggested in the article by Zaki.
		Collections.sort(frequentItems, new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1) {
				return mapItemTIDS.get(arg0).support - mapItemTIDS.get(arg1).support;  
			}}); 
		
		// Now we will combine each pairs of single items to generate equivalence classes
		// of 2-itemsets
		
		// For each frequent item X according to the total order
		for(int i=0; i < frequentItems.size(); i++) {
			Integer itemX = frequentItems.get(i);
			// If the itemset is null (which means that it has been removed, then we 
			// continue to the next item
			if(itemX == null) {
				continue;
			}

			// We obtain the tidset and support of that item X
			BitSetSupport tidsetX = mapItemTIDS.get(itemX);
			
			// We create an itemset with the item X.
			int[] itemsetX = new int[] {itemX};
			
			// We create an empty equivalence class for storing all itemsets obtained by joining
			// X with other itemsets.
			// This equivalence class is represented by two structures.
			// The first structure stores the suffix of all itemsets starting with the prefix "X".
			// For example, if X = "1" and the equivalence class contains 12, 13, 14, then
			// the structure "equivalenceClassIitems" will only contain  2, 3 and 4 instead of
			// 12, 13 and 14.  The reason for this implementation choice is that it is more
			// memory efficient.
			/// Moreover, when the charm properties requires to replace X with Xj (see the article),
			//  it can be done very efficiently if we keep X separately.
			List<int[]> equivalenceClassIitemsets = new ArrayList<int[]>();
			// The second structure stores the tidset of each itemset in the equivalence class
			// of the prefix "i"
			List<BitSetSupport> equivalenceClassItidsets = new ArrayList<BitSetSupport>();
			
			// For each item itemJ that is larger than i according to the total order of
			// increasing support.
loopJ:		for(int j=i+1; j < frequentItems.size(); j++) {
				Integer itemJ = frequentItems.get(j);
				// If the itemset is null (which means that it has been removed, then we 
				// continue to the next item
				if(itemJ == null) {
					continue;
				}
				
				// If the triangular matrix optimization is activated and X is a single item
				// we obtain the support of the pair of item "x", "j" by using the matrix. 
				// This allows to determine
				// directly the support without performing a join.
				// Then if the support is less than minsup, the itemset X + j is infrequent
				// and we don't need to consider it anymore.
				int supportIJ = -1;
				if(itemsetX.length == 1 && useTriangularMatrixOptimization) {
					// check the support of {i,j} according to the triangular matrix
					supportIJ = matrix.getSupportForItems(itemX, itemJ);
					// if not frequent
					if (supportIJ < minsupRelative) {
						// skip j;
						continue loopJ;
					}
				}
				
				// We obtain the tidset of J.
				BitSetSupport tidsetJ = mapItemTIDS.get(itemJ);

				// Calculate the tidset of itemset "X" + "J" by performing the intersection of 
				// the tidsets of X and the tidset of J.
				BitSetSupport bitsetSupportUnion = new BitSetSupport();
				if(itemsetX.length == 1 && useTriangularMatrixOptimization) {
					// If the triangular matrix optimization is used and X is a single item, then
					// we perform the intersection but we do not calculate the support since
					// it was already calculated using the triangular matrix
					bitsetSupportUnion = performANDFirstTime(tidsetX, tidsetJ, supportIJ);
				}else {
					// Otherwise, we perform the intersection and calculate the support
					// by calculating the cardinality of the resulting tidset.
					bitsetSupportUnion = performAND(tidsetX, tidsetJ);
				}
				
				// if the union is infrequent, we don't need to consider it further
				if(bitsetSupportUnion.support < minsupRelative) {
					continue;
				}
				
				// We next check which of the four Charm properties hold
				// If Property 1 holds
				if(tidsetX.support == tidsetJ.support && 
					bitsetSupportUnion.support == tidsetX.support) {
					// We remove Xj 
					frequentItems.set(j, null);
					// Then, we calculate the union of X and Xj
					int[] realUnion = new int[itemsetX.length + 1];
					System.arraycopy(itemsetX, 0, realUnion, 0, itemsetX.length);
					realUnion[itemsetX.length] = itemJ;
					// Then we replace X by the union
					itemsetX = realUnion;
				}else if(tidsetX.support < tidsetJ.support
						&& bitsetSupportUnion.support == tidsetX.support) {
					// If property 2 holds
					// Then, we calculate the union of X and Xj
					int[] realUnion = new int[itemsetX.length + 1];
					System.arraycopy(itemsetX, 0, realUnion, 0, itemsetX.length);
					realUnion[itemsetX.length] = itemJ;
					// Then we replace X by the union
					itemsetX = realUnion;
				}else if(tidsetX.support > tidsetJ.support
						&& bitsetSupportUnion.support == tidsetJ.support) {
					// If property 3 holds
					// We remove Xj
					frequentItems.set(j, null);
					// Then, we add the itemset X + J to the equivalence class that
					// we are building.
					// Note that we actually only add J because we keep the prefix X for
					// for the whole equivalence class. Thus X + J can be reconstructed at any time.
					equivalenceClassIitemsets.add(new int[] {itemJ});
					// We also keep the tidset of X + J
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}else {  
					// If property 4 holds
					// Then, we add the itemset X + J to the equivalence class that
					// we are building.
					// Note that we actually only add J because we keep the prefix X for
					// for the whole equivalence class. Thus X + J can be reconstructed at any time.
					equivalenceClassIitemsets.add(new int[] {itemJ});
					// We also keep the tidset of X + J
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}
			}
			
			// Process all itemsets from the equivalence class that we are building, which 
			// has X as prefix, to find larger itemsets.
			// Note that we only do that if the equivalence class contains at least an itemset.
			if(equivalenceClassIitemsets.size() > 0) {
				// call to recursive method
				processEquivalenceClass(itemsetX, equivalenceClassIitemsets, equivalenceClassItidsets);
			}
			
			// Save the itemset X  with its support (can be obtained from its tidset.
			save(null, itemsetX, tidsetX);
		}
			
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		
		// we check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// record the end time for statistics
		endTime = System.currentTimeMillis();
		
		// Return all frequent itemsets found!
		return closedItemsets; 
	}


	private int calculateSupportSingleItems(TransactionDatabase database,
			final Map<Integer, BitSetSupport> mapItemTIDS, int maxItemId) {
		for (int i = 0; i < database.size(); i++) {
			// Add the transaction id to the set of all transaction ids
			// for each item in that transaction
			
			// For each item
			for (Integer item : database.getTransactions().get(i)) {
				// Get the current tidset of that item and its support
				BitSetSupport tids = mapItemTIDS.get(item);
				// If no tidset, then we create one
				if(tids == null){
					tids = new BitSetSupport();
					mapItemTIDS.put(item, tids);
					// we remember the largest item seen until now
					if (item > maxItemId) {
						maxItemId = item;
					}
				}
				// we add the current transaction id to the tidset of the item
				tids.bitset.set(i);
				// we increase the support of that item
				tids.support++;
			}
		}
		return maxItemId;
	}

	/**
	 * Perform the intersection of two tidsets representing single items.
	 * @param tidsetI the first tidset
	 * @param tidsetJ the second tidset
	 * @param supportIJ the support of the intersection (already known) so it does not need to 
	 *                  be calculated again
	 * @return  the resulting tidset and its support
	 */
	private BitSetSupport performANDFirstTime(BitSetSupport tidsetI,
			BitSetSupport tidsetJ, int supportIJ) {
		// Create the new tidset and perform the logical AND to intersect the tidset
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetI.bitset.clone();
		bitsetSupportIJ.bitset.and(tidsetJ.bitset);
		// set the support as the support provided as parameter
		bitsetSupportIJ.support = supportIJ;
		// return the new tidset
		return bitsetSupportIJ;
	}

	/**
	 * Perform the intersection of two tidsets for itemsets containing more than one item.
	 * @param tidsetI the first tidset
	 * @param tidsetJ the second tidset
	 * @return the resulting tidset and its support
	 */
	private BitSetSupport performAND(BitSetSupport tidsetI,
			BitSetSupport tidsetJ) {
		// Create the new tidset and perform the logical AND to intersect the tidset
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		bitsetSupportIJ.bitset = (BitSet)tidsetI.bitset.clone();
		bitsetSupportIJ.bitset.and(tidsetJ.bitset);
		// set the support as the cardinality of the new tidset
		bitsetSupportIJ.support = bitsetSupportIJ.bitset.cardinality();
		// return the new tidset
		return bitsetSupportIJ;
	}
	
	/**
	 * This method process all itemsets from an equivalence class to generate larger itemsets,
	 * @param prefix  the prefix of all itemsets of the current equivalence class
	 * @param equivalenceClassItemsets  the list of last items of itemsets of the current equivalence class
	 * @param equivalenceClassTidsets the list of tidsets of itemsets of the current equivalence class
	 * @throws IOException 
	 */
	private void processEquivalenceClass(int[] prefix, List<int[]> equivalenceClassItemsets,
			List<BitSetSupport> equivalenceClassTidsets) throws IOException {
		
		// If there is only on itemset in equivalence class
		if(equivalenceClassItemsets.size() == 1) {
			int[] itemsetI = equivalenceClassItemsets.get(0);
			BitSetSupport tidsetI = equivalenceClassTidsets.get(0);
			
			// Then, we just attempt to save that itemset to the output and stop.
			// To save the itemset we call the method save with the prefix "prefix" and the suffix
			// "itemsetI".
			save(prefix, itemsetI, tidsetI); 
			return;
		}
		
		// If there are only two itemsets in the equivalence class
		if(equivalenceClassItemsets.size() == 2) {
			
			// We get the suffix of the first itemset (an itemset that we will call I)
			int[] itemsetI = equivalenceClassItemsets.get(0);
			BitSetSupport tidsetI = equivalenceClassTidsets.get(0);

			// We get the suffix of the second itemset (an itemset that we will call J)
			int[] itemsetJ = equivalenceClassItemsets.get(1);
			BitSetSupport tidsetJ = equivalenceClassTidsets.get(1);
			
			// We calculate the tidset of the itemset resulting from the union of
			// the first itemset and the second itemset.
			BitSetSupport bitsetSupportIJ = performAND(tidsetI, tidsetJ);
			// If the itemset is frequent
			if(bitsetSupportIJ.support >= minsupRelative) {
				// we attempt to save the itemset  prefix + itemsetI + itemsetJ
				int[] suffixIJ = ArraysAlgos.concatenate(itemsetI, itemsetJ);
				save(prefix, suffixIJ, bitsetSupportIJ);
			}
			
			// If the itemset prefix+I does not have the same support as prefix+I+J,
			// then prefix+I may be closed, so we attempt to save it.
			if(bitsetSupportIJ.support != tidsetI.support) {
				save(prefix, itemsetI, tidsetI);
			}
			// If the itemset prefix+J does not have the same support as prefix+I+J,
			// then prefix+J may be closed, so we attempt to save it.
			if(bitsetSupportIJ.support != tidsetJ.support) {
				save(prefix, itemsetJ, tidsetJ);
			}
			return;
		}
		
		// The next loop combines each pairs of itemsets of the equivalence class
		// to form larger itemsets
				
		// For each itemset "prefix" + an itemset X
		for(int i=0; i < equivalenceClassItemsets.size(); i++) {
			int[] itemsetX = equivalenceClassItemsets.get(i);
			// If the itemset X is null, which means that it had been removed
			if(itemsetX == null) {
				continue;
			}
			// We obtain the tidset of X
			BitSetSupport tidsetX = equivalenceClassTidsets.get(i);
			
			// create the empty equivalence class for storing the equivalence class of 
			// all itemsets obtained by a join with X.
			List<int[]> equivalenceClassIitemsets = new ArrayList<int[]>();
			// We also create a structure to store the tidset of each itemset in the 
			// equivalence class
			List<BitSetSupport> equivalenceClassItidsets = new ArrayList<BitSetSupport>();
			
			// For each itemset "prefix" + an itemset J
			for(int j=i+1; j < equivalenceClassItemsets.size(); j++) {
				int[] itemsetJ = equivalenceClassItemsets.get(j);
				// If J is null, that means that it has been removed by a Charm property,
				// so we just continue to the next itemset
				if(itemsetJ == null) {
					continue;
				}

				// Get the tidset of J.
				BitSetSupport tidsetJ = equivalenceClassTidsets.get(j);
				 
				// Calculate the tidset intersection of prefix + X + J 
				BitSetSupport bitsetSupportUnion = new BitSetSupport();
				bitsetSupportUnion = performAND(tidsetX, tidsetJ);
			
				// If prefix + X + J  is infrequent, then we don't need
				// to consider it anymore
				if(bitsetSupportUnion.support < minsupRelative) {
					continue;
				}
				
				// We next check which of the four Charm properties hold
				// If Property 1 holds:
				if(tidsetX.support == tidsetJ.support && 
					bitsetSupportUnion.support == tidsetX.support) {
					// Remove prefix + j
					equivalenceClassItemsets.set(j, null);
					equivalenceClassTidsets.set(j, null);
					// Replace X by X + J
					int[] realUnion = ArraysAlgos.concatenate(itemsetX, itemsetJ);
					itemsetX = realUnion;
				}else if(tidsetX.support < tidsetJ.support
						&& bitsetSupportUnion.support == tidsetX.support) {
					// If property 2 holds
					// Replace X by X + J
					int[] realUnion = ArraysAlgos.concatenate(itemsetX, itemsetJ);
					itemsetX = realUnion;
				}else if(tidsetX.support > tidsetJ.support
						&& bitsetSupportUnion.support == tidsetJ.support) {
					// If property 3 holds
					// Remove prefix + j
					equivalenceClassItemsets.set(j, null);
					equivalenceClassTidsets.set(j, null);
					// Then, we add the itemset prefix + X + J to the equivalence class that
					// we are building.
					// Note that we actually only add J because we keep the prefix prefix+X for
					// for the whole equivalence class. Thus prefix+X + J can be reconstructed at any time.
					equivalenceClassIitemsets.add(itemsetJ);
					// We also keep the tidset of prefix+X+J
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}else {  
					// If property 4 holds
					// Then, we add the itemset prefix + X + J to the equivalence class that
					// we are building.
					// Note that we actually only add J because we keep the prefix prefix+X for
					// for the whole equivalence class. Thus prefix+X + J can be reconstructed at any time.
					equivalenceClassIitemsets.add(itemsetJ);
					// We also keep the tidset of prefix+X+J
					equivalenceClassItidsets.add(bitsetSupportUnion);
				}
			}
			
			// Process all itemsets from the equivalence class that we are building, which 
			// has prefix+X as prefix, to find larger itemsets.
			// Note that we only do that if the equivalence class contains at least an itemset
			if(equivalenceClassIitemsets.size()>0) {
				int[] newPrefix = ArraysAlgos.concatenate(prefix, itemsetX);
				processEquivalenceClass(newPrefix, equivalenceClassIitemsets, equivalenceClassItidsets);
			}
			// Finally, we attempt to save the itemset prefix+X since it may be a closed itemset.
			save(prefix, itemsetX, tidsetX);
		}
		
		// we check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  CHARM v96e Bitset - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Frequent closed itemsets count : "
				+ itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out
				.println("===================================================");
	}

	/**
	 * Get the set of frequent itemsets.
	 * @return the frequent itemsets (Itemsets).
	 */
	public Itemsets getClosedItemsets() {
		return closedItemsets;
	}
	
	/**
	 * Anonymous inner class to store a bitset and its cardinality.
	 * Storing the cardinality is useful because the cardinality() method
	 * of a bitset in Java is very expensive.
	 */
	public class BitSetSupport{
		BitSet bitset = new BitSet();
		int support;
	}
	
	/**
	 * Save an itemset(as described in the paper).
	 * @param prefix the prefix part of this itemset
	 * @param suffix the suffix part of this itemset
	 * @param tidset the tidset of this itemset
	 * @throws IOException if an error occurs when writing to file
	 */
	private void save(int[] prefix, int[] suffix, BitSetSupport tidset) throws IOException {
		// First we concatenate the suffix and prefix of that itemset.
		int[] prefixSuffix;
		if(prefix == null) {
			prefixSuffix = suffix;
		}else {
			prefixSuffix = ArraysAlgos.concatenate(prefix, suffix);
		}
		// Sort the resulting itemset
		Arrays.sort(prefixSuffix);
		
		// Create an instance of "Itemset" for that itemset to put in hash table
		ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset itemset = new ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset(prefixSuffix);
		itemset.setAbsoluteSupport(tidset.support);

		// Calculate the hash code of that itemset 
		int hashcode = hash.hashCode(tidset.bitset);
		
		// Check in the hash table to see if the itemset has 
		// a superset already in the hash table. If not, then it is
		// a closed itemset and we should output it as well
		// as insert it in the hash table.
		if (!hash.containsSupersetOf(itemset, hashcode)) {
			// increase the itemset count
			itemsetCount++;
			// if the result should be saved to memory
			if (writer == null) {
				// save it to memory with its tidset
				Itemset itemsetWithTidset = new Itemset(prefixSuffix, tidset.bitset, tidset.support);
				closedItemsets.addItemset(itemsetWithTidset, itemset.size());
			} else {
				// otherwise if the result should be saved to a file,
				// then write it to the output file
				writer.write(itemset.toString() + " #SUP: " + itemset.support);
				writer.newLine();
			}
			// add the itemset to the hashtable
			hash.put(itemset, hashcode);
		}
	}

}
