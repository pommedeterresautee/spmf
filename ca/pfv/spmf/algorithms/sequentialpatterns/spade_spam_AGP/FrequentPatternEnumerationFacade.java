package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP;

import java.util.concurrent.Callable;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.Saver;

/**
 * This class is used by the methods that mine the database in a parallelized
 * way. We make callable the class FrequentPatternEnumeration through this class.
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
public class FrequentPatternEnumerationFacade implements Callable<Void> {

    /**
     * Link to a FrequentPatternEnumeration object
     */
    private FrequentPatternEnumeration frequentPatternEnumeration;
    /**
     * Equivalence class through which we execute the main method of 
     * FrequentPatternEnumeration object
     */
    private EquivalenceClass equivalenceClass;
    /**
     * Flag indicating if we are interested in a depth-first search. If it false
     * we assume a breadth-first search.
     */
    private boolean dfs;
    /**
     * Flag indicating if we want to keep the different patterns that we find.
     */
    private boolean keepPatterns;
    /**
     * Flag for debugging purposes.
     */
    private boolean verbose;

    /**
     * Standard constructor.
     * @param frequentPatternEnumeration the FrequentPatternEnumeration object
     * through which we search for the patterns.
     * @param equivalenceClass Equivalence class from which we start the search.
     * @param dfs Flag indicating if we want to keep the different patterns that
     * we find.
     * @param keepPatterns Flag indicating if we want to keep the different 
     * patterns that we find.
     * @param verbose Flag for debugging purposes.
     * @param saver Object that is in charge of saving the output either in a 
     * file or in the main memory.
     */
    public FrequentPatternEnumerationFacade(FrequentPatternEnumeration frequentPatternEnumeration, EquivalenceClass equivalenceClass, boolean dfs, boolean keepPatterns, boolean verbose, Saver saver) {
        this.frequentPatternEnumeration = frequentPatternEnumeration;
        this.equivalenceClass = equivalenceClass;
        this.keepPatterns = keepPatterns;
        this.dfs = dfs;
        this.verbose = verbose;
    }

    /**
     * Implementation of callable interface. Thought for parallelized executions.
     * @return a Void object
     * @throws Exception 
     */
    @Override
    public Void call() throws Exception {
        frequentPatternEnumeration.execute(equivalenceClass, dfs, keepPatterns, verbose,null,null);
        return null;
    }
    
    public FrequentPatternEnumeration getFrequentPatternEnumeration() {
        return frequentPatternEnumeration;
    }

    public EquivalenceClass getEquivalenceClass() {
        return equivalenceClass;
    }

    public boolean isVerbose() {
        return verbose;
    }
}
