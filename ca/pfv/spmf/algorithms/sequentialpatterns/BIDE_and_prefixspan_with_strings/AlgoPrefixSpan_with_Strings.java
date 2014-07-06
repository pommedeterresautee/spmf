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
 * This is an implementation of the PrefixSpan algorithm by Pei et al. 2001 modfied to take
 * sequences of strings as input instead of sequences of integers.
 * <br/><br/>
 * 
 * NOTE: This implementation saves the pattern  to a file as soon 
 * as they are found or can keep the pattern into memory if no output path
 * is provided to the runAlgorithm() method.
<br/><br/>

 *  In future a version of SPMF, it is planned to remove this package and to provide a more general
 *  mechanism for handling strings in sequences that would work for all algorithms 
 *  that take sequences as input. But this has not been done yet.
 *  
  @see Sequence
  @see SequenceDatabase
  @see SequentialPattern
  @see SequentialPatterns
  @see Pair
  @see PseudoSequence
* @author Philippe Fournier-Viger
 */

public class AlgoPrefixSpan_with_Strings{
		
	// for statistics
	private long startTime;
	private long endTime;
	
	// the number of pattern found
	private int patternCount;
		
	// absolute minimum support
	private int minsuppAbsolute;
	
	// writer to write output file
	BufferedWriter writer = null;
	
	// The sequential patterns that are found 
	// (if the user want to keep them into memory)
	private SequentialPatterns patterns = null;
	
	/**
	 * Default constructor
	 */
	public AlgoPrefixSpan_with_Strings(){
		
	}
	
	/**
	 * Run the algorithm
	 * @param database : a sequence database
	 * @param minsup  :  the minimum support as an integer
	 * @param outputFilePath : the path of the output file to save the result
	 *                         or null if you want the result to be saved into memory
	 * @return return the result, if saved into memory, otherwise null 
	 * @throws IOException  exception if error while writing the file
	 */
	public SequentialPatterns runAlgorithm(SequenceDatabase database, String outputFilePath, int minsup) throws IOException {
		// initialize variables for statistics
		patternCount =0;
		MemoryLogger.getInstance().reset(); // to check the memory usage
		
		// keep the minimum support because we will need it
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
		
		// We have to scan the database to find all frequent patterns of size 1.
		// We note the sequences in which these patterns appear.
		Map<String, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE ITON A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. (OPTIMIZATION : OCTOBER-08 )
		
		// Create a list of pseudosequence
		List<PseudoSequence> initialContext = new ArrayList<PseudoSequence>();
		// for each sequence in  the database
		for(Sequence sequence : database.getSequences()){
			// remove infrequent items
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppAbsolute);
			if(optimizedSequence.size() != 0){
				// if the size is > 0, create a pseudo sequence with this sequence
				initialContext.add(new PseudoSequence(optimizedSequence, 0, 0));
			}
		}
		
		// For each item
		for(Entry<String, Set<Integer>> entry : mapSequenceID.entrySet()){
			// if the item is frequent  (has a support >= minsup)
			if(entry.getValue().size() >= minsuppAbsolute){ // if the item is frequent
				// build the projected context
				String item = entry.getKey();
				List<PseudoSequence> projectedContext = buildProjectedContext(item, initialContext,  false);

				// Create the prefix for the projected context.
				SequentialPattern prefix = new SequentialPattern(0);  
				prefix.addItemset(new Itemset(item));
				prefix.setSequencesID(entry.getValue());
				
				// The prefix is a frequent sequential pattern.
				// We save it in the result.
				savePattern(prefix);  // we found a sequence.
				
				// Recursive call !
				recursion(prefix, projectedContext); 
				
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
			StringBuffer r = new StringBuffer("");
			for(Itemset itemset : prefix.getItemsets()){
	//			r.append('(');
				for(String item : itemset.getItems()){
					String string = item.toString();
					r.append(string);
					r.append(' ');
				}
				r.append("-1 ");
			}
	//
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
		}// otherwise the result is kept into memory
		else{
			patterns.addSequence(prefix, prefix.size());
		}
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
		// for each sequence in the current database
		for(Sequence sequence : contexte.getSequences()){
			// for each itemset in this sequence
			for(List<String> itemset : sequence.getItemsets()){
				// for each item
				for(String item : itemset){
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
//					}
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

	private List<PseudoSequence> buildProjectedContext(String item, List<PseudoSequence> database, boolean inSuffix) {
		// We create a new projected database
		List<PseudoSequence> sequenceDatabase = new ArrayList<PseudoSequence>();
		
		// for each sequence in the database received as parameter
		for(PseudoSequence sequence : database){ // for each sequence
			for(int i =0; i< sequence.size(); i++){  // for each item of the sequence
				
				// check if the itemset contains the item that we use for the projection
				int index = sequence.indexOf(i, item);
				// if it does not, and the current item is part of a suffix if inSuffix is true
				//   and vice-versa
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					// if this is not the last item of the itemset of this sequence
					if(index != sequence.getSizeOfItemsetAt(i)-1){ // if this is not the last item of the itemset
						// create a new pseudo sequence
						PseudoSequence newSequence = new PseudoSequence( 
								sequence, i, index+1);
						if(newSequence.size() >0){
							sequenceDatabase.add(newSequence);
						} 
					}else if ((i != sequence.size()-1)){// if this is not the last itemset of the sequence			 
						// create a new pseudo sequence
						PseudoSequence newSequence = new PseudoSequence( sequence, i+1, 0);
						if(newSequence.size() >0){
							// if the size of this pseudo sequence is greater than 0
							// add it to the projected database.
							sequenceDatabase.add(newSequence);
						}	
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
	private void recursion(SequentialPattern prefix, List<PseudoSequence> database) throws IOException {	
		// find frequent items of size 1 in the current projected database.
		Set<Pair> pairs = findAllFrequentPairs(prefix, database);
		
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
				// build the projected database with this item
				List<PseudoSequence> projectedDB = buildProjectedContext(pair.getItem(), database, pair.isPostfix());

				newPrefix.setSequencesID(pair.getSequencesID()); 
				// save the pattern
				savePattern(newPrefix);
				// make a recursive call
				recursion(newPrefix, projectedDB);
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * Method to find all frequent items in a projected sequence database
	 * @param sequences  the set of sequences
	 * @return A list of pairs, where a pair is an item with (1) a boolean indicating if it
	 *         is in an itemset that is "cut" and (2) the sequence IDs where it occurs.
	 */
	protected Set<Pair> findAllFrequentPairs(SequentialPattern prefix, List<PseudoSequence> sequences){
		// We use a Map the store the pairs.
		Map<Pair, Pair> mapPairs = new HashMap<Pair, Pair>();
		// for each sequence
		for(PseudoSequence sequence : sequences){
			// for each itemset
			for(int i=0; i< sequence.size(); i++){
				// for each item
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					String item = sequence.getItemAtInItemsetAt(j, i);
					// create the pair corresponding to this item
					Pair paire = new Pair(sequence.isPostfix(i), item);   // false is ok?
					// get the pair object store in the map if there is one already
					Pair oldPaire = mapPairs.get(paire);
					// if there is no pair object yet
					if(oldPaire == null){
						// store the pair object that we created
						mapPairs.put(paire, paire);
					}else{
						// otherwise use the old one
						paire = oldPaire;
					}
					// record the current sequence id for that pair
					paire.getSequencesID().add(sequence.getId());
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
	private SequentialPattern appendItemToSequence(SequentialPattern prefix, String item) {
		SequentialPattern newPrefix = prefix.cloneSequence(); 
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
		private SequentialPattern appendItemToPrefixOfSequence(SequentialPattern prefix, String item) {
		SequentialPattern newPrefix = prefix.cloneSequence();
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  
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
		System.out.println(r.toString());
	}

}
