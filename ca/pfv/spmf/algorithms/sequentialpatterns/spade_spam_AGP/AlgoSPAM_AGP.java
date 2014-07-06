package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP;
/*
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
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.Saver;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.SaverIntoFile;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.SaverIntoMemory;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the SPAM algorithm. SPAM was proposed by Ayres
 * in 2002.
 * <br/><br/>
 *
 * NOTE: This implementation saves the pattern to a file as soon as they are
 * found or can keep the pattern into memory if no output path is provided by the user.
 * 
 * 
 * @author Antonio Gomariz Peñalver 
 */
public class AlgoSPAM_AGP {

    /**
     * the minimum support threshold
     */
    protected double minSupRelative;
    /**
     * The absolute minimum support threshold, i.e. the minimum number of
     * sequences where the patterns have to be
     */
    protected double minSupAbsolute;
    /**
     * Saver variable to decide where the user want to save the results, if it
     * the case
     */
    Saver saver = null;
    /**
     * Start and end points in order to calculate the overall time taken by the
     * algorithm
     */
    protected long start, end;
    /**
     * Equivalence class whose class' identifier is a frequent item
     */
    protected List<EquivalenceClass> frequentItems;
    /**
     * Number of frequent patterns found by the algorithm
     */
    private int numberOfFrequentPatterns;

    /**
     * Constructor of the class that calls SPAM algorithm.
     *
     * @param minsupRelative Minimum support (from 0 up to 1)
     */
    public AlgoSPAM_AGP(double minsupRelative) {
        this.minSupRelative = minsupRelative;
    }

    /**
     * Actual call to SPAM algorithm. The output can be either kept or ignore.
     * Whenever we choose to keep the patterns found, we can keep them in a file
     * or in the main memory
     *
     * @param database Original database in where we want to search for the
     * frequent patterns.
     * @param keepPatterns Flag indicating if we want to keep the output or not
     * @param verbose Flag for debugging purposes
     * @param outputFilePath Path of the file in which we want to store the
     * frequent patterns. If this value is null, we keep the patterns in the
     * main memory. This argument is taken into account just when keepPatterns
     * is activated.
     * @throws IOException
     */
    public void runAlgorithm(SequenceDatabase database, boolean keepPatterns, boolean verbose, String outputFilePath) throws IOException {
        //If we do no have any file path
        if (outputFilePath == null) {
            //The user wants to save the results in memory
            saver = new SaverIntoMemory();
        } else {
            //Otherwise, the user wants to save them in the given file
            saver = new SaverIntoFile(outputFilePath);
        }

        this.minSupAbsolute = (int) Math.ceil(minSupRelative * database.size());
        if (this.minSupAbsolute == 0) { // protection
            this.minSupAbsolute = 1;
        }
        // reset the stats about memory usage
	MemoryLogger.getInstance().reset();
        //keeping the starting time
        start = System.currentTimeMillis();
        //We run SPAM algorithm
        runSPAM(database, (long) minSupAbsolute, keepPatterns, verbose);
        //keeping the ending time
        end = System.currentTimeMillis();
        //Search for frequent patterns: Finished
        saver.finish();
    }

    /**
     * The actual method for extracting frequent sequences.
     * @param database The original database
     * @param minSupportAbsolute the absolute minimum  support
     * @param keepPatterns flag indicating if we are interested in keeping the 
     * output of the algorithm
     * @param verbose Flag for debugging purposes
     */
    protected void runSPAM(SequenceDatabase database, long minSupportAbsolute, boolean keepPatterns, boolean verbose) {

        //We get the equivalence classes formed by the frequent 1-patterns
        frequentItems = database.frequentItems();
        //We extract their patterns
        Collection<Pattern> size1sequences = getPatterns(frequentItems);
        //If we want to keep the output
        if (keepPatterns) {
            for (Pattern atom : size1sequences) {
                //We keep all the frequent 1-patterns
                saver.savePattern(atom);
            }
        }

        database = null;

        //We define the root class
        EquivalenceClass rootClass = new EquivalenceClass(null);
        /*And we insert the equivalence classes corresponding to the frequent
        1-patterns as its members*/
        for (EquivalenceClass atom : frequentItems) {
            rootClass.addClassMember(atom);
        }
        
        //Inizialitation of the class that is in charge of find the frequent patterns
        FrequentPatternEnumeration_SPAM frequentPatternEnumeration = new FrequentPatternEnumeration_SPAM(minSupAbsolute, saver);
        //We execute the search
        frequentPatternEnumeration.execute(rootClass, keepPatterns, verbose);

        //Once we had finished, we keep the number of frequent patterns that we found
        numberOfFrequentPatterns = frequentPatternEnumeration.getFrequentPatterns();
        // check the memory usage for statistics
        MemoryLogger.getInstance().checkMemory();
    }

    /**
     * It gets the patterns that are the identifiers of the given equivalence classes
     * @param equivalenceClasses The set of equivalence classes from where we want
     * to obtain their class identifiers
     * @return 
     */
    private Collection<Pattern> getPatterns(List<EquivalenceClass> equivalenceClasses) {
        ArrayList<Pattern> patterns = new ArrayList<Pattern>();
        for (EquivalenceClass equivalenceClass : equivalenceClasses) {
            Pattern frequentPattern = equivalenceClass.getClassIdentifier();
            patterns.add(frequentPattern);
        }
        return patterns;
    }

    public String printStatistics() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
        sb.append(getRunningTime());
        sb.append(" ms\n");
        sb.append(" Frequent sequences count : ");
        sb.append(numberOfFrequentPatterns);
        sb.append('\n');
        sb.append(" Max memory (mb):");
	sb.append(MemoryLogger.getInstance().getMaxMemory());
        sb.append('\n');
        sb.append(saver.print());
        sb.append("\n===================================================\n");
        return sb.toString();
    }

    public int getNumberOfFrequentPatterns() {
        return numberOfFrequentPatterns;
    }

    /**
     * It gets the time spent by the algoritm in its execution.
     * @return the total time
     */
    public long getRunningTime() {
        return (end - start);
    }

    /**
     * It gets the minimum relative support, i.e. the minimum number of database
     * sequences where a pattern has to appear
     * @return the minimum support
     */
    public double getMinSupRelative() {
        return minSupAbsolute;
    }

    /**
     * It clears all the attributes of AlgoSpam class
     */
    public void clear() {
        frequentItems.clear();
        if (saver != null) {
            saver.clear();
            saver = null;
        }
    }
}
