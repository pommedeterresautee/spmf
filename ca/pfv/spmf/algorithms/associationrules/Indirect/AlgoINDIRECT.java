package ca.pfv.spmf.algorithms.associationrules.Indirect;

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
 * This is an implementation of the INDIRECT algorithm for generating indirect association rules.
 * <br/><br/>
 * 
 * The implementation is based on the description in the book:
 * Tan, Steinbach & Kumar (2006) "Introduction to data mining", chapter 7, p. 469, Algorithm 7.2.
 * and the KDD 2000 paper by Tan et al.
 *  <br/><br/>
 *  
 *  However, note that the algorithm is not exactly the same as what the authors did, because there
 *  is not enough details in the original paper and in the book. To implement the algorithm, I therefore
 *  had to make some choices based on what I tought what the best or easiest way to do it.
 *  <br/><br/>
 *  
 *  Also, note that instead of using the IS measure to compute the dependancy between itemsets, I chose
 *  to use the confidence.  The confidence is easier to calculate.
 *  <br/><br/>
 *            
 * Also, note that there is some faster algorithm that exists for generating indirect association rules
 * that have been proposed after INDIRECT (but not implemented in SPMF). 
 * <br/><br/>
 * 
 * Lastly, note that in my implementation I use an AprioriTID like procedure for generating frequent itemsets that
 * are needed to generate indirect rules. However I do not save the frequent itemsets to file because we don't
 * need to keep them (we just want to generate the indirect rules).
 * <br/><br/>
 * 
 * If you find some errors or have some ideas for optimization, please let me know by contacting me on my website.
 *  <br/><br/>
 * 
 * One possible optimization that I could do in the future would be to use BitSet instead of HashSet 
 * to represent the tids sets.
 *  <br/><br/>
 *
 * @author Philippe Fournier-Viger
 */
public class AlgoINDIRECT {

	// variables for the tid (transaction ids) set of items
	Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();
	
	// Parameters
	int minSuppRelative;
	double minconf = 0;
	double tsRelative = 0;
	
	// for statistics
	long startTimestamp = 0; // start time of the last algorithm execution
	long endTimeStamp = 0; // end time of the last algorithm execution
	
	// object to write the result to disk
	BufferedWriter writer = null;

	// the number of rule found
	private int ruleCount;
	// the size of the database
	private int tidcount =0;
	
	/**
	 * Default constructor
	 */
	public AlgoINDIRECT() {
	}

	/**
	 * Run the algorithm.
	 * @param input the input file path
	 * @param output the output file path 
	 * @param minsup the minimum support threshold
	 * @param ts the ts threshold
	 * @param minconf the minconf threshold
	 * @throws IOException exception if there is an error while writing the output file.
	 */
	public void runAlgorithm(String input, String output, double minsup, double ts, double minconf) throws NumberFormatException, IOException {
		// record the algorithm start time
		startTimestamp = System.currentTimeMillis();
		// create an object to write the output file
		writer = new BufferedWriter(new FileWriter(output)); 
		this.minconf = minconf; // save minconf

		// (1) count the tid set of each item in the database in one database pass
		
		// To count, we use a map, where 
		//     key =  item   
		//     value = set of transactions ids of transactions containing that item
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); // id item, count
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		tidcount=0; // variable used to know the line number that we are reading
		// for each line (transactions) of the input file
		while( ((line = reader.readLine())!= null)){
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			// split the line according to spaces
			String[] lineSplited = line.split(" ");
			// for each item on that line
			for(String stringItem : lineSplited){
				// convert the item from string to int
				int item = Integer.parseInt(stringItem);
				// get the current tids set of this item
				Set<Integer> tids = mapItemTIDS.get(item);
				// if no set, create a new one
				if(tids == null){
					tids = new HashSet<Integer>();
					mapItemTIDS.put(item, tids);
				}
				// add the current transaction id to the set
				tids.add(tidcount);
			}
			tidcount++; // increase the transaction id 
		}
		reader.close(); // close input file
		
		// Convert the absolute minimum support and absolute ts value
		// to relative value by multiplying by the number of transactions
		// in the transaction database.
		this.minSuppRelative = (int) Math.ceil(minsup * tidcount);
		this.tsRelative= (int) Math.ceil(ts * tidcount);
		
		// This algorithm use an Apriori-style generation (level by level)
		// To build level 1, we keep only the frequent items.
		int k=1;
		// create the variable to store itemset from level 1
		List<Itemset> level = new ArrayList<Itemset>();
		// For each item
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS.entrySet().iterator();
		while (iterator.hasNext()) {
			// If the current item is frequent
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator.next();
			if(entry.getValue().size() >= minSuppRelative){ 
				// add the item to this level
				Integer item = entry.getKey();
				Itemset itemset = new Itemset(item);
				itemset.setTIDs(mapItemTIDS.get(item));
				level.add(itemset);
			}else{
				// otherwise the item is not frequent we don't 
				// need to keep it into memory.
				iterator.remove();  
			}
		}
		
		// Sort itemsets of size 1 according to lexicographical order.
		Collections.sort(level, new Comparator<Itemset>(){
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		
		// Now we recursively find larger itemset to generate rules
		// starting from k = 2 and until there is no more candidates.
		k = 2;
		// While the level is not empty
		while (!level.isEmpty() ) {
			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			level = generateCandidateSizeK(level, k); // We keep only the last level... 
			k++; 
		}
		
		// close the file
		writer.close();
		endTimeStamp = System.currentTimeMillis();
	}

	/**
	 * Generate candidate of size K by using frequent itemsets of size k-1.
	 * (this process is similar to Apriori).
	 * @param levelK_1 frequent itemsets of size k-1
	 * @param level  the value of k
	 * @return the list of candidates of size k
	 * @throws IOException exception if there is an error writing the file
	 */
	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1, int level) throws IOException {
		// create an empty list to store the candidate
		List<Itemset> nextLevel = new ArrayList<Itemset>();

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
						if(itemset1.getItems()[k] >= itemset2.get(k)){  
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

				// =======   GENERATE ITEMSETS OF NEXT LEVEL AS IN APRIORI ======================
				Set<Integer> list = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTransactionsIds()){
					if(itemset2.getTransactionsIds().contains(val1)){
						list.add(val1);
					}
				}
		
				if(list.size() >= minSuppRelative){
					// Create a new candidate by combining 
					// itemset1 and itemset2
					int newItemset[] = new int[itemset1.size()+1];
					System.arraycopy(itemset1.itemset, 0, newItemset, 0, itemset1.size());
					newItemset[itemset1.size()] = itemset2.getItems()[itemset2.size() -1];
					Itemset candidate = new Itemset(newItemset);
					candidate.setTIDs(list);
					
					// add the candidate to the set of candidate
					nextLevel.add(candidate);
				}	

			}
		}
		
		// TRY ALL COMBINATION TO GENERATE INDIRECT RULES FROM ITEMSET OF SIZE K, IF K > 2  -- NOT VERY EFFICIENT
		if(level > 2){
			// WE NEED TO FIND TWO IEMSETS WITH ONLY TWO ITEMS a,b THAT ARE DIFFERENT
			// SO WE COMPARE EACH ITEMSET OF SIZE K WITH EACH OTHER ITEMSET OF SIZE K.
			for(int i=0; i< levelK_1.size(); i++){
				for(int j=i+1; j< levelK_1.size(); j++){
					Itemset candidate1 = levelK_1.get(i);
					Itemset candidate2 = levelK_1.get(j);
					
					// We check if the pair of itemset have only one item that is different.
	  loopX:		for(Integer a : candidate1.getItems()){
		  				// if candidate2 does not contain item a
						if(candidate2.contains(a) == false){
							Integer b = null;
							// for each item of candidate 2
							for(Integer itemM : candidate2.getItems()){
								// if candidate1 does not contain that item
								if(candidate1.contains(itemM) == false){
									if(b!= null){
										continue loopX;  // more than two items are different... we don't want that.
									}
									b = itemM;  // the item that is different
								}
							}
							// if there is only one item that is different, then we call this method
							// to check if we can create an indirect rule such that it would meet the
							// ts threshold and the minconf threshold.
							testIndirectRule(candidate1, a, b);  
						}
					}
				}
	
			}
		}
		return nextLevel;
	}

	/**
	 * Test if an indirect rule satisfies the criterion for an indirect association rules  ts  and minconf.
	 * @param itemset a potential itemset that could be a mediator if we remove "a" and "b"
	 * @param a the item a
	 * @param b the item b
	 * @throws IOException exception if error while writing to the output file
	 */
	private void testIndirectRule(Itemset itemset, Integer a, Integer b)
			throws IOException {
		// These sets are respectively the sets of ids 
		// of transactions containing "a" and "b"
		Set<Integer> tidsA = mapItemTIDS.get(a);
		Set<Integer> tidsB = mapItemTIDS.get(b);
		
		// Calculate the support of {a,b} by doing
		// the intersection of these two sets.
		
		int supportAB = 0;  // variable to count the number of IDs in that intersection
		// for each ID in tidFromA
		for(Integer tidFromA : tidsA){
			// if it appears in tidsB
			if(tidsB.contains(tidFromA)){
				// increase the number of IDs shared by both
				supportAB++; 
			}
		}
		
		// if the support of {a,b} is lower than the "ts" threshold.
		if(supportAB < tsRelative ){
			// compute the support of Y U {a}
			int supAY =0;
			// for each tid of transactions containing "a"
	loop1:	for(Integer tidA: tidsA){
				// for each item in "itemset"
				for(Integer item: itemset.getItems()){
					// if this item is not "a" and not "b"
					if(!item.equals(a) && !item.equals(b)){
						// if this item appears in a transaction containing "a"
						if(!mapItemTIDS.get(item).contains(tidA)){
							continue loop1;
						}
					}
				}
				supAY++; // increase the support of Y U {a}
			}
			
			// Calculate the confidence of Y U {a}
			double confAY = supAY / ((double)tidsA.size()) ;
					
			// if the confidence is high enough
			if(confAY >= minconf){
				// We do the same thing....
				// This time we compute the support of Y U {b}
				
				// variable to calculate the support of Y U {b}
				int supBY =0;
				// for each tid of transactions containing "b"
		loop2:	for(Integer tidB: tidsB){
					// for each item in "itemset"
					for(Integer item: itemset.getItems()){
						// if this item is not "a" and not "b"
						if(!item.equals(a) && !item.equals(b)){
							// if this item appears in a transaction containing "a"
							if(!mapItemTIDS.get(item).contains(tidB)){
								continue loop2;
							}
						}
					}
					supBY++; // increase the support of Y U {b}
				}
				// Calculate the confidence of Y U {b}
				double confBY = supBY / ((double)tidsB.size()) ;
				
				// if the confidence is high enough
				if(confBY >= minconf){
					// save the rule
					saveRule(a, b, itemset, confAY, confBY, supAY, supBY);  
				}
			}
		}
	}
	
	/**
	 * Save an indirect association rule.
	 * @param a  the item a
	 * @param b  the item b
	 * @param itemset the mediator if we remove a and b
	 * @param confAY confidence of Y U {a}
	 * @param confBY confidence of Y U {b}
	 * @param supAY support of Y U {a}
	 * @param supBY support of Y U {b}
	 * @throws IOException exception if error writing the file
	 */
	private void saveRule(Integer a, Integer b, Itemset itemset, double confAY, double confBY, int supAY, int supBY) throws IOException{
		ruleCount++; // we increase the number of rule found
		
		// we create a string buffer because it is more efficient
		// for creating a string step by step
		StringBuffer buffer = new StringBuffer();
		// we append all the elements from the rule to the stringbuffer
		buffer.append("(a=");
		buffer.append(a);
		buffer.append(" b=");
		buffer.append(b);
		buffer.append(" | mediator=");
		for(int i=0; i < itemset.size(); i++){
			if(!itemset.get(i).equals(a) && !itemset.get(i).equals(b)){
				buffer.append(itemset.get(i));
				buffer.append(" ");
			}
		}
		buffer.append(")");
		buffer.append(" #sup(a,mediator)= ");
		buffer.append(supAY);
		buffer.append(" #sup(b,mediator)= ");
		buffer.append(supBY);
		buffer.append(" #conf(a,mediator)= ");
		buffer.append(confAY);
		buffer.append(" #conf(b,mediator)= ");
		buffer.append(confBY);
		
		// we write to the file
		writer.write(buffer.toString());
		writer.newLine(); // we write a new line
	}
	
	/**
	 * Print statistics about the last algorithm execution to System.out.
	 */
	public void printStats() {
		System.out
				.println("=============  INDIRECT RULES GENERATION - STATS =============");
		System.out.println(" Transactions count from database : " + tidcount);
		System.out.println(" Indirect rule count : " + ruleCount);

		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)+ " ms");
		System.out
				.println("===================================================");
	}
}
