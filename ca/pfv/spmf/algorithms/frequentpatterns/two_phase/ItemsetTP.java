package ca.pfv.spmf.algorithms.frequentpatterns.two_phase;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class represents an itemset (a set of items) with utility information found
 * by the TWO-PHASE algorithm.
 *
 * @see AlgoTwoPhase
 * @author Philippe Fournier-Viger
 */
public class ItemsetTP{
	/** an itemset is an ordered list of items */
	private final List<Integer> items = new ArrayList<Integer>(); 
	/** we also indicate the utility of the itemset */
	private int utility =0;
	/** this is the set of tids (ids of transactions) containing this itemset */
	private Set<Integer> transactionsIds = null;
	
	/**
	 * Default constructor
	 */
	public ItemsetTP(){
	}

	/**
	 * Get the relative support of this itemset
	 * @param nbObject  the number of transactions
	 * @return the support
	 */
	public double getRelativeSupport(int nbObject) {
		return ((double)transactionsIds.size()) / ((double) nbObject);
	}
	
	/**
	 * Get the relative support of this itemset
	 * @param nbObject  the number of transactions
	 * @return the support
	 */
	public String getRelativeSupportAsString(int nbObject) {
		// calculate the support
		double frequence = ((double)transactionsIds.size()) / ((double) nbObject);
		// format it to use two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(4); 
		// return the formated support
		return format.format(frequence);
	}
	
	/**
	 * Get the absolute support of that itemset
	 * @return the absolute support (integer)
	 */
	public int getAbsoluteSupport(){
		return transactionsIds.size();
	}

	/**
	 * Add an item to that itemset
	 * @param value the item to be added
	 */
	public void addItem(Integer value){
			items.add(value);
	}
	
	/**
	 * Get items from that itemset.
	 * @return a list of integers (items).
	 */
	public List<Integer> getItems(){
		return items;
	}
	
	/**
	 * Get the item at at a given position in that itemset
	 * @param index the position
	 * @return the item (Integer)
	 */
	public Integer get(int index){
		return items.get(index);
	}
	
	/**
	 * print this itemset to System.out.
	 */
	public void print(){
		System.out.print(toString());
	}
	
	/**
	 * Get a string representation of this itemset
	 * @return a string
	 */
	public String toString(){
		// create a string buffer
		StringBuffer r = new StringBuffer ();
		// for each item
		for(Integer attribute : items){
			// append it
			r.append(attribute.toString());
			r.append(' ');
		}
		// return the string
		return r.toString();
	}

	/**
	 * Set the tidset of this itemset.
	 * @param listTransactionIds  a set of tids as a Set<Integer>
	 */
	public void setTIDset(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}
	
	/**
	 * Get the number of items in this itemset
	 * @return the item count (int)
	 */
	public int size(){
		return items.size();
	}

	/**
	 * Get the set of transactions ids containing this itemset
	 * @return  a tidset as a Set<Integer>
	 */
	public Set<Integer> getTIDset() {
		return transactionsIds;
	}

	/**
	 * Get the utility of this itemset.
	 * @return utility as an int
	 */
	public int getUtility() {
		return utility;
	}
	
	/**
	 * Increase the utility of this itemset by a given amount.
	 * @param increment  the amount.
	 */
	public void incrementUtility(int increment){
		utility += increment;
	}
}
