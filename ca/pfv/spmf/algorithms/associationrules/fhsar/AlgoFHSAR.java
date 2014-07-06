package ca.pfv.spmf.algorithms.associationrules.fhsar;
/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.datastructures.redblacktree.RedBlackTree;

/**
 * An implementation of the FHSAR algorithm for hiding sensitive association rules in a
 * transaction database. This algorithm is described in the paper:
 * <br/><br/>
 * Weng, C. C., Chen, S. T., & Lo, H. C. (2008, November). A Novel Algorithm for Completely Hiding Sensitive Association Rules. In Intelligent Systems Design and Applications, 2008. ISDA'08. Eighth International Conference on (Vol. 3, pp. 202-208). IEEE.
 *
 * @author Philippe Fournier-Viger
 */
public class AlgoFHSAR {

	// variables for statistics
	int tidcount = 0; // the number of transactions in the last database read
	long startTimestamp = 0; // the start time of the last execution
	long endTimeStamp = 0; // the end time of the last execution
	
	// the relative minimum suport (integer) chosen by the user
	private int minSuppRelative;

	/**
	 * Run the FHSAR algorithm
	 * @param input  the file path to a transaction database
	 * @param inputSAR the file path to a set of sensitive association rules to be hidden
	 * @param output the output file path for writing the modified transaction database
	 * @param minsup the minimum support threshold
	 * @param minconf the minimum confidence threshold
	 * @throws IOException exception if an error while writing the file
	 */
	public void runAlgorithm(String input, String inputSAR, String output,
			double minsup, double minconf) throws IOException {
		// record the start time
		startTimestamp = System.currentTimeMillis();
		
		// the sensitive rules
		List<Rule> sensitiveRules = new ArrayList<Rule>();  
		 // the transactions from the database
		List<Set<Integer>> transactions = new ArrayList<Set<Integer>>();  
		
		// a red-black tree to store the transactions ordered by their wi value
		RedBlackTree<Transaction> PWT = new RedBlackTree<Transaction>();
		
		// STEP1 : Read the sensitive association rules from the file into memory
		readSensitiveRulesIntoMemory(inputSAR, sensitiveRules);
		
		// STAGE 1 of the FHSAR algorithm
		// Read the database into memory.
		// At the same time, we will calculate the wi for each transaction in the database
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(input));
		tidcount=0; // to count the number of transaction
		
		// for each line (transaction) of the input file until the end of the file
		while( ((line = reader.readLine())!= null)){
			// if the line is not a comment, is not empty or is not other
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
					|| line.charAt(0) == '@') {
				continue;
			}
			// we split the current transactions into items by separating the line by spaces
			String[] lineSplited = line.split(" ");
			
			// we will check if each association rule is contained in the transaction.
			Set<Integer> transaction = new HashSet<Integer>(lineSplited.length); // the items in this transaction
			boolean thereIsARuleSupportedByTransaction = false;  // flag to know if at least one sensitive rule appear in this transaction
			List<Rule> rulesContained = new ArrayList<Rule>();  // the list of sensitive rules contained in this transaction
			
			// for each sensitive rules
			for(Rule rule : sensitiveRules){
				// the number of items from the antecedent of the sensitive association rule
				// that was found in the current transaction.
				Set<Integer> matchLeft = new HashSet<Integer>();
				// the number of items from the consequent of the sensitive association rule
				// that was found in the current transaction.
				Set<Integer> matchRight = new HashSet<Integer>();
				
				// for each item in the current transaction
loop:			for(int i=0; i<lineSplited.length; i++){
					// convert from string to int
					int item = Integer.parseInt(lineSplited[i]);
					// add it to the
					transaction.add(item);
					
					// if the left side of this sensitive rule matches with this transaction
					if(matchLeft.size() != rule.leftSide.size() && rule.leftSide.contains(item)){
						matchLeft.add(item);
						// if the antecedent was completely found
						if(matchLeft.size() == rule.leftSide.size()){
							rule.leftSideCount++;
						}
					}  // else if the item appears in the right side of this transaction
					// but we have not seen all items from the right side yet
					else if(matchRight.size() != rule.rightSide.size() && rule.rightSide.contains(item)){
						matchRight.add(item);
					}
					// if the rule completely matches with this transaction... (both left and right sides)
					if(matchLeft.size() == rule.leftSide.size()  && matchRight.size() == rule.rightSide.size()){
						// increase the support of the rule
						rule.count++;
						// remember that this rule appears in this transaction
						rulesContained.add(rule);
						thereIsARuleSupportedByTransaction = true;
						break loop;  // stop the loop because we know that this rule match already!
					}
				}
			}
			
			// if at least a rule is supported by this transaction,
			//  we calculate the wi for the transaction and then
			// we will insert the transaction with its wi into PWT.
			if(thereIsARuleSupportedByTransaction){
				
				// (1) calculate MIC
				// MIC :  a map  where Key =  item   Value = support in rules
				Map<Integer, Integer> mapItemCount = new HashMap<Integer,Integer>();
				// for each rule contained in this transaction
				for(Rule rule : rulesContained){
					// for each item in the antecedent
					for(Integer item : rule.leftSide){
						// increase the support of the item
						Integer count = mapItemCount.get(item);
						if(count == null){
							count = 0;
						}
						mapItemCount.put(item, count+1);
					}
					// for each item in the consequent
					for(Integer item : rule.rightSide){
						// increase the support of the item
						Integer count = mapItemCount.get(item);
						if(count == null){
							count = 0;
						}
						mapItemCount.put(item, count+1);
					}
				}
				//  Do a loop to find the item that has the max count 
				// (represented as max(|rk| in the paper) and keep it
				int MIC = -1;
				int maxItem = -1;
				for(Entry<Integer, Integer> entry: mapItemCount.entrySet()){
					if(entry.getValue() > MIC){
						maxItem = entry.getKey();
						MIC = entry.getValue();
					}
				}
				//  WI is the maximum count divized by 2^(transaction.size() -1)
				double wi = MIC / Math.pow(2, transaction.size() - 1);
				// add transaction to PWT
				PWT.add(new Transaction(transaction, wi, maxItem));	
			}
			tidcount++; // increase the number of transaction
			transactions.add(transaction); // add the transaction to the list of transactions
		}
		// close the input file
		reader.close();
		
		// We transform the minsup parameter into a relative value.
		minSuppRelative = (int) Math.ceil(minsup * tidcount);
		
		// STAGE 2 of the FHSAR algorithm
		// This part is not well-explained in the paper so it might not be exactly like
		// what the authors did. But the main idea is the same.
		// We will delete items until the sensitive association rules fell below the threshold.
		while(sensitiveRules.isEmpty() == false){
			// We take the transaction that has the highest wi from PWT.
			Transaction td = PWT.popMaximum();
			// Select the item with the maximum |Rk| for this transaction
			// (we don't choose randomly in my implementation)
			int maxItem = td.maxItem;
			
			// Now we calculate the new wi for this transaction if we remove the item.
			// At the same time we will update the support count of the sensitive association rules.
			Map<Integer, Integer> mapItemCount = new HashMap<Integer,Integer>();
			boolean atLeastOneRule = false;
			// for each sensitive rule remaining
			for(Rule rule : sensitiveRules){
				// if the transaction td contains the rule
				if(td.items.containsAll(rule.leftSide) && td.items.containsAll(rule.rightSide)){
					// if the antecedent of the rule contains "maxItem"
					if(rule.leftSide.contains(maxItem)){
						// decrease the counts
						rule.count--;
						rule.leftSideCount--;
					}// if the consequent of the rule contains "maxItem"
					else if(rule.rightSide.contains(maxItem)){
						// decrease the count
						rule.count--;
					}else{
						// otherwise we note that there is at least one sensitive rule
						// still contained in this transaction
						atLeastOneRule = true;
						// for all items of the antecedent
						for(Integer item : rule.leftSide){
							// increase the count
							Integer count = mapItemCount.get(item);
							if(count == null){
								count = 0;
							}
							mapItemCount.put(item, count+1);
						}
						// for all items of the consequent
						for(Integer item : rule.rightSide){
							// increase the count
							Integer count = mapItemCount.get(item);
							if(count == null){
								count = 0;
							}
							mapItemCount.put(item, count+1);
						}
					}
				}
			}
			
			// we remove the item "maxItem" from the transaction
//			System.out.println("remove " + maxItem + " from " + td.items);
			td.items.remove(maxItem);  
			
			// We remove all rules that have become below the thresholds
			Iterator<Rule> iter = sensitiveRules.iterator();
			while (iter.hasNext()) {   // for each rule
				Rule rule = (Rule) iter.next();
				// if the threshold are not satisfied anymore, remove it
				if(rule.count < minSuppRelative  || ((rule.count / (double)rule.leftSideCount) < minconf)){
					iter.remove();
				}
			}

			// if at least one sensitive rule is STILL contained in this transaction, we need to update
			// the wi  and add this transaction again in PWT.
			if(atLeastOneRule){
				// find the maximum item like we did before
				int MIC = -1;
				int newMaxItem = -1;
				for(Entry<Integer, Integer> entry: mapItemCount.entrySet()){
					if(entry.getValue() > MIC){
						newMaxItem = entry.getKey();
						MIC = entry.getValue();
					}
				}
				// calculate WI 
				double wi = MIC / Math.pow(2, td.items.size() - 1);
				td.wi = wi;
				td.maxItem = newMaxItem;
				// add the transaction to PWT with the new wi and maxItem
				PWT.add(td);	
			}
		}
		
		// Now, write the transformed transaction database to disk!
		BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 
		for(Set<Integer> transaction : transactions){
			// we sort the transaction in lexical order because we were
			// using a set that was not sorted
			List<Integer> sorted = new ArrayList<Integer>(transaction);
			Collections.sort(sorted);
			// for each item
			for(int i=0; i< sorted.size(); i++){
				// we write the item to disk
				
				// if not the first item, we add a space before
				if(i > 0){
					writer.write(" " + sorted.get(i));
				} else{ //otherwise, no space
					writer.write("" + sorted.get(i));
				}
			}
			writer.newLine();
		}
		writer.close();  // close the output file
		
		// save the end time.
		endTimeStamp = System.currentTimeMillis();
	}

	/**
	 * This method reads the sensitive rules into memory
	 * @param inputSAR the file path to a set of sensitive association rules
	 * @param rules a structure for storing the sensitive association rules
	 * @throws IOException if error reading the file
	 */
	private void readSensitiveRulesIntoMemory(String inputSAR, List<Rule> rules)
			throws IOException {
		// open the input file
		BufferedReader reader = new BufferedReader(new FileReader(inputSAR));
		String line;
		// for each line (rule) until the end of the file
		while( ((line = reader.readLine())!= null)){ 
			// Each rule should have the format "4 ==> 5" in the file
			// So we split the line according to the arrow:
			
			String[] lineSplited = line.split("==> ");
			//  left side
			String [] leftStrings = lineSplited[0].split(" ");
			//  right side
			String [] rightStrings = lineSplited[1].split(" ");
			Rule rule = new Rule(); // create the rule
			// add each item from the left side after converting from string to int
			for(String string : leftStrings){
				rule.leftSide.add(Integer.parseInt(string));
			}
			// add each item from the right side after converting from string to int
			for(String string : rightStrings){
				rule.rightSide.add(Integer.parseInt(string));
			}
			// add the rule to the set of rules
			rules.add(rule);
		}
		// close the input file
		reader.close();
	}

	/**
	 * Print statistics about the latest execution.
	 */
	public void printStats() {
		System.out.println("=============  FSHAR - STATS =============");
		System.out.println(" Transactions count from original database : " + tidcount);
		System.out.println(" minsup : " + minSuppRelative + " transactions");
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)+ " ms");
		System.out.println("============================================");
	}
}