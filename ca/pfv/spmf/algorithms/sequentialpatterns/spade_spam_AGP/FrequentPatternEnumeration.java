package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP;

import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.Saver;

/**
 * This is an implementation of the main methods of SPADE algorithm. We keep
 * open the decision of which IdList to use. We have implemented three different
 * kinds of IdList so far: 1) One based on hash map with arraylist
 * (IDListStandard_Map) 2) One based on hash map with bitsets (IDListBitMap) 3)
 * One based on a big bitmap with all the information kept inside
 * (IDListFatBitmap)
 *
 * NOTE: This implementation saves the pattern to a file as soon as they are
 * found or can keep the pattern into memory, depending on what the user choose.
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
public class FrequentPatternEnumeration {

    /**
     * The candidate generator used by SPADE
     */
    private CandidateGenerator candidateGenerator;
    /**
     * The absolute minimum support  threshold, i.e. the minimum number of
     * sequences where the patterns have to be
     */
    private double minSupportAbsolute;
    /**
     * Number of frequent patterns found by the algorithm. Initially set to
     * zero.
     */
    private static int frequentPatterns = 0;
    public static int INTERSECTION_COUNTER = 0;
    /**
     * Saver variable to decide where the user want to save the results, if it
     * the case
     */
    private Saver saver = null;

    /**
     * Standard constructor of the class.
     *
     * @param candidateGenerator The candidate generator used by SPADE
     * @param minSupportAbsolute The absolute minimum support threshold
     * @param saver Saver object to decide where the user want to save the
     * results, if it the case
     */
    public FrequentPatternEnumeration(CandidateGenerator candidateGenerator, double minSupportAbsolute, Saver saver) {
        INTERSECTION_COUNTER = 0;
        this.candidateGenerator = candidateGenerator;
        this.minSupportAbsolute = minSupportAbsolute;
        this.saver = saver;
    }

    /**
     * Execution of the search of frequent patterns.
     *
     * @param eq The equivalence class from we start to search for.
     * @param dfs Flag indicating if we are interested in a depth-first search
     * if activated. Otherwise, we understand that we are interested in a
     * breadth-first search
     * @param keepPatterns Flag to indicate if we want to keep the patterns
     * found.
     * @param verbose Flag for debugging purposes
     * @param coocMapBefore
     */
    public void execute(EquivalenceClass eq, boolean dfs, boolean keepPatterns, boolean verbose, Map<Integer, Map<Integer, Integer>> coocMapAfter, Map<Integer, Map<Integer, Integer>> coocMapEquals) {
        /*eq.setIdList(null);
         eq.setClassIdentifier(null);*/
        //flag indicating if a new pattern has been created
        boolean anyPatternCreated = false;
        List<EquivalenceClass> eqMembers = eq.getClassMembers();

        //For all the members of the equivalence class
        for (int i = eqMembers.size() - 1; i >= 0; i--) {
            //we get it the member indicated by i index
            EquivalenceClass child_X = eqMembers.get(i);

            // NEW CODE-PFV-2013
            Map<Integer, Integer> cmapX = null;
            Map<Integer, Integer> cmapX_equals = null;
            Integer itemX = null;
            if (coocMapAfter != null || coocMapEquals != null) {
                itemX = (Integer) child_X.getClassIdentifier().getLastElement().getItem().getId();
                cmapX = (coocMapAfter == null) ? null : coocMapAfter.get(itemX);
                cmapX_equals = (coocMapEquals == null) ? null : coocMapEquals.get(itemX);
            }
            // END NEW CODE

            //For all the members that appear before in the list
            for (int j = i; j >= 0; j--) {

                //we get it the member indicated by j index
                EquivalenceClass child_Y = eqMembers.get(j);

                // NEW CODE-PFV-2013                
                boolean doNotExploreXY = false;
                boolean doNotExploreYX = false;
                boolean doNotExploreX_Y = false;
                boolean doNotExploreY_X = false;
                if (coocMapEquals != null) {
                    Integer itemY = (Integer) child_Y.getClassIdentifier().getLastElement().getItem().getId();
                    Map<Integer, Integer> cmapY = coocMapEquals.get(itemY);
                    Integer count1 = cmapX_equals == null ? null : cmapX_equals.get(itemY);
                    Integer count2 = cmapY == null ? null : cmapY.get(itemX);
                    doNotExploreYX = count2 == null || count2 < minSupportAbsolute;
                    doNotExploreXY = count1 == null || count1 < minSupportAbsolute;
                }
                if (coocMapAfter != null) {
                    Integer itemY = (Integer) child_Y.getClassIdentifier().getLastElement().getItem().getId();
                    Map<Integer, Integer> cmapY = coocMapAfter.get(itemY);
                    Integer count1 = cmapX == null ? null : cmapX.get(itemY);
                    Integer count2 = cmapY == null ? null : cmapY.get(itemX);
                    doNotExploreY_X = count2 == null || count2 < minSupportAbsolute;
                    doNotExploreX_Y = count1 == null || count1 < minSupportAbsolute;
                }

                if (doNotExploreXY && doNotExploreYX && doNotExploreX_Y && doNotExploreY_X) {
                    continue;
                }
                // END NEW-CODE-PFV-2013


                /* We obtain all the possible candidates generated by the two 
                 * dentifiers of the chosen equivalence classes
                 */
                List<Pattern> extensions = candidateGenerator.generateCandidates(child_X.getClassIdentifier(), child_Y.getClassIdentifier(), (int) minSupportAbsolute, doNotExploreXY, doNotExploreYX, doNotExploreX_Y, doNotExploreY_X);
                //For each candidate generated
                for (Pattern extension : extensions) {
                    IDList newIdList = candidateGenerator.join(extension, child_X, child_Y, (int) minSupportAbsolute);
                    INTERSECTION_COUNTER++;
                    //If the the pattern is frequent
                    if (newIdList != null && newIdList.getSupport() >= minSupportAbsolute) {
                        //we activated the flag
                        anyPatternCreated = true;
                        /*and we insert the appearances that we have in the 
                         *IdList in the recenctly created pattern
                         */

                        newIdList.setAppearingSequences(extension);

                        //We keep the pattern if the flag is activated
                        if (keepPatterns) {
                            keepPattern(extension);
                        }
                        /*And we make a new equivalence class with the new 
                         * pattern and the IdList
                         */
                        EquivalenceClass newEq = new EquivalenceClass(extension);
                        newEq.setIdList(newIdList);

                        //We increment the number of frequent patterns
                        increaseFrequentPatterns();

                        /*Finally, we keep the new class as a member of the 
                         * parent class that is its prefix
                         */
                        insertClassByPrefix(newEq, child_X, child_Y);
                    }
                }
            }
            //If dfs activated
            if (dfs) {
                /* We remove the current member pointed out by i index of the set 
                 * of members of eq
                 */
                eqMembers.remove(i);
                /* And if any pattern has been created, we make a recursive call
                 * with the child that we have just removed
                 */
                if (anyPatternCreated) {
                    execute(child_X, dfs, keepPatterns, verbose, coocMapAfter, coocMapEquals);
                }
            }
        }
        //If dfs is not activated, therefore we are interested in a breadth-first search
        if (!dfs) {
            //if any pattern has been created
            if (anyPatternCreated) {
                for (int i = eqMembers.size() - 1; i >= 0; i--) {
                    // we make a recursive call with the child pointed by i
                    execute(eqMembers.get(i), dfs, keepPatterns, verbose, coocMapAfter, coocMapEquals);
                    /* Once we finished of exploring, we remove from the members
                     * the member indicated by i index */
                    eqMembers.remove(i);
                }
            }
            eqMembers = null;
        }
    }

    /**
     * Method to insert a class in their corresponding father. This father is
     * that is a prefix for the class given as a parameter.
     *
     * @param eq Equivalence class with we want to insert as a member of its
     * father
     * @param eq_X Candidate father 1
     * @param eq_Y Candidate father 2
     */
    private void insertClassByPrefix(EquivalenceClass eq, EquivalenceClass eq_X, EquivalenceClass eq_Y) {
        //If eq_X is prefix of eq
        if (eq_X.getClassIdentifier().isPrefix(eq.getClassIdentifier())) {
            //We insert eq as a member of eq_X
            eq_X.addClassMember(eq);
        } else {//Otherwise, eq_Y is prefix of eq and we insert eq as a member of eq_Y
            eq_Y.addClassMember(eq);
        }
    }

    /**
     * It returns the number of frequent patterns found by the last execution of
     * the algorithm.
     *
     * @return
     */
    public int getFrequentPatterns() {
        return frequentPatterns;
    }

    public void setFrequentPatterns(int patronesFrecuentes) {
        FrequentPatternEnumeration.frequentPatterns = patronesFrecuentes;
    }

    /**
     * Increase the number of frequent patterns
     */
    private synchronized static void increaseFrequentPatterns() {
        frequentPatterns++;
    }

    /**
     * Keep the pattern given as a parameter.
     *
     * @param pattern The pattern that we want to keep.
     */
    private synchronized void keepPattern(Pattern pattern) {
        saver.savePattern(pattern);
    }
}