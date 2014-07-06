package ca.pfv.spmf.algorithms.sequentialpatterns.lapin;

import java.util.BitSet;

/*** 
 * This is an implementation of a position vector in the Item-is-exist table of LAPIN-SPAM.
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
public class PositionVector {
	
	/** The bitset for this vector */
	BitSet bitset;
	/** The position for this bit vector 
	 * Note that in the paper the positions are an integer >=0.  In this implementation
	 * we also add a position -1 for each Item-is-exist-table. This position contains 
	 * a bit set to 1 for all items appearing in the corresponding sequence */
	int position;


	/**
	 * Constructor
	 * @param currentBitset the bitset representing this vector
	 * @param position  the position that this vector represents in the item-is-exist table containing it.
	 */
	public PositionVector(int position, BitSet currentBitset) {
		this.bitset = currentBitset;
		this.position = position;
	}
	
	/**
	 * Get a string representation of this vector
	 */
	public String toString() {
		// We first append the position
		StringBuffer buffer = new StringBuffer();
		buffer.append(position);
		buffer.append(" ");
		// Then we append the bitset
		buffer.append(bitset);
		return buffer.toString();
	}

}
