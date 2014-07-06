package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;

/**
 * Inspired in SPMF.
 * Implementation of a Idlist for SPADE and SPAM. This IdList is based on a hash
 * map of entries <Integer, Bitset>, and it makes a correspondence between a sid, 
 * denoted  by the Integer, with a the apperances of the pattern in that sequence,
 * denoted by the bitset. In that bitset we will have one bit set to 1 if in that
 * itemset, a appearance of the pattern can be found.
 * In order to make the join operation, we will do it entry by entry, for those 
 * entries shared by two sequences.
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
public class IDListBitmap implements IDList {

    /**
     * the default number of bit that we use for each sequence
     */
    final int BIT_PER_SECTION = 8;  
    /**
     * the map where we keep the appearances of a pattern in a sequence. 
     * With an integer we stand for a sequence id, whereas a bitset is a 
     * representation of an itemset.
     */
    Map<Integer, BitSet> sequence_ItemsetEntries;
    
    /**
     * A bitset to keep just the sequences where a pattern appears. Is the bitset
     * representation of the keyset of the map sequence_ItemsetEntries
     */
    BitSet sequences;

    /**
     * Standard Constructor. It creates an empty IdList
     */
    public IDListBitmap() {
        super();
        sequence_ItemsetEntries = new HashMap<Integer, BitSet>();
        sequences = new BitSet();
    }

    /**
     * It creates a IdList from a map of entries <Integer, Bitset>
     * @param sequenceItemsetEntries 
     */
    private IDListBitmap(Map<Integer, BitSet> sequenceItemsetEntries) {
        sequence_ItemsetEntries = sequenceItemsetEntries;
        sequences = new BitSet(sequenceItemsetEntries.size());
    }

    /**
     * It adds the appearance of the pattern in the itemset "tid" and sequence "sid"
     * @param sid The sequence identifier where the pattern appears
     * @param tid The itemset timestamp where the pattern appears
     */
    public void registerBit(int sid, int tid) {
        int bitIndex = tid;
        BitSet bitmap = sequence_ItemsetEntries.get(sid);
        if (bitmap == null) {
            bitmap = new BitSet(BIT_PER_SECTION);
            sequence_ItemsetEntries.put(sid, bitmap);
            sequences.set(sid);
        }
        bitmap.set(bitIndex);
    }

    /**
     * It adds the appearances of the pattern in the itemsets contained in "tids" and sequence "sid"
     * @param sid The sequence identifier wher the pattern appears
     * @param tids The set of itemset timestamps where the pattern appears
     */
    public void registerNBits(int sid, List<Integer> tids) {
        BitSet bitmap = sequence_ItemsetEntries.get(sid);
        if (bitmap == null) {
            bitmap = new BitSet(BIT_PER_SECTION);
            sequence_ItemsetEntries.put(sid, bitmap);
            sequences.set(sid);
        }
        for (Integer tid : tids) {
            int bitIndex = tid;
            bitmap.set(bitIndex, true);
        }
    }

    /**
     * It return the number of sequences where the IdList is active.
     * @return the number of sequences
     */
    @Override
    public int getSupport() {
        return sequences.cardinality();
    }

    /**
     * Get the string representation of this kind of IdList
     * @return  the string representation
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Integer sid : sequence_ItemsetEntries.keySet()) {
            BitSet bitmap = sequence_ItemsetEntries.get(sid);
            for (int bit = bitmap.nextSetBit(0); bit >= 0; bit = bitmap.nextSetBit(bit + 1)) {
                buffer.append("[sid=");
                buffer.append(sid);
                buffer.append(" tid=");
                buffer.append(bit);
                buffer.append("]");
            }
        }
        return buffer.toString();
    }

    /**
     * It return the intersection IdList that results from the current object and
     * the IdList given as an argument.
     * @param idList IdList with which we join the current IdList.
     * @param equals Flag indicating if we want a intersection for equal relation,
     * or, if it is false, an after relation.
     * @param minSupport Minimum relative support.
     * @return the resulting idlist
     */
    @Override
    public IDList join(IDList idList, boolean equals, int minSupport) {
        //We create the result map of entries of bitsets
        Map<Integer, BitSet> intersection = new HashMap<Integer, BitSet>(((IDListBitmap) idList).getSecuenceItemsetEntries().size());
        //We create an empty bitset where we will keep the pattern appearances
        BitSet newSequences = new BitSet(getSecuenceItemsetEntries().size());
        //Cast in the argument IdList
        IDListBitmap idStandard = (IDListBitmap) idList;
        //And we get the map of entries of bitsets
        Map<Integer, BitSet> idListMap = idStandard.getSecuenceItemsetEntries();
        Set<Map.Entry<Integer, BitSet>> entries = idListMap.entrySet();
        //If flag equals is activated
        if (equals) {
                //We execute a join for equal relation
                equalLoop(intersection, entries,newSequences);
        } else {
            //Otherwise we execute a join for an after relation
            laterLoop(intersection, entries,newSequences);
        }
        //We create the new IdList from the resulting map and sequences bitset
        IDListBitmap output = new IDListBitmap(intersection);
        output.sequences=newSequences;
        return output;
    }

    /**
     * Method to do the join operation under equal relation.
     * @param sequenceItemsetEntries Map where we put the new elements resulting
     * from the join method
     * @param entries Map with which we are going to join the current IdList.
     * @param sequences New bitset where we keep the sequences where the new 
     * IdList is active
     */
    private void equalLoop(Map<Integer, BitSet> sequenceItemsetEntries, Set<Map.Entry<Integer, BitSet>> entries,BitSet sequences) {
        //For each entry
        for (Map.Entry<Integer, BitSet> entry : entries) {
            //we get the bitset of the entry of the Idlist argument
            BitSet otherIdList = entry.getValue();
            //We get the  bitset for the same entry (sid) in the current IdList
            BitSet thisIdList = sequence_ItemsetEntries.get(entry.getKey());
            //If contains any value for that sid
            if (thisIdList != null) {
                BitSet equalResult;
                /* We make a join equal operation for that pair of bitsets that 
                 * represent the different appearances in sequence sid of the 
                 * pattern with which the IdList will be associated
                 */
                equalResult = equalOperation(thisIdList, otherIdList);
                //If there is any result
                if (equalResult != null) {
                    int sid = entry.getKey();
                    //We keep that result in the new map
                    sequenceItemsetEntries.put(sid, equalResult);
                    sequences.set(sid);
                }
            }
        }
    }

    /**
     * Method to do the join operation under after relation.
     * @param sequenceItemsetEntries Map where we put the new elements resulting
     * from the join method
     * @param entries Map with which we are going to join the current IdList.
     * @param sequences New bitset where we keep the sequences where the new 
     * IdList is active
     */
    private void laterLoop(Map<Integer, BitSet> sequenceItemsetEntries, Set<Map.Entry<Integer, BitSet>> entries, BitSet sequences) {
        //For each entry
        for (Map.Entry<Integer, BitSet> entry : entries) {
            //we get the bitset of the entry of the Idlist argument
            BitSet otherIdList = entry.getValue();
            //We get the  bitset for the same entry (sid) in the current IdList
            BitSet thisIdList = sequence_ItemsetEntries.get(entry.getKey());
            //If contains any value for that sid
            if (thisIdList != null) {
                BitSet greaterThanResult;
                /* We make a join after operation for that pair of bitsets that 
                 * represent the different appearances in sequence sid of the 
                 * pattern with which the IdList will be associated
                 */
                greaterThanResult = greaterThanOperation(thisIdList, otherIdList);                
                //If there is any result
                if (greaterThanResult != null) {
                    int sid = entry.getKey();
                    //We keep that result in the new map
                    sequenceItemsetEntries.put(sid, greaterThanResult);
                    sequences.set(sid);
                }
            }
        }
    }

    /**
     * Setter method to insert in the pattern given as parameter the set of 
     * sequence identifiers where the IdList appears, so the pattern does
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
     * It adds, for a particular sequence, all the apperarances given by the list
     * of itemsets 
     * @param sid Sequence id where the itemsets will be inserted
     * @param itemsets Set of itemsets to insert in a sequence
     */
    public void addAppearancesInSequence(Integer sid, List<Integer> itemsets) {
        registerNBits(sid, itemsets);
    }

    /**
     * Getter method for the map of entries
     * @return the map of entries <integer, bitset>
     */
    public Map<Integer, BitSet> getSecuenceItemsetEntries() {
        return sequence_ItemsetEntries;
    }

    /**
     * Set the map of entries
     * @param sequenceItemsetEntries the map of entries
     */
    public void setSequenceItemsetEntries(Map<Integer, BitSet> sequenceItemsetEntries) {
        this.sequence_ItemsetEntries = sequenceItemsetEntries;
    }

    /**
     * 
     * @param thisBitmap
     * @param otherBitmap
     * @param temporalDistance
     * @return 
     */
    private BitSet equalOperation(BitSet thisBitmap, BitSet otherBitmap, int temporalDistance) {
        if (thisBitmap != null) {
            BitSet result = (BitSet) thisBitmap.clone();
            result.and(shiftToLeft(otherBitmap, temporalDistance));
            if (result.cardinality() > 0) {
                return shiftToRight(result, temporalDistance);
            }
        }
        return null;
    }

    /**
     * It executes a join operation under the equal relation for a two sets of 
     * appearances that correspond to a same sequence in two different patterns
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
     * @param thisBitmap Set of appearances of the the current IdList
     * @param otherBitmap Set of appearances of the given IdList
     * @return The resulting bitmap
     */
    private BitSet greaterThanOperation(BitSet thisBitmap, BitSet otherBitmap) {        
        BitSet result = (BitSet) otherBitmap.clone();
        //If the bitmap exist for the associated sequence
        if (thisBitmap != null) {
            /* We get the first index where there is a bit set to 1 value, i.e., 
             * the index where the first appearance of the first pattern is
             */
            int index = thisBitmap.nextSetBit(0);

            /*
             * If the index is 0 or positive and is less than the index of 
             * the last item of the other bitmap
             */
            if (index >= 0 && index < (otherBitmap.length() - 1)) {
                /*
                 * The new resulting value is equal to the bitmap associated to 
                 * the second Idlist (otherBitmap), having set to 0 all the values
                 * that appear before or at the same position of the first activated
                 * index in thisBitmap
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

    private BitSet shiftToLeft(BitSet bitsetArg, int temporalDistance) {
        BitSet result = new BitSet(bitsetArg.length());
        for (int bitIndex = bitsetArg.nextSetBit(0); bitIndex >= 0; bitIndex = bitsetArg.nextSetBit(bitIndex + 1)) {
            int dif = bitIndex - temporalDistance;
            if (dif >= 0) {
                result.set(dif);
            }
        }
        return result;
    }

    private BitSet shiftToRight(BitSet bitsetArg, int temporalDistance) {
        BitSet result = new BitSet(bitsetArg.length());
        for (int bitIndex = bitsetArg.nextSetBit(0); bitIndex >= 0; bitIndex = bitsetArg.nextSetBit(bitIndex + 1)) {
                int dif = bitIndex + temporalDistance;
                result.set(dif);
            
        }
        return result;
    }
}
