package ca.pfv.spmf.algorithms.sequential_rules.rulegrowth;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.algorithms.ArraysAlgos;
import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is the original implementation of the RULEGROWTH algorithm for mining sequential rules
 * common to several sequences where antecedent and consequent are unordered itemsets. The RuleGrowth 
 * algorithm is described in this paper:
 * <br/><br/>
 *  Fournier-Viger, P., Nkambou, R. & Tseng, V. S. (2011). 
 *  RuleGrowth: Mining Sequential Rules Common to Several Sequences by Pattern-Growth. 
 *  Proceedings of the 26th Symposium on Applied Computing (ACM SAC 2011). ACM Press, pp. 954-959. 
 * <br/><br/>
 * The main method of this algorithm is "runAlgorithm". It output the result to a file.
 * 
 * @see Occurence
 * @see Sequence
 * @see SequenceDatabase
 * @author Philippe Fournier-Viger
 */
public class AlgoRULEGROWTH {
	//*** for statistics ***/
	long timeStart = 0;  // start time of latest execution
	long timeEnd = 0;  // end time of latest execution
	int ruleCount; // number of rules generated
	
	//*** parameters ***/
	// minimum confidence
	double minConfidence;
	// minimum support
	int minsuppRelative;
	// this is the sequence database
	SequenceDatabase database;
	
	//*** internal variables ***/
	// This map contains for each item (key) a map of occurences (value).
	// The map of occurences associates to sequence ID (key), an occurence of the item (value).
	Map<Integer,  Map<Integer, Occurence>> mapItemCount;  // item, <tid, occurence>

	// object to write the output file
	BufferedWriter writer = null; 

	/**
	 * Default constructor
	 */
	public AlgoRULEGROWTH() {
	}


	/**
	 * The main method to run the algorithm
	 * @param minSupport : the minimum support (percentage as a double value)
	 * @param minConfidence : the minimum confidence threshold
	 * @param input : an input file path of a sequence database
	 * @param output : a file path for writing the output file containing the seq. rules.
	 * @exception IOException if error reading/writing files
	 */
	public void runAlgorithm(double minSupport, double minConfidence, String input, String output) throws IOException {
		try {
			// read the input database
			database = new SequenceDatabase(); 
			database.loadFile(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// convert minimum support to an absolute minimum support (integer)
		this.minsuppRelative = (int) Math.ceil(minSupport * database.size());
		
		// run the algorithm  with the just calculated absolute minimum support
		runAlgorithm(input, output, minsuppRelative, minConfidence);
	}
	
	/**
	 * The main method to run the algorithm
	 * @param relativeMinsup : the minimum support as an integer value (a relative minimum support)
	 * @param minConfidence : the minimum confidence threshold
	 * @param input : an input file path of a sequence database
	 * @param output : a file path for writing the output file containing the seq. rules.
	 * @exception IOException if error reading/writing files
	 */
	public void runAlgorithm(String input, String output, int relativeMinsup, double minConfidence) throws IOException {
		// save the minimum confidence parameter
		this.minConfidence = minConfidence;
		// reinitialize the number of rules found
		ruleCount = 0;
		
		// if the database was not loaded, then load it.
		if(database == null){
			try {
				database = new SequenceDatabase(); 
				database.loadFile(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// reset the stats for memory usage
		MemoryLogger.getInstance().reset();

		// prepare the object for writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// if minsup is 0, set it to 1 to avoid generating
		// rules not in the database
		this.minsuppRelative =  relativeMinsup;
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}

		// save the start time
		timeStart = System.currentTimeMillis(); // for stats

		// Remove infrequent items from the database in one database scan.
		// Then perform another database scan to count the
		// the support of each item in the same database scan 
		// and their occurrences.
		removeItemsThatAreNotFrequent(database);	
		
		// Put frequent items in a list.
		List<Integer> listFrequents = new ArrayList<Integer>();
		// for each item
		for(Entry<Integer,Map<Integer, Occurence>> entry : mapItemCount.entrySet()){
			// if it is frequent
			if(entry.getValue().size() >= minsuppRelative){
				// add it to the list
				listFrequents.add(entry.getKey());
			}
		}
		
		
		// We will now try to generate rules with one item in the
		// antecedent and one item in the consequent using
		// the frequent items.

		// For each pair of frequent items i  and j such that i != j
		for(int i=0; i< listFrequents.size(); i++){
			// get the item I and its map of occurences
			Integer intI = listFrequents.get(i);
			Map<Integer, Occurence> occurencesI = mapItemCount.get(intI);
			// get the tidset of item I
			Set<Integer> tidsI = occurencesI.keySet();
			
			for(int j=i+1; j< listFrequents.size(); j++){
				// get the item j and its map of occurences
				Integer intJ = listFrequents.get(j);
				Map<Integer,Occurence> occurencesJ = mapItemCount.get(intJ);
				// get the tidset of item J
				Set<Integer> tidsJ = occurencesJ.keySet();
				
				// (1) We will now calculate the tidsets
				// of I -->J  and the rule J-->I.
				
				// initialize the sets
				Set<Integer> tidsIJ = new HashSet<Integer>();  // tidset of  I -->J  
				Set<Integer> tidsJI = new HashSet<Integer>(); // tidset of J-->I
				
				// for each occurence of I
				for(Entry<Integer, Occurence> entryOccI : occurencesI.entrySet()){
					// get the occurence of J in the same sequence
					Occurence occJ = occurencesJ.get(entryOccI.getKey());
					// if J appears in that sequence
					if(occJ !=  null){
						// if J appeared before I in that sequence,
						// then we put this tid in the tidset of  J-->I
						if(occJ.firstItemset < entryOccI.getValue().lastItemset){
							tidsJI.add(entryOccI.getKey());
						}
						// if I appeared before J in that sequence,
						// then we put this tid in the tidset of  I-->J
						if(entryOccI.getValue().firstItemset < occJ.lastItemset){
							tidsIJ.add(entryOccI.getKey());
						}
					}
				}
				
				// (2) check if the two itemsets have enough common tids
				// if not, we don't need to generate a rule for them.
				
				// create rule IJ
				if(tidsIJ.size() >= minsuppRelative){
					// calculate the confidence of I ==> J
					double confIJ = ((double)tidsIJ.size()) / occurencesI.size();

					// create itemset of the rule I ==> J
					int[] itemsetI = new int[1];
					itemsetI[0]= intI;
					int[] itemsetJ = new int[1];
					itemsetJ[0]= intJ;
					
					// if the confidence is high enough, save the rule
					if(confIJ >= minConfidence){
						saveRule(tidsIJ, confIJ, itemsetI, itemsetJ);
					}
					// recursive call to try to expand the rule on the left and
					// right sides
					expandLeft(itemsetI, itemsetJ, tidsI, tidsIJ, occurencesJ);
					expandRight(itemsetI, itemsetJ, tidsI, tidsJ, tidsIJ, occurencesI, occurencesJ);
				}
					
				// check if J ==> I has enough common tids
				// If yes, we create the rule J ==> I
				if(tidsJI.size() >= minsuppRelative){
					// create itemset of the rule J ==> I
					int[] itemsetI = new int[1];
					itemsetI[0]= intI;
					int[] itemsetJ = new int[1];
					itemsetJ[0]= intJ;
					
					// calculate the confidence
					double confJI = ((double)tidsJI.size()) / occurencesJ.size();
					
					// if the confidence is high enough, save the rule
					if(confJI >= minConfidence){
						saveRule(tidsJI, confJI, itemsetJ, itemsetI);
					}
					
					// recursive call to try to expand the rule on the left and
					// right sides
					expandRight(itemsetJ, itemsetI, tidsJ,  tidsI, tidsJI, occurencesJ, occurencesI);
					expandLeft(itemsetJ, itemsetI, tidsJ, tidsJI, occurencesI);
				}
			}
		}
		// save end time
		timeEnd = System.currentTimeMillis(); 
		
		// close the file
		writer.close();
		
		// after the algorithm ends, we don't need a reference to the database anymore.
		database = null;
	}

	/**
	 * Save a rule I ==> J to the output file
	 * @param tidsIJ the tids containing the rule
	 * @param confIJ the confidence
	 * @param itemsetI the left part of the rule
	 * @param itemsetJ the right part of the rule
	 * @throws IOException exception if error writing the file
	 */
	private void saveRule(Set<Integer> tidsIJ, double confIJ, int[] itemsetI, int[] itemsetJ) throws IOException {
		// increase the number of rule found
		ruleCount++;
		
		// create a string buffer
		StringBuffer buffer = new StringBuffer();
		
		// write itemset 1 (antecedent)
		for(int i=0; i<itemsetI.length; i++){
			buffer.append(itemsetI[i]);
			if(i != itemsetI.length -1){
				buffer.append(",");
			}
		}
		
		// write separator
		buffer.append(" ==> ");
		
		// write itemset 2  (consequent)
		for(int i=0; i<itemsetJ.length; i++){
			buffer.append(itemsetJ[i]);
			if(i != itemsetJ.length -1){
				buffer.append(",");
			}
		}
		// write support
		buffer.append(" #SUP: ");
		buffer.append(tidsIJ.size());
		// write confidence
		buffer.append(" #CONF: ");
		buffer.append(confIJ);
		writer.write(buffer.toString());
		writer.newLine();
	}


	/**
	 * This method search for items for expanding left side of a rule I --> J 
	 * with any item c. This results in rules of the form I U�{c} --> J. The method makes sure that:
	 *   - c  is not already included in I or J
	 *   - c appear at least minsup time in tidsIJ before last occurence of J
	 *   - c is lexically bigger than all items in I
	 * @throws IOException 
	 */
    private void expandLeft(int [] itemsetI, int[] itemsetJ, Collection<Integer> tidsI, 
    						Collection<Integer> tidsIJ, 
    						Map<Integer, Occurence> occurencesJ) throws IOException {    	
    	// The following map will be used to count the support of each item
    	// c that could potentially extend the rule.
    	// The map associated a set of tids (value) to an item (key).
    	Map<Integer, Set<Integer>> frequentItemsC  = new HashMap<Integer, Set<Integer>>();  
    	
    	// We scan the sequence where I-->J appear to search for items c 
    	// that we could add to generate a larger rule  IU{c} --> J
    	int left = tidsIJ.size();  // the number of tid containing I-->J
    	
    	// For each tid of sequence containing I-->J
    	for(Integer tid : tidsIJ){
    		// get the sequence and occurences of J in that sequence
    		Sequence sequence = database.getSequences().get(tid);
			Occurence end = occurencesJ.get(tid);
			
			// for each itemset before the last occurence of J in that sequence
itemLoop:	for(int k=0; k < end.lastItemset; k++){
				List<Integer> itemset = sequence.get(k);
				// for each item c in that itemset
				for(int m=0; m< itemset.size(); m++){
					Integer itemC = itemset.get(m);
					
					// We will consider if we could create a rule IU{c} --> J
					// If lexical order is not respected or c is included in the rule already,
					// then we cannot so return.
					if(ArraysAlgos.containsLEXPlus(itemsetI, itemC) ||  ArraysAlgos.containsLEX(itemsetJ, itemC)){
						continue;
					}
					
					// Otherwise, we get the tidset of "c" 
					Set<Integer> tidsItemC = frequentItemsC.get(itemC);
					
					// if this set is not null, which means that "c" was not seen yet
					// when scanning the sequences from I==>J
					
					if(tidsItemC == null){ 
						// if there is less tids left in the tidset of I-->J to be scanned than
						// the minsup, we don't consider c anymore because  IU{c} --> J
						// could not be frequent
						if(left < minsuppRelative){
							continue itemLoop;
						}	
					// if "c" was seen before but there is not enough sequences left to be scanned
					// to allow IU{c} --> J to reach the minimum support threshold
					}else if(tidsItemC.size() + left < minsuppRelative){
						// remove c and continue the loop of items
						tidsItemC.remove(itemC);
						continue itemLoop;
					}
					// otherwise, if we did not see "c" yet, create a new tidset for "c"
					if(tidsItemC == null){
						tidsItemC = new HashSet<Integer>(tidsIJ.size());
						frequentItemsC.put(itemC, tidsItemC);
					}
					// add the current tid to the tidset of "c"
					tidsItemC.add(tid);				
				}
			}
			left--;  // decrease the number of sequences left to be scanned
		}
    	
     	// For each item c found, we create a rule	IU{c} ==> J
    	for(Entry<Integer, Set<Integer>> entry : frequentItemsC.entrySet()){
    		Integer itemC = entry.getKey();
    		// get the tidset IU{c} ==> J
    		Set<Integer> tidsIC_J = entry.getValue();
    		
    		// if the support of IU{c} ==> J is enough 
    		if(tidsIC_J.size() >= minsuppRelative){ 
    			
    			// Calculate tids containing IU{c} which is necessary
    			// to calculate the confidence
    			Set<Integer> tidsIC = new HashSet<Integer>(tidsI.size());
    	    	for(Integer tid: tidsI){
    	    		if(mapItemCount.get(itemC).containsKey(tid)){
    	    			tidsIC.add(tid);
    	    		}
    	    	}
    			
    			// Create rule and calculate its confidence of IU{c} ==> J 
    	    	// defined as:  sup(IU{c} -->J) /  sup(IU{c})			
				double confIC_J = ((double)tidsIC_J.size()) / tidsIC.size();
				// create the itemset IU{c}
				int [] itemsetIC = new int[itemsetI.length+1];
				System.arraycopy(itemsetI, 0, itemsetIC, 0, itemsetI.length);
				itemsetIC[itemsetI.length] = itemC;

				// if the confidence is high enough, then it is a valid rule
				if(confIC_J >= minConfidence){
					// save the rule
					saveRule(tidsIC_J, confIC_J, itemsetIC, itemsetJ);
				}
				// recursive call to expand left side of the rule
				expandLeft(itemsetIC, itemsetJ, tidsIC, tidsIC_J, occurencesJ);
    		}
    	}
    	// check the memory usage
    	MemoryLogger.getInstance().checkMemory();
	}
    
	/**
	 * This method search for items for expanding left side of a rule I --> J 
	 * with any item c. This results in rules of the form I --> J U�{c}. The method makes sure that:
	 *   - c  is not already included in I or J
	 *   - c appear at least minsup time in tidsIJ after the first occurence of I
	 *   - c is lexically bigger than all items in J
	 * @throws IOException 
	 */
    private void expandRight(int [] itemsetI, int []itemsetJ,
							Set<Integer> tidsI, 
    						Collection<Integer> tidsJ, 
    						Collection<Integer> tidsIJ, 
    						Map<Integer, Occurence> occurencesI,
    						Map<Integer, Occurence> occurencesJ) throws IOException {
		
    	// The following map will be used to count the support of each item
    	// c that could potentially extend the rule.
    	// The map associated a set of tids (value) to an item (key).
    	Map<Integer, Set<Integer>> frequentItemsC  = new HashMap<Integer, Set<Integer>>();  
    	
    	// we scan the sequence where I-->J appear to search for items c that we could add.
    	// for each sequence containing I-->J.
    	int left = tidsIJ.size();
    	
    	// For each tid of sequence containing I-->J
    	for(Integer tid : tidsIJ){
    		// get the sequence and get occurences of I in that sequence
    		Sequence sequence = database.getSequences().get(tid);
			Occurence first = occurencesI.get(tid);
			
			// for each itemset after the first occurence of I in that sequence
			for(int k=first.firstItemset+1; k < sequence.size(); k++){
				List<Integer> itemset = sequence.get(k);
				// for each item
	itemLoop:	for(int m=0; m< itemset.size(); m++){
					// for each item c in that itemset
					Integer itemC = itemset.get(m);
					
					// We will consider if we could create a rule I --> J U{c}
					// If lexical order is not respected or c is included in the rule already,
					// then we cannot so the algorithm return.
					if(ArraysAlgos.containsLEX(itemsetI, itemC) ||  ArraysAlgos.containsLEXPlus(itemsetJ, itemC)){
						continue;
					}
					Set<Integer> tidsItemC = frequentItemsC.get(itemC);
					
					// if "c" was seen before but there is not enough sequences left to be scanned
					// to allow IU --> J {c} to reach the minimum support threshold
					if(tidsItemC == null){ 
						if(left < minsuppRelative){
							continue itemLoop;
						}	
					}else if(tidsItemC.size() + left < minsuppRelative){
						// if "c" was seen before but there is not enough sequences left to be scanned
						// to allow I--> JU{c}  to reach the minimum support threshold,
						// remove "c" and continue the loop of items
						tidsItemC.remove(itemC);
						continue itemLoop;
					}
					if(tidsItemC == null){
						// otherwise, if we did not see "c" yet, create a new tidset for "c"
						tidsItemC = new HashSet<Integer>(tidsIJ.size());
						frequentItemsC.put(itemC, tidsItemC);
					}
					// add the current tid to the tidset of "c"
					tidsItemC.add(tid);					
				}
			}
			left--;  // decrease the number of sequences left to be scanned
		}
    	
    	// For each item c found, we create a rule	I ==> JU {c}
    	for(Entry<Integer, Set<Integer>> entry : frequentItemsC.entrySet()){
    		Integer itemC = entry.getKey();
    		// get the tidset of I ==> JU {c}
    		Set<Integer> tidsI_JC = entry.getValue();
    		
    		// if the support of I ==> JU{c} is enough 
    		if(tidsI_JC.size() >= minsuppRelative){  
    			// create the itemset JU{c} and calculate the occurences of JU{c}
    			Set<Integer> tidsJC = new HashSet<Integer>(tidsJ.size());
    			Map<Integer, Occurence> occurencesJC = new HashMap<Integer, Occurence>();
    			
    			// for each sequence containing J
    			for(Integer tid: tidsJ){
    				// Get the first and last occurences of C in that sequence
    				Occurence occurenceC = mapItemCount.get(itemC).get(tid);
    				// if there is an occurence
    	    		if(occurenceC != null){
    	    			// add the tid of the sequence to the tidset of JU{c}
    	    			tidsJC.add(tid);
    	    			// calculate last occurence of JU{c} depending on if
    	    			// the last occurence of J is before the last occurence
    	    			// of c or not.
    	    			Occurence occurenceJ = occurencesJ.get(tid);
    	    			if(occurenceC.lastItemset < occurenceJ.lastItemset){
    	    				occurencesJC.put(tid, occurenceC);
    	    			}else{
    	    				occurencesJC.put(tid, occurenceJ);
    	    			}
    	    		}
    	    	}
    			
    			// Create rule I ==> J U{c} and calculate its confidence   
    	    	// defined as:  sup(I -->J U{c}) /  sup(I)	
    			double confI_JC = ((double)tidsI_JC.size()) / tidsI.size();
				int[] itemsetJC = new int[itemsetJ.length+1];
				System.arraycopy(itemsetJ, 0, itemsetJC, 0, itemsetJ.length);
				itemsetJC[itemsetJ.length]= itemC;
				
				// if the confidence is enough
				if(confI_JC >= minConfidence){
					// then it is a valid rule so save it
					saveRule(tidsI_JC, confI_JC, itemsetI, itemsetJC);
				}
				// recursively try to expand the left and right side
				// of the rule
				expandRight(itemsetI, itemsetJC, tidsI, tidsJC, tidsI_JC, occurencesI, occurencesJC);  // occurencesJ
				expandLeft(itemsetI, itemsetJC,  tidsI, tidsI_JC, occurencesJC);  // occurencesJ
    		}
    	}
    	// check the memory usage
    	MemoryLogger.getInstance().checkMemory();
	}

    
	/**
	 * This method calculate the frequency of each item in one database pass.
	 * Then it remove all items that are not frequent.
	 * @param database : a sequence database 
	 * @return A map such that key = item
	 *                         value = a map  where a key = tid  and a value = Occurence
	 * This map allows knowing the frequency of each item and their first and last occurence in each sequence.
	 */
	private Map<Integer, Map<Integer, Occurence>> removeItemsThatAreNotFrequent(SequenceDatabase database) {
		// (1) Count the support of each item in the database in one database pass
		mapItemCount = new HashMap<Integer, Map<Integer, Occurence>>(); // <item, Map<tid, occurence>>
		
		// for each sequence in the database
		for(int k=0; k< database.size(); k++){
			Sequence sequence = database.getSequences().get(k);
			// for each itemset in that sequence
			for(short j=0; j< sequence.getItemsets().size(); j++){
				List<Integer> itemset = sequence.get(j);
				// for each item in that itemset
				for(int i=0; i< itemset.size(); i++){
					Integer itemI = itemset.get(i);
					
					// get the map of occurences of that item
					Map<Integer, Occurence> occurences = mapItemCount.get(itemI);
					// if this map is null, create a new one
					if(occurences == null){
						occurences = new HashMap<Integer, Occurence>();
						mapItemCount.put(itemI, occurences);
					}
					// then update the occurence by adding j as the 
					// last occurence in sequence k
					Occurence occurence = occurences.get(k);
					if(occurence == null){
						occurence = new Occurence(j, j);
						occurences.put(k, occurence);
					}else{
						occurence.lastItemset = j;
					}
				}
			}
		}
//		System.out.println("NUMBER OF DIFFERENT ITEMS : " + mapItemCount.size());
		// (2) remove all items that are not frequent from the database
		
		// for each sequence
		for(Sequence sequence : database.getSequences()){
			int i=0;
			
			// for each itemset
			while(i < sequence.getItemsets().size()){
				List<Integer> itemset = sequence.getItemsets().get(i);
				int j=0;
				
				// for each item
				while(j < itemset.size()){
					// if the item is not frequent remove it
					if( mapItemCount.get(itemset.get(j)).size() < minsuppRelative){
						itemset.remove(j);
					}else{
						// otherwise go to next item
						j++;
					}
				}
				i++;  // go to next itemset
			}
		}
		// return the map of occurences of items
		return mapItemCount;
	}
	
	/**
	 * Print statistics about the last algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  RULEGROWTH - STATS ========");
		System.out.println("Sequential rules count: " + ruleCount);
		System.out.println("Total time: " + (timeEnd - timeStart) + " ms");
		System.out.println("Max memory: " + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("==========================================");
	}

}
