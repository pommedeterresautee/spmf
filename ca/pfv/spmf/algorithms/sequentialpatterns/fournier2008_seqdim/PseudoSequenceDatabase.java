package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.kmeans_for_fournier08.Cluster;

/**
 * This class represents a projected database as used by SeqDim and Fournier-Viger (2008) algorithms.
 * A projected database is a list of pseudoSequences (projected sequences).
 * 
 * @see PseudoSequence
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */
class PseudoSequenceDatabase {
	// the list of pseudo-sequences
	private List<PseudoSequence> pseudoSequences = new ArrayList<PseudoSequence>();
	
	// if the Fournier-Viger08 algorithm was used, this variable
	// may indicate which cluster was used to obtain this projected database,
	// otherwise it is null
	private Cluster cluster = null;

	/**
	 * Get the list of pseudo-sequences.
	 * @return a List of PseudoSequences
	 */
	public List<PseudoSequence> getPseudoSequences(){
		return pseudoSequences;
	}
	
	/**
	 * Print this database to System.out.
	 */
	public void printDatabase(){
		System.out.println(toString());
	}
	
	/**
	 * Get a String representation of this database.
	 * @return a String.
	 */
	public String toString(){
		// create string uffer
		StringBuffer r = new StringBuffer("============  CONTEXTE ==========");
		// for each pseudo sequence
		for(PseudoSequence sequence : pseudoSequences){ 
			// append the sequence id
			r.append(sequence.getId());
			r.append(":  ");
			// append the itemsets
			r.append(sequence.toString());
			r.append('\n');
		}
		/// return the String
		return r.toString();
	}
	
	/**
	 * Get the number of pseudo-sequences.
	 * @return an int value
	 */
	public int size(){
		return pseudoSequences.size();
	}

	/**
	 * Get the sequence IDs of the pseudo-sequences.
	 * @return a Set of Integer objects.
	 */
	public Set<Integer> getSequenceIDs() {
		// create a Set
		Set<Integer> sequenceIDs = new HashSet<Integer>();
		// for each sequence, add its IDs to the set
		for(PseudoSequence sequence : getPseudoSequences()){
			sequenceIDs.add(sequence.getId());
		}
		// return the set
		return sequenceIDs;
	}
	
	
	/**
	 * Get the cluster that was used to obtain this projected database.
	 * @return the Cluster or null if none is associated.
	 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * Set the cluster that was used to obtain this projected database.
	 * @param cluster the Cluster or null if none is associated.
	 */
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * Add a pseudo-sequence to this database.
	 * @param newSequence a PseudoSequence
	 */
	public void addSequence(PseudoSequence newSequence) {
		pseudoSequences.add(newSequence);
		
	}
}
