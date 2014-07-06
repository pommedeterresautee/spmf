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


/**
 * This is the MaxSP algorithm that discovers
 * the set of maximal sequential patterns instead of the set of all
 * closed sequential patterns. It is described in the following paper:
 * 
 * Fournier-Viger, P., Wu, C.-W., Tseng, V.-S. (2013). Mining Maximal Sequential 
 * Patterns without Candidate Maintenance. Proc. 9th International Conference 
 * on Advanced Data Mining and Applications (ADMA 2013) Part I, Springer 
 * LNAI 8346, pp. 169-180. 
 *  * 
 * Copyright (c) 2008-2013 Philippe Fournier-Viger
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
public class AlgoMaxSP {
	
	// for statistics
	long startTime;
	long endTime;
	
	// the number of patterns found
	int patternCount = 0;
		
	// absolute minimum support
	private int minsuppAbsolute;
	
	// object to write the file
	BufferedWriter writer = null;
	
	// we have to keep a pointer to the original database
	private Map<Integer, PseudoSequenceBIDE> initialDatabase = null;  // sid, sequence
	
	// The sequential patterns that are found 
	// (if the user want to keep them into memory)
	private SequentialPatterns patterns = null;
//	
//
//	long debugFrequentPairsTime =0;
//	long debugithPeriodTime =0;
//	long debugAddPairTime =0;
//	long debugProjectDBTime =0;
		
	/**
	 * Default constructor
	 */
	public AlgoMaxSP(){
	}

	/**
	 * Run the algorithm
	 * @param database a sequence database
	 * @param outputPath an output file path
	 * @param minsup a minimum support as an integer representing a number of sequences
	 * @return return the result, if saved into memory, otherwise null
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(SequenceDatabase database, String outputPath, int minsup) throws IOException {

		// save minsup
		this.minsuppAbsolute = minsup;
		// reset the counter for the number of patterns found
		patternCount = 0; 
		// reset the stats about memory usage
		MemoryLogger.getInstance().reset();
		// save the start time
		startTime = System.currentTimeMillis();
		// start the algorithm
		maxSP(database, outputPath);
		// save the end time
		endTime = System.currentTimeMillis();

		// close the output file if the result was saved to a file
		if(writer != null){
			writer.close();
		}
		return patterns;
	}
	
	/**
	 * This is the main method for the MaxSP algorithm.
	 * @param database a sequence database
	 * @throws IOException exception if some error occurs while writing the output file.
	 */
	private void maxSP(SequenceDatabase database, String outputFilePath) throws IOException{
		// if the user want to keep the result into memory
		if(outputFilePath == null){
			writer = null;
			patterns = new SequentialPatterns("FREQUENT SEQUENTIAL PATTERNS");
		}else{ // if the user want to save the result to a file
			patterns = null;
			writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		}
		
		// The algorithm first scan the database to find all frequent items 
		// The algorithm note the sequences in which these items appear.
		// This is stored in a map:  Key: item  Value : IDs of sequences containing the item
		Map<Integer, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE TO A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE.

		// we create a database
		initialDatabase = new HashMap<Integer, PseudoSequenceBIDE>();
		// for each sequence of the original database
		for(Sequence sequence : database.getSequences()){
			// we make a copy of the sequence while removing infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppAbsolute);
			if(optimizedSequence.size() != 0){
				// if this sequence has size >0, we add it to the new database
				initialDatabase.put(sequence.getId(), new PseudoSequenceBIDE(optimizedSequence, 0, 0));
			}
			
		}
			
		
		// For each frequent item
loop1:	for(Entry<Integer, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent
			if(entry.getValue().size() >= minsuppAbsolute){ 
	
				// build the projected database with this item
				Integer item = entry.getKey();
				List<PseudoSequenceBIDE> projectedContext = buildProjectedContextSingleItem(item, initialDatabase,  false, entry.getValue());

				// Create the prefix with this item
				SequentialPattern prefix = new SequentialPattern();  
				prefix.addItemset(new Itemset(item));
				// set the sequence IDS of this prefix
				prefix.setSequenceIDs(entry.getValue());
				
				// variable to store the largest support of patterns
				// that will be found starting with this prefix
				if(projectedContext.size() >= minsuppAbsolute) {
					int successorSupport = 0;
					
//					if(!checkBackScanPruning(prefix, entry.getValue())) {
						successorSupport =  recursion(prefix, projectedContext); // récursion;
//					}
//					else
//					{
//						System.out.println("back scan pruned " + prefix);
//					}
					
					// Finally, because this prefix has support > minsup
					// and passed the backscan pruning,
					// we check if it has no sucessor with support >= minsup
					// (a forward extension)
					// IF no forward extension
					if(successorSupport < minsuppAbsolute){    // ######### MODIFICATION ####
						// IF there is also no backward extension
						if(!checkBackwardExtension(prefix, entry.getValue())){ 
							// the pattern is closed and we save it
							savePattern(prefix);  
						}
					}
				}else {
					if(!checkBackwardExtension(prefix, entry.getValue())){ 
						// the pattern is closed and we save it
						savePattern(prefix);  
					}
				}

				
			}
		}		
		// check the memory usage for statistics
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * Method to check if a prefix has a backward-extension (see Bide+ article for full details).
	 * This method do it a little bit differently than the BIDE+ article since
	 * we iterate with i on elements of the prefix instead of iterating with 
	 * a i on the itemsets of the prefix. But the idea is the same!
	 * @param prefix the current prefix
	 * @return boolean true, if there is a backward extension
	 */
	private boolean checkBackwardExtension(SequentialPattern prefix, Set<Integer> sidset) {	
//		System.out.println("======" + prefix);

		int totalOccurenceCount = prefix.getItemOccurencesTotalCount();
		// For the ith item of the prefix
		for(int i=0; i< totalOccurenceCount; i++){
			
			Set<Integer> alreadyVisitedSID = new HashSet<Integer>();
			
//			// SOME CODE USED BY "findAllFrequentPairsForBackwardExtensionCheck"
			Integer itemI = prefix.getIthItem(i);  // iPeriod 
			Integer itemIm1 = null;  // iPeriod -1
			if(i > 0){ 
				itemIm1 = prefix.getIthItem(i -1);	
			}
//			// END NEW

			// Create a Map of pairs to count the support of items (represented by a pair) 
			// in the ith semi-maximum periods
			Map<PairBIDE, PairBIDE> mapPaires = new HashMap<PairBIDE, PairBIDE>();
			
			// (1) For each i, we build the list of maximum periods
			// for each sequence in the original database
//			int seqCount =0;
			int highestSupportUntilNow = -1;
			
			// 1703 pat -  9391 ms
			for(int sequenceID : sidset){
				// OPTIMIZATION  PART 1==  DON'T CHECK THE BACK EXTENSION IF THERE IS NOT ENOUGH SEQUENCE LEFT TO FIND AN EXTENSION
				// THIS CAN IMPROVE THE PERFORMANCE BY UP TO 30% on FIFA
				if(highestSupportUntilNow != -1 && highestSupportUntilNow + (sidset.size() - alreadyVisitedSID.size()) < minsuppAbsolute) {
					break;
				}

				alreadyVisitedSID.add(sequenceID);
				// END OF OPTIMIZATION PART 1 (IT CONTINUES A FEW LINES BELOW...)
				
				PseudoSequenceBIDE sequence = initialDatabase.get(sequenceID);
				
				PseudoSequenceBIDE period = sequence.getIthMaximumPeriodOfAPrefix(prefix.getItemsets(), i);
		
				// if the period is not null
				if(period !=null){

					boolean hasBackwardExtension = findAllFrequentPairsForBackwardExtensionCheck(alreadyVisitedSID.size(), prefix, period, i, mapPaires, itemI, itemIm1);

					if(hasBackwardExtension) {
//						System.out.println(prefix + " has a backward extension from " + i + "th maxperiod  in sequence from seq. " + sequenceID );
						return true;
					}
					//===== OPTIMIZATION PART 2
					if((sidset.size() - alreadyVisitedSID.size()) < minsuppAbsolute){
						for(PairBIDE pair : mapPaires.values()) {
							int supportOfPair = pair.getSequenceIDs().size();
		
							if(supportOfPair > highestSupportUntilNow) {
								highestSupportUntilNow = supportOfPair; // +1 because it may be raised for this sequence...
							}
						}
						
					}
					//===== END OF OPTIMIZATION PART 2
				}
				
				
			}
			

		}
//		totaltimeForBackwardExtension += System.currentTimeMillis() - start;
		return false; // no backward extension, we return false
	} 
	
//	/**
//	 * This is the "backscan-pruning" strategy described in the BIDE+
//	 * paper to avoid extending some prefixs that are guaranteed to not
//	 * generate a closed pattern (see the BIDE+ paper for details).
//	 * 
//	 * @param prefix the current prefix
//	 * @return boolean true if we should not extend the prefix
//	 */
//	private boolean checkBackScanPruning(SequentialPattern prefix, Set<Integer> sidset) {	
////		
//		// See the BIDE+ paper for details about this method.
//		// For the number of item occurences that can be generated with this prefix:
//		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
//			
//			Set<Integer> alreadyVisitedSID = new HashSet<Integer>();
//
//			// Create a Map of pairs to count the support of items (represented by a pair) 
//			// in the ith semi-maximum periods
//			Map<PairBIDE, PairBIDE> mapPaires = new HashMap<PairBIDE, PairBIDE>();
//			
//			// SOME CODE USED BY "findAllFrequentPairsForBackwardExtensionCheck"
//			Integer itemI = prefix.getIthItem(i);  // iPeriod 
//			Integer itemIm1 = null;  // iPeriod -1
//			if(i > 0){ 
//				itemIm1 = prefix.getIthItem(i -1);	
//			}
////						// END NEW
//			
//			int seqCount =0;
////			int highestSupportUntilNow = -1;
//			
//			// (1) For each i, we build the list of maximum periods
//			// for each sequence in the original database
//			for(int sequenceID : sidset){
//
//				alreadyVisitedSID.add(sequenceID);
//				
//				PseudoSequenceBIDE sequence = initialDatabase.get(sequenceID);
//				PseudoSequenceBIDE period = sequence.getIthSemiMaximumPeriodOfAPrefix(prefix.getItemsets(), i);
//				
//				
//				if(period !=null){
////					// we add it to the list of maximum periods
//					boolean hasExtension = findAllFrequentPairsForBackwardExtensionCheck(alreadyVisitedSID.size(), prefix, period, i, mapPaires, itemI, itemIm1);
//					if(hasExtension) {
//						return true;
//					}
//				}
//			}
//		}
//		return false;
//	}
	
	/**
	 * Method to update the support count of item in a maximum period
	 * @param prefix the current prefix
	 * @param mapPaires 
	 * @param maximum periods  a maximum period
	 * @return a set of pairs indicating the support of items (note that a pair distinguish
	 *         between items in a postfix, prefix...).
	 */
	protected boolean findAllFrequentPairsForBackwardExtensionCheck(int seqProcessedCount,
			SequentialPattern prefix, PseudoSequenceBIDE maximumPeriod, int iPeriod, Map<PairBIDE, PairBIDE> mapPaires, Integer itemI, Integer itemIm1) {

		int maxPeriodSize = maximumPeriod.size();
		
			// for each itemset in that period
		for(int i=0; i< maxPeriodSize; i++){
			int sizeOfItemsetAtI = maximumPeriod.getSizeOfItemsetAt(i);
			
			// NEW
			boolean sawI = false;  // sawI after current position
			boolean sawIm1 = false; // sawI-1 before current position
			// END NEW
			
			// NEW march 20 2010 : check if I is after current position in current itemset
			for(int j=0; j < sizeOfItemsetAtI; j++){
				Integer item = maximumPeriod.getItemAtInItemsetAt(j, i);
				if(item.equals(itemI)){
					sawI = true; 
				}else if (item > itemI){
					break;
				}
			}
			// END NEW
			
			for(int j=0; j < sizeOfItemsetAtI; j++){
				Integer item = maximumPeriod.getItemAtInItemsetAt(j, i);
	
				if(itemIm1 != null && item == itemIm1){
					sawIm1 = true;
				}
				
				boolean isPrefix = maximumPeriod.isCutAtRight(i);
				boolean isPostfix = maximumPeriod.isPostfix(i);
				// END NEW

				PairBIDE paire = new PairBIDE(isPrefix, isPostfix, item);  
				if(seqProcessedCount >= minsuppAbsolute) {
					// $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
					// normal case  
					if(addPair(mapPaires, maximumPeriod.getId(),
							paire)) {
						return true;
					}
					
					// NEW: special cases
					if(sawIm1){
						PairBIDE paire2 = new PairBIDE(isPrefix, !isPostfix, item);  
						if(addPair(mapPaires,  maximumPeriod.getId(),
								paire2)) {
							return true;
						}
					}

					if(sawI ){  
						PairBIDE paire2 = new PairBIDE(!isPrefix, isPostfix, item);  
						if(addPair(mapPaires,  maximumPeriod.getId(),
								paire2)) {
							return true;
						}
					}
					// END NEW
				}else { // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
					// normal case
					addPairWithoutCheck(mapPaires, maximumPeriod.getId(), paire);
					
//					// NEW: special cases
					if(sawIm1){
						PairBIDE paire2 = new PairBIDE(isPrefix, !isPostfix, item);  
						addPairWithoutCheck(mapPaires,  maximumPeriod.getId(),
								paire2);
					}

					if(sawI ){  
						PairBIDE paire2 = new PairBIDE(!isPrefix, isPostfix, item);  
						addPairWithoutCheck(mapPaires,  maximumPeriod.getId(),
								paire2);
					}
					// END NEW
				}

				
			}
		}
		return false; 
	}


	/**
	 * Add a pair to the map of pairs and add a sequence ID to it. 
	 * If the pair is already in the map, the id is added to the old pair.
	 * @param mapPaires the map of pairs
	 * @param seqID a sequence id
	 * @param pair a pair
	 */
	private void addPairWithoutCheck(Map<PairBIDE, PairBIDE> mapPaires, Integer seqID, PairBIDE pair) {
//		long start = System.currentTimeMillis();
		// check if the pair is already in the map
		PairBIDE oldPaire = mapPaires.get(pair);
		// if not
		if(oldPaire == null){
			// we add the new pair "paire" to the map
			mapPaires.put(pair, pair);
			pair.getSequenceIDs().add(seqID);
		}else{
			// otherwise we use the old one
			oldPaire.getSequenceIDs().add(seqID);
		}
		return;
	} 
	
	private boolean addPair(Map<PairBIDE, PairBIDE> mapPaires, Integer seqID, PairBIDE pair) {
//		long start = System.currentTimeMillis();
		// check if the pair is already in the map
		PairBIDE oldPaire = mapPaires.get(pair);
		// if not
		if(oldPaire == null){
			// we add the new pair "paire" to the map
			mapPaires.put(pair, pair);
		}else{
			// otherwise we use the old one
			pair = oldPaire;
		}
		
		// we add the sequence ID  to the pair
		pair.getSequenceIDs().add(seqID);
		
		if(pair.getSequenceIDs().size() >= minsuppAbsolute) {
			return true;
		}
		
//		debugAddPairTime += System.currentTimeMillis() - start;
		return false;
	} 
	
	
	
	/**
	 * For each item, calculate the sequence id of sequences containing that item
	 * @param database the current sequence database
	 * @return Map of items to sequence IDs that contains each item
	 */
	private Map<Integer, Set<Integer>> findSequencesContainingItems(SequenceDatabase database) {
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
	Map<Integer, Set<Integer>> mapSequenceID = new HashMap<Integer, Set<Integer>>(); // pour conserver les ID des séquences: <Id Item, Set d'id de séquences>
		// for each sequence
		for(Sequence sequence : database.getSequences()){
			// for each itemset in that sequence
			for(List<Integer> itemset : sequence.getItemsets()){
				// for each item
				for(Integer item : itemset){
					// get the set of sequence ids for that item
					Set<Integer> sequenceIDs = mapSequenceID.get(item);
					if(sequenceIDs == null){
						// if null create a new set
						sequenceIDs = new HashSet<Integer>();
						mapSequenceID.put(item, sequenceIDs);
					}
					// add the current sequence id to this set
					sequenceIDs.add(sequence.getId());
				}
			}
		}
		return mapSequenceID;
	}
	
	//buildProjectedContextSingleItem
	
	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param context The current database.
	 * @param inSuffix This boolean indicates if the item "item" is part of a suffix or not.
	 * @return the projected database.
	 */
	private List<PseudoSequenceBIDE> buildProjectedContextSingleItem(Integer item, Map<Integer, PseudoSequenceBIDE> initialDatabase2, boolean inSuffix, Set<Integer> sidset) {
		// The projected pseudo-database
		List<PseudoSequenceBIDE> sequenceDatabase = new ArrayList<PseudoSequenceBIDE>();

		// for each sequence 
	loop1:for(int sid: sidset){ 
			PseudoSequenceBIDE sequence = initialDatabase2.get(sid);
			
			// for each itemset of the sequence
			for(int i =0; i< sequence.size(); i++){  
				
				int sizeOfItemsetAti = sequence.getSizeOfItemsetAt(i);
				
				// find the position of the item used for the projection in this itemset if it appears
				int index = sequence.indexOf(sizeOfItemsetAti, i, item);
				// if it does appear and it is in a postfix/suffix if the item is in a postfix/suffix
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					// if this is not the last item of the itemset
					if(index != sizeOfItemsetAti-1){ 
						// create a new pseudo sequence
						sequenceDatabase.add(new PseudoSequenceBIDE(sequence, i, index+1));
//						continue loop1;
					}else if (i != sequence.size()-1){// if this is not the last itemset of the sequence			 
						// create a new pseudo sequence
						// if the size of this pseudo sequence is greater than 0
						// add it to the projected database.
						sequenceDatabase.add(new PseudoSequenceBIDE( sequence, i+1, 0));
//						continue loop1;
					}	
				}
			}
		}
		return sequenceDatabase; // return the projected database
	}

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param context The current database.
	 * @param inSuffix This boolean indicates if the item "item" is part of a suffix or not.
	 * @return the projected database.
	 */
	private List<PseudoSequenceBIDE> buildProjectedDatabase(Integer item, List<PseudoSequenceBIDE> database, boolean inSuffix, Set<Integer> sidset) {
		// The projected pseudo-database
		List<PseudoSequenceBIDE> sequenceDatabase = new ArrayList<PseudoSequenceBIDE>();

		// for each sequence 
loop1:	for(PseudoSequenceBIDE sequence : database){ 
	
			if(sidset.contains(sequence.getId()) == false){
				continue;
			}
	
			// for each item of the sequence
			for(int i =0; i< sequence.size(); i++){  	
				
				int sizeOfItemsetAti = sequence.getSizeOfItemsetAt(i);
				
				// check if the itemset contains the item that we use for the projection
				int index = sequence.indexOf(sizeOfItemsetAti, i , item);
				// if it does not, and the current item is part of a suffix if inSuffix is true
				//   and vice-versa
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					if(index != sizeOfItemsetAti - 1){ // if this is not the last item of the itemset
						// create a new pseudo sequence
							// add it to the projected database.
							sequenceDatabase.add(new PseudoSequenceBIDE(sequence, i, index+1));
//							continue loop1;
//						} 
					}else if ((i != sequence.size()-1)){// if this is not the last itemset of the sequence			 
						// create a new pseudo sequence
						// add it to the projected database.
						sequenceDatabase.add(new PseudoSequenceBIDE( sequence, i+1, 0));
//						continue loop1;
					}	
				}
			}
		}
		return sequenceDatabase; // return the projected database
	}
	
	/**
	 * Method to recursively grow a given sequential pattern.
	 * @param prefix  the current sequential pattern that we want to try to grow
	 * @param database the current projected sequence database
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private int recursion(SequentialPattern prefix, List<PseudoSequenceBIDE> contexte) throws IOException {	
		// find frequent items of size 1 in the current projected database.
		Set<PairBIDE> pairs = findAllFrequentPairs(prefix, contexte);
		
		// we will keep tract of the maximum support of patterns
		// that can be found with this prefix, to check
		// for forward extension when this method returns.
		int maxSupport = 0;
		
		// For each pair found (a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(PairBIDE pair : pairs){
			// if the item is frequent.
			if(pair.getCount() >= minsuppAbsolute){
				// create the new postfix by appending this item to the prefix
				SequentialPattern newPrefix;
				// if the item is part of a postfix
				if(pair.isPostfix()){ 
					// we append it to the last itemset of the prefix
					newPrefix = appendItemToPrefixOfSequence(prefix, pair.getItem()); // is =<is, (deltaT,i)>
				}else{ // else, we append it as a new itemset to the sequence
					newPrefix = appendItemToSequence(prefix, pair.getItem());
				}
				// build the projected database with this item
//				long start = System.currentTimeMillis();
				List<PseudoSequenceBIDE> projectedContext = buildProjectedDatabase(pair.getItem(), contexte, pair.isPostfix(), pair.getSequenceIDs());
//				debugProjectDBTime += System.currentTimeMillis() - start;
				
				// create new prefix
				newPrefix.setSequenceIDs(pair.getSequenceIDs()); 

				// variable to keep track of the maximum support of extension 
				// with this item and this prefix
				if(projectedContext.size() >= minsuppAbsolute) {
					
					int maxSupportOfSuccessors = 0;
					
//					if(!checkBackScanPruning(newPrefix, pair.getSequenceIDs())) {
						 maxSupportOfSuccessors = recursion(newPrefix, projectedContext); // récursion
//					}
					
					// check the forward extension for the prefix
					// if no forward extension
					if(minsuppAbsolute > maxSupportOfSuccessors){ 
						//  if there is no backward extension
						if(!checkBackwardExtension(newPrefix, pair.getSequenceIDs())){
							// save the pattern
							savePattern(newPrefix);
						}
					}
				}else {
					if(!checkBackwardExtension(newPrefix, pair.getSequenceIDs())){
						// save the pattern
						savePattern(newPrefix);
					}
				}

				// record the largest support of patterns found starting
				// with this prefix until now
				if(newPrefix.getAbsoluteSupport() > maxSupport){
					maxSupport = newPrefix.getAbsoluteSupport();
				}
			}
		}
		return maxSupport; // return the maximum support generated by extension of the prefix
	}
	
	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @return A list of pairs, where a pair is an item with (1) booleans indicating if it
	 *         is in an itemset that is "cut" at left or right (prefix or postfix)
	 *         and (2) the sequence IDs where it occurs.
	 */
	protected Set<PairBIDE> findAllFrequentPairs(SequentialPattern prefix, List<PseudoSequenceBIDE> sequences){
		// We use a Map the store the pairs.
		Map<PairBIDE, PairBIDE> mapPairs = new HashMap<PairBIDE, PairBIDE>();
		
		// for each sequence
		for(PseudoSequenceBIDE sequence : sequences){
			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					Integer item = sequence.getItemAtInItemsetAt(j, i);
					// create the pair corresponding to this item
					PairBIDE pair = new PairBIDE(sequence.isCutAtRight(i), sequence.isPostfix(i), item);  
					// register this sequenceID for that pair.
					addPairWithoutCheck(mapPairs, sequence.getId(), pair);
				}
			}
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
		return mapPairs.keySet(); // return the pairs.
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
		// add to the last itemset
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  
		itemset.addItem(item); 
		return newPrefix;
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
		
			StringBuffer r = new StringBuffer("");
			for(Itemset itemset : prefix.getItemsets()){
	//			r.append('(');
				for(Integer item : itemset.getItems()){
					String string = item.toString();
					r.append(string);
					r.append(' ');
				}
				r.append("-1 ");
			}
	
	//		//  print the list of Pattern IDs that contains this pattern.
	//		if(prefix.getSequencesID() != null){
	//			r.append(" #SID: ");
	//			for(Integer id : prefix.getSequencesID()){
	//				r.append(id);
	//				r.append(' ');
	//			}
	//		}
			r.append(" #SUP: ");
			r.append(prefix.getSequenceIDs().size());
			
			writer.write(r.toString());
			writer.newLine();
		}// otherwise the result is kept into memory
		else{
			patterns.addSequence(prefix, prefix.size());
		}
	}
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 */
	public void printStatistics(int size) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm MaxSP - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Maximal sequential pattern count : ");
		r.append(patternCount);
		r.append('\n');
		r.append(" Max memory (mb):");
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append('\n');
		r.append("===================================================\n");
		System.out.println(r.toString());
//		System.out.println("Frequent pairs time : " + debugFrequentPairsTime);
//		System.out.println("Generate ith period time: " + debugithPeriodTime);
//		System.out.println("Add pair time: " + debugAddPairTime);
//		System.out.println("Project DB time: " + debugProjectDBTime);
	}

}
