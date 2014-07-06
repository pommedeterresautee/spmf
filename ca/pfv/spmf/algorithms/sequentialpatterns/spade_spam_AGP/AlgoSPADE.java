package ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.candidatePatternsGeneration.CandidateGenerator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.database.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.dataStructures.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.Saver;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.SaverIntoFile;
import ca.pfv.spmf.algorithms.sequentialpatterns.spade_spam_AGP.savers.SaverIntoMemory;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the SPADE. SPADE was proposed by ZAKI in 2001.
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
public class AlgoSPADE {

    public long joinCount; // PFV 2013
    
    /**
     * the minimum support threshold
     */
    protected double minSup;
    /**
     * The minimum support relative threshold, i.e. the minimum number of
     * sequences where the patterns have to be
     */
    protected double minSupRelative;
    /**
     * Flag indicating if we want a depth-first search when true. Otherwise we
     * say that we want a breadth-first search
     */
    protected boolean dfs;
    /**
     * Saver variable to decide where the user want to save the results, if it
     * the case
     */
    Saver saver = null;
    /**
     * Start and end points in order to calculate the overall time taken by the
     * algorithm
     */
    public long start, end;
    /**
     * Equivalence class whose class identifier is a frequent item
     */
    protected List<EquivalenceClass> frequentItems;
    /**
     * Abstraction creator
     */
    private AbstractionCreator abstractionCreator;
    /**
     * Number of frequent patterns found by the algorithm
     */
    private int numberOfFrequentPatterns;

    /**
     * Constructor of the class that calls SPADE algorithm.
     *
     * @param support Minimum support (from 0 up to 1)
     * @param dfs Flag for indicating if we want a depth first search. If false,
     * we indicate that we want a breath-first search.
     * @param abstractionCreator An abstraction creator.
     */
    public AlgoSPADE(double support, boolean dfs, AbstractionCreator abstractionCreator) {
        this.minSup = support;
        this.abstractionCreator = abstractionCreator;
        this.dfs = dfs;
    }

    /**
     * Actual call to SPADE algorithm. The output can be either kept or ignore.
     * Whenever we choose to keep the patterns found, we can keep them in a file
     * or in the main memory
     *
     * @param database Original database in where we want to search for the
     * frequent patterns.
     * @param candidateGenerator The candidate generator used by the algorithm
     * SPADE
     * @param keepPatterns Flag indicating if we want to keep the output or not
     * @param verbose Flag for debugging purposes
     * @param outputFilePath Path of the file in which we want to store the
     * frequent patterns. If this value is null, we keep the patterns in the
     * main memory. This argument is taken into account just when keepPatterns
     * is activated.
     * @throws IOException
     */
    public void runAlgorithm(SequenceDatabase database, CandidateGenerator candidateGenerator, boolean keepPatterns, boolean verbose, String outputFilePath) throws IOException {
        //If we do no have any file path
        if (outputFilePath == null) {
            //The user wants to save the results in memory
            saver = new SaverIntoMemory();
        } else {
            //Otherwise, the user wants to save them in the given file
            saver = new SaverIntoFile(outputFilePath);
        }
        //this.minSupRelative = minSup; // PFV 2013
		this.minSupRelative = (int) Math.ceil(database.size() * minSup);
        if (this.minSupRelative == 0) { // protection
            this.minSupRelative = 1;
        }
        // reset the stats about memory usage
        MemoryLogger.getInstance().reset();
        //keeping the starting time
        start = System.currentTimeMillis();
        //We run SPADE algorithm

        runSPADE(database, candidateGenerator, (long) minSupRelative, dfs, keepPatterns, verbose);

        //keeping the ending time
        end = System.currentTimeMillis();
        //Search for frequent patterns: Finished
        saver.finish();
    }
    
    /**
     * Actual call to SPADE algorithm. The output can be either kept or ignore.
     * Whenever we choose to keep the patterns found, we can keep them in a file
     * or in the main memory. The algorithm SPADE is executed in a parallel way.
     *
     * @param database Original database in where we want to search for the
     * frequent patterns.
     * @param candidateGenerator The candidate generator used by the algorithm
     * SPADE
     * @param keepPatterns Flag indicating if we want to keep the output or not
     * @param verbose Flag for debugging purposes
     * @param outputFilePath Path of the file in which we want to store the
     * frequent patterns. If this value is null, we keep the patterns in the
     * main memory. This argument is taken into account just when keepPatterns
     * is activated.
     * @throws IOException
     */
    public void runAlgorithmParallelized(SequenceDatabase database, CandidateGenerator candidateGenerator, boolean keepPatterns, boolean verbose, String outputFilePath) throws IOException {
        //If we do no have any file path
        if (outputFilePath == null) {
            //The user wants to save the results in memory
            saver = new SaverIntoMemory();
        } else {
            //Otherwise, the user wants to save them in the given file
            saver = new SaverIntoFile(outputFilePath);
        }
        this.minSupRelative = (int) Math.ceil(minSup * database.size());
        //this.minSupRelative = (int) (database.size() * minSup);
        if (this.minSupRelative == 0) { // protection
            this.minSupRelative = 1;
        }
        // reset the stats about memory usage
	MemoryLogger.getInstance().reset();
        //keeping the starting time
        start = System.currentTimeMillis();
        
        //We run SPADE algorithm
        runSPADEFromSize2PatternsParallelized2(database, candidateGenerator, (long) minSupRelative,  dfs, keepPatterns, verbose);

        //keeping the ending time
        end = System.currentTimeMillis();
        //Search for frequent patterns: Finished
        saver.finish();
    }

    /**
     *
     * The actual method for extracting frequent sequences.
     *
     * @param database The original database
     * @param candidateGenerator The candidate generator used by the algorithm
     * SPADE
     * @param minSupportCount The minimum relative support
     * @param dfs Flag for indicating if we want a depth first search. If false,
     * we indicate that we want a breath-first search.
     * @param keepPatterns flag indicating if we are interested in keeping the
     * output of the algorithm
     * @param verbose Flag for debugging purposes
     */
    protected void runSPADE(SequenceDatabase database, CandidateGenerator candidateGenerator, long minSupportCount, boolean dfs, boolean keepPatterns, boolean verbose) {
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
        
        // CREATE COOCURENCE MAP
        

        database = null;

        //We define the root class
        EquivalenceClass rootClass = new EquivalenceClass(null);
        /*And we insert the equivalence classes corresponding to the frequent
        1-patterns as its members*/
        for (EquivalenceClass atom : frequentItems) {
            rootClass.addClassMember(atom);
        }
        
        //Inizialitation of the class that is in charge of find the frequent patterns
        FrequentPatternEnumeration frequentPatternEnumeration = new FrequentPatternEnumeration(candidateGenerator, minSupRelative, saver);
        //We set the number of frequent items to the number of frequent items
        frequentPatternEnumeration.setFrequentPatterns(frequentItems.size());

        //We execute the search
        frequentPatternEnumeration.execute(rootClass, dfs, keepPatterns, verbose, null,null);

        /* Once we had finished, we keep the number of frequent patterns that we 
         * finally found
         */
        numberOfFrequentPatterns = frequentPatternEnumeration.getFrequentPatterns();
        // check the memory usage for statistics
        MemoryLogger.getInstance().checkMemory();

		joinCount = frequentPatternEnumeration.INTERSECTION_COUNTER;
    }

    /**
     *
     * The actual method for extracting frequent sequences. This method it starts
     * with both the frequent 1-patterns and 2-patterns already found.
     *
     * @param database The original database
     * @param candidateGenerator The candidate generator used by the algorithm
     * SPADE
     * @param minSupportCount The minimum relative support
     * @param dfs Flag for indicating if we want a depth first search. If false,
     * we indicate that we want a breath-first search.
     * @param keepPatterns flag indicating if we are interested in keeping the
     * output of the algorithm
     * @param verbose Flag for debugging purposes
     */
    protected void runSPADEFromSize2Sequences(SequenceDatabase database, CandidateGenerator candidateGenerator, long minSupportCount, boolean dfs, boolean keepPatterns, boolean verbose) {
        frequentItems = database.frequentItems();
        Collection<Pattern> size1Patterns = getPatterns(frequentItems);
        saver.savePatterns(size1Patterns);

        List<EquivalenceClass> size2Patterns = database.getSize2FrecuentSequences(minSupRelative);

        Collection<Pattern> size2sequences = getPatterns(size2Patterns);
        saver.savePatterns(size2sequences);


        size2Patterns.clear();
        database.clear();
        size2Patterns = null;
        database = null;

        FrequentPatternEnumeration frequentPatternEnumeration = new FrequentPatternEnumeration(candidateGenerator, minSupRelative, saver);
        frequentPatternEnumeration.setFrequentPatterns(size1Patterns.size() + size2sequences.size());

        size1Patterns = null;
        size2sequences = null;

        while (frequentItems.size() > 0) {
            EquivalenceClass frequentAtomClass = frequentItems.get(frequentItems.size() - 1);
            if (verbose) {
                System.out.println("Exploring... " + frequentAtomClass);
            }

            frequentPatternEnumeration.execute(frequentAtomClass, dfs, keepPatterns, verbose, null,null);
            frequentItems.remove(frequentItems.size() - 1);
            if (verbose) {
                System.out.println("\tWe found " + frequentPatternEnumeration.getFrequentPatterns() + " frequent patterns so far.");
            }
            
            
            // check the memory usage for statistics
            MemoryLogger.getInstance().checkMemory();
        }
        numberOfFrequentPatterns = frequentPatternEnumeration.getFrequentPatterns();
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
        sb.append(" Join count : ");
        sb.append(joinCount);
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
     * @return 
     */
    public long getRunningTime() {
        return (end - start);
    }

    /**
     * It gets the minimum relative support, i.e. the minimum number of database
     * sequences where a pattern has to appear
     * @return 
     */
    public double getMinSupRelative() {
        return minSupRelative;
    }

    /**
     * It clears all the attributes of AlgoPrefixSpan class
     */
    public void clear() {
        frequentItems.clear();
        abstractionCreator = null;
        if (saver != null) {
            saver.clear();
            saver = null;
        }
    }

    /**
     *
     * The actual method for extracting frequent sequences. This method it starts
     * with both the frequent 1-patterns and 2-patterns already found. Besides, it
     * resolves each equivalence class formed by the 1-patterns independently.
     * 
     * @param database The original database
     * @param candidateGenerator The candidate generator used by the algorithm
     * SPADE
     * @param minSupportCount The minimum relative support
     * @param dfs Flag for indicating if we want a depth first search. If false,
     * we indicate that we want a breath-first search.
     * @param keepPatterns flag indicating if we are interested in keeping the
     * output of the algorithm
     * @param verbose Flag for debugging purposes
     */
    protected void runSPADEFromSize2PatternsParallelized(SequenceDatabase database, CandidateGenerator candidateGenerator, long minSupportCount, boolean dfs, boolean keepPatterns, boolean verbose) {

        frequentItems = database.frequentItems();
        Collection<Pattern> size1Patterns = getPatterns(frequentItems);
        saver.savePatterns(size1Patterns);
        List<EquivalenceClass> size2EquivalenceClass = database.getSize2FrecuentSequences(minSupRelative);
        Collection<Pattern> size2Sequences = getPatterns(size2EquivalenceClass);
        saver.savePatterns(size2Sequences);

        size2EquivalenceClass = null;
        database = null;

        FrequentPatternEnumeration frequentPatternEnumeration = new FrequentPatternEnumeration(candidateGenerator, minSupRelative, saver);
        frequentPatternEnumeration.setFrequentPatterns(size1Patterns.size() + size2Sequences.size());

        size1Patterns = null;
        size2Sequences = null;

        Runtime runtime = Runtime.getRuntime();
        int numberOfAvailableProcessors = runtime.availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(numberOfAvailableProcessors);
        ArrayList<Future<Void>> set = new ArrayList<Future<Void>>();
        while (frequentItems.size() > 0) {
            EquivalenceClass frequentItem = frequentItems.get(frequentItems.size() - 1);

            if (verbose) {
                System.out.println("Exploring " + frequentItem);
            }

            Callable<Void> callable = new FrequentPatternEnumerationFacade(frequentPatternEnumeration, frequentItem, dfs, keepPatterns, verbose, saver);
            Future<Void> future = pool.submit(callable);
            set.add(future);
            frequentItems.remove(frequentItems.size() - 1);
            
            // check the memory usage for statistics
            MemoryLogger.getInstance().checkMemory();
        }

        try {
            int cont = 1;
            System.err.println("There are " + set.size() + " equivalence classes and " + numberOfAvailableProcessors + " available processors");
            while (!set.isEmpty()) {
                for (int i = 0; i < set.size(); i++) {
                    Future<Void> future = set.get(i);
                    if (future.isDone()) {
                        System.err.println(cont++ + ":this thread is done.");
                        set.remove(i);
                        i--;
                    }
                }
            }

            numberOfFrequentPatterns = frequentPatternEnumeration.getFrequentPatterns();// check the memory usage for statistics
            MemoryLogger.getInstance().checkMemory();

            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println("Problems with the concurrency!!");
            e.printStackTrace();
        }
    }

    /**
     *
     * The actual method for extracting frequent sequences. This method it starts
     * with both the frequent 1-patterns and 2-patterns already found. Besides, it
     * resolves each equivalence class formed by the 1-patterns independently.
     * 
     * @param database The original database
     * @param candidateGenerator The candidate generator used by the algorithm
     * SPADE
     * @param minSupportCount The minimum relative support
     * @param dfs Flag for indicating if we want a depth first search. If false,
     * we indicate that we want a breath-first search.
     * @param keepPatterns flag indicating if we are interested in keeping the
     * output of the algorithm
     * @param verbose Flag for debugging purposes
     */
    protected void runSPADEFromSize2PatternsParallelized2(SequenceDatabase database, CandidateGenerator candidateGenerator, long minSupportCount, boolean dfs, boolean keepPatterns, boolean verbose) {
        frequentItems = database.frequentItems();
        Collection<Pattern> size1Sequences = getPatterns(frequentItems);
        saver.savePatterns(size1Sequences);
        List<EquivalenceClass> size2EquivalenceClasses = database.getSize2FrecuentSequences(minSupRelative);
        Collection<Pattern> size2Sequences = getPatterns(size2EquivalenceClasses);
        saver.savePatterns(size2Sequences);

        numberOfFrequentPatterns = size1Sequences.size() + size2Sequences.size();
        size2EquivalenceClasses = null;
        database = null;

        Runtime runtime = Runtime.getRuntime();
        ExecutorService pool = Executors.newFixedThreadPool(runtime.availableProcessors());
        Set<Future<Void>> set = new LinkedHashSet<Future<Void>>();

        ArrayList<FrequentPatternEnumeration> enumerates = new ArrayList<FrequentPatternEnumeration>();

        while (frequentItems.size() > 0) {
            EquivalenceClass frequentAtom = frequentItems.get(frequentItems.size() - 1);

            if (verbose) {
                System.out.println("Exploring " + frequentAtom);
            }
            FrequentPatternEnumeration frequentPatternEnumeration = new FrequentPatternEnumeration(candidateGenerator, minSupRelative, saver);

            enumerates.add(frequentPatternEnumeration);

            Callable<Void> callable = new FrequentPatternEnumerationFacade(frequentPatternEnumeration, frequentAtom, dfs, keepPatterns, verbose, saver);
            Future<Void> future = pool.submit(callable);
            set.add(future);
            frequentItems.remove(frequentItems.size() - 1);
            
            // check the memory usage for statistics
            MemoryLogger.getInstance().checkMemory();
        }

        try {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println("Problems with the concurrency!!");
        }
        FrequentPatternEnumeration fpe = new FrequentPatternEnumeration(candidateGenerator, minSup, saver);
        numberOfFrequentPatterns += fpe.getFrequentPatterns();
            
        // check the memory usage for statistics
        MemoryLogger.getInstance().checkMemory();
    }
}
