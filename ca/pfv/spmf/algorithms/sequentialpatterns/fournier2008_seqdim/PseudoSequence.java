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

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a sequence from a projected database (as based in PrefixSpan).
 * Since it is a projected sequence, it makes reference to the original sequence.
 * <br/><br/>
 * 
 * This class also include several methods for calculating the maximum periods, 
 * semi-maximum periods, etc. as required by the BIDE+ algorithm.
 * 
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */

class PseudoSequence {

//  this represents how much time unit have been removed when the sequence is cut at left
	private long timeShift; 
	// this is the original sequence
	private Sequence sequence;
	// the following variables indicates at which itemset indexes and item indexes 
	// the sequence has been cut at left and right in terms of the original sequence
	private int firstItemset;
	private int firstItem;
	private int lastItemset;
	private int lastItem;
	
	/**
	 * Constructor to build a new pseudo-sequence cut at left based on a pseudo-sequence.
	 * @param timeShift how much time unit have been removed when the pseudo-sequence is cut at left
	 * @param sequence the original pseudo-sequence
	 * @param indexItemset the first itemset index 
	 * @param indexItem the first item index
	 */
	public PseudoSequence(long timeShift, PseudoSequence sequence, int indexItemset, int indexItem){
		// save the parameters
		this.timeShift = timeShift;
		this.sequence = sequence.sequence;
		// the first itemset is expressed in terms of the first itemset
		// of the pseudo-sequence sequence
		this.firstItemset = indexItemset + sequence.firstItemset;
		//  if the first itemset is the first itemset of the original sequence
		if(this.firstItemset == sequence.firstItemset){
			// then the first item position is the sum of the current position with
			// the position in the  pseudo-sequence received as parameter
			this.firstItem = indexItem + sequence.firstItem;
		}else{
			// otherwise, just save the parameter
			this.firstItem = indexItem; 
		}
		// The last item and itemset is by default the last itemset and item
		// of the sequence
		this.lastItemset = sequence.lastItemset;
		this.lastItem = sequence.lastItem;
	}
	
	/**
	 * Constructor to build a new pseudo-sequence based on a pseudo-sequence, and cut
	 * at left and right.
	 * @param timeShift how much time unit have been removed when the pseudo-sequence is cut at left
	 * @param sequence the original pseudo-sequence
	 * @param indexItemset the first itemset index 
	 * @param indexItem the first item index
	 * @param lastItemset the last itemset index
	 * @param lastItem  the last item index
	 */
	public PseudoSequence(long timeShift, PseudoSequence sequence, int indexItemset, int indexItem, int lastItemset, int lastItem){
		// save the parameters
		this.timeShift = timeShift;
		this.sequence = sequence.sequence;
		// the first itemset is expressed in terms of the first itemset
		// of the pseudo-sequence sequence
		this.firstItemset = indexItemset + sequence.firstItemset;
		// if the cut is in the same first itemset
		if(this.firstItemset == sequence.firstItemset){
			// add the first item position to the first item position in original sequence
			this.firstItem = indexItem + sequence.firstItem;
		}else{
			// otherwise, the first item position is the item position received as parameter
			this.firstItem = indexItem;
		}
		// Set the last itemset and last item position as indicated
		this.lastItemset = lastItemset;
		this.lastItem = lastItem;
	}
	
	/**
	 * Constructor of a pseudo-sequence based on a sequence.
	 * @param timeShift  the number of time units removed from the original sequence
	 * @param sequence the original sequence
	 * @param indexItemset  where the pseudo sequence starts in terms of itemset index
	 * @param indexItem    where the pseudo sequence starts in terms of item index in the itemset
	 *                     where the pseudosequence starts.
	 */
	public  PseudoSequence(long timeShift, Sequence sequence, int indexItemset, int indexItem){
		// save the parameters
		this.timeShift = timeShift;
		this.sequence = sequence;
		this.firstItemset = indexItemset;
		this.firstItem = indexItem;
		// Last itemset and item  (by default, this is the last item & itemset of the sequence.
		this.lastItemset = sequence.size()-1;
		this.lastItem = sequence.getItemsets().get(lastItemset).size()-1;
	}
	
	/**
	 * Get the size of this sequence in terms of itemset count.
	 * @return the size (int)
	 */
	public int size(){
		// size is calculated as follows:
		int size = sequence.size() - firstItemset - ((sequence.size()-1) - lastItemset);
		// if size 1 and there is no item in the itemset, then return 0
		if(size == 1 && sequence.getItemsets().get(firstItemset).size() == 0){
			return 0;
		}
		// otherwise return the calculated size.
		return size;
	}
	
	/**
	 * Get the size of the i-th itemset in a pseudo sequence
	 * @param index  i
	 * @return  the size in terms of item count
	 */
	public int getSizeOfItemsetAt(int index){
		// get the size of the itemset
		int size = sequence.getItemsets().get(index + firstItemset).size();
		// if it is the last itemset of the pseudoseq. consider that the
		// sequence may be cut at right
		if(isLastItemset(index)){
			size -= ((size -1) - lastItem);
		}
		// if it is the first itemset of the pseudoseq. consider that the
		// sequence may be cut at left
		if(isFirstItemset(index)){
			size -=  firstItem;
		}
		// return the size
		return size;
	}
	
	/**
	 * Check if the itemset at position "index" is cut at right 
	 * @param index  the index
	 * @return true if cut at right. Otherwise false.
	 */
	boolean isCutAtRight(int index){
		if(!isLastItemset(index)){
			return false;
		}
		return (sequence.getItemsets().get(index + firstItemset).size() -1) != lastItem;
	}
	
	/**
	 * Check if the itemset at position "index" is cut at left
	 * @param indexItemset  the index
	 * @return true if cut at false. Otherwise false.
	 */
	public boolean isCutAtLeft(int indexItemset){
		return indexItemset == 0  && firstItem !=0;
	}
	
	/**
	 * Check if the itemset at position "index" is the first itemset of the pseudoseq.
	 * @param index  the position "index"
	 * @return  true if yes, otherwise false.
	 */
	public boolean isFirstItemset(int index){
		return index == 0;
	}
	
	/**
	 * Check if the itemset at position "index" is the last itemset of the pseudoseq.
	 * @param index  the position "index"
	 * @return  true if yes, otherwise false.
	 */
	public boolean isLastItemset(int index){
		return (index + firstItemset) == lastItemset;
	}
	
	/**
	 * Get the item at a given position in a given itemset
	 * @param indexItem  the item position
	 * @param indexItemset the itemset position
	 * @return the item
	 */
	public ItemSimple getItemAtInItemsetAt(int indexItem, int indexItemset){
		// if in the first itemset
		if(isFirstItemset(indexItemset)){
			// we need to add the first item position to the item position
			return getItemset(indexItemset).get(indexItem + firstItem);
		}else{
			// otherwise, just return the item at the given position in this itemset
			return getItemset(indexItemset).get(indexItem);
		}
	}
	
	/**
	 * Get the timestamp associated to a given itemset
	 * @param indexItemset  the index of the itemset.
	 * @return  the timestamp
	 */
	public long getTimeStamp(int indexItemset){
		//we subtract the time units removed from the original sequence.
		return getItemset(indexItemset).getTimestamp() - timeShift;
	}
	
	
	/**
	 * Get the timestamp associated to a given itemset without considering the time shift.
	 * @param indexItemset  the index of the itemset.
	 * @return  the timestamp
	 */
	public long getAbsoluteTimeStamp(int indexItemset){
		return getItemset(indexItemset).getTimestamp();
	}
	
	/**
	 * Get the itemset at position index.
	 * @param index the position
	 * @return an Itemset
	 */
	private Itemset getItemset(int index){
		return sequence.get(index+firstItemset);
	}

	/**
	 * Get the ID of this sequence.
	 * @return an integer
	 */
	public int getId() {
		return sequence.getId();
	}

	/**
	 * Print this sequence to System.out.
	 */
	public void print() {
		System.out.print(toString());
	}
	
	/**
	 * Get a String representation of this sequence
	 * @return a String
	 */
	public String toString(){
		// create string buffer
		StringBuffer r = new StringBuffer();
		// for each itemset
		for(int i=0; i < size(); i++){
			// print timestamp
			r.append("{t=");
			r.append(getTimeStamp(i));
			r.append(", ");
			// print items in this itemset
			for(int j=0; j < getSizeOfItemsetAt(i); j++){
				if(!isLastItemset(i) || (j <= lastItem)){
					r.append(getItemAtInItemsetAt(j, i).toString());
					// if cut a left, print "*"
					if(isCutAtLeft(i)){  
						r.append('*');
					}
					r.append(' ');
				}
			}

			r.append("}");
		}
		r.append("  ");
		// return the string representation of the Stringbuffer
		return r.toString();
	}

	/**
	 * Find the position of an item in an itemset.
	 * @param indexItemset the itemset
	 * @param idItem  the item that is searched for
	 * @return  the position or -1 if the item does not appear in the itemset
	 */
	public int indexOf(int indexItemset, int idItem) {
		for(int i=0; i < getSizeOfItemsetAt(indexItemset); i++){
			// if the item is found, then return the position
			if(getItemAtInItemsetAt(i, indexItemset).getId() == idItem){
				return i;
			}
		}
		// not found, return -1
		return -1;
	}
	
	/******************************************
	 * All the following methods are specific to BIDE+.
	 * I provide a brief description of them.
	 * Please refer to the original BIDE+ paper for more
	 * details about the BIDE+ algorithm and how it works because
	 * it is quite complex.
	 * 
	 * Remember that a sequential pattern is not closed if there exists 
	 * a forward-extension or a backward-extension.
	 ******************************************/

	/**
	 * Structure to contains a sequence and list of positions to elements in the sequence.
	 */
	static class PseudoSequencePair{
		// a pseudo sequence
		final PseudoSequence pseudoSequence;
		// a list of positions
		final List<Position> list;
		// a simple constructor
		public PseudoSequencePair(PseudoSequence pseudoSequence, List<Position> list){
			this.pseudoSequence = pseudoSequence;
			this.list = list;
		}
	}
	
	/**
	 * Internal class representing a position in a pseudo-sequence.
	 * (position of an itemset + position of an item).
	 */
	static class Position{
		final int itemset;
		final int item;
		public Position(int itemset, int item){
			this.itemset = itemset;
			this.item = item;
		}
	}
	
	/**
	 * Method that find all instances of a prefix in a sequence S.
	 * The meaning of instance is the one from the BIDE+ article when they tak about
	 * "first instance", "last instance".  Here instead of finding only 
	 * the "first instance" or "last instance", we find all instances and
	 * they also respect the timestamps!
	 */
	 List<PseudoSequencePair> getAllInstancesOfPrefix(Sequence prefix, int i){
		List<List<Position>> listInstances  
		  = getAllInstancesOfPrefixHelper(prefix, 0, new ArrayList<List<Position>>(), new ArrayList<Position>(), 0, 0);
		//we cut the instances found according to the maximum size
		// of the prefix that we are searching for.
		List<PseudoSequencePair> allPairs = new ArrayList<PseudoSequencePair>();
		for(List<Position> listPositions : listInstances){
			PseudoSequence newSequence = new PseudoSequence(0, this, this.firstItemset, this.firstItem, 
					listPositions.get(i-1).itemset, listPositions.get(i-1).item);
			allPairs.add(new PseudoSequencePair(newSequence,listPositions));
		}
		return allPairs;
	}
	
	// helper for the above method
	 List<List<Position>> getAllInstancesOfPrefixHelper(Sequence prefix, int indexItemset, 
			 List<List<Position>> allInstances, List<Position> listPositionsTotal, 
			long itemsetShift, int decalageItemset){

		for(int i=decalageItemset; i< size(); i++){
			boolean firstTime = indexItemset ==0;
			if(!firstTime && getTimeStamp(i)-itemsetShift != prefix.get(indexItemset).getTimestamp()){  // VÉRIFIER DÉCALAGE TEMPS
				continue;
			}
			int indexItem =0;
			List<Position> listPositions = new ArrayList<Position>();
			int iDCourant = prefix.get(indexItemset).get(indexItem).getId();
			
			for(int j=0; j < getSizeOfItemsetAt(i); j++){
				int id = getItemAtInItemsetAt(j, i).getId();
				if(id == iDCourant){// l'item match
					listPositions.add(new Position(i,j));
					if(listPositions.size()+ listPositionsTotal.size()	== prefix.getItemOccurencesTotalCount())  // si on a trouvé tout le préfix
					{
						List<Position> newList = new ArrayList<Position>(listPositionsTotal);
						newList.addAll(listPositions);
						allInstances.add(newList);
					}else if(indexItem+1 >= prefix.get(indexItemset).size()){ 
						// if we have found the itemset
						long decalage = firstTime ? getTimeStamp(i) : itemsetShift;
						
						List<Position> newList = new ArrayList<Position>(listPositionsTotal);
						newList.addAll(listPositions);
						
						if(indexItemset+1 < prefix.size()){ 
							getAllInstancesOfPrefixHelper(prefix, indexItemset+1, allInstances, newList, decalage, i+1);
						}
					}else{
						indexItem++;
						iDCourant = prefix.get(indexItemset).get(indexItem).getId();
					}
				}
			}
		}
		return allInstances;
	}

	/**
	 * Last instance of a prefix sequence X in a sequence S.
	 * For example, the last instance of AB in ABBCA is  ABB.
	 * Additionnal difficulty : this sequence must respect timestamps!
	 */
	  PseudoSequencePair getLastInstanceOfPrefixSequence(Sequence prefix, int i){
		List<PseudoSequencePair> list = getAllInstancesOfPrefix(prefix, i);
		// Return the last one
		PseudoSequencePair sequenceRetourPair = list.get(0);
		for(PseudoSequencePair sequencePair : list){
			PseudoSequence sequence = sequencePair.pseudoSequence;
			PseudoSequence sequenceRetour = sequenceRetourPair.pseudoSequence;
			if((sequence.lastItemset > sequenceRetour.lastItemset) ||
					(sequenceRetour.lastItemset == sequence.lastItemset  && sequence.lastItem > sequenceRetour.lastItem)
					){
				sequenceRetourPair = sequencePair;
			}
		}
		return sequenceRetourPair;
	}

	/**
	 * First Instance of the prefix X in a sequence S.
	 * Method to find the first Instance of the prefix sequence X= e1, e2... ei+1 in a sequence S.
	 * Exemple: first instance of AB in the sequence CAABC = CAAB.
	 * Additionnal difficulty : this sequence must respect timestamps!
	 */
	 PseudoSequencePair getFirstInstanceOfPrefixSequence(Sequence prefix, int i){
		List<PseudoSequencePair> list = getAllInstancesOfPrefix(prefix, i);
		// Return the first one
		PseudoSequencePair sequenceRetourPair = list.get(0);
		for(PseudoSequencePair sequencePair : list){
			PseudoSequence sequence = sequencePair.pseudoSequence;
			PseudoSequence sequenceRetour = sequenceRetourPair.pseudoSequence;
			if((sequence.lastItemset < sequenceRetour.lastItemset) ||
					(sequenceRetour.lastItemset == sequence.lastItemset  && sequence.lastItem < sequenceRetour.lastItem)){
				sequenceRetourPair = sequencePair;
			}
		}
		return sequenceRetourPair;
	}
	
	/**
	 * Get the ith last-in-last appearance with respect to a prefix sequence Sp.
	 * n = size of S
	 * If i == n, it is the last appearance of ei in the last instance of Sp.
	 * If 0 <= i < n, it is the last appearance of ei in the last instance of Sp, and LLi must appear
	 *   before LLi+1.
	 * Example : If S= CAABC and SP  = AB   then  LL0 =  second A in CAABC
	 *           If S= CACAC and SP = CAC  then LL0 =  second C in S, 
	 *                                          LL1 =  second A in S and 
	 *                                          LL2 = third C in S
	 * @param prefix : le prefix
	 * @param i : le ième élément du préfixe.
	 * @return the ith last-in-last appearance
	 */
	Position getIthLastInLastApearanceWithRespectToPrefix(Sequence prefix, int i, boolean withTimeStamps){
		// we obtain the last instance:
		// The last instance is a PseudoSequencePair object.
		// It consists of 
		// - the pseudo sequence that is the last instance
		// - the list of positions for each element of prefix in that last instance.
		PseudoSequencePair lastInstancePair = getLastInstanceOfPrefixSequence(prefix, prefix.getItemOccurencesTotalCount());
		
		// IF WE DON'T USE TIMESTAMP THE "ITH LAST IN LAST" IS A LITTLE BIT COMPLICATED TO GET : 
		if(withTimeStamps == false){
			// ith item of prefix id is :
			int iditem = prefix.getIthItem(i).getId();
			
			if(i == prefix.getItemOccurencesTotalCount()-1){
				// return the last occurence of that item:
				for(int j=lastInstancePair.pseudoSequence.size()-1; j>=0; j--){
					for(int k=lastInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(lastInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}else{
				// return the last before LLi+1
				Position LLiplus1 = getIthLastInLastApearanceWithRespectToPrefix(prefix, i+1, false);
				for(int j=LLiplus1.itemset; j>=0; j--){
					for(int k=lastInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(j == LLiplus1.itemset && k >= LLiplus1.item){
							continue;
						}
						if(lastInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}
			return null; // should not happen!
		}
		else{
			// IF WE USE TIMESTAMPS, THE "ITH LAST IN LAST" IS SIMPLE TO GET :
			return lastInstancePair.list.get(i);
		}
	}
	


	/**
	 * Get the ith maximum period of a prefix sequence for this sequence S.
	 * The ith maximum period of the prefix Sp in S is : 
	 *  * if 0 < i <= n, it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
	 *    and the ith last-in-last appearance with respect to prefix Sp
	 *  * if i = 0, it is the piece of sequence in S located before the first last-in-last appearance with respect to prefix Sp.
	 *   Example1:  if S = ABCB  and Sp = AB
	 *   	the 1th semi-period of Sp in S is empty
	 *      the 2th semi-period of Sp in S is BC
	 *   Example2:  if S = ABBB  and Sp = BB
	 *   	the 1th semi-period of Sp in S is AB
	 *      the 2th semi-period of Sp in S is B
	 */
	PseudoSequence getIthMaximumPeriodOfAPrefix(Sequence prefix, int i, boolean withTimeStamps){
		if(i == 0){ //it is the piece of sequence in S located before the first last-in-last appearance with respect to prefix Sp.
			Position ithlastlast = getIthLastInLastApearanceWithRespectToPrefix(prefix, 0, withTimeStamps);
			return trimBeginingAndEnd(null, ithlastlast);
		}
		
		// ELSE it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
		// and the ith last-in-last appearance with respect to prefix Sp
		// Important : We thus have to cut the prefix at ei-1  (short prefix = e1 ... ei-1). 
		//      It is because the parameter i is used by getLastInstanceOfPrefixSequence(...). (???)
		PseudoSequencePair firstInstance = this.getFirstInstanceOfPrefixSequence(prefix, i); 
		Position lastOfFirstInstance = firstInstance.list.get(i-1); 
		
		Position ithlastlast = this.getIthLastInLastApearanceWithRespectToPrefix(prefix, i, withTimeStamps);
		return trimBeginingAndEnd(lastOfFirstInstance, ithlastlast);
	}
	
	// NEW09 ---------- for the algorithm with timestamps
	List<PseudoSequence> getAllIthMaxPeriodOfAPrefix(Sequence prefix,
			int i, boolean b) {
		if(i == 0){ 
			List<PseudoSequence> periods = new ArrayList<PseudoSequence>();
			for(PseudoSequencePair instance: getAllInstancesOfPrefix(prefix, prefix.getItemOccurencesTotalCount())){
				PseudoSequence period = trimBeginingAndEnd(null, instance.list.get(0));
				periods.add(period);
			}
			return periods;
		}
		
		List<PseudoSequence> periods = new ArrayList<PseudoSequence>();
		for(PseudoSequencePair instance: getAllInstancesOfPrefix(prefix, i)){
			PseudoSequence period = trimBeginingAndEnd(instance.list.get(i-1), instance.list.get(i));
			periods.add(period);
		}
		return periods;
	}

	
	
	
	
	
	
	
	/**
	 * Method that is used by the one above.
	 * This method cut a sequence by removing some part at the begining and at the end.
	 * IMPORTANT : this method assumes that the sequence has never been cut.
	 * This simplify what this method has to do to handle time.
	 * 
	 * @return null If the result is an empty sequence.!
	 */
	PseudoSequence trimBeginingAndEnd(Position positionStart, Position positionEnd){
		int itemsetStart = 0;
		int itemStart =0;
		int itemsetEnd=lastItemset;
		int itemEnd=lastItem;
		long newTimeStamp = 0;
		
		if(positionStart != null){  // Coupe du debut
			itemsetStart = positionStart.itemset;
			itemStart =  positionStart.item + 1;
			if(itemStart == getSizeOfItemsetAt(itemsetStart)){
				itemsetStart++;
				itemStart =0;
			}
			if(itemsetStart == size()){// the resulting sequence is empty!
				return null;
			}
			newTimeStamp = getTimeStamp(itemsetStart);
		}
		
		if(positionEnd != null){ // We cut the right part
			itemsetEnd = positionEnd.itemset;
			itemEnd = positionEnd.item -1;
			if(itemEnd<0){
				itemsetEnd--;
				if(itemsetEnd < itemsetStart){
					return null;
				}
				itemEnd = getSizeOfItemsetAt(itemsetEnd)-1;
			}
		}
		// Check if the end is not before the beginning of the sequence!
		if(itemsetEnd == itemsetStart && itemEnd < itemStart){ 
			return null;
		}
		return new PseudoSequence(newTimeStamp, this, itemsetStart, itemStart, itemsetEnd, itemEnd);
	}

	 long getTimeShift() {
		return timeShift;
	}

	long getTimeSucessor() {
		
		int positionLastElement = size()-1;
		int absolutePositionLastElement = size()-1 + firstItemset;
		//firstItem ==0
//		lastIteml
		if(isCutAtRight(positionLastElement)  // NEW PHIL 2008-09
		){ //if the last itemset is cut at the right
			return getAbsoluteTimeStamp(positionLastElement);
		}else if(absolutePositionLastElement < sequence.size()-1){ // if the last itemset is not the last itemset of the original sequence.
			return sequence.get(absolutePositionLastElement+1).getTimestamp();
		}
		return 0; // if it is the last itemset of the original sequence and it was not cut.
	}
	
	public long getTimePredecessor() {  // NOUVEAU 2008-09-11
		if(firstItemset ==0){
			return 0;
		}
		if(firstItem ==0){
			return getAbsoluteTimeStamp(-1);
		}else{
			return getAbsoluteTimeStamp(0);
		}
	}

	//----------------------- Backscan search space pruning  sept. 2009 updated
	
	/**
	 * Get the ith first-in-last appearance with respect to a prefix sequence Sp.
	 * n = size of S
	 * If i == n, it is the last appearance of ei in the first instance of Sp.
	 * If 0 <= i < n, it is the last appearance of ei in the first instance of Sp, and LFi must appear
	 *   before LFi+1.
	 * @param prefix : the prefix
	 * @param i : the ième element of the prefix.
	 * @return the ith first-in-last appearance.
	 */
	Position getIthLastInFirstApearanceWithRespectToPrefix(Sequence prefix, int i, boolean withTimestamps){
		// First, we get the first instance.
		// The first instance is a PseudoSequencePair object.
		// It consists of 
		// - the pseudosequence that is the first instance
		// - the list of positions for each element of prefix in that first instance.
		PseudoSequencePair firstInstancePair = getFirstInstanceOfPrefixSequence(prefix, prefix.getItemOccurencesTotalCount());
		
		// IF WE DON'T USE TIMESTAMP THE "ITH LAST IN LAST" IS A LITTLE BIT COMPLICATED TO GET : 
		if(withTimestamps == false){
			// ith item of prefix id is :
			int iditem = prefix.getIthItem(i).getId();
			
			if(i == prefix.getItemOccurencesTotalCount()-1){
				// return the last occurence of that item:
				for(int j=firstInstancePair.pseudoSequence.size()-1; j>=0; j--){
					for(int k=firstInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(firstInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}else{
				// return the last before LLi+1
				Position LLiplus1 = getIthLastInFirstApearanceWithRespectToPrefix(prefix, i+1, false);
				if(LLiplus1 == null){
					System.out.println("DEBUG");
				}
				for(int j= LLiplus1.itemset; j>=0; j--){
					for(int k=firstInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(j == LLiplus1.itemset && k >= LLiplus1.item){
							continue;
						}
						if(firstInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}
			return null; // should not happen!
		}
		else{
			return firstInstancePair.list.get(i);
		}
	}
	
	/**
	 * Get the ith semi-maximum period of a prefix sequence for this sequence S.
	 * The ith semi-maximum period of the prefix Sp in S is : 
	 *  * if 0 < i <= n, it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
	 *    and the ith last-in-first appearance with respect to prefix Sp
	 *  * if i = 0, it is the piece of sequence in S located before the first last-in-first appearance with respect to prefix Sp.
	 */
	PseudoSequence getIthSemiMaximumPeriodOfAPrefix(Sequence prefix, int i, boolean withTimestamps){
				
		if(i == 0){ //it is the piece of sequence in S located before the first last-in-first appearance with respect to prefix Sp.
			Position ithlastfirst = getIthLastInFirstApearanceWithRespectToPrefix(prefix, 0, withTimestamps);
			return trimBeginingAndEnd(null, ithlastfirst);
		}
		
		// ELSE it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
		// and the ith last-in-first appearance with respect to prefix Sp
		/// Important: we have to cut the prefix at ei-1  (short prefix = e1 ... ei-1). 
		//  since the parameter i is used by getLastInstanceOfPrefixSequence(...) and.....(???)
		
		// THIS IS DONE AS FOLLOWS:
		// We get the first instance of prefix e1... ei-1
		PseudoSequencePair firstInstance = this.getFirstInstanceOfPrefixSequence(prefix, i);  // e1... ei-1
		// we get the position of the last item of that first instance
		Position endOfFirstInstance = firstInstance.list.get(i-1); 
		
		// we get the ith last-in-first appearance with respect to prefix Sp
		Position ithlastfirst = this.getIthLastInFirstApearanceWithRespectToPrefix(prefix, i, withTimestamps);
		//we return the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
		// and the ith last-in-first appearance with respect to prefix Sp
		return trimBeginingAndEnd(endOfFirstInstance, ithlastfirst);
	}

	
}