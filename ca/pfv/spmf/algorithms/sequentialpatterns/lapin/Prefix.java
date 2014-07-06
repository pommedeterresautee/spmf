package ca.pfv.spmf.algorithms.sequentialpatterns.lapin;


import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of a sequential pattern.
 * A sequential pattern is a list of itemsets. An itemset is a list of integers.
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
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
public class Prefix{
	
	/** the internal representation of this sequential pattern (a list of list of integers) */
	final List<List<Integer>> itemsets = new ArrayList<List<Integer>>();
	
	/**
	 * Constructor
	 */
	public Prefix(){
	}

	/**
	 * Get a copy of this sequential pattern
	 * @return the copy
	 */
	public Prefix cloneSequence(){
		// Create a new sequential pattern object
		Prefix sequence = new Prefix();
		// for each itemset
		for(List<Integer> itemset : itemsets){
			// copy the itemset
			List<Integer> cloneItemset = new ArrayList<Integer>(itemset.size());
			for(int i=0; i< itemset.size(); i++) {
				cloneItemset.add(itemset.get(i));
			}
			// add the itemset to the sequential pattern
			sequence.itemsets.add(cloneItemset);
		}
		// return the copied pattern
		return sequence;
	}

	/**
	 * Print this sequential pattern to the console
	 */
	public void print() {
		System.out.print(toString());
	}
	
	/**
	 * Get a string representation of that sequential pattern
	 */
	public String toString() {
		// for each itemset in that pattern
		StringBuffer r = new StringBuffer("");
		for(List<Integer> itemset : itemsets){
			r.append('(');
			// for each item in the current itemset
			for(Integer item : itemset){
				// append it
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append(')');
		}
		// return the string
		return r.append("    ").toString();
	}

	/**
	 * Get the number of itemset in this pattern
	 * @return the number of itemsets (int).
	 */
	public int size(){
		return itemsets.size();
	}

}
