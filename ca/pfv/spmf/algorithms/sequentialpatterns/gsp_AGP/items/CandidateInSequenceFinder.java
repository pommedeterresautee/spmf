/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items;


import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.Abstraction_Generic;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.Pattern;

/**
 * In this class a particular pattern is searched in a sequence. This class is a
 * tool of the class that implements the phase that check the support for a
 * candidate set.
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
public class CandidateInSequenceFinder {

    AbstractionCreator creator;
    /**
     * flag to indicate if a candidate is present in the sequence
     */
    private boolean present = false;

    /**
     * Standard constructor. It only needs the abstraction creator.
     *
     * @param creator
     */
    public CandidateInSequenceFinder(AbstractionCreator creator) {
        this.creator = creator;
    }

    /**
     * Recursive method to search for a candidate in a sequence. If the
     * candidate appears in the sequence, set the flag present to true
     *
     * @param candidate to find in the sequence
     * @param sequence the sequence where we will search for the candidate
     * @param k the level in which we are in the main loop of GSP, i.e. the
     * number of items of the candidate
     * @param length the current element with which we are dealing with.
     * @param position List of the positions of all of the elements of the
     * candidate in the sequence
     */
    public void isCandidatePresentInTheSequence_qualitative(Pattern candidate, Sequence sequence, int k, int length, List<int[]> position) {

        //We get the current pair to deal with
        ItemAbstractionPair pair = candidate.getIthElement(length);
        //And we keep its item,
        Item itemPair = pair.getItem();
        //its abstraction
        Abstraction_Generic abstractionPair = pair.getAbstraction();
        // and the previous abstraction
        Abstraction_Generic previousAbstraction = length > 0 ? candidate.getIthElement(length - 1).getAbstraction() : null;
        //flag to know if we should cancel the search
        boolean cancelled = false;
        //Initialization of the position of the current item, itemPair, to search for
        int[] pos = null;
        //while the itemset index that correspond to the current item, itempair, is withing the sequence
        while (position.get(length)[0] < sequence.size()) {
            /*If we are dealing with the first element of the candidate, we 
             * search for the item from the beginning of the sequence
             */
            if (length == 0) {
                pos = sequence.searchForTheFirstAppearance(position.get(length)[0], position.get(length)[1], itemPair);
            } else {
                /*
                 * Otherwise, we find that item depending on the temporal relation
                 * between the  current abstraction and the previous one
                 */
                pos = creator.findPositionOfItemInSequence(sequence, itemPair, abstractionPair, previousAbstraction, position.get(length)[0], position.get(length)[1], position.get(length - 1)[0], position.get(length - 1)[1]);
            }
            //If a duple <itemset index, item index> is found
            if (pos != null) {
                //We keep it in the current index of the position list
                position.set(length, pos);

                //If we are not in the last element of the candidate
                if (length + 1 < k) {
                    //We establish as a new position the following one
                    int[] newPos = increasePosition(sequence, position.get(length));
                    //and we keep it in the position of the following item of the candidate
                    position.set(length + 1, newPos);
                    //And we make a recursive call in order to deal with the next element of the candidate
                    isCandidatePresentInTheSequence_qualitative(candidate, sequence, k, length + 1, position);
                    //If the flag is activated, the process is over
                    if (present) {
                        return;
                    }
                } else {
                    /*If we are in the last element of the candidate, we 
                     * have checked that the candidate it appears in the sequence, and we set the corresponding flat to true
                     */
                    present = true;
                    return;
                }
            } else {//If we cannot find a position where the item appears
                //If we are not in the first element of the candidate
                if (length > 0) {
                    //we increase the previous position and we update it
                    int[] newPos = increaseItemset(position.get(length - 1));
                    position.set(length - 1, newPos);
                }
                /*We set the flag of cancel to true, in order to end this
                 * execution and come back to the previous call, that to
                 * referred to the previous element of the candidate
                 */
                cancelled = true;
                //And we break the loop
                break;
            }
        }
        
        /*if we exceeded the sequence limits we have to try modifying the 
         * previous element position in order to test if we can find any 
         * combination that contains the whole candidate
         */
        if (length > 0 && !cancelled) {
            int[] newPos = increaseItemset(position.get(length - 1));
            position.set(length - 1, newPos);
        }
    }

    /**
     * It answers if the candidate appears in the sequence
     * @return true if it appears otherwise flase
     */
    public boolean isPresent() {
        return present;
    }

    /**
     * Setter of the "present" flag
     * @param present the present flag value 
     */
    public void setPresent(boolean present) {
        this.present = present;
    }

    /**
     * We move to the next item in the same itemset, returning < currentItemsetIndex, currentItemIndex + 1 >.
     * If the itemset is exceeded, we change to the next itemset, establishing the item index in 0 and so, in this case, our returning value is < currentItemsetIndex +1 , 0 >.
     * @param sequence sequence which we are dealing with
     * @param pos the current position
     * @return the incremented position
     */
    private int[] increasePosition(Sequence sequence, int[] pos) {
        int[] newPos;
        if (pos[1] < sequence.get(pos[0]).size() - 1) {
            newPos = new int[]{pos[0], pos[1] + 1};
        } else {
            newPos = new int[]{pos[0] + 1, 0};
        }
        return newPos;
    }

    /**
     * It returns a new pair pointing out the beginning of the next itemset to the given current position
     * @param pos the current position
     * @return the position of beginning of the new itemset
     */
    private int[] increaseItemset(int[] pos) {
        int newItemset = pos[0] + 1;
        int[] newPos = new int[]{newItemset, 0};
        return newPos;
    }
}
