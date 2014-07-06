package ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.abstractions;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixSpan_AGP.items.PseudoSequence;

/**
 * Abstract Class to enable any kind of abstraction.
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
public abstract class Abstraction_Generic implements Comparable<Abstraction_Generic> {

    /**
     * It checks if this abstraction is equal to another abstraction
     * @param o the other abstraction
     * @return true if equal. Otherwise, false.
     */
    @Override
    public abstract boolean equals(Object o);

    /**
     * It returns the hash code associated with an abstraction
     * @return the hash code
     */
    @Override
    public abstract int hashCode();

    /**
     * It returns a string representation of an abstraction
     * @return a string
     */
    @Override
    public abstract String toString();
    
    /**
     * It returns a string representation according to SPMF format
     * @return a string
     */
    public abstract String toStringToFile();

    /**
     * It returns true if the projection indicated starts in the middle of an 
     * itemset and the abstraction is an equal relation, or if the projection 
     * starts in the first item of the itemset and the abstraction does not 
     * have an equal relation.
     * @param sequence a sequence
     * @param projection the projection index
     * @param indexItemset a given itemset position
     * @return true if the condition above is met.
     */
    public abstract boolean compute(PseudoSequence sequence, int projection, int indexItemset);
}
