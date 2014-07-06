package ca.pfv.spmf.algorithms.frequentpatterns.vme;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;

/**
 * This is an implementation of the VME algorithm (Deng and Xu, 2011) for
 * erasable itemset mining.<br/><br/>
 * 
 * The VME algorithm finds all the ereasable itemsets from a product database.<br/><br/>
 * 
 * Actually, this algorithms is a only slight modification of the AprioriTID algorithm.<br/><br/>
 * 
 * I have implemented mostly as described in the paper with some modifications to make
 * it more efficient.<br/>
 * First, the authors suggested to generate all candidates of a level before 
 * removing the unereasable ones. This is inefficient. Instead, in my implementation, 
 * I check the "gain" (loss of profit) directly after generating a candidate so I can eliminate
 * them right away. <br/>
 * Second, it is unecessary to check the subsets like the authors
 * suggest because they use a vertical representation.  <br/>
 * Third, the authors suggest to store the profit of transactions 
 * in PID List. This is not memory efficient. For implementation it is better to 
 * store the profit of each transaction only once in a hashtable.
 * 
 * @see Itemset
 * @author Philippe Fournier-Viger
 */
public class AlgoVME {
	
	// variables for counting support of items
	// key: item    value:  tidset of the item as a set of integers
	Map<Integer, Set<Integer>> mapItemTIDs = new HashMap<Integer, Set<Integer>>();
	
	// variables for storing the profit of each transaction
	// key: transaction id     value: transaction profit
	Map<Integer, Integer> mapTransactionProfit = new HashMap<Integer, Integer>();

	// for statistics
	long startTimestamp = 0; //start time of latest execution
	long endTimeStamp = 0; //end time of latest execution
	
	// the maximum profit loss
	double maxProfitLoss =0;
	// the overall profit
	double overallProfit = 0;
	
	// the number of erasable itemsets found by the latest execution
	private int erasableItemsetCount = 0;

	//object to write the output file
	BufferedWriter writer = null;
	
	/**
	 * Default constructor
	 */
	public AlgoVME() {
		
	}

	/**
	 * Run the VME algorithm.
	 * @param input path to an input file
	 * @param output path to be used for writing the output file
	 * @param threshold  the threshold chosen by the user.
	 * @throws IOException exception if error reading/writing files
	 */
	public void runAlgorithm(String input, String output, double threshold) throws NumberFormatException, IOException {
		// record start time
		startTimestamp = System.currentTimeMillis();
		
		// create writer
		writer = new BufferedWriter(new FileWriter(output)); 
		// reset number of erasale itemsts o 0
		erasableItemsetCount = 0;
		
		// Scan the database one time to get the overall profit
		// and at the same time we record the profit of each transaction (product).
		overallProfit = 0;
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		int i=0;
		// for each transaction (line) until the end of file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line
			String[] lineSplited = line.split(" ");
			// get the profit (in first position of the line)
			int profit = Integer.parseInt(lineSplited[0]);
			// add the profit to overall profit
			overallProfit += profit;
			// put the profit of this transaction in the map of transaction profit
			mapTransactionProfit.put(i++, profit);
			
		}
		// close input file
		reader.close();
		
		// Calculate max profit loss
		maxProfitLoss  = overallProfit * threshold;
		
		// Scan the database second time to find erasable itemset of size 1 
		// and their tid list.
		reader = new BufferedReader(new FileReader(input));
		i=0;
		// for each transaction (line) until the end of file
		while( ((line = reader.readLine())!= null)){ 
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the line
			String[] lineSplited = line.split(" ");
			// for each item in that line
			for(int j=1; j< lineSplited.length; j++){
				// convert item to integer
				int item = Integer.parseInt(lineSplited[j]);
				// get the tidset of that item
				// and update it with the current tid for this transaction
				Set<Integer> tids = mapItemTIDs.get(item);
				if(tids == null){
					tids = new HashSet<Integer>();
					mapItemTIDs.put(item, tids);
				}
				tids.add(i);
			}
			i++; // increase the tid for next transaction
		}
		// close the input file
		reader.close();
		
		// Find erasable itemsets of size 1 and delete items that are
		// not erasable from memory
		List<Itemset> level = new ArrayList<Itemset>();

		// for each item
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDs.entrySet().iterator();
		while (iterator.hasNext()) {
			// get the tidset  of that item
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator.next();
			// init loss to 0
			int loss =0;
			// for each tid in the tidset
			for(Integer tid : entry.getValue()){
				// add the loss resulting from erasing that item
				loss += mapTransactionProfit.get(tid);
			}
			// if the looss is less than the max profit loss
			if(loss <= maxProfitLoss){
				// it is an erasable itemset
				Itemset itemset = new Itemset(entry.getKey());
				itemset.setTIDs(mapItemTIDs.get(entry.getKey()));
				level.add(itemset);
				// save it to the output file
				saveItemsetToFile(itemset, loss);
			}else{
				// otherwise, not erasable so we remove from memory.
				iterator.remove();  
			}
		}
		
		// sort items because apriori based algorithm need
		// a total order for candidate generation
		Collections.sort(level, new Comparator<Itemset>(){
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		
		// Recursively generate candidate erasable itemsets of size k>1 by using
		// erasable itemsets of size k-1 and stop
		// when no candidates can be generated
		while (!level.isEmpty()) {
			// Generate candidates of size K
			level = generateCandidateSizeK(level);
		}
		
		// close the file
		writer.close();
		// record end time
		endTimeStamp = System.currentTimeMillis();
	}
	
	/**
	 * Generate candidate itemsets of size K by using HWTUIs of size k-1
	 * @param levelK_1   itemsets of size k-1
	 * @return  candidates of size K
	 */
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1) throws IOException {
		// create list to store candidates of size k
		List<Itemset> candidates = new ArrayList<Itemset>();

// For each itemset I1 and I2 of level k-1
loop1:	for(int i=0; i< levelK_1.size(); i++){
			Itemset itemset1 = levelK_1.get(i);
loop2:		for(int j=i+1; j< levelK_1.size(); j++){
				Itemset itemset2 = levelK_1.get(j);
				
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
				for(int k=0; k< itemset1.size(); k++){
					// if they are the last items
					if(k == itemset1.size()-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1.getItems()[k] >= itemset2.getItems()[k]){  
							continue loop1;
						}
					}
					// if they are not the last items, and 
					else if(itemset1.getItems()[k] < itemset2.get(k)){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems()[k] > itemset2.get(k)){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}
				
				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				Integer missing = itemset2.get(itemset2.size()-1);

				// create the union of tids
				Set<Integer> unionTIDS = new HashSet<Integer>(itemset1.getTransactionsIds());
				unionTIDS.addAll(itemset2.getTransactionsIds());
				
				// calculate loss
				int loss =0;
				// for each tid, add the profit ot the transaction to the loss
				for(Integer tid : unionTIDS){
					loss += mapTransactionProfit.get(tid);
				}
				// if the loss is higher or equal to the max profit loss
				// that we can tolerate
				if(loss <= maxProfitLoss){
					// Create a new candidate by combining itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					candidate.setTIDs(unionTIDS);
					
					// add the itemset to the set of candidates
					candidates.add(candidate);
					// save the itemset to the output file
					saveItemsetToFile(candidate, loss);
				}
			}
		}
		// return candidates
		return candidates;
	}
	
	/**
	 * Save an itemset to the output file.
	 * @param itemset the itemset
	 * @param loss the loss
	 * @throws IOException exception if error while writing to output file
	 */
	private void saveItemsetToFile(Itemset itemset, int loss) throws IOException{
		// write the itemset
		writer.write(itemset.toString() + " #LOSS: " + loss);
		writer.newLine();
		// increase the itemset count
		erasableItemsetCount++; 
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  VME - STATS =============");
		long temps = endTimeStamp - startTimestamp;
		System.out.println("Overall profit: " + overallProfit);
		System.out.println("Maximum profit loss (over. profit x treshold): " + maxProfitLoss);
		System.out.println(" Erasable itemset count : " + erasableItemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
