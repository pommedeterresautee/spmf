package ca.pfv.spmf.algorithms.frequentpatterns.pascal;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;

/**
 * This class represents an itemset as used by the PASCAL algorithm.
 * <br/><br/>
 * 
 * This file is copyright (c) 2008-2012 Philippe Fournier-Viger <br/><br/>
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).<br/><br/>
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.<br/><br/>
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
* 
 * @see AlgoPASCAL
 * @author Philippe Fournier-Viger
*/
public class ItemsetPascal extends Itemset{
	/** field indicating if this itemset is a generator **/
	public boolean isGenerator = true;
	
	/**
	 * minimum support of the subsets of size k-1 of this itemset (assuming
	 * that this itemset is an itemset of size k)
	 */
	public int pred_sup = Integer.MAX_VALUE;

	/**
	 * Constructor
	 * @param newItemset an itemset
	 */
	public ItemsetPascal(int[] newItemset) {
		super(newItemset);
	}

}
