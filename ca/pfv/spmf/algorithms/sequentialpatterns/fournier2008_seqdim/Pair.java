package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim;
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

import java.util.HashSet;
import java.util.Set;


/**
 * This class is used by the Fournier08 algorithm and 
 * represents 
 * a pair of an (1) Item  and (2) a time interval. 
 * It is used for calculating the support
 * of items in a database. A pair also contains some other information too that 
 * are needed by BIDE/PrefixSpan such as if the item was in an itemset 
 * that is a prefix (cut at right) or if the item was in an itemset that is a postfix (cut a left).
 * 
 * @see AlgoBIDEPlus
 * @see AlgoPrefixSpanMDSPM
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */
class Pair{
	// a timestamp (not used by prefixspan
	private final long timestamp; 
	// indicate  if the item is in an itemset that is cut at left
	private final boolean postfix;
	// indicate  if the item is in an itemset that is cut at right
	private final boolean prefix; 
	// an item
	private final ItemSimple item;
	
	// List of all the sequence IDs that contains this Pair .
	private Set<Integer> sequencesID = new HashSet<Integer>();
	
	/**
	 * Constructor
	 * @param timestamp  a timestamp
	 * @param prefix  true if the item was found in an itemset that is a prefix
	 * @param postfix true if the item was found in an itemset that is a postfix
	 * @param item the item
	 */
	Pair(long timestamp, boolean prefix, boolean postfix, ItemSimple item){
		this.timestamp = timestamp;
		this.postfix = postfix;
		this.prefix  = prefix;
		this.item = item;
	}
	
	/**
	 * Constructor
	 * @param prefix  true if the item was found in an itemset that is a prefix
	 * @param postfix true if the item was found in an itemset that is a postfix
	 * @param item the item
	 */
	public Pair( boolean prefix, boolean postfix, ItemSimple item){
		this.timestamp = 0; // not used by prefixspan
		this.postfix = postfix;
		this.prefix = prefix;
		this.item = item;
	}
	
	/**
	 * Check if this pair is equal to another given Pair.
	 * @param object a Pair
	 * @return true if equal, otherwise false.
	 */
	public boolean equals(Object object){
		// cast to Pair
		Pair paire = (Pair) object;
		// if they have the same timestamp, postfix and prefix boolean values,
		// and the same item ID, then return true.
		if((paire.timestamp == this.timestamp) && (paire.postfix == this.postfix) 
				&& (paire.prefix == this.prefix)
				&& (paire.item.equals(this.item))){
			return true;
		} // otherwise false
		return false;
	}
	
	/**
	 * Get the hashcode for this pair
	 * @return an int value
	 */
	public int hashCode()
	{// Ex: 127333,P,X,1  127333,N,Z,2
		// create a stringbuffer
		StringBuffer r = new StringBuffer();
		// make a string by appending the field variables values.
		r.append(timestamp);
		r.append((postfix ? 'P' : 'N')); // the letters here have no meanings. they are just used for the hashcode
		r.append((prefix ? 'X' : 'Z')); // the letters here have no meanings. they are just used for the hashcode
		r.append(item.getId());
		// convert to string and use the hashcode method of the String class
		return r.toString().hashCode();
	}

	/**
	 * Get the timestamp for this Pair.
	 * @return a long value
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Check if this Pair is for an item found in an itemset that is a postfix.
	 * @return true, if yes, otherwise false
	 */
	public boolean isPostfix() {
		return postfix;
	}

	/**
	 * Get the item.
	 * @return an Item.
	 */
	public ItemSimple getItem() {
		return item;
	}

	/**
	 * Get the number of sequences containing this Pair.
	 * @return an int value.
	 */
	public int getCount() {
		return sequencesID.size();
	}		
	
	/**
	 * Get the IDs of sequences containing this Pair.
	 * @return a Set of Integer
	 */
	public Set<Integer> getSequencesID() {
		return sequencesID;
	}

	/**
	 * Set the IDs of sequences containing this Pair.
	 * @param sequencesID a Set of Integer
	 */
	public void setSequencesID(Set<Integer> sequencesID) {
		this.sequencesID = sequencesID;
	}

	/**
	 * Check if this Pair is for an item found in an itemset that is a prefix.
	 * @return true, if yes, otherwise false
	 */
	public boolean isPrefix() {
		return prefix;
	}
}