package ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/


import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a set of itemsets where an itemset is an array of integer with a tid list
 * represented by a list of integers. Itemsets are ordered by size. For
 * example, level 1 means itemsets of size 1 (that contains 1 item).
* 
 * @author Philippe Fournier-Viger
 */
public class Itemsets {
	/** We store the itemsets in a list named "levels".
	 Position i in "levels" contains the list of itemsets of size i */
	private final List<List<Itemset>> levels = new ArrayList<List<Itemset>>(); 
	/** the total number of itemsets */
	private int itemsetsCount = 0;
	/** a name that we give to these itemsets (e.g. "frequent itemsets") */
	private String name;

	/**
	 * Constructor
	 * @param name the name of these itemsets
	 */
	public Itemsets(String name) {
		this.name = name;
		levels.add(new ArrayList<Itemset>()); // We create an empty level 0 by
												// default.
	}

	/**
	 * Print all itemsets to System.out, ordered by their size.
	 * @param nbObject The number of transaction/sequence in the database where
	 * there itemsets were found.
	 */
	public void printItemsets(int nbObject) {
		System.out.println(" ------- " + name + " -------");
		int patternCount = 0;
		int levelCount = 0;
		// for each level (a level is a set of itemsets having the same number of items)
		for (List<Itemset> level : levels) {
			// print how many items are contained in this level
			System.out.println("  L" + levelCount + " ");
			// for each itemset
			for (Itemset itemset : level) {
				// print the itemset
				System.out.print("  pattern " + patternCount + ":  ");
				itemset.print();
				// print the support of this itemset
				System.out.print("support :  "
						+ itemset.getRelativeSupportAsString(nbObject));
				patternCount++;
				System.out.println("");
			}
			levelCount++;
		}
		System.out.println(" --------------------------------");
	}

	/** 
	 * Add an itemset to this structure
	 * @param itemset the itemset
	 * @param k the number of items contained in the itemset
	 */
	public void addItemset(Itemset itemset, int k) {
		while (levels.size() <= k) {
			levels.add(new ArrayList<Itemset>());
		}
		levels.get(k).add(itemset);
		itemsetsCount++;
	}

	/**
	 * Get all itemsets.
	 * @return A list of list of itemsets.
	 * Position i in this list is the list of itemsets of size i.
	 */
	public List<List<Itemset>> getLevels() {
		return levels;
	}

	/**
	 * Get the total number of itemsets
	 * @return the number of itemsets.
	 */
	public int getItemsetsCount() {
		return itemsetsCount;
	}

	/**
	 * Set the name of this group of itemsets
	 * @param string the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Decrease the count of itemsets stored in this structure by 1.
	 */
	public void decreaseItemsetCount() {
		this.itemsetsCount--;
	}
}
