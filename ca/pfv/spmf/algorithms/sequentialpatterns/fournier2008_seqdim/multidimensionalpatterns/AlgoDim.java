package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTID.AlgoAprioriTID;
import ca.pfv.spmf.algorithms.frequentpatterns.aprioriTIDClose.AlgoAprioriTIDClose;
import ca.pfv.spmf.algorithms.frequentpatterns.charm.AlgoCharm_Bitset;
import ca.pfv.spmf.input.transaction_database_list_integers.TransactionDatabase;
import ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemsets;


/**
 * Implementation of the DIM algorithm by Pinto et al. (2001) to extract frequent MD-Patterns 
 *  (multi-dimensional patterns) from a MD-Database. The algorithm is described in:
 *   <br/><br/>
 *   
 *  Pinto, H., Han, J., Pei, J., Wang, K., Chen, Q., & Dayal, U. (2001, October). 
 *  Multi-dimensional sequential pattern mining. In 
 *  Proceedings of the tenth international conference on Information and knowledge management (pp. 81-88). 
 *  ACM.
 *  <br/><br/>
 *  
 * This implementation use the Apriori, AprioriClose or CHARM algorithms depending on what the user prefers.
 * This allow to find all frequent MD-Patterns or just those that are closed.
 * <br/><br/>
 * 
 * The idea of closed MD sequential pattern mining is described in (Songram, 2006):
 * <br/><br/>
 * 
 * P. Songram, V. Boonjing, S. Intakosum: Closed Multi-dimensional Sequential-Pattern Minin. Proc. of ITNG 2006.
 * <br/><br/>
 * 
 * This algorithm implementation proceeds as follow,  it 
 * (1) convert MD-Patterns into itemsets, 
 * (2) mine frequent (closed) itemsets from the md-patterns generated
 * in Step 1
 * and (3) convert frequent (closed) itemsets back in MD-Patterns.
 *
 * @see MDPattern
 * @see MDPatterns
 * @see MDPatternsDatabase
* @author Philippe Fournier-Viger
 */
public class AlgoDim{
	// the list of MDpatterns found
	private MDPatterns patterns = new MDPatterns("Frequent MD Patterns");
	// the number of dimensions in each pattern
	private int dimensionsCount;
	
	// if true, the algorithm finds closed patterns 
	private boolean findClosedPatterns;
	// if true, the algorithm finds closed patterns with Charm instead
	// of AprioriClose
	private boolean findClosedPatternsWithCharm;
	
	
	// The following structure are used to convert
	// from a dimension value used by MD-Patterns to an item ID
	// used by Apriori and CHarm and vice-versa.
	// The identifier of a mdpattern is a String of the form  "val-i"
	// where val is a dimension value for the i-th dimension.
	
	// Key: item id     Value:  mdpattern identifier
	private Map<Integer, String> mapItemIdIdentifier = new HashMap<Integer,String>();
	// Value: item id     key:  mdpattern identifier
	private Map<String, Integer> mapIdentifierItemId = new HashMap<String, Integer>();
	
	// the largest Item id  that was used when converting from a mdpattern
	// to an itemset
	int lastUniqueItemIdGiven=0;

	/**
	 * @param findClosedPatterns Indicates if this class has to find respectively frequent itemsets
	 *  or frequent closed itemsets.
	 * @param findClosedPatternsWithCharm if true, the algorithm finds closed patterns with Charm instead
	// of AprioriClose
	 */
	public AlgoDim(boolean findClosedPatterns, 
			boolean findClosedPatternsWithCharm){
		this.findClosedPatterns = findClosedPatterns;
		this.findClosedPatternsWithCharm = findClosedPatternsWithCharm;
	}
	
	/**
	 * Run the DIM algorithm
	 * @param mdPatDatabase an md-pattern database
	 * @param minsupp a minimum support threshold as a percentage (double)
	 * @return the md-patterns found
	 * @throws IOException  exception if error reading/writing file
	 */
	public MDPatterns runAlgorithm(MDPatternsDatabase mdPatDatabase, double minsupp) throws IOException {
		// initialize the set of MDpatterns for storing the result
		patterns = new MDPatterns("FREQUENT MD Patterns");
		
		// get the number of dimensions from the first pattern
		// in the mdpattern database
		this.dimensionsCount = mdPatDatabase.getMDPatterns().get(0).size();

		// if the user wants to use the CHARM algorithm
		if(findClosedPatternsWithCharm){ 
			
			// create the transaction database by converting from
			// a mdpattern database
			TransactionDatabase contextCharm = new TransactionDatabase();
			for(MDPattern pattern : mdPatDatabase.getMDPatterns()){
				contextCharm.addTransaction(convertPatternToItemset(pattern));
			}
			
			// run the charm algorithm to get closed patterns
			AlgoCharm_Bitset charm = new AlgoCharm_Bitset();
			Itemsets frequentPatterns = charm.runAlgorithm(null, contextCharm, minsupp, true, 10000);
			
			int maxSupport = 0;
			// Convert patterns found by Charm into MDPatterns
			
			// for each level
			for(List<ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset> itemsets : frequentPatterns.getLevels()){
				// for each pattern found by charm
				for(ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset itemset : itemsets){
					// convert to a md-pattern
					MDPattern pattern = convertItemsetCharmToPattern(itemset);
					// add to the set of patterns found
					patterns.addPattern(pattern, pattern.size());
					
					// if the support is highest seen until
					// now, update the maximum support seen.
					if(itemset.getAbsoluteSupport() > maxSupport){
						maxSupport = itemset.getAbsoluteSupport();
					}
				}
			}
			
			// add the empty set to the list of patterns if necessary
			// if the maximum support is smaller than the number
			// of transactions in the transaction database for charm 
			// (it means that the empty set is a closed itemset)
			if(maxSupport < contextCharm.size()){
				patterns.addPattern(convertItemsetCharmToPattern(new ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset()), 0);
			}
			

		// if the user wants to use the APRIORI-CLOSE algorithm
		}else if(findClosedPatterns){  
		
			// (1) create the transaction database by converting from
			// a mdpattern database
			TransactionDatabase database = new TransactionDatabase();
			for(MDPattern pattern : mdPatDatabase.getMDPatterns()){
				database.addTransaction(convertPatternToItemset(pattern));
			}
			// run the APRIORI-TID-CLOSE algorithm to get closed patterns
			AlgoAprioriTIDClose apriori = new AlgoAprioriTIDClose();
			ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets closedItemsets = apriori.runAlgorithm(database,minsupp, null);

			// Convert patterns found by AprioriClose into MDPatterns
			
			// for each level
			for(List<ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset> itemsets : closedItemsets.getLevels()){
				// for each pattern of that level
				for(ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset itemset : itemsets){
					// convert to a md-pattern
					MDPattern pattern = convertItemsetToPattern(itemset);
					// add to the set of patterns found
					patterns.addPattern(pattern, pattern.size());
				}
			}
		}else{  // otherwise, if the user want to use APRIORI-TID
			
			// (1)create the transaction database by converting from
			// a mdpattern database to a transaction database
			TransactionDatabase database = new TransactionDatabase();
			for(MDPattern pattern : mdPatDatabase.getMDPatterns()){
				database.addTransaction(convertPatternToItemset(pattern));
			}
			
			// Apply the APRIORI-TID algorithm
			AlgoAprioriTID apriori = new AlgoAprioriTID();
			ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemsets closedItemsets = apriori.runAlgorithm(database,minsupp);
			apriori.setEmptySetIsRequired(true);
			
			// Convert patterns found by AprioriClose into MDPatterns
			
			// for each level
			for(List<ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset> itemsets : closedItemsets.getLevels()){
				// for each pattern of that level
				for(ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset itemset : itemsets){
					// convert to a md-pattern
					MDPattern pattern = convertItemsetToPattern(itemset);
					// add to the set of patterns found
					patterns.addPattern(pattern, pattern.size());
				}
			}
			// add the empty set
			patterns.addPattern(convertItemsetCharmToPattern(new ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset()), 0);
	}
		// return the set of patterns found
		return patterns;
	}

	/**
	 * Convert from an item ID to a dimension value.
	 * @param itemID an item ID
	 * @return the dimension value  corresponding to this item ID
	 */
	private Integer getValueForItemId(int itemID){
		// convert to an identifier
		String identifier = mapItemIdIdentifier.get(itemID);
		int index = identifier.indexOf("-");
		// return only the part before the "-"
		return Integer.valueOf(identifier.substring(0, index));
	}
	
	/**
	 * Convert from an item ID to a dimension position.
	 * @param value an item ID
	 * @return the dimension position in the list of dimensions corresponding
	 *    to that item.
	 */
	private Integer getDimensionForItemId(int value){
		// convert to an identifier
		String identifier = mapItemIdIdentifier.get(value);
		int index = identifier.indexOf('-');
		// return only the part after the "-"
		return Integer.valueOf(identifier.substring(index+1, identifier.length()));
	}
	
	/**
	 * Convert dimension value to an item ID.
	 * @param indexDimension  the position of the dimension in the list of dimensions
	 * @param value the value for the dimension
	 * @return the item ID
	 */
	private int convertDimensionValueToItemId(int indexDimension, Integer value){
		// get the item ID by using the map
		Integer itemId = mapIdentifierItemId.get("" + value + "-" + indexDimension);
		// if there is no item ID for this dimension value yet
		if(itemId == null){
			// we create a new item ID
			itemId = lastUniqueItemIdGiven++;
			// we create the corresponding identifier for the 
			// dimension value
			StringBuffer identifier = new StringBuffer();
			identifier.append(value);
			identifier.append('-');
			identifier.append(indexDimension);
			// we update the map so that we can convert
			// from item ID to dimension value and vice versa
			mapIdentifierItemId.put(identifier.toString(), itemId);
			mapItemIdIdentifier.put(itemId, identifier.toString());
		}
		return itemId;
	}
	
	/**
	 * Convert an MD-pattern to an itemset
	 * @param pattern an MD-pattern
	 * @return  an itemset as a list of integers
	 */
	private List<Integer> convertPatternToItemset(MDPattern pattern) {
		// create the itemset
		List<Integer> itemset = new ArrayList<Integer>();
		// for each dimension value in the pattern
		for(int i=0; i < pattern.values.size(); i++){
			// convert to an item ID and add it to the itemset
			itemset.add(convertDimensionValueToItemId(i, pattern.values.get(i)));
		}
		// return the itemset
		return itemset;
	}

	/**
	 * Convert from an itemset to an MD-Pattern
	 * @param itemset an itemset
	 * @return an MD-pattern
	 */
	private MDPattern convertItemsetToPattern(ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset itemset) {
		// create the md-pattern
		MDPattern mdpattern = new MDPattern(0);
		// for each dimension i
		for(int i=0; i< dimensionsCount; i++){
			// for each item j
			for(int j=0; j<itemset.size(); j++){
				// get the dimension corresponding to the item ID
				int dimension = getDimensionForItemId(itemset.get(j));
				// get the dimension value corresponding to the item ID
				int value = getValueForItemId(itemset.get(j));
				// if it is the dimension i
				if(dimension == i){
					// add the dimension value to the MD pattern
					mdpattern.addInteger(value);
				}
			}
			// if the dimension value was not found,
			// add the value *.
			if(mdpattern.size() == i){
				mdpattern.addWildCard();
			}
		}
 
		//We also need to set the tidset of the mdpattern
		mdpattern.setPatternsIDList(itemset.getTransactionsIds());
		
		// we return the mdpattern.
		return mdpattern;
	}
	
	/**
	 * Convert from an itemset used by CHARM to an MD-Pattern
	 * @param itemset
	 * @return
	 */
	private MDPattern convertItemsetCharmToPattern(ca.pfv.spmf.patterns.itemset_array_integers_with_tids_bitset.Itemset itemset) {
		// create the mdpattern
		MDPattern mdpattern = new MDPattern(0);
		// for each dimension i
		for(int i=0; i< dimensionsCount; i++){
			// for each item j
			for(int j=0; j<itemset.size(); j++){
				// create array 
				int[] objects = itemset.getItems();
				// get the dimension corresponding to the item ID
				int dimension = getDimensionForItemId(objects[j]);
				// get the dimension value corresponding to the item ID
				int value = getValueForItemId(objects[j]);
				if(dimension == i){
					// if it is the dimension i
					// add the dimension value to the MD pattern
					mdpattern.addInteger(value);
				}
			}
			// if the dimension value was not found,
			// add the value *.
			if(mdpattern.size() == i){
				mdpattern.addWildCard();
			}
		}
		// HERE WE CONVERT FROM A BITSET TO A SET OF INTEGER
		// NOTE: THIS MAY BE COSTLY AND IT WOULD BE BETTER
		// IF WE DON'T HAVE TO DO THAT AND WE JUST USE A SET OF INTEGER
		// OR A BITSET EVERYWHERE. 
		// BUT SINCE THE CODE FOR THE FOURNIER-VIGER 2008 ALGORITHM
		// IS QUITE COMPLEX, I WILL NOT FIX THIS ISSUE NOW.
		Set<Integer> tidset = new HashSet<Integer>();
		for (int tid = itemset.getTransactionsIds().nextSetBit(0); tid >= 0; tid = itemset.getTransactionsIds().nextSetBit(tid+1)) {
			// make the sum
			tidset.add(tid);
		}
		mdpattern.setPatternsIDList(tidset);
		
		// return the md-pattern
		return mdpattern;
	}

	/**
	 * Print statistics about this algorithm execution to System.out
	 * @param databaseSize  the number of mdpattern in the md-pattern database.
	 */
	public void printStats(int databaseSize) {
		System.out.println("=============  DIM - STATS =============");
		System.out.println(" Frequent patterns count : " + patterns.size()); 
		patterns.printPatterns(databaseSize);
		System.out.println("===================================================");
	}

}
