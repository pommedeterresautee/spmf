package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;
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

/**
 * This class represents an item as used by the multidimensional sequential pattern mining algorithm SeqDim
 * and the Fournier-Viger et al. (2008) algorithm.
 * 
 * @see Itemset
 * @see AlgoSeqDim
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */
public class ItemSimple{
	
	// an item as an ID
	private final int id;
	
	/**
	 * Constructor
	 * @param id the ID of the item
	 */
	public ItemSimple(int id){
		this.id = id;
	}
	
	/**
	 * Get the ID of this item.
	 * @return the ID.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get a String representation of this item.
	 * @return a String
	 */
	public String toString(){
		return "" + getId();
	}
	
	/**
	 * Check if this item is equal to another given item
	 * @param object another Item.
	 * @return true if equal, otherwise, false.
	 */
	public boolean equals(Object object){
		ItemSimple item = (ItemSimple) object;
		if((item.getId() == this.getId())){
			return true;
		}
		return false;
	}
	
	/**
	 * Get the hashcode for this item.
	 * @return a hashcode as a int value.
	 */
	public int hashCode()
	{
		String string = ""+getId(); // This could be improved.
		return string.hashCode();
	}
	
}
