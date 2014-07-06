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


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a set of high utility itemsets found by the Two-Phase algorithm. 
 * They are ordered by size. For
 * example, "level 1" means the itemsets of size 1 (containing 1 item).
 * 
 * @see ItemsetTP
 * @see AlgoTwoPhase
 * @author Philippe Fournier-Viger
 */
public class ItemsetsTP {
	// A list containing itemsets ordered by size
	// Level i contains itemsets of size i
	private final List<List<ItemsetTP>> levels = new ArrayList<List<ItemsetTP>>(); 
	
	// The number of itemsets 
	private int itemsetsCount = 0;
	
	// A name given to those itemsets
	private String name;

	/**
	 * Constructor.
	 * @param name  a name to give to these itemsets
	 */
	public ItemsetsTP(String name) {
		// remember the name
		this.name = name;
		// We create an empty level 0 by
		// default.
		levels.add(new ArrayList<ItemsetTP>()); 
	}

	/**
	 * Print all itemsets to System.out
	 * @param transactionCount the number of transaction in the database
	 */
	public void printItemsets(int transactionCount) {
		// print name
		System.out.println(" ------- " + name + " -------");
		int patternCount = 0;
		int levelCount = 0;
		// for each level
		for (List<ItemsetTP> level : levels) {
			// for each itemset in that level
			System.out.println("  L" + levelCount + " ");
			for (ItemsetTP itemset : level) {
				// print the itemset with the support and its utility value
				System.out.print("  pattern " + patternCount + "  ");
				itemset.print();
				System.out.print(" #SUP: "
						+ itemset.getAbsoluteSupport());
				System.out.print(" #UTIL: " + itemset.getUtility());
//				System.out.print(" #TIDSET: " + itemset.getTransactionsIds());
				
				// increase counter to get the next pattern id
				patternCount++;
				System.out.println("");
			}
			levelCount++; // next level
		}
		System.out.println(" --------------------------------");
	}


	/**
	 * Save the itemsets to the file
	 * @param output the output file path
	 * @param transactionCount the number of transactions in the database
	 * @throws IOException exception if error while writing the file
	 */
	public void saveResultsToFile(String output, int transactionCount) throws IOException {
		// Prepare to write the output file
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));

		// for each level
		for (List<ItemsetTP> level : levels) {
			// for each itemset in that level
			for (ItemsetTP itemset : level) {
				// write the itemset with its support and utility
				writer.write(itemset.toString());
				writer.write(" #SUP: "
						+ itemset.getRelativeSupport(transactionCount));
				writer.write(" #UTIL: " + itemset.getUtility());
//				writer.write(" tidset : " + itemset.getTIDset());
				// write new line
				writer.newLine();
			}
		}
		// close the output file
		writer.close();
		
	}	
	
	/**
	 * Add an itemset to these itemsets.
	 * @param itemset the itemset to be added
	 * @param k the size of the itemset
	 */
	public void addItemset(ItemsetTP itemset, int k) {
		// if the level does not exist in the arraylist structure,
		// then create it
		while (levels.size() <= k) {
			levels.add(new ArrayList<ItemsetTP>());
		}
		// add the itemset to the list
		levels.get(k).add(itemset);
		// increase the number of itemsets
		itemsetsCount++;
	}

	/**
	 * Get the itemsets stored in this structure as a List of List where
	 * position i contains the list of itemsets of size i.
	 * @return the itemsets.
	 */
	public List<List<ItemsetTP>> getLevels() {
		return levels;
	}

	/**
	 * Get the total number of itemsets.
	 * @return the itemset count.
	 */
	public int getItemsetsCount() {
		return itemsetsCount;
	}

	/**
	 * Decrease the total number of itemsets by 1.
	 */
	public void decreaseCount() {
		itemsetsCount--;
	}

}
