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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;


/**
 * Implementation of a sequence (a list of itemsets), which 
 * represents a sequential pattern or a sequence of a sequence database, as used by SeqDim and
 * Fournier-Viger (2008) algorithms.
 * 
 * @see AlgoSeqDim
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */
public class Sequence{
	/** indicate by how many time units a shift was performed for this
	 sequence (used for working with timestamps based on the
	 Hirate & Yamana algorithm). */
	private long shift = 0;
	
	/** the list of itemsets in that sequence */
	private final List<Itemset> itemsets = new ArrayList<Itemset>();
	
	/** the sequence ID  */
	private int id; 
	
	/** List of IDS of all patterns that contains this one. */
	private Set<Integer> sequencesID = null;
	
	/**
	 * Constructor.
	 * @param id a sequence ID.
	 */
	public Sequence(int id){
		this.id = id;
	}
	
	/**
	 * Get the support of this itemset as a percentage (double value).
	 * @param databaseSize the number of sequences in the database.
	 * @return the support as a string with five decimals
	 */
	public String getRelativeSupportFormated(int databaseSize) {
		// calculate the support
		double support = ((double)sequencesID.size()) / ((double) databaseSize);
		// format as a String with two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		// return the string
		return format.format(support);
	}
	
	/***
	 * Get the support of this sequential pattern as an integer value.
	 * @return an integer.
	 */
	public int getAbsoluteSupport(){
		return sequencesID.size();
	}

	/**
	 * Add an itemset to this sequence.
	 * @param itemset An Itemset
	 */
	public void addItemset(Itemset itemset) {
		itemsets.add(itemset);
	}
	
	/**
	 * Make a copy of this sequence
	 * @return a new Sequence
	 */
	public Sequence cloneSequence(){
		// create new sequence with same ID
		Sequence sequence = new Sequence(getId());
		// for each itemset, make a copy and add it to the new sequence
		for(Itemset itemset : itemsets){
			sequence.addItemset(itemset.cloneItemSet());
		}
		// return the new sequence.
		return sequence;
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
		// create string buffer
		StringBuffer r = new StringBuffer("");
		// for each itemset
		for(Itemset itemset : itemsets){
			// append timestamp
			r.append("{t=");
			r.append(itemset.getTimestamp());
			r.append(", ");
			// append each item from this itemset
			for(ItemSimple item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}

		//  print the list of IDs  of sequences that contains this pattern.
		if(getSequencesID() != null){
			r.append("  Sequence ID: ");
			for(Integer id : getSequencesID()){
				r.append(id);
				r.append(' ');
			}
		}
		// return the string
		return r.append("    ").toString();
	}
	
	/**
	 * Return an abbreviated string representation of this sequence.
	 */
	public String toStringShort() {
		// create string buffer
		StringBuffer r = new StringBuffer("");
		// for each itemset
		for(Itemset itemset : itemsets){
			// appennd its timestamp
			r.append("{t=");
			r.append(itemset.getTimestamp());
			r.append(", ");
			// append all items in that itemset
			for(ItemSimple item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}
		// return the string
		return r.append("    ").toString();
	}
	
	/**
	 * Get a String representation of the itemsets in this Sequence.
	 * @return a string
	 */
	public String itemsetsToString() {
		// create a stringbuffer
		StringBuffer r = new StringBuffer("");
		// for each itemset in that sequence
		for(Itemset itemset : itemsets){
			// append timestamp
			r.append("{t=");
			r.append(itemset.getTimestamp());
			r.append(", ");
			// append each item
			for(ItemSimple item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}
		// return the string
		return r.append("    ").toString();
	}
	
	/**
	 * Get the ID of this sequence.
	 * @return an integer.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the list of itemsets in this sequence.
	 * @return the list of Itemset objects.
	 */
	public List<Itemset> getItemsets() {
		return itemsets;
	}
	
	/**
	 * Get the itemset at a given position in this sequence.
	 * @param index the position
	 * @return the Itemset
	 */
	public Itemset get(int index) {
		return itemsets.get(index);
	}
	
	/**
	 * Get the ith item in this sequence.
	 * @param i the position i
	 * @return an Item.
	 */
	public ItemSimple getIthItem(int i) { 
		// for each itemset
		for(int j=0; j< itemsets.size(); j++){
			// if i is smaller than the current itemset size
			if(i < itemsets.get(j).size()){
				// return the item at position i
				return itemsets.get(j).get(i);
			}
			// otherwise subtract the current itemset size from i
			i = i- itemsets.get(j).size();
		}
		// if there is no i-th item, then return null.
		return null;
	}
	
	/**
	 * Get the size of this sequence (number of itemsets).
	 * @return the size (an integer).
	 */
	public int size(){
		return itemsets.size();
	}

	/**
	 * Get the sequence IDs containing this seq. pattern
	 * @return a Set of Integer
	 */
	public Set<Integer> getSequencesID() {
		return sequencesID;
	}

	/**
	 * Set the sequence IDs containing this seq. pattern.
	 * @param sequencesID a Set of Integer.
	 */
	public void setSequencesID(Set<Integer> sequencesID) {
		this.sequencesID = sequencesID;
	}
	
	/**
	 * Return the sum of the size of all itemsets of this sequence.
	 */
	public int getItemOccurencesTotalCount(){
		// initialize counter
		int count =0;
		// for each itemset in that sequence
		for(Itemset itemset : itemsets){
			// add the size of the itemset
			count += itemset.size();
		}
		// return thte count
		return count;
	}
	
	/**
	 * Get the time length in terms of time units of this sequential pattern
	 * (is useful if timestamps are used, otherwise it is 0)
	 * @return the time length as a long.
	 */
	public long getTimeLength() {
		return itemsets.get(itemsets.size()-1).getTimestamp() - itemsets.get(0).getTimestamp();
	}
	
	/**
	 * Check if this sequence contains another given sequence.
	 * @param sequence2  a given sequence
	 * @return 		Return 1 if this sequence STRICTLY contains sequence  
	 * 				Return 2 if this sequence is exactly the same as sequence2  
	 * 				Return 0 if this sequence does not contains sequence2.
	 */
	public int strictlyContains(Sequence sequence2) {
		// call another recursive method to check if this sequence is contained
		int retour = strictlyContainsHelper(sequence2, 0, 0, 0, 0);
		// if it is contained
		if(retour ==2){
			//  if the size is the same, they are equal, otherwise it is strictly contained
			return (size() == sequence2.size()) ? 2 : 1;
		}
		// return the value found by the other method
		return retour;
	}
	
	/**
	 * Helper method for checking if this sequence is contained in another given sequence
	 * @param sequence2 the given sequence
	 * @param index  itemset position indicating where the comparison should
	 *    start for this sequence
	 * @param index2 itemset position indicating where the comparison should
	 *    start for the given sequence
	 * @param previousTimeStamp  previous timestamp in this sequence
	 * @param previousTimeStamp2 previous timestamp in the given sequence
	 * @return Return 1 if this sequence STRICTLY contains sequence  
	 * 				Return 2 if this sequence is exactly the same as sequence2  
	 * 				Return 0 if this sequence does not contains sequence2.
	 */
	private int strictlyContainsHelper(Sequence sequence2, int index, int index2, 
			long previousTimeStamp, long previousTimeStamp2) {
		if(index == size()){
			// then this sequence does not contain the given sequence
			return 0;
		}
		// if the size of this sequence minus the current position is smaller
		// than what is remaining to be compared in the given sequence, 
		// then it cannot be conained so return 0.
		if(size() - index < sequence2.size() - index2){
			return 0;
		}
		
		//
		int returnValue = 0;
		// for each itemset in this sequence starting from the index position
		for(int i=index; i <size(); i++){
			// calculate timestamp interval between itemset i and previous itemset
			// for this sequence
			long interval1 =  get(i).getTimestamp() - previousTimeStamp; 
			// do the same thing for the given sequence
			long interval2 =  sequence2.get(index2).getTimestamp() - previousTimeStamp2; 
			
			// if itemset at position i contains all items from the itemset at position
			// index in the given sequence and that they have the same time intervals
			if(get(i).getItems().containsAll(sequence2.get(index2).getItems()) && interval1 == interval2){
				// check if the two itemsets have the same size
				boolean sameSize = get(i).getItems().size() == sequence2.get(index2).size();

				// If we have found here the last itemset of the given sequence
				if(sequence2.size()-1 == index2){ 
					// if this last itemset has the same size
					if(sameSize){ 
						// then it is strictly contained and we return 2
						return 2;
					}
					// otherwise strictly contains (1)
					returnValue = 1;
				}
				else{
					// if it was not the last itemset,
					// then the method is called recursively to try to 
					// find the next itemset of the given sequence
					int resultat = strictlyContainsHelper(sequence2, i+1, index2+1, get(i).getTimestamp(), sequence2.get(index2).getTimestamp());
					// if the result from this recursive call is 2 and 
					// they have the same size, they are equal so return 2
					if(resultat == 2 && sameSize){
						return 2;
					}else if (resultat != 0){
						// otherwise, if !-0, then strictly contains and return -1.
						returnValue = 1;
					}
				}
			}
		} 
		// return the value calculated.
		return returnValue;
	}

	/**
	 * Make a copy of a sequence minus a given item
	 * @param mapSequenceID a map indicating for each item (key) the IDs of sequences
	 * containing the item (value).
	 * @param relativeMinSup  a minimum support value (double)
	 * @return a new Sequence
	 */
	public Sequence cloneSequenceMinusItems(Map<ItemSimple, Set<Integer>> mapSequenceID, double relativeMinSup) {
		// create sequence
		Sequence sequence = new Sequence(getId());
		// for each itemset
		for(Itemset itemset : itemsets){
			// make a copy without the new item
			Itemset newItemset = itemset.cloneItemSetMinusItems(mapSequenceID, relativeMinSup);
			// if the resulting itemset size is not 0
			if(newItemset.size() !=0){ 
				// add it to the sequence
				sequence.addItemset(newItemset);
			}
		}
		// return the new sequence
		return sequence;
	}

	/**
	 * Set the ID of this squence.
	 * @param id2 a new ID.
	 */
	public void setID(int id2) {
		id = id2;
	}

}
