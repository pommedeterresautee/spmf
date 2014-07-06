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

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.kmeans_for_fournier08.AlgoKMeansWithSupport;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.kmeans_for_fournier08.Cluster;

/**
 * This is the original implementation of the Fournier-Viger algorithm (2008) for sequential
 * pattern mining, which  combines features from several algorithms and includes original features such
 * as accepting items with double values.  For details about this algorithm see:
 * <br/><br/>
 * 
 * Fournier-Viger, P., Nkambou, R & Mephu Nguifo, E. (2008), A Knowledge Discovery 
 * Framework for Learning Task Models from User Interactions in Intelligent Tutoring Systems. 
 * Proceedings of the 7th Mexican International Conference on Artificial Intelligence (MICAI 2008). 
 * LNAI 5317, Springer, pp. 765-778.
 * <br/><br/>
 * 
 * This implementation can keep the result into memory and return it by the method 
 * runAlgorithm()  or save the result directly to a file, if an output file path is provided.
 *
 * @see SequenceDatabase
 * @see Sequence
 * @see Sequences
 * @see PseudoSequence
 * @see PseudoSequenceDatabase
 * @see Pair
 * @see AlgoKMeansWithSupport
 * @see AbstractAlgoPrefixSpan
* @author Philippe Fournier-Viger
 */
public class AlgoFournierViger08 extends AbstractAlgoPrefixSpan{
	// The sequential patterns that are found
	private Sequences patterns = null;
	
	/// number of sequential pattern found
	int patternCount =0;
	
	// start time of latest execution
	private long startTime;
	// end time of latest execution
	private long endTime;
	
	// parameters of runAlgorithm
	private final double minInterval;  // min time interval between two itemsets  (c1)
	private final double maxInterval;  // max time interval between two itemsets  (c2)
	private final double minWholeInterval; // min time length of a seq. pattern (c3)
	private final double maxWholeInterval;  // max time length of a seq. pattern (c4)
	private final double minsupp;  // minimum support threshold
	private final boolean findClosedPatterns;  // find closed patterns or not
	private int minsuppRelative;  // minimum support as an integer
	private boolean enableBackscanPruning;  // use backscan pruning or not
	
	// For performing the clustering, this algorithm need an instance
	// of KMeans with support.
	private final AlgoKMeansWithSupport algoClustering;
	
	// For BIDE+, we have to keep a pointer to the original database
	private PseudoSequenceDatabase initialDatabase = null;
	
	// object to write the output file if the user wish to write to a file
	BufferedWriter writer = null;

	/**
	 * @param minsupp minimum support
	 * @param minInterval minimum item interval between two adjacent items. (C1)
	 * @param maxInterval maximum item interval between two adjacent items. (C2)
	 * @param minWholeInterval minimum item interval between the head and tail of a sequence. (C3)
	 * @param maxWholeInterval maximum item interval between the head and tail of a sequence (C4)
	 * @param algoClustering  algorithm for clustering 
	 * @param findClosedPatterns to mine only closed sequences
	 */
	public AlgoFournierViger08(double minsupp, 
			double minInterval, double maxInterval, 
			double minWholeInterval, double maxWholeInterval, 
			AlgoKMeansWithSupport algoClustering, 
			boolean findClosedPatterns, boolean enableBackscanPruning){
		// Checking if the parameters are correct.
		if((minInterval > maxInterval) ||
			(minWholeInterval > maxWholeInterval) ||
			(minInterval > maxWholeInterval) ||
			(maxInterval > maxWholeInterval)){
			throw new RuntimeException("Parameters are not valid!!!");
		}	
		
		// Save the parameters in some fields of this class
		this.minInterval = minInterval;
		this.maxInterval = maxInterval;
		this.minWholeInterval = minWholeInterval;
		this.maxWholeInterval = maxWholeInterval;
		this.algoClustering = algoClustering;
		this.minsupp = minsupp;
		this.findClosedPatterns = findClosedPatterns;
		this.enableBackscanPruning = enableBackscanPruning;
	}
	

	/**
	 * Run the algorithm and save the result to a file
	 * @param database a sequence database
	 * @param outputFilePath an output file
	 * @throws IOException throw exception if error creating output file
	 */
	public void runAlgorithm(SequenceDatabase database, String outputFilePath) throws IOException {
    	// if the user want to save the result to a file
		// create output file
		writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		patterns = null;
		
		// run the algorithm
		runAlgorithm(database);
		
		// close output file
		writer.close();
		writer = null;
}
	
	/**
	 * Run the algorithm and save the result to memory
	 * @param database a sequence database
	 * @return a set of sequential patterns (Sequences)
	 * @throws IOException 
	 */
	public Sequences runAlgorithm(SequenceDatabase database) throws IOException {
		// if the user wants to save the result to memory
		if(writer == null){
			patterns = new Sequences("FREQUENT SEQUENCES WITH TIME + CLUSTERING");
		}
		
		patternCount =0;
		
		// convert the minimum support from a percentage to an integer
		// (a number of sequences)
		this.minsuppRelative = (int) Math.ceil(minsupp * database.size());
		// if support is 0, then set it to 1
		if(this.minsuppRelative == 0){
			this.minsuppRelative = 1;
		}
		// record start time
		startTime = System.currentTimeMillis();
		// call to the main method
		isdb(database);
		// record end time
		endTime = System.currentTimeMillis();
		
		// return the set of patterns found
		return patterns;
	}

	
	/**
	 * The main method. It is inspired by
	 * the method ISDB based on the description in the article of Hirate & Yumana but
	 * with some additional modifications for clustering and for finding
	 * closed seq. patterns.
	 * @param originalDatabase The initial context.
	 * @throws IOException  exception if error writing to output file
	 */
	private void isdb(SequenceDatabase originalDatabase) throws IOException{
		// The algorithm first scan the database to find all frequent items 
		// The algorithm note the sequences in which these items appear.
		// This is stored in a map:  Key: item  Value : IDs of sequences containing the item
		Map<ItemSimple, Set<Integer>> mapSequenceID = findSequencesContainingItems(originalDatabase);
		
		// WE CONVERT THE DATABASE IN A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. (OPTIMIZATION : OCTOBER-08 )
		
		// we create a database
		initialDatabase = new PseudoSequenceDatabase();
		// for each sequence of the original database
		for(Sequence sequence : originalDatabase.getSequences()){
			// we make a copy of the sequence while removing infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			if(optimizedSequence.size() != 0){
				// if this sequence has size >0, we add it to the new database
				initialDatabase.addSequence(new PseudoSequence(0, optimizedSequence, 0, 0));
			}
		}
		
		// For each item
		for(Entry<ItemSimple, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent
			if(entry.getValue().size() >= minsuppRelative){ 
				// build the projected database with this item
				ItemSimple item = entry.getKey();
				PseudoSequenceDatabase[] projectedContexts = null;
				// if the item has a value
				if(item instanceof ItemValued){
					// build projected database by a using method specific to the case of valued items
					projectedContexts = buildProjectedContextItemValued((ItemValued)item, initialDatabase,  false, -1);
				}else{
					// otherwise use the regular method
					projectedContexts = buildProjectedDatabase(item, initialDatabase,  false, -1);
				}
				
				// For each projected database (because of clustering, there could be many)
				for(PseudoSequenceDatabase projectedDatabase : projectedContexts){
					// Create the prefix for the projected database.
					Sequence prefix = new Sequence(0);  
					// if there no clustering was performed
					if(projectedDatabase.getCluster() ==  null){  
						prefix.addItemset(new Itemset(item, 0));
						// set the sequence IDS of this prefix
						prefix.setSequencesID(entry.getValue());
					}
					else{ 
						// If there was valued items (clustering or not)
						// Create an item for the current cluster
						ItemValued item2 = new ItemValued(entry.getKey().getId(),
								projectedDatabase.getCluster().getaverage(),
								projectedDatabase.getCluster().getLower(),
								projectedDatabase.getCluster().getHigher());         
						prefix.addItemset(new Itemset(item2, 0));
						// Sequence IDs
						prefix.setSequencesID(projectedDatabase.getCluster().getSequenceIDs());
					}
					// variable to store the largest support of patterns
					// that will be found starting with this prefix
					int maxSuccessorSupport =0;
					
					// We recursively try to extend the prefix.
					
					// If the user wants to find closed patterns, then 
					// if the current prefix respect the backscan pruning condition
					// (see BIDE paper for details).
					if(!findClosedPatterns  || !checkBackScanPruning(prefix)){
						// recursive call
						maxSuccessorSupport = projection(prefix, 2, projectedDatabase); 
					}
					
					if(isMinWholeIntervalRespected(prefix)){ 
						// Finally, because this prefix has support > minsup
						// and passed the backscan pruning,
						// we check if it has no sucessor with the same support
						// (a forward extension)
						// IF no forward extensionb
						boolean noForwardSIExtension = !findClosedPatterns || !(prefix.getAbsoluteSupport() == maxSuccessorSupport);
						// IF there is also no backward extension
						boolean noBackwardExtension = !findClosedPatterns  || !checkBackwardExtension(prefix);
						// IF CLOSED
						if(noForwardSIExtension && noBackwardExtension){ 
							 // we found a sequence, so save it!
							savePattern(prefix);  
						}
					}
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
	private void savePattern(Sequence prefix) throws IOException {
		// increase the number of pattern found for statistics purposes
		patternCount++; 
	
		// if the result should be saved to a file
		if(writer != null){
			// create a stringbuffer
			StringBuffer r = new StringBuffer("");
			// for each itemset in this sequential pattern
			for(Itemset itemset : prefix.getItemsets()){
				// write timestamp
				r.append('<');
				r.append(itemset.getTimestamp());
				r.append("> ");
				// for each item
				for(ItemSimple item : itemset.getItems()){
					String string = item.toString();
					r.append(string); // add the item
					r.append(' ');
				}
				r.append("-1 "); // add the itemset separator
			}		
			// add the support
			r.append(" #SUP: ");
			r.append(prefix.getSequencesID().size());
			
			//
//			//  print the list of Pattern IDs that contains this pattern.
//			if(prefix.getSequencesID() != null){
//				r.append(" #SID: ");
//				for(Integer id : prefix.getSequencesID()){
//					r.append(id);
//					r.append(' ');
//				}
//			}
			
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
	 * This is the "backscan-pruning" strategy described in the BIDE+
	 * paper to avoid extending some prefixs that are guaranteed to not
	 * generate a closed pattern (see the BIDE+ paper for details).
	 * 
	 * @param prefix the current prefix
	 * @return boolean true if we should not extend the prefix
	 */
	private boolean checkBackwardExtension(Sequence prefix) {	

		// See the BIDE+ paper for details about this method.
		// For the number of item occurences that can be generated with this prefix:
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1)For each i, create the list of maximum periods
			List<PseudoSequence> maximumPeriods = new ArrayList<PseudoSequence>();
			for(PseudoSequence sequence : initialDatabase.getPseudoSequences()){
				if(prefix.getSequencesID().contains(sequence.getId())){
					
					// nov 2009 : FIXED BUG HERE, so that maxgap works
					// with timestamp we need to do it differently than bide..
					List<PseudoSequence> periods = sequence.getAllIthMaxPeriodOfAPrefix(prefix, i, true);
					
					for(PseudoSequence period : periods){
						if(period !=null){
							maximumPeriods.add(period);
						}
					}
				}
			}
			
			// (2)check if an element from the maximum periods has the same support as the prefix.
			for(Pair pair : findAllFrequentPairsSatisfyingC1andC2ForBackwardExtensionCheck(prefix, maximumPeriods, i)){
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					return true;
				}
			}
		}
//		System.out.println("NO BACKWARD");
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
	private boolean checkBackScanPruning(Sequence prefix) {	
		//*********************************************************************
		// VERY IMPORTANT : The backscan pruning cannot work correctly if
		// the maximum whole interval constraint (constraint C4 in Hirate & Yamana)
		// is not equal to infinity.
		// *********************************************************************
		
		// check if the backscan pruning is enabled
		if(enableBackscanPruning == false){
			return false;
		}
		
		// We check for an S-extension 
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we build the list of maximum periods
			List<PseudoSequence> semimaximumPeriods = new ArrayList<PseudoSequence>();
			// for each sequence in the original database
			for(PseudoSequence sequence : initialDatabase.getPseudoSequences()){
				// if the prefix appear in this sequence
				if(prefix.getSequencesID().contains(sequence.getId())){
					// get the ith maximum period
					PseudoSequence period = sequence.getIthSemiMaximumPeriodOfAPrefix(prefix, i, true);
					
					// if the period is not null
					if(period !=null){
						// we add it to the list of maximum periods
						semimaximumPeriods.add(period);
					}
				}
			}
			// (2) check if an element of the semi-max perdios as the same frequency as the prefix.
			Set<Pair> paires = findAllFrequentPairsSatisfyingC1andC2ForBackwardExtensionCheck(prefix, semimaximumPeriods, i);
			for(Pair pair : paires){
				// if there is extension with the same support
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					// the prefix will not be closed and we return true
					return true;
				}
			}
		}
//		System.out.println("NO PRUNING SHOULD BE DONE");
		return false;
	}
	
	
	/**
	 * Method to find all frequent items in a database thas satisfy the C1, C2, C3 and C4
	 * time constraints if they were appended to the current prefix.
	 * This is for k> 1.
	 * @param prefix  the current prefix
	 * @param maximumPeriods a list of i-th maximum periods
	 * @param iPeriod the variable i
	 * @return A list of pairs, where a pair is an item with (1) booleans indicating if it
	 *         is in an itemset that is "cut" at left or right (prefix or postfix)
	 *         , (2) the sequence IDs where it occurs and (3) a time interval.
	 */
	protected Set<Pair> findAllFrequentPairsSatisfyingC1andC2ForBackwardExtensionCheck(
			Sequence prefix, List<PseudoSequence> maximumPeriods, int iPeriod) {
		
		// We use a Map the store the pairs.
		Map<Pair, Pair> mapPaires = new HashMap<Pair, Pair>();
		// the set of pair that we have already seen for the current sequence
		// (to count each item only one time for each sequence ID)
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); 
		// the last period that was scanned
		PseudoSequence lastPeriod = null;
		
		// for each period
		for(PseudoSequence period : maximumPeriods){
			// if the sequence does not have the same ID, we empty the set
			// of items already seen
			if(period != lastPeriod){
				alreadyCountedForSequenceID.clear(); 
				lastPeriod = period;
			}

			// for each itemset in the period
			for(int i=0; i< period.size(); i++){
				// for each item
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					// get the item
					ItemSimple item = period.getItemAtInItemsetAt(j, i);
					
					// Reminder: a maximum period is a subsequence of a sequence
					
					
					// successorInterval: the time interval between (1) the itemset of the maximum period, which contains
					// the item and (2) the itemset immediately after this maximum period.
					// If the sucessor is cut in half, it is the time interval between (1) the item of the itemset containing the item
					// and (2) the time of the last itemset in the maximum period.
					long successorInterval = period.getTimeSucessor() - period.getAbsoluteTimeStamp(i); 
					// totaltime: total time length of the prefix if we add the current item to the prefix.
					long totalTime = prefix.getTimeLength() + successorInterval; 
					
					
					//predecessorInterval : the time interval etween (1) the itemset in the maximum period, which contains
					// the item and (2) the itemset immediately before the maximum period.  If the predecessor is cut
					// in half, it is the time interval between (1) the time of the itemset containing the item and
					// (2) the time of the first itemset of the maximum period.
					long predecessorInterval = period.getAbsoluteTimeStamp(i) - period.getTimePredecessor();
					
					// Check if the time interval of the successor meet the C1 and C2 constraints
					boolean checkGapSucessor
						= successorInterval >= minInterval && successorInterval <= maxInterval || successorInterval == 0;
					
					// Check if the time inverval with the predecessor meet the C1 and C2 constraints.
					// If the "i" of this ith max period is 0.
					// we don't need to check because it is the case of a backward extension where the
					// item would be added before the prefix.
					boolean checkGapPredecesseur 
						= predecessorInterval >= minInterval && predecessorInterval <= maxInterval 
							|| iPeriod ==0 || predecessorInterval == 0;
					
					// Check that the sequential pattern would meet the C3 and C4 constraints.
						// If the "i" of this ith max period is 0.
						// we don't need to check because it is the case of a backward extension where the
						// item would be added INSIDE the prefix.
					boolean checkWholeInterval 
						= totalTime <= maxWholeInterval && totalTime >= minWholeInterval || iPeriod !=0;
					
					// If all the constraints (C1, C2, C3 and C4) are met (the constraints about time interval)
					if(checkGapSucessor  && checkGapPredecesseur && checkWholeInterval){ // C1 C2, C3, C4 check    
						// create a new pair with the current item and indicate if 
						// the item would be part of an itemset that is cut at right or at left,
						// and the time interval with the previous item in the prefix
						Pair pair = new Pair(successorInterval, period.isCutAtRight(i), period.isCutAtLeft(i), item);  // INTERVALLE ?
						// check if there is already a pair for that item
						Pair oldpair = mapPaires.get(pair);
						// if the pair was not already counted for that sequence
						if(!alreadyCountedForSequenceID.contains(pair)){
							// if there was no pair already
							if(oldpair == null){
								// put the pair
								mapPaires.put(pair, pair);
							}else{
								//otherwise use the old one
								pair = oldpair;
							}
							// remember that we have seen this pair for that sequence
							alreadyCountedForSequenceID.add(pair);
							// add the sequence ID to the pair
							pair.getSequencesID().add(period.getId());
						}
					}
				}
			}
		}
		// return the pairs
		return mapPaires.keySet();
	}
	
	/**
	 * For each item, calculate the sequence id of sequences containing that item
	 * @param database the current sequence database
	 * @return Map of items to sequence IDs that contains each item
	 */
	private Map<ItemSimple, Set<Integer>> findSequencesContainingItems(SequenceDatabase contexte) {
		// the following set is to remember if an item was already seen for a sequence
		Set<Integer> alreadyCounted = new HashSet<Integer>(); 
		// The latest sequence that was scanned
		Sequence lastSequence = null;
		// We use a map to store the sequence IDs where an item appear
		// Key : item   Value :  a set of sequence IDs
		Map<ItemSimple, Set<Integer>> mapSequenceID = new HashMap<ItemSimple, Set<Integer>>(); 
		
		// for each sequence
		for(Sequence sequence : contexte.getSequences()){
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
		// return the map
		return mapSequenceID;
	}

	/**
	 * Method to recursively grow a given sequential pattern.
	 * @param prefix  the current sequential pattern that we want to try to grow
	 * @param k  the size of the prefix in terms of item count.
	 * @param database the current projected sequence database
	 * @throws IOException exception if there is an error writing to the output file
	 */
	private int projection(Sequence prefix, int k, PseudoSequenceDatabase database) throws IOException {	
		int maxSupport = 0;
		// For each pair found (a pair is an item with a boolean indicating if it
		// appears in an itemset that is cut (a postfix) or not, and the sequence IDs
		// where it appears in the projected database) that satisfy the C1, C2 and C3 constraints
		for(Pair pair : findAllFrequentPairsSatisfyingC1andC2(prefix, database.getPseudoSequences())){
			// If the pair is frequent
			if(pair.getCount() >= minsuppRelative){
				Sequence newPrefix;
				// if the item is part of a postfix (an itemset cut at right)
				if(pair.isPostfix()){
					// we append it to the last itemset of the prefix
					newPrefix = appendItemToPrefixOfSequence(prefix, pair.getItem()); // is =<is, (deltaT,i)>
				}else{
					// else, we append it as a new itemset to the sequence
					newPrefix = appendItemToSequence(prefix, pair.getItem(), pair.getTimestamp());
				}
				// if the constraint C4 is respected
				if(isMaxWholeIntervalRespected(newPrefix)){ // C4 check
					// make a recursive call to extend the prefix with this item
					// and generate other patterns starting with that prefix + item
					int successorSupport = projectionPair(newPrefix, pair, prefix, database, k);
					// record the largest support of patterns found starting
					// with this prefix+pair until now
					if(successorSupport > maxSupport){
						maxSupport = successorSupport;
					}
				}
			}
		}
		// return the maximum support
		return maxSupport;
	}
	

	/**
	 * Check if the constraints C1 and C2 are respected.
	 * @param timeInterval a time interval
	 * @return true, if yes. false, if no.
	 */
	private boolean isTheMinAndMaxIntervalRespected(long timeInterval){
		return (timeInterval >= minInterval) && (timeInterval <= maxInterval);
	}
	
	/**
	 * Check if the constraints C3 is respected by a seq. pattern.
	 * @param sequence a sequential pattern
	 * @return true, if yes. false, if no.
	 */
	private boolean isMaxWholeIntervalRespected(Sequence sequence){ 
		return (sequence.get(sequence.size()-1).getTimestamp() <= maxWholeInterval);
	}
	
	/**
	 * Check if the constraints C4 is respected by a seq. pattern.
	 * @param sequence a sequential pattern
	 * @return true, if yes. false, if no.
	 */
	private boolean isMinWholeIntervalRespected(Sequence sequence){    
		return (sequence.get(sequence.size()-1).getTimestamp() >= minWholeInterval);
	}

	/**
	 * Do a database projection of a sequence database with a pair.
	 * @param pair  the pair
	 * @param oldPrefix  the current prefix
	 * @param newPrefix  the new prefix obtained by appending the pair to the current prefix
	 * @param database the database to 
	 * @param k  the length of the current prefix in terms of itemset count
	 * @throws IOException if error writing to output file
	 */
	private int projectionPair(Sequence newPrefix, Pair paire, Sequence oldPrefix, PseudoSequenceDatabase database, int k) throws IOException {
		// variable to store the maximum support of frequent seq. patterns that can be obtained
		// by growing newPrefix.
		int maxSupport = 0;
		// Create projected databases (because of the clustering, there can be more than one
		// unlike the regular PrefixSpan algorithm)
		
		// Create array to store the projected databases
		PseudoSequenceDatabase[] projectedContexts = null;
		// if the projection is with a valued item
		if(paire.getItem() instanceof ItemValued){
			// we use clustering
			projectedContexts = buildProjectedContextItemValued((ItemValued)paire.getItem(), database, paire.isPostfix(), paire.getTimestamp());
		}else{
			// otherwise, we do a simple database projection similarly to the Hirate & Yamana algorithm
			projectedContexts = buildProjectedDatabase(paire.getItem(), database, paire.isPostfix(), paire.getTimestamp());
		}
		
		// for each projected database
		for(PseudoSequenceDatabase projectedContext : projectedContexts){
			Sequence prefix;
			// if there is no valued item (no clustering was done)
			if(projectedContext.getCluster() == null){ 
				// just clone the new prefix and set its sequence IDs
				prefix = newPrefix.cloneSequence();
				prefix.setSequencesID(paire.getSequencesID()); 
			}
			else{ 
				// Otherwise there is one or more clusters
				
				// create the item corresponding to this cluster
				ItemValued item2 = new ItemValued(projectedContext.getCluster().getItemId(),
					projectedContext.getCluster().getaverage(),
					projectedContext.getCluster().getLower(),
					projectedContext.getCluster().getHigher()); 
				// Get the sequence IDs corresponding to this cluster
				Set<Integer> sequenceIDs = projectedContext.getCluster().getSequenceIDs();
				// if the item used for the projection was found in a postfix
				if(paire.isPostfix()){
					// we use special method to append for this case
					prefix = appendItemToPrefixOfSequence(oldPrefix, item2); 
				}else{
					// otherwise we use the regular method
					prefix = appendItemToSequence(oldPrefix, item2, paire.getTimestamp());  
				}
				// we set the sequence id of the prefix
				prefix.setSequencesID(sequenceIDs); 
			}
			
			// variable to store the largest support of patterns
			// that will be found starting with this prefix
			int maxSuccessor =0;
			
			// We recursively try to extend the prefix
			// if the users want to find closed pattern, otherwise, we make
			// sure that the current prefix respects the backscan pruning condition 
			// (see BIDE paper for details).
			if(!findClosedPatterns  || !checkBackScanPruning(prefix)){
				// recursive call
				maxSuccessor = projection(prefix, k+1, projectedContext); 
			}		
			
			// if the C3 constraint is respected
			if(isMinWholeIntervalRespected(prefix)){ 
				// if the user wants closed patterns,
				// then check if there is a forward extension of the current prefix
				boolean noForwardSIExtension = !findClosedPatterns || !(prefix.getAbsoluteSupport() == maxSuccessor);
				// if the user wants closed patterns,
				// then check if there is a backward extension of the current prefix
				boolean noBackwardExtension = !findClosedPatterns  || !checkBackwardExtension(prefix);
				// if the pattern is closed
				if(noForwardSIExtension && noBackwardExtension){ 
					// add the sequential patterns to the set of patterns found 
					savePattern(prefix);
				}
				//  if this is the pattern with the highest support found,
				// then record the support.
				if(prefix.getAbsoluteSupport() > maxSupport){
					maxSupport = prefix.getAbsoluteSupport();
				}
			}
		}
		return maxSupport;
	}

	/**
	 * Method find all the frequent pairs that could extend the current prefix in a sequence database
	 * and such that the resulting prefix would respect the C1 and C2 constraints.
	 * For k>1.
	 * @param prefixe the current prefix
	 * @param database the current sequence database
	 * @return the set of frequent pairs
	 */
	protected Set<Pair> findAllFrequentPairsSatisfyingC1andC2(Sequence prefixe, List<PseudoSequence> database) {
		
		// Create a Map of pairs to store the pairs
		Map<Pair, Pair> mapPaires = new HashMap<Pair, Pair>();
		// Important: We need to make sure that don't count two time the same element 
		// This is the remember the last sequence scanned
		PseudoSequence lastSequence = null;
		// This is the remember the pairs that have been already counted
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); 
		
		// for each sequence
		for(PseudoSequence sequence : database){
			// if this is a sequence with a different ID than the previous sequence
			if(lastSequence == null || sequence.getId() != lastSequence.getId()){  //  NEW PHILIPPE OCT-08
				// reset the Pairs that have been already processed
				alreadyCountedForSequenceID.clear(); 
				// remember this sequence as the last sequence scanned for next time
				lastSequence = sequence;
			}
			
			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item in this itemset
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					// get the item
					ItemSimple item = sequence.getItemAtInItemsetAt(j, i);
					// check the C1 and C2 constraints if the item i was added to the current prefix 
					// if this item is not in a postfix.
					if(isTheMinAndMaxIntervalRespected(sequence.getTimeStamp(i)) 
						|| sequence.isCutAtLeft(i)){ 
						// Create the pair corresponding to this item
						Pair paire = new Pair(sequence.getTimeStamp(i), sequence.isCutAtRight(i),sequence.isCutAtLeft(i), item);
						
						// if this pair was not processed already for this sequence ID
						if(!alreadyCountedForSequenceID.contains(paire)){
							// Get the old pair for this item in the map
							Pair oldPaire = mapPaires.get(paire);
							// if none, put the new one
							if(oldPaire == null){
								mapPaires.put(paire, paire);
							}else{
								// otherwise use the old one
								paire = oldPaire;
							}
							// remember that we process this pair now
							alreadyCountedForSequenceID.add(paire);
							// remember the sequence ID of this sequence for this pair
							paire.getSequencesID().add(sequence.getId());
						}
					}
				}
			}
		}
		// return the pairs
		return mapPaires.keySet();
	}


	/**
	 * Do a database projection on a sequence database with an item with a given timestamp. This
	 * method is for the case of an item that is not a valued item.
	 * @param item the item
	 * @param contexte the database
	 * @param inSuffix true if the item was in a suffix
	 * @param timestamp the timestamp
	 * @return a projected sequence database
	 */
	private PseudoSequenceDatabase[] buildProjectedDatabase(ItemSimple item, 
			PseudoSequenceDatabase contexte, boolean inSuffix, long timestamp) {

		// This structure will store the projected database
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();

		// Contrarily to PrefixSpan,  we need to create all subsequences in the projected database
		// corresponding to each occurence of the item (because of the timestamps).  Each time that 
		// we encounter the item that is used
		// for the projection and its timestamp, we will add the sequence to the projected database
		// (if this database is not empty). We also need to adjust timestamps for all new 
		// sequences that are created (as in Hirate & Yamana) in the projected database.
		
		// for each sequence
		for(PseudoSequence sequence : contexte.getPseudoSequences()){
			// for each itemset
			for(int i =0; i< sequence.size(); i++){ 
				// if the timestamp does not match, then skip it
				if(timestamp != -1 && timestamp != sequence.getTimeStamp(i)){
					continue;
				}
				
				// if the current itemset contain the item
				int index = sequence.indexOf(i, item.getId());
				// if it contains the item and the current itemset is cut at left if the item is in a suffix
				if(index != -1 && sequence.isCutAtLeft(i) == inSuffix){
					// if its not the last item
					if(index != sequence.getSizeOfItemsetAt(i)-1){ 
						// create the projected sequence
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), 
								sequence, i, index+1);
						// if the projected sequence is not empty
						// then add it to the projected database
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						} 
					}else if ((i != sequence.size()-1)){
						// otherwise, if it is not the last itemset of the sequence,
						// create the projected sequence
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), sequence, i+1, 0);
						// if size of pseudo sequence >0, add it to the projected database
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}	
					}	
				}
			}
		}
		// return the projected database
		return new PseudoSequenceDatabase[]{sequenceDatabase};
	}
	
	/**
	 * Do a database projection with a valued item (an item having a value)
	 * @param item  the item
	 * @param database  the database
	 * @param inSuffix if the item was found in a suffix (an itemset cut at right)
	 * @param timestamp the timestamp of the itemsets where the item was found
	 * @return  a set of pseudo-sequence database(s) obtained by the projection
	 */
	private PseudoSequenceDatabase[] buildProjectedContextItemValued(ItemValued item, 
			PseudoSequenceDatabase database, boolean inSuffix, long timestamp) {
		
		// structure that will contain the projected database
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();
		// For clustering, we will keep all the item occurences of the item, removed from the projected sequences.
		// This information is kept in a list ordered by the sequences containing the item.
		List<ItemValued> removedItems = new ArrayList<ItemValued>(); 

		// The item occurrences that are removed to do the projection and that resulted 
		// in an empty pseudo-projected sequence.
		List<ItemValued> removedItemsDestroyed = new ArrayList<ItemValued>(); 
		
		// for each sequence
		for(PseudoSequence sequence : database.getPseudoSequences()){
			// for each itemset in the current sequence
			for(int i =0; i< sequence.size(); i++){ 
				// if the timestamp does not match with the timestamp of the item
				// that is used for projection, skip this itemset
				if(timestamp != -1 && timestamp != sequence.getTimeStamp(i)){
					continue;
				}
				
				
				int index = sequence.indexOf(i, item.getId());
				// if the curren itemset contains the item
				if(index != -1 && sequence.isCutAtLeft(i) == inSuffix){
					// if it is not the last item from the current itemset
					if(index != sequence.getSizeOfItemsetAt(i)-1){ 
						// create new pseudosequence
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), 
								sequence, i, index+1);
						// if the length of the pseudosequence is >0, then add it to the projected database.
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}
						// remember the  item occurences used for the projection
						removedItems.add((ItemValued)sequence.getItemAtInItemsetAt(index, i)); 	 
					}else if(i == sequence.size()-1){
						// if it is the last item from the sequence and the last itemset, the projected 
						// sequence is empty, but we still need to 
						// remember the  item occurences used for the projection
							removedItemsDestroyed.add((ItemValued)sequence.getItemAtInItemsetAt(index, i));
//							removedItems.add(sequence.getItemAtInItemsetAt(index, i));  // AJOUT PHILIPPE 2 OCT
					}else{
						// otherwise if it is not the last itemset from the sequence
						// create new pseudosequence		 
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), sequence, i+1, 0);
						// if the length of the pseudosequence is >0, then add it to the projected database.
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}
						// remember the  item occurences used for the projection
						removedItems.add((ItemValued)sequence.getItemAtInItemsetAt(index, i)); 	
					}	
				}
			}
		}

		// now that the previous for loop has created all the projected sequence, we need to perform
		// the clustering to try to separate the pseudo sequences in several sequence database according
		// to the values that are given to the items.
		return breakInClusters(item, database, sequenceDatabase, removedItems, removedItemsDestroyed);
	}
	

	/**
	 * Separate a pseudo-sequence database into several sequence databases according to the values
	 * associated to occurences of the item used for the pseudo-projection.
	 * @param item the item used for the pseudo-projection
	 * @param database the original sequence database
	 * @param sequenceDatabase the projected sequence database
	 * @param removedItems the item occurrences that were removed to do the projection in a non empty pseudo-projected sequence.
	 * @param removedItemsDestroyed the item occurrences that were removed to do the projection and that resulted in an empty pseudo-projected sequence.
	 * @return
	 */
	private PseudoSequenceDatabase[] breakInClusters(ItemValued item,
			PseudoSequenceDatabase database, PseudoSequenceDatabase sequenceDatabase,
			List<ItemValued> removedItems, List<ItemValued> removedItemsDestroyed) {
		// If no clustering was performed
		if(removedItems.size() == 0 &&
			removedItemsDestroyed.size() ==0) { 
			// return a single sequence database
			return new PseudoSequenceDatabase[]{sequenceDatabase};
		}

		PseudoSequenceDatabase[] sequenceDatabases;
		// if the number of sequences in the projected database is at least twice the minsup threshold,
		// then it would make sence to try to separate the databases according to clusters
		if(sequenceDatabase.getSequenceIDs().size() >= (minsuppRelative *2)){
			// we call the method to separate the database by clusters
			sequenceDatabases = createSequenceDatabasesByClusters(sequenceDatabase, removedItems);
		}else{ // Otherwise, we return a single database
			sequenceDatabases = new PseudoSequenceDatabase[]{sequenceDatabase};
			Cluster cluster = new Cluster(removedItems, removedItemsDestroyed);
			cluster.addItems(removedItemsDestroyed);
			cluster.computeHigherAndLower();
			sequenceDatabase.setCluster(cluster);
		}
//		------------------------------------------------------
		// Extra step: Compute support for each cluster from sequences of the initial database taken
		// as parameter (instread of the projected context, which would be wrong). This could
		// probably be optimized by combining the first loop with this method.
		findSequencesContainingClusters(database, sequenceDatabases, item); // could be optimized
		return sequenceDatabases;
	}
	
	/**
	 * This method 
	 * @param database
	 * @param sequenceDatabases
	 * @param item
	 */
	private void findSequencesContainingClusters(PseudoSequenceDatabase database, PseudoSequenceDatabase[] sequenceDatabases, ItemValued item) {
		// Create a list of clusters corresponding to each sequence database
		Cluster[] clusters = new Cluster[sequenceDatabases.length];
		
		// for each sequence  database
		for(int i=0; i< sequenceDatabases.length; i++){
			// get the corresponding cluster
			clusters[i] = sequenceDatabases[i].getCluster();
			clusters[i].setSequenceIDs(new HashSet<Integer>());
		}
		
		// This set will be used to make sure that we don,t count the same cluster twice
		// for sequences having the same id
		Set<Cluster> alreadyCounted = new HashSet<Cluster>(); 
		// this variable is to remember the last sequence scanned
		PseudoSequence lastSequence = null;

		// for each sequence
		for(PseudoSequence sequence : database.getPseudoSequences()){
			// if the sequence is not null and it has not the same ID as the last sequence scanned
			// clear the set of clusters already seen and update the last sequence seen
			if(lastSequence == null || lastSequence.getId() != sequence.getId()){
				alreadyCounted.clear(); 
				lastSequence = sequence;
			}
			// 
			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item
				for(int j=0; j< sequence.getSizeOfItemsetAt(i); j++){
					ItemSimple item2 = sequence.getItemAtInItemsetAt(j, i);
					// if it is the item that we are looking for
					if(item2.getId() == item.getId()){  
						// find the cluster containing this item
						Cluster cluster = findClusterContainingItem(clusters, (ItemValued)item2);
						// if there is a cluster and that we did not see it yet
						if(cluster != null && !alreadyCounted.contains(cluster)){
							// add the sequence ID to the set of sequence IDs of the cluster
							cluster.getSequenceIDs().add(sequence.getId());
							// rememer that the sequence ID was added to the cluster
							alreadyCounted.add(cluster); 
						}
					}
				}
			}
		}
	}
	
	/**
	 * Find the cluster that contains a given item from an array of clusters
	 * @param clusters the array of clusters
	 * @param item the item
	 * @return the cluster containing the item or null if no cluster contains the item
	 */
	private Cluster findClusterContainingItem(Cluster[] clusters, ItemValued item) {
		// for each cluster
		for(Cluster cluster : clusters){
			// if the cluster contains the item
			if(cluster.containsItem(item)){
				//return the cluster
				return cluster;
			}
		}
		// no cluster contains the item, so return null
		return null;
	}

	/**
	 * This method separate sequences from a sequence database in several sequence databases
	 * according to a set of clusters found
	 * by clustering valued items.
	 * @param database a sequence database
	 * @param items The items to be clustered
	 * @return Un ou plusieurs contexte
	 */
	private PseudoSequenceDatabase[] createSequenceDatabasesByClusters(
			PseudoSequenceDatabase database, List<ItemValued> items) {

		// We associate sequence IDs to each item to make sure that the clusters generated
		// by the clustering algorithm are frequent cluster and that items from the same sequence
		// are note counted more than once for the same sequence ID.
		for(int i=0; i< items.size(); i++){
			items.get(i).setSequenceID(database.getPseudoSequences().get(i).getId());
		}
		
		// Apply the clustering algorithm on the list of items
		List<Cluster> clusters = algoClustering.runAlgorithm(items);
		
		// create a sequenceDatabase for each cluster
		PseudoSequenceDatabase[] sequenceDatabases = new PseudoSequenceDatabase[clusters.size()];
		// For each sequence, assign it to a sequenceDatabase based on the clusters found.
		for(int i=0; i< database.size(); i++){
			// Get the corresponding valued item
			ItemValued item = items.get(i);
			// find the cluster containing the item
			int clusterIndex = clusters.indexOf(item.getCluster());
			if(clusterIndex == -1){  //2010  ADDED THIS TO FIX A PROBLEM
				continue;
			}
			// if the sequence database for this cluster has not been created
			if(sequenceDatabases[clusterIndex] == null){
				// create it 
				sequenceDatabases[clusterIndex] = new PseudoSequenceDatabase();
				sequenceDatabases[clusterIndex].setCluster(clusters.get(clusterIndex));
			}
			// add the sequence to the cluster
			sequenceDatabases[clusterIndex].addSequence(database.getPseudoSequences().get(i));
		}
		// return the sequence databases.
		return sequenceDatabases;
	}

	/**
	 *  This method creates a copy of the sequence and add a given item 
	 *  as a new itemset to the sequence. 
	 *  It sets the support of the sequence as the support of the item.
	 * @param prefix  the sequence
	 * @param item the item
	 * @return the new sequence
	 */
	private Sequence appendItemToSequence(Sequence prefix, ItemSimple item, long timestamp) {
		Sequence newPrefix = prefix.cloneSequence();  // isSuffix
		long decalage = newPrefix.get(newPrefix.size()-1).getTimestamp();
		newPrefix.addItemset(new Itemset(item, timestamp + decalage));  // créé un nouvel itemset   + decalage
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
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  // add to last itemset
		itemset.addItem(item);   
		return newPrefix;
	}
	
	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStatistics() {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : ");
		r.append(patternCount);
		r.append('\n');
//		r.append(patterns.toString(databaseSize));
		r.append("===================================================\n");
		System.out.println(r.toString());
	}
	
	/**
	 * Print the seq. patterns found to System.out. with
	 * @param databaseSize  the size of the database (a number of sequences)
	 */
	public void printResult(int databaseSize) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : ");
		r.append(patternCount);
		r.append('\n');
		r.append(patterns.toString(databaseSize));
		r.append("===================================================\n");
		System.out.println(r.toString());
	}
	
	/**
	 * Get the minsup threshold as a percentage (doule) 
	 * @return a double
	 */
	public double getMinSupp() {
		return minsupp;
	}

	/**
	 * Get the minsup threshold as an integer (sequence count)
	 * @return an integer
	 */
	public int getMinsuppRelative() {
		return minsuppRelative;
	}


	
}
