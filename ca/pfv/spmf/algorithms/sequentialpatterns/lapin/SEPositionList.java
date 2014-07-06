package ca.pfv.spmf.algorithms.sequentialpatterns.lapin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/*** 
 * This is an implementation of a SE position list used by the LAPIN-SPAM algorithm, to represent the 
 * positions where some items appear in a sequence. 
 * 
 * The LAPIN-SPAM algorithm was originally described in this paper:
 * 
 *     Zhenlu Yang and Masrau Kitsuregawa. LAPIN-SPAM: An improved algorithm for mining sequential pattern
 *     In Proc. of Int'l Special Workshop on Databases For Next Generation Researchers (SWOD'05) 
 *     in conjunction with ICDE'05, pp. 8-11, Tokyo, Japan, Apr. 2005. 
 *
 * Copyright (c) 2008-2013 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */
public class SEPositionList {
	/** The list of items  */
	int[] listItems = null;
	/** the list of positions corresponding to the items*/
	List<Short> [] listPositions = null;

	/**
	 * Constructor (perform some intialization
	 * @param a set of integers that will be inserted into this list
	 */
	@SuppressWarnings("unchecked")
	public  SEPositionList(Set<Integer> itemsAlreadySeen) {
		// Get the number of items that will be inserted to initialized the lists
		int size = itemsAlreadySeen.size();
		listItems = new int[size];
		listPositions = new List[size];
		// For each item, add them to the list of items and initialize the corresponding list of positions
		int i=0;
		for(int item : itemsAlreadySeen) {
			listItems[i] = item;
			listPositions[i] = new ArrayList<Short>();
			i++;
		}
		// Sort items by ascending order so that later we can do a binary search on the list
		// (as described in the LAPIN paper)
		Arrays.sort(listItems);
	}
	
	/**
	 * This method add the position of an item to this position list
	 * @param item the item id
	 * @param position  the position (a byte indicating in which itemset the item appears, e.g. 0 for the first itemset)
	 */
	public void register(Integer item, short position) {
		int index = Arrays.binarySearch(listItems, item);
		listPositions[index].add(position);
	}
	
	/**
	 * Get a string representation of this SE position list
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		// for each item
		for(int i=0; i<listItems.length; i++) {
			// append the corresponding position list
			buffer.append("  position list of item: ");
			buffer.append(listItems[i]);
			buffer.append("  is: ");
			// for each position
			for(Short pos : listPositions[i]) {
				// append the position
				buffer.append(pos);
				buffer.append(" ");
			}
			buffer.append("\n");
		}
		// return the string
		return buffer.toString();
	}

	/**
	 * Get the position list of an item
	 * @param item the item
	 * @return the position list as a List of Shorts, or null if there is none for that item.
	 */
	public List<Short> getListForItem(int item) {
		// Do a binary search to find the index where the item is in the position list
		int index = Arrays.binarySearch(listItems, item);
		// if the item does not appear in the list
		if(index < 0) {
			// we return null
			return null;
		}
		// return the position list
		return listPositions[index];
	}




}
