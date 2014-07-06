package ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR;
/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a  transaction 
 * optimized for the TNR and TopKRules algorithms for top-k
 * association rule mining.  In particular transactions
 * are implemented as LinkedList for efficient removal of infrequent 
 * items as done by these algorithms.
 * 
 * @see AlgoTNR
 * @see AlgoTopKRules
 * @see Database
 * @author Philippe Fournier-Viger
 */

public class Transaction{
	// a transaction is an ordered list of items
	private final List<Integer> items; 
	
	/**
	 * Constructor
	 * @param size the size of the transaction
	 */
	public Transaction(int size){
		// WE USE A LINKEDLIST BECAUSE WE PERFORM MANY DELETE OPERATIONS.
		items = new LinkedList<Integer>();  
	}

	/**
	 * Add an item to the transaction.
	 * @param item an item.
	 */
	public void addItem(Integer item){
		items.add(item);
	}

	/**
	 * Get the list of items in this transaction
	 * @return a List of Integers
	 */
	public List<Integer> getItems() {
		return items;
	}
}
