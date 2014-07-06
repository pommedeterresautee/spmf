package ca.pfv.spmf.algorithms.sequential_rules.cmrules;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;

/**
 * The CMRules algorithm for mining sequential rules common to several sequences.
 * <br/><br/>
 * 
 * This algorithm is described in:
 * <br/><br/>
 * 
 * Fournier-Viger, P., Faghihi, U., Nkambou, R. & Mephu Nguifo, E. (2010). 
 * CMRules: An Efficient Algorithm for Mining Sequential Rules Common to Several Sequences. 
 * Proceedings of the 23th International Florida Artificial Intelligence Research Society Conference 
 * (FLAIRS 2010). AAAI press. 
 * <br/><br/>
 * 
 * This implementation use a modified AprioriTID algorithm for generating association rules
 * in phase 1.
 * 
 * @see Rule
 * @see Rules
 * @see Itemset
 * @see Itemsets
 * @see TransactionDatabase
 * @see SequenceDatabase
 * @see Sequence
 * @see TriangularMatrix
 * @see AlgoAprioriTID_forCMRules
 * 
 * @author Philippe Fournier-Viger
 */

public class AlgoCMRules {

	//*** statistics about the latest execution ***
	int associationRulesCount = 0 ; // number of association rules
	int ruleCount;  // the number of sequential rules generated
	long timeStart = 0; // start time
	long timeEnd = 0; // end time
	long timeEndConvert = 0;  // end time for conversion to transaction database
	long timeEndApriori = 0; // end time for calculating frequent itemsets
	long timeEndSequentialMeasures = 0; // end time for calculating measures for sequential rules
	long timeBeginCalculateSequentialMeasures = 0;  // start time for calculating measures for sequential rules
	long timeEndPreprocessing = 0;  // end time for pre-processing
	
	// *** parameters  ***
	public int minCSupRelative = 0;  // min. seq. support
	public double minSeqConfidence; // min seq. confidence
	SequenceDatabase sequences; // the sequence database
	
	// Special parameters to set the size of rules to be discovered
	int minLeftSize = 0;  // min size of left part of the rule
	int maxLeftSize = 500; // max size of left part of the rule
	int minRightSize = 0; // min  size of right part of the rule
	int maxRightSize = 500; // max size of right part of the rule
	
	
	// *** internal variables  ***
	
	// this is the largest item ID in the database
	int maxItemId = 0;
	
	// this map indicate the tidset (value) for each item (key)
	Map<Integer, Set<Integer>> mapItemCount = new HashMap<Integer, Set<Integer>>();
	
	// list of frequent items
	List<Integer> listFrequentsSize1 = new ArrayList<Integer>();
	
	// the set of frequent itemsets found by Apriori TID
	private Itemsets patterns;
	
//	 a triangular matrix for efficiently counting the support of pairs of items
	private TriangularMatrix matrix;
	
	// object to write the output file
	BufferedWriter writer = null;

	/**
	 * Default constructor.
	 */
	public AlgoCMRules() {
	}

	
	/**
	 * Run the algorithm with the minsup parameter as a percentage value (double).
	 * @param input input file containing a sequence database.
	 * @param output the file path for writing the result
	 * @param absoluteMinSupport   the minsup is a percentage value (ex.: 0.05 =  5 % of all sequences in the database)
	 * @param minConfidence  the minimum confidence threshold
	 * @throws IOException exception if error while writing the output file.
	 */
	public void runAlgorithm(String input, String output, double absoluteMinSupport, double minConfidence) throws IOException {
		// load the sequence database from the input file
		sequences = new SequenceDatabase();
		sequences.loadFile(input);
		
		// convert absolute minimum support to a relative minimum support by
		// multiplying by the database size
		this.minCSupRelative = (int) Math.ceil(absoluteMinSupport * sequences.size());
		runAlgorithm(minCSupRelative, minConfidence, input, output);
	}

	/**
	 * Run the algorithm with the minsup parameter as a number of sequences (integer).
	 * @param input input file containing a sequence database.
	 * @param output the file path for writing the result
	 * @param relativeSupport   the minsup is a number of sequences (ex.: 5 =  5 sequences of the database)
	 * @param minConfidence  the minimum confidence threshold
	 * @throws IOException exception if error while writing the output file.
	 */
	public void runAlgorithm(int relativeSupport, double minConfidence, String input, String output) throws IOException {
		// reset the utility for recording memory usage
		MemoryLogger.getInstance().reset();

		// save the parameters 
		this.minSeqConfidence = minConfidence;
		this.minCSupRelative = relativeSupport;
		
		// if set to 0, then set it to 1
		if(this.minCSupRelative == 0){ // protection
			this.minCSupRelative = 1;
		}
		
		// It the sequence database has not been loaded yet, then load it from
		// the input file
		if(sequences == null){
			sequences = new SequenceDatabase();
			sequences.loadFile(input);
		}
		
		// Create the writer oject for writing the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// record the start time
		timeStart = System.currentTimeMillis(); // for stats

		// remove items that are infrequent from the database and 
		// at the same time calculate the support of each item
		// as well as the largest item in the dataase
		System.out.println("STEP 0");
		removeItemsThatAreNotFrequent(sequences);
		
		// Put items that are frequent in a list that is lexically ordered
		for(int i=0; i<= maxItemId; i++){
			// if it is frequent (tidset with size >0)
			if(mapItemCount.get(i) != null && mapItemCount.get(i).size() >= minCSupRelative){
				// add to the list
				listFrequentsSize1.add(i);
			}
		}
		
		// sort the list by lexical order
		Collections.sort(listFrequentsSize1);
		
		// record end time for pre-processing
		timeEndPreprocessing = System.currentTimeMillis(); // for stats
		
		//STEP 1 : Transform sequence database in a binary context
		// by calling the following method:
		TransactionDatabase context = convert(sequences);
		
		// (1.b) create the triangular matrix for counting the support of itemsets of size 2
		// for optimization purposes.
//		matrix = new TriangularMatrix(maxItemId+1);
		// for each transaction, take each itemset of size 2,
//		// and update the triangular matrix.
//		for(Itemset itemset : context.getObjects()){
//			Object[] array = itemset.getItems().toArray();
//			for(int i=0; i< itemset.size(); i++){
//				Integer itemI = (Integer) array[i];
//				for(int j=i+1; j< itemset.size(); j++){
//					Integer itemJ = (Integer) array[j];
//					// update the matrix
//					matrix.incrementCount(itemI, itemJ);
//				}
//			}
//		}
//		
		// record the end time for converting the database
		timeEndConvert = System.currentTimeMillis(); 
		
		// STEP 2: Applying the APRIORI-TID algorithm to find frequent itemsets
		System.out.println("STEP2");
		AlgoAprioriTID_forCMRules apriori = new AlgoAprioriTID_forCMRules(context, matrix);
		// we don't want itemset having more item that the maximum desired size
		// for a sequential rules in terms of items
		apriori.setMaxItemsetSize(maxLeftSize +  maxRightSize);  
		// apply apriori
		patterns = apriori.runAlgorithm(minCSupRelative, listFrequentsSize1, mapItemCount);
		// check memory usage
		MemoryLogger.getInstance().checkMemory();
		// record end time for Apriori algorithm
		timeEndApriori = System.currentTimeMillis(); 
 		
  		// STEP 3: Generate all rules from the set of frequent itemsets.
		// This is based on the algorithm for association rule by Agrawal & Srikant, 94
		// except that the sequential measures are calculated for each rule
		// to see if it is a valid sequential rule
		
		//System.out.println("STEP3 " + patterns.getItemsetsCount());
		generateRules(patterns);
		
		// check memory usage
		MemoryLogger.getInstance().checkMemory();

		// record end time for rule generation
		timeEnd = System.currentTimeMillis(); 
		
		/// we don't need the sequence database anymore
		sequences = null;
		
		// close the output file
		writer.close();
	}

	/**
	 * Remove itemsets that are not frequent from a sequence database
	 * @param sequences
	 * @return
	 */
	private Map<Integer, Set<Integer>> removeItemsThatAreNotFrequent(SequenceDatabase sequences) {
		// (1) count the support of each item in the database in one database pass
		// Store the information in a map where each item (key) is associated to
		// a tidset (value).
		mapItemCount = new HashMap<Integer, Set<Integer>>(); 
		
		// for each sequence
		for(Sequence sequence : sequences.getSequences()){
			
			// for each itemset in that sequence
			for(List<Integer> itemset : sequence.getItemsets()){
				
				// for each item in that itemset
				for(int i=0; i< itemset.size(); i++){
					// get its tidset
					Set<Integer> ids = mapItemCount.get(itemset.get(i));
					if(ids == null){
						// if no tidset create one
						ids = new HashSet<Integer>();
						mapItemCount.put(itemset.get(i), ids);
						
						// if it is the largest item seen until now, then remember that
						if(itemset.get(i) > maxItemId){
							maxItemId = itemset.get(i);
						}
					}
					// add the sequence ID to the tidset
					ids.add(sequence.getId());
				}
			}
		}
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
					if(count < minCSupRelative){
						// then remove it
						itemset.remove(j);
					}else{
						// otherwise go to next item
						j++;
					}
				}
				// if a sequence becomes empty ecause of removed items, then remove it
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
	 * This method update the interestingness measure of a given rule based
	 * on a sequence
	 * @param rule  the rule
	 * @param sequence the sequence
	 */
	private void calculateSequentialMeasures( Rule rule, Sequence sequence) {
		// This method will pass through the sequence and try to see if the left part 
		// matches and then if the right part matches
		
		// This is a set to remember the items previously seen from the left or right part of the rules
		Set<Integer> setAlreadySeen = new HashSet<Integer>(rule.getItemset1().size() * 3); // could be replaced with a flag on items
		
		// First we will try to match the left part of the rule
		int i=0;
		firstpass:{
			// for each itemset in the sequence
			for(; i< sequence.getItemsets().size(); i++){ 
				int j=0;
				List<Integer> itemset = sequence.get(i);
				for(; j< itemset.size(); j++){ // FOR EACH ITEM OF THIS SEQUENCE
					int item = itemset.get(j);
					// if the left part of the rule contains item J
					if(rule.getItemset1().contains(item)){ // left part of rule
						// if we have found all items from the leftpart,
						// we will break and try to find the right part next
						setAlreadySeen.add(item);
						if(setAlreadySeen.size() == rule.getItemset1().size()){
							 break firstpass;
						}
					}
				 }
			}
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
					setAlreadySeen.add(item);
					// if we have found all items from the right part,
					if(setAlreadySeen.size() == rule.getItemset2().size()){
						// update the support of the rule because we have found
						// the whole rule
						rule.sequentialTransactionCount++;
						// we can stop scanning the sequence
						return;
					}
				 }
			 }
		 }
	}

	/**
	 * This method convert a sequence database into a transaction database
	 * @param sequences a sequence database
	 * @return a transaction database
	 */
	private TransactionDatabase convert(SequenceDatabase sequences) {
		// create new transaction database
		TransactionDatabase transactionDatabase = new TransactionDatabase();
		
		// for each sequence in the seq. database
		for(Sequence sequence : sequences.getSequences()){
			//create a empty itemset that will be the transaction corresponding
			// to this sequence
			List<Integer> transaction = new ArrayList<Integer>();
			
			// for each itemset in the sequence
			for(List<Integer> itemset : sequence.getItemsets()){
				// add all items to the transaction
				transaction.addAll(itemset);
			}
			// add the transaction to the transaction database
			transactionDatabase.addTransaction(transaction);
		}
		transactionDatabase.printDatabase();
		return transactionDatabase;
	}

	/**
	 * Print statistics about the latest algorithm execution.
	 */
	public void printStats() {
		System.out.println("=============  SEQUENTIAL RULES - STATS =============");
		System.out.println("Association rules count: " + associationRulesCount);
		System.out.println("Sequential rules count: " + ruleCount);
		System.out.println("Total time : " + (timeEnd - timeStart) + " ms");
		System.out.println("Max memory: " + MemoryLogger.getInstance().getMaxMemory());
		System.out
				.println("===================================================");
	}
	
	/**
	 * Calculate the interestingness measures of a rule and then if it is a valid rule,
	 * save it to the output file.
	 * @param rule an association rule that may be a valid sequential rule
	 * @throws IOException exception if error writing the output file.
	 */
	void checkRule(Rule rule) throws IOException{
		// increase the number of association rules
		associationRulesCount++;
		
		// calculate the interestingness measure by scanning sequences
		// from the tid set of the rule to update its measures
		for(Integer seqId : rule.getItemset1().getTransactionsIds()){ 
			calculateSequentialMeasures(rule, sequences.getSequences().get(seqId));
		}

		// if the rule meet the minconf and minsuf criteria
		if(rule.sequentialTransactionCount >= minCSupRelative
				&& rule.getSequentialConfidence() >= minSeqConfidence){
			// save the rule to the file
			saveRule(rule);
		}
	}
	
	/**
	 * Run the  Agrawal algorithm for generating association rules that is modified
	 * to also find sequential rules
	 * @param patterns  a set of frequent itemsets
	 * @throws IOException exception if error writing to the output file
	 */
	void generateRules(Itemsets patterns) throws IOException {
		
		//For each frequent itemset of size >=2
		for(int k=2; k< patterns.getLevels().size(); k++){
			for(Itemset lk : patterns.getLevels().get(k)){ 
				// create H1
				Set<Itemset> H1 = new HashSet<Itemset>();
				for(Itemset itemsetSize1 : patterns.getLevels().get(1)){
					if(lk.contains(itemsetSize1.getItems()[0])){
						H1.add(itemsetSize1);
					}
				}
//				lk.print(); // DEBUG
//				System.out.println(); // DEBUG
				
				/// ================ I ADDED THIS BECAUSE THE ALGORITHM AS DESCRIBED BY AGRAWAL94
				/// ================ DID NOT GENERATE ALL  THE ASSOCIATION RULES
				Set<Itemset> H1_for_recursion  = new HashSet<Itemset>();
				// for each itemset in H1
				for(Itemset hm_P_1 : H1){
					
					// make a copy of  itemset_Lk_minus_hm_P_1 but remove 
					// items from  hm_P_1
					Itemset itemset_Lk_minus_hm_P_1 = (Itemset)lk.cloneItemSetMinusAnItemset(hm_P_1);

					// This is the definition of confidence:
					// double conf = supp(lk) / supp (lk - hm+1)
					// To calculate the confidence, we need 
					// the support of :  itemset_Lk_minus_hm_P_1
					calculateSupport(itemset_Lk_minus_hm_P_1);   //  THIS COULD BE OPTIMIZED ? OR DONE ANOTHER WAY ?
					// calculate the confidence
					double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
					
					// if the confidence is enough
					if(conf >= minSeqConfidence){
						// check if it respect the size constraint
						int leftsize = lk.size() - 1;
						if(leftsize <= maxLeftSize && leftsize >= minLeftSize && 1 >= minRightSize && 1 <= maxRightSize){
							// if yes create the rule
							Rule rule = new Rule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
							// then check if this association rule is also
							// a sequential rule
							checkRule(rule);
						}
						//  if the size constraints are met, then
						// record Hm+1 for recursion 
						if(1 != maxRightSize && leftsize != minLeftSize){
							H1_for_recursion.add(hm_P_1);// for recursion
						}
					}
				}
				// ================ END OF WHAT I HAVE ADDED

				// If it is still possible to exapdn the rule
				if(1 != maxRightSize && lk.size() - 1 != minLeftSize){
					// then  call the apGenRules procedure for further expansion
					apGenrules(k, 1, lk, H1_for_recursion);
				}
			}
		}
	}
	
	/**
	 * Save a rule to the output file
	 * @param support  the support of the rule
	 * @param confIJ  the confidence of the rule
	 * @param itemsetI  the left itemset
	 * @param itemsetJ  the right itemset
	 * @throws IOException exception if error writing the file
	 */
	private void saveRule(Rule rule) throws IOException {
		// increase the number of valid rules found
		ruleCount++;
		
		// create string buffer
		StringBuffer buffer = new StringBuffer();
		
		// write the left itemset
		for(int i=0; i<rule.getItemset1().size(); i++){
			buffer.append(rule.getItemset1().get(i));
			if(i != rule.getItemset1().size() -1){
				buffer.append(",");
			}
		}
		
		// write separator
		buffer.append(" ==> ");
		
		// write right itemset
		for(int i=0; i<rule.getItemset2().size(); i++){
			buffer.append(rule.getItemset2().get(i));
			if(i != rule.getItemset2().size() -1){
				buffer.append(",");
			}
		}
		
		// write support
		buffer.append(" #SUP: ");
		buffer.append(rule.getSequentialAbsoluteSeqSupport());
		
		// write confidence
		buffer.append(" #CONF: ");
		buffer.append(rule.getConfidence());
		
		// write the buffer to the file and write a new line
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * The ApGenRules as described in p.14 of the paper by Agrawal.
	 * (see the Agrawal paper for more details).
	 * @param lk  a itemset that is used to generate rules
	 * @throws IOException exception if error while writing output file
	 */
	private void apGenrules(int k, int m, Itemset lk, Set<Itemset> Hm) throws IOException {
//		System.out.println(" " + lk.toString() + "  " + Hm.toString());
		if(k > m+1){
			int leftsize = lk.size() - (1 + m);
			
			Set<Itemset> Hm_plus_1 = generateCandidateSizeK(Hm);
			Set<Itemset> Hm_plus_1_for_recursion = new HashSet<Itemset>();
			// for each itemset Hm+1
			for(Itemset hm_P_1 : Hm_plus_1){
				// Generate the itemset Lk / Hm+1
				Itemset itemset_Lk_minus_hm_P_1 = (Itemset)lk.cloneItemSetMinusAnItemset(hm_P_1);

				// Calculate the support of Lk / Hm+1
				calculateSupport(itemset_Lk_minus_hm_P_1);   
				
				// calculate the confidence of the rule Lk / Hm+1  ==>  Hm+1
				double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
				
				// if this association rule has enough confidence
				if(conf >= minSeqConfidence){
					// if it respect the size constraints
					if(leftsize <= maxLeftSize && leftsize >= minLeftSize && m+1 >= minRightSize && m+1 <= maxRightSize){
						// Create the rule Lk / Hm+1  ==>  Hm+1
						Rule rule = new Rule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
						// Then check if it is also a valid sequential rules
						checkRule(rule);
					}
					// if the size constraints allow further expansion of the rule
					if(1+m != maxRightSize && leftsize != minLeftSize){
						// add Hm+1 to the set for the recursion
						Hm_plus_1_for_recursion.add(hm_P_1);
					}
				}
			}
			// if the size constraints allow further expansion of the rule
			if(1+m != maxRightSize && leftsize != minLeftSize){
				// recursive call to apGenRules
				apGenrules(k, m+1, lk, Hm_plus_1_for_recursion); 
			}
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Calculate the support of an itemset by looking at the frequent patterns
	 * of the same size.
	 * 
	 * @param itemset_Lk_minus_hm_P_1
	 *            The itemset.
	 */
	private void calculateSupport(Itemset itemset_Lk_minus_hm_P_1) {
		// loop over all the patterns of the same size.
		for(Itemset itemset : patterns.getLevels().get(itemset_Lk_minus_hm_P_1.size())){
			// If the pattern is found
			if(itemset.isEqualTo(itemset_Lk_minus_hm_P_1)){
				// set its support to the same value.
				itemset_Lk_minus_hm_P_1.setTIDs(itemset.getTransactionsIds());
				return;
			}
		}
	}

	/**
	 * Generating candidate itemsets of size k from frequent itemsets of size
	 * k-1. This is called "apriori-gen" in the paper by agrawal. This method is
	 * also used by the Apriori algorithm for generating candidates.
	 * 
	 * @param levelK_1  a set of itemsets of size k-1
	 * @return a set of candidates
	 */
	protected Set<Itemset> generateCandidateSizeK(Set<Itemset> levelK_1) {
		// Initialize the set of candidates
		Set<Itemset> candidates = new HashSet<Itemset>();

		// For each itemset I1 and I2 of level k-1
		for(Itemset itemset1 : levelK_1){
			for(Itemset itemset2 : levelK_1){
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				Integer missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = missing;
					Itemset candidate = new Itemset(newItemset);

					// The candidate is tested to see if its subsets of size k-1
					// are included in
					// level k-1 (they are frequent).
					if(allSubsetsOfSizeK_1AreFrequent(candidate,levelK_1)){
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}
	
	/**
	 * This method checks if all the subsets of size "k" of the itemset
	 * "candidate" are frequent. It is similar to what is used in the
	 * Apriori algorithm for generating frequent itemsets.
	 * 
	 * @param candidate
	 *            An itemset of size "k".
	 * @param levelK_1
	 *            The frequent itemsets of size "k-1".
	 * @return true is all susets are frequent
	 */
	protected boolean allSubsetsOfSizeK_1AreFrequent(Itemset candidate, Set<Itemset> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if(candidate.size() == 1){
			return true;
		}
		// for each item of candidate, we will consider that this item is removed
		for(Integer item : candidate.getItems()){
			// create the subset without this item
			Itemset subset = (Itemset) candidate.cloneItemSetMinusOneItem(item);
			// we will search itemsets of size k-1 to see if this itemset appears
			boolean found = false;
			// for each  itemset of size k-1
			for(Itemset itemset : levelK_1){
				// if the itemset is equals to "subset", we found it and stop the loop
				if(itemset.isEqualTo(subset)){
					found = true;
					break;
				}
			}
			// if not found return false
			if(found == false){
				return false;
			}
		}
		// otherwise, all the subsets were found, so we return true
		return true;
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
