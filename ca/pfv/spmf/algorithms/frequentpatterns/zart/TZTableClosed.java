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
 * This class represents the TZ table containing the closed itemsets
 * and their generators, used by the Zart algorithm.
 *
 * @see AlgoZart
 * @author Philippe Fournier-Viger
 */
public class TZTableClosed {
	
	/** This store the closed itemsets by size
	 At position i, there are the list of closed itemsets 
	 of size i */
	public final List<List<Itemset>> levels = new ArrayList<List<Itemset>>();  // itemset classé par taille
	
	/** each entry of the following map is :  <br/>
	 key : a closed itemset       <br/>
	 values : the corresponding generator(s)  */
	public Map<Itemset, List<Itemset>> mapGenerators = new HashMap<Itemset, List<Itemset>>();

	/**
	 * Add a closed itemsets to this structure.
	 * @param itemset  the itemset
	 */
	void addClosedItemset(Itemset itemset){
		// if there is no array list until the size of this itemset
		// then create them.
		while(levels.size() <= itemset.size()){
			levels.add(new ArrayList<Itemset>());
		}
		// add the itemset in the proper arraylist according to the size of the itemset
		levels.get(itemset.size()).add(itemset);
	}
	
	/**
	 * Get the closed itemsets of a given size i.
	 * @param i  the size i.
	 * @return  a list of itemsets
	 */
	public List<Itemset> getLevelForZart(int i){
		// if this level does not exist
		if(i+1 == levels.size()){
			// create it
			List<Itemset> newList = new ArrayList<Itemset>();
			levels.add(newList);
			return newList;
		}
		// return the list of itemsets  (empty or not)
		return levels.get(i+1);
	}
}
