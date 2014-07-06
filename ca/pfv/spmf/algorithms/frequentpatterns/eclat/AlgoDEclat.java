package ca.pfv.spmf.algorithms.frequentpatterns.eclat;
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.datastructures.triangularmatrix.TriangularMatrix;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets;
import ca.pfv.spmf.tools.MemoryLogger;
 

/**
 * This is a version of the dECLAT algorithm. It uses sets of integers to represent tidsets. It 
 * extends the class AlgoDEclat to avoid redundancy of common code.
 * Note than unlike Eclat, dEclat returns itemsets annotated with diffsets instead of tidsets.
 * About implementation details, note that this implementation uses tidsets initially for single items, 
 * then it uses diffsets starting from itemsets containing two itemsets (2-itemsets).
 * 
 * See this article for details about dECLAT:
 * <br/><br/>
 * 
 * Zaki, M.J., Gouda, K.: Fast vertical mining using diffsets. Technical Report 01-1, Computer Science Dept., Rensselaer Polytechnic Institute (March 2001) 10
 * <br/><br/>
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
public class AlgoDEclat extends AlgoEclat{

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  dECLAT v0.96j - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Frequent itemsets count : "
				+ itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Maximum memory usage : "
				+ MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println("===================================================");
	}

	/**
	 * This method performs the calculation of a new diffset by merging two tidsets/diffsets.
	 * @param tidsetI the first tidset/diffset
	 * @param supportI  the cardinality of the first tidset/diffset
	 * @param tidsetJ  the second tidset/diffset
	 * @param supportJ the cardinality of the second tidset/diffset
	 * @return the resulting tidset.
	 */
	private Set<Integer> performAND(Set<Integer> tidsetI, int supportI, Set<Integer> tidsetJ, int supportJ) {
		// Create the new tidset that will store the difference
		Set<Integer> diffsetIJ = new HashSet<Integer>();
		// for each tid containing j
		for(Integer tid : tidsetJ) {
			// if the transaction does not contain i, add it to the diffset
			if(tidsetI.contains(tid) == false) {
				// add it to the intersection
				diffsetIJ.add(tid);
			}			
		}
		// return the new tidset
		return diffsetIJ;
	}
	
	/**
	 * Calculate the support of an itemset X using the tidset of X if the size = 1. Otherwise uses diffsets
	 * to calculate the support
	 * @param lengthOfX  the length of the itemset X - 1 (used by dEclat)
	 * @param supportPrefix the support of the prefix (not used by Eclat, but used by dEclat).
	 * @param tidsetI the tidset of X
	 * @return the support
	 */
	private int calculateSupport(int supportPrefix, int lengthOfX, Set<Integer> tidsetX) {
		// if length of prefix = 1 then we are using tidsets
		if(lengthOfX == 1) {
			return tidsetX.size();
		}else {
			// otherwise we are using diffsets
			return supportPrefix - tidsetX.size();
		}
	}


}
