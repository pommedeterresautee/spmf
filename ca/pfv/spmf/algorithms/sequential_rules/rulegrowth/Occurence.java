package ca.pfv.spmf.algorithms.sequential_rules.rulegrowth;

import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;
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
 * This class represent the first and last occurences of an itemset in a sequence, as defined 
 * in the RuleGrowth algorithm.
 * 
 * @see AlgoRULEGROWTH
 * @see Sequence
 * @see SequenceDatabase
 * @author Philippe Fournier-Viger
 */
public class Occurence {
	/** the first occurence <br/>
	//  e.g.   1 means that the occurence starts at the second itemset of the sequence <br/>
	//         2 means that the occurence starts at the third itemset of the sequence */
	public short firstItemset;
	/** the last occurence */
	public short lastItemset;
	
	/**
	 * Constructor
	 * @param firstItemset  the first occurence 
	 * @param lastItemset   the last occurence 
	 */
	public Occurence(short firstItemset, short lastItemset){
		this.firstItemset = firstItemset;
		this.lastItemset = lastItemset;
	}
}
