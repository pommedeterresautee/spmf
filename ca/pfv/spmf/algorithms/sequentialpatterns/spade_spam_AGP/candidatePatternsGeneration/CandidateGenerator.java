package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration;

import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.EquivalenceClass;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.idLists.IDList;

/**
 * Interface for a candidate generator class. If we are interested in having 
 * several types of patterns, we can define them by implementing the methods 
 * here exposed.
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
public interface CandidateGenerator {

    /**
     * It generates a list of candidate patterns from the two patterns given as
     * parameters
     * @param pattern1 The first pattern from which a new candidate is generated
     * @param pattern2 The second pattern from which a new candidate is generated
     * @param minSupport The mininum relative support
     * @param doNotExploreYX 
     * @param doNotExploreXY 
     * @return A list of candidate patterns created from pattern1 and pattern2
     */
    public List<Pattern> generateCandidates(Pattern pattern1, Pattern pattern2, int minSupport, boolean doNotExploreXY, boolean doNotExploreYX,boolean doNotExploreX_Y, boolean doNotExploreY_X);

    /**
     * It executes the join operation over the Idlists  of the equivalence 
     * classesgiven as parameters. How the call is done it depends on the two 
     * last items in the pattern extension, given as parameter. The minimum 
     * support is provided to the method in order to avoid those join operation 
     * that we know that lead to infrequent results.
     * 
     * @param extension The candidate pattern previously made from the extension
     * of two frequent patterns
     * @param equivalenceClass_i Equivalence class from the pattern1 that allowed
     * creating the candidate extension
     * @param equivalenceClass_j Equivalence class from the pattern2 that allowed
     * creating the candidate extension
     * @param minSupport Minimum relative support
     * @param notExploreYX 
     * @param notExploreXY 
     * @return The IdList associated with the pattern extension previously created.
     */
    public IDList join(Pattern extension, EquivalenceClass equivalenceClass_i, EquivalenceClass equivalenceClass_j,int minSupport);
}
