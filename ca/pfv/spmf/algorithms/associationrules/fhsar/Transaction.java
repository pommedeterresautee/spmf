package ca.pfv.spmf.algorithms.associationrules.fhsar;
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
import java.util.Set;

/**
 * This class represent a transaction from a transaction database.
 * This implementation is defined and optimized for the FHSAR algorithm for
 * association rule hiding.
 * 
 * @see AlgoFHSAR
 * @author Philippe Fournier-Viger
 */

class Transaction implements Comparable<Transaction> {

	// the set of items in this transaction
	Set<Integer> items;
	
	double wi;  //  wi  value (see paper for definition)
	int maxItem;  // item with the  maximum |Rk|  in this transaction

	/**
	 * Constructor
	 * @param items the items in this transaction
	 * @param wi the wi value
	 * @param maxItem the item with the  maximum |Rk|  in this transaction
	 */
	Transaction(Set<Integer> items, double wi, int maxItem) {
		this.items = items;
		this.wi = wi;
		this.maxItem = maxItem;
	}

	@Override
	/**
	 * Compare this transaction with a given transaction according to their wi values. If they have
	 * the same wi values, we use the hashcode.
	 * @param o  another transaction
	 * @return 1 if this transaction is greater than the other one, of -1 otherwise.
	 */
	public int compareTo(Transaction o) {
		if (o == this) {
			return 0;
		}
		int compare = Double.compare(this.wi, o.wi);
		if (compare != 0) {
			return (int) compare;
		}
		return this.hashCode() - o.hashCode();
	}

}
