package ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.CandidateInSequenceFinder;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.abstractions.ItemAbstractionPair;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.patterns.Pattern;


/** 
 * This is an implementation of the counting of support phase addressed in GSP algorithm.
 * This class is one of the two method continuously repeated by means of the GSP's main loop.
 * Here, from a set of (k+1)-sequences candidates we check which of those sequences are actually frequent and which can be ruled out.
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

class SupportCounting {

    /**
     * Original database where we have to look for each candidate.
     */
    private SequenceDatabase database;
    /**
     * Indexation map. A tool for the next candidate generation step.
     */
    private Map<Item, Set<Pattern>> indexationMap;
    private AbstractionCreator abstractionCreator;

    /**
     * The only constructor
     * @param database the original sequence database
     * @param creador 
     */
    public SupportCounting(SequenceDatabase database, AbstractionCreator creador) {
        this.database = database;
        this.abstractionCreator = creador;
        this.indexationMap = new HashMap<Item, Set<Pattern>>();
    }

    /**
     * Main method. For all of the elements from the candidate set, we check if
     * they are or not frequent. 
     * @param candidateSet the candidate set
     * @param k the level where we are checking
     * @param minSupportAbsolute the absolute minimum support, i.e. the minimum number of
     * sequences where a candidate have to appear
     * @return the set of frequent patterns.
     */
    public Set<Pattern> countSupport(List<Pattern> candidateSet, int k, double minSupportAbsolute) {
        indexationMap.clear();
        //For each sequence of the original database
        for (Sequence sequence : database.getSequences()) {
            //we check for each candidate if it appears in that sequence
            checkCandidateInSequence(sequence, k, candidateSet);
        }
        Set<Pattern> result = new LinkedHashSet<Pattern>();
        //We keep all the frequent candidates and we put them in the indexation map
        for (Pattern candidate : candidateSet) {
            if (candidate.getSupport() >= minSupportAbsolute) {
                result.add(candidate);
                putInIndexationMap(candidate);
            }
        }
        candidateSet = null;
        //We end returning the frequent candidates, i.e. the frequent k-sequence set
        return result;
    }

    /**
     * We check, for a sequence, if each candidate from the candidate set it appears or not
     * @param sequence a sequence
     * @param k he level where we are checking
     * @param candidateSet the candidate set
     */
    private void checkCandidateInSequence(Sequence sequence, int k, List<Pattern> candidateSet) {
        //For each candidate
        for (Pattern candidate : candidateSet) {
            //We define a list of k positions, all initialized at itemset 0, item 0, i.e. first itemset, first item.
            List<int[]> position = new ArrayList<int[]>(k);
            for (int i = 0; i < k; i++) {
                position.add(new int[]{0,0});
            }
            CandidateInSequenceFinder finder = new CandidateInSequenceFinder(abstractionCreator);
            //we check if the current candidate appears in the sequence
            abstractionCreator.isCandidateInSequence(finder, candidate, sequence, k, 0, position);
            if (finder.isPresent()) {
                /*if we have a positive result, we add the sequence Id to the list
                * of appearances associated with the candidate pattern
                */
                candidate.addAppearance(sequence.getId());
            }
        }
    }

    /**
     * Method to create the indexation map useful for the next step of
     * generation of candidates
     * @param entry 
     */
    private void putInIndexationMap(Pattern entry) {
        ItemAbstractionPair pair = entry.getIthElement(0);
        Set<Pattern> correspondence = indexationMap.get(pair.getItem());
        if (correspondence == null) {
            correspondence = new LinkedHashSet<Pattern>();
            indexationMap.put(pair.getItem(), correspondence);
        }
        correspondence.add(entry);
    }

    /**
     * Get the indexation map associated with the frequent k-sequence set
     * @return the indexation map
     */
    public Map<Item, Set<Pattern>> getIndexationMap() {
        return indexationMap;
    }
}
