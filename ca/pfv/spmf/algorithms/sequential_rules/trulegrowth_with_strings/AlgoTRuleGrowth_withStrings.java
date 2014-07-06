package ca.pfv.spmf.algorithms.sequential_rules.trulegrowth_with_strings;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth.AlgoTRuleGrowth;
import ca.pfv.spmf.algorithms.sequential_rules.trulegrowth.Occurence;
import ca.pfv.spmf.input.sequence_database_list_strings.Sequence;
import ca.pfv.spmf.input.sequence_database_list_strings.SequenceDatabase;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is a modified implementation of the TRULEGROWTH algorithm for mining sequential rules from
 * sequences containing Strings instead of integers.  
 * <br/><br/>
 * 
 * Fournier-Viger, P., Wu, C.-W., Tseng, V.S., Nkambou, R. (2012). 
 *  Mining Sequential Rules Common to Several Sequences with the Window Size Constraint. 
 *  Proceedings of the 25th Canadian Conf. on Artificial Intelligence (AI 2012), 
 *  Springer, LNAI 7310, pp.299-304. 
 * <br/><br/>
 *  
 *  In future a version of SPMF, it is planned to remove this class and to provide a more general
 *  mechanism for handling strings that would work for all algorithms that take sequences as input. 
 *  
 * @see Itemset
 * @see AlgoTRuleGrowth
 *@see Sequence
 *@see SequenceDatabase
 *@author Philippe Fournier-Viger
 */
public class AlgoTRuleGrowth_withStrings {
	
	// statistics
	long timeStart = 0;  // start time of latest execution
	long timeEnd = 0;    // end time of latest execution
	
	// A map to record the occurences of each item in each sequence
	// KEY: an item
	// VALUE:  a map of  key: sequence ID  value: occurences of the item in that sequence.
	// (note: an occurence is an itemset position)
	Map<String,  Map<Integer, Occurence>> mapItemCount;
	
	// PARAMETERS OF THE ALGORITHM
	SequenceDatabase database; // a sequence database
	double minconf;   // minimum confidence
	int minsuppRelative; // minimum support
	int windowSize =0;  // window size

	// The number of patterns found
	int ruleCount;
	
	// object to write the output file
	BufferedWriter writer = null; 

	/**
	 * Default constructor
	 */
	public AlgoTRuleGrowth_withStrings() {
	}
	
	/**
	 * Run the algorithm.  
	 * @param minSupport  Minsup as a percentage (ex: 0.05 = 5 %)
	 * @param minConfidence minimum confidence (a value between 0 and 1).
	 * @param input  the input file path
	 * @param output the output file path
	 * @param windowSize a window size
	 * @throws IOException exception if there is an error reading/writing files
	 */
	public void runAlgorithm(double minSupport, double minConfidence, String input, String output, int windowSize) throws IOException{
		// load the input file into memory
		try {
			this.database = new SequenceDatabase();
			database.loadFile(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// convert minimum support to a relative minimum support (integer)
		this.minsuppRelative = (int) Math.ceil(minSupport * database.size());
		// run the algorithm
		runAlgorithm(input, output, minsuppRelative, minConfidence, windowSize);
	}
	
	/**
	 * Run the algorithm.
	 * @param relativeMinSupport  minsup as a a relative value (integer)
	 * @param minConfidence minimum confidence (a value between 0 and 1).
	 * @param input  the input file path
	 * @param output the output file path
	 * @param windowSize a window size
	 * @throws IOException exception if there is an error reading/writing files
	 */
	public void runAlgorithm(String input, String output, int relativeMinSupport, double minConfidence, int windowSize 
			) throws IOException{
		this.minconf = minConfidence;
		
		// read the database into memory
		if(database == null){
			try {
				this.database = new SequenceDatabase();
				database.loadFile(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// IMPORTANT : THIS IS A FIX SO THAT THE DEFINITION IS THE SAME AS IN THE ARTICLE!!
		this.windowSize = windowSize + 1;  
		
		// if minsup is 0, set it to 1
		this.minsuppRelative = relativeMinSupport;
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}

		// reset the stats for memory usage
		MemoryLogger.getInstance().reset();
		// prepare the object for writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 

		// save the start time
		timeStart = System.currentTimeMillis(); // for stats
		
		// remove infrequent items from the database
		removeItemsThatAreNotFrequent(database);	
		
		// note frequent items in a list "listFrequents"
		List<String> listFrequents = new ArrayList<String>();
		// for each item
		for(Entry<String,Map<Integer, Occurence>> entry : mapItemCount.entrySet()){
			// if it is frequent
			if(entry.getValue().size() >= minsuppRelative){
				// add the item to the list
				listFrequents.add(entry.getKey());
			}
		}
		
		// FOR EACH FREQUENT ITEM WE COMPARE WITH EACH OTHER FREQUENT ITEM TO 
		// TRY TO GENERATE A RULE 1-1.
		for(int i=0; i< listFrequents.size(); i++){
			String intI = listFrequents.get(i);
			Map<Integer,Occurence> occurencesI = mapItemCount.get(intI);
			for(int j=i+1; j< listFrequents.size(); j++){
				String intJ = listFrequents.get(j);
				Map<Integer,Occurence> occurencesJ = mapItemCount.get(intJ);
				
				// (1) Calculate tidsI, tidsJ, tidsJ-->J  and tidsI->J
				Set<Integer> tidsI = new HashSet<Integer>();
				Set<Integer> tidsJ = null;
				Set<Integer> tidsIJ = new HashSet<Integer>();
				Set<Integer> tidsJI= new HashSet<Integer>();

				// for each occurence of I
	looptid:	for(Occurence occI : occurencesI.values()){
					// add the sequenceID to tidsI
					tidsI.add(occI.sequenceID);
					
					// if J does not appear in that sequence continue loop
					Occurence occJ = occurencesJ.get(occI.sequenceID);
					if(occJ == null){
						continue looptid;
					}
					
					// make a big loop to compare if I appears before
					// J in that sequence and
					// if J appears before I
					boolean addedIJ= false;
					boolean addedJI= false;
					// for each occurence of I in that sequence
			loopIJ:	for(Short posI : occI.occurences){
						// for each occurence of J in that sequence
						for(Short posJ : occJ.occurences){
							if(!posI.equals(posJ) && Math.abs(posI - posJ) <= windowSize){
								if(posI <= posJ){
									// if I is before J
									tidsIJ.add(occI.sequenceID);
									addedIJ = true;
								}else{
									// if J is before I
									tidsJI.add(occI.sequenceID);
									addedJI = true;
								}
								// if we have found that I is before J and J is before I
								// we don't need to continue.
								if(addedIJ && addedJI){
									break loopIJ;
								}
							}
						}
					}
				}
				// END
				
				// (2) check if the two itemsets have enough common tids
				// if not, we don't need to generate a rule for them.
				// create rule IJ
				if(tidsIJ.size() >= minsuppRelative){
					// calculate the confidence of I ==> J
					double confIJ = ((double)tidsIJ.size()) / occurencesI.size();

					// create itemset of the rule I ==> J
					String[] itemset1 = new String[]{intI};
					String[] itemset2 = new String[]{intJ};
					
					// if the confidence is high enough, save the rule
					if(confIJ >= minConfidence){
						saveRule(tidsIJ, confIJ, itemset1, itemset2);
					}
					// Calculate tidsJ.
					tidsJ = new HashSet<Integer>();
					for(Occurence occJ : occurencesJ.values()){
						tidsJ.add(occJ.sequenceID);
					}
					
					// recursive call to try to expand the rule
					expandLeft(itemset1, itemset2, tidsI, tidsIJ);
					expandRight(itemset1, itemset2, tidsI, tidsJ, tidsIJ);
				}
					
				// create rule JI
				if(tidsJI.size() >= minsuppRelative){
						double confJI = ((double)tidsJI.size()) / occurencesJ.size();
 
						// create itemsets for that rule
						String[] itemset1 = new String[]{intI};
						String[] itemset2 = new String[]{intJ};
						
						// if the rule has enough confidence, save it!
						if(confJI >= minConfidence){
							saveRule(tidsJI, confJI, itemset2, itemset1);
//							rules.addRule(ruleJI);
						}
						
						// Calculate tidsJ.
						if(tidsJ == null){
							tidsJ = new HashSet<Integer>();
							for(Occurence occJ : occurencesJ.values()){
								tidsJ.add(occJ.sequenceID);
							}
						}
						// recursive call to try to expand the rule
						expandRight(itemset2, itemset1, tidsJ,  tidsI, tidsJI /*, occurencesJ, occurencesI*/);
						expandLeft(itemset2, itemset1, tidsJ, tidsJI /*, occurencesI*/);
					}
				}
		}
		// save the end time for the execution of the algorithm
		timeEnd = System.currentTimeMillis(); // for stats
		
		// close the file
		writer.close();
		database = null;
	}

	/**
	 * This method search for items for expanding left side of a rule I --> J 
	 * with any item c. This results in rules of the form I U {c} --> J. The method makes sure that:
	 *   - c  is not already included in I or J
	 *   - c appear at least minsup time in tidsIJ before last occurence of J
	 *   - c is lexically bigger than all items in I
	 * @param itemsetI the left side of a rule (see paper)
	 * @param itemestJ the right side of a rule (see paper)
	 * @param tidsI the tids set of I
	 * @param tidsJ the tids set of J
	 * @throws IOException  exception if error while writing output file
	 */
    private void expandLeft(String[] itemsetI, String[] itemsetJ,
    						Collection<Integer> tidsI, 
    						Collection<Integer> tidsIJ // ,
//    						Map<Integer, Occurence> mapOccurencesJ
    						) throws IOException {    	
    	
    	
    	if(itemsetI.length ==2 && itemsetI[0].equals("a") && itemsetI[1].equals("b") && itemsetJ[0].equals("d") ){

    		System.out.println();
    	}
//    	
    	// map-key: item   map-value: set of tids containing the item
    	Map<String, Set<Integer>> frequentItemsC  = new HashMap<String, Set<Integer>>();  
    	
    	////////////////////////////////////////////////////////////////////////
    	// for each sequence containing I-->J
    	for(Integer tid : tidsIJ){
    		Sequence sequence = database.getSequences().get(tid);
    		
    		LinkedHashMap<String, Integer> mapMostLeftFromI = new LinkedHashMap<String, Integer>();
    		LinkedHashMap<String, Integer> mapMostLeftFromJ = new LinkedHashMap<String, Integer>();
    		LinkedHashMap<String, LinkedList<Integer>> mapMostRightFromJ = new LinkedHashMap<String, LinkedList<Integer>>();

        	int lastItemsetScannedForC = Integer.MAX_VALUE;
        	
    		// For each itemset starting from the last...
        	int k= sequence.size()-1;
        	do{
    			final int fistElementOfWindow = k;    //  - windowSize +1
    			final int lastElementOfWindow = k + windowSize -1; 
    			
    			// remove items from J that fall outside the time window
    			int previousJSize = mapMostLeftFromJ.size();
    			removeElementOutsideWindow(mapMostLeftFromJ, lastElementOfWindow);
    			// important: if J was all there, but become smaller we need to clear the
    			// hashmap for items of I.
    			int currentJSize = mapMostLeftFromJ.size();
    			if(previousJSize == itemsetJ.length && previousJSize != currentJSize){
    				mapMostLeftFromI.clear();
    			}
				
    			// remove items from I that fall outside the time window
    			removeElementOutsideWindow(mapMostLeftFromI, lastElementOfWindow);
    			
    			// For each item of the current itemset
    			for(String item : sequence.get(k)){
    				// record the first position until now of each item in I or J
    				if(mapMostLeftFromJ.size() == itemsetJ.length  && contains(itemsetI, item)){ 
    					addToLinked(mapMostLeftFromI, item, k);
    				}else if(contains(itemsetJ, item)){ 
    					addToLinked(mapMostLeftFromJ, item, k);
    					LinkedList<Integer> list = mapMostRightFromJ.get(item);
    					if(list == null){
    						list = new LinkedList<Integer>();
    						addToLinked(mapMostRightFromJ, item, list);
    					}
    					list.add(k);
    				}
    			}
 
    			// if all the items of IJ are in the current window
    			if(mapMostLeftFromI.size() == itemsetI.length && mapMostLeftFromJ.size() == itemsetJ.length){
    				
    				//remove items from mostRight that fall outside the time window.
        			// at the same time, calculate the minimum index for items of J.
        			int minimum = Integer.MAX_VALUE;
        			for(LinkedList<Integer> list: mapMostRightFromJ.values()){
        				while(true){
        					Integer last = list.getLast();
        					if(last > lastElementOfWindow){
        						list.removeLast();
        					}else{ 
        						if(last < minimum){
                					minimum = last -1;
                				}
        						break;
    						}
    					}
    				}
    				
	    			// we need to scan for items C to extend the rule...	
	    		    // Such item c has to appear in the window before the last occurence of J (before "minimum")
	    		    // and if it was scanned before, it should not be scanned again.
    				int itemsetC = minimum;
    				if(itemsetC >= lastItemsetScannedForC){
    					itemsetC = lastItemsetScannedForC -1;
    				}
    				
    				for(; itemsetC >= fistElementOfWindow; itemsetC--){
    					for(String itemC : sequence.get(itemsetC)){
//    						if lexical order is not respected or c is included in the rule already.			
							if(containsLEXPlus(itemsetI, itemC)  
						   || containsLEX(itemsetJ, itemC)){
								continue;
							}	
							Set<Integer> tidsItemC = frequentItemsC.get(itemC);
							if(tidsItemC == null){
								tidsItemC = new HashSet<Integer>();
								frequentItemsC.put(itemC, tidsItemC);
							}
							tidsItemC.add(tid);	
    					}
    				}
    				lastItemsetScannedForC = fistElementOfWindow;
    			}
    			k--;
        	}while(k >= 0  && lastItemsetScannedForC >0);
 		}
    	////////////////////////////////////////////////////////////////////////

     	// for each item c found, we create a rule	 	
    	for(Entry<String, Set<Integer>> entry : frequentItemsC.entrySet()){
    		Set<Integer> tidsIC_J = entry.getValue();
    		
    		// if the support is enough      Sup(R)  =  sup(IC -->J)
    		if(tidsIC_J.size() >= minsuppRelative){ 
    			String itemC = entry.getKey();
    			String [] itemsetIC = new String[itemsetI.length+1];
				System.arraycopy(itemsetI, 0, itemsetIC, 0, itemsetI.length);
				itemsetIC[itemsetI.length] = itemC;
				
				if(itemC.equals("f") && itemsetIC[0].equals("a")){
					System.out.println("6");
				}

    			// ---- CALCULATE ALL THE TIDS CONTAINING IC WITHIN A TIME WINDOW ---
    			Set<Integer> tidsIC = new HashSet<Integer>();
   loop1:	    for(Integer tid: tidsI){
    	    		Sequence sequence = database.getSequences().get(tid);
    	    		// MAP: item : itemset index
    	    		LinkedHashMap<String, Integer> mapAlreadySeenFromIC = new LinkedHashMap<String, Integer>();
    	    		
    	    		// For each itemset
    	    		for(int k=0; k< sequence.size(); k++){
    					// For each item
    	    			for(String item : sequence.get(k)){
    	    				if(contains(itemsetIC, item)){ // record the last position of each item in IC
    	    					addToLinked(mapAlreadySeenFromIC, item, k);
    	    				}
    	    			}
    	    			// remove items that fall outside the time window
    	    			Iterator<Entry<String, Integer>> iter = mapAlreadySeenFromIC.entrySet().iterator();
    	    			while(iter.hasNext()){
    	    				Entry<String, Integer> entryMap = iter.next();
    	    				if(entryMap.getValue() < k - windowSize +1){
    	    					iter.remove();
    	    				}else{
    	    					break;
    	    				}
    	    			}
    	    			// if all the items of I are inside the current window, then record the tid
    	    			if(mapAlreadySeenFromIC.keySet().size() == itemsetIC.length){
    	    				tidsIC.add(tid);
    	    				continue loop1;
    	    			}
    	    		}
    	    	}
    			// ----  ----
    			
    			// Create rule and calculate its confidence:  Conf(r) = sup(IUC -->J) /  sup(IUC)			
				double confIC_J = ((double)tidsIC_J.size()) / tidsIC.size();

				if(confIC_J >= minconf){
					saveRule(tidsIC_J, confIC_J, itemsetIC, itemsetJ);
				}
				
				// recursive call to expand left side of the rule
				expandLeft(itemsetIC, itemsetJ, tidsIC, tidsIC_J );
    		}
    	}

    	MemoryLogger.getInstance().checkMemory();
    	////////////////////////////////////////////////////////////////////////
	}

    // this method is to make sure that the insertion order is preserved.
    // It was necessary to do that because when an element is re-inserted in a linked list,
    // the access order remain the one of the first insertion. 
	private void addToLinked(LinkedHashMap<String, LinkedList<Integer>> mapMostLeftFromI,
			String key, LinkedList<Integer> value) {
		if(mapMostLeftFromI.containsKey(key)){
			mapMostLeftFromI.remove(key);
		}
		mapMostLeftFromI.put(key, value);
	}
	
	private void addToLinked(LinkedHashMap<String, Integer> mapMostLeftFromI,
			String key, Integer value) {
		if(mapMostLeftFromI.containsKey(key)){
			mapMostLeftFromI.remove(key);
		}
		mapMostLeftFromI.put(key, value);
	}

	/**
	 * This method removes elements out of the current window from a  hashmap containing
	 * the position of items at the left of an itemset.
	 *  key: item   value : a itemset position
	 * @param mapMostLeftFromItemset  the map
	 * @param lastElementOfWindow  the last itemset of the window in terms of itemset position
	 *                             in the sequence.
	 */
	private void removeElementOutsideWindow(
			LinkedHashMap<String, Integer> mapMostLeftFromI,
			final int lastElementOfWindow) {
		// iterate over elements of the map
		Iterator<Entry<String, Integer>> iter = mapMostLeftFromI.entrySet().iterator();
		while(iter.hasNext()){
			// if the position is outside the window, remove it
			if(iter.next().getValue() > lastElementOfWindow){
				iter.remove();
			}else{
				// otherwise, we break
				break;
			}
		}
	}
	
	/**
	 * This method removes elements out of the current window from a  hashmap containing
	 * the position of items at the right of an itemset.
	 *  key: item   value : a itemset position
	 * @param mapMostLeftFromItemset  the map
	 * @param lastElementOfWindow  the last itemset of the window in terms of itemset position
	 *                             in the sequence.
	 */
	private void removeElementOutsideWindowER(
			LinkedHashMap<String, Integer> mapMostRightfromI,
			final int firstElementOfWindow) {
		// iterate over elements of the map
		Iterator<Entry<String, Integer>> iter = mapMostRightfromI.entrySet().iterator();
		while(iter.hasNext()){
			// if the position is outside the window, remove it
			Entry<String, Integer> entry = iter.next();
			if(entry.getValue() < firstElementOfWindow){
				iter.remove();
			}else{
				// otherwise, we break
				break;
			}
		}
	}
    
	/**
	 * This method search for items for expanding left side of a rule I --> J 
	 * with any item c. This results in rules of the form I --> J U {c}. The method makes sure that:
	 *   - c  is not already included in I or J
	 *   - c appear at least minsup time in tidsIJ after the first occurence of I
	 *   - c is lexically bigger than all items in J
	 * @param mapWindowsJI 
	 * @throws IOException 
	 */
    private void expandRight(String[] itemsetI, String[] itemsetJ, 
							Set<Integer> tidsI, 
    						Collection<Integer> tidsJ, 
    						Collection<Integer> tidsIJ //,
//    						Map<Integer, Occurence> occurencesI,
//    						Map<Integer, Occurence> occurencesJ
    						) throws IOException {

//    	// map-key: item   map-value: set of tids containing the item
    	Map<String, Set<Integer>> frequentItemsC  = new HashMap<String, Set<Integer>>();  
    	
    	// for each sequence containing I-->J
    	 for(Integer tid : tidsIJ){
    		Sequence sequence = database.getSequences().get(tid);
    		
    		LinkedHashMap<String, Integer> mapMostRightFromI = new LinkedHashMap<String, Integer>();
    		LinkedHashMap<String, Integer> mapMostRightFromJ = new LinkedHashMap<String, Integer>();
    		LinkedHashMap<String, LinkedList<Integer>> mapMostLeftFromI = new LinkedHashMap<String, LinkedList<Integer>>();

        	int lastItemsetScannedForC = Integer.MIN_VALUE;
        	
    		// For each itemset starting from the first...
        	int k= 0;
        	do{
    			final int firstElementOfWindow = k - windowSize +1;   
    			int lastElementOfWindow = k; 
				
    			// remove items from I that fall outside the time window
    			int previousISize = mapMostRightFromI.size();
    			removeElementOutsideWindowER(mapMostRightFromI, firstElementOfWindow);
    			// important: if I was all there, but become smaller we need to clear the
    			// hashmap for items of J.
    			int currentISize = mapMostRightFromI.size();
    			if(previousISize == itemsetJ.length && previousISize != currentISize){
    				mapMostRightFromJ.clear();
    			}

    			// remove items from J that fall outside the time window
    			removeElementOutsideWindowER(mapMostRightFromJ, firstElementOfWindow);
    			
    			// For each item of the current itemset
    			for(String item : sequence.get(k)){
    				// record the first position until now of each item in I or J
    				if(mapMostRightFromI.size() == itemsetI.length && 	contains(itemsetJ, item)){ 
    					addToLinked(mapMostRightFromJ, item, k);
    				}else if(contains(itemsetI, item)){ 
    					addToLinked(mapMostRightFromI, item, k);
    					LinkedList<Integer> list = mapMostLeftFromI.get(item);
    					if(list == null){
    						list = new LinkedList<Integer>();
    						addToLinked(mapMostLeftFromI, item, list);
    					}
    					list.add(k);
    				}
    			}
 
    			// if all the items of IJ are in the current window
    			if(mapMostRightFromI.size() == itemsetI.length && mapMostRightFromJ.size() == itemsetJ.length){
    				
    				//remove items from mostLeft that fall outside the time window.
        			// at the same time, calculate the minimum index for items of I.
        			int minimum = 1;
        			for(LinkedList<Integer> list: mapMostLeftFromI.values()){
        				while(true){
        					Integer last = list.getLast();
        					if(last < firstElementOfWindow){
        						list.removeLast();
        					}else{ 
        						if(last > minimum){
                					minimum = last + 1;  
                				}
        						break;
    						}
    					}
    				}
    				
	    			// we need to scan for items C to extend the rule...	
	    		    // Such item c has to appear in the window before the last occurence of J (before "minimum")
	    		    // and if it was scanned before, it should not be scanned again.
    				int itemsetC = minimum;
    				if(itemsetC < lastItemsetScannedForC){
    					itemsetC = lastItemsetScannedForC +1;
    				}
    				
    				for(; itemsetC <= lastElementOfWindow; itemsetC++){
    					for(String itemC : sequence.get(itemsetC)){
//    	    						if lexical order is not respected or c is included in the rule already.			
							if(containsLEX(itemsetI, itemC) 
						   ||  containsLEXPlus(itemsetJ, itemC)){
								continue;
							}	
							Set<Integer> tidsItemC = frequentItemsC.get(itemC);
							if(tidsItemC == null){
								tidsItemC = new HashSet<Integer>();
								frequentItemsC.put(itemC, tidsItemC);
							}
							tidsItemC.add(tid);	
    					}
    				}
    				lastItemsetScannedForC = lastElementOfWindow;
    			}
    			k++;
        	}while(k < sequence.size() && lastItemsetScannedForC < sequence.size()-1);
 		}  	
    	 
      	////////////////////////////////////////////////////////////////////////
    	// for each item c found, we create a rule	 	
     	for(Entry<String, Set<Integer>> entry : frequentItemsC.entrySet()){
     		Set<Integer> tidsI_JC = entry.getValue();
     		
     		// if the support is enough      Sup(R)  =  sup(IC -->J)
     		if(tidsI_JC.size() >= minsuppRelative){ 
     			String itemC = entry.getKey();
         		String[] itemsetJC = new String[itemsetJ.length+1];
				System.arraycopy(itemsetJ, 0, itemsetJC, 0, itemsetJ.length);
				itemsetJC[itemsetJ.length]= itemC;
//
//     			Itemset itemsetJC = new Itemset(ruleIJ.getItemset2()); 
// 				itemsetJC.addItem(itemC);
 				
     			// ---- CALCULATE ALL THE TIDS CONTAINING JC WITHIN A TIME WINDOW ---
     			Set<Integer> tidsJC = new HashSet<Integer>();
    loop1:	    for(Integer tid: tidsJ){
     	    		Sequence sequence = database.getSequences().get(tid);
     	    		// MAP: item : itemset index
     	    		LinkedHashMap<String, Integer> mapAlreadySeenFromJC = new LinkedHashMap<String, Integer>();
     	    		
     	    		// For each itemset
     	    		for(int k=0; k< sequence.size(); k++){
     					// For each item
     	    			for(String item : sequence.get(k)){
     	    				if(contains(itemsetJC, item)){ // record the last position of each item in JC
     	    					addToLinked(mapAlreadySeenFromJC, item, k);
     	    				}
     	    			}
     	    			// remove items that fall outside the time window
     	    			Iterator<Entry<String, Integer>> iter = mapAlreadySeenFromJC.entrySet().iterator();
     	    			while(iter.hasNext()){
     	    				Entry<String, Integer> entryMap = iter.next();
     	    				if(entryMap.getValue() < k - windowSize +1){
     	    					iter.remove();
     	    				}else{
     	    					break;
     	    				}
     	    			}
     	    			// if all the items of I are inside the current window, then record the tid
     	    			if(mapAlreadySeenFromJC.keySet().size() == itemsetJC.length){
     	    				tidsJC.add(tid);
     	    				continue loop1;
     	    			}
     	    		}
     	    	}
     			// ----  ----
     			
    			// Create rule and calculate its confidence:  Conf(r) = sup(I-->JC) /  sup(I)	
				double confI_JC = ((double)tidsI_JC.size()) / tidsI.size();
//				Rule ruleI_JC = new Rule(ruleIJ.getItemset1(), itemsetJC, confI_JC, tidsI_JC.size());
				
				// if the confidence is enough
				if(confI_JC >= minconf){
					saveRule(tidsI_JC, confI_JC, itemsetI, itemsetJC);
				}

				expandRight(itemsetI, itemsetJC, tidsI, tidsJC, tidsI_JC);  // 

				// recursive call to expand left and right side of the rule
				expandLeft(itemsetI, itemsetJC, tidsI, tidsI_JC);  // occurencesJ
     		}
     	}
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
	private Map<String, Map<Integer, Occurence>> removeItemsThatAreNotFrequent(SequenceDatabase database) {
		// (1) Count the support of each item in the database in one database pass
		mapItemCount = new HashMap<String, Map<Integer, Occurence>>(); // <item, Map<tid, occurence>>
		
		// for each sequence
		for(Sequence sequence : database.getSequences()){
			// for each itemset
			for(short j=0; j< sequence.getItemsets().size(); j++){
				List<String> itemset = sequence.get(j);
				// for each item
				for(int i=0; i< itemset.size(); i++){
					String itemI = itemset.get(i);
					Map<Integer, Occurence> occurences = mapItemCount.get(itemI);
					if(occurences == null){
						occurences = new HashMap<Integer, Occurence>();
						mapItemCount.put(itemI, occurences);
					}
					Occurence occurence = occurences.get(sequence.getId());
					if(occurence == null){
						occurence = new Occurence(sequence.getId());
						occurences.put(sequence.getId(), occurence);
					}
					occurence.add(j);
				}
			}
		}
//		System.out.println("NUMBER OF DIFFERENT ITEMS : " + mapItemCount.size());
		// (2) remove all items that are not frequent from the database
		for(Sequence sequence : database.getSequences()){
			int i=0;
			while(i < sequence.getItemsets().size()){
				List<String> itemset = sequence.getItemsets().get(i);
				int j=0;
				while(j < itemset.size()){
					double count = mapItemCount.get(itemset.get(j)).size();
					
					if(count < minsuppRelative){
						itemset.remove(j);
					}else{
						j++;
					}
				}
				i++;
			}
		}
		return mapItemCount;
	}
	
	/**
	 * Save a rule I ==> J to the output file
	 * @param tidsIJ the tids containing the rule
	 * @param confIJ the confidence
	 * @param itemsetI the left part of the rule
	 * @param itemsetJ the right part of the rule
	 * @throws IOException exception if error writing the file
	 */
	private void saveRule(Set<Integer> tidsIJ, double confIJ, String[] itemsetI, String[] itemsetJ) throws IOException {
		ruleCount++;
		StringBuffer buffer = new StringBuffer();
		// write itemset 1
		for(int i=0; i<itemsetI.length; i++){
			buffer.append(itemsetI[i]);
			if(i != itemsetI.length -1){
				buffer.append(",");
			}
		}
		// write separator
		buffer.append(" ==> ");
		// write itemset 2
		for(int i=0; i<itemsetJ.length; i++){
			buffer.append(itemsetJ[i]);
			if(i != itemsetJ.length -1){
				buffer.append(",");
			}
		}
		// write separator
		buffer.append(" #SUP: ");
		// write support
		buffer.append(tidsIJ.size());
		// write separator
		buffer.append(" #CONF: ");
		// write confidence
		buffer.append(confIJ);
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Check if an itemset contains an item.
	 * @param itemset the itemset
	 * @param item an item
	 * @return true if the item appears in the itemset
	 */
	boolean contains(String[] itemset, String item) {
		// for each item in the itemset
		for(int i=0; i<itemset.length; i++){
			// if the item is found, return true
			if(itemset[i].equals(item)){
				return true;
				// if the current item is larger than the item that is searched,
				// then return false because of the lexical order
			}else if(itemset[i].compareTo(item) > 0){
				// not found, return false
				return false;
			}
		}
		return false;
	}
	
	/**
	 * This method checks if the item "item" is in the itemset.
	 * It assumes that items in the itemset are sorted in lexical order
	 * This version also checks that if the item "item" was added it would be the largest one
	 * according to the lexical order
	 * @param item an item
	 * @param itemset an itemset
	 * @return true if the item is contained in the itemset
	 */
	boolean containsLEXPlus(String[] itemset, String item) {
		// for each item in itemset
		for(int i=0; i< itemset.length; i++){
			// check if the current item is equal to the one that is searched
			if(itemset[i].equals(item)){
				// if yes return true
				return true;
				// if the current item is larger than the item that is searched,
				// then return true because if if the item "item" was added it would be the largest one
				// according to the lexical order.  
			}else if(itemset[i].compareTo(item) > 0){
				return true; // <-- xxxx
			}
		}
		// if the searched item was not found, return false.
		return false;
	}
	
	/**
	 * This method checks if the item "item" is in the itemset.
	 * It asumes that items in the itemset are sorted in lexical order
	 * @param item an item
	 * @param itemset an itemset
	 * @return true if the item is contained in the itemset
	 */
	 boolean containsLEX(String[] itemset, String item) {
		// for each item in itemset
		for(int i=0; i< itemset.length; i++){
			// check if the current item is equal to the one that is searched
			if(itemset[i].equals(item)){
				// if yes return true
				return true;
				// if the current item is larger than the item that is searched,
				// then return false because of the lexical order.
			}else if(itemset[i].compareTo(item) > 0){
				return false;  // <-- xxxx
			}
		}
		// if the searched item was not found, return false.
		return false;
	}
	
	/**
	 * Print statistics about the last algorithm execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  TRULEGROWTH - STATS =============");
//		System.out.println("minsup: " + minsuppRelative);
		System.out.println("Sequential rules count: " + ruleCount);
		System.out.println("Total time : " + (timeEnd - timeStart) + " ms");
		System.out.println("Max memory (mb)" + MemoryLogger.getInstance().getMaxMemory());
		System.out.println("=====================================");
	}

	/**
	 * Get the total runtime of the last execution.
	 * @return the time as a double.
	 */
	public double getTotalTime(){
		return timeEnd - timeStart;
	}
}
