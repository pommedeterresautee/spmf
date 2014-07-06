package ca.pfv.spmf.algorithms.sequential_rules.trulegrowth_with_strings;
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
import java.util.List;

import ca.pfv.spmf.input.sequence_database_list_integers.Sequence;
import ca.pfv.spmf.input.sequence_database_list_integers.SequenceDatabase;

/**
 * This class represent the first and last occurences of an itemset in a sequence, as defined 
 * in the RuleGrowth algorithm.
 * 
 * @see AlgoTRuleGrowth_withStrings
 * @see Sequence
 * @see SequenceDatabase
 * @author Philippe Fournier-Viger
 */
public class Occurence {
	/** the sequenceID (a.k.a transaction id) */
	int sequenceID =-1;
	/** a list of occurences (position in this sequence) */
	List<Short> occurences = new ArrayList<Short>();
	
	/**
	 * Contructor
	 * @param sequenceID a sequence ID
	 */
	public Occurence(int sequenceID){
		this.sequenceID = sequenceID;
	}
	
	/**
	 * Add an occurence.
	 * @param occurence the position of an itemset
	 */
	public void add(short occurence){
		occurences.add(occurence);
	}
	
	/**
	 * Get the first occurence in this sequence.
	 * @return the position of an itemset
	 */
	public short getFirst(){
		return occurences.get(0);
	}
	
	/**
	 * Get the last occurence in this sequence.
	 * @return the position of an itemset
	 */
	public short getLast(){
		return occurences.get(occurences.size()-1);
	}
	
	/**
	 * Check if a given Occurence is the same as this one (used to store occurence in Collections).
	 * @param obj another Occurence
	 * @return true if they both have the same sequence ID.
	 */
	public boolean equals(Object obj) {
		return ((Occurence)obj).sequenceID == sequenceID;
	}

	/**
	 * Hashcode method.
	 */
	public int hashCode() {
		return sequenceID;
	}
}
