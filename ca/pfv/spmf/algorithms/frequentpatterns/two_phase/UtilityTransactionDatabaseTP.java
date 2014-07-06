package ca.pfv.spmf.algorithms.frequentpatterns.two_phase;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a transaction database with utility values, 
 * as used by the Two-Phase algorithm for high 
 * utility itemset mining.
 *
 * @see AlgoTwoPhase
 * @see TransactionTP
 * @author Philippe Fournier-Viger
 */
public class UtilityTransactionDatabaseTP {

	// this is the set of items in the database
	private final Set<Integer> allItems = new HashSet<Integer>();
	// this is the list of transactions in the database
	private final List<TransactionTP> transactions = new ArrayList<TransactionTP>();
	
	/**
	 * Add a transaction.
	 * @param t the transaction to be added
	 */
	public void addTransaction(TransactionTP t){
		// add it
		transactions.add(t);
		// add its item to the set of items
		allItems.addAll(t.getItems());
	}

	/**
	 * Load a transaction database from a file.
	 * @param path the path of the file
	 * @throws IOException exception if error while reading the file.
	 */
	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			// for each transaction (line) in the input file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// process the transaction
				processTransaction(thisLine.split(":"));
			}
		} catch (Exception e) {
			// catch exceptions
			e.printStackTrace();
		}finally {
			if(myInput != null){
				// close the file
				myInput.close();
			}
	    }
	}
	
	/**
	 * Process a line (transaction) from the input file
	 * @param line  a line
	 */
	private void processTransaction(String line[]){
		// get the transaction utility
		int transactionUtility = Integer.parseInt(line[1]);
		
		
		// Create a list for storing items
		List<Integer> items = new ArrayList<Integer>();
		// for each item
		for(String item:  line[0].split(" ")){
			// convert the item to integer before storing it in the list
			items.add(Integer.parseInt(item));
		}
		
		// Create a list for storing item utility values
		List<Integer> itemsUtilities = new ArrayList<Integer>();
		for(String utility:  line[2].split(" ")){
			// convert the item to an integer and add it to the list of item utilities
			itemsUtilities.add(Integer.parseInt(utility));
		}
		
		// sort the items according to item utilities
		bubbleSort(items, itemsUtilities);
		
		// add the transaction to the list of transactions
		transactions.add(new TransactionTP(items, itemsUtilities, transactionUtility));
	}
	
	/**
	 * This perform a bubble sort on two lists at the same time (a list
	 * of items in a transaction and a list of corresponding utility values)
	 * @param items  a list of items
	 * @param itemsUtilities  a corresponding list of utility values
	 */
	private void bubbleSort(List<Integer> items, List<Integer> itemsUtilities) {
		// This is just a modified bubble sort algorithm.
		// Explanation of the bubble sort algorithm can be found online and
		// in most reference books on algorithms.
			for(int i=0; i < items.size(); i++){
				for(int j= items.size() -1; j>= i+1; j--){
					if(items.get(j) < items.get(j-1)){
						int temp = items.get(j);
						items.set(j, items.get(j-1));
						items.set(j-1, temp);
						int tempUtilities = itemsUtilities.get(j);
						itemsUtilities.set(j, itemsUtilities.get(j-1));
						itemsUtilities.set(j-1, tempUtilities);
					}
				}
			}
		}

	/**
	 * Print this database to System.out.
	 */
	public void printDatabase(){
		System.out
		.println("===================  Database ===================");
		int count = 0;
		// for each transaction
		for(TransactionTP itemset : transactions){
			// print the transaction
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	/**
	 * Get the number of transactions.
	 * @return a int
	 */
	public int size(){
		return transactions.size();
	}

	/**
	 * Get the list of transactions.
	 * @return the list of Transactions.
	 */
	public List<TransactionTP> getTransactions() {
		return transactions;
	}

	/**
	 * Get the set of items in this database.
	 * @return a Set of Integers
	 */
	public Set<Integer> getAllItems() {
		return allItems;
	}

}
