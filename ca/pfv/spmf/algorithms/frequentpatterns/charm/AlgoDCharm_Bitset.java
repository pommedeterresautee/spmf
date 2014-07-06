package ca.pfv.spmf.algorithms.frequentpatterns.charm;
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

import java.util.BitSet;
import java.util.Map;

import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;
 
/**
 * This is an implementation of the dCharm algorithm. The difference between DECLAT
 * and CHARM is that dCharm utilizes diffsets instead of tidsets.
 * In this implementation, diffsets are represented as bitsets.
 * Note that this class is a subclass of the Charm algorithm because a lot of
 * code is the same and we wanted to avoid redundancy. 
 * 
 * IMPORTANT: dCharm returns Itemsets annotated with their diffsets
 * rather than tidsets when the user choose to keep the result in memory.
 *  
 * dCharm was proposed by ZAKI (2000).
 * <br/><br/>
 * 
 * See this article for details about dCharm:
 * <br/><br/>
 * 
 * Mohammed Javeed Zaki, Ching-Jiu Hsiao: CHARM: An Efficient Algorithm for Closed Itemset Mining. SDM 2002. * <br/><br/>
 * 
 * and diffsets have been proposed in: <br/><br/>
 * 
 * M. J. Zaki and K. Gouda. Fast vertical mining using Diffsets. Technical Report 01-1, Computer Science
 * Dept., Rensselaer Polytechnic Institute, March 2001.
 * 
 * This  version  saves the result to a file
 * or keep it into memory if no output path is provided
 * by the user to the runAlgorithm method().
 * 
 * @see TriangularMatrix
 * @see TransactionDatabase
 * @see Itemset
 * @see Itemsets
 * @author Philippe Fournier-Viger
 */
public class AlgoDCharm_Bitset extends AlgoCharm_Bitset{

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  dCharm vALTERNATE-Bitset - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : " + database.size());
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("===================================================");
	}
	
	/**
	 * This method scans the database to calculate the support of each single item
	 * @param database the transaction database
	 * @param mapItemTIDS  a map to store the tidset corresponding to each item
	 * @return the maximum item id appearing in this database
	 */
	private int calculateSupportSingleItems(TransactionDatabase database,
			final Map<Integer, BitSetSupport> mapItemTIDS) {
		
		// (1) First database pass : calculate diffsets of each item.
		int maxItemId = 0;
		// for each transaction
		for (int i = 0; i < database.size(); i++) {
			// Add the transaction id to the set of all transaction ids
			// for each item in that transaction
			
			// For each item
			for (Integer item : database.getTransactions().get(i)) {
				// Get the current tidset of that item
				BitSetSupport tids = mapItemTIDS.get(item);
				// If none, then we create one
				if(tids == null){
					tids = new BitSetSupport();
					// For a new item, we sets all the bits of its diffset to true
					tids.bitset.set(0, database.size(), true);
					mapItemTIDS.put(item, tids);
					// we remember the largest item seen until now
					if (item > maxItemId) {
						maxItemId = item;
					}
				}
				//We set to false the bit corresponding to this transaction
				// in the diffset of that item
				tids.bitset.set(i, false);
				// we increase the support of that item
				tids.support++;
			}
		}	
		return maxItemId;
	}

	
	/**
	 * Perform the intersection of two diffsets for itemsets containing more than one item.
	 * @param tidsetI the first diffset
	 * @param tidsetJ the second diffset
	 * @return the resulting diffset and its support
	 */
	private BitSetSupport performAND(BitSetSupport tidsetI, BitSetSupport tidsetJ) {
		// Create the new diffset 
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		// Calculate the diffset 
		bitsetSupportIJ.bitset = (BitSet)tidsetJ.bitset.clone();
		bitsetSupportIJ.bitset.andNot(tidsetI.bitset);
		// Calculate the support
		bitsetSupportIJ.support = tidsetI.support - bitsetSupportIJ.bitset.cardinality();
		// return the new diffset
		return bitsetSupportIJ;
	}
	
	/**
	 * Perform the intersection of two diffsets representing single items.
	 * @param tidsetI the first diffset
	 * @param tidsetJ the second diffset
	 * @param supportIJ the support of the intersection (already known) so it does not need to 
	 *                  be calculated again
	 * @return  the resulting diffset and its support
	 */
	private BitSetSupport performANDFirstTime(BitSetSupport tidsetI,
			BitSetSupport tidsetJ, int supportIJ) {
		// Create the new diffset and perform the logical AND to intersect the diffsets
		BitSetSupport bitsetSupportIJ = new BitSetSupport();
		//Calculate the diffset
		bitsetSupportIJ.bitset = (BitSet)tidsetJ.bitset.clone();
		bitsetSupportIJ.bitset.andNot(tidsetI.bitset);
		// Calculate the support
		bitsetSupportIJ.support = tidsetI.support - bitsetSupportIJ.bitset.cardinality();
		// return the new tidset
		return bitsetSupportIJ;
	}


}
