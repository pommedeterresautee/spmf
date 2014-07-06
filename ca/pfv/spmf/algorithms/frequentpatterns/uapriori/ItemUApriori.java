package ca.pfv.spmf.algorithms.frequentpatterns.uapriori;
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

/**
 * This class represents an item from a transaction database 
 * as used by the UApriori algorithm uncertain itemset mining.
 * 
 * @see AlgoUApriori
 * @see UncertainTransactionDatabase
 * @see ItemsetUApriori
 * @author Philippe Fournier-Viger
 */
public class ItemUApriori {
	// the item id
	private final int id;
	// the probability associated to that item
	private final double probability;

	/**
	 * Constructor
	 * @param id id ot the item
	 * @param probability the existential proability
	 */
	public ItemUApriori(int id, double probability) {
		this.id = id;
		this.probability = probability;
	}

	/**
	 * Get the item id.
	 * @return a int.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get a string representation of this item.
	 * @return a string
	 */
	public String toString() {
		return "" + getId() + " (" + probability + ")";
	}

	/**
	 * Check if this item is equal to another.
	 * @param object another item
	 * @return true if equal, otherwise false.
	 */
	public boolean equals(Object object) {
		ItemUApriori item = (ItemUApriori) object;
		// if the same id, then true
		if ((item.getId() == this.getId())) {
			return true;
		}
		// if not the same id, then false
		return false;
	}

	/**
	 * Generate an hash code for that item.
	 * @return an hash code as a int.
	 */
	public int hashCode() {
		String string = "" + getId();
		return string.hashCode();
	}

	/**
	 * Get the existential probability associated to this item
	 * @return  the probability as a double
	 */
	public double getProbability() {
		return probability;
	}
}
