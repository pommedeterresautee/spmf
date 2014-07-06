package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.Pair;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.PseudoSequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.PseudoSequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.creators.ItemAbstractionPairCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.savers.Saver;

/**
 * This is an the real execution of PrefixSpan algorithm.
 * The main methods of this class are called from class AlgoPrefixSpan_AGP, and
 * the main loop of the algorithm is executed here.
 * 
 * NOTE: This implementation saves the pattern  to a file as soon 
 * as they are found or can keep the pattern into memory, depending
 * on what the user choose.
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
class RecursionPrefixSpan_AGP {

    /**
     * Abstraction creator
     */
    private AbstractionCreator abstractionCreator;
    /**
     * Saver, got from Class AlgoPrefixSpan where the user has already chosen
     * where he wants to keep the results.
     */
    private Saver saver;
    /**
     * absolute minimum support.
     */
    private long minSupportAbsolute;
    /**
     * Original pseudosequence database (without infrequent items)
     */
    private PseudoSequenceDatabase pseudoDatabase;
    /**
     * Map which match the frequent items with their appearances
     */
    private Map<Item, BitSet> mapSequenceID;
    /**
     * Number of frequent items found by PrefixSpan
     */
    private int numberOfFrequentPatterns = 0;

    /**
     * Standard constructor
     * @param abstractionCreator the abstraction creator
     * @param saver The saver for correctly save the results where the user wants
     * @param minSupportAbsolute The absolue minimum support
     * @param pseudoDatabase The original pseudoSequence database (without frequent items)
     * @param mapSequenceID Map which match the frequent items with their appearances
     */
    public RecursionPrefixSpan_AGP(AbstractionCreator abstractionCreator, Saver saver, long minSupportAbsolute, PseudoSequenceDatabase pseudoDatabase, Map<Item, BitSet> mapSequenceID) {
        this.abstractionCreator = abstractionCreator;
        this.saver = saver;
        this.minSupportAbsolute = minSupportAbsolute;
        this.pseudoDatabase = pseudoDatabase;
        this.mapSequenceID = mapSequenceID;
    }

    /**
     * It executes the actual PrefixSpan Algorithm
     * @param keepPatterns Flag indicating if the user wants to keep the results
     * or he is just interested in the number of frequent patterns
     * @param verbose Flag for debugging purposes
     */
    public void execute(boolean keepPatterns, boolean verbose) {
        //We get all the frequent items and we sort them
        List<Item> keySetList = new ArrayList<Item>(mapSequenceID.keySet());
        Collections.sort(keySetList);
        if (verbose) {
            System.out.println(keySetList.size() + " frequent items");
        }
        int numberOfFrequentItems = keySetList.size();
        int cont = 0;
        //For each frequent item
        for (Item item : keySetList) {
            cont++;
            if (verbose) {
                System.out.println("Projecting item = " + item + " (" + cont + "/" + numberOfFrequentItems + ")");
            }
            // We make a projection in the original database
            PseudoSequenceDatabase projectedContext = makePseudoProjections(item, pseudoDatabase, abstractionCreator.CreateDefaultAbstraction(), true);
            // And we create a new 1-pattern with that frequent item
            ItemAbstractionPair pair = new ItemAbstractionPair(item, abstractionCreator.CreateDefaultAbstraction());
            Pattern prefix = new Pattern(pair);
            //And we insert it its appearances
            prefix.setAppearingIn((BitSet) ((mapSequenceID.get(item).clone())));
            if (keepPatterns) {
                //We keep the 1-patterns if the flag is active
                saver.savePattern(prefix);
            }
            //We update the number of frequent patterns
            numberOfFrequentPatterns++;
            if (projectedContext != null && projectedContext.size() >= minSupportAbsolute) {
                //And we call the main loop
                prefixSpanLoop(prefix, 2, projectedContext, keepPatterns, verbose);
            }
        }
    }

    /**
     * It projects the database given as parameter
     * @param item The item from which we make the projection
     * @param database The database where we make the projection
     * @param abstraction Abstraction associated with the item to project
     * @param firstTime Flag that points out if it the first time that 
     * @return The new projected database
     */
    private PseudoSequenceDatabase makePseudoProjections(Item item, PseudoSequenceDatabase database, Abstraction_Generic abstraction, boolean firstTime) {
        // The projected pseudo-database
        PseudoSequenceDatabase newProjectedDatabase = new PseudoSequenceDatabase();
        List<PseudoSequence> pseudoSequences = database.getPseudoSequences();
        for (int sequenceIndex = 0; sequenceIndex < pseudoSequences.size(); sequenceIndex++) { // for each sequence
            PseudoSequence sequence = pseudoSequences.get(sequenceIndex);
            /* We guess the maximum size that our new projected database can
             * achieve. If its number of potential sequences is less than the 
             * minimum support, we can stop projecting
             */ 
            int potentialSize = newProjectedDatabase.size() + pseudoSequences.size() - sequenceIndex;
            if (potentialSize < minSupportAbsolute) {
                return null;
            }
            /*Flag indicating if the current sequence has already been projected
             * for the new projected database
             */
            boolean alreadyProjected = false;
            //Initialization of the new projected sequence for the current one
            PseudoSequence newSequence = null;
            //Initialization of the number of projections done in the current sequence
            int numberOfProjections = 0;
            //Set keeping the projections already done
            Set<Integer> projectionsAlreadyMade = new HashSet<Integer>();
            //For all the existing projections in the current sequence
            for (int k = 0; k < sequence.numberOfProjectionsIncluded(); k++) {
                int sequenceSize = sequence.size(k);
                // for each itemset of the sequence
                for (int i = 0; i < sequenceSize; i++) {  
                    // we get the index ofthe given item to project in current the itemset
                    int index = sequence.indexOf(k, i, item);
                    //If the item has been found and either is the first projection or the method compute is true
                    if (index != -1 && (firstTime || (abstraction.compute(sequence, k, i)))) {
                        int itemsetSize = sequence.getSizeOfItemsetAt(k, i);
                        // if the found item is not the last item of the itemset
                        if (index != itemsetSize - 1) {
                            //If this sequence has not been yet projected
                            if (!alreadyProjected) {
                                //A new pseudosequence is created starting from the next point to the found item
                                newSequence = new PseudoSequence(sequence.getRelativeTimeStamp(i, k), sequence, i, index + 1, k);
                                //We keep the projection point
                                projectionsAlreadyMade.add(sequence.getFirstItemset(k) + i);
                                //If the new pseudosequence has more than one item
                                if (newSequence.size(numberOfProjections) > 0) {
                                    //we increase the number of projections
                                    numberOfProjections++;
                                    //And we add the new projected sequence to the new database
                                    newProjectedDatabase.addSequence(newSequence);
                                }
                                /*We set the flag to true, indicating that the 
                                 * current sequence has been already projected
                                 */
                                alreadyProjected = true;
                            } else {
                                /*If the sequence is already projected and the 
                                projection point has not been previously used*/
                                if (projectionsAlreadyMade.add(sequence.getFirstItemset(k) + i)) {
                                    /*We make another projection in the same 
                                     * sequence previously projected, adding a 
                                     * new projection point*/
                                    newSequence.addProjectionPoint(k, sequence.getRelativeTimeStamp(i, k), sequence, i, index + 1);
                                }
                            }
                            /* if the found item is the last item of the sequence
                             * and the item where it is, it is not the last itemset
                             * of the sequence*/
                        } else if ((i != sequenceSize - 1)) {
                            //and has not been yet projected
                            if (!alreadyProjected) {
                                /*We create a new projected sequence starting 
                                 * in the next itemset to where the item appeared*/
                                newSequence = new PseudoSequence(sequence.getRelativeTimeStamp(i, k), sequence, i + 1, 0, k);
                                //And we count the projection
                                projectionsAlreadyMade.add(sequence.getFirstItemset(k) + i);
                                //If there is any item in the new sequence
                                if (itemsetSize > 0 && newSequence.size(numberOfProjections) > 0) {
                                    //we increase the number of projections
                                    numberOfProjections++;
                                    //And we add the new projected sequence to the new database
                                    newProjectedDatabase.addSequence(newSequence);
                                }
                                /*We set the flag to true, indicating that the 
                                 * current sequence has been already projected
                                 */
                                alreadyProjected = true;
                            } else {
                                /*If the sequence is already projected and the 
                                projection point has not been previously used*/
                                if (projectionsAlreadyMade.add(sequence.getFirstItemset(k) + i)) {
                                    /*We make another projection in the same 
                                     * sequence previously projected, adding a 
                                     * new projection point*/
                                    newSequence.addProjectionPoint(k, sequence.getRelativeTimeStamp(i, k), sequence, i + 1, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
        return newProjectedDatabase;
    }

    /**
     * Method that executes the main loop of prefixSpan for all the patterns
     * with a size greater than 1
     * @param prefix prefix from which we made the projected database and where
     * the frequent items that we find will be added
     * @param k size of patterns that are going to be generated
     * @param context prefix-projected databases
     * @param keepPatterns flag indicating if we want to keep the output or we 
     * are interesting in just the number of frequent patterns
     * @param verbose flag for debuggin purposes
     */
    private void prefixSpanLoop(Pattern prefix, int k, PseudoSequenceDatabase context, boolean keepPatterns, boolean verbose) {
        // find frequent items that appear in the given pseudosequence database.
        Set<Pair> pairs = abstractionCreator.findAllFrequentPairs(context.getPseudoSequences());
        ItemAbstractionPairCreator pairCreator = ItemAbstractionPairCreator.getInstance();
        if (verbose) {
            StringBuilder tab = new StringBuilder();
            for (int i = 0; i < k - 2; i++) {
                tab.append('\t');
            }
            System.out.println(tab + "Projecting prefix = " + prefix);
            System.out.print(tab + "\tFound " + pairs.size() + " frequent items in this projection\n");
        }
        // For each pair found,
        for (Pair pair : pairs) {
            // if the item is frequent.
            if (pair.getSupport() >= minSupportAbsolute) {
                // create the new pattern
                Pattern newPrefix = prefix.clonePattern();
                ItemAbstractionPair newPair = pairCreator.getItemAbstractionPair(pair.getPair().getItem(), abstractionCreator.createAbstractionFromAPrefix(prefix, pair.getPair().getAbstraction()));
                newPrefix.add(newPair);
                // build the projected database with respect to this frequent item (the item which forms the prefix)
                PseudoSequenceDatabase projection = makePseudoProjections(pair.getPair().getItem(), context, pair.getPair().getAbstraction(), false);
                // We add its set of sequences where the prefix appear
                newPrefix.setAppearingIn((BitSet) (pair.getSequencesID().clone()));
                // if the flag of keeping patterns if active, we keep this new pattern
                if (keepPatterns) {
                    saver.savePattern(newPrefix);
                }
                //update the number of frequent patterns
                numberOfFrequentPatterns++;
                //If the projection exists and has more sequences than the absolute minimum  support
                if (projection != null && projection.size() >= minSupportAbsolute) {
                    //We make a recursive call to the main method
                    prefixSpanLoop(newPrefix, k + 1, projection, keepPatterns, verbose); // r�cursion
                }
            }
        }
    }

    /**
     * It returns the number of frequent patterns.
     * @return the number of frequent patterns.
     */
    public int numberOfFrequentPatterns() {
        return numberOfFrequentPatterns;
    }

    /**
     * It clears the attributes of this class.
     */
    public void clear() {
        if (saver != null) {
            saver.clear();
            saver = null;
        }
        if (pseudoDatabase != null) {
            pseudoDatabase.clear();
            pseudoDatabase = null;
        }
        if (mapSequenceID != null) {
            mapSequenceID.clear();
            mapSequenceID = null;
        }
    }
}
