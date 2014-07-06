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
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;

/**
 * Implementation of a set of sequential patterns, grouped by their size (how many items they have) as
 * used by the SeqDim and Fournier-Viger(2008) algorithms. Level i = patterns containing i items.
 * 
 * @see AlgoFournierViger08
 * @see AlgoSeqDim
* @author Philippe Fournier-Viger
 */
public class Sequences {
	/** Sequences are organized into levels.
	 In the following list,  the position i (level i) is the
	 list of sequential containing i items. **/
	final List<List<Sequence>> levels = new ArrayList<List<Sequence>>();  // itemset classé par taille
	/** the number of sequential patterns */
	int sequenceCount=0;
	
	/** the name of this group of sequential patterns */
	private final String name;
	
	/**
	 * Constructor
	 * @param name a name to be given to this group of sequential patterns
	 */
	public Sequences(String name){
		this.name = name;
		levels.add(new ArrayList<Sequence>()); 
	}
	
	/**
	 * Print the seq. patterns to System.out.
	 * @param databaseSize the number of sequences in the database.
	 */
	public void printSequentialPatterns(int databaseSize){
		System.out.println(toString(databaseSize));
	}
	
	/**
	 * Get a string representations of this group of sequences.
	 * @param databaseSize the number of sequences in the sequence database
	 * @return a string
	 */
	public String toString(int databaseSize){
		
		// create a string uffer
		StringBuffer r = new StringBuffer(200);
		// append the name of this structure
		r.append(" ----------");
		r.append(name);
		r.append(" -------\n");
		int levelCount=0;
		// for each level
		for(List<Sequence> level : levels){
			r.append("  L");
			r.append(levelCount);
			r.append(" \n");
			// for each seq. pattern
			for(Sequence sequence : level){
				// append the seq. pattern
				r.append("  pattern ");
				r.append(sequence.getId());
				r.append(":  ");
				r.append(sequence.toString());
				// append the support:
				r.append("support :  ");
				r.append(sequence.getRelativeSupportFormated(databaseSize));
				r.append(" (" );
				r.append(sequence.getAbsoluteSupport());
				r.append('/');
				r.append(databaseSize);
				r.append(") \n");
			}
			levelCount++;
		}
		// append seq. pattern count
		r.append(" -------------------------------- Patterns count : ");
		r.append(sequenceCount);
		// return the String
		return r.toString();
	}
	
	/**
	 * Add a sequential pattern to this structure.
	 * @param sequence a sequential pattern
	 * @param k the number of items in the seq. pattern
	 */
	void addSequence(Sequence sequence, int k){
		// create lists for storing seq. patterns until size k
		while(levels.size() <= k){
			levels.add(new ArrayList<Sequence>());
		}
		// add the pattern to the list for level k
		levels.get(k).add(sequence);
		// increase the sequential pattern count.
		sequenceCount++;
	}
	
	/**
	 * Get all sequential patterns with a given number of items.
	 * @param index  a given number of items.
	 * @return a List of sequential patterns
	 */
	public List<Sequence> getLevel(int index){
		return levels.get(index);
	}
	
	/**
	 * Get the number of level (the number of items in the largest seq. pattern)
	 * @return an integer
	 */
	public int getLevelCount(){
		return levels.size();
	}

	/**
	 * Get the list of levels.
	 * @return  a List of List of Sequence objects.
	 */
	public List<List<Sequence>> getLevels() {
		return levels;
	}
}
