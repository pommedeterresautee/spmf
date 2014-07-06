package ca.pfv.spmf.algorithms.frequentpatterns.dci_closed_optimized;
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

import java.util.BitSet;

/**
 * This class represents a bix matrix as used by the optimized version of the
 * DCI_Closed algorithm.
 * 
 * @see AlgoDCI_Closed_Optimized
 * @author Philippe Fournier-Viger
 */
public class BitMatrix {

	// Array containing the tidset of each item as a bitset.
	// The position i represents the tidset of item i.
	private BitSet[] matrixItemTIDs;
	private int[] support1item; // array to keep the support of each item.

	/**
	 * Constructor of a bit matrix
	 * @param itemCount the number of items in the bitmatrix
	 * @param transactionCount  the number of transactions (number of bits in each bitset)
	 */
	BitMatrix(int itemCount, int transactionCount) {
		// initialize the support array
		support1item = new int[itemCount];
		// initialize the matrix of bits
		matrixItemTIDs = new BitSet[itemCount];
		for (int i = 0; i < matrixItemTIDs.length; i++) {
			matrixItemTIDs[i] = new BitSet(transactionCount);
		}
	}

	/**
	 * Add a tid to the tidset of an item.
	 * @param item  the item
	 * @param bit   the bit corresponding to the tid
	 */
	public void addTidForItem(Integer item, int bit) {
		matrixItemTIDs[item - 1].set(bit, true);
	}

	/**
	 * Get the support of an item (first time only)
	 * @param i  the item
	 * @return  the support
	 */
	public int getSupportOfItemFirstTime(int i) {
		// We use the cardinality method of the tidset to get the support.
		// We save the result because the cardinality method for bitset is
		// expensive  (it scans the bitset each time it is called to count
		// the number of 1)
		support1item[i - 1] = matrixItemTIDs[i - 1].cardinality();
		return support1item[i - 1];
	}

	/**
	 * Get the support of an item (not the first time)
	 * @param i  the item
	 * @return  the support
	 */
	public int getSupportOfItem(int i) {
		// return the precalculated support.
		return support1item[i - 1];
	}

	/**
	 * Get the tidset of an item
	 * @param i the item
	 * @return a bitset representing the tidset of the item
	 */
	public BitSet getBitSetOf(Integer i) {
		return matrixItemTIDs[i - 1];
	}

	/**
	 * Return a string representation of the bitmatrix
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		// for each bitset
		for (BitSet bitset : matrixItemTIDs) {
			// append it to the string
			buffer.append(bitset.toString());
		}
		// return the string
		return buffer.toString();
	}
}
