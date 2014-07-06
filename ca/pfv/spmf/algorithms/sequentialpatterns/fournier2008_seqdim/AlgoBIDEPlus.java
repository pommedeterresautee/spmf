package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is an implementation of the BIDE+ algorithm by Wang et al. 2007 to be used
 * with the SeqDim algorithm. This implementation is optimized to be used with SeqDim.
 * If you wish to use BIDE+ without SeqDIM, please see the package: <br/>
 *   ca.pfv.spmf.sequential_patterns.bide_and_prefixspan.<br/><br/>
 *   
 * BIDE+ is described in: <br/>
 * J. Wang, J. Han: BIDE: Efficient Mining of Frequent Closed Sequences. ICDE 2004: 79-90
 *
 * @author Philippe Fournier-Viger
 */

public class AlgoBIDEPlus extends AbstractAlgoPrefixSpan {
	
	// The sequential patterns that are found
	private Sequences patterns = null;
	
	// for statistics
	private long startTime;
	private long endTime;
	
	private final double minsup;
	
	// relative minimum support
	private int minsuppRelative;
	
	// For BIDE+, we have to keep a pointer to the original database
	private PseudoSequenceDatabase initialDatabase = null;
		
	/**
	 * Constructor
	 * @param minsup  minimum support threshold as a percentage (double)
	 */
	public AlgoBIDEPlus(double minsup){
		this.minsup = minsup;
	}

	/**
	 * Get the minimum support.
	 * @return minimum support as a double
	 */
	public double getMinSupp() {
		return minsup;
	}

	/**
	 * Run the algorithm
	 * @param database  a sequence database
	 * @return the sequential patterns found in a Sequences structure.
	 */
	public Sequences runAlgorithm(SequenceDatabase database) {
		// initialize set of patterns
		patterns = new Sequences("FREQUENT CLOSED SEQUENTIAL PATTERNS");
		// convert minsup from a percentage to an integer representing
		// a number of sequences
		this.minsuppRelative = (int) Math.ceil(minsup* database.size());
		// if minsup =0, then set it to 1 sequence.
		if(this.minsuppRelative == 0){ 
			this.minsuppRelative = 1;
		}
		// save the start time
		startTime = System.currentTimeMillis();
		// start the algorithm
		bide(database);
		// save the end time
		endTime = System.currentTimeMillis();
		// return patterns found
		return patterns;
	}
	
	/**
	 * This is the main method for the BIDE+ algorithm.
	 * @param database The initial sequence database.
	 */
	private void bide(SequenceDatabase database){
		// The algorithm first scan the database to find all frequent items 
		// The algorithm note the sequences in which these items appear.
		// This is stored in a map:  Key: item  Value : IDs of sequences containing the item
		Map<ItemSimple, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE TO A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE.
				
		// we create a database
		initialDatabase = new PseudoSequenceDatabase();
		// for each sequence of the original database
		for(Sequence sequence : database.getSequences()){
			// we make a copy of the sequence while removing infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			if(optimizedSequence.size() != 0){
				// if this sequence has size >0, we add it to the new database
				initialDatabase.addSequence(new PseudoSequence(0, optimizedSequence, 0, 0));
			}
		}
		
		// For each frequent item
		for(Entry<ItemSimple, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent
			if(entry.getValue().size() >= minsuppRelative){
				// build the projected database with this item
				ItemSimple item = entry.getKey();
				PseudoSequenceDatabase projectedContext = buildProjectedDatabase(item, initialDatabase,  false);

				// Create the prefix for the projected database.
				Sequence prefix = new Sequence(0);  
				prefix.addItemset(new Itemset(item, 0));
				// set the sequence IDS of this prefix
				prefix.setSequencesID(entry.getValue());
				
				// variable to store the largest support of patterns
				// that will be found starting with this prefix
				int successorSupport =0;
				
				// We recursively try to extend the prefix
				// if it respect the backscan pruning condition (see BIDE paper for details).
				if(!checkBackScanPruning(prefix)){
					// recursive call
					successorSupport = recursion(prefix, projectedContext); 
				}
				
				// Finally, because this prefix has support > minsup
				// and passed the backscan pruning,
				// we check if it has no sucessor with the same support
				// (a forward extension)
				// IF no forward extension
				if(prefix.getAbsoluteSupport() != successorSupport){
					// IF there is also no backward extension
					if(!checkBackwardExtension(prefix)){ 
						// the pattern is closed and we save it
						patterns.addSequence(prefix, 1); 
					}
				}
			}
		}		
	}
	
	/**
	 * This is the "backscan-pruning" strategy described in the BIDE+
	 * paper to avoid extending some prefixs that are guaranteed to not
	 * generate a closed pattern (see the BIDE+ paper for details).
	 * 
	 * @param prefix the current prefix
	 * @return boolean true if we should not extend the prefix
	 */
	private boolean checkBackScanPruning(Sequence prefix) {	
		// See the BIDE+ paper for details about this method.
		// For the number of item occurences that can be generated with this prefix:
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we construct the list of semi-maximum periods.
			List<PseudoSequence> semimaximumPeriods = new ArrayList<PseudoSequence>();
			for(PseudoSequence sequence : initialDatabase.getPseudoSequences()){
				if(prefix.getSequencesID().contains(sequence.getId())){
					PseudoSequence period = sequence.getIthSemiMaximumPeriodOfAPrefix(prefix, i, false);
					
					if(period !=null){
						semimaximumPeriods.add(period);
					}
				}
			}
			// (2) check if an element of the semi-max perdios as the same frequency as the prefix.
			Set<Pair> paires = findAllFrequentPairsForBackwardExtensionCheck(prefix, semimaximumPeriods, i);
			for(Pair pair : paires){				
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
	private boolean checkBackwardExtension(Sequence prefix) {	

		// We check for an S-extension 
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we build the list of maximum periods
			List<PseudoSequence> maximumPeriods = new ArrayList<PseudoSequence>();
			// for each sequence in the original database
			for(PseudoSequence sequence : initialDatabase.getPseudoSequences()){
				// if the prefix appear in this sequence
				if(prefix.getSequencesID().contains(sequence.getId())){
					// get the ith maximum period
					PseudoSequence period = sequence.getIthMaximumPeriodOfAPrefix(prefix, i, false);
					// if the period is not null
					if(period !=null){
						// we add it to the list of maximum periods
						maximumPeriods.add(period);
					}
				}
			}
			// (2)check if an element from the maximum periods has the same support as the prefix.
			for(Pair pair : findAllFrequentPairsForBackwardExtensionCheck(prefix, maximumPeriods, i)){
				// if there is extension with the same support
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					// the prefix will not be closed and we return true
					return true;
				}
			}
		}
		return false; // no backward extension, we return false
	} 
	
	/**
	 * Method to find all frequent items in a list of maximum periods.
	 * @param prefix the current prefix
	 * @param maximum periods  a list of maximum periods
	 * @return a set of pairs indicating the support of items (note that a pair distinguish
	 *         between items in a postfix, prefix...).
	 */
	protected Set<Pair> findAllFrequentPairsForBackwardExtensionCheck(
			Sequence prefix, List<PseudoSequence> maximumPeriods, int iPeriod) {
		// Create a Map of pairs to store the pairs
		Map<Pair, Pair> mapPaires = new HashMap<Pair, Pair>();
		
		// Important: We need to make sure that don't count two time the same element 
		PseudoSequence lastPeriod = null;
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); 
		
		// NEW CODE 2010-02-04
		ItemSimple itemI = prefix.getIthItem(iPeriod);  // iPeriod 
		ItemSimple itemIm1 = null;  // iPeriod -1
		if(iPeriod > 0){ 
			itemIm1 = prefix.getIthItem(iPeriod -1);	
		}
		// END NEW
		
		
		// for each maximum period
		for(PseudoSequence period : maximumPeriods){
			if(period != lastPeriod){
				alreadyCountedForSequenceID.clear(); 
				lastPeriod = period;
			}

			// for each itemset in that period
			for(int i=0; i< period.size(); i++){
				// NEW
				boolean sawI = false;  // sawI after current position
				boolean sawIm1 = false; // sawI-1 before current position
				// END NEW
				
				// NEW march 20 2010 : check if I is after current position in current itemset
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					ItemSimple item = period.getItemAtInItemsetAt(j, i);
					if(item.getId() == itemI.getId()){
						sawI = true; 
					}else if (item.getId() > itemI.getId()){
						break;
					}
				}
				// END NEW
				
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					ItemSimple item = period.getItemAtInItemsetAt(j, i);
					
					// NEW
//					if(item.getId() == itemI.getId()){
//						sawI = true;
//					}
					if(itemIm1 != null && item.getId() == itemIm1.getId()){
						sawIm1 = true;
					}
					
					boolean isPrefix = period.isCutAtRight(i);
					boolean isPostfix = period.isCutAtLeft(i);
					// END NEW

					// normal case
					Pair paire = new Pair(isPrefix, isPostfix, item);  
					addPair(mapPaires, alreadyCountedForSequenceID, period,
							paire);
					
					// NEW: special cases
					if(sawIm1){
						Pair paire2 = new Pair(isPrefix, !isPostfix, item);  
						addPair(mapPaires, alreadyCountedForSequenceID, period,
								paire2);
					}

					if(sawI ){  
						Pair paire2 = new Pair(!isPrefix, isPostfix, item);  
						addPair(mapPaires, alreadyCountedForSequenceID, period,
								paire2);
					}
					// END NEW
				}
			}
		}
		// return the map of pairs
		return mapPaires.keySet();
	}

	/**
	 * Add a pair to the map of pairs and add a sequence ID to it. 
	 * If the pair is already in the map, the id is added to the old pair.
	 * @param mapPaires the map of pairs
	 * @param seqID a sequence id
	 * @param pair a pair
	 */
	private void addPair(Map<Pair, Pair> mapPaires,
			Set<Pair> alreadyCountedForSequenceID, PseudoSequence period,
			Pair pair) {
		// check if the pair is already in the map
		Pair oldPaire = mapPaires.get(pair);
		if(!alreadyCountedForSequenceID.contains(pair)){
			// if not
			if(oldPaire == null){
				// we add the new pair "paire" to the map
				mapPaires.put(pair, pair);
			}else{
				// otherwise we use the old one
				pair = oldPaire;
			}
			alreadyCountedForSequenceID.add(pair);
			// we add the sequence ID  to the pair
			pair.getSequencesID().add(period.getId());
		}
	}
	
	/**
	 * For each item, calculate the sequence id of sequences containing that item
	 * @param database the current sequence database
	 * @return Map of items to sequence IDs that contains each item
	 */

	private Map<ItemSimple, Set<Integer>> findSequencesContainingItems(SequenceDatabase database) {
		// the following set is to remember if an item was already seen for a sequence
		Set<Integer> alreadyCounted = new HashSet<Integer>(); 
		// The latest sequence that was scanned
		Sequence lastSequence = null;
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
		Map<ItemSimple, Set<Integer>> mapSequenceID = new HashMap<ItemSimple, Set<Integer>>(); // pour conserver les ID des séquences: <Id Item, Set d'id de séquences>
		
		
		// for each sequence
		for(Sequence sequence : database.getSequences()){
			// If we scan a new sequence (with a different id),
			// then reset the set of items that we have seen...
			if(lastSequence == null || lastSequence.getId() != sequence.getId()){ // FIX
				alreadyCounted.clear(); 
				lastSequence = sequence;
			}
			// for each itemset in that sequence
			for(Itemset itemset : sequence.getItemsets()){
				// for each item
				for(ItemSimple item : itemset.getItems()){
					// if we have not seen this item yet for that sequence
					if(!alreadyCounted.contains(item.getId())){
						// get the set of sequence ids for that item
						Set<Integer> sequenceIDs = mapSequenceID.get(item);
						if(sequenceIDs == null){
							// if null create a new set
							sequenceIDs = new HashSet<Integer>();
							mapSequenceID.put(item, sequenceIDs);
						}
						// add the current sequence id to this set
						sequenceIDs.add(sequence.getId());
						// remember that we have seen this item
						alreadyCounted.add(item.getId()); 
					}
				}
			}
		}
		return mapSequenceID;
	}
	

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param context The current database.
	 * @param inSuffix This boolean indicates if the item "item" is part of a suffix or not.
	 * @return the projected database.
	 */
	private PseudoSequenceDatabase buildProjectedDatabase(ItemSimple item, PseudoSequenceDatabase database, boolean inSuffix) {
		// The projected pseudo-database
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();

		// for each sequence 
		for(PseudoSequence sequence : database.getPseudoSequences()){ // for each sequence
			// for each item of the sequence
			for(int i =0; i< sequence.size(); i++){  // for each item of the sequence
				
				// check if the itemset contains the item that we use for the projection
				int index = sequence.indexOf(i, item.getId());
				if(index != -1 && sequence.isCutAtLeft(i) == inSuffix){
					if(index != sequence.getSizeOfItemsetAt(i)-1){ // if this is not the last item of the itemset
						// create a new pseudo sequence
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), 
								sequence, i, index+1);
						if(newSequence.size() >0){
							// if the size of this pseudo sequence is greater than 0
							// add it to the projected database.
							sequenceDatabase.addSequence(newSequence);
						} 
					}else if ((i != sequence.size()-1)){// if this is not the last itemset of the sequence			 
						// create a new pseudo sequence
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), sequence, i+1, 0);
						if(newSequence.size() >0){
							// if the size of this pseudo sequence is greater than 0
							// add it to the projected database.
							sequenceDatabase.addSequence(newSequence);
						}	
					}	
				}
			}
		}
		// return the projected database
		return sequenceDatabase;
	}
	
	/**
	 * Method to recursively grow a given sequential pattern.
	 * @param prefix  the current sequential pattern that we want to try to grow
	 * @param database the current projected sequence database
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private int recursion(Sequence prefix, PseudoSequenceDatabase contexte) {	
		// find frequent items of size 1.
		Set<Pair> pairs = findAllFrequentPairs(prefix, contexte.getPseudoSequences());
		
		// we will keep track of the maximum support of patterns
		// that can be found with this prefix, to check
		// for forward extension when this method returns.
		int maxSupport = 0;
		
		// For each pair found (a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database).
		for(Pair paire : pairs){
			// if the item is freuqent.
			if(paire.getCount() >= minsuppRelative){
				// create the new postfix by appending this item to the prefix
				Sequence newPrefix;
				// if the item is part of a postfix
				if(paire.isPostfix()){ 
					// we append it to the last itemset of the prefix
					newPrefix = appendItemToPrefixOfSequence(prefix, paire.getItem()); // is =<is, (deltaT,i)>
				}else{  // else, we append it as a new itemset to the sequence
					newPrefix = appendItemToSequence(prefix, paire.getItem());
				}
				// build the projected database
				PseudoSequenceDatabase projectedContext = buildProjectedDatabase(paire.getItem(), contexte, paire.isPostfix());

				// create new prefix
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
				if(noForwardSIExtension){ 
					// check if there is a backward extension
					if(!checkBackwardExtension(newPrefix)){
						// none, so we save the pattern
						patterns.addSequence(newPrefix, newPrefix.size());  // it is a closed sequence
					}
				}
				// record the largest support of patterns found starting
				// with this prefix until now
				if(newPrefix.getAbsoluteSupport() > maxSupport){
					maxSupport = newPrefix.getAbsoluteSupport();
				}
			}
		}
		return maxSupport;
	}
	
	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @return A list of pairs, where a pair is an item with (1) booleans indicating if it
	 *         is in an itemset that is "cut" at left or right (prefix or postfix)
	 *         and (2) the sequence IDs where it occurs.
	 */
	protected Set<Pair> findAllFrequentPairs(Sequence prefix, List<PseudoSequence> sequences){
		// We use a Map the store the pairs.
		Map<Pair, Pair> mapPairs = new HashMap<Pair, Pair>();
		
		// the last sequence that was scanned
		PseudoSequence lastSequence = null;
		// the set of pair that we have already seen for the current sequence
		// (to count each item only one time for each sequence ID)
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); 

		// for each sequence
		for(PseudoSequence sequence : sequences){
			// if the sequence does not have the same id, we clear the map.
			if(sequence != lastSequence){
				alreadyCountedForSequenceID.clear(); 
				lastSequence = sequence;
			}

			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					ItemSimple item = sequence.getItemAtInItemsetAt(j, i);
					// create the pair corresponding to this item
					Pair pair = new Pair(sequence.isCutAtRight(i), sequence.isCutAtLeft(i), item);  
					// register this sequenceID for that pair.
					addPair(mapPairs, alreadyCountedForSequenceID, sequence,
							pair);
				}
			}
		}
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
	private Sequence appendItemToSequence(Sequence prefix, ItemSimple item) {
		Sequence newPrefix = prefix.cloneSequence();  // isSuffix
		newPrefix.addItemset(new Itemset(item, 0));  
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
	private Sequence appendItemToPrefixOfSequence(Sequence prefix, ItemSimple item) {
		Sequence newPrefix = prefix.cloneSequence();
		// add to the last itemset
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  
		itemset.addItem(item);   
		return newPrefix;
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 * @param databaseSize  the size of the database (a number of sequences)
	 */
	public void printStatistics(int databaseSize) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Closed sequential patterns count : ");
		r.append(patterns.sequenceCount);
		r.append('\n');
		r.append(patterns.toString(databaseSize));
		r.append("===================================================\n");
		System.out.println(r.toString());
	}

}
