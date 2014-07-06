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

/**
 * Implementation of an MDPatterns Database (as defined in the DIM algorithm of Pinto et al, 2001).
 *
 * @see AlgoDim
 * @see MDPattern
 * @see MDPatterns
* @author Philippe Fournier-Viger
 */
public class MDPatternsDatabase {
	/** The list of MDPatterns in this database */
	private final List<MDPattern> patterns = new ArrayList<MDPattern>();

	/**  Array of values indicating the number of different values for each dimension [i]. */
	private int[] valuesCountForDimension = null; 
	
	/**
	 * Add an MD-Pattern to the database
	 * @param pattern
	 */
	public void addMDPattern(MDPattern pattern){
		// We add the MD-Pattern.
		patterns.add(pattern);
		// If this is the first time, we initialize this 
		// array to the number of dimensions.
		if(valuesCountForDimension == null){
			valuesCountForDimension = new int[pattern.size()];
		}else{
			// Otherwise, we update the number of values for each dimension.
			
			// for each dimension
			for(int i=0; i< pattern.size(); i++){
				// get the value for this dimension
				int value = pattern.get(i);
				// if the value is larger than the current value stored 
				// in valuesCountForDimension  and it is not a wildcard (*)
				// then update the largest value for that dimension
				if(value > valuesCountForDimension[i] && value != MDPattern.WILDCARD){
					valuesCountForDimension[i] = value;
				}
			}
		}
	}
	
	/**
	 * Print this MD-Pattern database to the System.out.
	 */
	public void printDatabase(){
		// print general statistics about the database
		System.out
		.println("============  MDPatterns Context ==========");
		System.out.println("Dimensions count : " + getDimensionCount());
		System.out.print("Number of value for each dimension  : ");
		for(int j : valuesCountForDimension){
			System.out.print(" " + j + " ");
		}
		System.out.println();
		
		// Print each MD pattern
		for(MDPattern pattern : patterns){ 
			System.out.print(pattern.getId() + ":  ");
			pattern.print();
			System.out.println("");
		}
	}
	
	/**
	 * Get the pattern count.
	 * @return the number of patterns (int)
	 */
	public int size(){
		return patterns.size();
	}

	/**
	 * Get the list of MDPatterns store in this database
	 * @return a List of MDPatterns
	 */
	public List<MDPattern> getMDPatterns() {
		return patterns;
	}

	/**
	 * Return the number of dimensions in this database.
	 */
	public int getDimensionCount() {
		return valuesCountForDimension.length;
	}

}
