package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

import java.util.List;

import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
/**
 * This represents a sequence from a projected database (as based in PrefixSpan).
 * Since it is a projected sequence, it makes reference to the original sequence.
 * 
 * This class is used by the PrefixSpan and BIDE+ algorithms.
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
public class PseudoSequence {

	// the corresponding sequence in the original database
	protected Sequence sequence;

	// the first itemset of this pseudo-sequence  in the original sequence
	protected int firstItemset;
	// the first item of this pseudo-sequence in the original sequence
	protected int firstItem;
	
	/**
	 * Default constructor
	 */
	protected PseudoSequence(){
		
	}
	
	/**
	 * Get the original sequence corresponding to this projected sequence.
	 * @return the original sequence
	 */
	public Sequence getOriginalSequence() {
		return sequence;
	}


	/**
	 * Create a pseudo-sequence from a sequence that is a pseudo sequence.
	 * @param sequence the original pseudo-sequence.
	 * @param indexItemset the itemset where the pseudo-sequence should start in terms of the original sequence.
	 * @param indexItem the item where the pseudo-sequence should start in terms of the original sequence.
	 */
	protected PseudoSequence(PseudoSequence sequence, int indexItemset, int indexItem){
		// remember the original sequence
		this.sequence = sequence.sequence;
		// record the position of where the pseudo-sequence starts
		// in terms of the original pseudo-sequence
		this.firstItemset = indexItemset + sequence.firstItemset;
		if(this.firstItemset == sequence.firstItemset){
			this.firstItem = indexItem + sequence.firstItem;
		}else{
			this.firstItem = indexItem; 
		}
	}
	
	/**
	 * Create a pseudo-sequence from a sequence that is an original sequence.
	 * @param sequence the original sequence.
	 * @param indexItemset the itemset where the pseudo-sequence should start in terms of the original sequence.
	 * @param indexItem the item where the pseudo-sequence should start in terms of the original sequence.
	 */
	protected  PseudoSequence(Sequence sequence, int indexItemset, int indexItem){
		// remember the original sequence
		this.sequence = sequence;
		// remember the starting position of this pseudo-sequence in terms
		// of the original sequence.
		this.firstItemset = indexItemset;
		this.firstItem = indexItem;
	}

	/**
	 * Return the size of this pseudo-sequence in terms of itemsets.
	 * @return the size.
	 */
	protected int size() {
		// the size is the size of the original sequence minus
		// the itemset where this pseudo-sequence start
		int size = sequence.size() - firstItemset;
		// if the size is 1 and it the only itemset is empty, return 0
		if(size == 1 && sequence.getItemsets().get(firstItemset).size() == 0){
			return 0;
		}
		// return the size
		return size;
	}

	/**
	 * Return the size in terms of items of an itemset at a given position
	 * (overloaded).
	 * @param index the position of the itemset
	 * @return the number of items in that itemset
	 */
	public int getSizeOfItemsetAt(int index) {
		// We obtain the size of the itemset by looking at the original
		// sequence. To obtain the position of the itemset we do
		//   index + firstItemset.
		int size = sequence.getItemsets().get(index + firstItemset).size();
		// if it is the first itemset of the pseudo-sequence
		if(isFirstItemset(index)){
			// we remove some items if this itemset is cut at left.
			size -=  firstItem;
		}
		return size; // return the size
	}

	/**
	 * Return true if this itemset is cut at left (a postfix).
	 * @param indexItemset the position of the given itemset.
	 * @return true if it is cut at left.
	 */
	protected boolean isPostfix(int indexItemset) {
		// if it is the first itemset of the pseudo-sequence
		// and it is cut at left, we return true.
		return indexItemset == 0  && firstItem !=0;
	}

	/**
	 * Method to check if an itemset is the first one of a pseudo-sequence
	 * @param index  the position of an itemset
	 * @return true if it is the first one.
	 */
	protected boolean isFirstItemset(int index) {
		return index == 0;
	}
	
	/**
	 * Method to check if an itemset is the last one of a pseudo-sequence
	 * @param index  the position of an itemset
	 * @return true if it is the last one.
	 */
	protected boolean isLastItemset(int index) {
		return (index + firstItemset) == sequence.getItemsets().size() -1;
	}

	/**
	 * Get an item at a given position inside a given itemset
	 * @param indexItem the position of the item
	 * @param indexItemset the position of the itemset
	 * @return the item.
	 */
	public Integer getItemAtInItemsetAt(int indexItem, int indexItemset) {
		// if it is in the first itemset
		if(isFirstItemset(indexItemset)){
			// we need to consider if the itemset was cut at the left
			// by adding "firstItem"
			return getItemset(indexItemset).get(indexItem + firstItem);
		}else{// otherwise
			return getItemset(indexItemset).get(indexItem);
		}
	}

	/**
	 * Get the itemset at a given position
	 * @param index the position of the itemset
	 * @return the itemset
	 */
	public List<Integer> getItemset(int index) {
		return sequence.get(index+firstItemset);
	}

	/**
	 * Get the sequence ID of this sequence.
	 * @return a sequence ID (integer)
	 */
	protected int getId() {
		return sequence.getId();
	}

	/**
	 * Print this pseudo-sequence to System.out.
	 */
	public void print() {
		System.out.print(toString());
	}

	/**
	 * Get a string representation of this sequence.
	 */
	public String toString() {
		StringBuffer r = new StringBuffer();
		// for each itemset
		for(int i=0; i < size(); i++){
			// for each item
			for(int j=0; j < getSizeOfItemsetAt(i); j++){
				// append the item
				r.append(getItemAtInItemsetAt(j, i).toString());
				// if it is in a postfix, we add a "*" symbol beside the item
				if(isPostfix(i)){
					r.append('*');
				}

//				if(!isLastItemset(i) ){
					r.append(' ');
//				}
			}
			r.append(" -1 "); // end of an itemset
		}
		r.append(" -2 ");
		// return the string
		return r.toString();
	}

	/**
	 * Get the position of an item inside an itemset.
	 * @param indexItemset the given itemset position
	 * @param idItem the item that we want to search.
	 * @return the position of the item or -1 if it is not found
	 */
	protected int indexOfBis(int indexItemset, int idItem) {
		// for each item in that itemset
		for(int i=0; i < getSizeOfItemsetAt(indexItemset); i++){
			// check if equals to the item that we search
			if(getItemAtInItemsetAt(i, indexItemset) == idItem){
				return i; // if equal, return the current position
			}else if(getItemAtInItemsetAt(i, indexItemset) > idItem){
				continue;
			}
		}
		return -1; // not found, return -1.
	}
	
	/**
	 * Get the position of an item inside an itemset.
	 * @param indexItemset the given itemset position
	 * @param idItem the item that we want to search.
	 * @return the position of the item or -1 if it is not found
	 */
	protected int indexOf(int sizeOfItemsetAti, int indexItemset, int idItem) {
		// for each item in that itemset
		for(int i=0; i <sizeOfItemsetAti; i++){
			// check if equals to the item that we search
			if(getItemAtInItemsetAt(i, indexItemset) == idItem){
				return i; // if equal, return the current position
			}else if(getItemAtInItemsetAt(i, indexItemset) > idItem){
				continue;
			}
		}
		return -1; // not found, return -1.
	}
	
	@Override
	public boolean equals(Object obj) {
		PseudoSequence temp = (PseudoSequence) obj;
		return temp.getId() == getId() && firstItemset == temp.firstItemset &&
				temp.firstItem == this.firstItem;
	}

}