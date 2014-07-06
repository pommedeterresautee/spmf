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

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;

/**
 * Implementation of a set of MD-Patterns (as defined in the DIM algorithm by Pinto et al, 2001), 
 * sorted by size. <br/>
 * Level 0 = MDPatterns of size 0 <br/>
 * Level 1 = MDPatterns of size 1 <br/>
 *  ...<br/>
 * Level n = MDPatterns of size n
 *  
 * @see AlgoDim
 * @see MDPattern
 * @see MDPatternsDatabase
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */
public class MDPatterns {

	/** a list of list containing the MDPatterns by size
	 For example, the position i is the list of MD-patterns of 
	 size i.*/
	private final List<List<MDPattern>> levels = new ArrayList<List<MDPattern>>();  // sorted by size
	/** the number of patterns stored in this structure*/
	private int patternsCount=0;
	
	/** a name given to the patterns*/
	private final String name;
	
	/**
	 * Constructor.
	 * @param name a name for the patterns that will be contained in this
	 *    structure (String).
	 */
	public MDPatterns(String name){
		this.name = name;
		levels.add(new ArrayList<MDPattern>()); // level 0 is empty.
	}
	
	/**
	 * Print the patterns contained in this structure to System.out.
	 * @param databaseSize the number of MD-patterns in the MD-Pattern database.
	 */
	public void printPatterns(int databaseSize){
		// create a string buffer
		StringBuffer r = new StringBuffer(150);
		r.append(" ------- ");
		r.append(name);
		r.append(" -------\n");
		int levelCount=0;
		// for each level (patterns of the same size) in this
		// structure
		for(List<MDPattern> level : levels){
			r.append("  L");
			r.append(levelCount);
			r.append(" \n");
			// for each pattern in this level
			for(MDPattern pattern : level){
				StringBuffer s = new StringBuffer(100);
				// print information on this pattern
				s.append("  pattern ");
				s.append(pattern.getId());
				s.append(":  ");
				s.append(pattern.toString());
				s.append("support :  ");
				s.append(pattern.getRelativeSupportFormatted(databaseSize));
				s.append(" (");
				s.append(pattern.getAbsoluteSupport());
				s.append("/");
				s.append(databaseSize);
				s.append(") ");
				s.append("\n");
				r.append(s);
			}
			// increase the level count
			levelCount++;
		}
		r.append(" --------------------------------\n");
		// print the string
		System.out.print(r); 
	}
	
	/**
	 * Add a MD-pattern to this structure
	 * @param pattern an md-pattern
	 * @param k  the size of this md-pattern in terms of the number of dimensions
	 */
	void addPattern(MDPattern pattern, int k){
		// for each level in this structure
		while(levels.size() <= k){
			// if there is no list for this level, then create one
			levels.add(new ArrayList<MDPattern>());
		}
		// add the pattern to this level
		levels.get(k).add(pattern);
		// increase the number of patterns found.
		patternsCount++;
	}
	
	/**
	 * Remove a MD pattern from this structure.
	 * @param pattern the pattern
	 * @param k  the size of the pattern
	 */
	 void removePattern(MDPattern pattern, int k){
		// remove it.
		levels.get(k).remove(pattern);
		// decrease the pattern count.
		patternsCount--;
	}
	
	/**
	 * Get the MD-patterns of a given size in terms of number of dimensions.
	 * @param k  the size
	 * @return a list of MDPatterns
	 */
	public List<MDPattern> getLevel(int k){
		return levels.get(k);
	}

	/**
	 * Get the number of md-patterns stored in this structure
	 * @return a int value.
	 */
	public int size() {
		return patternsCount;
	}

	/**
	 * Get the size of the largest MD-patterns stored in this structure.
	 * @return an int value.
	 */
	public int getLevelCount() {
		return levels.size();
	}

}
