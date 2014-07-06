package ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings;
/* This file is copyright (c) 2008-2013 Philippe Fournier-Viger
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
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/

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

import ca.pfv.spmf.input.sequence_database_list_strings.Sequence;
import ca.pfv.spmf.input.sequence_database_list_strings.SequenceDatabase;
import ca.pfv.spmf.tools.MemoryLogger;

/*** 
 * This is an implementation of the BIDE+ algorithm that is optimized to take
 * sequences of strings as input instead of sequences of integers.
 * <br/><br/>
 * 
 * In this file, I have tried to put some comments about how the algorithm works.
 * But if one wants to understand the algorithm, he should read the paper
 * by Wang et al. first because this algorithm is quite complex.
 * <br/><br/>
 * 
 *  In future a version of SPMF, it is planned to remove this package and to provide a more general
 *  mechanism for handling strings in sequences that would work for all algorithms 
 *  that take sequences as input. But this has not been done yet.
 *  
  @see Sequence
  @see SequenceDatabase
  @see SequentialPattern
  @see SequentialPatterns
  @see PairBIDE
  @see PseudoSequenceBIDE
* @author Philippe Fournier-Viger
 */

public class AlgoBIDEPlus_withStrings {
	
	// for statistics
	private long startTime;
	private long endTime;
	
	// the number of patterns found
	private int patternCount = 0;
		
	// absolute minimum support
	private int minsuppAbsolute;
	
	// object to write the file
	BufferedWriter writer = null;
	
	// For BIDE+, we have to keep a pointer to the original database
	private List<PseudoSequenceBIDE> initialContext = null;
	
/*
	 * Default constructor
	 */	
	public AlgoBIDEPlus_withStrings(){
	}

	/**
	 * Run the algorithm
	 * @param database a sequence database
	 * @param outputPath an output file path
	 * @param minsup a minimum support as an integer representing a number of sequences
	 * @throws IOException  exception if error while writing the file
	 */
	public void runAlgorithm(SequenceDatabase database, String outputPath, int minsup) throws IOException {
		// object to write the output file
		writer = new BufferedWriter(new FileWriter(outputPath)); 
		// save the minimum support
		this.minsuppAbsolute = minsup;
		// number of pattern found
		patternCount = 0;
		// reset the utility to check the memory usage
		MemoryLogger.getInstance().reset();
		// save start time for stats
		startTime = System.currentTimeMillis();
		// run the algorithm
		bide(database);
		// save end time for stats
		endTime = System.currentTimeMillis();
		// close the output file
		writer.close();
	}
	
	/**
	 * This is the main method for the BIDE+ algorithm.
	 * @param database a sequence database
	 * @throws IOException exception if some error occurs while writing the output file.
	 */
	private void bide(SequenceDatabase database) throws IOException{
		// The algorithm first scan the database to find all frequent items 
		// The algorithm note the sequences in which these items appear.
		// This is stored in a map:  Key: item  Value : IDs of sequences containing the item
		Map<String, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE TO A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. (OPTIMIZATION : OCTOBER-08 )
		
		// we create a database
		initialContext = new ArrayList<PseudoSequenceBIDE>();
		// for each sequence in the original databse
		for(Sequence sequence : database.getSequences()){
			// make a copy of the sequence but remove infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppAbsolute);
			if(optimizedSequence.size() != 0){
				// if the sequence has more than 1 item, add it to the new database
				initialContext.add(new PseudoSequenceBIDE(optimizedSequence, 0, 0));
			}
		}
		
		// For each frequent item
		for(Entry<String, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent
			if(entry.getValue().size() >= minsuppAbsolute){ 
				// build the projected context with that item
				String item = entry.getKey();
				List<PseudoSequenceBIDE> projectedContext = buildProjectedContext(item, initialContext,  false);

				// Create the prefix for the projected database with that item
				SequentialPattern prefix = new SequentialPattern(0);  
				prefix.addItemset(new Itemset(item));
				prefix.setSequencesID(entry.getValue());
				
				// variable to store the largest support of patterns
				// that will be found starting with this prefix
				int supportSuccessors =0;
				
				/// We recursively try to extend the prefix
				// if it respect the backscan pruning condition (see BIDE paper for details).
				if(!checkBackScanPruning(prefix)){
					// recursive call
					supportSuccessors = recursion(prefix, projectedContext); 
				}
				
				// Finally, because this prefix has support > minsup
				// and passed the backscan pruning,
				// we check if it has no sucessor with the same support
				// (a forward extension)
				// IF no forward extension
				if(prefix.getAbsoluteSupport() != supportSuccessors){
					// IF there is also no backward extension
					if(!checkBackwardExtension(prefix)){ 
						// the pattern is closed and we save it
						savePattern(prefix);  
					}
				}
			}
		}		
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * This is the "backscan-pruning" strategy described in the BIDE+
	 * paper to avoid extending some prefixs that are guaranteed to not
	 * generate a closed pattern (see the BIDE+ paper for details).
	 * 
	 * @param prefix the current prefix
	 * @return boolean true if we should not extend the prefix
	 */
	private boolean checkBackScanPruning(SequentialPattern prefix) {	
		// See the BIDE+ paper for details about this method.
		// For the number of item occurences that can be generated with this prefix:	
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we construct the list of semi-maximum periods.
			List<PseudoSequenceBIDE> semimaximumPeriods = new ArrayList<PseudoSequenceBIDE>();
			for(PseudoSequenceBIDE sequence : initialContext){
				if(prefix.getSequencesID().contains(sequence.getId())){
					PseudoSequenceBIDE period = sequence.getIthSemiMaximumPeriodOfAPrefix(prefix.getItemsets(), i);
					
					if(period !=null){
						semimaximumPeriods.add(period);
					}
				}
			}
			// (2) check if an element of the semi-max perdios as the same frequency as the prefix.
			Set<PairBIDE> paires = findAllFrequentPairsForBackwardExtensionCheck(prefix, semimaximumPeriods, i);
			for(PairBIDE pair : paires){				
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Method to check if a prefix has a backward-extension (see Bide+ article for full details).
	 * This method do it a little bit differently than the BIDE+ article since
	 * we iterate with i on elements of the prefix instead of iterating with 
	 * a i on the itemsets of the prefix. But the idea is the same!
	 * @param prefix the current prefix
	 * @return boolean true, if there is a backward extension
	 */
	private boolean checkBackwardExtension(SequentialPattern prefix) {	

		// We check for an S-extension 
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we build the list of maximum periods
			List<PseudoSequenceBIDE> maximumPeriods = new ArrayList<PseudoSequenceBIDE>();
			// for each sequence in the original database
			for(PseudoSequenceBIDE sequence : initialContext){
				// if the prefix appear in this sequence
				if(prefix.getSequencesID().contains(sequence.getId())){
					// get the ith maximum period
					PseudoSequenceBIDE period = sequence.getIthMaximumPeriodOfAPrefix(prefix.getItemsets(), i);
					
					// if the period is not null
					if(period !=null){
						// we add it to the list of maximum periods
						maximumPeriods.add(period);
					}
				}
			}
			// (2)check if an element from the maximum periods has the same support as the prefix.
			for(PairBIDE pair : findAllFrequentPairsForBackwardExtensionCheck(prefix, maximumPeriods, i)){
				// if there is extension with the same support
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					// the prefix will not be closed and we return true
					return true;
				}
			}
		}
		return false; // no backward extension
	} 
	
	/**
	 * Method to find all frequent items in a list of maximum periods.
	 * @param prefix the current prefix
	 * @param maximum periods  a list of maximum periods
	 * @return a set of pairs indicating the support of items (note that a pair distinguish
	 *         between items in a postfix, prefix...).
	 */
	protected Set<PairBIDE> findAllFrequentPairsForBackwardExtensionCheck(
			SequentialPattern prefix, List<PseudoSequenceBIDE> maximumPeriods, int iPeriod) {
		// Create a Map of pairs to store the pairs
		Map<PairBIDE, PairBIDE> mapPaires = new HashMap<PairBIDE, PairBIDE>();

		// NEW CODE 2010-02-04
		String itemI = prefix.getIthItem(iPeriod);  // iPeriod 
		String itemIm1 = null;  // iPeriod -1
		if(iPeriod > 0){ 
			itemIm1 = prefix.getIthItem(iPeriod -1);	
		}
		// END NEW
		
		// for each maximum period
		for(PseudoSequenceBIDE period : maximumPeriods){

			// for each itemset in that period
			for(int i=0; i< period.size(); i++){
				// NEW
				boolean sawI = false;  // sawI after current position
				boolean sawIm1 = false; // sawI-1 before current position
				// END NEW
				
				// NEW march 20 2010 : check if I is after current position in current itemset
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					String item = period.getItemAtInItemsetAt(j, i);
					if(item.equals(itemI)){
						sawI = true; 
					}else if (item.compareTo(itemI) > 0 ){
						break;
					}
				}
				// END NEW
				
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					String item = period.getItemAtInItemsetAt(j, i);
					
					// NEW
//					if(item.getId() == itemI.getId()){
//						sawI = true;
//					}
					if(itemIm1 != null && item.equals(itemIm1)){
						sawIm1 = true;
					}
					
					boolean isPrefix = period.isCutAtRight(i);
					boolean isPostfix = period.isPostfix(i);
					// END NEW

					// normal case
					PairBIDE paire = new PairBIDE(isPrefix, isPostfix, item);  
					addPaire(mapPaires, period.getId(),
							paire);
					
					// NEW: special cases
					if(sawIm1){
						PairBIDE paire2 = new PairBIDE(isPrefix, !isPostfix, item);  
						addPaire(mapPaires, period.getId(),
								paire2);
					}

					if(sawI ){  
						PairBIDE paire2 = new PairBIDE(!isPrefix, isPostfix, item);  
						addPaire(mapPaires, period.getId(),
								paire2);
					}
					// END NEW
				}
			}
		}
		return mapPaires.keySet(); // return the map of pairs
	}

	/**
	 * Add a pair to the map of pairs and add a sequence ID to it. 
	 * If the pair is already in the map, the id is added to the old pair.
	 * @param mapPaires the map of pairs
	 * @param seqID a sequence id
	 * @param paire a pair
	 */
	private void addPaire(Map<PairBIDE, PairBIDE> mapPaires, Integer seqID, PairBIDE paire) {
		// check if the pair is already in the map
		PairBIDE oldPaire = mapPaires.get(paire);
		// if not
		if(oldPaire == null){
			// we add the new pair "paire" to the map
			mapPaires.put(paire, paire);
		}else{
			// otherwise we use the old one
			paire = oldPaire;
		}
		// we add the sequence ID  to the pair
		paire.getSequencesID().add(seqID);
	}
	
	/**
	 * For each item, calculate the sequence id of sequences containing that item
	 * @param database the current sequence database
	 * @return Map of items to sequence IDs that contains each item
	 */
	private Map<String, Set<Integer>> findSequencesContainingItems(SequenceDatabase contexte) {
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
		Map<String, Set<Integer>> mapSequenceID = new HashMap<String, Set<Integer>>(); // pour conserver les ID des séquences: <Id Item, Set d'id de séquences>
		// for each sequence
		for(Sequence sequence : contexte.getSequences()){
			// for each itemset
			for(List<String> itemset : sequence.getItemsets()){
				// for each item
				for(String item : itemset){
						// get the sequence IDs of this itemset until now
						Set<Integer> sequenceIDs = mapSequenceID.get(item);
						// if null, create a new set
						if(sequenceIDs == null){
							sequenceIDs = new HashSet<Integer>();
							mapSequenceID.put(item, sequenceIDs);
						}
						// add the current sequence ID to the set
						sequenceIDs.add(sequence.getId());
				}
			}
		}
		return mapSequenceID; // return the map
	}
	

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param context The current database.
	 * @param inSuffix This boolean indicates if the item "item" is part of a suffix or not.
	 * @return the projected database.
	 */
	private List<PseudoSequenceBIDE> buildProjectedContext(String item, List<PseudoSequenceBIDE> database, boolean inSuffix) {
		// The projected pseudo-database
		List<PseudoSequenceBIDE> sequenceDatabase = new ArrayList<PseudoSequenceBIDE>();

		// for each sequence 
		for(PseudoSequenceBIDE sequence : database){ // for each sequence
			// for each item of the sequence
			for(int i =0; i< sequence.size(); i++){  // for each item of the sequence
				
				// check if the itemset contains the item that we use for the projection
				int index = sequence.indexOf(i, item);
				// if it does not, and the current item is part of a suffix if inSuffix is true
				//   and vice-versa
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					if(index != sequence.getSizeOfItemsetAt(i)-1){ // if this is not the last item of the itemset
						// create a new pseudo sequence
						PseudoSequenceBIDE newSequence = new PseudoSequenceBIDE(				sequence, i, index+1);
						if(newSequence.size() >0){
							// if the size of this pseudo sequence is greater than 0
							// add it to the projected database.
							sequenceDatabase.add(newSequence);
						} 
					}else if ((i != sequence.size()-1)){// if this is not the last itemset of the sequence			 
						// create a new pseudo sequence
						PseudoSequenceBIDE newSequence = new PseudoSequenceBIDE( sequence, i+1, 0);
						if(newSequence.size() >0){
							// if the size of this pseudo sequence is greater than 0
							// add it to the projected database.
							sequenceDatabase.add(newSequence);
						}	
					}	
				}
			}
		}
		return sequenceDatabase; // the projected database
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
		for(PairBIDE paire : pairs){
			// if the item is freuqent.
			if(paire.getCount() >= minsuppAbsolute){
				// create the new postfix by appending this item to the prefix
				SequentialPattern newPrefix;
				if(paire.isPostfix()){ 
					// we append it to the last itemset of the prefix
					newPrefix = appendItemToPrefixOfSequence(prefix, paire.getItem()); // is =<is, (deltaT,i)>
				}else{ // else, we append it as a new itemset to the sequence
					newPrefix = appendItemToSequence(prefix, paire.getItem());
				}
				// build the projected database with this item
				List<PseudoSequenceBIDE> projectedContext = buildProjectedContext(paire.getItem(), contexte, paire.isPostfix());

				// create new prefix with this item
				newPrefix.setSequencesID(paire.getSequencesID()); 
				
				// variable to keep track of the maximum support of extension 
				// with this item and this prefix
				int maxSupportOfSuccessors = 0;
				
				// Apply the "backscan pruning" strategy (see BIDE+ paper)
				if(checkBackScanPruning(newPrefix) == false){
					// make a recursive call to extend the prefix with this item
					// and generate other patterns starting with that prefix + item
					 maxSupportOfSuccessors = recursion(newPrefix, projectedContext); // récursion
				}	
				
				// check the forward extension for the prefix
				boolean noForwardSIExtension =  newPrefix.getAbsoluteSupport() != maxSupportOfSuccessors;
				// if no forward extension
				if(noForwardSIExtension){ 
					// check if there is a backward extension
					if(!checkBackwardExtension(newPrefix)){
						// none, so we save the pattern
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
					String item = sequence.getItemAtInItemsetAt(j, i);
					// create the pair corresponding to this item
					PairBIDE paire = new PairBIDE(sequence.isCutAtRight(i), sequence.isPostfix(i), item);  
					// register this sequenceID for that pair.
					addPaire(mapPairs, sequence.getId(),
							paire);
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
	private SequentialPattern appendItemToSequence(SequentialPattern prefix, String item) {
		SequentialPattern newPrefix = prefix.cloneSequence();  // isSuffix
		newPrefix.addItemset(new Itemset(item));  // créé un nouvel itemset   + decalage
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
	private SequentialPattern appendItemToPrefixOfSequence(SequentialPattern prefix, String item) {
		SequentialPattern newPrefix = prefix.cloneSequence();
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  // ajoute au dernier itemset
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
		// increase the number of patterns found
		patternCount++;
		
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : prefix.getItemsets()){
//			r.append('(');
			for(String item : itemset.getItems()){
				r.append(item);
				r.append(' ');
			}
			r.append("-1 ");
		}

//		//  print the list of Pattern IDs that contains this pattern.
//		if(prefix.getSequencesID() != null){
//			r.append("SID: ");
//			for(Integer id : prefix.getSequencesID()){
//				r.append(id);
//				r.append(' ');
//			}
//		}
		r.append(" #SUP: ");
		r.append(prefix.getSequencesID().size());
		
		writer.write(r.toString());
		writer.newLine();
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param size  the size of the database
	 */
	public void printStatistics(int size) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Closed sequential patterns count : ");
		r.append(patternCount);
		r.append('\n');
		r.append(" Max memory (mb):");
		r.append(MemoryLogger.getInstance().getMaxMemory());
		r.append('\n');
		r.append("===================================================\n");
		System.out.println(r.toString());
	}
}
