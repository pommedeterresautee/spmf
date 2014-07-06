package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;

/**
 * Inspired in SPMF. Implementation of a Idlist for SPADE and SPAM. This IdList
 * is based on a hash map of entries <Integer, List<Integer>>, and it makes a
 * correspondence between a sid, denoted by the Integer, with a the apperances
 * of the pattern in that sequence, denoted by the list of Integer. In that list
 * we will have one itemset timestamp where an appearance of the pattern can be
 * found, and is increasingly sorted in the itemset timestamps.
 *
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
public class IDListStandard_Map implements IDList {

    /**
     * The map where we keep the appearances of a pattern in a sequence. With an
     * integer we stand for a sequence id, whereas a list of itemsets correspond
     * to all the itemset timestamps where the pattern occurs
     */
    Map<Integer, List<Integer>> itemsetSequenceEntries;
    /**
     * A bitset to keep just the sequences where a pattern appears. Is the
     * bitset representation of the keyset of the map sequence_ItemsetEntries
     */
    BitSet sequences;

    /**
     * The standard constructor. It creates an empty IdList.
     */
    public IDListStandard_Map() {
        this.itemsetSequenceEntries = new HashMap<Integer, List<Integer>>();
        this.sequences = new BitSet();
    }

    /**
     * It creates an IdList from a map of <Integer,List<Integer>>
     *
     * @param itemsetSequenceEntries
     */
    public IDListStandard_Map(Map<Integer, List<Integer>> itemsetSequenceEntries) {
        this.itemsetSequenceEntries = itemsetSequenceEntries;
        this.sequences = new BitSet(itemsetSequenceEntries.size());
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
        //We create the result map of entries of list of itemset timestamps
        Map<Integer, List<Integer>> intersection = new HashMap<Integer, List<Integer>>(((IDListStandard_Map) idList).getSequenceItemsetEntries().size());
        //We create an empty bitset where we will keep the pattern appearances
        BitSet newSequences = new BitSet(idList.getSupport());
        //Cast in the argument IdList
        IDListStandard_Map idStandard = (IDListStandard_Map) idList;
        //And we get the map of entries of bitsets
        Map<Integer, List<Integer>> idListMap = idStandard.getSequenceItemsetEntries();
        Set<Map.Entry<Integer, List<Integer>>> entries = idListMap.entrySet();
        //For each entry of the given IdList
        for (Map.Entry<Integer, List<Integer>> entry : entries) {
            /*
             * We get the transactions that correspond with the sequence given
             * by the key of the current entry
             */
            List<Integer> transactionAppearancesInSequence = entry.getValue();
            /*
             * We create a new list of itemset timestamp where we keep the
             * result for this entry
             */
            List<Integer> transactionAppearances = null;
            int sid = entry.getKey();
            //If the flag is activated
            if (equals) {
                //We make an equal operation join for the current sequence sid
                transactionAppearances = equalOperation(sid, transactionAppearancesInSequence);
            } else {
                //otherwise, we make an after operation join for the current sequence sid
                transactionAppearances = laterOperation(sid, transactionAppearancesInSequence);
            }
            //If there is any result, we keep it
            if (transactionAppearances != null) {
                intersection.put(sid, transactionAppearances);
                newSequences.set(sid);
            }
        }
        //Finally, we return the new IdList and the sequence bitset associated with it
        IDListStandard_Map output = new IDListStandard_Map(intersection);
        output.sequences = newSequences;
        return output;
    }

    /**
     * It gets the map that codes the appearances of the pattern in this IdList
     *
     * @return the map
     */
    public Map<Integer, List<Integer>> getSequenceItemsetEntries() {
        return itemsetSequenceEntries;
    }

    /**
     * It executes a join operation under the after relation for a two sets of
     * appearances that correspond to a same sequence in two different patterns
     *
     * @param sid Sequence identifier of the sequence where we want to check if
     * it exists the pattern
     * @param transactionAppearancesInSequence Itemset timestamps of the
     * parameter Idlist
     * @return The new Entry for the new IdList
     */
    private List<Integer> laterOperation(Integer sid, List<Integer> transactionAppearancesInSequence) {
        //We get the itemset timestamps for the same sequence for the current IdList
        List<Integer> transactionAppearancesInSequenceOfMyIdList = itemsetSequenceEntries.get(sid);
        //If there is not any occurrence we end the join operation
        if (transactionAppearancesInSequenceOfMyIdList == null || transactionAppearancesInSequenceOfMyIdList.isEmpty()) {
            return null;
        }
        //Otherwise we create a new List of itemset where we keep the new entries
        List<Integer> result = new ArrayList<Integer>();

        int index = -1;
        /*
         * For all the timestamps of the itemset of the parameter Idlist that
         * appear after the first timestamp of the itemset of the current IdList
         */
        for (int i = 0; i < transactionAppearancesInSequence.size() && index < 0; i++) {
            int eid = transactionAppearancesInSequence.get(i);
            if (transactionAppearancesInSequenceOfMyIdList.get(0) < eid) {
                index = i;
            }
        }
        /*
         * We keep them in the new result list
         */
        if (index >= 0) {
            for (int i = index; i < transactionAppearancesInSequence.size(); i++) {
                result.add(transactionAppearancesInSequence.get(i));
            }
        }

        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    /**
     * It executes a join operation under the equal relation for a two sets of
     * appearances that correspond to a same sequence in two different patterns
     *
     * @param sid Sequence identifier of the sequence where we want to check if
     * it exists the pattern
     * @param transactionAppearancesInSequence Itemset timestamps of the
     * parameter Idlist
     * @return The new Entry for the new IdList
     */
    private List<Integer> equalOperation(Integer sid, List<Integer> transactionAppearancesInSequence) {
        //We get the itemsets for the same sequence for the current IdList
        List<Integer> transactionAppearancesInSequenceOfMyIdList = itemsetSequenceEntries.get(sid);
        //If there is not any occurrence we end the join operation
        if (transactionAppearancesInSequenceOfMyIdList == null || transactionAppearancesInSequenceOfMyIdList.isEmpty()) {
            return null;
        }
        //Otherwise we create a new List of itemset where we keep the new entries
        List<Integer> result = new ArrayList<Integer>();
        int beginningIndex = 0;

        /*
         * We explore the smaller list and we search in the greater one
         */
        List<Integer> listToExplore, listToSearch;
        if (transactionAppearancesInSequenceOfMyIdList.size() <= transactionAppearancesInSequence.size()) {
            listToExplore = transactionAppearancesInSequenceOfMyIdList;
            listToSearch = transactionAppearancesInSequence;
        } else {
            listToExplore = transactionAppearancesInSequence;
            listToSearch = transactionAppearancesInSequenceOfMyIdList;
        }
        //For each itemset timestamp in the list to explores
        for (Integer eid : listToExplore) {
            /*
             * For each itemset timestamp from the beginning index to the end of
             * the list to search
             */
            for (int i = beginningIndex; i < listToSearch.size(); i++) {
                //We make a comparison
                int comparison = listToSearch.get(i).compareTo(eid);
                /*
                 * If that comparison says that the element of the list to
                 * search is greater than or equal to eid
                 */
                if (comparison >= 0) {
                    /*
                     * If is equal to eid, we add it in the result list and
                     * update the beginning index (The lists from the IdList are
                     * sorted)
                     */
                    if (comparison == 0) {
                        result.add(eid);
                        beginningIndex = i + 1;
                    }
                    /*
                     * Nevertheless, we stop searching since we know that the
                     * rest of timestamp are all greater than eid (the timestamp
                     * occur later since the idlists are sorted)
                     */
                    break;
                }
            }
        }

        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    @Override
    public int getSupport() {
        return sequences.cardinality();
    }

    /**
     * It adds an appearance for the sequence and timestamp given as parameter
     * in the current IdList
     *
     * @param sequence Sequence identifier where the appearence occurs
     * @param timestamp Itemset timestamp where the appearance occurs
     */
    public void addAppearance(Integer sequence, Integer timestamp) {
        List<Integer> transactionAppearancesInSequenceOfMyIdList = itemsetSequenceEntries.get(sequence);
        if (transactionAppearancesInSequenceOfMyIdList == null) {
            transactionAppearancesInSequenceOfMyIdList = new ArrayList<Integer>();
        }
        if (!transactionAppearancesInSequenceOfMyIdList.contains(timestamp)) {
            transactionAppearancesInSequenceOfMyIdList.add(timestamp);
            itemsetSequenceEntries.put(sequence, transactionAppearancesInSequenceOfMyIdList);
            sequences.set(sequence);
        }
    }

    /**
     * It adds the appearances for the sequence and the timestamp list given as
     * parameter in the current IdList
     *
     * @param sid sequence identifier where the appearence occurs
     * @param itemsets Itemset timestamps where the appearances occur
     */
    public void addAppearancesInSequence(Integer sid, List<Integer> itemsets) {
        List<Integer> transactionAppearancesInSequenceOfMyIdList = itemsetSequenceEntries.get(sid);
        if (transactionAppearancesInSequenceOfMyIdList == null) {
            transactionAppearancesInSequenceOfMyIdList = itemsets;
        }
        itemsetSequenceEntries.put(sid, transactionAppearancesInSequenceOfMyIdList);
        sequences.set(sid);
    }

    /**
     * Get the string representation of this IdList
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        Set<Map.Entry<Integer, List<Integer>>> entries = itemsetSequenceEntries.entrySet();
        for (Map.Entry<Integer, List<Integer>> entry : entries) {
            result.append("\t").append(entry.getKey()).append(" {");
            List<Integer> eids = entry.getValue();
            for (Integer i : eids) {
                result.append(i).append(",");
            }
            result.deleteCharAt(result.length() - 1);
            result.append("}\n");
        }
        return result.toString();
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

    /**
     * It clears the attributes of this IdList
     */
    @Override
    public void clear() {
        itemsetSequenceEntries.clear();
        sequences.clear();
    }
}
