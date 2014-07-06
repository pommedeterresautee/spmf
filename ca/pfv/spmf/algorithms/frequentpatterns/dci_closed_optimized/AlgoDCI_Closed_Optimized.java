package ca.pfv.spmf.algorithms.frequentpatterns.dci_closed_optimized;
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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This is the optimized implementation of the "DCI_Closed" algorithm.  
 * The DCI_Closed algorithm finds all closed itemsets in a transaction database. <br/><br/>
 *   
 *  DCI_Closed was initially proposed in this article:
 *  <br/><br/>
 * 
 *   Lucchese, C., Orlando, S. & Perego, Raffaele (2004), DCI_Closed: a fast and memory efficient
 *   algorithm to mine frequent closed itemsets, Proc. 2nd IEEE ICDM Workshop on Frequent Itemset
 *   Mining Implementations at ICDM 2004.
 *  <br/><br/>
 * 
 *  Note: My implementation assumes that there is no item named "0".
 *  <br/><br/>
 *  
 *  My implementation include several optimization:<br/>
 *   - the use of a bit matrix (as described in the TKDE paper)<br/>
 *   - projecting the database (as described in the TKDE paper)<br/>
 *   - intersecting bit by bit and stop at first different bit for inclusion check (similar to what is described in the TKDE paper, but check bits instead of words)
 * <br/><br/>
 * 
 * But more optimizations could be done:<br/>
 *  - intersecting word by word and stop at first different word for inclusion check (described in the TKDE paper)<br/>
 *  - reorder columns of the matrix (described in the TKDE paper)<br/>
 *  - reusing results of previous bitwise intersections (described in the TKDE paper)<br/>
 *  - changing for a breath-first DCI-like approach for dense datasets (as described in the TKDE paper)<br/>
 *  - ...<br/>
 *  - remove elements from postsets and use a linkedlist for postsets.<br/>
 *  - closedset could be an array.<br/>
 *  - etc.<br/><br/>
 *  
 *  Some of these further optimizations would need to use a custom BitSet class
 *  instead of the BitSet class of Java, because the BitSet class of Java does not let us
 *  iterate over the words inside the BitSet directly.
 *
 * @see BitMatrix
 * @author Philippe Fournier-Viger
 */
public class AlgoDCI_Closed_Optimized {
	
	// number of closed itemsets found
	int closedCount =0;
	// the number of transaction in the transaction database
	int tidsCount =0;
	// the largest item in the transaction database
	int maxItemId =1;  
	
	// relative minimum support set by the user
	private int minSuppRelative;
	// object to write the output file
	BufferedWriter writer = null; 
	
	/**
	 * Default constructor
	 */
	public AlgoDCI_Closed_Optimized() {
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
		
		// (0) SCAN TO KNOW THE DATABASE SIZE AND # OF ITEMS TO INITIALISE BIT-MATRIX
		firstScan(input);
		
		// create the bit matrix
		final BitMatrix matrix = new BitMatrix(maxItemId, tidsCount);

		// (1) CREATE VERTICAL DATABASE INTO MEMORY
		createVerticalDatabase(input, matrix);

		// (2) INITIAL VARIABLES FOR THE FIRST CALL TO THE "DCI_CLOSED" PROCEDURE
		// (as described in the paper)
		List<Integer> closedset = new ArrayList<Integer>();
		BitSet closedsetTIDs = null;
		List<Integer> preset = new ArrayList<Integer>();
	 	List<Integer> postset = new ArrayList<Integer>(maxItemId);
		
		// Create postset and sort it by descending order or support.
	 	// For each item:
		for(int i=1; i<= maxItemId; i++){
			// if the item is frequent
			if(matrix.getSupportOfItemFirstTime(i) >= minSuppRelative){
				// add it to the postset
				postset.add(i);
			}
		}
		
		// Sort items by support ascending order. 
		// But use the lexicographical order if 
		// the support is the same for two items.
		Collections.sort(postset, new Comparator<Integer>(){
			public int compare(Integer item1, Integer item2) {
				// if the support is the same
				if(matrix.getSupportOfItem(item1) == matrix.getSupportOfItem(item2)){
					// compare the lexical order
					return (item1 < item2) ? -1 : 1;
				}
				// otherwise, use the support
				return matrix.getSupportOfItem(item1) - matrix.getSupportOfItem(item2);
			}
		});
		
		// (3) CALL THE "DCI_CLOSED" RECURSIVE PROCEDURE
		dci_closed(true, closedset, closedsetTIDs, postset, preset, matrix, matrix);
		
		// print statistics
		System.out.println("========== DCI_CLOSED - STATS ============");
		System.out.println(" Number of transactions: " + tidsCount );
		System.out.println(" Number of frequent closed itemsets: " + closedCount );
		System.out.println(" Total time ~: " + (System.currentTimeMillis() - startTimestamp) + " ms");
		// close the file
		writer.close();
	}
	
	/**
	 * Scan database to know the database size and  number of items to 
	 * initialize the bit matrix.
	 * @param input  the input file
	 * @throws IOException exception if error while reading the file
	 */
	private void firstScan(String input) throws NumberFormatException, IOException {
		// Prepareobject  to read the file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		maxItemId = 0;
		tidsCount =0; // variable to count the number of transaction.
		
		// for each line (transaction) until the end of file
		while( ((line = reader.readLine())!= null)){
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			
			// split the transaction into items
			String[] lineSplited = line.split(" ");
			// for each item
			for(String itemString : lineSplited){
				// convert the item from string to integer
				Integer item = Integer.parseInt(itemString);
				// update the maximum item in the database
				if(item > maxItemId){
					maxItemId = item;
				}
			}
			tidsCount++; // increase the transaction count
		}
		// close the file
		reader.close();
	}

	/**
	 * The method "DCI_CLOSED" as described in the paper.
	 * @param firstime true if this method is called for the first time
	 * @param closedset the closed set (see paper).
	 * @param bitset the tids set of the closed set
	 * @param postset  the postset (see paper for full details)
	 * @param preset  the preset (see paper)
	 * @param matrix  the modified matrix
	 * @param originalMatrix  the original bitmatrix
	 * @exception IOException if error writing the output file
	 */
	private void dci_closed(boolean firstTime, List<Integer> closedset, BitSet bitset, 
			List<Integer> postset, List<Integer> preset, BitMatrix matrix, BitMatrix originalMatrix) throws IOException {

		//L2: For all i in postset
		for(Integer i : postset){
			// L4 Calculate the tidset of newgen 
			// where newgen is "closedset" U {i}
			BitSet newgenTIDs;
			// if the first time
			if(firstTime){
				// it is the tidset of it
				newgenTIDs = matrix.getBitSetOf(i);
			}else{
				// otherwise we intersect the tidset of closedset and the
				// tidset of i
				newgenTIDs = (BitSet)bitset.clone();
				newgenTIDs.and(matrix.getBitSetOf(i));
			}
			// if newgen has a support no less than minsup
			if(newgenTIDs.cardinality() >= minSuppRelative){
				// L3: newgen = closedset U {i}
				// Create the itemset for newgen
				List<Integer> newgen = new ArrayList<Integer>(closedset.size()+1);
				newgen.addAll(closedset);
				newgen.add(i);
				
				// L5:  if newgen is not a duplicate
				if(is_dup(newgenTIDs, preset, matrix) == false){
					// L6: ClosedsetNew = newGen
					List<Integer> closedsetNew = new ArrayList<Integer>();
					closedsetNew.addAll(newgen);
					// calculate tidset
					BitSet closedsetNewTIDs = null;
					// if first time
					if(firstTime){
						// the new tidset of closed set is the tidset of i
						closedsetNewTIDs = (BitSet)matrix.getBitSetOf(i).clone();
					}else{
						// otherwise, we add the tidset of newgen
						closedsetNewTIDs = (BitSet)newgenTIDs.clone();
					}
					
					// L7 : PostsetNew = emptyset
					List<Integer> postsetNew = new ArrayList<Integer>();
					// L8 for each j in Postset such that i _ j : 
					for(Integer j : postset){
						// if i is smaller than j according to the total order on items
						if(smallerAccordingToTotalOrder(i, j, originalMatrix)){
							// L9
							// if the tidset of j contains the tidset of newgen
							if(isAllContainedIn(newgenTIDs, matrix.getBitSetOf(j))){
								closedsetNew.add(j);
								// recalculate TIDS of closedsetNEW by intersection
								closedsetNewTIDs.and(matrix.getBitSetOf(j));
							}else{
								// otherwise add j to the new postset
								postsetNew.add(j);
							}
						}
					}
					
					// L15 : write out closedsetNew and its support
					int support = closedsetNewTIDs.cardinality();
					writeOut(closedsetNew, support);
					
					// L16: recursive call
					// FIXED: we have to make a copy of preset before the recursive call
					List<Integer> presetNew = new ArrayList<Integer>(preset);
					if(firstTime){
						// THIS IS THE "Dataset projection" optimization described in the TKDE paper.
						BitMatrix projectedMatrix = projectMatrix(matrix, closedsetNewTIDs, support);
						BitSet replacement = new BitSet(support);
						replacement.set(0, support, true);
						dci_closed(false, closedsetNew, replacement, postsetNew, presetNew, projectedMatrix, matrix);
					}else{
						dci_closed(false, closedsetNew, closedsetNewTIDs, postsetNew, presetNew, matrix, originalMatrix);
					}
					// L17 : Preset = Preset U {i}
					preset.add(i);
				}
			}	
		}
	}

	/**
	 * Check if an item is smaller than another according to the support ascending order
	 * or if the support is the same, use the lexicographical order.
	 */
	private boolean smallerAccordingToTotalOrder(Integer i, Integer j, BitMatrix matrix) {
		if(matrix.getSupportOfItem(i) == matrix.getSupportOfItem(j)){
			return (i < j) ? true : false;
		}
		return matrix.getSupportOfItem(j) - matrix.getSupportOfItem(i) >0;
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
	 * @param matrix      the current transaction database as a bit matrix
	 */
	private boolean is_dup(BitSet newgenTIDs, List<Integer> preset, BitMatrix matrix) {
		// L25
		// For each item in preset
		for(Integer j : preset){
			// L26 :  
			// If tidset of newgen is included in tids of j	
			if(isAllContainedIn(newgenTIDs, matrix.getBitSetOf(j))){
				return true; // FIXED: IN ORIGINAL PAPER THEY WROTE FALSE, BUT IT SHOULD BE TRUE
			}
		}
		return false;  // FIXED: IN ORIGINAL PAPER THEY WROTE TRUE, BUT IT SHOULD BE FALSE
	}
	
	
	/**
	 * Project the bitmatrix with a given bitset.
	 * This removes all the columns in the bitmatrix that do not contain a 1
	 * in the given bitset.
	 * @param matrix the original bitmatrix
	 * @param bitset a bitset
	 * @param projectedsize the number of transaction in the projected bitmatrix.
	 * @return a new bit matrix
	 */
	private BitMatrix projectMatrix(BitMatrix matrix, BitSet bitset, int projectedsize) {
		// create a new batrix
		BitMatrix newMatrix = new BitMatrix(maxItemId, projectedsize);
		// This variable will be used to count the columns in the new bitmatrix
		// because columns with no 1 in bitset will not be kept.
		int newBit =0;
		// for each bit in bitset
		for (int bit = bitset.nextSetBit(0); bit >= 0; bit = bitset.nextSetBit(bit+1)) {
			// for each item
			for(int item = 1; item <= maxItemId; item++){
				// if the bit is set to 1 in the bitset of the item
				if(matrix.getBitSetOf(item).get(bit)){
					// add the tid for that item in the new bit matrix
					// at position newBit
					newMatrix.addTidForItem(item, newBit);
				}
			}
			// increase the current bit position
			newBit++;
		}
		// return the new matrix
		return newMatrix;
	}
	

	/**
	 * Create the in-memory vertical database by reading the input file.
	 * @param input an input file path.
	 * @throws IOException exception if an error while reading the file
	 */
	private void createVerticalDatabase(String input, BitMatrix matrix) throws IOException {
		// Prepare object to read the input file
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		int tidCount =0;
		// for each line (transaction) until the end of the file
		while( ((line = reader.readLine())!= null)){
			// for each item
			for(String itemString : line.split(" ")){
				// add the current transaction id to the tidset of the item
				matrix.addTidForItem(Integer.parseInt(itemString), tidCount);
			}
			// increase the transaction count
			tidCount++;
		}
		// close the file
		reader.close();
	}


	/**
	 * Checks if the TIDs set represented by bs1 is included in the TIDs set represented by bs2.
	 * @param bs1 a first bitset
	 * @param bs2 another bitset
	 * @return true if the first bitset is contained in the second bitset
	 */
	private boolean isAllContainedIn(BitSet bs1, BitSet bs2) {
		// for each bit of bs1
		for (int i = bs1.nextSetBit(0); i >= 0; i = bs1.nextSetBit(i+1)) {
			// if the bit is not in bs2 return false
		     if(bs2.get(i) == false){
		    	 return false;
		     }
		}
		// if all bits of bs1 are in bs2, return true
		return true;
	}

}
