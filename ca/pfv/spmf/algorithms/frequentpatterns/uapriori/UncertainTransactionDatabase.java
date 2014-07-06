package ca.pfv.spmf.algorithms.frequentpatterns.uapriori;
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
 * This class represents a transactions database with existential probabilities,
 * as used by the UApriori algorithm uncertain itemset mining.
 * 
 * @see AlgoUApriori
 * @see UncertainTransactionDatabase
 * @see ItemsetUApriori
 * @see ItemUApriori
 * @author Philippe Fournier-Viger
 */
public class UncertainTransactionDatabase {

	// this is the set of items in the database
	private final Set<ItemUApriori> allItems = new HashSet<ItemUApriori>();
	// this is the list of transactions in the database
	private final List<ItemsetUApriori> transactions = new ArrayList<ItemsetUApriori>();
	
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
				processTransactions(thisLine.split(" "));
			}
		} catch (Exception e) {
			// catch exceptions
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				// close the file
				myInput.close();
			}
		}
	}

	private void processTransactions(String itemsString[]) {
		// We assume that there is no empty line
		
		// create a new itemset oject representing the transaction
		ItemsetUApriori transaction = new ItemsetUApriori();
		// for each item
		for (String itemString : itemsString) {
			// get the position of left parenthesis and right parenthesis
			int indexOfLeftParanthesis = itemString.indexOf('(');
			int indexOfRightParanthesis = itemString.indexOf(')');
			// get the item ID
			int itemID = Integer.parseInt(itemString.substring(0,
					indexOfLeftParanthesis));
			// get the existential probability
			double value = Double.parseDouble(itemString.substring(
					indexOfLeftParanthesis + 1, indexOfRightParanthesis));

			// create an item
			ItemUApriori item = new ItemUApriori(itemID, value);
			// add it to the transaction
			transaction.addItem(item);
			// add it to the set of all items
			allItems.add(item);
		}
		// add the itemset to the transaction to the in-memory database
		transactions.add(transaction);
	}

	/**
	 * Print this database to System.out.
	 */
	public void printDatabase() {
		System.out
				.println("===================  UNCERTAIN DATABASE ===================");
		int count = 0;
		// for each transaction
		for (ItemsetUApriori itemset : transactions) {
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
	public int size() {
		return transactions.size();
	}

	/**
	 * Get the list of transactions.
	 * @return the list of Transactions.
	 */
	public List<ItemsetUApriori> getTransactions() {
		return transactions;
	}

	/**
	 * Get the set of items in this database.
	 * @return a Set of Integers
	 */
	public Set<ItemUApriori> getAllItems() {
		return allItems;
	}

}
