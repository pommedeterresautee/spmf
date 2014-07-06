package ca.pfv.spmf.algorithms.frequentpatterns.zart;
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
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
* This class represents the TC table of candidates , used by the Zart algorithm.
* 
* @see AlgoZart
* @author Philippe Fournier-Viger
*/
public class TCTableCandidate {

	/** This structure stores the candidate itemsets by size
	 Position i contains the list of candidate itemsets of size i */
	public final List<List<Itemset>> levels = new ArrayList<List<Itemset>>();  // itemset ordered by size
	
	/** This map is the mapPredSupp used by Zart to store the support of a candidate
	<br/> key: itemset   value: support */
	Map<Itemset, Integer> mapPredSupp = new HashMap<Itemset, Integer>();
	/** this map indicate the key value for this itemset
	<br/> key: itemset   value: key value */
	Map<Itemset, Boolean> mapKey = new HashMap<Itemset, Boolean>();
	
	/**
	 * This map checks if there is an itemset of size i with its key value to true
	 * @param i  the size i
	 * @return true if yes, otherwise false
	 */
	boolean thereisARowKeyValueIsTrue(int i) {
		// for each itemset of size i
		for(Itemset c : levels.get(i)){
			// if key set to true, then return yes
			if(mapKey.get(c) == true){
				return true;
			}
		}
		// otherwise return false
		return false;
	}

}
