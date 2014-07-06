package ca.pfv.spmf.algorithms.sequentialpatterns.lapin;

import java.util.ArrayList;
import java.util.List;

/*** 
 * This is an implementation of an Item-is-exist table used by the LAPIN-SPAM algorithm. 
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
public class Table {
	
	/** The list of vectors in this item-is-exist table */
	List<PositionVector> positionVectors;

	/**
	 * Constructor
	 */
	public Table() {
		positionVectors = new ArrayList<PositionVector>();
	}

	/**
	 * Add a position vector to this table
	 * @param vector the vector
	 */
	void add(PositionVector vector) {
		positionVectors.add(vector);
	}
	
	/**
	 * Get a string representation of this table
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		// for each vector of this table
		for(PositionVector vector : positionVectors) {
			// we convert it to string
			buffer.append(" " + vector.toString());
			buffer.append("\n");
		}
		return buffer.toString();
	}

}
