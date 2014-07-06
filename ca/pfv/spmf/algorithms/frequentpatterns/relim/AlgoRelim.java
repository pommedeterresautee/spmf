package ca.pfv.spmf.algorithms.frequentpatterns.relim;
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

import ca.pfv.spmf.tools.MemoryLogger;


/**
 * This is an implementation of the RELIM algorithm for mining frequent itemsets. RELIM is proposed by :
 * <br/><br/>
 * 
 * Borgelt, C. (2005) Keeping Things Simple: Finding Frequent Item Sets by Recursive Elimination
 * Workshop Open Source Data Mining Software (OSDM'05, Chicago, IL), 66-70.
 * ACM Press, New York, NY, USA 2005<br/><br/>
 * 
 * RELIM is not a very efficient frequent itemset mining algorithm, but I decided to implement it
 * because it is simple.<br/><br/>
 * 
 * Note that it might not be implemented in a very optimized way. One reason is that in the original
 * article there is no pseudo-code for the algorithm.
 *
 * @see DatabaseStructureRelim
 * @author Philippe Fournier-Viger
 */
public class AlgoRelim {
	// for statistics
	private long startTimestamp;  // the start time
	private long endTimestamp;    // the end time
	private int relativeMinsupp;  // the minimum support as a relative value (integer)
	
	// the array 
	private int items[];

	// object to write the result to a file
	BufferedWriter writer = null;
	
	// the number of frequent itemsets found (for
	// statistics)
	private int frequentCount; 
	
	/**
	 * Default constructor
	 */
	public AlgoRelim() {
	}

	/**
	 * Run the algorithm
	 * @param minsupp minimum support threshold
	 * @param input the file path of the input file
	 * @param output the file path of the desired output file
	 * @throws IOException exception if error reading/writing files
	 */
	public void runAlgorithm(double minsupp, String input, String output) throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// prepare output file
		writer = new BufferedWriter(new FileWriter(output));
		// reset the number of itemsets found to 0
		frequentCount = 0;
		// reset the utility for checking the memory usage
		MemoryLogger.getInstance().reset();
		
		// reset the number of transactions to 0
		int transactionCount =0;
		
		// (1) Scan the database and count the support of each item (in a map)
		// for this map : key = item value = tidset
		final Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
		// scan the database
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transaction) until the end of file
		while (((line = reader.readLine()) != null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction into items
			String[] lineSplited = line.split(" ");
			// for each item in the
			// transaction
			for (String itemString : lineSplited) { 
				// convert item to integer
				Integer item = Integer.parseInt(itemString);
				// increase the support count of the item
				Integer count = mapSupport.get(item);
				if (count == null) {
					mapSupport.put(item, 1);
				} else {
					mapSupport.put(item, ++count);
				}
			}
			// increase transaction count
			transactionCount++;
		}
		// close the input file
		reader.close();

		// transform the minimum support from absolute to relative value
		// by multiplying by the number of transactions
		this.relativeMinsupp = (int) Math.ceil(minsupp * transactionCount);
		
		
		// (2) Sort items by frequency and then lexical ordering
		
		// a list to store items
		List<Integer> listItems = new ArrayList<Integer>();
		// for each item
		for(Entry<Integer,Integer> entry : mapSupport.entrySet()){
			Integer item = entry.getKey();
			// if it is frequent add it to the list
			if(mapSupport.get(item) >= relativeMinsupp){
				listItems.add(item);
			}
		}
		// sort the list
		Collections.sort(listItems, new Comparator<Integer>(){
			public int compare(Integer item1, Integer item2){
				// compare the support
				int compare = mapSupport.get(item1) - mapSupport.get(item2);
				// if same support, use lexical order
				if(compare ==0){
					return (item1- item2);
				}
				// otherwise, use the support
				return compare;
			}
		});

		//(3) Create initial database structure
		
		// This array will contain the support of each item
		// position i = support of item i
		int supports[] = new int[listItems.size()];
		
		// put all frequent items in an array
		items = new int[listItems.size()];
		for(int i=0; i< listItems.size(); i++){
			items[i] = listItems.get(i);
		}
		// create adatabase structure
		DatabaseStructureRelim initialDatabase =  new DatabaseStructureRelim(supports);
		initialDatabase.initializeTransactions();
		
		// insert transactions into initial database structure...
		reader = new BufferedReader(new FileReader(input));
		
		// for each line (transaction) until the end of file
		while (((line = reader.readLine()) != null)) { 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			
			// split the transaction into items
			String[] lineSplited = line.split(" ");
			
			// create a list to store items of the transaction
			List<Integer> transaction = new ArrayList<Integer>();
			
			//for each item
			for (String itemString : lineSplited) {
				
				// convert item to integer
				Integer item = Integer.parseInt(itemString);
				
				// if frequent add it to the transaction otherwise ignore it
				if(mapSupport.get(item) >= relativeMinsupp){
					transaction.add(item);  
				}
			}
			
			// if the transaction is empty, then we just ignore it
			if(transaction.size() ==0){
				continue;
			}
			
			// Otherwise sort the transaction according to the frequency of items
			Collections.sort(transaction, new Comparator<Integer>(){
				public int compare(Integer item1, Integer item2){
					// first compare the support
					int compare = mapSupport.get(item1) - mapSupport.get(item2);
					// if equals then use the lexical order
					if(compare ==0){
						return (item1 - item2);
					}
					//otherwise use the support
					return compare;
				}
			});
			
			// increase the support of the first item of this transaciton
			int firstItem = transaction.get(0);
			int indexArray = listItems.indexOf(firstItem);
			supports[indexArray]++;

			// insert transaction in the data structure 
			initialDatabase.transactions.get(indexArray).add(transaction.subList(1, transaction.size()));
		}
		// close the input file
		reader.close();	
		
		// (7) START RECURSION
		
		// call the recursive procedure to discover itemsets
		recursion(initialDatabase, new int[0]);
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		
		// close the output file
		writer.close();
		
		// record end time
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Recursive method for discovering frequent itemsets starting with a given prefix.
	 * @param database  the database structure
	 * @param prefix  the current prefix
	 * @throws IOException exception if error writing to the output file
	 */
	private void recursion(DatabaseStructureRelim database, int[] prefix) throws IOException {
		
		// for each item
		for(int i=0; i< items.length; i++){
			// if the support is higher than 0
			if(database.supports[i] > 0 ){
				// Check if frequent
				if(database.supports[i]>= relativeMinsupp){
					// (1) add the frequent itemset to the set of frequent itemsets found!
					writeOut(items[i], prefix, database.supports[i]);
				}
				// for each transaction for this item
				database.supports[i] = 0; // empty list for i
				
				//  create new prefix
				int[] newSupportPrefix = new int[database.supports.length];
				
				// create new database structure for that prefix
				DatabaseStructureRelim databasePrefix =  new DatabaseStructureRelim(newSupportPrefix);
				databasePrefix.initializeTransactions();
	
				// for each transaction in the database
				for(List<Integer> transaction : database.transactions.get(i)){
						// if the transaction is empty, then skip it
						if(transaction.size() == 0){  
							continue;
						}
						// Get the first item
						Integer firstItem = transaction.get(0);
						// find its position in the item array
						int index = getIndexOf(firstItem);
						// increase its support
						database.supports[index]++;
						// increase its support with respect to the new prefix
						newSupportPrefix[index]++;
						// if the transaction has more than two items
						if(transaction.size() >= 2){
							// create sublist as described in the paper
							List<Integer> subList = transaction.subList(1, transaction.size());
							// Get the database prefix
							databasePrefix.transactions.get(index).add(subList);
							// add the sublist at the item index
							database.transactions.get(index).add(subList);
						}
				}
				
				// Create the new prefix for recursion by appending the item at i
				int []newPrefix = new int[prefix.length+1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = items[i];
	
				// recursive call
				recursion(databasePrefix, newPrefix);
			}
		}
		// check the memory usage for statistics purpose
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 *  Get the position of an item in the list of all items
	 * @param item the item that is searched
	 * @return the position (integer) or -1 if it is not there
	 */
	private int getIndexOf(int item){ 
		// for each item
		for(int i=0; i < items.length; i++){
			// if it is equal to the item that we search, return the position
			if(item == items[i]){
				return i;
			}
		}
		// not found, then return -1
		return -1;
	}

	/**
	 * Write a frequent itemset to the output file.
	 * @param prefix the itemset
	 * @param item an item that should be appended to the itemset
	 * @param support the support of the itemset with the item
	 * @throws IOException exception if error while writing to the output file.
	 */
	private void writeOut(int item, int[]prefix, int support) throws IOException{
		// increase the number of itemsets found
		frequentCount++; 
		// create a string uffer
		StringBuffer buffer = new StringBuffer();
		// add the item
		buffer.append(item);
		buffer.append(" ");
		// next add all other items from the itemset
		for (int i = 0; i < prefix.length; i++) {
			buffer.append(prefix[i]);
			if (i != prefix.length - 1) {
				buffer.append(' ');
			}
		}
		//Finally, write the support.
		buffer.append(" #SUP: ");
		buffer.append(support);
		writer.write(buffer.toString());
		writer.newLine(); // create new line to be ready for next itemset
	}

	/**
	 * Print statistics about the latest execution of the algorithm to System.out
	 */
	public void printStatistics() {

		System.out.println("========== RELIM - STATS ============");
		System.out.println(" Number of frequent  itemsets: " + frequentCount);
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("=====================================");
	}
	
}
