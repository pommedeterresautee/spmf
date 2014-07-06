package ca.pfv.spmf.patterns.itemset_list_integers_without_support;

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
import java.util.Map;
import java.util.Set;


/**
 * This class represents an itemset from a sequence where
 *   the itemset is a list of strings ordered by lexical order and does not 
 *     contain an item twice, and the support of the itemset is not stored.
*  @see AbstractOrderedItemsetsAdapter
 * @author Philippe Fournier-Viger
 */
public class Itemset{

	/** The list of items in this itemset.
	 The items are lexically ordered and an item can only
	 appear once in an itemset. */
	private final List<Integer> items = new ArrayList<Integer>(); 
	
	/**
	 * Constructor to create an itemset with an item
	 * @param item the item
	 */
	public Itemset(Integer item){
		addItem(item);
	}
	
	/**
	 * Constructor to create an empty itemset.
	 */
	public Itemset(){
	}

	/**
	 * Add an item to this itemset
	 * @param value the item
	 */
	public void addItem(Integer value){
			items.add(value);
	}
	
	/**
	 * Get the list of items
	 * @return list of items
	 */
	public List<Integer> getItems(){
		return items;
	}
	
	/**
	 * Get an item at a given position in this itemset
	 * @param index the position
	 * @return the item
	 */
	public Integer get(int index){
		return items.get(index);
	}

	/**
	 * Get this itemset as a string
	 * @return this itemset as a string
	 */
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(Integer item : items){
			r.append(item.toString());
			r.append(' ');
		}
		return r.toString();
	}
	
	/**
	 * Get the size of this itemset (the number of items)
	 * @return the size
	 */
	public int size(){
		return items.size();
	}

	/**
	 * This methods makes a copy of this itemset but without
	 * items having a support lower than minsup
	 * @param mapSequenceID a map indicating the support of each item. key: item  value: support
	 * @param relativeMinsup the support expressed as a percentage
	 * @return the new itemset
	 */
	public Itemset cloneItemSetMinusItems(Map<Integer, Set<Integer>> mapSequenceID, double relativeMinsup) {
		Itemset itemset = new Itemset();
		for(Integer item : items){
			if(mapSequenceID.get(item).size() >= relativeMinsup){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	/**
	 * This method makes a copy of an itemset
	 * @return the copy.
	 */
	public Itemset cloneItemSet(){
		Itemset itemset = new Itemset();
		itemset.getItems().addAll(items);
		return itemset;
	}
	
	/**
	 * This methods checks if another itemset is contained in this one.
	 * @param itemset2 the other itemset
	 * @return true if it is contained
	 */
	public boolean containsAll(Itemset itemset2){
		// we will use this variable to remember where we are in this itemset
		int i = 0;
		
		// for each item in itemset2, we will try to find it in this itemset
		for(Integer item : itemset2.getItems()){
			boolean found = false; // flag to remember if we have find the item
			
			// we search in this itemset starting from the current position i
			while(found == false && i < size()){
				// if we found the current item from itemset2, we stop searching
				if(get(i).equals(item)){
					found = true;
				}// if the current item in this itemset is larger than 
				// the current item from itemset2, we return false
				// because the itemsets are assumed to be lexically ordered.
				else if(get(i) > item){
					return false;
				}
				
				i++; // continue searching from position  i++
			}
			// if the item was not found in the previous loop, return false
			if(!found){
				return false;
			}
		}
		return true; // if all items were found, return true
	}
}
