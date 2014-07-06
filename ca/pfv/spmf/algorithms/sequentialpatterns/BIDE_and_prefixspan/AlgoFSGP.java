package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

import java.io.BufferedWriter;
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
 * This is an implementation of the FSGP algorithm.
 * FSGP is an algorithm proposed by Yi et al (2011-2012)
 *
 * Copyright (c) 2008-2014 Philippe Fournier-Viger
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

public class AlgoFSGP{
		
	// for statistics
	long startTime;
	long endTime;

	
	// relative minimum support
	public int minsuppRelative;

	// writer to write output file
	BufferedWriter writer = null;
	
	// The set of all sequential patterns that are found 
	private SequentialPatterns patterns = null;
	List<SequentialPattern> generators =  null;  // NOTE : DOES NOT INCLUDE EMPTY SEQUENCE
	
	// maximum pattern length in terms of item count
	private int maximumPatternLength = Integer.MAX_VALUE;
	
	//number of prefix pruned
	public int prefixPrunedCount = 0;
	
	// indicate if the pruning will be activated or not
	private boolean performPruning = true;  // Note: set by runAlgorithm(..)
	
	// if enabled, the result will be verified to see if some patterns found are not generators.
	boolean DEBUG_MODE = true;  	
	
	/**
	 * Default constructor
	 */
	public AlgoFSGP(){
	}
	
	/**
	 * Run the algorithm
	 * @param database : a sequence database
	 * @param minsupPercent  :  the minimum support as a percentage (e.g. 50%)
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null
	 * @throws IOException  exception if error while writing the file
	 */
	public List<SequentialPattern> runAlgorithm(SequenceDatabase database, double minsupPercent, String outputFilePath, boolean performPruning) throws IOException {
    	if(DEBUG_MODE){
    		System.out.println(" %%%%%%%%%%  DEBUG MODE %%%%%%%%%%");
    	}
    	
    	this.performPruning = performPruning;
		
		// convert to a relative minimum support
		this.minsuppRelative = (int) Math.ceil(minsupPercent* database.size());
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}
		// record start time
		startTime = System.currentTimeMillis();
		
		// run the algorithm
		fsgp(database, outputFilePath);
		
		// filter non generator patterns
		filterNonGenerator(database);  // GeneratorCheck(...)
		
		// record end time
		endTime = System.currentTimeMillis();
		
		// ################################## FOR DEBUGGGING #############################
        // ########  THIS CODE CHECK IF A PATTERN FOUND IS NOT A GENERATOR ##############
        if(DEBUG_MODE) {
        	// CHECK IF SOME PATTERNS ARE NOTE GENERATORS
        	for(SequentialPattern pat1 : generators) {
        		// if this pattern is not the empty set and the support is same as empty set, then it is not a generator
        		if(pat1.size() > 0 && pat1.getAbsoluteSupport() == database.size()) {
        			System.out.println("NOT A GENERATOR !!!!!!!!!  "  + pat1 + "    sup: " + pat1.getAbsoluteSupport() + " because of empty set");
        		}
        		
        		// otherwise we have to compare with every other patterns.
        		for(SequentialPattern pat2 : generators) {
            		if(pat1 == pat2) {
            			continue;
            		}
            		
            		if(pat1.getAbsoluteSupport() == pat2.getAbsoluteSupport()) {
            			if(strictlyContains(pat1, pat2)) {
            				System.out.println("NOT A GENERATOR !!!!!!!!!  "  + pat1 + " " + pat2 + "   sup: " + pat1.getAbsoluteSupport());
                			System.out.println(pat1.getAbsoluteSupport() + " " +  pat2.getAbsoluteSupport());
            			}
            		}
            	}
        	}
        	// PRINT ALL THE PATTERNS
	        for(SequentialPattern pattern : generators) {
	        	System.out.println(pattern + " #SUP: " + pattern.getAbsoluteSupport());
	        }
	        // ############################ END OF DEBUGGING CODE ################################
        }
        
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
       
		
		return generators;
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
	public List<SequentialPattern> runAlgorithm(SequenceDatabase database, String outputFilePath, int minsup, boolean performPruning) throws IOException {
    	if(DEBUG_MODE){
    		System.out.println(" %%%%%%%%%%  DEBUG MODE %%%%%%%%%%");
    	}

    	this.performPruning = performPruning;
		
		// initialize variables for statistics
		MemoryLogger.getInstance().reset();
		// save the minsup chosen  by the user
		this.minsuppRelative = minsup;
		// save the start time
		startTime = System.currentTimeMillis();
		
		// run the algorithm
		fsgp(database, outputFilePath);
				
		// filter non generator patterns
		filterNonGenerator(database);  // GeneratorCheck(...)
		
		// record end time
		endTime = System.currentTimeMillis();
		
		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		
		return generators;
	}
	
	
	/**
	 * This method remove all the patterns that are not generators from the set of patterns found.
	 * @param database 
	 */
	private List<SequentialPattern> filterNonGenerator(SequenceDatabase database) {
		int emptySequenceSupport = database.size();
		
		// create a new list to store generators
		generators = new ArrayList<SequentialPattern>();
		
		// THERE ARE TWO VERSIONS OF THE STEP FOR FILTERING NON GENERATOR
		// DEPENDING ON IF THE PREFIX PRUNING STRATEGY IS ACTIVATED OR NOT
		if(performPruning) {
			// IF THE PRUNING IS  ACTIVATED, WE  NEED TO COMPARE EACH PATTERN OF SIZE i  WITH
			// EVERY OTHER PATTERNS OF SIZE < i WITH THE SAME SUPPORT
			
			// for patterns of size i=0 to the maximum size
			for(int i=1; i< patterns.levels.size(); i++){
				// for each pattern of size i
	patLoop: for(SequentialPattern pattern : patterns.levels.get(i)) {
		

					if(pattern.getItemsets().size() ==1 && pattern.get(0).size() == 3 && pattern.get(0).get(0) == 1
							&& pattern.get(0).get(1) == 2 && pattern.get(0).get(2) == 3) {
						System.out.println("TEST");
					}
					
					//if the pattern has size 0, if the pattern has the same support
					// as the empty sequence, it is not a generator, so we remove it.
					if(pattern.getAbsoluteSupport() == emptySequenceSupport) {
						continue patLoop;
					}else {
						// if there is such a sub-pattern of the current pattern with size < i with the same support
						// then the current pattern is not a generator
						for(int j = 1; j < i; j++) {
							List<SequentialPattern> levelJ = patterns.levels.get(j);
							
							for(SequentialPattern pattern2 : levelJ) {
								if(pattern2.getAbsoluteSupport() == pattern.getAbsoluteSupport() 
										&& strictlyContains(pattern, pattern2)) {
									continue patLoop;
								}
							}
						}
						
					}
					// if the pattern is a generator
					generators.add(pattern);
				}
			}
			
		}else {
			// IF THE PRUNING IS NOT ACTIVATED, WE ONLY NEED TO COMPARE EACH PATTERN OF SIZE i WITH
			// PATTERNS OF SIZE i-1 WITH THE SAME SUPPORT
			
			// for patterns of size i=0 to the maximum size
			for(int i=1; i< patterns.levels.size(); i++){
				// for each pattern of size i
	patLoop: for(SequentialPattern pattern : patterns.levels.get(i)) {
					//if the pattern has size 0, if the pattern has the same support
					// as the empty sequence, it is not a generator, so we remove it.
					if(pattern.getAbsoluteSupport() == emptySequenceSupport) {
						continue patLoop;
					}else {
						// if there is such a sub-pattern ofthe current pattern with size i-1 with the same support
						// then the current pattern is not a generator
						for(SequentialPattern pattern2 : patterns.levels.get(i-1)) {
							if(pattern2.getAbsoluteSupport() == pattern.getAbsoluteSupport() 
									&& strictlyContains(pattern, pattern2)) {
								continue patLoop;
							}
						}
					}
					// if the pattern is a generator
					generators.add(pattern);
				}
			}
		}
		
		
		
		
		// add the empty sequence   ############################# TODO
		//########################### TODO ####################
//		SequentialPattern emptySequence = new SequentialPattern();
//		emptySequence.setSequenceIDs(sequencesIds)
//		generators.add(emptySequence);
		return generators;
	}
	

	public long getPatternCount() {
		return generators.size();  // for the empty sequence
	}

	
	/**
	 * Check the pruning property to prune the search space, for the current prefix (pattern).
	 * @param pattern the current prefix.
	 * @param projectedDatabase the projected database for the current prefix.
	 * @return true if the prefix should NOT be pruned
	 */
	private boolean pruningCheck(SequentialPattern pattern, List<PseudoSequence> projectedDatabase) {
		// If this pattern is of size i
		int i = pattern.size();
		// we need to compare this pattern with every pattern of size i -1 
loop: for(SequentialPattern pattern2 : patterns.levels.get(i-1)) {
			// if there is such a pattern of size i-1, 
			// then we remove the current pattern of size i because it is not a generator
			if(pattern2.getAbsoluteSupport() == pattern.getAbsoluteSupport() 
					&& strictlyContains(pattern, pattern2)) {
				// we need to compare the projected databases
				for(PseudoSequence pseudoSeq : projectedDatabase) {
					Sequence originalSequence = pseudoSeq.getOriginalSequence();
					if(sameProjection(originalSequence, pattern, pattern2) == false){
						continue loop;
					}
				}
				// if the projected databases are the same
				return false;
			}
		}
		return true; // passed the pruning check
	}

	/**
	 * Check if two patterns have the same projection for a given sequence.
	 * @param originalSequence  the sequence
	 * @param pattern1 the first pattern
	 * @param pattern2 the second pattern, which is a sub-pattern of the first one
	 * @return true if the patterns have the same pseudo-projection.
	 */
	private boolean sameProjection(Sequence originalSequence,
			SequentialPattern pattern1, SequentialPattern pattern2) {
		int pat1itemsetPos = 0;
		int pat1itemPos = 0;
		int pat2itemsetPos = 0;
		int pat2itemPos = 0;  
		for(List<Integer> itemset : originalSequence.getItemsets()) {
			for(Integer item: itemset) {
				// if match pattern 1
				if(item.intValue() == pattern1.getItemsets().get(pat1itemsetPos).get(pat1itemPos)) {
					pat1itemPos++;
					if(pattern1.getItemsets().get(pat1itemsetPos).size() == pat1itemPos) {
						pat1itemsetPos++;
						pat1itemPos = 0;
					}
				}
				// if match pattern 2
				if(item.intValue() == pattern2.getItemsets().get(pat2itemsetPos).get(pat2itemPos)) {
					pat2itemPos++;
					if(pattern2.getItemsets().get(pat2itemsetPos).size() == pat2itemPos) {
						pat2itemsetPos++;
						pat2itemPos = 0;
					}
				}
				// if completely match pattern 2 then it should also have matched completely pattern 1
				if(pat2itemsetPos == pattern2.getItemsets().size()) {
					// if completely matched pattern 1
					if(pat1itemsetPos == pattern1.getItemsets().size()) {
						// same projected sequence
						return true;
					}else {
						return false;
					}
				}
			}
		}
		// should not happen
		System.out.println("This should never happen");
		return false;
	}
	
	/**
	 * This methods checks if a seq. pattern "pattern2" is strictly contained in a seq. pattern "pattern1".
     * @param pattern1 a sequential pattern
     * @param pattern2 another sequential pattern
	 * @return true if the pattern1 contains pattern2.
	 */
	boolean strictlyContains(SequentialPattern pattern1, SequentialPattern pattern2) {
//		// if pattern2 is larger or equal in size, then it cannot be contained in pattern1
//		if(pattern1.size() <= pattern2.size()){
//			return false;
//		} 
		
		// To see if pattern2 is strictly contained in pattern1,
		// we will search for each itemset i of pattern2 in pattern1 by advancing
		// in pattern 1 one itemset at a time.
		
		int i =0; // position in pattern2
		int j= 0; // position in pattern1
		while(true){
			//if the itemset at current position in pattern1 contains the itemset
			// at current position in pattern2
			if(pattern1.get(j).containsAll(pattern2.get(i))){
				// go to next itemset in pattern2
				i++;
				
				// if we reached the end of pattern2, then return true
				if(i == pattern2.size()){
					return true;
				}
			}
				
			// go to next itemset in pattern1
			j++;
			
			// if we reached the end of pattern1, then pattern2 is not strictly included
			// in it, and return false
			if(j >= pattern1.size()){
				return false;
			}
			
//			// lastly, for optimization, we check how many itemsets are left to be matched.
//			// if there is less itemsets left in pattern1 than in pattern2, then it will
//			// be impossible to get a total match, and so we return false.
			if((pattern1.size() - j) < pattern2.size()  - i){
				return false;
			}
		}
	}


	
	/**
	 * This is the main method for the PrefixSpan algorithm that is called
	 * to start the algorithm
	 * @param outputFilePath  an output file path if the result should be saved to a file
	 *                        or null if the result should be saved to memory.
	 * @param database a sequence database
	 * @throws IOException exception if an error while writing the output file
	 */
	private void fsgp(SequenceDatabase database, String outputFilePath) throws IOException{
		// if the user want to keep the result into memory
		patterns = new SequentialPatterns("SEQUENTIAL GENERATOR PATTERNS");
//		if(outputFilePath == null){
			writer = null;
//		}else{ // if the user want to save the result to a file
//			patterns = null;
//			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
//		}
		
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
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			if(optimizedSequence.size() != 0){
				// if the size is > 0, create a pseudo sequence with this sequence
				initialDatabase.add(new PseudoSequence(optimizedSequence, 0, 0));
			}
		}
				
		// For each item
		for(Entry<Integer, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent  (has a support >= minsup)
			if(entry.getValue().size() >= minsuppRelative){ 
				Integer item = entry.getKey();
				
				// Create the prefix for this projected database
				SequentialPattern prefix = new SequentialPattern();  
				prefix.addItemset(new Itemset(item));
				prefix.setSequenceIDs(entry.getValue());

				// The prefix is a frequent sequential pattern.
				// We save it in the result.
				savePattern(prefix, 1);  

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
	private void savePattern(SequentialPattern prefix, int itemCount) throws IOException {
		// increase the number of pattern found for statistics purposes
		patterns.addSequence(prefix, itemCount);

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
		for(PseudoSequence sequence : initialDatabase){ 

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
			if(pair.getCount() >= minsuppRelative){
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

				// check if this prefix should be pruned
				boolean passedPruningCheck = !performPruning  || pruningCheck(newPrefix, projectedDatabase);
				// if it should not be pruned
				if(passedPruningCheck) {
					// save the prefix
					savePattern(newPrefix, k);
					// make a recursive call
					if( k < maximumPatternLength){
						recursion(newPrefix, projectedDatabase, k+1);
					}
				}else {
					prefixPrunedCount++;
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
		newPrefix.addItemset(new Itemset(item));  
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
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  // add to the last itemset
		itemset.addItem(item);  
		return newPrefix;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 */
	public void printStatistics(int size) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  FSGP - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + getPatternCount());
		r.append(" + 1 (the empty sequence) ");
		r.append('\n');
		r.append(" Max memory (mb) : " );
		r.append(" Prefix pruned count: " + prefixPrunedCount);
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append('\n');
		r.append("===================================================\n");
		// print the result
//		int patternCount = 0;
//		for(SequentialPattern sequence : generators){
//			patternCount++;
//			r.append("  pattern ");
//			r.append(patternCount);
//			r.append(":  ");
//			r.append(sequence.toString());
//			r.append("support :  ");
//			r.append(sequence.getRelativeSupportFormated(size));
//			r.append(" (" );
//			r.append(sequence.getAbsoluteSupport());
//			r.append('/');
//			r.append(size);
//			r.append("\n");
//		}
		
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
