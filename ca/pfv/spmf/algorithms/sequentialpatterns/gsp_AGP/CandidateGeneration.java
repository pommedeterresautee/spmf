package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.Pattern;

/** 
 * This is an implementation of the candidate generation addressed in GSP algorithm.
 * This class is one of the two method continuously repeated by means of the GSP's main loop.
 * Here, from a set of frequent candidates k-sequences we generate a set of possible (k+1)-supersequences.
 *
 * Copyright Antonio Gomariz Peñalver 2013
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author agomariz
 */
 class CandidateGeneration {

    /**
     * Main method that creates, from frequent (k-1)-sequence set (aka L(k-1))
     * the new set of (k)-sequences candidates. Before returning the 
     * candidate set, the algorithm prunes those canidates that cannot be frequent
     * at all
     * @param frequentSet the frequent (k-1)-sequence set, L(k-1)
     * @param abstractionCreator the abstraction creator
     * @param indexationMap a map where the frequent sequences are indexed by 
     * their first item
     * @param k the number that corresponds to the current level
     * @param minSupportAbsolute the absolute minimum  support
     * @return the final k-candidate set
     */
    public List<Pattern> generateCandidates(Set<Pattern> frequentSet, AbstractionCreator abstractionCreator, Map<Item, Set<Pattern>> indexationMap, int k, double minSupportAbsolute) {
        //Definition of the set of candidates
        List<Pattern> candidateSet = new ArrayList<Pattern>();
        //copy of (k-1)-frequent sequence set
        List<Pattern> frequentList = new ArrayList<Pattern>(frequentSet);
        //Definition of the set of candidates already pruned
        List<Pattern> prunedCandidates = null;
        if (k > 2) { //If we are not in the base case, i.e. we are generating a level k>2
            Item previousItem = null;
            Set<Pattern> matching = null;
            //For each frequent (k-1)-sequence
            for (Pattern frequentPattern1 : frequentList) {
                //For the second element of the frequent patterns
                Item currentItem = frequentPattern1.getIthElement(1).getItem();
                //If we did not previously processed
                if (!currentItem.equals(previousItem)) {
                    //We get all the patterns that starts with that item as first element
                    matching = indexationMap.get(currentItem);
                    //and assign that item to the previous one
                    previousItem = currentItem;
                }
                //If matching is not empty
                if (matching != null) {
                    //for each of its patterns
                    for (Pattern frequentPattern2 : matching) {
                        //we try combining both frequentPattern1 and frequentPattern2
                        Pattern candidate = abstractionCreator.generateCandidates(abstractionCreator, frequentPattern1, frequentPattern2, minSupportAbsolute);
                        //And if we succeed, we add it to the candidate set
                        if (candidate != null) {
                            candidateSet.add(candidate);
                        }
                    }
                }
            }
            //Once the loop is over, if the candidate set is not empty
            if (!candidateSet.isEmpty()) {
                //We prune those candidates that have some infrequent subpatterns
                prunedCandidates = prunedSubset(candidateSet, frequentSet, abstractionCreator);
            } else {
                return null;
            }
        } else if (k == 2) { //base case, i.e. k=2
            prunedCandidates = new ArrayList<Pattern>();
            for (int i = 0; i < frequentList.size(); i++) {
                for (int j = i; j < frequentList.size(); j++) {
                    //We create candidates with all the possible combinations of frequent 1-sequences
                    prunedCandidates.addAll(abstractionCreator.generateSize2Candidates(abstractionCreator, frequentList.get(i), frequentList.get(j)));
                }
            }
        }

        if (prunedCandidates.isEmpty()) {
            return null;
        }
        return prunedCandidates;
    }

    /**
     * Return the pruned k-candidate set of candidates.
     * @param candidateSet the candidate k-sequence set
     * @param frequentSet the frequent (k-1)-sequence set
     * @param abstractionCreator
     * @return 
     */
    private List<Pattern> prunedSubset(List<Pattern> candidateSet, Set<Pattern> frequentSet, AbstractionCreator abstractionCreator) {
        List<Pattern> candidatePatterns = new ArrayList<Pattern>();
        //for each candidate
        for (Pattern candidate : candidateSet) {
            boolean isInfrequent = false;
            //for each one of its element
            for (int i = 0; i < candidate.getElements().size() && !isInfrequent; i++) {
                //we obtain the subpattern resulting of removing the element chosen just above
                Pattern subpattern = abstractionCreator.getSubpattern(candidate, i);
                //and if this subpattern does not appear in the frequent (k-1)-sequence set, L(k-1)
                if (!frequentSet.contains(subpattern)) {
                    //we mark it as infrequent
                    isInfrequent = true;
                }
            }
            if (!isInfrequent) {
                //We only add in the output set those patterns that not have any infrequent subpattern
                candidatePatterns.add(candidate);
            }
        }
        return candidatePatterns;
    }
}
