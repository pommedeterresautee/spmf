 package ca.pfv.spmf.algorithms.frequentpatterns.uapriori;
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
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of the U-Apriori algorithm as described by :<br/><br/>
 * 
 *   Chui, C., Kao, B., Hung, E. (2007), Mining Frequent Itemsets fomr Uncertain Data, PAKDD 2007,  pp 47-58.
 * 
 * @see ItemUApriori
 * @see UncertainTransactionDatabase
 * @see ItemsetUApriori
 * @see ItemsetsUApriori
 * @author Philippe Fournier-Viger
 */
public class AlgoUApriori {

	// this is the database
	protected UncertainTransactionDatabase database;
	// variable indicating the current level for the Apriori generation
	// (itemsets of size k)
	protected int k; 

	// stats
	protected int totalCandidateCount = 0;  // number of candidates generated
	protected int databaseScanCount = 0;  // number of database scan
	protected long startTimestamp;  // start time of latest execution
	protected long endTimestamp; // end time of latest execution
	private int itemsetCount; // the number of itemsets found
	
	// write to file
	BufferedWriter writer = null;	
	
	/**
	 * Constructor
	 * @param database the database for applying this algorithm
	 */
	public AlgoUApriori(UncertainTransactionDatabase database) {
		this.database = database;
	}

	/**
	 * Run this algorithm
	 * @param minsupp  a minimum support threshold
	 * @param output  the output file path for writing the result
	 * @throws IOException exception if error reading/writing files
	 */
	public void runAlgorithm(double minsupp, String output) throws IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		// reset variables for statistics
		totalCandidateCount = 0;
		databaseScanCount = 0;
		itemsetCount=0;
		
		// prepare the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		
		// Generate candidates with size k = 1 (all itemsets of size 1)
		k=1;
		Set<ItemsetUApriori> candidatesSize1 = generateCandidateSize1();
		
		// increase the number of candidates generated
		totalCandidateCount+=candidatesSize1.size();

		// calculate the support of each candidate of size 1
		// by scanning the database
		calculateSupportForEachCandidate(candidatesSize1);
		
		// To build level 1, we keep only the frequent candidates.
		// We scan the database one time to calculate the support of each candidate.
		Set<ItemsetUApriori> level = createLevelWithFrequentCandidates(minsupp,
				candidatesSize1);

		// Now this is the recursive step
		// itemsets of size k will be generated recursively starting from k=2
		//  by using itemsets of size k-1 until no candidates
		// can be generated
		k = 2;
		// While the level is not empty
		while (!level.isEmpty()  ) {
			// Generate candidates of size K
			Set<ItemsetUApriori> candidatesK = generateCandidateSizeK(level);
			// increase the candidate count
			totalCandidateCount+=candidatesK.size();

			// We scan the database one time to calculate the support
			// of each candidates.
			calculateSupportForEachCandidate(candidatesK);

			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			Set<ItemsetUApriori> levelK = createLevelWithFrequentCandidates(
					minsupp, candidatesK);
			level = levelK; // We keep only the last level... 
			k++;
		}
		// close the output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
	}

	/**
	 * Save an itemset to the output file.
	 * @param itemset  the itemset
	 * @throws IOException exception if error writing the itemset to the file
	 */
	private void saveItemsetToFile(ItemsetUApriori itemset) throws IOException{
		writer.write(itemset.toString() + " Support: " + itemset.getExpectedSupport());
		writer.newLine();
		itemsetCount++;
	}
	
	/**
	 * Take a set of candidates and compare them with the min expected support to keep
	 * only the itemset meeting that  minimum threshold.
	 * @param minsupp  the minimum expected threshold
	 * @param candidatesK  a set of itemsets of size k
	 * @return  the set of frequent itemsets of size k 
	 * @throws IOException exception if error writing output file
	 */
	protected Set<ItemsetUApriori> createLevelWithFrequentCandidates(double minsupp,Set<ItemsetUApriori> candidatesK) throws IOException {
		Set<ItemsetUApriori> levelK = new HashSet<ItemsetUApriori>();
		// for each itemset
		for (ItemsetUApriori candidate : candidatesK) { 
			// check if it has enough support
			if (candidate.getExpectedSupport() >= minsupp) {
				// if yes add it to the set of frequent itemset of size k
				levelK.add(candidate);
				// save the itemset to the output file
				saveItemsetToFile(candidate);
			}
		}
		// return frequent k-itemsets 
		return levelK;
	}
	
	/**
	 * Calculate the support of a set of candidates by scanning the database.
	 * @param candidatesK  a set of candidates of size k
	 */
	protected void calculateSupportForEachCandidate(
			Set<ItemsetUApriori> candidatesK) {
		// increase database scan count
		databaseScanCount++;
		// for each transaction
		for (ItemsetUApriori transaction : database.getTransactions()) {
			// For each candidate of level K, we increase its support
			// if it is included in the transaction.
			
			// for each candidate
		candidateLoop : for (ItemsetUApriori candidate : candidatesK) {
			
				// initialize the expected support to 0
				double expectedSupport = 0;
				
				// for each item in candidate we will try to find it
				for(ItemUApriori item : candidate.getItems()){
					boolean found = false;
					
					// for each item in the transaction
					for(ItemUApriori itemT : transaction.getItems()){
						
						// if we found the item
						if(item.getId() == itemT.getId()){
							found = true;
							// update the expected support
							if(expectedSupport == 0){
								expectedSupport = itemT.getProbability();
							}else{
								expectedSupport *= itemT.getProbability();
							}
							break;
						}
						// if the lexical order is not respected then it is impossible
						// that this item will be in this transaction so 
						// we stop searching for that item
						else if (item.getId() < itemT.getId()){
							break;
						}
					}	
					// if the last item that we searched was not found
					// then the full itemset is not here, so we stop
					if(found == false){
						continue candidateLoop;
					}
				}
				// If the candidate itemset was completely found,
				// we increase the support of the candidate its calculated 
				// expected support.
				candidate.increaseSupportBy(expectedSupport);
			}
		}
	}

	/**
	 * Generate candidate itemsets containing a single item.
	 * @return a set of candidate itemsets
	 */
	protected Set<ItemsetUApriori> generateCandidateSize1() {
		// create the set of candidates as empty
		Set<ItemsetUApriori> candidates = new HashSet<ItemsetUApriori>(); 
		// for each item
		for (ItemUApriori item : database.getAllItems()) {
			// simply add it to the set of candidates
			ItemsetUApriori itemset = new ItemsetUApriori();
			itemset.addItem(item);
			candidates.add(itemset);
		}
		return candidates;
	}

	/**
	 * Generate candidate itemsets of size K by using HWTUIs of size k-1
	 * @param levelK_1   itemsets of size k-1
	 * @return  candidates of size K
	 */
	protected Set<ItemsetUApriori> generateCandidateSizeK(Set<ItemsetUApriori> levelK_1) {
		// a set to store candidates
		Set<ItemsetUApriori> candidates = new HashSet<ItemsetUApriori>();

		// For each itemset I1 and I2 of level k-1
		Object[] itemsets = levelK_1.toArray();
		for(int i=0; i< levelK_1.size(); i++){
			ItemsetUApriori itemset1 = (ItemsetUApriori)itemsets[i];
			for(int j=0; j< levelK_1.size(); j++){
				ItemsetUApriori itemset2 = (ItemsetUApriori)itemsets[j];
				
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				ItemUApriori missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Then, create a new candidate by combining itemset1 and itemset2
					ItemsetUApriori candidate = new ItemsetUApriori();
					for(ItemUApriori item : itemset1.getItems()){
						candidate.addItem(item);
					}
					candidate.addItem(missing);

					// The candidate is tested to see if its subsets of size k-1 are included in
					// level k-1 (they are frequent).
					if(allSubsetsOfSizeK_1AreFrequent(candidate,levelK_1)){
						// if it pass the test, add it to the set of candidates
						candidates.add(candidate);
					}
				}
			}
		}
		// return the set of candidates
		return candidates;
	}

	/**
	 * Check if all subsets of size k-1 of a candidate itemset of size k are frequent.
	 * @param candidate  the candidate itemset
	 * @param levelK_1  frequent itemsets of size k-1
	 * @return true if all subsets are frequent, otherwise false
	 */
	protected boolean allSubsetsOfSizeK_1AreFrequent(ItemsetUApriori candidate, Set<ItemsetUApriori> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		
		//if only one item, return true because the empty set is always frequent
		if(candidate.size() == 1){
			return true;
		}
		// for each item
		for(ItemUApriori item : candidate.getItems()){
			// copy the itemset without this item to get a suset
			ItemsetUApriori subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			// we scan itemsets of size k-1
			for(ItemsetUApriori itemset : levelK_1){
				// if we found the subset, then set found to true 
				// and stop this loop
				if(itemset.isEqualTo(subset)){
					found = true;  
					break;
				}
			}
			// if the subset was not found, then we return false
			if(found == false){
				return false;
			}
		}
		// all the subsets were found, so we return true
		return true;
	}

	/**
	 * Print statistics about the latest execution.
	 */
	public void printStats() {
		System.out
				.println("=============  U-APRIORI - STATS =============");
		long temps = endTimestamp - startTimestamp;
//		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Candidates count : " + totalCandidateCount);
		System.out.println(" Database scan count : " + databaseScanCount);
		System.out.println(" The algorithm stopped at size " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Uncertain itemsets count : " + itemsetCount);

		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
