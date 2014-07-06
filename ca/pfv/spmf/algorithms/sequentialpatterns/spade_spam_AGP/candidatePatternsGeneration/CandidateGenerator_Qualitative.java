package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.EquivalenceClass;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions.Abstraction_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator_Qualitative;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.ItemAbstractionPairCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;

/**
 * Class that implements a candidate generator for standard SDM.
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
public class CandidateGenerator_Qualitative implements CandidateGenerator {

    /**
     * Static reference in order to make the class singleton
     */
    private static CandidateGenerator_Qualitative instance = null;

    /**
     * Method to remove the static reference of this class
     */
    public static void clear() {
        instance = null;
    }

    /**
     * Standard constructor.
     */
    private CandidateGenerator_Qualitative() {
    }

    /**
     * Get the static reference to the singleton class.
     *
     * @return
     */
    public static CandidateGenerator_Qualitative getInstance() {
        if (instance == null) {
            instance = new CandidateGenerator_Qualitative();
        }
        return instance;
    }

    /**
     * It generates a list of candidate patterns from the two patterns given as
     * parameters
     *
     * @param pattern1 The first pattern from which a new candidate is generated
     * @param pattern2 The second pattern from which a new candidate is
     * generated
     * @param minSupport The mininum relative support
     * @return A list of candidate patterns created from pattern1 and pattern2
     */
    @Override
    public List<Pattern> generateCandidates(Pattern pattern1, Pattern pattern2, int minSupport, boolean doNotExploreXY, boolean doNotExploreYX, boolean doNotExploreX_Y, boolean doNotExploreY_X) {

        //New list where we keep the new candidate patterns
        List<Pattern> candidates = new ArrayList<Pattern>();

        /* We check if just taking the intersection of sequence appearances we 
         * exceed or equal the minimum support
         */
        BitSet joinBitmap = (BitSet) pattern1.getAppearingIn().clone();

        joinBitmap.and(pattern2.getAppearingIn());

        if (joinBitmap.cardinality() >= minSupport) {//If that is the case

            ItemAbstractionPairCreator pairCreator = ItemAbstractionPairCreator.getInstance();
            AbstractionCreator_Qualitative qualitativeCreator = AbstractionCreator_Qualitative.getInstance();

            /* We get the last two items of the last patterns as well as their 
             * qualitative relations
             */
            ItemAbstractionPair lastPairOfPattern1 = pattern1.getElements().get(pattern1.size() - 1);
            ItemAbstractionPair lastPairOfPattern2 = pattern2.getElements().get(pattern2.size() - 1);

            Abstraction_Qualitative abstractionOfLastPairOfPattern1 = ((Abstraction_Qualitative) lastPairOfPattern1.getAbstraction());
            Abstraction_Qualitative abstractionOfLastPairOfPattern2 = ((Abstraction_Qualitative) lastPairOfPattern2.getAbstraction());

            /*
             * If both of them does not have an equal relation
             */
            if (!abstractionOfLastPairOfPattern1.hasEqualRelation() && !abstractionOfLastPairOfPattern2.hasEqualRelation()) {
                //Both pattern1 and pattern2 has as last element and item occurring in a different itemset to the previous one (last but one)
                //And if the last item of both patterns is not the same
                if (!lastPairOfPattern1.getItem().equals(lastPairOfPattern2.getItem())) {
                    //we have to create three candidate patterns

                    /* EXAMPLE: if the first pattern is (P < x), being x the last item,
                     * and the second pattern is (P < y), being y its last item,
                     * 
                     * We have three possible candidate patterns:
                     * C1 = (P < x < y), with x before y
                     * C2 = (P < y < x), with y before x
                     * C3 = (P < (x y)), with x and y at the same time
                     */

                    //So we make C3
                    Pattern newCandidate_equalRelation = null;
                    if (pattern1.compareTo(pattern2) < 0 && doNotExploreXY == false) {
                        newCandidate_equalRelation = pattern1.clonePattern();
                        newCandidate_equalRelation.add(pairCreator.getItemAbstractionPair(lastPairOfPattern2.getItem(), qualitativeCreator.createAbstraction(true)));
                    } else if (doNotExploreYX == false) {
                        newCandidate_equalRelation = pattern2.clonePattern();
                        newCandidate_equalRelation.add(pairCreator.getItemAbstractionPair(lastPairOfPattern1.getItem(), qualitativeCreator.createAbstraction(true)));
                    }
                    if (newCandidate_equalRelation != null) {
                        candidates.add(newCandidate_equalRelation);
                    }

                    //And we make C2
                    if (doNotExploreY_X == false) {
                        Pattern newCandidate_BeforeRelationWithChangedItems = pattern2.clonePattern();
                        newCandidate_BeforeRelationWithChangedItems.add(lastPairOfPattern1);
                        candidates.add(newCandidate_BeforeRelationWithChangedItems);
                    }
                }
                //And we make C1
                if (doNotExploreX_Y == false) {
                    Pattern newCandidate_BeforeRelation = pattern1.clonePattern();
                    newCandidate_BeforeRelation.add(lastPairOfPattern2);

                    candidates.add(newCandidate_BeforeRelation);
                }

                /*
                 * if the items of both patterns have an equal relation
                 */
            } else if (abstractionOfLastPairOfPattern1.hasEqualRelation() && abstractionOfLastPairOfPattern2.hasEqualRelation()) {
                //We only create a candidate

                /* EXAMPLE: if the first pattern is (P x), being x the last item,
                 * and the second pattern is (P y), being y its last item,
                 * 
                 * We have a possible candidate pattern:
                 * C1 = (P x y), with x and y at the same time
                 */

                Pattern newCandidate_equalRelation;
                if (pattern1.compareTo(pattern2) < 0 && doNotExploreXY == false) {
                    newCandidate_equalRelation = pattern1.clonePattern();
                    newCandidate_equalRelation.add(pairCreator.getItemAbstractionPair(lastPairOfPattern2.getItem(), qualitativeCreator.createAbstraction(true)));
                    candidates.add(newCandidate_equalRelation);
                } else if (pattern1.compareTo(pattern2) > 0 && doNotExploreYX == false) {
                    newCandidate_equalRelation = pattern2.clonePattern();
                    newCandidate_equalRelation.add(pairCreator.getItemAbstractionPair(lastPairOfPattern1.getItem(), qualitativeCreator.createAbstraction(true)));
                    candidates.add(newCandidate_equalRelation);
                }
                /*
                 * if the one pattern has an equal relation and the other has not
                 */
            } else {
                //We only create a candidate

                /* EXAMPLE: if the first pattern is (P x), being x the last item,
                 * and the second pattern is (P < y), being y its last item,
                 * 
                 * We have three possible candidate patterns:
                 * C1 = (P x < y), with y appearing after x
                 * 
                 * Conversely,
                 * if the first pattern is (P < x), being x the last item,
                 * and the second pattern is (P y), being y its last item,
                 * 
                 * We have three possible candidate patterns:
                 * C1 = (P y < x), with x appearing after y
                 */
                Pattern newCandidate_BeforeRelation = null;
                if (abstractionOfLastPairOfPattern1.hasEqualRelation() && doNotExploreX_Y == false) {
                    newCandidate_BeforeRelation = pattern1.clonePattern();
                    newCandidate_BeforeRelation.add(lastPairOfPattern2);
                } else if (doNotExploreY_X == false) {
                    newCandidate_BeforeRelation = pattern2.clonePattern();
                    newCandidate_BeforeRelation.add(lastPairOfPattern1);
                }
                if (newCandidate_BeforeRelation != null) {
                    candidates.add(newCandidate_BeforeRelation);
                }
            }
        }
        return candidates;
    }

    /**
     * It executes the join operation over the Idlists of the equivalence
     * classesgiven as parameters. How the call is done it depends on the two
     * last items in the pattern extension, given as parameter. The minimum
     * support is provided to the method in order to avoid those join operation
     * that we know that lead to infrequent results.
     *
     * @param extension The candidate pattern previously made from the extension
     * of two frequent patterns
     * @param equivalenceClass_i Equivalence class from the pattern1 that
     * allowed creating the candidate extension
     * @param equivalenceClass_j Equivalence class from the pattern2 that
     * allowed creating the candidate extension
     * @param minSupport Minimum relative support
     * @return The IdList associated with the pattern extension previously
     * created.
     */
    @Override
    public IDList join(Pattern extension, EquivalenceClass equivalenceClass_i, EquivalenceClass equivalenceClass_j, int minSupport) {
//    	if(notExploreYX || notExploreXY) {
//        	System.out.println("TEST");
//        }

        //We get the last but one pair of the extension pattern
        ItemAbstractionPair lastButOnePair = extension.getLastButOneElement();
        //We get the last pair extension pattern
        ItemAbstractionPair lastPair = extension.getLastElement();
        /* We get the last pair of the identifier associated with the second Idlist, 
         *i.e. the pattern2 last pair
         */
        ItemAbstractionPair lastPairFromEquivalenceClass_j = equivalenceClass_j.getClassIdentifier().getLastElement();

        /*
         * If the last pair of the extension has an equal relation
         */
        if (((Abstraction_Qualitative) lastPair.getAbstraction()).hasEqualRelation()) { // PFV-2013
            //And the last but one is not the same item
            if (!lastButOnePair.equals(lastPair)) {
                //We get the join of the IdList respecting the order given as parameters
                return equivalenceClass_i.getIdList().join(equivalenceClass_j.getIdList(), true, minSupport);
            }
            //If the last pair of the extension pattern does not have an equal relation
        } else {  //
            //If that last pair of the extension is the same as the last pair of the second Idlist
            if (lastPairFromEquivalenceClass_j.equals(lastPair)) {
                //We get the join of the IdList respecting the order given as parameters
                return equivalenceClass_i.getIdList().join(equivalenceClass_j.getIdList(), false, minSupport);
                //Otherwise
            } else {  // PFV-2013
                //We revert the order in the call to the join operation for both IdLists
                return equivalenceClass_j.getIdList().join(equivalenceClass_i.getIdList(), false, minSupport);
            }
        }

        return null;
    }
}
