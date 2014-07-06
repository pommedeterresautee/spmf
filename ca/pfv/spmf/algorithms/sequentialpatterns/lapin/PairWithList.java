package ca.pfv.spmf.algorithms.sequentialpatterns.lapin;

import java.util.ArrayList;
import java.util.List;

/*** 
 * This is an implementation of a pair of items  used by the LAPIN-SPAM algorithm to 
 * represent a 2-IE-sequence and its list of positions in a sequence.
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
public class PairWithList implements Comparable<PairWithList> {

	/** the first item */
	protected int item1;
	/** the second item */
	protected int item2;
	/**  the list of positions where this pair appears */
	List<Short> listPositions;
	/**
	 * Constructor
	 * @param item1 the first item
	 * @param item2 the second item
	 */
	PairWithList(int item1, int item2){
		this.item1 = item1;
		this.item2 = item2;
	}
	
	/**
	 * Initialize the list of positions (by default it is not created)
	 */
	void createPositionList() {
		listPositions = new ArrayList<Short>();
	}
	
	/**
	 * Compare this pair with another pair.  
	 * @pram o  the other pair
	 * @return  a value > 0, == 0 or <0 if this pair is larger, equal of smaller than the other pair
	 */
	public int compareTo(PairWithList o) {
		int val = this.item1 - o.item1;
		if(val == 0) {
			val = this.item2 - o.item2;
		}
		return val;
	}

	/**
	 * Method to check if this pair is equal to another
	 * @param obj the other pair
	 * @return true if both pair represents the same pair of items
	 */
	public boolean equals(Object obj) {
		PairWithList pair = (PairWithList) obj;
		return pair.item1 == this.item1 && pair.item2 == this.item2;
	}
}
