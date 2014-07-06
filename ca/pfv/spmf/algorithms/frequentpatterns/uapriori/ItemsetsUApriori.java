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

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a set of itemsets found in an uncertain database, 
 * as used by the UApriori algorithm uncertain itemset mining.
 * They are ordered by size. For example, level 1 means itemsets of size 1
 *  (that contains 1 item).
 *
 * @see AlgoUApriori
 * @see ItemUApriori
 * @see ItemsetUApriori
 * @author Philippe Fournier-Viger
 */
public class ItemsetsUApriori {
	// A list containing itemsets ordered by size
	// Level i contains itemsets of size i
	private final List<List<ItemsetUApriori>> levels = new ArrayList<List<ItemsetUApriori>>();  // itemset classé par taille
	
	// The number of itemsets 
	private int itemsetsCount=0;
	// A name given to those itemsets
	private final String name;
	
	/**
	 * Constructor.
	 * @param name  a name to give to these itemsets
	 */
	public ItemsetsUApriori(String name){
		// remember the name
		this.name = name;
		// We create an empty level 0 by default.
		levels.add(new ArrayList<ItemsetUApriori>()); 
	}
	
	/**
	 * Print all itemsets to the console (system.out).
	 */
	public void printItemsets(){
		// print name
		System.out.println(" ------- " + name + " -------");
		int patternCount=0;
		int levelCount=0;
		// for each level
		for(List<ItemsetUApriori> level : levels){
			// for each itemset in that level
			System.out.println("  L" + levelCount + " ");
			for(ItemsetUApriori itemset : level){
				// print the itemset with the support and its utility value
				System.out.print("  pattern " + patternCount + ":  ");
				itemset.printWithoutSupport();
				System.out.print("support :  " + itemset.getSupportAsString());
				// increase counter to get the next pattern id
				patternCount++;
				System.out.println("");
			}
			levelCount++;
		}
		System.out.println(" --------------------------------");
	}
	
	/**
	 * Add an itemset to these itemsets.
	 * @param itemset the itemset to be added
	 * @param k the size of the itemset
	 */
	public void addItemset(ItemsetUApriori itemset, int k){
		while(levels.size() <= k){
			levels.add(new ArrayList<ItemsetUApriori>());
		}
		levels.get(k).add(itemset);
		itemsetsCount++;
	}

	/**
	 * Get the itemsets stored in this structure as a List of List where
	 * position i contains the list of itemsets of size i.
	 * @return the itemsets.
	 */
	public List<List<ItemsetUApriori>> getLevels() {
		return levels;
	}

	/**
	 * Get the total number of itemsets.
	 * @return the itemset count.
	 */
	public int getItemsetsCount() {
		return itemsetsCount;
	}
	
}
