package ca.pfv.spmf.algorithms.sequentialpatterns.lapin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*** 
 * This is an implementation of an 2-IE-position list used by the LAPIN algorithm. 
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
public class IEPositionList {
	/** A list of pairs contained in this position list 
	 * Note that each Pair stored in this list, contains the corresponding list of positions for that pair.
	 */
	List<PairWithList> listPairs = null;

	/**
	 * Constructor
	 */
	public  IEPositionList() {
		// initialize the list of pairs
		listPairs = new ArrayList<PairWithList>();
	}
	
	/**
	 * Sort the list of pairs by ascending order
	 */
	public void sort() {
		Collections.sort(listPairs);
	}
	
	/**
	 * Register a new position for a pair of items in this IE-position list
	 * @param item1  the first item
	 * @param item2  the second item
	 * @param position the position (an itemset number, e.g. 0,1...)
	 */
	public void register(int item1, int item2, short position) {
		// Create the corresponding pair object
		PairWithList thePair = new PairWithList(item1, item2);
		// DO a binary search to see if a pair for item1 and item2 is already stored there
		int index = Collections.binarySearch(listPairs, thePair);
		// if not
		if(index < 0) {
			// we add the new pair object that we have just created
			listPairs.add(thePair);
			// we create the position list of that pair object
			thePair.createPositionList();
			// we add the position
			thePair.listPositions.add(position);
		}else {
			// otherwise, we reuse the same pair object previously used for item1 and item2 
			thePair = listPairs.get(index);
			// we add the position to the list of positions for that pair
			thePair.listPositions.add(position);
		}
	}
	
	/**
	 * Get a string representation of this 2-IE position list
	 * @return a string
	 */
	public String toString() {
		// create a string buffer
		StringBuffer buffer = new StringBuffer();
		// Fore each pair
		for(PairWithList thePair : listPairs) {
			// append  item1 and item2
			buffer.append("  position list of pair: {");
			buffer.append(thePair.item1);
			buffer.append("," );
			buffer.append(thePair.item2);
			buffer.append("}  is: ");
			// append the list of positions
			for(Short pos : thePair.listPositions) {
				buffer.append(pos);
				buffer.append(" ");
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}

	/**
	 * Get the list of positions for a pair of items : item1 and item2
	 * @param item1 the first item
	 * @param item2 the second item
	 * @return the list of positions as a list of Shorts, or null if none.
	 */
	public List<Short> getListForPair(int item1, int item2) {
		// Create the pair object
		PairWithList thePair = new PairWithList(item1, item2);
		// Perform a binary search to find the pair
		int index = Collections.binarySearch(listPairs, thePair);
		// if not found, return null
		if(index < 0) {
			return null;
		}
		// otherwise, return the list of positions
		return listPairs.get(index).listPositions;
	}




}
