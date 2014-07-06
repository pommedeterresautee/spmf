package ca.pfv.spmf.algorithms.frequentpatterns.cfpgrowth;
/* This file is copyright (c) 2008-2013 Azadeh Soltani
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
import java.io.FileNotFoundException;
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
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the CFPGrowth++ algorithm. CFPGrowth++ was proposed in this paper:
 * <br/><br/>
 *
 * Kiran, R. U., & Reddy, P. K. (2011). Novel techniques to reduce search space 
 * in multiple minimum supports-based frequent pattern mining algorithms. 
 * In Proceedings of the 14th International Conference on Extending Database 
 * Technology, ACM (pp. 11-20). 
 *
 * and it is an optimization of the original CFPGrowth algorithm:
 * 
 * Hu, Y. H., & Chen, Y. L. (2006). Mining association rules with multiple minimum supports: a new mining algorithm and a support tuning mechanism. Decision Support Systems, 42(1), 1-24.
 * <br/><br/>
 *   
 * This is an optimized version that saves the result to a file
 * or keep it into memory if no output path is provided
 * by the user to the runAlgorithm method().
 * 
 * This implementation was made by Azadeh Soltani based on the FPGrowth
 * implementation by Philippe Fournier-Viger
 * 
 * @see MISNode
 * @see MISTree
 * @author Azadeh Soltani
 */
public class AlgoCFPGrowth {

	// for statistics
	private long startTimestamp; // start time of the latest execution
	private long endTime; // end time of the latest execution
	private int transactionCount = 0; // transaction count in the database
	private int itemsetCount; // number of freq. itemsets found
	
	// object to write the output file
	BufferedWriter writer = null;
	
	// The  patterns that are found 
	// (if the user want to keep them into memory)
	protected Itemsets patterns = null;

	// the comparator that is used to compare the item ordering
	final Comparator<Integer> itemComparator;
	
	// array indicating the minimum support for each item
	int MIS[];
	// the minimum MIS
	int minMIS;
	
	/** Object to check the maximum memory usage */
	private MemoryLogger memoryLogger = null;

	/**
	 * Constructor
	 */
	public AlgoCFPGrowth() {
		// Create a comparator that will be used to establish a total
		// order between items.
		itemComparator = new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				// compare according to MIS value
				int compare = MIS[o2] - MIS[o1]; 
				if (compare == 0) { // if the same MIS, we check the lexical
									// ordering!
					return (o1 - o2);
				}
				return compare;
			}
		};
	}
	
	/**
	 * Run the algorithm.
	 * @param input the path to an input file containing a transaction database.
	 * @param output the output file path for saving the result (if null, the result 
	 *        will be returned by the method instead of being saved).
	 * @param MISIn path to a file containing the MIS thresholds.
	 * @return the result if no output file path is provided.
	 * @throws IOException if error reading/writing files
	 */
	public Itemsets runAlgorithm(String input, String output, String MISIn)
			throws FileNotFoundException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		//initialize tool to record memory usage
		memoryLogger = new MemoryLogger();
		memoryLogger.checkMemory();
		
		
		// if the user want to keep the result into memory
		if(output == null){
			writer = null;
			patterns =  new Itemsets("FREQUENT ITEMSETS");
	    }else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(output)); 
		}
		
		// (1) PREPROCESSING: Perform an initial database scan to determine the 
		// MIS of each item
		
		// This map is used to count the support of each item
		// Key: item   Value: support
		final Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();

		// az---initializing MISs--------------
		initMISfromFile(MISIn);

		// reset the number of frequent itemsets to 0
		itemsetCount = 0;

		// (2) Scan the database to build the initial FP-Tree
		// Before inserting a transaction in the FPTree, we sort the items
		// by decreasing order of MIS.
		MISTree tree = new MISTree();

		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// read the transaction database line (transaction) by line
		// until the end of file
		while (((line = reader.readLine()) != null)) { 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the current transaction into items (they are separated by spaces)
			String[] lineSplited = line.split(" ");
			List<Integer> transaction = new ArrayList<Integer>();
			
			// for each item in the transaction
			for (String itemString : lineSplited) { 
				// convert item to integer
				Integer item = Integer.parseInt(itemString);
				
				// increase the support of the item by 1
				Integer count = mapSupport.get(item);
				if (count == null) {
					mapSupport.put(item, 1);
				} else {
					mapSupport.put(item, ++count);
				}
				// all items are added to transactions
				transaction.add(item);
			}
			transactionCount++; // increase the number of transactions
			
			// sort item in the transaction by non increasing order of MIS
			Collections.sort(transaction, this.itemComparator);
			
			// add the sorted transaction to the MISTree.
			tree.addTransaction(transaction);
			
		}// while
		reader.close();  // close the input file
		
		// tree.print(tree.root);

		// We create the header table for the tree
		tree.createHeaderList(this.itemComparator);

		// We search for for items with support smaller than minMIS and remove
		// them from the tree
		boolean sw = false;
		// for each item
		for (Entry<Integer, Integer> entry : mapSupport.entrySet()) {
			// if the support is lower than the minimum MIS value
			if (entry.getValue() < minMIS) {
				//  remove from header list
				tree.deleteFromHeaderList(entry.getKey(), itemComparator);
				// System.out.println(entry.getKey());

				// remove from the tree
				tree.MISPruning(entry.getKey());
				// System.out.println(entry.getKey());
				// tree.print(tree.root);
				sw = true;
			}// if
		}// for
			
		// merge child node with the same item id
		if (sw == true) {
			tree.MISMerge(tree.root);
		}
		// tree.print(tree.root);

		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, prefix alpha is empty.
		int[] prefixAlpha = new int[0];

		if(tree.headerList.size() > 0) {
			cfpgrowth(tree, prefixAlpha, transactionCount, mapSupport);
		}

		// check the memory usage
		memoryLogger.checkMemory();
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		// record end time
		endTime = System.currentTimeMillis();
		
		return patterns;
	}

	/**
	 * Read MIS values from the MIS file.
	 * @param input path to the file containing the MIS values
	 * @throws IOException if error occurs while reading the file or the file does not exist
	 */
	private void initMISfromFile(String input) throws FileNotFoundException,
			IOException {
		// create object to read the file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		minMIS = Integer.MAX_VALUE;  // to store the minimum MIS value
		int maxItemID = 0;  // to store the largest item id
		
		// A map to store the MIS of each item
		//  key :  item   value : MIS
		final Map<Integer, Integer> mapMIS = new HashMap<Integer, Integer>();
		
		 // For reach line (transaction) until the end of the file
		while (((line = reader.readLine()) != null)) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line according to spaces
			String[] lineSplited = line.split(" ");
			// convert item to integer
			Integer item = Integer.parseInt(lineSplited[0]);
			// convert MIS to integer
			Integer itemMIS = Integer.parseInt(lineSplited[1]);
			
			// update minimum MIS if necessary
			if ((minMIS > itemMIS) && (itemMIS != 0)) {
				minMIS = itemMIS;
			}
			// record the MIS for that item in the map
			mapMIS.put(item, itemMIS);
			
			// update maximum item ID if necessary
			if (item > maxItemID) {
				maxItemID = item;
			}
		}
		// Store the values from the map in an array for more efficiency
		MIS = new int[maxItemID + 1];
		for (Entry<Integer, Integer> entry : mapMIS.entrySet()) {
			MIS[entry.getKey()] = entry.getValue();
		}
		// close the file
		reader.close();
	}

//	/**
//	 * This method used the frequency of items to generate an MIS value by
//	 * using the function presented in the article of MISApriori  
//	 * (This method is an alternative to initMISfromFile() and is not used
//	 * by default).
//	 * @param input  the input file
//	 * @param mapSupport  a
//	 * @param beta
//	 * @param LS
//	 * @return
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 */
//	private int initMISfromFrequency(String input,
//			final Map<Integer, Integer> mapSupport, double beta, double LS)
//			throws FileNotFoundException, IOException {
//		int maxItemID = 0;
//		BufferedReader reader = new BufferedReader(new FileReader(input));
//		String line;
//		// for each transaction
//		while (((line = reader.readLine()) != null)) { 
//			String[] lineSplited = line.split(" ");
//			// for each item in the transaction
//			for (String itemString : lineSplited) { 
//				// increase the support count of the item
//				Integer item = Integer.parseInt(itemString);
//				Integer count = mapSupport.get(item);
//				if (count == null) {
//					mapSupport.put(item, 1);
//					// az
//					if (maxItemID < item)
//						maxItemID = item;
//				} else {
//					mapSupport.put(item, ++count);
//				}
//			}
//			transactionCount++;
//		}
//		reader.close();
//		MIS = new int[maxItemID + 1];
//		minMIS = 1;
//		int LSRelative = (int) Math.ceil(LS * transactionCount);
//		for (Entry<Integer, Integer> entry : mapSupport.entrySet()) {
//			// calculate the MIS value
//			MIS[entry.getKey()] = (int) (beta * entry.getValue());
//			if (MIS[entry.getKey()] < LSRelative) {
//				MIS[entry.getKey()] = LSRelative;
//			}// if
//			if (MIS[entry.getKey()] < minMIS) {
//				minMIS = MIS[entry.getKey()];
//			}// if
//		}// for
//
//		return minMIS;
//	}
//
//	// end az-------------------------------------------------------------------

	/**
	 * This method mines pattern from a Prefix-Tree recursively
	 * 
	 * @param tree    The Prefix Tree
	 * @param prefix  The current prefix "alpha"
	 * @param mapSupport    The frequency of each item in the prefix tree.
	 * @throws IOException exception if error writing the output file.
	 */
	private void cfpgrowth(MISTree tree, int[] prefixAlpha, int prefixSupport,
			Map<Integer, Integer> mapSupport) throws IOException {

		// String test = "";
		// for(int item : prefixAlpha){
		// test += item + " ";
		// }
		// System.out.println(test);

		// We check if there is only one item in the header table
		if (tree.headerList.size() == 1) {
			MISNode node = tree.mapItemNodes.get(tree.headerList.get(0));
			// If this node has no child
			if (node.nodeLink == null) {
				// If the support of this node is higher than the MIS of the first item
				// of the current prefix alpha
				if (node.counter >= MIS[prefixAlpha[0]]) {
					
					//write the itemset to the output file
					writeItemsetToFile(prefixAlpha, node.itemID, node.counter);
				}
				// end of code that i moved
			} else {
				// recursive call
				cfpgrowthMoreThanOnePath(tree, prefixAlpha, prefixSupport,
						mapSupport);
			}
		} else { // There is more than one path, recursive call
			cfpgrowthMoreThanOnePath(tree, prefixAlpha, prefixSupport,
					mapSupport);
		}
	}

	/**
	 * Mine an FP-Tree having more than one path.
	 * 
	 * @param tree        the FP-tree
	 * @param prefix      the current prefix, named "alpha"
	 * @param mapSupport  the frequency of items in the FP-Tree
	 * @throws IOException  exception if error writing the file
	 */
	private void cfpgrowthMoreThanOnePath(MISTree tree, int[] prefixAlpha,
			int prefixSupport, Map<Integer, Integer> mapSupport)
			throws IOException {
		// We process each frequent item in the header table list of the tree in
		// reverse order.
		for (int i = tree.headerList.size() - 1; i >= 0; i--) {
			
			// get the item and its support
			Integer item = tree.headerList.get(i);
			int support = mapSupport.get(item);
			
			// if the item is not frequent, we skip it
			int mis = (prefixAlpha.length == 0) ? MIS[item]
					: MIS[prefixAlpha[0]]; // pfv
			if (support < mis)
				continue;
			
			// Let's Beta be the concatenation of Alpha with the current item
			int betaSupport = (prefixSupport < support) ? prefixSupport
					: support;
			// az
			// int mis = (prefixAlpha.length == 0) ? MIS[item] :
			// MIS[prefixAlpha[0]]; // pfv

			// if the support is higher than the MIS
			if (support >= mis) {
				// save the itemset to the file
				writeItemsetToFile(prefixAlpha, item, betaSupport); 
			}

			// === Construct beta's conditional pattern base ===
			// It is a subdatabase which consists of the set of prefix paths
			// in the FP-tree co-occuring with the suffix pattern.
			List<List<MISNode>> prefixPaths = new ArrayList<List<MISNode>>();
			MISNode path = tree.mapItemNodes.get(item);
			// for each path
			while (path != null) {
				// if the path is not just the root node
				if (path.parent.itemID != -1) {
					// create the prefixpath
					List<MISNode> prefixPath = new ArrayList<MISNode>();
					
					// add this node.
					prefixPath.add(path);
					// NOTE: we add it just to keep its support,
					// actually it should not be part of the prefixPath

					// Recursively add all the parents of this node.
					MISNode parent = path.parent;
					while (parent.itemID != -1) {
						prefixPath.add(parent);
						parent = parent.parent;
					}
					prefixPaths.add(prefixPath);
				}
				// We will look for the next prefixpath
				path = path.nodeLink;
			}

			// (A) Calculate the frequency of each item in the prefixpath
			Map<Integer, Integer> mapSupportBeta = new HashMap<Integer, Integer>();
			// for each prefixpath
			for (List<MISNode> prefixPath : prefixPaths) {
				// the support of the prefixpath is the support of its first
				// node.
				int pathCount = prefixPath.get(0).counter;
				// for each node, except the first one, we
				// count the frequency
				for (int j = 1; j < prefixPath.size(); j++) { 
					// Get the node
					MISNode node = prefixPath.get(j);
					if (mapSupportBeta.get(node.itemID) == null) {
						mapSupportBeta.put(node.itemID, pathCount);
					} else {
						mapSupportBeta.put(node.itemID,
								mapSupportBeta.get(node.itemID) + pathCount);
					}
				}
			}

			// (B) Construct beta's conditional FP-Tree
			MISTree treeBeta = new MISTree();
			// add each prefixpath in the FP-tree
			for (List<MISNode> prefixPath : prefixPaths) {
				treeBeta.addPrefixPath(prefixPath, mapSupportBeta, minMIS); 
			}
			// create the header list
			treeBeta.createHeaderList(itemComparator); 

			// System.out.println();
			// treeBeta.print(treeBeta.root);

			// Mine recursively the Beta tree.
			if (treeBeta.root.childs.size() > 0) {
				// create beta
				int[] beta = new int[prefixAlpha.length + 1];
				System.arraycopy(prefixAlpha, 0, beta, 0, prefixAlpha.length);
				beta[prefixAlpha.length] = item;

				// recursive call to the main method to mine the conditional tree
				cfpgrowth(treeBeta, beta, betaSupport, mapSupportBeta);
			}
		}
	}

	/**
	 * Write a frequent itemset that is found to the output file.
	 * @param itemset  an itemset
	 * @param lastItem an item that should be appended to the itemset
	 * @param support the support of "itemset" + "item".
	 */
	private void writeItemsetToFile(int[] itemset, int lastItem, int support)
			throws IOException {
		// increase the number of frequent itemsets found
		itemsetCount++;
		
		// if the result should be saved to a file
		if(writer != null){
			// Create a string buffer
			StringBuffer buffer = new StringBuffer();
			// write the items of the itemset
			for(int i=0; i< itemset.length; i++){
				buffer.append(itemset[i]);
				buffer.append(' ');
			}
			buffer.append(lastItem);
			// Then, write the support
			buffer.append(" #SUP: ");
			buffer.append(support);
			// write to file and create a new line
			writer.write(buffer.toString());
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			// concatenate the last item to the itemset
			int [] itemsetWithLastItem = new int[itemset.length+1];
			System.arraycopy(itemset, 0, itemsetWithLastItem, 0, itemset.length);
			itemsetWithLastItem[itemset.length] = lastItem;
			
			Arrays.sort(itemsetWithLastItem); // ADDED TO FIX ASSOCIATION RULE BUG FOR CFPGROWTH+
			
			// create an object Itemset and add it to the set of patterns 
			// found.
			Itemset itemsetObj = new Itemset(itemsetWithLastItem);
			itemsetObj.setAbsoluteSupport(support);
			patterns.addItemset(itemsetObj, itemsetObj.size());
		}
	}
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  CFP-GROWTH - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ transactionCount);
		System.out.print(" Max memory usage: " + memoryLogger.getMaxMemory() + " mb \n");
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}

	/**
	 * Get the number of transactions in the last transaction database read.
	 * @return the number of transactions.
	 */
	public int getDatabaseSize() {
		return transactionCount;
	}
}
