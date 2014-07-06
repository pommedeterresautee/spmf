package ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth_with_strings;

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * This is an implementation of the FPGROWTH algorithm (Han et al., 2004) that take
 * as input a transaction database where items are represented by strings rather
 * than integers.
 * FPGrowth is described here:
 * <br/><br/>
 * 
 * Han, J., Pei, J., & Yin, Y. (2000, May). Mining frequent patterns without candidate generation. In ACM SIGMOD Record (Vol. 29, No. 2, pp. 1-12). ACM
 * <br/><br/>
 * 
 * This is an optimized version that saves the result to a file.
 *
 * @see FPTree_Strings
 * @author Philippe Fournier-Viger
 */
public class AlgoFPGrowth_Strings {

	
	// for statistics
	private long startTimestamp; // start time of the latest execution
	private long endTime; // end time of the latest execution
	private int transactionCount = 0; // transaction count in the database
	private int itemsetCount; // number of freq. itemsets found
	
	// minimum support threshold
	public int relativeMinsupp;
	
	// object to write the output file
	BufferedWriter writer = null; 
	

	/**
	 * Default constructor
	 */
	public AlgoFPGrowth_Strings() {
		
	}

	/**
	 * Run the algorithm.
	 * @param input the file path of an input transaction database.
	 * @param output the path of the desired output file
	 * @param minsupp minimum support threshold as a percentage (double)
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, double minsupp) throws FileNotFoundException, IOException {
		// record the start time
		startTimestamp = System.currentTimeMillis();
		// reinitialize the number of itemsets found to 0
		itemsetCount =0;
		// Prepare the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// (1) PREPROCESSING: Initial database scan to determine the frequency of each item
		// The frequency is store in a map where:
		// key: item   value: support count
		final Map<String, Integer> mapSupport = new HashMap<String, Integer>();
		// call this method  to perform the database scan
		scanDatabaseToDetermineFrequencyOfSingleItems(input, mapSupport);
		
		// convert the absolute minimum support to a relative minimum support
		// by multiplying by the database size.
		this.relativeMinsupp = (int) Math.ceil(minsupp * transactionCount);
		
		// (2) Scan the database again to build the initial FP-Tree
		// Before inserting a transaction in the FPTree, we sort the items
		// by descending order of support.  We ignore items that
		// do not have the minimum support.
		
		// create the FPTree
		FPTree_Strings tree = new FPTree_Strings();
		
		
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) in the input file until the end of file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction into items
			String[] lineSplited = line.split(" ");
			// create an array list to store the items
			List<String> transaction = new ArrayList<String>();
			// for each item in the transaction
			for(String itemString : lineSplited){  
				// if it is frequent, add it to the transaction
				// otherwise not because it cannot be part of a frequent itemset.
				if(mapSupport.get(itemString) >= relativeMinsupp){
					transaction.add(itemString);	
				}
			}
			// sort item in the transaction by descending order of support
			Collections.sort(transaction, new Comparator<String>(){
				public int compare(String item1, String item2){
					// compare the support
					int compare = mapSupport.get(item2) - mapSupport.get(item1);
					// if the same support, we check the lexical ordering!
					if(compare == 0){ 
						return item1.compareTo(item2);
					}
					// otherwise use the support
					return compare;
				}
			});
			// add the sorted transaction to the fptree.
			tree.addTransaction(transaction);
		}
		// close the input file
		reader.close();
		
		// We create the header table for the tree
		tree.createHeaderList(mapSupport);
		
		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, the prefix alpha is empty.
		String[] prefixAlpha = new String[0];
		if(tree.headerList.size() > 0) {
			fpgrowth(tree, prefixAlpha, transactionCount, mapSupport);
		}
		
		// close the output file
		writer.close();
		// record the end time
		endTime= System.currentTimeMillis();
		
//		print(tree.root, " ");
	}

//	private void print(FPNode node, String indentation) {
//		System.out.println(indentation + "NODE : " + node.itemID + " COUNTER" + node.counter);
//		for(FPNode child : node.childs) {
//			print(child, indentation += "\t");
//		}
//	}

	/**
	 * This method scans the input database to calculate the support of single items
	 * @param input the path of the input file
	 * @param mapSupport a map for storing the support of each item (key: item, value: support)
	 * @throws IOException  exception if error while writing the file
	 */
	private void scanDatabaseToDetermineFrequencyOfSingleItems(String input,
			final Map<String, Integer> mapSupport)
			throws FileNotFoundException, IOException {
		//Create object for reading the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction into items
			String[] lineSplited = line.split(" ");
			 // for each item in the transaction
			for(String itemString : lineSplited){ 
				// increase the support count of the item
				Integer count = mapSupport.get(itemString);
				if(count == null){
					mapSupport.put(itemString, 1);
				}else{
					mapSupport.put(itemString, ++count);
				}
			}
			// increase the transaction count
			transactionCount++;
		}
		// close the input file
		reader.close();
	}


	/**
	 * This method mines pattern from a Prefix-Tree recursively
	 * @param tree  The Prefix Tree
	 * @param prefix  The current prefix "alpha"
	 * @param mapSupport The frequency of each item in the prefix tree.
	 * @throws IOException   exception if error writing the output file
	 */
	private void fpgrowth(FPTree_Strings tree, String[] prefixAlpha, int prefixSupport, Map<String, Integer> mapSupport) throws IOException {
		// We need to check if there is a single path in the prefix tree or not.
		if(tree.hasMoreThanOnePath == false){
			// That means that there is a single path, so we 
			// add all combinations of this path, concatenated with the prefix "alpha", to the set of patterns found.
			addAllCombinationsForPathAndPrefix(tree.root.childs.get(0), prefixAlpha); // CORRECT?
			
		}else{ // There is more than one path
			fpgrowthMoreThanOnePath(tree, prefixAlpha, prefixSupport, mapSupport);
		}
	}
	
	/**
	 * Mine an FP-Tree having more than one path.
	 * @param tree  the FP-tree
	 * @param prefix  the current prefix, named "alpha"
	 * @param mapSupport the frequency of items in the FP-Tree
	 * @throws IOException   exception if error writing the output file
	 */
	private void fpgrowthMoreThanOnePath(FPTree_Strings tree, String [] prefixAlpha, int prefixSupport, Map<String, Integer> mapSupport) throws IOException {
		// We process each frequent item in the header table list of the tree in reverse order.
		for(int i= tree.headerList.size()-1; i>=0; i--){
			String item = tree.headerList.get(i);
			
			int support = mapSupport.get(item);
			// if the item is not frequent, we skip it
			if(support <  relativeMinsupp){
				continue;
			}
			// Create Beta by concatening Alpha with the current item
			// and add it to the list of frequent patterns
			String [] beta = new String[prefixAlpha.length+1];
			System.arraycopy(prefixAlpha, 0, beta, 0, prefixAlpha.length);
			beta[prefixAlpha.length] = item;
			
			// calculate the support of beta
			int betaSupport = (prefixSupport < support) ? prefixSupport: support;
			// save beta to the output file
			writeItemsetToFile(beta, betaSupport);
			
			// === Construct beta's conditional pattern base ===
			// It is a subdatabase which consists of the set of prefix paths
			// in the FP-tree co-occuring with the suffix pattern.
			List<List<FPNode_Strings>> prefixPaths = new ArrayList<List<FPNode_Strings>>();
			FPNode_Strings path = tree.mapItemNodes.get(item);
			while(path != null){
				// if the path is not just the root node
				if(path.parent.itemID != null){
					// create the prefixpath
					List<FPNode_Strings> prefixPath = new ArrayList<FPNode_Strings>();
					// add this node.
					prefixPath.add(path);   // NOTE: we add it just to keep its support,
					// actually it should not be part of the prefixPath
					
					//Recursively add all the parents of this node.
					FPNode_Strings parent = path.parent;
					while(parent.itemID != null){
						prefixPath.add(parent);
						parent = parent.parent;
					}
					// add the path to the list of prefixpaths
					prefixPaths.add(prefixPath);
				}
				// We will look for the next prefixpath
				path = path.nodeLink;
			}
			
			// (A) Calculate the frequency of each item in the prefixpath
			Map<String, Integer> mapSupportBeta = new HashMap<String, Integer>();
			// for each prefixpath
			for(List<FPNode_Strings> prefixPath : prefixPaths){
				// the support of the prefixpath is the support of its first node.
				int pathCount = prefixPath.get(0).counter;  
				 // for each node in the prefixpath,
				// except the first one, we count the frequency
				for(int j=1; j<prefixPath.size(); j++){ 
					FPNode_Strings node = prefixPath.get(j);
					// if the first time we see that node id
					if(mapSupportBeta.get(node.itemID) == null){
						// just add the path count
						mapSupportBeta.put(node.itemID, pathCount);
					}else{
						// otherwise, make the sum with the value already stored
						mapSupportBeta.put(node.itemID, mapSupportBeta.get(node.itemID) + pathCount);
					}
				}
			}
			
			// (B) Construct beta's conditional FP-Tree
			FPTree_Strings treeBeta = new FPTree_Strings();
			// add each prefixpath in the FP-tree
			for(List<FPNode_Strings> prefixPath : prefixPaths){
				treeBeta.addPrefixPath(prefixPath, mapSupportBeta, relativeMinsupp); 
			}  
			// Create the header list.
			treeBeta.createHeaderList(mapSupportBeta); 
			
			// Mine recursively the Beta tree if the root as child(s)
			if(treeBeta.root.childs.size() > 0){
				// recursive call
				fpgrowth(treeBeta, beta, betaSupport, mapSupportBeta);
			}
		}
		
	}

	/**
	 * This method is for adding recursively all combinations of nodes in a path, concatenated with a given prefix,
	 * to the set of patterns found.
	 * @param nodeLink the first node of the path
	 * @param prefix  the prefix
	 * @param minsupportForNode the support of this path.
	 * @throws IOException 
	 */
	private void addAllCombinationsForPathAndPrefix(FPNode_Strings node, String[] prefix) throws IOException {
		// Concatenate the node item to the current prefix
		String [] itemset = new String[prefix.length+1];
		System.arraycopy(prefix, 0, itemset, 0, prefix.length);
		itemset[prefix.length] = node.itemID;

		// save the resulting itemset to the file with its support
		writeItemsetToFile(itemset, node.counter);
			
		if(node.childs.size() != 0) {
			addAllCombinationsForPathAndPrefix(node.childs.get(0), itemset);
			addAllCombinationsForPathAndPrefix(node.childs.get(0), prefix);
		}
	}
	

	/**
	 * Write a frequent itemset that is found to the output file.
	 */
	private void writeItemsetToFile(String [] itemset, int support) throws IOException {
		// increase the number of itemsets found for statistics purpose
		itemsetCount++;
		
		// create a string buffer 
		StringBuffer buffer = new StringBuffer();
		// write items from the itemset to the stringbuffer
		for(int i=0; i< itemset.length; i++){
			buffer.append(itemset[i]);
			if(i != itemset.length-1){
				buffer.append(' ');
			}
		}
		// append the support of the itemset
		buffer.append(':');
		buffer.append(support);
		// write the strinbuffer and create a newline so that we are
		// ready for the next itemset to be written
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  FP-GROWTH - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + transactionCount);
		System.out.println(" Frequent itemsets count : " + itemsetCount); 
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
