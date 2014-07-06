package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;

/**
 * This class represents a sequential pattern.
 * A sequential pattern is a list of itemsets.
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
public class SequentialPattern implements Comparable<SequentialPattern>{
	
	// the list of itemsets
	private final List<Itemset> itemsets;
//	private int id; 
	
	// IDs of sequences containing this pattern
	private Set<Integer> sequencesIds;
	
	private int itemCount = -1;
	
	/**
	 * Set the set of IDs of sequence containing this prefix
	 * @param a set of integer containing sequence IDs
	 */
	public void setSequenceIDs(Set<Integer> sequencesIds) {
		this.sequencesIds = sequencesIds;
	}

	/**
	 * Defaults constructor
	 */
	public SequentialPattern(){
		itemsets = new ArrayList<Itemset>();
	}
	
	public SequentialPattern(Itemset itemset, Set<Integer> sequencesIds){
		itemsets = new ArrayList<Itemset>();
		this.itemsets.add(itemset);
		this.sequencesIds = sequencesIds;
	}
	
	public SequentialPattern(List<Itemset> itemsets, Set<Integer> sequencesIds){
		this.itemsets = itemsets;
		this.sequencesIds = sequencesIds;
	}
	
	/**
	 * Get the relative support of this pattern (a percentage)
	 * @param sequencecount the number of sequences in the original database
	 * @return the support as a string
	 */
	public String getRelativeSupportFormated(int sequencecount) {
		double relSupport = ((double)sequencesIds.size()) / ((double) sequencecount);
		// pretty formating :
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(relSupport);
	}
	
	/**
	 * Get the absolute support of this pattern.
	 * @return the support (an integer >= 1)
	 */
	public int getAbsoluteSupport(){
		return sequencesIds.size();
	}

	/**
	 * Add an itemset to this sequential pattern
	 * @param itemset the itemset to be added
	 */
	public void addItemset(Itemset itemset) {
//		itemCount += itemset.size();
		itemsets.add(itemset);
	}
	
	/**
	 * Make a copy of this sequential pattern
	 * @return the copy.
	 */
	public SequentialPattern cloneSequence(){
		// create a new empty sequential pattenr
		SequentialPattern sequence = new SequentialPattern();
		// for each itemset
		for(Itemset itemset : itemsets){
			// make a copy and add it
			sequence.addItemset(itemset.cloneItemSet());
		}
		return sequence; // return the copy
	}

	/**
	 * Print this sequential pattern to System.out
	 */
	public void print() {
		System.out.print(toString());
	}
	
	/**
	 * Get a string representation of this sequential pattern, 
	 * containing the sequence IDs of sequence containing this pattern.
	 */
	public String toString() {
		StringBuffer r = new StringBuffer("");
		// For each itemset in this sequential pattern
		for(Itemset itemset : itemsets){
			r.append('('); // begining of an itemset
			// For each item in the current itemset
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string); // append the item
				r.append(' ');
			}
			r.append(')');// end of an itemset
		}
//
//		//  add the list of sequence IDs that contains this pattern.
//		if(getSequencesID() != null){
//			r.append("  Sequence ID: ");
//			for(Integer id : getSequencesID()){
//				r.append(id);
//				r.append(' ');
//			}
//		}
		return r.append("    ").toString();
	}
	
	/**
	 * Get a string representation of this sequential pattern.
	 */
	public String itemsetsToString() {
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : itemsets){
			r.append('{');
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}
		return r.append("    ").toString();
	}

	/**
	 * Get the itemsets in this sequential pattern
	 * @return a list of itemsets.
	 */
	public List<Itemset> getItemsets() {
		return itemsets;
	}
	
	/**
	 * Get an itemset at a given position.
	 * @param index the position
	 * @return the itemset
	 */
	public Itemset get(int index) {
		return itemsets.get(index);
	}
	
	/**
	 * Get the ith item in this sequential pattern.
	 * @param i the position of the item.
	 * @return the item or null if the position does not exist.
	 */
	public Integer getIthItem(int i) { 
		// for each item
		for(int j=0; j< itemsets.size(); j++){
			// check if the position is in this itemset
			if(i < itemsets.get(j).size()){
				// if yes, return the item
				return itemsets.get(j).get(i);
			}
			// otherwise subtract the size of this itemset
			// from i.
			i = i- itemsets.get(j).size();
		}
		return null; // if not found.
	}
	
	/**
	 * Get the number of itemsets in this sequential pattern.
	 * @return the number of itemsets.
	 */
	public int size(){
		return itemsets.size();
	}

	
	/**
	 * Get the number of items in this pattern.
	 * Note that if an item appear twice, it will be counted twice.
	 * @return the number of items
	 */
	public int getItemOccurencesTotalCount(){
		if(itemCount == -1) {
			itemCount =0;
			// for each itemset
			for(Itemset itemset : itemsets){
				// add the size of this itemset
				itemCount += itemset.size();
			}
		}
		return itemCount; // return the total size.
	}

	public Set<Integer> getSequenceIDs() {
		// TODO Auto-generated method stub
		return sequencesIds;
	}

	@Override
	public int compareTo(SequentialPattern o) {
			if(o == this){
				return 0;
			}
			int compare = this.getAbsoluteSupport() - o.getAbsoluteSupport();
			if(compare !=0){
				return compare;
			}

			return this.hashCode() - o.hashCode();
		}
}
