package ca.pfv.spmf.algorithms.frequentpatterns.dci_closed;
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
import java.util.Set;

/**
 * This is a basic implementation of the "DCI_Closed" algorithm (see AlgoDCI_Closed_Optimized 
 * for the optimized version).
 * <br/><br/>
 * 
 * The DCI_Closed algorithm finds all closed itemsets in a transaction database. 
 * <br/><br/>
 * 
 * This algorithm was originally proposed in the article:
 * <br/><br/>
 * 
 *   Lucchese, C., Orlando, S. & Perego, Raffaele (2004), DCI_Closed: a fast and memory efficient
 *   algorithm to mine frequent closed itemsets, Proc. 2nd IEEE ICDM Workshop on Frequent Itemset
 *   Mining Implementations at ICDM 2004.
 * <br/><br/>
 * 
 * Implementation note:  <br/>
 *  - My implementation assumes that there is no item named "0".
 * <br/><br/>
 * 
 * Possible optimizations:<br/>
 *  - use a bit matrix like it is suggested in the article<br/>
 *  - remove elements from postsets and use a linkedlist for postsets.<br/>
 *  - closedset could be an array.<br/>
 *  - etc.
 *  
 *@author Philippe Fournier-Viger
 */
public class AlgoDCI_Closed {
	
	// number of closed itemsets found
	int closedCount =0;
	// the number of transaction in the transaction database
	int tidCount =0;
	// the largest item in the transaction database
	int maxItemId =1;  
	
	
	// relative minimum support set by the user
	private int minSuppRelative;
	
	// object to write the output file
	BufferedWriter writer = null; 
	
	// Map to store the database as a verticabl database
	// Key: item   value :  Set of Ids of transactions containing the item
	Map<Integer, Set<Integer>> database = null;
	
	/**
	 * Default constructor
	 */
	public AlgoDCI_Closed() {
	}

	/**
	 * Run the algorithm.
	 * @param input the path of an input file (transaction database).
	 * @param output the path of the output file for writing the result
	 * @param minsup a minimum support threshold
	 * @throws IOException exception if error while writing/reading files
	 */
	public void runAlgorithm(String input, String output, int minsup) throws IOException {
		// record start time
		long startTimestamp = System.currentTimeMillis();
		// reset number of itemsets found
		closedCount=0;
		
		System.out.println("Running the DCI-Closed algorithm");
		
		// Prepare object to write the output file
		 writer = new BufferedWriter(new FileWriter(output)); 
		
		 // save the minimum support
		this.minSuppRelative = minsup;

		// (1) CREATE VERTICAL DATABASE INTO MEMORY
		createVerticalDatabase(input);

		// (2) INITIALIZE VARIABLES FOR THE FIRST CALL TO THE "DCI_CLOSED" PROCEDURE
		// (as described in the paper)
		List<Integer> closedset = new ArrayList<Integer>();
		Set<Integer> closedsetTIDs = new HashSet<Integer>();
		List<Integer> preset = new ArrayList<Integer>();
	 	List<Integer> postset = new ArrayList<Integer>(maxItemId);
		
		// Create postset and sort it by descending order or support.
	 	// For each item:
		for(int i=1; i<= maxItemId; i++){
			// Get the tidset of item i
			Set<Integer> tidset = database.get(i); 
			// if the item is frequent
			if(tidset != null && tidset.size() >= minSuppRelative){
				// add it to postset
				postset.add(i);
			}
		}
		
		// Sort items by support ascending order. 
		// But use the lexicographical order if 
		// the support is the same for two items.
		Collections.sort(postset, new Comparator<Integer>(){
			public int compare(Integer item1, Integer item2) {
				int size1 = database.get(item1).size();  // support is the size of the tidset
				int size2 = database.get(item2).size();  // support is the size of the tidset
				// if the support is the same
				if(size1 == size2){
					// use the lexical order
					return (item1 < item2) ? -1 : 1;
				}
				// otherwise, use the support
				return size1 - size2;
			}
		});
//		System.out.println(postset);
		
		// (3) CALL THE "DCI_CLOSED" RECURSIVE PROCEDURE
		dci_closed(true, closedset, closedsetTIDs, postset, preset);
		
		// print statistics
		System.out.println("========== DCI_CLOSED - STATS ============");
		System.out.println(" Number of transactions: " + tidCount );
		System.out.println(" Number of frequent closed itemsets: " + closedCount );
		System.out.println(" Total time ~: " + (System.currentTimeMillis() - startTimestamp) + " ms");
		// close the file
		writer.close();
	}
	
	/**
	 * The method "DCI_CLOSED" as described in the paper.
	 * @param firstime true if this method is called for the first time
	 * @param closedset the closed set (see paper).
	 * @param closedsetTIDs the tids set of the closed set
	 * @param postset  the postset (see paper for full details)
	 * @param preset  the preset (see paper)
	 * @exception IOException if error writing the output file
	 */
	private void dci_closed(boolean firstTime, List<Integer> closedset, Set<Integer> closedsetTIDs, 
		List<Integer> postset, List<Integer> preset) throws IOException {
		
		//L2: for all i in postset
		for(Integer i : postset){
			// L4 Calculate the tidset of newgen 
			// where newgen is "closedset" U {i}
			Set<Integer> newgenTIDs;
			// if the first time
			if(firstTime){
				// it is the tidset of it
				newgenTIDs = database.get(i);
			}else{
				// otherwise we intersect the tidset of closedset and the
				// tidset of i
				newgenTIDs = intersectTIDset(closedsetTIDs, database.get(i));
			}
			// if newgen has a support no less than minsup
			if(newgenTIDs.size() >= minSuppRelative){
				// L3: newgen = closedset U {i}
				// Create the itemset for newgen
				List<Integer> newgen = new ArrayList<Integer>(closedset.size()+1);
				newgen.addAll(closedset);
				newgen.add(i);
				
				// L5:  if newgen is not a duplicate
				if(is_dup(newgenTIDs, preset) == false){
					// L6: ClosedsetNew = newGen
					List<Integer> closedsetNew = new ArrayList<Integer>();
					closedsetNew.addAll(newgen);
					// calculate tidset
					Set<Integer> closedsetNewTIDs = new HashSet<Integer>();
					// if first time
					if(firstTime){
						// the new tidset of closed set is the tidset of i
						closedsetNewTIDs = database.get(i);
					}else{
						// otherwise, we add the tidset of newgen
						closedsetNewTIDs.addAll(newgenTIDs);
					}
					
					// L7 : PostsetNew = emptyset
					List<Integer> postsetNew = new ArrayList<Integer>();
					// L8 for each j in Postset such that i _ j : 
					for(Integer j : postset){
						// if i is smaller than j according to the total order on items
						if(smallerAccordingToTotalOrder(i, j)){
							// L9
							// if the tidset of j contains the tidset of newgen
							if(database.get(j).containsAll(newgenTIDs)){
								closedsetNew.add(j);
								// recalculate TIDS of closedsetNEW by intersection
								Set<Integer> jTIDs = database.get(j);
								Iterator<Integer> iter = closedsetNewTIDs.iterator();
								while(iter.hasNext()){
									Integer tid = iter.next();
									if(jTIDs.contains(tid) == false){
										iter.remove();
									}
								}
							}else{
								// otherwise add j to the new postset
								postsetNew.add(j);
							}
						}
					}
					
					// L15 : write out Closed_setNew and its support
					writeOut(closedsetNew, closedsetNewTIDs.size());
					
					// L16: recursive call
					// FIXED: we have to make a copy of preset before the recursive call
					List<Integer> presetNew = new ArrayList<Integer>(preset);
					dci_closed(false, closedsetNew, closedsetNewTIDs, postsetNew, presetNew);
					
					// L17 : Preset = Preset U {i}
					preset.add(i);
					
				}
			}	
		}
	}

	/**
	 * Check if an item is smaller than another according to the support ascending order
	 * or if the support is the same, use the lexicographical order.
	 * @param i an item
	 * @param j another item
	 */
	private boolean smallerAccordingToTotalOrder(Integer i, Integer j) {
		// compare the support 
		int size1 = database.get(i).size(); // support of i is the tidset of i
		int size2 = database.get(j).size();// support of j is the tidset of j
		// if the support is the same
		if(size1 == size2){
			// use the lexical order
			return (i < j) ? true : false;
		}
		// otherwise use the support
		return size2 - size1 >0;
	}

	/**
	 * Write a frequent closed itemset that is found to the output file.
	 */
	private void writeOut(List<Integer> closedset, int support) throws IOException {
		// increase the number of closed itemsets
		closedCount++;
		
		StringBuffer buffer = new StringBuffer();
		// for each item in the closed itemset
		Iterator<Integer> iterItem = closedset.iterator();
		while(iterItem.hasNext()){
			// append the item
			buffer.append(iterItem.next());
			// if it is not the last item, append a space
			if(iterItem.hasNext()){
				buffer.append(' ');
			}else{
				break;
			}
		}
		// append the support
		buffer.append(" #SUP: ");
		buffer.append(support);
		// append the buffer
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * The method "is_dup" as described in the paper.
	 * @param newgenTIDs  the tidset of newgen
	 * @param preset      the itemset "preset"
	 */
	private boolean is_dup(Set<Integer> newgenTIDs, List<Integer> preset) {
		// L25
		// for each integer j in preset
		for(Integer j : preset){
			// L26 :  
			// If tidset of newgen is included in tids of j, return true
			if(database.get(j).containsAll(newgenTIDs)){
				// IMPORTANT
				// NOTE THAT IN ORIGINAL PAPER THEY WROTE FALSE, BUT IT SHOULD BE TRUE
				return true; 
			}
		}
		return false;  // NOTE THAT IN ORIGINAL PAPER THEY WROTE TRUE, BUT IT SHOULD BE FALSE
	}


	/**
	 * Perform the intersection of two tidsets.
	 * @param tidset1  a first tidset
	 * @param tidset2  a second tidset
	 * @return the intersection of the two tidsets.
	 */
	private Set<Integer> intersectTIDset(Set<Integer> tidset1,
			Set<Integer> tidset2) {
		// Create the new tidset
		Set<Integer> tidset = new HashSet<Integer>();
		// if tidset1 is larger than tidset2
		if(tidset1.size() > tidset2.size()){
			// for each tid in tidset2
			for(Integer tid : tidset2){
				// if the tid is in tidset1
				if(tidset1.contains(tid)){
					// add it to the new tidset
					tidset.add(tid);
				}
			}
		}else{
			// otherwise
			for(Integer tid : tidset1){
				// if the tid is in tidset2
				if(tidset2.contains(tid)){
					// add it to the new tidset
					tidset.add(tid);
				}
			}
		}
		// return the new tidset
		return tidset;
	}

	/**
	 * Create the in-memory vertical database by reading the input file.
	 * @param input an input file path.
	 * @throws IOException exception if an error while reading the file
	 */
	private void createVerticalDatabase(String input) throws IOException {
		// Prepare object to read the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// variable to count the number of transactions
		tidCount =0;
		maxItemId = 0;
		// the vertical database is a map: key= item  value= tidset
		database = new HashMap<Integer, Set<Integer>>();
		// for each line (transaction) in the transaction database
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
			// for each item
			for(String itemString : lineSplited){
				// convert the item to integer
				Integer item = Integer.parseInt(itemString);
				// update the tidset of the item
				// by adding the tid of the current transaction
				Set<Integer> tidset = database.get(item);
				if(tidset == null){
					tidset = new HashSet<Integer>();
					database.put(item, tidset);
				}
				tidset.add(tidCount);
				
				// if this item is larger than maxItemId, replace it.
				if(item > maxItemId){
					maxItemId = item;
				}
			}
			//increase the number of transactions read until now
			tidCount++;
		}
		// close the input file
		reader.close();
	}
}
