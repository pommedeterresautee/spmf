/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.CandidateInSequenceFinder;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.Abstraction_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.PatternCreator;

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
public class AbstractionCreator_Qualitative implements AbstractionCreator {

    /**
     * Static reference to make this class singleton
     */
    private static AbstractionCreator_Qualitative instance = null;

    private AbstractionCreator_Qualitative() {
    }

    /**
     * Get the static instance of this singleton class
     *
     * @return the instance
     */
    public static AbstractionCreator_Qualitative getInstance() {
        if (instance == null) {
            instance = new AbstractionCreator_Qualitative();
        }
        return instance;
    }

    /**
     * It creates the default abstraction. Defined to false, for this
     * implementation.
     *
     * @return the abstraction
     */
    @Override
    public Abstraction_Generic CreateDefaultAbstraction() {
        return Abstraction_Qualitative.create(false);
    }

    /**
     * It creates the abstraction such as is given by the argument
     *
     * @param appearingInTheSameItemset the abstraction value
     * @return the abstraction
     */
    public Abstraction_Generic createAbstraction(boolean appearingInTheSameItemset) {
        return Abstraction_Qualitative.create(appearingInTheSameItemset);
    }

    @Override
    public List<Pattern> createSize2Sequences(List<Sequence> sequences) {
        Map<Pattern, Pattern> totalMap = new HashMap<Pattern, Pattern>();
        List<Pattern> output = new LinkedList<Pattern>();
        for (Sequence s : sequences) {
            List<Itemset> itemsets = s.getItemsets();
            //For each sequence itemset
            for (int i = 0; i < itemsets.size(); i++) {
                Itemset currentItemset = itemsets.get(i);
                //We get each one of the items appearing there
                for (int j = 0; j < currentItemset.size(); j++) {
                    Item item = currentItemset.get(j);

                    ItemAbstractionPair pair1 = new ItemAbstractionPair(item, CreateDefaultAbstraction());
                    //With all of the rest of items in the itemset we make 2-itemsets extensions
                    for (int k = j + 1; k < currentItemset.size(); k++) {
                        Item item2 = currentItemset.get(k);
                        ItemAbstractionPair pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(true));
                        updateAppeareanceSet(totalMap, pair1, pair2, s.getId());
                    }
                    //And with all the items that are in later itemsets we make 2-sequence extensions
                    for (int k = i + 1; k < itemsets.size(); k++) {
                        Itemset nextItemset = itemsets.get(k);
                        for (int n = 0; n < nextItemset.size(); n++) {
                            Item item2 = nextItemset.get(n);
                            ItemAbstractionPair pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(false));
                            updateAppeareanceSet(totalMap, pair1, pair2, s.getId());
                        }
                    }
                }
            }
        }
        output.addAll(totalMap.values());
        Collections.sort(output);
        return output;
    }

    public void updateAppeareanceSet(Map<Pattern, Pattern> totalMap, ItemAbstractionPair pair1, ItemAbstractionPair pair2, int seqId) {
        PatternCreator patternCreator = PatternCreator.getInstance();
        List<ItemAbstractionPair> elementsPatternSize2 = new ArrayList<ItemAbstractionPair>(2);
        elementsPatternSize2.add(pair1);
        elementsPatternSize2.add(pair2);
        Pattern newPattern = patternCreator.createPattern(elementsPatternSize2);

        Pattern existingPattern = totalMap.get(newPattern);
        if (existingPattern == null) {
            existingPattern = newPattern;
            totalMap.put(newPattern, newPattern);
        }
        existingPattern.addAppearance(seqId);
    }

    /**
     * It returns the subpattern obtained by removing an item 
     *  at a given index in a pattern.
     *
     * @param extension The pattern on which we will get a subpattern
     * @param index the element to remove from the given pattern
     * @return the resulting subpattern
     */
    @Override
    public Pattern getSubpattern(Pattern extension, int index) {
        ItemAbstractionPairCreator pairCreator = ItemAbstractionPairCreator.getInstance();
        PatternCreator patternCreator = PatternCreator.getInstance();
        List<ItemAbstractionPair> subpatternElements = new ArrayList<ItemAbstractionPair>(extension.size() - 1);
        Abstraction_Generic abstraction = null;
        int nextIndex = index + 1;
        //For each element in the pattern
        for (int i = 0; i < extension.size(); i++) {
            //we copy it if is not in the given index argument
            if (i != index) {
                //If is the element that appears after our index
                if (i == nextIndex) {
                    //If we do no have any value in "abstraction" it means that it was not prepared before. In that case we take the abstraction appearing in the ith-element
                    if (abstraction == null) {
                        abstraction = extension.getIthElement(i).getAbstraction();
                    }
                    //Otherwise, we use the abstraction that we had previously prepared
                    subpatternElements.add(pairCreator.getItemAbstractionPair(extension.getIthElement(i).getItem(), abstraction));
                } else {
                    subpatternElements.add(extension.getIthElement(i));
                }
                //If we remove it, we check how are the temporal relations when we do it
            } else {
                //If it is the first element in the pattern, we establish the default abstraction for the next element
                if (index == 0) {
                    abstraction = CreateDefaultAbstraction();
                } else {
                    //If it is not the first element, we check if it appears in a itemset later than its predecessor
                    Abstraction_Qualitative abstractionOfRemovedElement = (Abstraction_Qualitative) extension.getIthElement(i).getAbstraction();
                    //If that is the case, we prepare the abstraction for its sucessor
                    if (!abstractionOfRemovedElement.hasEqualRelation()) {
                        abstraction = createAbstraction(false);
                    }
                }
            }
        }
        return patternCreator.createPattern(subpatternElements);
    }

    @Override
    public List<Pattern> createSize2Sequences(Map<Integer, Map<Item, List<Integer>>> bbdd, Map<Item, Pattern> frequentItems) {
        Map<Pattern, Pattern> totalMap = new HashMap<Pattern, Pattern>();
        List<Pattern> output = new LinkedList<Pattern>();
        for (Entry<Integer, Map<Item, List<Integer>>> seq : bbdd.entrySet()) {
            Integer sequenceId = seq.getKey();
            List<Entry<Item, List<Integer>>> itemItemsetsAssociations = new ArrayList(seq.getValue().entrySet());
            //For each sequence itemset
            for (int i = 0; i < itemItemsetsAssociations.size(); i++) {
                Entry<Item, List<Integer>> currentEntry1 = itemItemsetsAssociations.get(i);
                Item item1 = currentEntry1.getKey();
                List<Integer> appearances1 = currentEntry1.getValue();
                if (!isFrequent(item1, frequentItems)) {
                    continue;
                }

                for (int m = 0; m < appearances1.size(); m++) {
                    int appearanceItem1 = appearances1.get(m);

                    ItemAbstractionPair pair1 = new ItemAbstractionPair(item1, CreateDefaultAbstraction());

                    for (int j = 0; j < itemItemsetsAssociations.size(); j++) {
                        Entry<Item, List<Integer>> currentEntry2 = itemItemsetsAssociations.get(j);
                        Item item2 = currentEntry2.getKey();
                        List<Integer> appearances2 = currentEntry2.getValue();
                        if (!isFrequent(item2, frequentItems)) {
                            continue;
                        }
                        for (int k = 0; k < appearances2.size(); k++) {
                            int appearanceItem2 = appearances2.get(k);
                            ItemAbstractionPair pair2 = null;
                            if (appearanceItem2 == appearanceItem1) {
                                if (-item2.compareTo(item1) == 1) {
                                    pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(true));
                                }
                            } else if (appearanceItem2 > appearanceItem1) {
                                pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(false));
                            }
                            if (pair2 != null) {
                                updateAppeareanceSet(totalMap, pair1, pair2, sequenceId);
                            }
                        }
                    }
                }
            }
        }
        output.addAll(totalMap.values());
        Collections.sort(output);
        return output;
    }

    private boolean isFrequent(Item item1, Map<Item, Pattern> itemsfrecuentes) {
        return (itemsfrecuentes.get(item1)) != null;
    }

    @Override
    public void clear() {
        instance = null;
    }

    public static void sclear() {
        instance = null;
    }

    /**
     * Method that creates an abstraction depending of two time values. The
     * abstraction will be true if both times are the same, otherwise it will be
     * false. The two time values usually comes from comparing the time of two
     * items.
     *
     * @param currentTime  the current time
     * @param previousTime the previous time
     * @return the abstraction
     */
    public Abstraction_Generic createAbstraction(long currentTime, long previousTime) {
        boolean inTheSameItemset = false;
        if (currentTime == previousTime) {
            inTheSameItemset = true;
        }
        Abstraction_Qualitative abstraction = Abstraction_Qualitative.create(inTheSameItemset);
        return abstraction;
    }

    /**
     * Method that finds the position that an item has within a concrete
     * sequence. The position is returned in a duple <itemset index, item
     * index>. The method used to find the item it depends on how the relations,
     * between the current item and the previous one, are
     *
     * @param sequence The sequence where we want to find the item
     * @param itemPair Item to find
     * @param absPair abstraction of the item to find
     * @param previousAbs Previous abstraction in the pattern of this item
     * @param itemsetIndex Itemset index that the current item has
     * @param itemIndex Item index that the current item has.
     * @param previousItemsetIndex Itemset index of the previous item in the
     * pattern
     * @param previousItemIndex Item index of the previous item in the pattern
     * @return The position where the item is
     */
    public int[] findPositionOfItemInSequence(Sequence sequence, Item itemPair, Abstraction_Generic absPair, Abstraction_Generic previousAbs, int itemsetIndex, int itemIndex, int previousItemsetIndex, int previousItemIndex) {
        Abstraction_Qualitative abs = (Abstraction_Qualitative) absPair;
        int[] pos = null;
        //If our item has an equal relation
        if (abs.hasEqualRelation()) {
            //If both itemset indices, for the current and the previous ones, are the same
            if (itemsetIndex == previousItemsetIndex) {
                //We search for the item in the same itemset
                pos = sequence.SearchForItemAtTheSameItemset(itemPair, itemsetIndex, itemIndex);
            }
        } else {//If, conversely, our item has not an equal relation
            int itemsetIndexToSearchFor = itemsetIndex;
            int itemIndexToSearchFor = itemIndex;
            //If the current itemset index and the previous one point out to the same position
            if (itemsetIndex == previousItemsetIndex) {
                //We make the current itemset index to point to the next itemset, starting from the first item index
                itemsetIndexToSearchFor++;
                itemIndexToSearchFor = 0;
            }
            //And, finally, we look for the item in all the itemsets that appear later than the original itemset index
            //pos = sequence.searchForAnItemInLaterItemset(itemPair, itemsetIndexToSearchFor, itemIndexToSearchFor);
            pos = sequence.searchForTheFirstAppearance(itemsetIndexToSearchFor, itemIndexToSearchFor, itemPair);
        }
        return pos;
    }

    /**
     * Method that generates a candidate from two given patterns. if all the
     * elements of pattern1, except the first one, are the same as all the
     * elements of pattern2, except the last one. For example, if pattern1= a <
     * (b c) < d, and pattern2=(b c) < d < e, we are interested in checking if
     * the subpattern of pattern1, obtaining by suppressing the first item, is
     * equal to the subpattern of pattern2 that is obtained by removing its last
     * item. I.e., 
     * p1 = a < (b c) < d 
     * p2 = (b c) < d < e 
     * and we want two know if the central part of both pattern is the same. 
     * If is not, we set different to true.
     *

     *
     * @param creator
     * @param pattern1 The pattern that plays the role of prefix
     * @param pattern2 The pattern that plays the rol of suffix
     * @param minSupport The minimum relative support
     * @return The candidate generated from the two patterns
     */
    public Pattern generateCandidates(AbstractionCreator creator, Pattern pattern1, Pattern pattern2, double minSupport) {
        // Flag to know if the central part of the pattern is different
        boolean different = false;
        List<ItemAbstractionPair> elements1 = pattern1.getElements();
        List<ItemAbstractionPair> elements2 = pattern2.getElements();
        for (int i = 0; i < (elements1.size() - 1) && !different; i++) {
            ItemAbstractionPair pair1 = elements1.get(i + 1);
            ItemAbstractionPair pair2 = elements2.get(i);
            if (i == 0) {
                if (!pair1.getItem().equals(pair2.getItem())) {
                    different = true;
                }
            } else {
                if (!pair1.equals(pair2)) {
                    different = true;
                }
            }
        }
        if (different) {//If we cannot compose any candidate we return null
            return null;
        } else {/*otherwise, we first check if the junction of their appearing 
         * sets still has more appearances than the minimum relative support is
         */
            BitSet intersection = (BitSet) pattern1.getAppearingIn().clone();
            intersection.and(pattern2.getAppearingIn());
            //If we have more appearences than the minSupport is
            if (intersection.cardinality() >= minSupport) {
                //the candidate can be frequent, so we keep it
                Pattern newPattern = pattern1.clonePattern();
                newPattern.add(pattern2.getLastElement());
                return newPattern;
            } else {//Otherwise, it never could be frequent and we can remove it
                return null;
            }
        }
    }

    /**
     * Method to find a candidate in a sequence. It is an intermedium call in 
     * order to separate the implementation and to be able to define different
     * ways of searching.
     * @param finder finder of candidates
     * @param candidate The candidate to find
     * @param sequence The sequence where we want to try finding the candidate
     * @param k The level in which we are, i.e. the candidate length
     * @param i the candidate item in which we start
     * @param position The position list for all the candiate elements
     */
    public void isCandidateInSequence(CandidateInSequenceFinder finder, Pattern candidate, Sequence sequence, int k, int i, List<int[]> position) {
        finder.isCandidatePresentInTheSequence_qualitative(candidate, sequence, k, 0, position);
    }

    public List<Pattern> generateSize2Candidates(AbstractionCreator creator, Pattern pat1, Pattern pat2) {
        List<Pattern> output = new LinkedList<Pattern>();
        PatternCreator patternCreator = PatternCreator.getInstance();
        ItemAbstractionPairCreator pairCreator = ItemAbstractionPairCreator.getInstance();

        ItemAbstractionPair elementFromPattern1 = pat1.getIthElement(0);
        ItemAbstractionPair elementFromPattern2 = pat2.getIthElement(0);

        List<ItemAbstractionPair> elementsOfNewPattern1 = new ArrayList<ItemAbstractionPair>(2);
        elementsOfNewPattern1.add(elementFromPattern1);
        elementsOfNewPattern1.add(pairCreator.getItemAbstractionPair(elementFromPattern2.getItem(), Abstraction_Qualitative.create(false)));
        Pattern newPattern1 = patternCreator.createPattern(elementsOfNewPattern1);
        output.add(newPattern1);

        if (!elementFromPattern1.equals(elementFromPattern2)) {
            List<ItemAbstractionPair> elementsOfNewPattern2 = new ArrayList<ItemAbstractionPair>(2);
            elementsOfNewPattern2.add(elementFromPattern2);
            elementsOfNewPattern2.add(pairCreator.getItemAbstractionPair(elementFromPattern1.getItem(), Abstraction_Qualitative.create(false)));
            Pattern newPattern2 = patternCreator.createPattern(elementsOfNewPattern2);
            output.add(newPattern2);

            ItemAbstractionPair smallestPair, greaterPair;
            if (elementFromPattern1.compareTo(elementFromPattern2) > 0) {
                smallestPair = elementFromPattern1;
                greaterPair = elementFromPattern2;
            } else {
                smallestPair = elementFromPattern2;
                greaterPair = elementFromPattern1;
            }
            List<ItemAbstractionPair> elementsOfNewPattern3 = new ArrayList<ItemAbstractionPair>(2);
            elementsOfNewPattern3.add(smallestPair);
            elementsOfNewPattern3.add(pairCreator.getItemAbstractionPair(greaterPair.getItem(), Abstraction_Qualitative.create(true)));
            Pattern newPattern3 = patternCreator.createPattern(elementsOfNewPattern3);
            output.add(newPattern3);
        }
        return output;
    }
}
