package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan;

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
 * This is an implementation of the FEAT algorithm.
 * FEAT was proposed in a WWW2008 article by C. Gao, J. Wang, Y. He, L. Zhou <br/><br/>
 *
 * Copyright (c) 2008-2014 Philippe Fournier-Viger  <br/><br/>
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).  <br/><br/>
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. <br/><br/>
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. <br/><br/>
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */

public class AlgoFEAT{
		
	// for statistics
	long startTime;
	long endTime;

	// relative minimum support
	public int minsuppRelative;
	
	// The set of all sequential patterns that are found 
	private List<SequentialPattern> generators = null;
	
	// maximum pattern length in terms of item count
	private int maximumPatternLength = Integer.MAX_VALUE;
	
	//number of prefix pruned
	public int prefixPrunedCount = 0;
//	
//	// indicate if the pruning will be activated or not
//	private boolean performPruning = true;  // Note: set by runAlgorithm(..)
	
	// if enabled, the result will be verified to see if some patterns found are not generators.
	boolean DEBUG_MODE = false;  	
	
	// the initial database after removing infrequent items
	List<PseudoSequence> initialDatabase = null;
	
	boolean performPruning = true;
	
	/**
	 * Default constructor
	 */
	public AlgoFEAT(){
		
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
	public List<SequentialPattern> runAlgorithm(SequenceDatabase database, double minsupPercent, String outputFilePath) throws IOException {
		
		// convert to a relative minimum support
		this.minsuppRelative = (int) Math.ceil(minsupPercent* database.size());
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}
		
    	if(DEBUG_MODE){
    		System.out.println(" %%%%%%%%%%  DEBUG MODE %%%%%%%%%%");
    		System.out.println("minsup = " + minsuppRelative);
    	}
    	
		// record start time
		startTime = System.currentTimeMillis();
		
		// run the algorithm
		feat(database, outputFilePath);

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
	public List<SequentialPattern> runAlgorithm(SequenceDatabase database, String outputFilePath, int minsup) throws IOException {
    	if(DEBUG_MODE){
    		System.out.println(" %%%%%%%%%%  DEBUG MODE %%%%%%%%%%");
    	}

		// initialize variables for statistics
		MemoryLogger.getInstance().reset();
		// save the minsup chosen  by the user
		this.minsuppRelative = minsup;
		// save the start time
		startTime = System.currentTimeMillis();
		
		// run the algorithm
		feat(database, outputFilePath);
		
		// record end time
		endTime = System.currentTimeMillis();
		
		return generators;
	}

	/**
	 * Get the number of generator patterns found.
	 * Note that this method does not count the empty sequence.
	 * @return the number of generators.
	 */
	public long getPatternCount() {
		return generators.size();  // for the empty sequence
	}

	
	/**
	 * This methods checks if a seq. pattern "pattern2" is strictly contained in a seq. pattern "pattern1".
     * Note: we assume that pattern 2 is larger than pattern 1 (it contains more items).
     * @param pattern1 a sequential pattern
     * @param pattern2 another sequential pattern of larger size
	 * @return true if the pattern1 contains pattern2.
	 */
	boolean strictlyContains(SequentialPattern pattern1, SequentialPattern pattern2) {
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
	private void feat(SequenceDatabase database, String outputFilePath) throws IOException{
		// if the user want to keep the result into memory
		generators = new ArrayList<SequentialPattern>();
		
		// We have to scan the database to find all frequent sequential patterns of size 1.
		// We note the sequences in which the items appear.
		Map<Integer, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE ITON A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. 
		
		// Create a list of pseudosequence
		initialDatabase = new ArrayList<PseudoSequence>();
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


				// ================ SPECIFIC TO FEAT ================
				
				// build the projected database for that item
				List<PseudoSequence> projectedDatabase
				   = buildProjectedDatabaseForSingleItem(item, initialDatabase, entry.getValue());
				
				boolean canPrune = false;
				boolean isGenerator = true;
				
				if(initialDatabase.size() == entry.getValue().size()) {
					// check forward pruning
					canPrune = checkforwardPruningFor1ItemSequence(item, projectedDatabase);
					isGenerator = false;
				}
				
				// if we cannot prune, then we should check backwardPruning(newprefix, projectedDB, canprune, isgenerator)
				// For patterns of size 1, we don't need to check the backward pruning,
				// so we do nothing.

				// The prefix is a generator
				// We save it in the result.
				if(isGenerator) {
					savePattern(prefix); 
				}
				
				// We make a recursive call to try to find larger sequential
				// patterns starting with this prefix
				if((performPruning == false || !canPrune) && maximumPatternLength >1){
					featRecursion(prefix, projectedDatabase, 2); 
				}else {
					prefixPrunedCount++;
				}
				
				// ================ END OF SPECIFIC TO FEAT ================	
			}
		}		
	}
	
	/**
	 * This method checks the forward pruning for a sequence with a single item
	 * @param item the item.
	 * @param projectedDatabase the database that is projected with this item.
	 * @return true if this prefix can be pruned.
	 */
	private boolean checkforwardPruningFor1ItemSequence(Integer item,
			List<PseudoSequence> projectedDatabase) {
		// There is a forward extension if the item of the prefix appeared in the first
		// position of each sequence.
		
		// for each sequence
		for(PseudoSequence seq : projectedDatabase) {
			// we use the first item of the ORIGINAL sequence:
			Integer firstItem = seq.getOriginalSequence().get(0).get(0);
			// if not the same item
			if(!firstItem.equals(item)) {
				return false; // cannot prune
			}
		}
		return true; // can prune
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
		generators.add(prefix);
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
		
		// return the projected database
		return sequenceDatabase; 
	}

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param database The current sequence database.
	 * @param inPostFix This boolean indicates if the item "item" is part of a suffix or not.
	 * @param sidset the set of sequence IDs of sequence containing this item
	 * @return the projected database.
	 */
	private PairSequences buildProjectedDatabase(Integer item, List<PseudoSequence> database, Set<Integer> sidset, boolean inPostFix) {
		// We create a new projected database
		PairSequences sequenceDatabase = new PairSequences();

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
//						PairSequences pair = new PairSequences();
						sequenceDatabase.newSequences.add(new PseudoSequence( sequence, i+1, 0));
						sequenceDatabase.olderSequences.add(sequence);
//						sequenceDatabase.add(pair);
						//System.out.println(sequence.getId() + "--> "+ newSequence.toString());
//						break itemsetLoop;
					}
				}else{
					// create a new pseudo sequence and
					// add it to the projected database.
//					PairSequences pair = new PairSequences();
					sequenceDatabase.newSequences.add(new PseudoSequence(sequence, i, index+1));
					sequenceDatabase.olderSequences.add(sequence);
//					sequenceDatabase.add(pair);
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
	private void featRecursion(SequentialPattern prefix, List<PseudoSequence> database, int k) throws IOException {	
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

//				if(newPrefix.getItemsets().size() == 2 && newPrefix.getItemsets().get(0).get(0) == 1 
//						&& newPrefix.getItemsets().get(1).size() == 1 && newPrefix.getItemsets().get(1).get(0) == 4
////						&& newPrefix.getItemsets().get(2).size() == 1 && newPrefix.getItemsets().get(1).get(0) == 2
//						) {
//					System.out.println(pair.item + " " + pair.postfix);
//					System.out.println(newPrefix);
//					System.out.println();
//				}
				// ================ SPECIFIC TO FEAT ================
				
				// build the projected database with this item
				PairSequences projectedDatabase = buildProjectedDatabase(pair.getItem(), database, pair.getSequenceIDs(), pair.isPostfix());

				boolean canPrune = false;
				boolean isGenerator = true;
				
				if(prefix.getAbsoluteSupport() == pair.getSequenceIDs().size()) {
					// check forward pruning
					canPrune = checkForwardPruningGeneralCase(projectedDatabase, pair.getItem(), pair.isPostfix());
					isGenerator = false;
					System.out.println(prefix);
				}	
				
				if(!canPrune) { 
//					System.out.println(newPrefix);
					Boolean[] returnValues = checkBackwardPruning(newPrefix, projectedDatabase.newSequences, isGenerator);
					isGenerator = returnValues[0];
					canPrune = returnValues[1];
				}
				
				if(isGenerator) {
					savePattern(newPrefix); 
				}
				
				if((performPruning == false || !canPrune)  && k < maximumPatternLength){
					featRecursion(newPrefix, projectedDatabase.newSequences, k+1);
				}else {
					prefixPrunedCount++;
				}

				// ================ END OF SPECIFIC TO FEAT ================
				
			}
		}
		// check the current memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * Check if there is some backwardExtension.
	 * @param newPrefix  the prefix "newPrefix" used to make the database projection
	 * @param projectedDatabase  the  database projected by "newprefix"
	 * @return  an array of booleans indicating if (1) newPrefix is a generator and (2) if this prefix should be pruned
	 */
	private Boolean[] checkBackwardPruning(SequentialPattern newPrefix,
			List<PseudoSequence> projectedDatabase, boolean isGeneratorParameter) {
		
		// initialize variables for returning values
		boolean isGenerator = isGeneratorParameter;
		boolean canPrune = false;
		
		// calculate the size of this prefix
		int prefixTotalSize = newPrefix.getItemOccurencesTotalCount();
		
		// for each item j 
loop:	for(int j = 1; j < prefixTotalSize; j++) {
	
			// Create the truncated prefix
			List<List<Integer>> prefixTruncated = new ArrayList<List<Integer>>();
			int itemCounter = j;
			loop1:	for(Itemset itemsetPrefix : newPrefix.getItemsets()) {
					List<Integer> newItemset = new ArrayList<Integer>();
					prefixTruncated.add(newItemset);
					for(Integer currentItem : itemsetPrefix.getItems()) {
						newItemset.add(currentItem);
						itemCounter--;
						if(itemCounter < 0) {
							break loop1;
						}
					}
					
			}
			// for the i-th item of the newPrefix such that i is before j
			for(int i=0; i < j; i++) {		

				// variable to count the support of the prefix without i
				int supCount = 0;
				// variable to check if we should prune according to this prefix without i
				boolean localCanPrune = true;
				
				// variable to count the number of sequences remaining to be checked
				int seqRemaining = initialDatabase.size();
						
				// for each sequence of the original database
				for(PseudoSequence originalSequence: initialDatabase) {
					//  decrease the count of sequences remaining
					seqRemaining--;
					
					// we check if the prefix and the prefix without i have
					// the same projection or not
					ProjectionEnum result = sameProjection(originalSequence, prefixTruncated, i);
					
					// if the sequence contain the prefix without i, we increase its support
					if(result.equals(ProjectionEnum.SAME_PROJECTION) ||
							result.equals(ProjectionEnum.CONTAIN_PREFIX_WITHOUT_I)) {
						supCount++;
					}
					
					// if not the same projection, then we cannot prune
					if(!result.equals(ProjectionEnum.SAME_PROJECTION)
							&& !result.equals(ProjectionEnum.SAME_PROJECTION_NOT_CONTAINED_IN)) {
						// so we note that.
						localCanPrune = false;
						// Then, if we know that the prefix is not a generator, then we don't
						// need to count the support of prefix without i, so we break
						if(isGenerator == false) {
							break;
						}else if(supCount + seqRemaining < newPrefix.getAbsoluteSupport()){
							// this means that the support of prefix without i cannot
							// be the same as prefix
							break;
						}
					}
					
				}
				// if the projections are the same for all sequences
				if(localCanPrune == true) {
					canPrune = true;
					// if we have established that we can prune and this is not a generator, we
					// don't need to continue this loop
					if(canPrune == true && isGenerator == false) {
						break loop;
					}
				}
				if(supCount == newPrefix.getAbsoluteSupport()) {
					isGenerator = false;
				}
				
			}
		}
		
		
		// return values
		Boolean returnValues[] = new Boolean[2];
		returnValues[0] = isGenerator;
		returnValues[1] = canPrune;
		return returnValues;
	}

	/**
	 * Check if two prefix have the same projection for a given sequence.
	 * @param originalSequence  the sequence
	 * @param newPrefix  the original prefix    e1 e2 ... ei-1 ei ei+1...ej... en
	 * @param i   the item that should be ignored to generate the second prefix  e1 e2 ... ei-1  ei+1... ej
	 * @param j   the last item that should be considered for the first prefix  e1  e2 ...  ej
	 * @return true if they have the same projection,  false if not and null if the prefix without i
	 *   is not contained in the sequence
	 */
	private ProjectionEnum sameProjection(PseudoSequence originalSequence,
			List<List<Integer>> prefix, int i) {
		
		// CALCULATE THE PROJECTION WITHOUT I
		int projectionWithoutI = -1;
		int itemsetPos = 0;
		
		// determine item I or null if not in this itemset
		Integer itemI = null;
		if(i < prefix.get(itemsetPos).size()) {
			// if there is only a single item, we just move to the next itemset
			if(prefix.get(itemsetPos).size()==1) {
				itemsetPos++;
				i--;
			}else {
				// otherwise, there is more than one item so we set item I correctly
				itemI = prefix.get(itemsetPos).get(i);
			}
		}
		
		// for each itemset of the sequence
		for(int k = 0; k < originalSequence.size(); k++) {
			List<Integer> itemsetSequence = originalSequence.getItemset(k);	
			
			// check containment
			boolean contained = true;
			for(Integer item : prefix.get(itemsetPos)){
				if(item != itemI && !itemsetSequence.contains(item)) {
					contained = false;
					break;
				}
			}
			
			if(contained) {
				i-= prefix.get(itemsetPos).size();
				
				// move to next itemset
				itemsetPos++;
				
				if(itemsetPos == prefix.size()) {
					projectionWithoutI = k;
					break;	
				}
				
				// update item I
				itemI = null;
				if(i < prefix.get(itemsetPos).size() && i >=0) {
					// if there is only a single item, we just move to the next itemset
					if(prefix.get(itemsetPos).size()==1) {
						itemsetPos++;
						i--;
					}else {
						// otherwise, there is more than one item so we set item I correctly
						itemI = prefix.get(itemsetPos).get(i);
					}
				}
			}
		}
		
		// CALCULATE THE PROJECTION WITH I
		int projectionWithI = -1;
		itemsetPos = 0;
		
		// for each itemset of the sequence
		for(int k = 0; k < originalSequence.size(); k++) {
			List<Integer> itemsetSequence = originalSequence.getItemset(k);	
			
			// check containment
			boolean contained = true;
			for(Integer item : prefix.get(itemsetPos)){
				if(!itemsetSequence.contains(item)) {
					contained = false;
					break;
				}
			}
			
			if(contained) {
				// move to next itemset
				itemsetPos++;
				
				if(itemsetPos == prefix.size()) {
					projectionWithI = k;
					break;	
				}
			}
		}
		
		// if same projection
		if(projectionWithI == projectionWithoutI) {
			if(projectionWithI > 0) {
				return ProjectionEnum.SAME_PROJECTION;
			}else {
				return ProjectionEnum.SAME_PROJECTION_NOT_CONTAINED_IN;
			}
		}

		// if the prefix without i occurred in this sequence
		if(projectionWithoutI >=0) {
			return ProjectionEnum.CONTAIN_PREFIX_WITHOUT_I;
		}
		// should not occur
		return null;
	}
	
	// for the above method
	public enum ProjectionEnum {
	    SAME_PROJECTION, SAME_PROJECTION_NOT_CONTAINED_IN, CONTAIN_PREFIX_WITHOUT_I
	}

	/**
	 * Class to store the sequences from a previous projected database and the resulting 
	 * corresponding sequences after another projection.
	 */
	private class PairSequences{
		List<PseudoSequence> olderSequences = new ArrayList<PseudoSequence>();
		List<PseudoSequence> newSequences = new ArrayList<PseudoSequence>();
	}
	
	/**
	 * Check if a prefix "newprefix" should be pruned because of a forward extension.
	 * @param projectedDatabase  the database before projecting it with the last item of newprefix, and the newly projected database
	 * @param item  the last item
	 * @param postfix  if the lastitem is in a postfix or not
	 * @return true if the prefix should be pruned.
	 */
	private boolean checkForwardPruningGeneralCase(PairSequences projectedDatabase, Integer item, boolean postfix) {
		
		// for each sequence
		for(int i=0; i< projectedDatabase.newSequences.size(); i++) {
			PseudoSequence seq = projectedDatabase.newSequences.get(i);
			PseudoSequence seqProjected = projectedDatabase.olderSequences.get(i);
			
			// we need to check if there is exactly one less item in the
			// sequence projected by new prefix.
			// in this case it is a forward extension.
			
			// calculate the position of the next item following the sequence
			// projected by prefix.
			Integer firstItem = seq.getItemAtInItemsetAt(0, 0);
			
			// if the first item is not the one used to extend the prefix,
			// then there is an item between so this is not a forward extension
			if(!firstItem.equals(item)) {
				return false; // cannot prune
			}
			
			// calculate what is the next item position following the projection
			// by prefix in the original sequence
			int itemPos = seq.firstItem+1;
			int itemsetPos = seq.firstItemset;
			if(seq.getSizeOfItemsetAt(0) == itemPos) {
				itemPos = 0;
				itemsetPos++;
			}
			// if this position is different than the position projected by new prefix
			// than it is not a forward extension
			if(seqProjected.firstItem != itemPos ||
			    seqProjected.firstItemset != itemsetPos) {
				return false;
			}
		}
		return true; //  can prune
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
		r.append("=============  FEAT - STATISTICS =============\n Total time ~ ");
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
//		if(DEBUG_MODE) {
//			int patternCount = 0;
//			for(SequentialPattern sequence : generators){
//				patternCount++;
//				r.append("  pattern ");
//				r.append(patternCount);
//				r.append(":  ");
//				r.append(sequence.toString());
//				r.append("support :  ");
//				r.append(sequence.getRelativeSupportFormated(size));
//				r.append(" (" );
//				r.append(sequence.getAbsoluteSupport());
//				r.append('/');
//				r.append(size);
//				r.append("\n");
//			}
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
