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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;


/**
 * This is an implementation of the PrefixSpan algorithm by Pei et al. 2001 using
 * optimizations such as pseudo-projections, designed specifically for 
 * being used with the multi-dimensional sequential pattern mining algorithm SeqDim. There is another
 * implementation of PrefixSpan for general use that is more optimized and can be found in the package:
 * ca.pfv.spmf.sequential_patterns/bide_and_prefixspan
 * 
 * @see Sequence
 * @see SequenceDatabase
 * @see AlgoSeqDim
* @author Philippe Fournier-Viger
 */

public class AlgoPrefixSpanMDSPM extends AbstractAlgoPrefixSpan {
	
	// The sequential patterns that are found
	private Sequences patterns = null;
	
	// for statistics
	private long startTime;  // start time of the latest algorithm execution
	private long endTime; // end time of the latest algorithm exeuction
	
	// minimum support set by the user as a percentage
	private final double minsup;
	
	// minimum support in terms of sequence count
	private int minsuppRelative;
		
	/**
	 * Constructor
	 * @param minsup minimum support threshold as a percentage (double)
	 */
	public AlgoPrefixSpanMDSPM(double minsup){
		this.minsup = minsup;
	}

	/**
	 * Get the minsup threshold as a percentage (double).
	 * @return a double value
	 */
	public double getMinSupp() {
		return minsup;
	}

	/**
	 * Run the algorithm
	 * @param database a sequence database
	 * @return a set of sequential patterns (Sequences)
	 */
	public Sequences runAlgorithm(SequenceDatabase database) {
		// initialize the set of seq. patterns
		patterns = new Sequences("FREQUENT SEQUENTIAL PATTERNS");
		// convert minsup from a percentage to an integer value indicating
		// minsup in terms of sequence count
		this.minsuppRelative = (int) Math.ceil(minsup* database.size());
		
		// if minsup is zero, then set it to 1
		if(this.minsuppRelative == 0){ 
			this.minsuppRelative = 1;
		}
		// record start time
		startTime = System.currentTimeMillis();
		// start the prefixspan algorithm
		prefixSpan(database);
		// record end time
		endTime = System.currentTimeMillis();
		// return sequential patterns found
		return patterns;
	}
	
	/**
	 * This is the main method of the Prefixspan algorithm
	 * @param database A sequence database
	 */
	private void prefixSpan(SequenceDatabase database){
		// We have to scan the database to find all frequent patterns of size 1.
		// We note the sequences in which these patterns appear in a map
		// where key = item  and value =  a set of IDs of sequences containing
		// that item.
		Map<ItemSimple, Set<Integer>> mapSequenceID = calculateSupportOfItems(database);
		
		// We convert the database in a pseudo-sequence database,
		// and remove the items that are not frequent so that the
		// algorithm will not consider them anymore because infrequent
		// items cannot be part of a frequent seq. pattern.
		PseudoSequenceDatabase initialDatabase = new PseudoSequenceDatabase();
		// for each sequence
		for(Sequence sequence : database.getSequences()){
			// clone the sequence but remove infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			// if the sequence is not empty
			if(optimizedSequence.size() != 0){
				// add the sequence to the new database
				initialDatabase.addSequence(new PseudoSequence(0, optimizedSequence, 0, 0));
			}
		}
		
		// Now, the algorithm will consider each frequent item as 
		// a frequent seq. pattern, and try to grow them recursively to
		// find larger seq. patterns.
		
		// For each item
		for(Entry<ItemSimple, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent
			if(entry.getValue().size() >= minsuppRelative){ 
				// build the projected database with this item
				ItemSimple item = entry.getKey();
				PseudoSequenceDatabase projectedDatabase = buildProjectedContext(item, initialDatabase,  false);

				// Create a new prefix with this item only
				Sequence prefix = new Sequence(0);  
				prefix.addItemset(new Itemset(item, 0));
				// set the IDs of sequence containing this item to the Prefix
				prefix.setSequencesID(entry.getValue());

				// This prefix is a frequent sequential pattern
				// so we add it to the result.
				patterns.addSequence(prefix, 1);  
				
				// We make a recursive call to try to grow the prefix
				// recursively to find larger sequential patterns.
				recursion(prefix, 2, projectedDatabase); 
			}
		}		
	}
	
	/**
	 * Calculate the support of each item in a given sequence database.
	 * @param database a sequence database
	 * @return Map associating to each item (key) the set of IDs of sequences (value)
	 *             containing the item.
	 */
	private Map<ItemSimple, Set<Integer>> calculateSupportOfItems(SequenceDatabase database) {
		//  Use a set to remember which item were seen already from a sequence when scanning
		// a sequence
		Set<Integer> alreadyCounted = new HashSet<Integer>(); 
		// the last sequence scanned
		Sequence lastSequence = null;
		// the map for storing association from each item (key) to the set of IDs of sequences (value)
		// containing the item.
		Map<ItemSimple, Set<Integer>> mapSequenceID = new HashMap<ItemSimple, Set<Integer>>(); 
		// for each sequence		
		for(Sequence sequence : database.getSequences()){
			// if this sequence has not the same id as the previous sequence
			if(lastSequence == null || lastSequence.getId() != sequence.getId()){ // FIX
				// reset the set of items previously seen
				alreadyCounted.clear(); 
				// change the last sequence to the current sequence
				lastSequence = sequence;
			}
			// scan the sequence
			
			// for each itemset of the sequence
			for(Itemset itemset : sequence.getItemsets()){
				// for each item of the current itemset
				for(ItemSimple item : itemset.getItems()){
					
					// if we did not see the item yet in this sequence
					if(!alreadyCounted.contains(item.getId())){
						//  get the set of sequence IDs containing this item until now
						Set<Integer> sequenceIDs = mapSequenceID.get(item);
						if(sequenceIDs == null){
							// if no set, then create one
							sequenceIDs = new HashSet<Integer>();
							mapSequenceID.put(item, sequenceIDs);
						}
						// then add the current sequence ID to the set
						sequenceIDs.add(sequence.getId());
						// remember that we have seen this item already in this sequence
						alreadyCounted.add(item.getId()); 
					}
				}
			}
		}
		// return the map
		return mapSequenceID;
	}
	

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param context The current database.
	 * @param inSuffix This boolean indicates if the item "item" is part of a suffix or not.
	 * @return the projected database.
	 */
	private PseudoSequenceDatabase buildProjectedContext(ItemSimple item, PseudoSequenceDatabase database, boolean inSuffix) {
		// Create the projected pseudo-database
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();

		// for each pseudo-sequence
		for(PseudoSequence sequence : database.getPseudoSequences()){ 
			 // for each itemset of the sequence
			for(int i =0; i< sequence.size(); i++){ 				
				// get the position of the item in this sequence
				int index = sequence.indexOf(i, item.getId());
				// if the itemset contains the item ad it appears in a suffix
				// (an itemset cut at left)
				if(index != -1 && sequence.isCutAtLeft(i) == inSuffix){
					// if this is not the last item of the itemset
					if(index != sequence.getSizeOfItemsetAt(i)-1){ 
						// create a new pseudo-sequence such that the
						// original sequence will be cut right after the item 
						// used for the projection
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), 
								sequence, i, index+1);
						// if the resulting sequence is not empty
						if(newSequence.size() >0){
							// add the sequence to the sequence database
							sequenceDatabase.addSequence(newSequence);
						} 
					}else if ((i != sequence.size()-1)){
						// if this is not the last itemset of the sequence	and
						// the item does not appear in an itemset that is a postfix
						
						// create a new pseudo-sequence by cutting right after the item
						// used for the projection
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), sequence, i+1, 0);

						// if the resulting sequence is not empty
						if(newSequence.size() >0){
							// add the sequence to the sequence database
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
	 * This is the recursive method for growing a prefix of size >=1 to find larger
	 * sequential patterns
	 * @param prefix the prefix
	 * @param k the size of the current prefix + 1
	 * @param database the sequence database
	 */
	private void recursion(Sequence prefix, int k, PseudoSequenceDatabase database) {	
		//Call this method to find all frequent pairs in the current pseudo
		// sequence database.
		// A pair is an item plus a boolean indicating if the item appears in 
		// a suffix (an itemset that is cut) or not.
		Set<Pair> pairs = findAlllPairsAndCountTheirSupport(database.getPseudoSequences());
		
		// For each pair found, 
		for(Pair paire : pairs){
			// if the pair is frequent.
			if(paire.getCount() >= minsuppRelative){
				// create the new postfix by appending the item from the pair
				// to the prefix
				Sequence newPrefix;
				if(paire.isPostfix()){ // if the item is part of a postfix
					// use the method for the case of a postfix
					newPrefix = appendItemToPrefixOfSequence(prefix, paire.getItem()); // is =<is, (deltaT,i)>
				}else{ // else use the regular method
					newPrefix = appendItemToSequence(prefix, paire.getItem(), paire.getTimestamp());
				}
				// build the projected database for the new prefix
				PseudoSequenceDatabase projectedContext = buildProjectedContext(paire.getItem(), database, paire.isPostfix());

				// create new prefix
				Sequence prefix2 = newPrefix.cloneSequence();
				prefix2.setSequencesID(paire.getSequencesID()); 
				
				// add the new prefix to the set of frequent seq. patterns
				patterns.addSequence(prefix2, prefix2.size());  
				// make a recursive call to this method to try to find
				// larger sequential patterns starting with the new prefix.
				recursion(prefix2, k+1, projectedContext); 
			}
		}
	}
	
	/**
	 * Method to count the support of all  pairs in a pseudo-sequence database. A pair
	 * is an item ID and a boolean indicating if the item appear in a postfix or not.
	 * This is for k> 1.
	 * @param sequences the sequence database
	 * @return  a set of Pair objects
	 */
	protected Set<Pair> findAlllPairsAndCountTheirSupport(List<PseudoSequence> sequences){
		// we will scan the database and store the cumulative support of each pair
		// in a map.
		
		// This map contains pairs 
		Map<Pair, Pair> mapPairs = new HashMap<Pair, Pair>();
		
		// variable to remember what is the last sequence considered
		PseudoSequence lastSequence = null;
		// Map to rememer which item were seen already for the current sequence
		// that is scanned.
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); 

		// for each pseudo sequence
		for(PseudoSequence sequence : sequences){
			// if the sequence does not have the same id as the previous one
			if(sequence != lastSequence){
				// reset the map of items already seen
				alreadyCountedForSequenceID.clear(); 
				// set the last sequence to this sequence
				lastSequence = sequence;
			}
			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item in the current itemset
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					ItemSimple item = sequence.getItemAtInItemsetAt(j, i);
					// create the pair corresponding to this item in this itemset
					Pair pair = new Pair(false, sequence.isCutAtLeft(i), item);  
					// if this pair was not already seen for this sequence
					if(!alreadyCountedForSequenceID.contains(pair)){
						// look for the pair in the map  of pairs
						Pair oldPair = mapPairs.get(pair);
						// if none
						if(oldPair == null){
							// put the current pair
							mapPairs.put(pair, pair);
						}else{
							// otherwise use the old one
							pair = oldPair;
						}
						// add the pair to the set of pairs already seen for this sequence.
						alreadyCountedForSequenceID.add(pair);
						// add the sequence ID of this sequence to
						// the pair
						pair.getSequencesID().add(sequence.getId());
					}
				}
			}
		}
		// return the set of pairs
		return mapPairs.keySet();
	}

	/**
	 *  This method creates a copy of the sequence and add a given item 
	 *  as a new itemset to the sequence. 
	 *  It sets the support of the sequence as the support of the item.
	 * @param prefix  the sequence
	 * @param item the item
	 * @param timestamp a timestamp (not used by prefixspan)
	 * @return the new sequence
	 */
	private Sequence appendItemToSequence(Sequence prefix, ItemSimple item, long timestamp) {
		Sequence newPrefix = prefix.cloneSequence(); 
		newPrefix.addItemset(new Itemset(item,  0)); 
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
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  // ajoute au dernier itemset
		itemset.addItem(item);   
		return newPrefix;
	}

	/**
	 * Print statistics about the latest execution of this algorithm.
	 * @param databaseSize the number of sequences in the original sequence database.
	 */
	public void printStatistics(int databaseSize) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  PREFIXSPAN - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : ");
		r.append(patterns.sequenceCount);
		r.append('\n');
		r.append(patterns.toString(databaseSize));
		r.append("===================================================\n");
		System.out.println(r.toString());
	}

}
