package ca.pfv.spmf.algorithms.frequentpatterns.relim;
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
 * This class represents a transaction database optimized for the RELIM algorithm. 
 * In particular, the transaction database is represented by a list of list of list of
 * integers (items) and the support of each item is calculated and stored in an interna array.
 * 
 * @see AlgoRelim
 * @author Philippe Fournier-Viger
 */
class DatabaseStructureRelim {

	// the support of each item in the database
	// position i indicates the support of item i
	int[] supports;

	// the transactions structure of the relim algorithm (see paper for full details)
	List<List<List<Integer>>> transactions = new ArrayList<List<List<Integer>>>();

	/**
	 * Create the database structure
	 * @param supports an array of support indicating the support of each item.
	 */
	public DatabaseStructureRelim(int[] supports) {
		this.supports = supports;
	}

	/**
	 * Initialize the database structure.
	 */
	public void initializeTransactions() {
		// Initialize "transactions"
		// for each item, create a list of list of integers.
		for (int i = 0; i < supports.length; i++) {
			transactions.add(new ArrayList<List<Integer>>());
		}
	}

	/**
	 * Get a string representation of the database structure.
	 */
	public String toString() {
		// create a stringuffer 
		StringBuffer temp = new StringBuffer();
		temp.append("\n supports : ");
		// for each item, return its support
		for (Integer integer : supports) {
			temp.append(integer);
			temp.append(" ");
		}
		// for each item, print the support and transactions at position i
		temp.append("\nLISTS\n");
		for (int i = 0; i < supports.length; i++) {
			temp.append(" #SUP: " + supports[i] + " "
					+ transactions.get(i).toString() + "\n");
		}
		// return the string
		return temp.toString();
	}
}
