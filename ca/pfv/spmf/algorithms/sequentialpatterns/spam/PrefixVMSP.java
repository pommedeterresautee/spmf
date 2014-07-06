package ca.pfv.spmf.algorithms.sequentialpatterns.spam;

import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;


/**
 * Implementation of a prefix sequence as a list of itemsets as used by the VMSP algorithm
 * as used by the VMSP algorithm.
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
public class PrefixVMSP extends Prefix{
	// the two following variables are used for optimizations in VGEN to
		// avoid some containment checkings
		Integer sumOfEvenItems = null; // sum of even items in this prefix
		Integer sumOfOddItems = null;  // sumof odd items in this prefix f
	
	/**
	 * Default constructor
	 */
	public PrefixVMSP(){
	}
	
	/**
	 * Make a copy of that sequence
	 * @return a copy of that sequence
	 */
	public PrefixVMSP cloneSequence(){
		// create a new empty sequence
		PrefixVMSP sequence = new PrefixVMSP();
		// for each itemset
		for(Itemset itemset : itemsets){
			// copy the itemset
			sequence.addItemset(itemset.cloneItemSet());
		}
		return sequence; // return the sequence
	}

}
