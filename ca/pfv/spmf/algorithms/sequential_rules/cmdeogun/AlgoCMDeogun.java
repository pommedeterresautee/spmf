package ca.pfv.spmf.algorithms.sequential_rules.cmdeogun;
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
import java.util.Set;

import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * This is the original implementation of the CMDeo algorithm 
 * for mining sequential rules common to several sequences. It is an algorithm
 * adapted from the algorithm of Deogun et al. so that it works for the
 * case of multiple sequences instead of a single sequence
 * <br/><br/>
 * 
 * This modified algorithm is described briefly in this paper
 * <br/><br/>
 * 
 * Fournier-Viger, P., Faghihi, U., Nkambou, R., Mephu Nguifo, E. (2012). CMRules: 
 * Mining Sequential Rules Common to Several Sequences. Knowledge-based Systems,
 *  Elsevier, 25(1): 63-76.
 * <br/><br/>
 * 
 * and a french description is provided in my Ph.D. thesis:
 * <br/><br/>
 * 
 * Fournier-Viger, P. (2010), Un modèle hybride pour le support à l'apprentissage 
 * dans les domaines procéduraux et mal-définis. Ph.D. Thesis, University of Quebec 
 * in Montreal, Montreal, Canada, 184 pages.
 * 
 * @see Itemset
 * @see Rule
 * @see Rules
 * @author Philippe Fournier-Viger
 */
public class AlgoCMDeogun {
	
	// statistics
	long timeStart = 0;  // start time of latest execution
	long timeEnd = 0;  // end time of latest execution

	long timeStart11 = 0; // start time for generating rules of size 1*1
	long timeEnd11 = 0;  // start time for generating rules of size 1*1
	public long timeEndPreprocessing = 0;  // the end time for preprocessing phase
		
	// the parameters set by the user
	double minConfidence;  // minconf threshold
	int minsuppRelative;   // minsup threshold
	
	int maxItemId=0;  // the largest item ID in this database
	
	// A map indicating the tidset (value) of each item (key)
	Map<Integer, Set<Integer>> mapItemCount;
	
	// the list of frequent items
	List<Integer> listFrequents = new ArrayList<Integer>();
	
	// the sequence database
	SequenceDatabase database;
	
	// Special parameter to set the size of rules to be discovered
	int minLeftSize = 0;  // min size of left part of the rule
	int maxLeftSize = 500; // max size of left part of the rule
	int minRightSize = 0; // min  size of right part of the rule
	int maxRightSize = 500; // max size of right part of the rule
	
	// an object to write the output file
	BufferedWriter writer = null;

	// the number of sequential rules found
	private int ruleCount; 
	
	/**
	 * Default constructor.
	 */
	public AlgoCMDeogun() {
		
	}
	
	/**
	 * Run the algorithm with an absolute minimum support (double).
	 * @param input input file containing a sequence database.
	 * @param output the file path for writing the result
	 * @param absoluteMinSupport   the minsup is a percentage value (ex.: 0.05 =  5 % of all sequences in the database)
	 * @param minConfidence  the minimum confidence threshold
	 * @throws IOException exception if error while writing the output file.
	 */
	public void runAlgorithm(String input, String output, double absoluteMinSupport, double minConfidence) throws IOException {
		// load the sequence database from the input file
		database = new SequenceDatabase();
		database.loadFile(input);
		
		// convert absolute minimum support to a relative minimum support by
		// multiplying by the database size
		this.minsuppRelative = (int) Math.ceil(absoluteMinSupport * database.size());
		
		// run the algorithm
		runAlgorithm(input, output, minsuppRelative, minConfidence);
	}
	

	/**
	 * Run the algorithm with a relative minimum support (integer)
	 * @param input input file containing a sequence database.
	 * @param output the file path for writing the result
	 * @param relativeSupport   the minsup is a number of sequences (ex.: 5 =  5 sequences of the database)
	 * @param minConfidence  the minimum confidence threshold
	 * @throws IOException exception if error while writing the output file.
	 */
	public void runAlgorithm(String input, String output, int relativeSupport, double minConfidence) throws IOException {
		// remember the confidence
		this.minConfidence = minConfidence;
		// reset the utility for recording memory usage
		MemoryLogger.getInstance().reset();

		// save the minsup threshold received as parameter
		this.minsuppRelative = relativeSupport;
		// if set to 0, then set it to 1
		if(this.minsuppRelative == 0){ 
			this.minsuppRelative = 1;
		}
		
		// It the sequence database has not been loaded yet, then load it from
		// the input file
		if(database == null){
			database = new SequenceDatabase();
			database.loadFile(input);
		}
		
		// Create the writer oject for writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// record the start time
		timeStart = System.currentTimeMillis();
		
		// remove items that are infrequent from the database and 
		// at the same time calculate the support of each item
		// as well as the largest item in the dataase
		removeItemsThatAreNotFrequent(database);
		
		// Put items that are frequent in a list that is lexically ordered
		listFrequents = new ArrayList<Integer>();
		// for each item
		for(int i=0; i <= maxItemId; i++){
			// if it is frequent (tidset with size >0)
			if(mapItemCount.get(i) != null && mapItemCount.get(i).size() >= minsuppRelative){
				// add to the list
				listFrequents.add(i);
			}
		}
		
		// record the end time for preprocessing
		timeEndPreprocessing   = System.currentTimeMillis(); 

		// start the main procedure of the algorithm that will recursively
		// grow rules
		start(mapItemCount);
		
		// record the end time
		timeEnd = System.currentTimeMillis(); 
		
		// close the output file
		writer.close();
		
		// set the database as null because we don't need it anymore
		database =  null;
	}

	/**
	 * Start the algorithm.
	 * @param mapItemCount  a map with the tidset (value) of each item (key)
	 * @throws IOException exception if error writing the output file
	 */
	private void start(Map<Integer, Set<Integer>> mapItemCount) throws IOException {
		// record start time for generation of rules of size 11
		timeStart11 = System.currentTimeMillis();
		
		// (1) generate all rules with 1-left-itemset and 1-right-itemset
		Rules ruleSize11 = new Rules("candidate size 11");
		
		// FOR EACH FREQUENT ITEM i WE COMPARE 
		// WITH EVERY OTHER FREQUENT ITEM  j TO 
		// TRY TO GENERATE RULES  I --> J  and J --> I
		
		// for each item i
		for(int i=0; i< listFrequents.size(); i++){
			Integer intI = listFrequents.get(i);
			Set<Integer> tidsI = mapItemCount.get(intI);
			// for each other item j
			for(int j=i+1; j< listFrequents.size(); j++){
				Integer intJ = listFrequents.get(j);
				Set<Integer> tidsJ = mapItemCount.get(intJ);
				
				// (1) Build list of common  tids shared by i and j
				List<Integer> commonTids = new ArrayList<Integer>();
				for(Integer tid : tidsI){
					if(tidsJ.contains(tid)){
						commonTids.add(tid);
					}
				}
				// (2) check if the two itemsets have enough common tids
				// if not, we don't need to generate rules for them.
				if(commonTids.size() >= minsuppRelative){
					// they have enough so we generate I ==> J  and J ==> I
					generateRuleSize11(intI, tidsI, intJ, tidsJ, commonTids, ruleSize11);
				}
			}
		}
		// record end time for generation of rules of size 1*1
		timeEnd11 = System.currentTimeMillis();
		
//		System.out.println("11 size" + ruleSize11.getRulesCount() + " confid " +  sequentialRules.getRulesCount());
		
		//  if the user wish to discover rules with more than 1 item in the left side
		if( maxLeftSize >1){
			// perform expansion of left side of rules
			performLeftExpansion(ruleSize11);
		}
		//  if the user wish to discover rules with more than 1 item in the right side
		if( maxRightSize >1){
			// perform expansion of right side of rules
			performRightExpansion(ruleSize11);
		}
	}

	/**
	 *  Generate rules of size 1*1
	 * @param item1 an item
	 * @param tids1 the tidset of that item
	 * @param item2 another item
	 * @param tids2 the tidset of that second item
	 * @param commonTids  the intersection of the tidsets of the items
	 * @param ruleSize11  a set of rules to store the frequent rules found
	 * @throws IOException exception if error writing to output file
	 */
	private void generateRuleSize11(Integer item1, Set<Integer> tids1, Integer item2, Set<Integer> tids2, List<Integer> commonTids,
			Rules ruleSize11) throws IOException {
		
		int countLeftBeforeRight =0; // support of rule  item1 --> item2
		int countRightBeforeLeft =0; // support of rule  item2 --> item1
		
		// FOR EACH SEQUENCE where both items appear
		// we will update the support of both rules at the same time
		
		// Note that an item can appear multiple times in a same sequence.
		
		//for each sequence 
		for(Integer tid : commonTids){ 
			int firstOccurence1 = -1;  // to store the position of the first occurence of item1 in that sequence
			int firstOccurence2 = -1;  // to store the position of the first occurence of item2 in that sequence
			boolean saw1before2 = false;  // true if 1 was before 2 in that sequence
			boolean saw2before1 = false; // true if 2 was before 1 in that sequence
			
			// get the sequence
			List<List<Integer>> itemsets = database.getSequences().get(tid).getItemsets();
			 
			// for each itemset in that sequence
			for(int i=0; i< itemsets.size(); i++){ 
				// for each item in that sequence
				for(Integer item : itemsets.get(i)){  
					// if we have found item1
					if(item.equals(item1)){
						 // note the position of the first occurence of item 1 if not seen yet
						if(firstOccurence1 == -1){
							firstOccurence1 = i; 
						}
						// if item 2 was seen already 
						if(firstOccurence2 > -1 && firstOccurence2 < i){
							// than note that we saw item2 before item1
							saw2before1 = true;
						}
					}
					else if(item.equals(item2)){
						// note the position of the first occurence of item2 if not seen yet
						if(firstOccurence2 == -1){
							firstOccurence2 = i;
						}
						// if item 1 was seen already 
						if(firstOccurence1 > -1 && firstOccurence1 < i){
							// than note that we saw item1 before item2
							saw1before2 = true;
						}
					}
				}
			}
			if(saw2before1){
				// if we saw 2 before 1, increase the support of the rule  i2 -> i1
				countRightBeforeLeft++;
			}
			if(saw1before2){
				// if we saw 1 before 2, increase the support of the rule  i1 -> i2
				countLeftBeforeRight++;
			}
		}

		// if  i1 -> i2 has minimum support
		if(countLeftBeforeRight >= minsuppRelative){
			// We  create the Rule
			Itemset itemset1 = new Itemset(item1);
			itemset1.setTIDs(tids1);
			Itemset itemset2 = new Itemset(item2);
			itemset2.setTIDs(tids2); 
			Rule ruleLR = new Rule(itemset1, itemset2);
			ruleLR.setTransactioncount(countLeftBeforeRight);  // set its support
			
			// we add the rule to the set of frequent rules of size 1*1
			ruleSize11.addRule(ruleLR);
			
			// if the rule is valid
			if(ruleLR.getConfidence() >= minConfidence){
				
				// if it meet the size constraints
				if(ruleLR.getItemset1().size()>= minLeftSize &&
						ruleLR.getItemset2().size()>= minRightSize){
					
					// then the rule is saved to the output file
					saveRule(ruleLR.getRelativeSupport(), ruleLR.getConfidence(), ruleLR.getItemset1(), ruleLR.getItemset2());

				}
			}
		}
		// if  i2 -> i1 has minimum support
		if(countRightBeforeLeft >= minsuppRelative){
			// We  create the Rule
			Itemset itemset1 = new Itemset(item1);
			itemset1.setTIDs(tids1);
			Itemset itemset2 = new Itemset(item2);
			itemset2.setTIDs(tids2);
			Rule ruleRL = new Rule(itemset2, itemset1);
			ruleRL.setTransactioncount(countRightBeforeLeft);  // set its support
			
			// we add the rule to the set of frequent rules of size 1*1
			ruleSize11.addRule(ruleRL); 
			
			// if the rule is valid
			if(ruleRL.getConfidence() >= minConfidence){
				
				// if it meet the size constraints
				if(ruleRL.getItemset1().size()>= minLeftSize &&
						ruleRL.getItemset2().size()>= minRightSize){
					
					// then the rule is saved to the output file
					saveRule(countRightBeforeLeft, ruleRL.getConfidence(), itemset2, itemset1);
				}
			}
		}
	}
	


	/**
	 * Perform a left expansion of a rule with k-1 items in its antecedent
	 * @param ruleSizeKm1 a  rule with a left part of size k-1
	 * @throws IOException exception if error while writing the output file
	 */
	private void performLeftExpansion(Rules ruleSizeKm1) throws IOException {
		// For each rule such that the right itemset is the same,
		// check if we can combine them to generate a candidate.
		// If so, check if that candidate is valid.
		Rules ruleSizeK = new Rules("Candidates");
		
		// for each rule I
		for(int i=0; i< ruleSizeKm1.getRulesCount(); i++){
			Rule ruleI = ruleSizeKm1.getRules().get(i);
			
			// for each rule J != I
			for(int j=i+1; j< ruleSizeKm1.getRulesCount(); j++){
				Rule ruleJ = ruleSizeKm1.getRules().get(j);
				
				// if the right part is the same..
				if(ruleI.getItemset2().isEqualTo(ruleJ.getItemset2())){
					
					// check if the left part share all items except the last one.
					if(ruleI.getItemset1().allTheSameExceptLastItemV2(ruleJ.getItemset1())){
						
						// if yes, then we wil merge both left itemsets
						// to generate a larger left itemset to get a new rule
						
						// we combine both left itemset
						int newItemset[] = new int[ruleI.getItemset1().size()+1];
						System.arraycopy(ruleI.getItemset1().itemset, 0, newItemset, 0, ruleI.getItemset1().size());
						newItemset[ruleI.getItemset1().size()] = ruleJ.getItemset1().getItems()[ruleJ.getItemset1().size() -1];
						Itemset newLeftItemset = new Itemset(newItemset);
						
						// ONLY FOR LEFT EXPANSION : WE NEED TO CALCULATE THE NEW SUPPORT FOR 
						// THE LEFT ITEMSET (for the confidence calculation, later):
						// It is the intersection of the tids for the left itemset of both rules.
						
						// so we loop over the tid of the left part of rule I
						for(Integer id : ruleI.getItemset1().transactionsIds){
							// we check if the tidset of the left part of rule J also contains id
							if(ruleJ.getItemset1().transactionsIds.contains(id)){
								// if yes, we add it to tidset of the new itemset
								newLeftItemset.transactionsIds.add(id);
							}
						}
	
						// create the candidate rule
						Rule candidate = new Rule(newLeftItemset,  ruleI.getItemset2());
						// calculate the support and confidence of the rule
						
						// to do that, the algorithm loop over the tidset of the
						// new left itemset
						for(Integer tid : candidate.getItemset1().getTransactionsIds()){ 
							// it checks if the right side also got the same tid
							if(candidate.getItemset2().transactionsIds.contains(tid)){
								// then it scan this sequence to calculate the confidence 
								calculateInterestingnessMeasures(candidate, database.getSequences().get(tid), true, false);
							}
						}
						//  if the rule is frequent
						if(candidate.getRelativeSupport() >= minsuppRelative){
							// add it to set of candidates that can be used for future expansions
							ruleSizeK.addRule(candidate); 
							// if the rule is valid
							if(candidate.getConfidence() >= minConfidence){
//								// if the rule meets the size constraints
								if(candidate.getItemset1().size()>= minLeftSize &&
										candidate.getItemset2().size()>= minRightSize){
									//save the rule to the output file
									saveRule(candidate.getRelativeSupport(), candidate.getConfidence(), candidate.getItemset1(), candidate.getItemset2());
								}
							}
						}
					}
				}
			}
		}
		//if there is some frequent rules generated
		// and that we have not reached the maximum size allowed for the antecedent
		if(ruleSizeK.getRulesCount() !=0  &&  ruleSizeK.getRules().get(0).getItemset1().size() < maxLeftSize){
			// make a recursive call to further try to expand the left side of the rule
			performLeftExpansion(ruleSizeK);
		}

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}
	


	/**
	 * Perform a right expansion of a rule with k-1 items in its consequent
	 * @param ruleSizeKm1 a  rule with a right part of size k-1
	 * @throws IOException exception if error while writing the output file
	 */
	private void performRightExpansion(Rules ruleSizeKm1) throws IOException {
		// For each rule such that the right itemset is the same,
		// check if we can combine them to generate a candidate.
		// If so, check if that candidate is 
		Rules ruleSizeK = new Rules("Candidates");
		

		// for each rule I
		for(int i=0; i< ruleSizeKm1.getRulesCount(); i++){
			Rule ruleI = ruleSizeKm1.getRules().get(i);
			
			// for each rule J != I
			for(int j=i+1; j< ruleSizeKm1.getRulesCount(); j++){
				Rule ruleJ = ruleSizeKm1.getRules().get(j);
				
				// if the right part is the same..
				if(ruleI.getItemset1().isEqualTo(ruleJ.getItemset1())){
					
					// check if the right part share all items except the last one.
					if(ruleI.getItemset2().allTheSameExceptLastItemV2(ruleJ.getItemset2())){
						
						// if yes, then we wil merge both right itemsets
						// to generate a larger right itemset to get a new rule
						
						// we combine both right itemset
						int newItemset[] = new int[ruleI.getItemset1().size()+1];
						System.arraycopy(ruleI.getItemset2().itemset, 0, newItemset, 0, ruleI.getItemset2().size());
						newItemset[ruleI.getItemset2().size()] = ruleJ.getItemset2().getItems()[ruleJ.getItemset2().size() -1];
						Itemset newRightItemset = new Itemset(newItemset);
		
						// SPECIAL TRICK FOR RIGHT EXPANSION : WE CALCULATE THE NEW SUPPORT FOR 
						// THE RIGHT ITEMSET ):
						// It is the intersection of the tids for the right itemset of both rules.
						
						// so we loop over the tid of the right part of rule I
						for(Integer id : ruleI.getItemset2().transactionsIds){
							// we check if the tidset of the right part of rule J also contains id
							if(ruleJ.getItemset2().transactionsIds.contains(id)){
								// if yes, we add it to tidset of the new itemset
								newRightItemset.transactionsIds.add(id);
							}
						}
						
						// create the candidate rule
						Rule candidate = new Rule(ruleI.getItemset1(), newRightItemset);
						
						// calculate the support and confidence of the rule
						
						// to do that, the algorithm loop over the tidset of the
						// new left itemset
						for(Integer tid : candidate.getItemset1().getTransactionsIds()){ // FOR EACH SEQUENCE that is relevant for this rule..
							// it checks if the right side also got the same tid
							if(candidate.getItemset2().transactionsIds.contains(tid)){
								// then it scan this sequence to calculate the confidence 
								calculateInterestingnessMeasures(candidate, database.getSequences().get(tid), false, true);
							}
						}
						
						// if the rule is frequent
						if(candidate.getRelativeSupport() >= minsuppRelative){
							ruleSizeK.addRule(candidate); // ADD TO RULE OF SIZE 1-1    // I CHANGE IT !!!!!
							// add it to set of candidates that can be used for future expansions
							if(candidate.getConfidence() >= minConfidence){
//								
								// if the rule meets the size constraints
								if(candidate.getItemset1().size()>= minLeftSize &&
										candidate.getItemset2().size()>= minRightSize){
									
									//save the rule to the output file
									saveRule(candidate.getRelativeSupport(), candidate.getConfidence(), candidate.getItemset1(), candidate.getItemset2());
//									sequentialRules.addRule(candidate); // ADD TO RESULT SET !!!!!!!!!!
//									validExpansionCount++;
								}
							}
						}
					}
				}
			}
		}
		//if there is some frequent rules generated
		if(ruleSizeK.getRulesCount() !=0){
			//if we have not reached the maximum size allowed for the antecedent
			if(ruleSizeK.getRules().get(0).getItemset1().size() < maxLeftSize){
				//call recursive method to try to expand the left side of the rule
				performLeftExpansion(ruleSizeK);   
			}
			//if we have not reached the maximum size allowed for the consequent
			if(ruleSizeK.getRules().get(0).getItemset2().size() < maxRightSize){
				//call recursive method to try to expand the right side of the rule
				performRightExpansion(ruleSizeK);
			}
		}
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * This method update the interestingness measure of a given rule based
	 * on a sequence
	 * @param rule  the rule
	 * @param sequence the sequence
	 * @param calculateTIDSLeftItemset if true, the support of the antecedent may be updated
	 * @param calculateTIDSRightItemset if true, the support of the consequent may be updated
	 */
	private void calculateInterestingnessMeasures(Rule rule, Sequence sequence,
			boolean calculateTIDSLeftItemset, boolean calculateTIDSRightItemset) {
		// This method will pass through the sequence and try to see if the left part 
		// matches and then if the right part matches
		
		// This is a set to remember the items previously seen from the left or right part of the rule
		Set<Integer> setAlreadySeen = new HashSet<Integer>(rule.getItemset1().size() * 3); 
		
		// First we will try to match the left part of the rule
		int i=0;
		firstpass:{
			// for each itemset in the sequence
			for(; i< sequence.getItemsets().size(); i++){ 
				// for each item j in that itemset
				int j=0;
				List<Integer> itemset = sequence.get(i);
				for(; j< itemset.size(); j++){ 
					int item = itemset.get(j);
					// if the left part of the rule contains item J
					if(rule.getItemset1().contains(item)){ 
						// then add it to the set of items seen from the left part
						setAlreadySeen.add(item);
						// if we have found all items from the leftpart,
						// we will break and try to find the right part next
						if(setAlreadySeen.size() == rule.getItemset1().size()){
							 break firstpass;
						}
					}
				 }
			}
		}
		
		// we found the left part, so we can now update the tidset of the antecedent
		if(calculateTIDSLeftItemset){
			rule.getItemset1().transactionsIds.add(sequence.getId());
		}
		
		i++; // i++ because we will try to find the right part of the rule starting from the next itemset
		setAlreadySeen.clear();  // clear the set of items seen because we will reuse this variable but here for the right side of the rule

		// for each itemset in the sequence starting at i
		for(; i< sequence.getItemsets().size(); i++){ 
			int j=0;
			List<Integer> itemset = sequence.get(i);
			
			// for each item j in that itemset
			for(; j< itemset.size(); j++){
				int item = itemset.get(j);
				// if the right part of the rule contains item J
				if(rule.getItemset2().contains(item)){ 
					// then add it to the set of items seen from the right part
					setAlreadySeen.add(item);
					// if we have found all items from the right part,
					if(setAlreadySeen.size() == rule.getItemset2().size()){
						if(calculateTIDSRightItemset){
							// increase the support of the consequent
							rule.getItemset2().transactionsIds.add(sequence.getId());
						}
						// update the support of the rule because we have found
						// the whole rule
						rule.incrementTransactionCount();  
						// we can stop scanning the sequence
						return;
					}
				 }
			 }
		 }
	}

	/**
	 * Remove items that are infrequent from the database and 
	 * at the same time calculate the support of each item
	 * as well as the largest item in the dataase
	 * @param sequences  a sequence database
	 * @return a map indicating the tidset (value) of each item (key)
	 */
	private Map<Integer, Set<Integer>> removeItemsThatAreNotFrequent(SequenceDatabase sequences) {
		// (1) count the support of each item in the database in one database pass
		
		//a map to store the tidset (value) of each item (key)
		mapItemCount = new HashMap<Integer, Set<Integer>>(); 
		
		// for each sequence
		for(Sequence sequence : sequences.getSequences()){
			// for each itemset in that sequence
			for(List<Integer> itemset : sequence.getItemsets()){
				// for each item in that itemset
				for(int i=0; i< itemset.size(); i++){
					// get the tidset of that item
					Set<Integer> tids = mapItemCount.get(itemset.get(i));
					// if null create a new tidset
					if(tids == null){
						tids = new HashSet<Integer>();
						mapItemCount.put(itemset.get(i), tids);
						// if this item is the largest item seen until now, then note it.
						if(itemset.get(i) > maxItemId){
							maxItemId = itemset.get(i);
						}
					}
					// add the id of this sequence to the tidset
					tids.add(sequence.getId());
				}
			}
		}
		// print the number of different items in the database
		System.out.println("NUMBER OF DIFFERENT ITEMS : " + mapItemCount.size());
		
		// (2) remove all items that are not frequent from the database

		// for each sequence
		for(Sequence sequence : sequences.getSequences()){
			int i=0;
			// for each itemset in that sequence
			while(i < sequence.getItemsets().size()){
				List<Integer> itemset = sequence.getItemsets().get(i);
				int j=0;
				// for each item in that itemset
				while(j < itemset.size()){
					// if the item is not frequent
					double count = mapItemCount.get(itemset.get(j)).size();
					if(count < minsuppRelative){
						// then remove it
						itemset.remove(j);
					}else{
						// otherwise go to next item
						j++;
					}
				}
				// if a sequence becomes empty, then remove it
				if(itemset.size() == 0){
					sequence.getItemsets().remove(i);
				}else{
					// otherwise go to next itemset
					i++;
				}
			}
		}
		// return the map of items - tidsets
		return mapItemCount;
	}
	
	/**
	 * Save a rule to the output file
	 * @param support  the support of the rule
	 * @param confIJ  the confidence of the rule
	 * @param itemsetI  the left itemset
	 * @param itemsetJ  the right itemset
	 * @throws IOException exception if error writing the file
	 */
	private void saveRule(int support, double confIJ, Itemset itemsetI, Itemset itemsetJ) throws IOException {
		// increase the number of valid rules found
		ruleCount++;
		
		// create string buffer
		StringBuffer buffer = new StringBuffer();
		
		// write the left itemset
		for(int i=0; i<itemsetI.size(); i++){
			buffer.append(itemsetI.get(i));
			if(i != itemsetI.size() -1){
				buffer.append(",");
			}
		}
		
		// write separator
		buffer.append(" ==> ");
		
		// write the right itemset
		for(int i=0; i<itemsetJ.size(); i++){
			buffer.append(itemsetJ.get(i));
			if(i != itemsetJ.size() -1){
				buffer.append(",");
			}
		}
		
		// write support
		buffer.append(" #SUP: ");
		buffer.append(support);
		
		// write confidence
		buffer.append(" #CONF: ");
		buffer.append(confIJ);
		
		// write the buffer and a new line
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * Print statistics about the latest algorithm execution.
	 */
	public void printStats() {
		System.out
				.println("=============  SEQUENTIAL RULES - STATS =============");
		System.out.println("Sequential rules count: " + ruleCount);
		System.out.println("Total time : " + (timeEnd - timeStart) + " ms");		
		System.out.println("Max memory: " + MemoryLogger.getInstance().getMaxMemory());
		System.out
				.println("===================================================");
	}

	/**
	 * Set the minimum antecedent  size constraint for rules to be found.
	 * @param minLeftSize an integer
	 */
	public void setMinLeftSize(int minLeftSize) {
		this.minLeftSize = minLeftSize;
	}

	/**
	 * Set the maximum antecedent size  constraint for rules to be found.
	 * @param maxLeftSize an integer
	 */
	public void setMaxLeftSize(int maxLeftSize) {
		this.maxLeftSize = maxLeftSize;
	}


	/**
	 * Set the minimum consequent size constraint for rules to be found.
	 * @param minRightSize an integer
	 */
	public void setMinRightSize(int minRightSize) {
		this.minRightSize = minRightSize;
	}

	/**
	 * Set the maximum consequent size  constraint for rules to be found.
	 * @param maxRightSize an integer
	 */
	public void setMaxRightSize(int maxRightSize) {
		this.maxRightSize = maxRightSize;
	}
}
