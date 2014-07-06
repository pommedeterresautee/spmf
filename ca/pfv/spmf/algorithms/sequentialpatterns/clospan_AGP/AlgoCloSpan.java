package ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP;
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
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Item;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.PseudoSequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.PseudoSequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.patterns.Pattern;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.savers.Saver;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.savers.SaverIntoFile;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.savers.SaverIntoMemory;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is an implementation of the CloSpan algorithm. CloSpan was proposed by
 * X. Yan et al. 2003.
 *<br/><br/>
 *
 * NOTE: This implementation saves the pattern to a file as soon as they are
 * found or can keep the pattern into memory if no output path is provided by the user.
 * 
 * @author Antonio Gomariz Peñalver 
 */
public class AlgoCloSpan {

    protected double minSupRelative;
    protected double minSupAbsolute;
    /**
     * original sequential data set to be used for sequential patterns
     * extraction
     */
    protected SequenceDatabase originalDataset;
    /**
     * all generated frequent sequences, i.e. sequential patterns
     */
    /**
     * Saver variable to decide where the user want to save the results, if it
     * the case
     */
    Saver saver = null;
    /**
     * Start and End points in order to calculate the overall time taken by the 
     * algorithm
     */
    protected long overallStart, overallEnd;
    /**
     * Start and End points in order to calculate the time taken by the main 
     * part of CloSpan algorithm
     */
    protected long mainMethodStart, mainMethodEnd;
    /**
     * Start and End points in order to calculate the time taken by the 
     * post-processing method of CloSpan algorithm
     */
    protected long postProcessingStart, postProcessingEnd;
    /**
     * The abstraction creator
     */
    private AbstractionCreator abstractionCreator;
    /**
     * Number of frequent patterns found by the algorithm
     */
    private int numberOfFrequentPatterns = 0;
    /**
     * flag to indicate if we are interesting in only finding the closed 
     * sequences
     */
    private boolean findClosedPatterns;
    /**
     * flag to indicate if we are interesting in only finding the closed sequence 
     * through the postprocessing step
     */
    private boolean executePruningMethods;

    /**
     * Standard constructor. It takes the relative minimum support threshold (from 1 up
     * to 0) and an abstraction creator
     *
     * @param minSupRelative the relative minimum support threshold
     * @param creator the abstraction creator
     * @param findClosedPatterns flag to indicate if we are interesting in only
     * finding the closed sequences
     */
    public AlgoCloSpan(double minSupRelative, AbstractionCreator creator, boolean findClosedPatterns, boolean executePruningMethods) {
        this.minSupRelative = minSupRelative;
        this.abstractionCreator = creator;
        this.findClosedPatterns = findClosedPatterns;
        this.executePruningMethods = executePruningMethods;
    }

    /**
     * Method that starts the execution of the algorithm.
     *
     * @param database The original database in which we apply PrefixSpan
     * @param keepPatterns Flag indicating if the user want to keep the frequent
     * patterns or he just want the amount of them
     * @param verbose Flag for debugging purposes
     * @param outputFilePath Path pointing out to the file where the output,
     * composed of frequent patterns, has to be kept. If, conversely, this
     * parameter is null, we understand that the user wants the output in the
     * main memory
     * @throws IOException
     */
    public void runAlgorithm(SequenceDatabase database, boolean keepPatterns, boolean verbose, String outputFilePath) throws IOException {
        //calculation of the minimum relative support
        minSupAbsolute = (int) Math.ceil(minSupRelative * database.size());
        if (this.minSupAbsolute == 0) { // protection
            this.minSupAbsolute = 1;
        }
        // reset the stats about memory usage
	MemoryLogger.getInstance().reset();
        //keeping the starting time
        overallStart = System.currentTimeMillis();
        //Starting CloSpan algorithm
        cloSpan(database, keepPatterns, verbose, findClosedPatterns, executePruningMethods, outputFilePath);
        //keeping the ending time
        overallEnd = System.currentTimeMillis();
        //Search for frequent patterns: Finished
        saver.finish();
    }

    /**
     * Method that executes the first steps before calling the actual main 
     * method of CloSpan. In particular, the original database is fully 
     * converted to a pseudosequece database, removing the infrequent items 
     * that appeared in the original dabase
     * @param database The original database
     * @param keepPatterns Flag indicating if the user want to keep the frequent 
     * patterns or he just want the amount of them
     * @param verbose Flag for debugging purposes
     * @param findClosedPatterns flag to indicate if we are interesting in only
     * finding the closed sequences
     * @param outputFilePath Path pointing out to the file where the output, 
     * composed of frequent patterns, has to be kept. If, conversely, this 
     * parameter is null, we understand that the user wants the output in the main memory
     * @throws IOException 
     */
    protected void cloSpan(SequenceDatabase database, boolean keepPatterns, boolean verbose, boolean findClosedPatterns, boolean executePruningMethods, String outputFilePath) throws IOException {
        //If we do no have any file path
        if (outputFilePath == null) {
            //The user wants to save the results in memory
            saver = new SaverIntoMemory();
        } else {
            //Otherwise, the user wants to save them in the given file
            saver = new SaverIntoFile(outputFilePath);
        }

        /*
         * We get the map which relates the frequent items with their
         * appearances in the database
         */
        Map<Item, BitSet> mapSequenceID = database.getFrequentItems();

        /*
         * projection of the original database in order to obtain a
         * pseudosequence
         * database
         */
        PseudoSequenceDatabase pseudoDatabase = projectInitialDatabase(database, mapSequenceID, (int) minSupAbsolute);

        //We initialize the class that is in charge of managing the main loop of PrefixSpan
        RecursionCloSpan algorithm = new RecursionCloSpan(abstractionCreator, saver, (int) minSupAbsolute, pseudoDatabase, mapSequenceID, findClosedPatterns,executePruningMethods);
        
        mainMethodStart=System.currentTimeMillis();
        //And we execute the actual algorithm
        algorithm.execute(verbose);
        mainMethodEnd=System.currentTimeMillis();

        long timeForMainMethod = (mainMethodEnd - mainMethodStart) / 1000;

        //Finally we update the number of frequent patterns that we found
        numberOfFrequentPatterns = algorithm.numberOfFrequentPatterns();
        
        // check the memory usage for statistics
	MemoryLogger.getInstance().checkMemory();
        
        if (verbose) {
            System.out.println("CLOSPAN: The algorithm takes " + timeForMainMethod + " seconds and finds " + numberOfFrequentPatterns + " patterns");
        }
        //If the we are interested in closed patterns, we execute the post-processing step
        if (findClosedPatterns) {
            List<Pattern> outputPatternsFromMainMethod = algorithm.getFrequentPatterns();

            postProcessingStart = System.currentTimeMillis();
            algorithm.removeNonClosedPatterns(outputPatternsFromMainMethod, keepPatterns);
            postProcessingEnd = System.currentTimeMillis();

            long timeForPostProcessingStep = (postProcessingEnd - postProcessingStart) / 1000;
            numberOfFrequentPatterns = algorithm.numberOfFrequentPatterns();
            if (verbose) {
                System.out.println("CLOSPAN: The post-processing algorithm to remove the non-Closed patterns takes " + timeForPostProcessingStep + " seconds and finds " + numberOfFrequentPatterns + " Closed patterns");
            }
        }else{
            if(keepPatterns){
                List<Pattern> outputPatternsFromMainMethod = algorithm.getFrequentPatterns();
                for(Pattern p:outputPatternsFromMainMethod){
                    saver.savePattern(p);
                }
            }
        }

        algorithm.clear();
        pseudoDatabase.clear();        
        
        // check the memory usage for statistics
	MemoryLogger.getInstance().checkMemory();
    }

    /**
     * Method to get the outlined information about the search for frequent 
     * sequences by means of CloSpan algorithm s a string
     * @return a string containing this information
     */
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
        sb.append('\n');
        sb.append("\n===================================================\n");
        return sb.toString();
    }
    
    public int getNumberOfFrequentPatterns() {
        return numberOfFrequentPatterns;
    }

    /**
     * It gets the total time spent by the algoritm in its execution.
     * @return the time
     */
    public long getRunningTime() {
        return (overallEnd - overallStart);
    }

    /**
     * It gets the absolute minimum support, i.e. the minimum number of database
     * sequences where a pattern has to appear
     * @return the minimum support
     */
    public double getAbsoluteMinSupport() {
        return minSupAbsolute;
    }

    /**
     * It projects the initial database converting each original sequence to
     * pseudosequences in order to enable the later pseudoprojections in the
     * main loop of PrefixSpan
     * @param database The original Database
     * @param mapSequenceID Map with all the items appearing in the original 
     * database, and a bitset pointing out in which sequences the items appear
     * @param minSupportAbsolute The absolute minimum  support
     * @return 
     */
    private PseudoSequenceDatabase projectInitialDatabase(SequenceDatabase database, Map<Item, BitSet> mapSequenceID, long minSupportAbsolute) {
        PseudoSequenceDatabase initialContext = new PseudoSequenceDatabase();
        //For each database sequence 
        for (Sequence sequence : database.getSequences()) {
            //The new pseudosequences are optimized, since do not have the infrequent items
            Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minSupportAbsolute);
            if (optimizedSequence.size() != 0) {
                /*
                 * If after remove the infrequent items, we remove all the items 
                 * of an original sequence, we insert an empty pseudosequence 
                 * in order not to affect to the absolute minimum support
                 */
                PseudoSequence pseudoSequence = new PseudoSequence(0, optimizedSequence, 0, 0);
                initialContext.addSequence(pseudoSequence);
            }
        }
        return initialContext;
    }

    /**
     * It clears all the attributes of AlgoPrefixSpan class
     */
    public void clear() {
        if (originalDataset != null) {
            originalDataset.clear();
            originalDataset = null;
        }
        if (saver != null) {
            saver.clear();
            saver = null;
        }
        abstractionCreator = null;
    }
}
