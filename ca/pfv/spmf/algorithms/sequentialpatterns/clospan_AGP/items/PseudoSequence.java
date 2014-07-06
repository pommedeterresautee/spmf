package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a sequence from a projected database (as based in PrefixSpan).
 * Since it is a projected sequence, it makes reference to the original sequence.
 * 
 * This class is inspired in SPMF PrefixSpan implementation.
 *
 * Copyright Antonio Gomariz Peñalver 2013
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
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author agomariz
 */
public class PseudoSequence {

    /**
     * List with the corresponding timeshift that each projection in the sequence has
     */
    private List<Long> timeShift = new ArrayList<Long>();
    /**
     * Sequence where the different projections of the pseudosequence refer
     */
    private Sequence sequence;
    /**
     * List with the first itemset where each projection starts
     */
    private List<Integer> firstItemset = new ArrayList<Integer>();
    /**
     * List with the first item where each projection starts
     */
    private List<Integer> firstItem = new ArrayList<Integer>();

    /**
     * It make a pseudosequence from a previous pseudosequence. Therefore, we
     * are making a projection in a previous projection and we have to take into
     * account the previous index of that projection to establish the ours.
     * @param timeShift The time shift of the new pseudosequence
     * @param pseudosequence The pseudosequence where we make another projection
     * @param itemsetIndex The itemset index where our new projection is starting
     * @param itemIndex The item index where our new projection is starting
     * @param firstItemset An index that indicates in which projection of the 
     * previous pseudoseuqence we are making the new pseudosequence
     */
    public PseudoSequence(long timeShift, PseudoSequence pseudosequence, int itemsetIndex, int itemIndex, int firstItemset) {
        //We point to the same original sequence
        this.sequence = pseudosequence.sequence;
        /*We define the new time shift, that is the timeshift given plus the
         * value that the given pseudosequence has. Then we add it to the list 
         * in its corresponding position
         */
        long newTimeShift = timeShift+pseudosequence.timeShift.get(firstItemset);
        this.timeShift.add(newTimeShift);
        /*
         * We establish the first itemset of the projection in absolute terms.
         * We have to add the given itemset index to the first itemset index
         * kept in the given pseudosequence
         */
        this.firstItemset.add(itemsetIndex + pseudosequence.firstItemset.get(firstItemset));
        //If our itemset index is the same that the given pseudosequence had
        if (this.firstItemset.get(0) == pseudosequence.firstItemset.get(firstItemset)) {
            //We compute our first item index from the previous one
            this.firstItem.add(itemIndex + pseudosequence.firstItem.get(firstItemset));
        } else {
            //Otherwise, our first item index is just the given parameter
            this.firstItem.add(itemIndex);
        }
    }

    /**
     * PseudoSequence made from a starndard sequence. Its purpose is make possible
     * the later projections
     * @param timeShift The original timeshift (Normally, it will be 0)
     * @param sequence The original standard sequence
     * @param itemsetIndex The itemset index where the projection it starts
     * @param itemIndex The item index where the projection it starts
     */
    public PseudoSequence(long timeShift, Sequence sequence, int itemsetIndex, int itemIndex) {
        this.timeShift.add(timeShift);
        this.sequence = sequence;
        this.firstItemset.add(itemsetIndex);
        this.firstItem.add(itemIndex);
    }

    /**
     * Method to make a projection in a pseudosequence already projected. 
     * In this way, for a same pseudosequence, we can have several 
     * projections. For example, let us think in a sequence A = (a) (a b) (a b c).
     * If we project by item a, we obtain three different projections:
     * 1)   (a b) (a b c)
     * 2)   (*b) (a b c)
     * 3)   (*b c)
     * That we do is to put all the three projections in the same pseudoseuqence,
     * and we refers each projection with an index. We call to every projection, 
     * projection point.
     * @param firstItemset The projection point of the given pseudosequence 
     * where we are making our new projection
     * @param timeShift The given timeshift for our projection
     * @param pseudosequence The given pseudosequence where we are making our 
     * projection
     * @param itemsetIndex  The itemset index where our projection it starts
     * @param itemIndex The item index where our projection it starts
     */
    public void addProjectionPoint(int firstItemset, long timeShift, PseudoSequence pseudosequence,int itemsetIndex, int itemIndex) {
        /*
         * We update the timeshift from the given pseudosequence time shift and, 
         * them we keep it
         */
        long newTimeShift = timeShift+pseudosequence.timeShift.get(firstItemset);
        this.timeShift.add(newTimeShift);
        /*
         * We establish the starting itemset index (for our projection), from the
         * previous starting itemset index
         */
        this.firstItemset.add(itemsetIndex + pseudosequence.firstItemset.get(firstItemset));
        //If our itemset index is the same index in the previous pseudosequence
        if (this.firstItemset.get(this.firstItemset.size()-1) == pseudosequence.firstItemset.get(firstItemset)) {
            //We compose the item index from the previous one
            this.firstItem.add(itemIndex + pseudosequence.firstItem.get(firstItemset));
        } else {
            //Otherwise we simply use the given item index
            this.firstItem.add(itemIndex);
        }
    }

    /**
     * It returns the first itemset of the projection, in absolute terms
     * @param index the ith projection in which we want to know the first itemset
     * @return the index
     */
    public int getFirstItemset(int index){
        return this.firstItemset.get(index);
    }

   /**
     * It returns the size for the current pseudosequence
     * @param i the ith projection in which we want to know the size
     * @return the size (int)
     */
    public int size(int i) {
        int size = sequence.size() - firstItemset.get(i);
        return size;
    }
    
    public int length(int firstItemset){
        int itemsToNotBeTakenIntoAccount=0;
        /* 
         * We count all the elements that appear before the first element from 
         * which we start the projected sequence
         */
        for(int i=0;i<this.firstItemset.get(firstItemset);i++){
            itemsToNotBeTakenIntoAccount+=sequence.get(i).size();
        }
        itemsToNotBeTakenIntoAccount+=this.firstItem.get(firstItemset);
        /* 
         * And we return the difference between the original sequence length 
         * and this counted value
         */
        return (sequence.length()-itemsToNotBeTakenIntoAccount);
    }

    /**
     * It returns the number of projections included in this pseudosequence
     * @return the number of projections
     */
    public int numberOfProjectionsIncluded() {
        return firstItemset.size();
    }

    /**
     * It returns the size of the itemset in a specified projection of the 
     * sequence
     * @param firstItemset the ith projection in which we are interested
     * @param index The ith itemset of the current pseudosequence
     * @return the size (int)
     */
    public int getSizeOfItemsetAt(int firstItemset, int index) {
        //we get the itemset size in the original standard sequence
        int size = sequence.getItemsets().get(index + this.firstItemset.get(firstItemset)).size();
        //And if that itemset if where the projection starts
        if (isFirstItemset(index)) {
            //We substract the starting index
            size -= firstItem.get(firstItemset);
        }
        return size;
    }

    /**
     * It returns the first item of the first itemset of the specified projection
     * @param firstItem the specified projection in which we are interested
     * @param itemsetIndex the itemset of the pseudosequence in which we are interested
     * @return the item
     */
    public int getBeginningOfItemset(int firstItem, int itemsetIndex){
        if(isFirstItemset(itemsetIndex)){
            return this.firstItem.get(firstItem);
        }
        return 0;
    }

    /**
     * It informs if a projection it starts in the midle of an itemset
     * @param firstItem The projection in which we are interested
     * @param itemsetIndex The itemset to study
     * @return true if yes, otherwise false
     */
    public boolean isPostfix(int firstItem, int itemsetIndex) {
        return isFirstItemset(itemsetIndex) && this.firstItem.get(firstItem) != 0;
    }

    /**
     * To know if an itemset is the first one
     * @param index the position of the itemset 
     * @return true if it is the first one
     */
    public boolean isFirstItemset(int index) {
        return index == 0;
    }

    /**
     * Get an item of an itemset 
     * @param firstItem The projection in which are interested
     * @param itemIndex The item index
     * @param itemsetIndex The itemset index
     * @return the item
     */
    public Item getItemAtInItemsetAt(int firstItem, int itemIndex, int itemsetIndex) {

        if (isFirstItemset(itemsetIndex)) {
            return getItemset(itemsetIndex, firstItem).get(itemIndex + this.firstItem.get(firstItem));
        } else {
            return getItemset(itemsetIndex, firstItem).get(itemIndex);
        }
    }

    /**
     * It returns the timestamp of an itemset in the current pseudosequence
     * @param itemsetIndex the itemset index that we want to know its timestamp
     * @param firstItemset The projection of the pseudosequence in which we are
     * interested
     * @return the timestamp
     */
    private long getTimeStamp(int itemsetIndex, int firstItemset) {
        return getItemset(itemsetIndex, firstItemset).getTimestamp() - timeShift.get(firstItemset);
    }

    /**
     * It obtains the original timestamp for that itemset
     * @param itemsetIndex the itemset index for the pseudosequence
     * @param firstItemset the projection in which we are interested
     * @return  the timestamp
     */
    public long getAbsoluteTimeStamp(int itemsetIndex, int firstItemset) {
        return getItemset(itemsetIndex, firstItemset).getTimestamp();
    }

    /**
     * It obtains the relative timestamp
     * @param itemsetIndex the itemset index
     * @param firstItemset the projection in which we are interested
     * @return the relative timestamp
     */
    public long getRelativeTimeStamp(int itemsetIndex, int firstItemset) {
        return getTimeStamp(itemsetIndex,firstItemset);
    }

    /**
     * It gets the itemset in the original sequence.
     * @param itemsetIndex the itemset index
     * @param firstitemset the projection in which we are interested
     * @return the itemset
     */
    public Itemset getItemset(int itemsetIndex, int firstitemset) {
        return sequence.get(itemsetIndex + firstItemset.get(firstitemset));
    }

    /**
     * Get the pseudosequence ID
     * @return the id of this pseudo sequence(int)
     */
    public int getId() {
        return sequence.getId();
    }

    /**
     * Get the string representation of the pseudosequence.
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();
        for (int k = 0; k < firstItemset.size(); k++) {
            for (int i = 0; i < size(k); i++) {
                r.append("{t=");
                r.append(getTimeStamp(i, k));
                r.append(", ");
                for (int j = 0; j < getSizeOfItemsetAt(k, i); j++) {
                    r.append(getItemAtInItemsetAt(k, j, i).toString());
                    if (isPostfix(k, i)) {
                        r.append('*');
                    }
                    r.append(' ');
                }
                r.append("}");
            }
            r.append("\n");
        }
        return r.toString();
    }

    /**
     * Get the index where an item appears in the specified itemset of the
     * specified projection of the current pseudosequence. If the item does not
     * appear, it returns -1.
     * @param firstItemset the first itemset
     * @param itemsetIndex the index of the itemset
     * @param item the item
     * @return  the index or -1 if it does not appear
     */
    public int indexOf(int firstItemset, int itemsetIndex, Item item) {
        //We get the itemset
        Itemset itemset = getItemset(itemsetIndex,firstItemset);
        //We get its starting point with respect to the original itemset
        int beginning = getBeginningOfItemset(firstItemset, itemsetIndex);
        //We search for the item
        List items=itemset.getItems();
        int i= Collections.binarySearch(items,item);
        //If the index exist, and it does not appear before the beginning
        if(i>=beginning) {
            //we return it, substracting the beinning of the itemset
            return i-beginning;
        }
        //Otherwise we return -1
        return -1;
    }

    /**
     * It gets the timeShift for a specified projection of the pseudosequence
     * @param firstItemset the projection in which we are interested
     * @return the time shift.
     */
    public long getTimeShift(int firstItemset) {
        return timeShift.get(firstItemset);
    }
}
