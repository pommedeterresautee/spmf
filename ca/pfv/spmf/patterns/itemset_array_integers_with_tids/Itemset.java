package ca.pfv.spmf.patterns.itemset_array_integers_with_tids;

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


import java.util.HashSet;
import java.util.Set;

import ca.pfv.spmf.patterns.AbstractOrderedItemset;

/**
 * This class represents an itemset (a set of items) where the itemset is an array of integers 
 * sorted by lexical order where no item can appear twice, and 
 * 	the ids of transactions/sequences containing this itemset is represented
 *     as a set of integers.
* 
 * @author Philippe Fournier-Viger
 */
public class Itemset extends AbstractOrderedItemset{
	/** The list of items contained in this itemset, ordered by 
	 lexical order */
	public int[] itemset; 
	/** The set of transactions/sequences id containing this itemset */
	public Set<Integer> transactionsIds = new HashSet<Integer>();

	/**
	 * Constructor
	 */
	public Itemset() {
	}
	
	/**
	 * Constructor 
	 * @param item an item that should be added to the new itemset
	 */
	public Itemset(int item){
		itemset = new int[]{item};
	}
	
	/**
	 * Constructor 
	 * @param items an array of items that should be added to the new itemset
	 */
	public Itemset(int [] items){
		this.itemset = items;
	}

	/**
	 * Get the support of this itemset (as an integer)
	 */
	public int getAbsoluteSupport() {
		return transactionsIds.size();
	}


	/**
	 * Get the items in this itemset as a list.
	 * @return the items.
	 */
	public int[] getItems() {
		return itemset;
	}
	
	/**
	 * Get the item at a given position.
	 * @param index the position
	 * @return the item
	 */
	public Integer get(int index) {
		return itemset[index];
	}

	/**
	 * Set the list of transaction/sequence ids containing this itemset
	 * @param listTransactionIds  the list of transaction/sequence ids
	 */
	public void setTIDs(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}

	/**
	 * Get the size of this itemset.
	 */
	public int size() {
		return itemset.length;
	}

	/**
	 * Get the list of sequence/transaction ids containing this itemset.
	 * @return the list of transaction ids.
	 */
	public Set<Integer> getTransactionsIds() {
		return transactionsIds;
	}
	
	/**
	 * Make a copy of this itemset but exclude a set of items
	 * @param itemsetToNotKeep the set of items to be excluded
	 * @return the copy
	 */
	public Itemset cloneItemSetMinusAnItemset(Itemset itemsetToNotKeep) {
		// create a new itemset
		int[] newItemset = new int[itemset.length - itemsetToNotKeep.size()];
		int i=0;
		// for each item of this itemset
		for(int j =0; j < itemset.length; j++){
			// copy the item except if it is not an item that should be excluded
			if(itemsetToNotKeep.contains(itemset[j]) == false){
				newItemset[i++] = itemset[j];
			}
		}
		return new Itemset(newItemset); // return the copy
	}
	
	/**
	 * Make a copy of this itemset but exclude a given item
	 * @param itemsetToRemove the given item
	 * @return the copy
	 */
	public Itemset cloneItemSetMinusOneItem(Integer itemsetToRemove) {
		// create the new itemset
		int[] newItemset = new int[itemset.length -1];
		int i=0;
		// for each item in this itemset
		for(int j =0; j < itemset.length; j++){
			// copy the item except if it is the item that should be excluded
			if(itemset[j] != itemsetToRemove){
				newItemset[i++] = itemset[j];
			}
		}
		return new Itemset(newItemset); // return the copy
	}
	
}
