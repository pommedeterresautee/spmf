package ca.pfv.spmf.algorithms.frequentpatterns.clostream;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
 * This is an implementation of the CloStream algorithm for mining
 * closed itemsets from a stream as proposed 
 * by S.J Yen et al.(2009)
 * in the proceedings of the IEA-AIE 2009 conference, pp.773.
 * <br/><br/>
 * 
 * It is a very simple algorithm that do not use a minimum support threshold.
 * It thus finds all closed itemsets.
 *
 *@see Itemset
 *@author Philippe Fournier-Viger
 */
public class AlgoCloSteam {
	
	// a table to store the closed itemsets
	List<Itemset> tableClosed = new ArrayList<Itemset>();
	
	//
	Map<Integer, List<Integer>> cidListMap = new HashMap<Integer, List<Integer>>();

	/**
	 * Constructor that also initialize the algorithm
	 */
	public AlgoCloSteam() { 
		// create the empty set with a support of 0
		Itemset emptySet = new Itemset(new int[] {});
		emptySet.setAbsoluteSupport(0);
		// add the empty set in the list of closed sets
		tableClosed.add(emptySet); 
	}
	
	/**
	 * This method process a new transaction from a stream to update
	 * the set of closed itemsets.
	 * @param transaction a transaction (Itemset)
	 */
	public void processNewTransaction(Itemset transaction){
		// a temporary table (as described in the paper) to 
		// associate itemsets with cids.
		Map<Itemset, Integer> tableTemp = new HashMap<Itemset, Integer>();
		
		// Line 02 of the pseudocode in the article
		// We add the transaction in a temporary table
		tableTemp.put(transaction, 0); 
		
		// Line 03  of the pseudocode in the article
		// Create a set to store the combined cidlist of items in the transaction
		Set<Integer> cidset = new HashSet<Integer>();
		// For each item in the transaction
		for(Integer item : transaction.getItems()){
			// get the cid list of that item
			List<Integer> cidlist = cidListMap.get(item);
			if(cidlist != null){
				// add the cid list to the combined cid list
				cidset.addAll(cidlist);
			}
		}
		
		// Line 04  of the pseudocode in the article
		// For each cid in the combined set of cids
		for(Integer cid : cidset){
			
			// Get the closed itemset corresponding to this cid
			Itemset cti = tableClosed.get(cid);
			// create the intersection of this closed itemset
			// and the transaction.
			Itemset intersectionS = (Itemset) transaction.intersection(cti);

			// Check if the intersection calculated in the previous step is in Temp
			boolean found = false;
			// for each entry in temp
			for(Map.Entry<Itemset, Integer> entry : tableTemp.entrySet()){
				// if it is equal to the intersection
				if(entry.getKey().isEqualTo(intersectionS)){
					// we found it 
					found = true;
					// Get the corresponding closed itemsetitemset  
					Itemset ctt = tableClosed.get(entry.getValue());
					// if the support of cti is higher than ctt
					if(cti.getAbsoluteSupport() > ctt.getAbsoluteSupport()){  
						// set the value as "cid".
						entry.setValue(cid);
					}
					break;
				}
			}
			// If the search was unsuccessful
			if(found == false){ 
				// add the instersection to the temporary table with "cid".
				tableTemp.put(intersectionS, cid);
			}
		}
		
		// For each entry in the temporary table
		for(Map.Entry<Itemset, Integer> xc : tableTemp.entrySet()){
			// get the itemset
			Itemset x = xc.getKey();
			// get the cid
			Integer c = xc.getValue();
			// get the closed itemset for that cid
			Itemset ctc = tableClosed.get(c);
			
			// if the itemset is the same as the closed itemset
			if(x.isEqualTo(ctc)){
				// we have to increase its support
				ctc.increaseTransactionCount();
			}else{ 
				// otherwise the itemset "x" is added to the table of closed itemsets
				tableClosed.add(x);
				// its support count is set to the support of ctc + 1.
				x.setAbsoluteSupport(ctc.getAbsoluteSupport()+1);
				// Finally, we loop over each item in the transaction again
				for(Integer item : transaction.getItems()){
					// we get the cidlist of the current item
					List<Integer> cidlist = cidListMap.get(item);
					// if null
					if(cidlist == null){
						cidlist = new ArrayList<Integer>();
						// we  create one
						cidListMap.put(item, cidlist);
					}
					// then we add x to the cidlist
					cidlist.add(tableClosed.size()-1);
				}
			}
		
		}
	}

	/**
	 * Get the current list of closed itemsets without the empty set.
	 * @return a List of closed itemsets
	 */
	public List<Itemset> getClosedItemsets() {
		// if the empty set is here
		if(tableClosed.get(0).size() ==0){
			// remove it
			tableClosed.remove(0); 
		}
		// return the remaining closed itemsets
		return tableClosed;
	}
}
