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

import java.io.IOException;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;

/**
 * This class is an abstract class for PrefixSpan based algorithms that are used with the multi-dimensional
 * sequential pattern mining algorithm SeqDim.
 * 
 * @see AlgoSeqDim
* @author Philippe Fournier-Viger
 */
public abstract class AbstractAlgoPrefixSpan {
	
	/**
	 * Run the algorithm.
	 * @param database a sequence database
	 * @return some sequential patterns
	 * @throws IOException if error writing to output file (if user wants to write results to output file)
	 */
	public abstract Sequences runAlgorithm(SequenceDatabase database) throws IOException;
	
	/**
	 * Get the minsup parameter used by the algorithm as a double.
	 * @return a double.
	 */
	public abstract double getMinSupp();
}
