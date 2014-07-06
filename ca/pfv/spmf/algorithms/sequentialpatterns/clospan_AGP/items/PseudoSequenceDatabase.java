package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to make possible the implementation of pseudoprojections already
 * explained in the paper of PrefixSpan Algorithm. By means of this class, we
 * convert a usual sequence in a pseudosequence, where we can point out the
 * different projection points that we have in every database projection.
 *
 * This class is inspired in SPMF PrefixSpan implementation.
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
public class PseudoSequenceDatabase {

    /**
     * The pseudosequences that compose the database
     */
    private List<PseudoSequence> pseudoSequences = new ArrayList<PseudoSequence>();
    /**
     * Value that represents the sum of all the itemset that appear in all the
     * pseudosequences
     */
    private int cumulativeSum;
    /**
     * Value that represents the sum of all the projections that appear in all
     * the pseudosequences
     */
    private int cumulativeSumNumberOfProjections;
    /**
     * Value that represents the sum of all the elements of the projections that
     * appear in all the pseudosequences
     */
    private int numberOfElementsProjectedDatabase;
    /**
     * Value that have a concatenation of values that corresponds to all the
     * elements of the projections that appear in all the pseudosequences
     */
    private int elementsProjectedDatabase;

    /**
     * Get the list of pseudosequences from this database
     *
     * @return the list of PseudoSequence objects
     */
    public List<PseudoSequence> getPseudoSequences() {
        return pseudoSequences;
    }

    /**
     * It returns a string representation of this database
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder r = new StringBuilder("============  CONTEXTE ==========");
        for (PseudoSequence sequence : pseudoSequences) { // pour chaque objet
            r.append(sequence.getId());
            r.append(":  ");
            r.append(sequence.toString());
            r.append('\n');
        }
        return r.toString();
    }

    /**
     * It returns the number of pseudosequences that the database has
     *
     * @return the number of pseudosequences
     */
    public int size() {
        return pseudoSequences.size();
    }

    public int getNumberOfElementsProjectedDatabase() {
        return this.numberOfElementsProjectedDatabase;
    }

    public void setNumberOfElementsProjectedDatabase(int value) {
        this.numberOfElementsProjectedDatabase = value;
    }

    /**
     * It returns the set of sequence IDs of pseudosequences in this sequence database
     *
     * @return the Set of sequence ids.
     */
    public Set<Integer> getSequenceIDs() {
        Set<Integer> ensemble = new HashSet<Integer>();
        for (PseudoSequence sequence : getPseudoSequences()) {
            ensemble.add(sequence.getId());
        }
        return ensemble;
    }

    /**
     * It adds a sequence to the database
     *
     * @param newSequence the sequence to be added
     */
    public void addSequence(PseudoSequence newSequence) {
        pseudoSequences.add(newSequence);
    }

    /**
     * It clears the whole database
     */
    public void clear() {
        if (pseudoSequences != null) {
            pseudoSequences.clear();
            pseudoSequences = null;
        }
    }

    public void setCumulativeSum(int cumulativeSum) {
        this.cumulativeSum = cumulativeSum;
    }

    public int getCumulativeSum() {
        return this.cumulativeSum;
    }

    public int getCumulativeSumNumberOfProjections() {
        return cumulativeSumNumberOfProjections;
    }

    public void setCumulativeSumNumberOfProjections(int cumulativeSumNumberOfProjections) {
        this.cumulativeSumNumberOfProjections = cumulativeSumNumberOfProjections;
    }

    public int getElementsProjectedDatabase() {
        return elementsProjectedDatabase;
    }

    public void setElementsProjectedDatabase(String string) {
        if (string.length() > 0) {
            //this.elementsProjectedDatabase = Integer.valueOf(string);
        }else{
            this.elementsProjectedDatabase=0;
        }
    }
}
