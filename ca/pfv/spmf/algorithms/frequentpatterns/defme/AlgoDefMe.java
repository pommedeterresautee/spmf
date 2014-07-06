package ca.pfv.spmf.algorithms.frequentpatterns.defme;
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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;
 
/**
 * This is a recent implementation of the DefMe algorithm that uses bitsets to represent
 * tidsets, and is implemented to mine itemsets.
 *  
 * Defme was proposed by Soulet et al (2014).
 * <br/><br/>
 * 
 * See this article for details about DefMe:
 * <br/><br/>
 * 
 * Soulet, A., Rioult, F. (2014). Efficiently Depth-First Minimal Pattern Mining, PAKDD 2014.
 * <br/><br/>
 * 
 * This  version  saves the result to a file
 * or keep it into memory if no output path is provided
 * by the user to the runAlgorithm method().
 * 
 * @see TransactionDatabase
 * @see Itemset
 * @see Itemsets
 * @author Philippe Fournier-Viger
 */
public class AlgoDefMe {

	/** relative minimum support **/
	private int minsupRelative;  
	/** the transaction database **/
	private TransactionDatabase database; 

	/**  start time of the last execution */
	private long startTimestamp;
	/** end  time of the last execution */
	private long endTime; 
	
	/** 
	 The  patterns that are found 
	 (if the user want to keep them into memory) */
	protected Itemsets generators;
	/** object to write the output file */
	BufferedWriter writer = null; 
	/** the number of patterns found */
	private int itemsetCount;
	
	/** A map containing the tidset (i.e. cover) of each item represented as a bitset */
	private Map<Integer, BitSetSupport> mapItemTIDS; 

	/**
	 * Default constructor
	 */
	public AlgoDefMe() {
		
	}


	/**
	 * Run the algorithm.
	 * @param database a transaction database
	 * @param output an output file path for writing the result or if null the result is saved into memory and returned
	 * @param minsup the minimum support
	 * @return the set of generators if the user chose to save the result to memory. Otherwise, null.
	 * @throws IOException exception if error while writing the file.
	 */
	public Itemsets runAlgorithm(String output, TransactionDatabase database, double minsup) throws IOException {

		// Reset the tool to assess the maximum memory usage (for statistics)
		MemoryLogger.getInstance().reset();
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			generators =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
	    	generators = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}

		// reset the number of itemset found to 0
		itemsetCount = 0;

		this.database = database;
		
		// record the start time
		startTimestamp = System.currentTimeMillis();
		
		// convert from an absolute minsup to a relative minsup by multiplying
		// by the database size
		this.minsupRelative = (int) Math.ceil(minsup * database.size());

		// Calculate the tidset of each single item (what is called COV() in the paper)
		mapItemTIDS = new HashMap<Integer, BitSetSupport>();
		// for each transaction
		for (int i = 0; i < database.size(); i++) {
			// Add the transaction id to the set of all transaction ids
			// for each item in that transaction
			
			// For each item
			for (Integer item : database.getTransactions().get(i)) {
				// Get the current tidset of that item
				BitSetSupport tids = mapItemTIDS.get(item);
				// If none, then we create one
				if(tids == null){
					tids = new BitSetSupport();
					mapItemTIDS.put(item, tids);
				}
				// we add the current transaction id to the tidset of the item
				tids.bitset.set(i);
				// we increase the support of that item
				tids.support++;
			}
		}

		// (2) create the list of single frequent items
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
		

		// Create the tidset of the empty set 
		BitSet tidsetEmptySet = new BitSet(database.size());
		tidsetEmptySet.set(0, database.size());
		
		// Initial call of the defme procedure 
		defme(new int[] {}, tidsetEmptySet, database.size(), frequentItems, 0, new BitSet[0]);
		
		// we check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		
		// record the end time for statistics
		endTime = System.currentTimeMillis();

		// Return all frequent itemsets found!
		return generators; 
	}

	/**
	 * This is the main procedure of DefMe, which is called recursively to grow patterns
	 * @param itemsetX The itemset X.
	 * @param tidsetX  The tidset (cover) of X.
	 * @param supportX The support of X
	 * @param frequentItems The set of frequent items
	 * @param posTail  The set "tail" is defined as the interval [postail, frequentItems.size()-1] in "frequentItems".
	 * @param critItemsetX The critical objects of each item from the itemset X, stored in an array.
	 * @throws IOException if an error occured while writing result to disk
	 */
	private void defme(int[] itemsetX, BitSet tidsetX, int supportX,
			List<Integer> frequentItems, int posTail, BitSet[] critItemsetX) throws IOException {
		
		// If not the empty set
		if(itemsetX.length != 0) {
		// check if for all e in X,  COV*(X, e) != emptyset
			for(BitSet covStarXe : critItemsetX) {
				if(covStarXe.cardinality() ==0) {
					// if the critical object (COV*) is empty, return..
					return;
				}
			}
		}
		
		// save the itemset
		save(itemsetX,  tidsetX, supportX);
		
		// for all e in tail
		for(int i=posTail; i< frequentItems.size(); i++) {
			// Calculate e
			Integer e = frequentItems.get(i);
			
			// Calculate Cov(e), i.e. the tidset of e
			BitSetSupport tidsetE = mapItemTIDS.get(e);
			
			// Calculate Xe, i.e. X U {e}
			int[] xe = new int[itemsetX.length+1];
			System.arraycopy(itemsetX, 0, xe, 0, itemsetX.length);
			xe[itemsetX.length] = e;
			
			// Calculate cov(Xe), i.e. tidset(X U {e})
			BitSet tidsetXe = (BitSet)tidsetX.clone();
			tidsetXe.and(tidsetE.bitset);
			
			// The support of XU{e} is the cardinality of its tidset
			int supportXe = tidsetXe.cardinality();
			
			// If XU{e} is infrequent, we don't need to consider it anymore
			if(supportXe < minsupRelative) {
				continue;
			}
			
			// ==  Calculate critical objects (cov*(Y, e)) for each item e in Y = XU{e} == 
			BitSet[] critItemsetY = new BitSet[xe.length];
			
			// For the item e
			BitSet critE = (BitSet)tidsetX.clone();
			critE.andNot(tidsetE.bitset);
			critItemsetY[critItemsetY.length-1] = critE;
			
			// For any other item e' in X 
			for(int j=0; j< itemsetX.length; j++) {
				// calculate cov* as follows:
				critItemsetY[j] = (BitSet)critItemsetX[j].clone();
				critItemsetY[j].and(tidsetE.bitset);
			}
				
			// recursive call to explore patterns by extending XU{e} with items from "tail"
			defme(xe, tidsetXe, supportXe, frequentItems, i+1, critItemsetY);
		}
	}

	/**
	 * Save an itemset to disk or memory (depending on what the user chose).
	 * @param itemsetArray the itemset to be saved
	 * @param tidset the tidset and support of this itemset 
	 * @param support the support of that itemset
	 * @throws IOException if an error occurrs when writing to disk.
	 */
	private void save(int[] itemsetArray, BitSet tidset, int support) throws IOException {
		// increase the itemset count
		itemsetCount++;
		// if the result should be saved to memory
		if(writer == null){
			// Create an object "Itemset" and add it to the set of frequent itemsets
			Itemset itemset = new Itemset(itemsetArray);
			itemset.setTIDs(tidset, support);
			generators.addItemset(itemset, itemset.size());
		}else{
			// if the result should be saved to a file
			// write it to the output file
			StringBuffer buffer = new StringBuffer();
			for(int item: itemsetArray) {
				buffer.append(item);
				buffer.append(" ");
			}
			// as well as its support
			buffer.append("#SUP: ");
			buffer.append(support);
			writer.write(buffer.toString());
			writer.newLine();
		}
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  DefMe - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + database.size());
		System.out.println(" Generator itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("===================================================");
	}

	/**
	 * Get the set of frequent itemsets.
	 * @return the frequent itemsets (Itemsets).
	 */
	public Itemsets getItemsets() {
		return generators;
	}
	
	/**
	 * Anonymous inner class to store a bitset and its cardinality
	 * (an itemset's tidset and its support).
	 * Storing the cardinality is useful because the cardinality() method
	 * of a bitset in Java is very expensive, so it should not be called
	 * more than once.
	 */
	public class BitSetSupport{
		BitSet bitset = new BitSet();
		int support;
	}
}
