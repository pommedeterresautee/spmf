package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Itemset;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Pair;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.PseudoSequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.Abstraction_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.patterns.Pattern;

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
     * @return the static reference
     */
    public static AbstractionCreator_Qualitative getInstance() {
        if (instance == null) {
            instance = new AbstractionCreator_Qualitative();
        }
        return instance;
    }
    
    /**
     * It creates a default abstraction. The abstraction is established to false
     * @return the abstraction
     */
    @Override
    public Abstraction_Generic CreateDefaultAbstraction() {
        return Abstraction_Qualitative.create(false);
    }

    /**
     * It creates a relation with the given parameter.
     * @param equalRelation The boolean indicatin if the item has an equal 
     * relation with the previous item in the pattern
     * @return the created relation
     */
    public Abstraction_Generic createAbstraction(boolean equalRelation) {
        return Abstraction_Qualitative.create(equalRelation);
    }

    /**
     * It adds a Pair object to one list when we keep the sequences counted for
     * that pair. If the pair has not been previously kept, we keep the sequenceID
     * @param pairMap
     * @param alreadyCountedForSequenceID
     * @param sequenceId
     * @param item
     * @param postfix 
     */
    private void addPair(Map<Pair, Pair> pairMap,Set<Pair> alreadyCountedForSequenceID, int sequenceId, Item item, boolean postfix) {
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
            // we keep the sequence ID
            pair.getSequencesID().set(sequenceId);
        }
    }

    /**
     * Method to find all frequent items in a database.
     * This is for k> 1.
     * @param sequences the sequences from the database
     * @return the set of frequent items
     */
    @Override
    public Set<Pair> findAllFrequentPairs(List<PseudoSequence> sequences) {
        // we will scan the database and store the cumulative support of each pair in a map.
        Map<Pair, Pair> pairMap = new HashMap<Pair, Pair>();
        Set<Pair> alreadyCountedForSequenceID = new LinkedHashSet<Pair>();

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
                    Itemset itemset = sequence.getItemset(i,k);
                    //We obtain the beginning of that itemset for our projection
                    int beginning = sequence.getBeginningOfItemset(k, i);
                    /*And for each item from the beginning we add a new Pair in
                     *order to find the frequent items in the projection*
                     */
                    for (int j = beginning; j < itemset.size(); j++) {
                        Item item = itemset.get(j);
                        boolean postfix = sequence.isPostfix(k, i);
                        addPair(pairMap, alreadyCountedForSequenceID,sequence.getId(), item, postfix);
                    }
                }
            }
        }
        return pairMap.keySet();
    }

    /**
     * Convert  a Map<Item, Set<Abstraction_Generic>> to  Map<Item, Set<Abstraction_Generic>> 
     * @param sequence a sequence (not used)
     * @param frequentItems a set of frequent items
     * @return the Map<Item, Set<Abstraction_Generic>> 
     */
    @Override
    public Map<Item, Set<Abstraction_Generic>> createAbstractions(Sequence sequence, Map<Item, BitSet> frequentItems) {
        return new HashMap<Item, Set<Abstraction_Generic>>();
    }

    public Abstraction_Generic createAbstractionFromAPrefix(Pattern prefix, Abstraction_Generic abstraccion) {
        return abstraccion;
    }
}
