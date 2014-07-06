package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;

/**
 * Inspired in SPMF. Implementation of a Idlist for SPADE and SPAM. This IdList
 * is based on a big bitmap and it codes all the sequences by means of a bitset
 * and a list of bitsets. In the first only set we keep the sequence identifiers
 * of all the sequences where the pattern appears. For each sequence we have an
 * itemset. Therefore, if we have a bitset with 512 bits, we also have a list of
 * 512 bitsets, where each bitset codes an itemset.
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
public class IDListFatBitmap implements IDList {

    final int BIT_PER_SEQUENCE = 512; // the number of bit that we use to code a database
    final int BIT_PER_ITEMSET = 64;  // the number of bit that we use for each sequence
    /**
     * Bitset that codes all the sequences where the pattern associated with
     * this bitmap appears.
     */
    private BitSet sequences;
    /**
     * List That codes all the itemsets of this bitmap. We have as many itemsets
     * as sequences exist
     */
    private List<BitSet> itemsetsOfSequences;
    /**
     * Support corresponding to the pattern associated to this IdList. It is
     * extracted by counting the number of occurrences that there are in
     * sequences attribute.
     */
    private int support;

    /**
     * Standard constructor for a bitmap.
     */
    public IDListFatBitmap() {
        super();
        sequences = new BitSet(BIT_PER_SEQUENCE);
        itemsetsOfSequences = new ArrayList<BitSet>(BIT_PER_SEQUENCE);
    }

    /**
     * Constructor from the set of sequences and a list of itemsets.
     *
     * @param sequences
     * @param itemsets
     */
    private IDListFatBitmap(BitSet sequences, List<BitSet> itemsets) {
        this.sequences = sequences;
        this.itemsetsOfSequences = (ArrayList<BitSet>) itemsets;
    }

    /**
     * It adds a appearance of the pattern in the sequence, denoted by sid, and
     * the itemset, denoted by tid
     *
     * @param sid The sequence identifier where the pattern appears
     * @param tid The itemset where the pattern appears
     */
    public void registerBit(int sid, int tid) {
        int bitIndex = tid;
        //Insert the sid in the bitset of sequences
        insertInSequence(sid);
        //Get the bitset associated to that sequence sid
        BitSet itemsetsFromSequence = itemsetsOfSequences.get(sid);
        if (itemsetsFromSequence == null) {
            itemsetsFromSequence = new BitSet(BIT_PER_ITEMSET);
            itemsetsOfSequences.set(sid, itemsetsFromSequence);
        }
        //and we set the appearance given by tid
        itemsetsFromSequence.set(bitIndex);
        //Updating the support
        this.support = sequences.cardinality();
    }

    /**
     * It adds all the appearances of the pattern in the sequence, denoted by
     * sid, and all the itemset apperances, denoted by tids
     *
     * @param sid The sequence identifier where the pattern appears
     * @param tids The itemsets where the pattern appears
     */
    public void registerNBits(int sid, List<Integer> tids) {
        //Insert the sid in the bitset of sequences
        insertInSequence(sid);
        //Updating the support
        this.support = sequences.cardinality();
        //Get the bitset associated to that sequence sid
        BitSet itemsetsFromSequence = itemsetsOfSequences.get(sid);
        if (itemsetsFromSequence == null) {
            itemsetsFromSequence = new BitSet(BIT_PER_ITEMSET);
            itemsetsOfSequences.set(sid, itemsetsFromSequence);
        }
        //and we set all the appearances given by tids
        for (Integer tid : tids) {
            int bitIndex = tid;
            itemsetsFromSequence.set(bitIndex);
        }
    }

    /**
     * It gets the number of sequences the IdList is active, so the pattern does
     * appear
     *
     * @return the number of sequences
     */
    @Override
    public int getSupport() {
        return support;
    }

    /**
     * Get the string representation of this IdList
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        /*
         * for (int i = secuencias.nextSetBit(0); i >= 0;
         * secuencias.nextSetBit(i + 1)) { int sid = i; BitSet bitmap =
         * itemsets.get(sid); for (int bit = bitmap.nextSetBit(0); bit >= 0; bit
         * = bitmap.nextSetBit(bit + 1)) { buffer.append("[sid=");
         * buffer.append(sid); buffer.append(" tid="); buffer.append(bit);
         * buffer.append("]"); }
        }
         */
        return buffer.toString();
    }

    /**
     * It return the intersection IdList that results from the current object
     * and the IdList given as an argument.
     *
     * @param idList IdList with which we join the current IdList.
     * @param equals Flag indicating if we want a intersection for equal
     * relation, or, if it is false, an after relation.
     * @param minSupport Minimum relative support.
     * @return the intersection
     */
    @Override
    public IDList join(IDList idList, boolean equals, int minSupport) {
        //We create a new fatBitmap to keep the result
        IDListFatBitmap result = new IDListFatBitmap();
        //We get the parameter idList
        IDListFatBitmap idStandard = (IDListFatBitmap) idList;
        //And we obtain its sequence bitset and the itemsets bitsets
        BitSet sequencesIdList = idStandard.sequences;
        List<BitSet> itemsetsIdList = idStandard.itemsetsOfSequences;
        //If the flag is activated
        if (equals) {
            //We make a join operation under the equal relation
            equalLoop(result, sequencesIdList, itemsetsIdList, minSupport);
        } else {
            //Otherwise we do it under the after operation
            laterLoop(result, sequencesIdList, itemsetsIdList, minSupport);
        }
        return result;
    }

    /**
     * Setter method to insert in the pattern given as parameter the set of
     * sequence identifiers where the IdList appears, so the pattern does
     *
     * @param pattern Pattern where we insert the sid list
     */
    @Override
    public void setAppearingSequences(Pattern pattern) {
        pattern.setAppearingIn(sequences);
    }

    @Override
    public void clear() {
    }

    /**
     * It adds, for a particular sequence, all the apperarances given by the
     * list of itemsets
     *
     * @param sequence Sequence id where the itemsets will be inserted
     * @param itemsets Set of itemsets to insert in a sequence
     */
    public void addAppearancesInSequence(Integer sequence, List<Integer> itemsets) {
        registerNBits(sequence, itemsets);
    }

    /**
     * It executes a join operation under the equal relation for a two sets of
     * appearances that correspond to a same sequence in two different patterns
     *
     * @param thisBitmap Set of appearances of the the current IdList
     * @param otherBitmap Set of appearances of the given IdList
     * @return The resulting bitmap
     */
    private BitSet equalOperation(BitSet thisBitmap, BitSet otherBitmap) {
        //If the bitmap exist for the associated sequence
        if (thisBitmap != null) {
            BitSet result = (BitSet) thisBitmap.clone();
            //We make an and operation
            result.and(otherBitmap);
            //And if there is a result, we return it
            if (result.cardinality() > 0) {
                return result;
            }
        }
        return null;
    }

    /**
     * It executes a join operation under the after relation for a two sets of
     * appearances that correspond to a same sequence in two different patterns
     *
     * @param thisBitmap Set of appearances of the the current IdList
     * @param otherBitmap Set of appearances of the given IdList
     * @return The resulting bitmap
     */
    private BitSet greaterThanOperation(BitSet thisBitmap, BitSet otherBitmap) {
        BitSet result = (BitSet) otherBitmap.clone();
        //If the bitmap exist for the associated sequence
        if (thisBitmap != null) {
            /*
             * We get the first index where there is a bit set to 1 value, i.e.,
             * the index where the first appearance of the first pattern is
             */
            int index = thisBitmap.nextSetBit(0);

            /*
             * If the index is 0 or positive and is less than the index of the
             * last item of the other bitmap
             */
            if (index >= 0 && index < (otherBitmap.length() - 1)) {
                /*
                 * The new resulting value is equal to the bitmap associated to
                 * the second Idlist (otherBitmap), having set to 0 all the
                 * values that appear before or at the same position of the
                 * first activated index in thisBitmap
                 */
                int newIndex = index + 1;
                result.clear(0, newIndex);
                //If there still are some appearances
                if (result.nextSetBit(newIndex) > 0) {
                    //We return the new bitmap
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Method to do the join operation under equal relation.
     *
     * @param newIdList Map where we put the new elements resulting from the
     * join method
     * @param sequencesFromIdList Sequence bitset with which we are going to
     * join the current IdList.
     * @param itemsetsFromIdList Itemsets bitset with which we are going to join
     * the current IdList.
     * @param minSupport Mininum relative support
     */
    private void equalLoop(IDListFatBitmap newIdList, BitSet sequencesFromIdList, List<BitSet> itemsetsFromIdList, int minSupport) {
        List<BitSet> itemsetIntersection = (ArrayList<BitSet>) newIdList.getItemsets();
        //We clone the sequence bitset of the current IdList
        BitSet sequencesIntersection = (BitSet) sequences.clone();
        /*
         * And we make an and operation with the sequence bitset of the other
         * IDlist in order to know the potential support
         */
        sequencesIntersection.and(sequencesFromIdList);
        //We fit the number of itemsets to the number of sequences that we have
        setSize(itemsetIntersection, sequencesIntersection.length());
        //Updating of support
        newIdList.setSupport(sequencesIntersection.cardinality());
        //If the new sequence bitset has a potential support at least as the minimum support
        if (newIdList.getSupport() >= minSupport) {
            newIdList.setSequences(sequencesIntersection);
            //For each sequence appearance
            for (int i = sequencesIntersection.nextSetBit(0); i >= 0; i = sequencesIntersection.nextSetBit(i + 1)) {
                /*
                 * we get the itemsets of both the current Idlist and the other
                 * with which we are joining
                 */
                BitSet otherItemset = itemsetsFromIdList.get(i);
                BitSet thisItemset = itemsetsOfSequences.get(i);
                //If the itemset for the current IdList is not null
                if (thisItemset != null) {
                    //We make an equal operation
                    BitSet equalResult = null;
                    equalResult = equalOperation(thisItemset, otherItemset);
                    if (equalResult != null) {//We keep it if the result exists
                        itemsetIntersection.set(i, equalResult);
                    } else {//otherwise we decrease the support
                        sequencesIntersection.clear(i);
                        newIdList.decreaseSupport();
                    }
                }
            }
        }
    }

    /**
     * Method to do the join operation under an after relation.
     *
     * @param newIdList Map where we put the new elements resulting from the
     * join method
     * @param sequencesFromIdList Sequence bitset with which we are going to
     * join the current IdList.
     * @param itemsetsFromIdList Itemsets bitset with which we are going to join
     * the current IdList.
     * @param minSupport Mininum relative support
     */
    private void laterLoop(IDListFatBitmap newIdList, BitSet sequencesFromIdList, List<BitSet> itemsetsFromIdList, int minSupport) {
        List<BitSet> itemsetIntersection = (ArrayList<BitSet>) newIdList.getItemsets();
        //We clone the sequence bitset of the current IdList
        BitSet sequenceIntersection = (BitSet) sequences.clone();
        /*
         * And we make an and operation with the sequence bitset of the other
         * IDlist in order to know the potential support
         */
        sequenceIntersection.and(sequencesFromIdList);
        //We fit the number of itemsets to the number of sequences that we have
        setSize(itemsetIntersection, sequenceIntersection.length());
        //Updating of support
        newIdList.setSupport(sequenceIntersection.cardinality());
        //If the new sequence bitset has a potential support at least as the minimum support
        if (newIdList.getSupport() >= minSupport) {
            newIdList.setSequences(sequenceIntersection);
            //For each sequence appearance
            for (int i = sequenceIntersection.nextSetBit(0); i >= 0; i = sequenceIntersection.nextSetBit(i + 1)) {
                /*
                 * we get the itemsets of both the current Idlist and the other
                 * with which we are joining
                 */
                BitSet otherItemset = itemsetsFromIdList.get(i);
                BitSet thisItemset = itemsetsOfSequences.get(i);
                //If the itemset for the current IdList is not null
                if (thisItemset != null) {
                    //We make an after operation
                    BitSet greaterThanResult = null;
                    greaterThanResult = greaterThanOperation(thisItemset, otherItemset);
                    if (greaterThanResult != null) {//We keep it if the result exists
                        itemsetIntersection.set(i, greaterThanResult);
                    } else {//otherwise we decrease the support
                        sequenceIntersection.clear(i);
                        newIdList.decreaseSupport();
                    }
                }
            }
        }
    }

    /**
     * It returns the list of sequences, meaning their itemsets, of the current
     * IdList
     *
     * @return
     */
    private List<BitSet> getItemsets() {
        return this.itemsetsOfSequences;
    }

    private void setSupport(int support) {
        this.support = support;
    }

    /**
     * It decreases the support associated with the IdList
     */
    private void decreaseSupport() {
        this.support--;
    }

    /**
     * It increases the support associated with the IdList
     */
    private void increaseSupport() {
        this.support++;
    }

    /**
     * It insert the sequence identifier given as parameter in the set of
     * sequences. If it did not exist before, we create a null itemset for it.
     *
     * @param sid
     */
    private void insertInSequence(int sid) {
        //We add the sequence
        sequences.set(sid);
        int currentSize = itemsetsOfSequences.size();
        /*
         * If the sequence inserted is greater than the greatest sequence
         * identifier that we had so far
         */
        int last = sid + 1;
        if (currentSize < last) {
            //We add null values in the itemset list until get the position pointed by sid
            while (currentSize < last) {
                itemsetsOfSequences.add(null);
                currentSize++;
            }
        }
    }

    private void setSequences(BitSet sequences) {
        this.sequences = sequences;
    }

    /**
     * It adjust the list of itemsets bitset to the same size given by the
     * length parameter
     *
     * @param list list of itemsets bitset to adjust
     * @param length size that the list should have
     */
    private void setSize(List<BitSet> list, int length) {
        //We get the difference
        int dif = list.size() - length;
        //If we have more element than it is necessary, we remove them
        if (dif > 0) {
            int index = list.size() - 1;
            for (int i = 0; i < dif; i++) {
                list.remove(index);
                index--;
            }
            //If, conversely, we have less elements, we add nulls buckets until get the given length
        } else if (dif < 0) {
            int amountOfNulls = (-1) * dif;
            for (int i = 0; i < amountOfNulls; i++) {
                list.add(null);
            }
        }
    }

    private BitSet equalOperation(BitSet thisItemset, BitSet otherItemset, int temporalDistance) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
