package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings;
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
import java.util.Map;
import java.util.Set;



/**
 * This class represents a sequential pattern found by the PrefixSpan or BIDE algorithm.
 * A sequential pattern is represented by these algorithms as a list of itemsets with an associated
 * hash set indicating the IDS of the sequence containing this sequential pattern.

@see AlgoPrefixSpan_with_Strings
@see AlgoBIDEPlus_withStrings
@see Itemset
* @author Philippe Fournier-Viger
 */
public class SequentialPattern{
	/** list of itemsets */
	private final List<Itemset> itemsets = new ArrayList<Itemset>();
	
	/** sequence id */
	private int id; 
	
	/** List of IDS of all patterns that contains this one. */
	private Set<Integer> sequencesID = null;
	
	/**
	 * Constructor
	 * @param id the sequence id
	 */
	SequentialPattern(int id){
		this.id = id;
	}
	
	/**
	 * Get the relative support of this pattern (a percentage)
	 * @param sequencecount the number of sequences in the original database
	 * @return the support as a string
	 */
	public String getRelativeSupportFormated(int sequencecount) {
		double frequence = ((double)sequencesID.size()) / ((double) sequencecount);
		// pretty formating :
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(frequence);
	}
	
	/**
	 * Get the absolute support of this pattern.
	 * @return the support (an integer >= 1)
	 */
	public int getAbsoluteSupport(){
		return sequencesID.size();
	}

	/**
	 * Add an itemset to this sequential pattern
	 * @param itemset the itemset to be added
	 */
	 void addItemset(Itemset itemset) {
		itemsets.add(itemset);
	}
	
	/**
	 * Make a copy of this sequential pattern
	 * @return the copy.
	 */
	SequentialPattern cloneSequence(){
		SequentialPattern sequence = new SequentialPattern(getId());
		for(Itemset itemset : itemsets){
			sequence.addItemset(itemset.cloneItemSet());
		}
		return sequence;
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
		for(Itemset itemset : itemsets){
			r.append('(');
			for(String item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append(')');
		}

		//  print the list of Pattern IDs that contains this pattern.
		if(getSequencesID() != null){
			r.append("  Sequence ID: ");
			for(Integer id : getSequencesID()){
				r.append(id);
				r.append(' ');
			}
		}
		return r.append("    ").toString();
	}
	
	/**
	 * Get a string representation of this sequential pattern.
	 */
	public String itemsetsToString() {
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : itemsets){
			for(String item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}
		return r.append("    ").toString();
	}
	
	public int getId() {
		return id;
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
	public String getIthItem(int i) { 
		for(int j=0; j< itemsets.size(); j++){
			if(i < itemsets.get(j).size()){
				return itemsets.get(j).get(i);
			}
			i = i- itemsets.get(j).size();
		}
		return null;
	}
	/**
	 * Get the number of itemsets in this sequential pattern.
	 * @return the number of itemsets.
	 */
	public int size(){
		return itemsets.size();
	}

	public Set<Integer> getSequencesID() {
		return sequencesID;
	}

	void setSequencesID(Set<Integer> sequencesID) {
		this.sequencesID = sequencesID;
	}
	
	/**
	 * Get the number of items in this pattern.
	 * Note that if an item appear twice, it will be counted twice.
	 * @return the number of items
	 */
	int getItemOccurencesTotalCount(){
		int count =0;
		for(Itemset itemset : itemsets){
			count += itemset.size();
		}
		return count;
	}


	SequentialPattern cloneSequenceMinusItems(Map<String, Set<Integer>> mapSequenceID, double relativeMinSup) {
		SequentialPattern sequence = new SequentialPattern(getId());
		for(Itemset itemset : itemsets){
			Itemset newItemset = itemset.cloneItemSetMinusItems(mapSequenceID, relativeMinSup);
			if(newItemset.size() !=0){ 
				sequence.addItemset(newItemset);
			}
		}
		return sequence;
	}

	public void setID(int id2) {
		id = id2;
	}

}
