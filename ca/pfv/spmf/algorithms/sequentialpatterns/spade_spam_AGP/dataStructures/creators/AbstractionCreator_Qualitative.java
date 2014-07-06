package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.EquivalenceClass;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions.Abstraction_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.PatternCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.creators.IdListCreator;

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

    private static AbstractionCreator_Qualitative instance = null;

    public static void sclear() {
        instance=null;
    }

    private AbstractionCreator_Qualitative() {
    }

    public static AbstractionCreator_Qualitative getInstance() {
        if (instance == null) {
            instance = new AbstractionCreator_Qualitative();
        }
        return instance;
    }

    /**
     * It creates an default abstraction. The abstraction is established to false
     * @return the created abstraction
     */
    @Override
    public Abstraction_Generic createDefaultAbstraction() {
        return Abstraction_Qualitative.create(false);
    }

    /**
     * It creates a relation with the given parameter.
     * @param hasEqualRelation The boolean indicatin if the item has an equal 
     * relation with the previous item in the pattern
     * @return the created relation
     */
    public Abstraction_Generic createAbstraction(boolean hasEqualRelation) {
        return Abstraction_Qualitative.create(hasEqualRelation);
    }

    /**
     * It finds all the frequent 2-patterns in the original database. This method
     * is specially useful if we think of parallelizing the execution of the Spade
     * algorithm.
     * @param sequences the list of sequences
     * @param idListCreator the idlist creator for creating the idlists
     * @return the list of equivalence classes
     */
    @Override
    public List<EquivalenceClass> getFrequentSize2Sequences(List<Sequence> sequences, IdListCreator idListCreator) {
        Map<Pattern, EquivalenceClass> totalMap = new HashMap<Pattern, EquivalenceClass>();
        List<EquivalenceClass> result = new LinkedList<EquivalenceClass>();
        //For each Sequence
        for (Sequence seq : sequences) {
            List<Itemset> itemsets = seq.getItemsets();
            //For each itemset in the sequence
            for (int i = 0; i < itemsets.size(); i++) {
                Itemset currentItemset = itemsets.get(i);
                //for each item in the itemset
                for (int j = 0; j < currentItemset.size(); j++) {
                    //we choose it as first item
                    Item item = currentItemset.get(j);

                    ItemAbstractionPair par1 = new ItemAbstractionPair(item, createDefaultAbstraction());
                    //With the rest of items in the same itemset we make a 2-sequence i-extension
                    for (int k = j + 1; k < currentItemset.size(); k++) {
                        Item item2 = currentItemset.get(k);
                        ItemAbstractionPair pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(true));
                        updateIdList(totalMap, par1, pair2, seq.getId(), (int) currentItemset.getTimestamp(), idListCreator);
                    }
                    //With the rest of items in the rest of itemsets we make a 2-sequence s-extension
                    for (int k = i + 1; k < itemsets.size(); k++) {
                        Itemset nextItemset = itemsets.get(k);
                        for (int n = 0; n < nextItemset.size(); n++) {
                            Item item2 = nextItemset.get(n);
                            ItemAbstractionPair pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(false));
                            updateIdList(totalMap, par1, pair2, seq.getId(), (int) nextItemset.getTimestamp(), idListCreator);
                        }
                    }
                }
            }
        }
        result.addAll(totalMap.values());
        Collections.sort(result);
        return result;
    }

    /**
     * Helper method for getFrequentSize2Sequence, useful for making the pattern
     * and the IdList from two given pairs <item, abstraction>.
     * @param totalMap Correspondences between patterns and equivalence classes
     * @param pair1 First element of the pattern to create
     * @param pair2 Second element of the pattern to create
     * @param sid Sequence identifier
     * @param tid Transaction timestamp
     * @param idListCreator IdlistCreator
     */
    public void updateIdList(Map<Pattern, EquivalenceClass> totalMap, ItemAbstractionPair pair1, ItemAbstractionPair pair2, int sid, int tid, IdListCreator idListCreator) {
        PatternCreator patternCreator = PatternCreator.getInstance();
        List<ItemAbstractionPair> size2PatternElements = new ArrayList<ItemAbstractionPair>(2);
        size2PatternElements.add(pair1);
        size2PatternElements.add(pair2);
        Pattern pattern = patternCreator.createPattern(size2PatternElements);

        EquivalenceClass eq = totalMap.get(pattern);
        if (eq == null) {
            IDList id = idListCreator.create();
            eq = new EquivalenceClass(pattern, id);
            totalMap.put(pattern, eq);
        }
        IDList id = eq.getIdList();
        idListCreator.addAppearance(id, sid, tid);
    }

    /**
     * It obtains the subpattern that is derived from removing from the given
     * pattern, the item specified in the position pointed out by the given index 
     * @param extension a pattern
     * @param index the position
     * @return the subpattern
     */
    @Override
    public Pattern getSubpattern(Pattern extension, int index) {
        ItemAbstractionPairCreator pairCreator = ItemAbstractionPairCreator.getInstance();
        PatternCreator patternCreator = PatternCreator.getInstance();
        List<ItemAbstractionPair> subpatternElements = new ArrayList<ItemAbstractionPair>(extension.size() - 1);
        Abstraction_Generic abstraction = null;
        int nextIndex = index + 1;
        for (int i = 0; i < extension.size(); i++) {
            if (i != index) {
                if (i == nextIndex) {
                    if (abstraction == null) {
                        abstraction = extension.getIthElement(i).getAbstraction();
                    }
                    subpatternElements.add(pairCreator.getItemAbstractionPair(extension.getIthElement(i).getItem(), abstraction));
                } else {
                    subpatternElements.add(extension.getIthElement(i));
                }
            } else {
                if (index == 0) {
                    abstraction = createDefaultAbstraction();
                } else {
                    Abstraction_Qualitative abstractionOfRemovedPair = (Abstraction_Qualitative) extension.getIthElement(i).getAbstraction();                    
                    if (!abstractionOfRemovedPair.hasEqualRelation()) {
                        abstraction = createAbstraction(false);
                    }
                }
            }
        }
        return patternCreator.createPattern(subpatternElements);
    }

    
    @Override
    public List<EquivalenceClass> getFrequentSize2Sequences(Map<Integer, Map<Item, List<Integer>>> database, Map<Item, EquivalenceClass> frequentItems, IdListCreator idListCreator) {
        Map<Pattern, EquivalenceClass> totalMap = new HashMap<Pattern, EquivalenceClass>();
        List<EquivalenceClass> result = new LinkedList<EquivalenceClass>();
        for (Entry<Integer, Map<Item, List<Integer>>> seq : database.entrySet()) {
            Integer sequence = seq.getKey();
            List<Entry<Item, List<Integer>>> itemItemsetsAssociation = new ArrayList(seq.getValue().entrySet());
            //for each itemset in the sequence
            for (int i = 0; i < itemItemsetsAssociation.size(); i++) {
                Entry<Item, List<Integer>> currentEntry1 = itemItemsetsAssociation.get(i);
                Item item1 = currentEntry1.getKey();
                List<Integer> appearances1 = currentEntry1.getValue();
                if (!isFrequent(item1, frequentItems)) {
                    continue;
                }

                for (int m = 0; m < appearances1.size(); m++) {
                    int item1Appearance = appearances1.get(m);

                    ItemAbstractionPair pair1 = new ItemAbstractionPair(item1, createDefaultAbstraction());
                    for (int j = 0; j < itemItemsetsAssociation.size(); j++) {
                        Entry<Item, List<Integer>> currentEntry2 = itemItemsetsAssociation.get(j);
                        Item item2 = currentEntry2.getKey();
                        List<Integer> appearances2 = currentEntry2.getValue();
                        if (!isFrequent(item2, frequentItems)) {
                            continue;
                        }

                        for (int k = 0; k < appearances2.size(); k++) {
                            int item2Apperance = appearances2.get(k);
                            ItemAbstractionPair pair2 = null;
                            if (item2Apperance == item1Appearance) {
                                if (-item2.compareTo(item1) == 1) {
                                    pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(true));
                                }
                            } else if (item2Apperance > item1Appearance) {
                                pair2 = new ItemAbstractionPair(item2, Abstraction_Qualitative.create(false));
                            }
                            if (pair2 != null) {
                                updateIdList(totalMap, pair1, pair2, sequence, item2Apperance, idListCreator);
                            }
                        }
                    }
                }
            }
        }
        result.addAll(totalMap.values());
        Collections.sort(result);
        return result;
    }

    private boolean isFrequent(Item item1, Map<Item, EquivalenceClass> itemsfrecuentes) {
        return (itemsfrecuentes.get(item1)) != null;
    }

    @Override
    public void clear() {
    }
}
