package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents
 * a pair of an (1) item  
 * (2)  if it is contained in an itemset that was cut or not (a postfix). 
 * and (3) the sequence IDs containing this item.
 * 
 * This class is used by PrefixSpan.
 * 
 * It is used for calculating the support of an item in a database.
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
class Pair{
	// the item
	protected final Integer item;
	// indicate if this represents the item appearing 
	// in an itemset that is cut at the left or not
	protected final boolean postfix; 
	
	// List of the sequence IDs that contains this item .
	private Set<Integer> sequencesID = new HashSet<Integer>();

	/**
	 * Constructor
	 * @param postfix indicate if this is the case of an item appearing
	 *  in an itemset that is cut at the left because of a projection
	 * @param item the item
	 */
	Pair(boolean postfix, Integer item){
		this.postfix = postfix;
		this.item = item;
	}
	
	/**
	 * Check if two pairs are equal (same item and both appears in a postfix or not).
	 * @return true if equals.
	 */
	public boolean equals(Object object){
		Pair paire = (Pair) object;
		if((paire.postfix == this.postfix) 
				&& (paire.item.equals(this.item))){
			return true;
		}
		return false;
	}
	
	/**
	 * Method to calculate an hashcode (because pairs are stored in a map).
	 */
	public int hashCode()
	{// Ex: 127333,P,X,1  127333,N,Z,2
		// transform it into a string
		StringBuffer r = new StringBuffer();
		r.append((postfix ? 'P' : 'N')); // the letters here have no meanings. they are just used for the hashcode
		r.append(item);
		// then use the hashcode method from the string class
		return r.toString().hashCode();
	}
	
	/**
	 * Check if this is the case of the item appearing in a postfix
	 * @return true if this is the case.
	 */
	public boolean isPostfix() {
		return postfix;
	}

	/**
	 * Get the item represented by this pair
	 * @return the item.
	 */
	public Integer getItem() {
		return item;
	}

	/**
	 * Get the support of this item (the number of sequences 
	 * containing it).
	 * @return the support (an integer)
	 */
	public int getCount() {
		return sequencesID.size();
	}		

	/**
	 * Get the list of sequence IDs associated with this item.
	 * @return  the list of sequence IDs.
	 */
	public Set<Integer> getSequenceIDs() {
		return sequencesID;
	}
}