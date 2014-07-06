package ca.pfv.spmf.input.sequence_database_array_integers;
/* Copyright (c) 2008-2013 Philippe Fournier-Viger
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
* 
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/
import java.util.ArrayList;
import java.util.List;



/**
 * Implementation of a sequence as a list of itemsets, where itemsets are array of integers.
* 
* @see SequenceDatabase
 * @author Philipe-Fournier-Viger
 **/
public class Sequence{
	
	// A sequence is a list of itemsets, where an itemset is an array of integers
	private final List<Integer[]> itemsets = new ArrayList<Integer[]>();

	/**
	 * Add an itemset to this sequence.
	 * @param itemset An itemset (array of integers)
	 */
	public void addItemset(Object[] itemset) {
		Integer[] itemsetInt = new Integer[itemset.length];
		System.arraycopy(itemset, 0, itemsetInt, 0, itemset.length);
		itemsets.add(itemsetInt);
	}
	
	/**
	 * Print this sequence to System.out.
	 */
	public void print() {
		System.out.print(toString());
	}
	
	/**
	 * Return a string representation of this sequence.
	 */
	public String toString() {
		StringBuffer r = new StringBuffer("");
		// for each itemset
		for(Integer[] itemset : itemsets){
			r.append('(');
			// for each item in the current itemset
			for(int i=0; i< itemset.length; i++){
				String string = itemset[i].toString();
				r.append(string);
				r.append(' ');
			}
			r.append(')');
		}

		return r.append("    ").toString();
	}

	/**
	 * Get the list of itemsets in this sequence.
	 * @return the list of itemsets.
	 */
	public List<Integer[]> getItemsets() {
		return itemsets;
	}
	
	/**
	 * Get the itemset at a given position in this sequence.
	 * @param index the position
	 * @return the itemset as an array of integers.
	 */
	public Integer[] get(int index) {
		return itemsets.get(index);
	}
	
	/**
	 * Get the size of this sequence (number of itemsets).
	 * @return the size (an integer).
	 */
	public int size(){
		return itemsets.size();
	}
}
