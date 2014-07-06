package ca.pfv.spmf.algorithms.sequentialpatterns.spam;
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

import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;


/**
 * Implementation of a sequence as a list of itemsets as used by the SPAM, TKS and CMSPAM algorithm.
 *<br/><br/>
 *
 * Copyright (c) 2013 Philippe Fournier-Viger
 *<br/><br/>
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *<br/><br/>
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *<br/><br/>
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *<br/><br/>
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoSPAM
*  @see AlgoCMSPAM
*  @see AlgoTKS
*  @see AlgoVMSP
*  
 */
class Prefix{
	
	final List<Itemset> itemsets = new ArrayList<Itemset>();
	
	/**
	 * Default constructor
	 */
	public Prefix(){
	}
	
	/**
	 * Add an itemset to that sequence
	 * @param itemset
	 */
	public void addItemset(Itemset itemset) {
		itemsets.add(itemset);
	}
	
	/**
	 * Make a copy of that sequence
	 * @return a copy of that sequence
	 */
	public Prefix cloneSequence(){
		// create a new empty sequence
		Prefix sequence = new Prefix();
		// for each itemset
		for(Itemset itemset : itemsets){
			// copy the itemset
			sequence.addItemset(itemset.cloneItemSet());
		}
		return sequence; // return the sequence
	}

	/**
	 * Print this sequence to System.out.
	 */
	public void print() {
		System.out.print(toString());
	}
	
	/**
	 * Return a string representation of this sequence
	 */
	public String toString() {
		// create a stringbuffer
		StringBuffer r = new StringBuffer("");
		// for each itemset
		for(Itemset itemset : itemsets){
			// add a left parenthesis to indicate a new itemset
			r.append('(');
			// for each item in the current itemset
			for(Integer item : itemset.getItems()){
				// append the item to the stringbuffer
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			// add a right parenthesis to indicate the end of the itemset
			r.append(')'); 
		}
		return r.append("    ").toString(); // return the string
	}

	/**
	 * Get the list of itemsets in this sequence
	 * @return A list of itemsets.
	 */
	public List<Itemset> getItemsets() {
		return itemsets;
	}
	
	/**
	 * Get the itemset at a given position
	 * @param index the position
	 * @return the itemset
	 */
	public Itemset get(int index) {
		return itemsets.get(index);
	}
	
	/**
	 * Get the ith item in this sequence (no matter in which itemset)
	 * @param i  the position
	 * @return the item
	 */
	public Integer getIthItem(int i) { 
		// make a for loop through all itemset
		for(int j=0; j< itemsets.size(); j++){
			// if the position that we look for is in this itemset
			if(i < itemsets.get(j).size()){
				// we return the position in this itemset
				return itemsets.get(j).get(i);
			}
			// otherwise we substract the size of the current itemset
			// from the position that we are searching for
			i = i- itemsets.get(j).size();
		}
		return null;
	}
	
	/**
	 * Get the number of elements in this sequence
	 * @return the number of elements.
	 */
	public int size(){
		return itemsets.size();
	}
	
	/**
	 * Return the sum of the total number of items in this sequence
	 */
	public int getItemOccurencesTotalCount(){
		// variable to count
		int count =0;
		// for each itemset
		for(Itemset itemset : itemsets){
			count += itemset.size();  // add the size of the current itemset
		}
		return count; // return the total
	}
	
	// NEW FOR MAX...
	public boolean containsItem(Integer item) {
		for(Itemset itemset : itemsets) {
			if(itemset.getItems().contains(item)) {
				return true;
			}
		}
		return false;
	}

}
