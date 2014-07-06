package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Pair;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.PseudoSequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.abstractions.Abstraction_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.patterns.Pattern;

/**
 * This class is the implementation of a creator of a qualitative abstraction.
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
public class AbstractionCreator_Qualitative extends AbstractionCreator {

    /**
     * Static reference to make this class singleton
     */
    private static AbstractionCreator_Qualitative instance = null;
    
    private AbstractionCreator_Qualitative() {
    }
    
    /**
     * Get the static reference of this singleton class
     *
     * @return the static instance
     */
    public static AbstractionCreator_Qualitative getInstance() {
        if (instance == null) {
            instance = new AbstractionCreator_Qualitative();
        }
        return instance;
    }
    
    /**
     * It creates a default abstraction. The abstraction is established to false
     * @return the created abstraction
     */
    @Override
    public Abstraction_Generic CreateDefaultAbstraction() {
        return Abstraction_Qualitative.crear(false);
    }

    /**
     * It creates a relation with the given parameter.
     * @param equalRelation The boolean indicatin if the item has an equal 
     * relation with the previous item in the pattern
     * @return the created relation
     */
    public Abstraction_Generic createAbstraction(boolean equalRelation) {
        return Abstraction_Qualitative.crear(equalRelation);
    }

    /**
     * It adds a Pair object to one list when we keep the sequences counted for
     * that pair. If the pair has not been previously kept, we keep the sequenceID
     * @param pairMap a map of Pair object
     * @param alreadyCountedForSequenceID the set of sequence IDs that have been already counted
     * @param id an ID
     * @param item the item
     * @param postfix indicates if it is the case of a postfix (an itemset cut at left)
     */
    private void addPair(Map<Pair, Pair> pairMap, Set<Pair> alreadyCountedForSequenceID, int id, Item item, boolean postfix) {
        /*
         * We create a new Pair object from the given item and the postfix flag
         */
        Pair pair = new Pair(postfix, ItemAbstractionPairCreator.getInstance().getItemAbstractionPair(item, createAbstraction(postfix)));
        //We obtain the pair that was previously managed
        Pair oldPair = pairMap.get(pair);
        //And if this sequence was not already used for this pair
        if (alreadyCountedForSequenceID.add(pair)) {
            //we keep the new pair if if did not appear in the map
            if (oldPair == null) {
                pairMap.put(pair, pair);
            } else {
                pair = oldPair;
            }
            // we keep the sequence id
            pair.getSequencesID().set(id);
        }
    }

    /**
     * Method to find all frequent items in a context (database).
     * This is for k> 1.
     * @param sequences the list of pseudosequences from the database
     * @return a set of pair containing the items and their support
     */
    @Override
    public Set<Pair> findAllFrequentPairs(List<PseudoSequence> sequences) {
        // we will scan the database and store the cumulative support of each pair in a map.
        Map<Pair, Pair> pairMap = new HashMap<Pair, Pair>();
        Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>();

        for (PseudoSequence sequence : sequences) {
            // if the sequence does not have the same id, we clear the map.
            alreadyCountedForSequenceID.clear();

            loop1:
            for (int k = 0; k < sequence.numberOfProjectionsIncluded(); k++) {
                for (int i = 0; i < sequence.size(k); i++) {
                    //If we are after the first projection and after the first itemset of the pseudosequence
                    if (k > 0 && i > 0) {
                        //we continue to the next projection
                        continue loop1;
                    }
                    //we get the original itemset
                    Itemset itemset = sequence.getItemset(i, k);
                    //We obtain the beginning of that itemset for our projection
                    int beginning = sequence.getBeginningOfItemset(k, i);
                    /*And for each item from the beginning we add a new Pair in
                     *order to find the frequent items in the projection*
                     */
                    for (int j = beginning; j < itemset.size(); j++) {
                        Item item = itemset.get(j);
                        boolean postfix = sequence.isPostfix(k, i);
                        addPair(pairMap, alreadyCountedForSequenceID, sequence.getId(), item, postfix);
                    }
                }
            }
        }
        //We sort the set of keys
        ArrayList<Pair> sortedSet = new ArrayList<Pair>(pairMap.keySet());
        Collections.sort(sortedSet);
        Set s=new HashSet<Pair>();
        s.addAll(sortedSet);
        return s;
    }

    /**
     * Convert a Map<Item, BitSet> to  Map<Item, Set<Abstraction_Generic>>.
     * @param sequence the sequence
     * @param frequentItems a map of frequent item and their corresponding bitsets.
     * @return the resulting map
     */
    @Override
    public Map<Item, Set<Abstraction_Generic>> createAbstractions(Sequence sequence, Map<Item, BitSet> frequentItems) {
        return new HashMap<Item, Set<Abstraction_Generic>>();
    }

    /**
     * It creates an abstraction from a prefix and it is the same abstraction that is received as parameter
     * @param prefix a prefix
     * @param abstraccion the abstraction
     * @return the abstraction
     */
    @Override
    public Abstraction_Generic createAbstractionFromAPrefix(Pattern prefix, Abstraction_Generic abstraccion) {
        return abstraccion;
    }

    /**
     * Method that check if for the two patterns given as parameters, the
     * shortest one is a subpattern of the longest one
     * @param shorter The pattern which we check if is a subpattern of another
     * longer than it
     * @param larger Pattern which we want to check if another pattern is 
     * subpattern of itself
     * @param index index that indicates which position we have to take into account
     * @param positions List of positions of the appearances of the elements of
     * the shorter pattern in the longer one
     * @return true if the condition is met
     */
    @Override
    public boolean isSubpattern(Pattern shorter, Pattern larger, int index, List<Integer> positions) {
        //We get the pair indicated by index
        ItemAbstractionPair pair = shorter.getIthElement(index);
        Item itemPair = pair.getItem();
        Abstraction_Generic absPair = pair.getAbstraction();

        //And we also get the abstraction that was in the index-1 position
        Abstraction_Generic previousAbs = index > 0 ? shorter.getIthElement(index - 1).getAbstraction() : null;
        //Flag in order to cancel the search
        boolean cancelled = false;
        Integer pos = null;
        /* While the item index pointed out by the position is less than the 
         * size of the largest pattern
         */
        while (positions.get(index) < larger.size()) {
            /* 
             * We search for the item of the shorter pattern pointed by index in 
             * the longer one
             */
            if (index == 0) {
                pos = searchForFirstAppearance(larger, positions.get(index), itemPair);
            } else {
                pos = findItemPositionInPattern(larger, itemPair, absPair, previousAbs, positions.get(index), positions.get(index - 1));
            }
            //If we found any position
            if (pos != null) {
                //We set it in the array of positions
                positions.set(index, pos);

                //if we are not in the last element of the shorter pattern
                if (index + 1 < shorter.size()) {
                    //We create a new position that is just one position after
                    Integer newPos = increasePosition(positions.get(index));
                    //And we initialize the next index position to that new position
                    positions.set(index + 1, newPos);
                    /* And we make a recursive call to go on checking if shorter
                     * is a subpattern of longer
                     */
                    boolean output = isSubpattern(shorter, larger, index + 1, positions);
                    //If we have found a matching between both patterns
                    if (output) {
                        //we return a true answer
                        return true;
                    }
                } else {//If, conversely, we are in the last element of the shorter pattern
                    /* 
                     * We have already found a matching between shorter and 
                     * longer and we conclude that one is a subpattern of the 
                     * other one
                     */
                    return true;
                }
            } else {//If conversely, we did not find any position for the current index
                //If we are not in the first element of the pattern
                if (index > 0) {
                    /* We increase the itemset position of the previous index in 
                     * order to find other matching elements
                     */
                    int newPos = increaseItemset(larger, positions.get(index - 1));
                    //And we update that position
                    positions.set(index - 1, newPos);
                }
                //We set to to true the flag that indicates the end of the method
                cancelled = true;
                /* 
                 * And break the loop in order to go back and try to find other 
                 * matching elements that makes the subsequence possible
                 */
                break;
            }
        }
        /* If we are finish the loop and not by breaking it, and we are not looking
         * for the first element of the shorter pattern
         */
        if (index > 0 && !cancelled) {
            /* We increase the itemset position of the previous index in order 
             * to find other matching elements
             */
            int newPos = increaseItemset(larger, positions.get(index - 1));
            //And we update that position
            positions.set(index - 1, newPos);
        }
        /* 
         * We return a false value, indicating that we cannot reach a matching 
         * with the current choices of elements in the longer pattern
         */
        return false;
    }

    /**
     * Method that search the first appearance of an item (given as parameter)
     * in a pattern, starting from a beginning index
     * @param p Pattern where we search for an item
     * @param beginning Index from which we start to search from the item
     * @param itemPair Item to search for
     * @return The item position where we found the item, or null if this
     * does not appear
     */
    public Integer searchForFirstAppearance(Pattern p, Integer beginning, Item itemPair) {
        for (int i = beginning; i < p.size(); i++) {
            Item currentItem = p.getIthElement(i).getItem();
            if (currentItem.equals(itemPair)) {
                return i;
            }
        }
        return null;
    }

    /**
     * It searches for a position in the pattern given as parameter where
     * an item, also given as a parameter, appears
     * @param p Pattern where we are going to search for
     * @param itemPair Item to search for
     * @param currentAbs Abstraction of the current element of the pattern 
     * where the item appeared
     * @param previousAbs Astraction of the previous element of the pattern 
     * where the item appeared
     * @param currentPosition Position for the current element
     * @param previousPosition Position of the previous element
     * @return the position
     */
    public Integer findItemPositionInPattern(Pattern p, Item itemPair, Abstraction_Generic currentAbs, Abstraction_Generic previousAbs, Integer currentPosition, Integer previousPosition) {
        Abstraction_Qualitative abs = (Abstraction_Qualitative) currentAbs;
        Integer pos = null;
        //If the current Abstraction has an equal relation with the previous pair
        if (abs.hasEqualRelation()) {
            //We search for the item in the same itemset where the previous item appeared
            pos = searchForInTheSameItemset(p, itemPair, currentPosition);
        } else {//Otherwise
            //We start keeping the currentPosition
            int positionToSearchFor = currentPosition;
            /* 
             * If the positions of both the current item and the previous one 
             * are not in different itemsets
             */
            if (!areInDifferentItemsets(p, previousPosition, currentPosition)) {
                /*
                 * We increase the position until we get the first element that 
                 * appear in another itemset
                 */
                positionToSearchFor = increaseItemset(p, currentPosition);
            }
            pos = searchForFirstAppearance(p, positionToSearchFor, itemPair);
        }
        return pos;
    }

    /**
     * It increase the position of a given position
     * @param beginning the position to be increased
     * @return  the position + 1
     */
    public Integer increasePosition(Integer beginning) {
        return beginning + 1;
    }

    /**
     * Increase a position to the first element position where it starts 
     * another itemset
     * @param p Pattern in which we search for the beginning of another itemset
     * @param beginning Index from which we start to search for
     * @return The item index where a new Itemset starts
     */
    public int increaseItemset(Pattern p, Integer beginning) {
        //For all the elements appearing after beginning index
        for (int i = beginning + 1; i < p.size(); i++) {
            ItemAbstractionPair currentPair = p.getIthElement(i);
            Abstraction_Qualitative qualitativeAbs = (Abstraction_Qualitative) currentPair.getAbstraction();
            //If the relation is not an equal relation, then we have changed of itemset
            if (!qualitativeAbs.hasEqualRelation()) {
                //And return the index
                return i;
            }
        }
        /* 
         * If we have got this point that means that we were in the last itemset
         * of the pattern and, therefore, we return the size of the pattern, 
         * since there can not be any index bigger than this value
         */
        return p.size();
    }

    /**
     * Search for an item in the same itemset that the previous one appeared
     * @param pattern Pattern where we are goin to search for the item
     * @param itemPair Item to search for
     * @param beginning Index from which we are going to start to search for
     * @return the index where the item appears, or null if this index does not
     * exist
     */
    private Integer searchForInTheSameItemset(Pattern pattern, Item itemPair, Integer beginning) {
        //From the beginning index and on
        for (int i = beginning; i < pattern.size(); i++) {
            ItemAbstractionPair currentPair = pattern.getIthElement(i);
            Abstraction_Qualitative qualitativeAbstraction = (Abstraction_Qualitative) currentPair.getAbstraction();
            //If the item has not an equal relation
            if (!qualitativeAbstraction.hasEqualRelation()) {
                /*
                 * We have finished without finding the item, since we have 
                 * already change of itemset
                 */
                return null;
            } else {
                /*
                 * If, conversely, there is an equal relation, we check if this 
                 * item is equal to which we are searching for
                 */
                if (currentPair.getItem().equals(itemPair)) {
                    //In that case we return the index position
                    return i;
                }
            }
        }
        return null;
    }

    /**
     * Method that informs if for a pattern, two positions correspond 
     * to a same itemset or not
     * @param pattern Pattern in which we check the two positions
     * @param p1 First position
     * @param p2 Second position
     * @return True if they are in different itemsets, False otherwise
     */
    private boolean areInDifferentItemsets(Pattern pattern, Integer p1, Integer p2) {
        //For all the elements between positions p1 and p2
        for (int i = p1+1; i <= p2 && i < pattern.size(); i++) {
            ItemAbstractionPair currentPair = pattern.getIthElement(i);
            Abstraction_Qualitative qualitativeAbs = (Abstraction_Qualitative) currentPair.getAbstraction();
            /*
             * If the ith element does not have an equal relation, we conclude 
             * that p1 and p2 are not in the same itemset and we can finish
             */
            if(!qualitativeAbs.hasEqualRelation())
                return true;
        }
        //If we get this point that means p1 and p2 are in the same itemset
        return false;
    }
}
