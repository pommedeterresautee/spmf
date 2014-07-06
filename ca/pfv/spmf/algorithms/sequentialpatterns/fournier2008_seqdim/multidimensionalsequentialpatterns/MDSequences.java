package ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns;
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

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;

/**
 * A set of MDSequences organized by levels representing their size. 
 * Returned by the AlgoSeqDim algorithm.<br/>
 * Level 0 = MDSequences of size 0 <br/>
 * Level 1 = MDSequences of size 1 <br/>
 *  ...<br/>
 * Level n = MDSequences of size n <br/>
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */
public class MDSequences {
	
	// A list of list containing the MDSequences by size
	// For example, the position i is the list of MDSequences of 
	// size i.
	private final List<List<MDSequence>> levels = new ArrayList<List<MDSequence>>();  // itemset sorted by size
	// the number of patterns stored in this structure
	private int sequencesCount=0;
	
	// a name given to the patterns
	private final String name;
	
	/**
	 * Constructor.
	 * @param name a name for the patterns that will be contained in this
	 *    structure (String).
	 */
	public MDSequences(String name){
		this.name = name;
		levels.add(new ArrayList<MDSequence>()); // the level 0  is empty by default.
	}
	
	/**
	 * Print the patterns contained in this structure to System.out.
	 * @param sequenceCount the number of MDSequences in the MDSequences database.
	 */
	
	public void printPatterns(int sequenceCount){
		// create a string buffer
		System.out.println(" ------- " + name + " -------");
		int levelCount=0;
		// for each level
		for(List<MDSequence> level : levels){
			System.out.println("  L" + levelCount + " ");
			// for each MDSequence
			for(MDSequence sequence : level){
				// print the MDSequence
				System.out.print("  pattern " + sequence.getId() + ":  ");
				sequence.print();
				// print its support
				System.out.print("support :  " + sequence.getFormattedRelativeSupport(sequenceCount));
				System.out.print(" (" + sequence.getAbsoluteSupport() + "/" + sequenceCount + ") ");
				System.out.println("");
			}
			levelCount++;  // go to next level
		}
		System.out.println(" --------------------------------");
	}
	
	/**
	 * Get a String representation of this set of MDSequences.
	 * @param sequenceCount the number of MDSequences in the MDSequences database.
	 * @return a string
	 */
	public String toString(int sequenceCount){
		// create a string buffer
		StringBuffer out = new StringBuffer(" ------- " + name + " -------");
		int levelCount=0;
		// for each level
		for(List<MDSequence> level : levels){
			out.append("  L" + levelCount + "\n");
			// for each MDSequence
			for(MDSequence sequence : level){
				// print the MDSequence
				out.append("  pattern " + sequence.getId() + ":  \n");
				out.append(sequence.toString());
				// print its support
				out.append("support :  " + sequence.getFormattedRelativeSupport(sequenceCount));
				out.append(" (" + sequence.getAbsoluteSupport() + "/" + sequenceCount + ") \n");
			}
			levelCount++;  // go to next level
		}
		out.append(" --------------------------------");
		return out.toString();
	}
	
	/**
	 * Add a MD-sequence to this structure
	 * @param sequence the MDSequence
	 * @param k  the size of the MDSequence
	 */
	public void addSequence(MDSequence sequence, int k){
		// for each level in this structure
		while(levels.size() <= k){
			// if there is no list for this level, then create one
			levels.add(new ArrayList<MDSequence>());
		}
		// add the pattern to this level
		levels.get(k).add(sequence);
		// increase the number of patterns found.
		sequencesCount++;
	}
	
	/**
	 * Get MDSequences of size k.
	 * @param k the size
	 * @return a list of MDSequence objects
	 */
	public List<MDSequence> getLevel(int k){
		return levels.get(k);
	}

	/**
	 * Get the number of MDSequences stored in this structure.
	 * @return an integer
	 */
	public int size() {
		return sequencesCount;
	}

	/**
	 * Get the size of the largest MD-Sequence stored in this structure.
	 * @return an int value.
	 */
	public List<List<MDSequence>> getLevels() {
		return levels;
	}

	/**
	 * Recalculate the number of MDSequences stored in this structure.
	 */
	public void recalculateSize() {
		// reset the count
		sequencesCount =0;
		// do a for loop over each level
		for(List<MDSequence> level : levels){
			// add the number of pattern for this level to the sum
			sequencesCount += level.size();
		}
	}
}
