package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import ca.pfv.spmf.patterns.itemset_list_integers_without_support.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;


/*** 
 * This is an implementation of the PrefixSpan algorithm.
 * PrefixSpan was proposed by Pei et al. 2001.
 * 
 * NOTE: This implementation saves the pattern  to a file as soon 
 * as they are found or can keep the pattern into memory, depending
 * on what the user choose.
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
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
 */

public class AlgoPrefixSpan{
		
	// for statistics
	long startTime;
	long endTime;
	
	// the number of pattern found
	int patternCount;
	
	// absolute minimum support
	private int minsuppAbsolute;

	// writer to write output file
	BufferedWriter writer = null;
	
	// The sequential patterns that are found 
	// (if the user want to keep them into memory)
	private SequentialPatterns patterns = null;
	
	// maximum pattern length in terms of item count
	private int maximumPatternLength = Integer.MAX_VALUE;
	
	/**
	 * Default constructor
	 */
	public AlgoPrefixSpan(){
	}
	
	/**
	 * Run the algorithm
	 * @param database : a sequence database
	 * @param minsupRelative  :  the minimum support as a percentage (e.g. 50%) as a value in [0,1]
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(SequenceDatabase database, double minsupRelative, String outputFilePath) throws IOException {
		// convert to a absolute minimum support
		this.minsuppAbsolute = (int) Math.ceil(minsupRelative * database.size());
		if(this.minsuppAbsolute == 0){ // protection
			this.minsuppAbsolute = 1;
		}
		// record start time
		startTime = System.currentTimeMillis();
		
		// run the algorithm
		prefixSpan(database, outputFilePath);
		
		// record end time
		endTime = System.currentTimeMillis();
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	
	/**
	 * Run the algorithm
	 * @param database : a sequence database
	 * @param minsupPercent  :  the minimum support as an integer
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null 
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(SequenceDatabase database, String outputFilePath, int minsup) throws IOException {
		// initialize variables for statistics
		patternCount =0;
		MemoryLogger.getInstance().reset();
		// save the minsup chosen  by the user
		this.minsuppAbsolute = minsup;
		// save the start time
		startTime = System.currentTimeMillis();
		
		// run the algorithm
		prefixSpan(database, outputFilePath);
		
		// save the end time
		endTime = System.currentTimeMillis();
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	
	/**
	 * This is the main method for the PrefixSpan algorithm that is called
	 * to start the algorithm
	 * @param outputFilePath  an output file path if the result should be saved to a file
	 *                        or null if the result should be saved to memory.
	 * @param database a sequence database
	 * @throws IOException exception if an error while writing the output file
	 */
	private void prefixSpan(SequenceDatabase database, String outputFilePath) throws IOException{
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			patterns = new SequentialPatterns("FREQUENT SEQUENTIAL PATTERNS");
		}else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		// We have to scan the database to find all frequent sequential patterns of size 1.
		// We note the sequences in which the items appear.
		Map<Integer, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE ITON A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. 
		
		// Create a list of pseudosequence
		List<PseudoSequence> initialDatabase = new ArrayList<PseudoSequence>();
		// for each sequence in  the database
		for(Sequence sequence : database.getSequences()){
			// remove infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppAbsolute);
			if(optimizedSequence.size() != 0){
				// if the size is > 0, create a pseudo sequence with this sequence
				initialDatabase.add(new PseudoSequence(optimizedSequence, 0, 0));
			}
		}
				
		// For each item
		for(Entry<Integer, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent  (has a support >= minsup)
			if(entry.getValue().size() >= minsuppAbsolute){ 
				Integer item = entry.getKey();
				
				// Create the prefix for this projected database
				SequentialPattern prefix = new SequentialPattern();  
				prefix.addItemset(new Itemset(item));
				prefix.setSequenceIDs(entry.getValue());

				// The prefix is a frequent sequential pattern.
				// We save it in the result.
				savePattern(prefix);  

				// build the projected database for that item
				List<PseudoSequence> projectedContext
				   = buildProjectedDatabaseForSingleItem(item, initialDatabase, entry.getValue());
		
				// We make a recursive call to try to find larger sequential
				// patterns starting with this prefix
				if(maximumPatternLength >1){
					recursion(prefix, projectedContext, 2); 
				}
			}
		}		
	}
	
	/**
	 * This method saves a sequential pattern to the output file or
	 * in memory, depending on if the user provided an output file path or not
	 * when he launched the algorithm
	 * @param prefix the pattern to be saved.
	 * @throws IOException exception if error while writing the output file.
	 */
	private void savePattern(SequentialPattern prefix) throws IOException {
		// increase the number of pattern found for statistics purposes
		patternCount++; 
	
		// if the result should be saved to a file
		if(writer != null){
			// create a stringbuffer
			StringBuffer r = new StringBuffer("");
			// for each itemset in this sequential pattern
			for(Itemset itemset : prefix.getItemsets()){
				// for each item
				for(Integer item : itemset.getItems()){
					r.append(item.toString()); // add the item
					r.append(' ');
				}
				r.append("-1 "); // add the itemset separator
			}		
			// add the support
			r.append("#SUP: ");
			r.append(prefix.getAbsoluteSupport());
			
			// write the string to the file
			writer.write(r.toString());
			// start a new line
			writer.newLine();
		}
		// otherwise the result is kept into memory
		else{
			patterns.addSequence(prefix, prefix.size());
		}

	}
	
	/**
	 * For each item, calculate the sequence id of sequences containing that item
	 * @param database the current sequence database
	 * @return Map of items to sequence IDs that contains each item
	 */
	private Map<Integer, Set<Integer>> findSequencesContainingItems(SequenceDatabase database) {
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
		Map<Integer, Set<Integer>> mapSequenceID = new HashMap<Integer, Set<Integer>>(); 
		// for each sequence in the current database
		for(Sequence sequence : database.getSequences()){
			// for each itemset in this sequence
			for(List<Integer> itemset : sequence.getItemsets()){
				// for each item
				for(Integer item : itemset){
					// get the set of sequence IDs for this item until now
					Set<Integer> sequenceIDs = mapSequenceID.get(item);
					if(sequenceIDs == null){
						// if the set does not exist, create one
						sequenceIDs = new HashSet<Integer>();
						mapSequenceID.put(item, sequenceIDs);
					}
					// add the sequence ID of the current sequence to the 
					// set of sequences IDs of this item
					sequenceIDs.add(sequence.getId());
				}
			}
		}
		return mapSequenceID;
	}
	
	/**
	 * Create a projected database by pseudo-projection with the initial database and a given item.
	 * @param item The item to use to make the pseudo-projection
	 * @param initialDatabase The current database.
	 * @param sidSet  The set of sequence ids containing the item
	 * @return the projected database.
	 */
	private List<PseudoSequence> buildProjectedDatabaseForSingleItem(Integer item, List<PseudoSequence> initialDatabase,Set<Integer> sidSet) {
		// We create a new projected database
		List<PseudoSequence> sequenceDatabase = new ArrayList<PseudoSequence>();

		// for each sequence in the database received as parameter
loopSeq:for(PseudoSequence sequence : initialDatabase){ 

			// if this sequence do not contain the current prefix, then skip it.
			if(!sidSet.contains(sequence.getId())){
				continue;
			}
			
			// for each itemset of the sequence
			for(int i = 0; i< sequence.size(); i++){

				// check if the itemset contains the item that is used for the projection
				int index = sequence.indexOfBis(i, item);
				// if it does not, and the current item is part of a suffix if inSuffix is true
				//   and vice-versa
				if(index == -1 ){
					continue;
				}
				
				// if the item is the last item of this itemset
				if(index == sequence.getSizeOfItemsetAt(i)-1){ 
					// if it is not the last itemset
					if ((i != sequence.size()-1)){
						// create new pseudo sequence
						// add it to the projected database.
						sequenceDatabase.add(new PseudoSequence( sequence, i+1, 0));
					}
				}else{
					// create a new pseudo sequence and
					// add it to the projected database.
					sequenceDatabase.add(new PseudoSequence(sequence, i, index+1));
				}

			}
		}
//
//		for(PseudoSequence seq : sequenceDatabase){
//			System.out.println(seq);
//			System.out.println("original seq: " + seq.sequence);
//		}
//		
		return sequenceDatabase; // return the projected database
	}

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param database The current sequence database.
	 * @param inPostFix This boolean indicates if the item "item" is part of a suffix or not.
	 * @param sidset the set of sequence IDs of sequence containing this item
	 * @return the projected database.
	 */
	private List<PseudoSequence> buildProjectedDatabase(Integer item, List<PseudoSequence> database, Set<Integer> sidset, boolean inPostFix) {
		// We create a new projected database
		List<PseudoSequence> sequenceDatabase = new ArrayList<PseudoSequence>();

		// for each sequence in the database received as parameter
		for(PseudoSequence sequence : database){ 
			
			if(sidset.contains(sequence.getId()) == false){
				continue;
			}
			
			// for each itemset of the sequence
			for(int i = 0; i< sequence.size(); i++){
	
				if (sequence.isPostfix(i) != inPostFix){
					// if the item is not in a postfix, but this itemset
					// is a postfix, then we can continue scanning from the next itemset
					continue;
				}

				// check if the itemset contains the item that we use for the projection
				int index = sequence.indexOfBis(i, item);
				
				// if it does not, move to next itemset
				if(index == -1 ){
					continue;
				}
				
				// if the item is the last item of this itemset
				if(index == sequence.getSizeOfItemsetAt(i)-1){ 
					// if it is not the last itemset
					if ((i != sequence.size()-1)){
						// create new pseudo sequence
						// add it to the projected database.
						sequenceDatabase.add(new PseudoSequence( sequence, i+1, 0));
						//System.out.println(sequence.getId() + "--> "+ newSequence.toString());
//						break itemsetLoop;
					}
				}else{
					// create a new pseudo sequence and
					// add it to the projected database.
					sequenceDatabase.add(new PseudoSequence(sequence, i, index+1));
					//System.out.println(sequence.getId() + "--> "+ newSequence.toString());
//					break itemsetLoop;
				}
			}
		}
		return sequenceDatabase; // return the projected database
	}
	
	/**
	 * Method to recursively grow a given sequential pattern.
	 * @param prefix  the current sequential pattern that we want to try to grow
	 * @param database the current projected sequence database
	 * @param k  the prefix length in terms of items
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private void recursion(SequentialPattern prefix, List<PseudoSequence> database, int k) throws IOException {	
		// find frequent items of size 1 in the current projected database.
		Set<Pair> pairs = findAllFrequentPairs(database);
	
		// For each pair found (a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(Pair pair : pairs){
			// if the item is frequent in the current projected database
			if(pair.getCount() >= minsuppAbsolute){
				// create the new postfix by appending this item to the prefix
				SequentialPattern newPrefix;
				// if the item is part of a postfix
				if(pair.isPostfix()){ 
					// we append it to the last itemset of the prefix
					newPrefix = appendItemToPrefixOfSequence(prefix, pair.getItem()); 
				}else{ // else, we append it as a new itemset to the sequence
					newPrefix = appendItemToSequence(prefix, pair.getItem());
				}
				newPrefix.setSequenceIDs(pair.getSequenceIDs()); 

				// build the projected database with this item
				List<PseudoSequence> projectedDatabase = buildProjectedDatabase(pair.getItem(), database, pair.getSequenceIDs(), pair.isPostfix());

				// save the pattern
				savePattern(newPrefix);
				// make a recursive call
				if( k < maximumPatternLength){
					recursion(newPrefix, projectedDatabase, k+1);
				}
			}
		}
		// check the current memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @return A list of pairs, where a pair is an item with (1) a boolean indicating if it
	 *         is in an itemset that is "cut" and (2) the sequence IDs where it occurs.
	 */
	protected Set<Pair> findAllFrequentPairs(List<PseudoSequence> sequences){
		// We use a Map the store the pairs.
		Map<Pair, Pair> mapPairs = new HashMap<Pair, Pair>();
		// for each sequence
		for(PseudoSequence sequence : sequences){
			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					Integer item = sequence.getItemAtInItemsetAt(j, i);
					// create the pair corresponding to this item
					Pair pair = new Pair(sequence.isPostfix(i), item);   
					// get the pair object store in the map if there is one already
					Pair oldPair = mapPairs.get(pair);
					// if there is no pair object yet
					if(oldPair == null){
						// store the pair object that we created
						mapPairs.put(pair, pair);
					}else{
						// otherwise use the old one
						pair = oldPair;
					}
					// record the current sequence id for that pair
					pair.getSequenceIDs().add(sequence.getId());
				}
			}
		}
		MemoryLogger.getInstance().checkMemory();  // check the memory for statistics.
		// return the map of pairs
		return mapPairs.keySet();
	}

	/**
	 *  This method creates a copy of the sequence and add a given item 
	 *  as a new itemset to the sequence. 
	 *  It sets the support of the sequence as the support of the item.
	 * @param prefix  the sequence
	 * @param item the item
	 * @return the new sequence
	 */
	private SequentialPattern appendItemToSequence(SequentialPattern prefix, Integer item) {
		SequentialPattern newPrefix = prefix.cloneSequence();  // isSuffix
		newPrefix.addItemset(new Itemset(item));  // cr�� un nouvel itemset   + decalage
		return newPrefix;
	}
	
	/**
	 *  This method creates a copy of the sequence and add a given item 
	 *  to the last itemset of the sequence. 
	 *  It sets the support of the sequence as the support of the item.
	 * @param prefix  the sequence
	 * @param item the item
	 * @return the new sequence
	 */
	private SequentialPattern appendItemToPrefixOfSequence(SequentialPattern prefix, Integer item) {
		SequentialPattern newPrefix = prefix.cloneSequence();
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  // ajoute au dernier itemset
		itemset.addItem(item);  
		return newPrefix;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 */
	public void printStatistics(int size) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  PREFIXSPAN - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + patternCount);
		r.append('\n');
		r.append(" Max memory (mb) : " );
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append(patternCount);
		r.append('\n');
		r.append("===================================================\n");
		// if the result was save into memory, print it
		if(patterns !=null){
			patterns.printFrequentPatterns(size);
		}
		System.out.println(r.toString());
	}
	
	/**
	 * Get the maximum length of patterns to be found (in terms of item count)
	 * @return the maximumPatternLength
	 */
	public int getMaximumPatternLength() {
		return maximumPatternLength;
	}

	/**
	 * Set the maximum length of patterns to be found (in terms of item count)
	 * @param maximumPatternLength the maximumPatternLength to set
	 */
	public void setMaximumPatternLength(int maximumPatternLength) {
		this.maximumPatternLength = maximumPatternLength;
	}

}
