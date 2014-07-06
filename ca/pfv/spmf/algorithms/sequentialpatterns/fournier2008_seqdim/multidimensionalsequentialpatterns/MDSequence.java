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

import java.text.DecimalFormat;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoFournierViger08;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.MDPattern;

/**
 * Implementation of a "MultiDimensional-Sequence" used by the SeqDim algorithm (Pinto et al., 2001).
 * <br/><br/>
 * An MD-Sequence is associated to a MD-Database.
 * <br/><br/>
 * An MD-Sequence has two parts: an mdpattern (the values for each dimension) and a sequence.
 * 
 * @see MDSequenceDatabase
 * @see MDPattern
 * @see Sequence
 * @see AlgoSeqDim
 * @see AlgoFournierViger08
* @author Philippe Fournier-Viger
 */
public class MDSequence {
	
	// the sequence
	private final Sequence sequence;
	// the mdpattern
	private final MDPattern mdpattern;
	
	// the id of this md-sequence
	private final int id; 
	
	// the support of this sequence
	private int support = 0;
	
	/**
	 * Constructor
	 * @param id  the id of the md-sequence
	 * @param mdpattern the md-pattern in this md-sequence
	 * @param sequence  the sequence in this md-sequence
	 */
	public MDSequence(int id, MDPattern mdpattern, Sequence sequence){
		// save the parameters
		this.id = id;
		this.sequence = sequence;
		this.mdpattern = mdpattern;
		// we set the same id for the sequence and mdpattern
		sequence.setID(id);
		mdpattern.setID(id);
	}

	/**
	 * Get the sequence in this md-sequence
	 * @return a Sequence
	 */
	public Sequence getSequence() {
		return sequence;
	}

	/**
	 * Get the MDPattern in this md-sequence
	 * @return a MDPattern
	 */
	public MDPattern getMdpattern() {
		return mdpattern;
	}

	/**
	 * Get the ID of this md-sequence
	 * @return an integer
	 */
	public int getId() {
		return id;
	}

	
	/**
	 * Get the support of this MD-sequence as a percentage (double)
	 * @param databaseSize the database size
	 * @return the support as a double
	 */
	public double getRelativeSupport(int databaseSize) {
//		System.out.println("((( " + transactioncount);
		return ((double)support) / ((double) databaseSize);
	}
	
	/**
	 * Get the support of this MD-sequence as an integer (mdsequence count)
	 * @return the support as an integer
	 */
	public int getAbsoluteSupport(){
		return support;
	}
	
	/**
	 * Get the support of this MD-sequence as a percentage with five
	 * decimals (String)
	 * @param databaseSize the database size
	 * @return the support as a double
	 */
	public String getFormattedRelativeSupport(int databaseSize) {
		// calculate the support
		double supportAsDouble = ((double)support) / ((double) databaseSize);
		// Convert to a string with two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5);
		return format.format(supportAsDouble);
	}
	
	/**
	 * Print a String representation of this MD-Sequence to System.out
	 */
	public void print(){
		// print the id
		System.out.print("MDSequence " + id + ": ");
		// print the md-pattern
		mdpattern.print();
		// print the sequence
		sequence.print();
	}
	
	/**
	 * Get a String representation of this MD-Sequence to System.out
	 * @return a String
	 */
	public String toString(){
		// create a string with the id,
		String out = "MDSequence " + id + ": ";
		// the md pattern,
		out += mdpattern.toString();
		// and the sequence
		out += sequence.toString();
		return out;
	}

	/**
	 * Set the support of this MDSequence 
	 * @param transactioncount the support as an integer value (as asequence count).
	 */
	public void setSupport(int transactioncount) {
		this.support = transactioncount;
	}
	
	/**
	 * Check if this md-sequence strictly contains a given md-sequence.
	 * @param sequence2 the given md-sequence
	 * @return true if it strictly contains the given md-sequence, otherwise false.
	 */
	boolean strictlyContains(MDSequence sequence2) {
		// check if the md patterns contains the other one
		int patternContains = getMdpattern().strictlyContains(sequence2.getMdpattern());
		// check if the sequence contains the other one
		int sequenceContains = getSequence().strictlyContains(sequence2.getSequence());

		// if they are equals return false
		if(patternContains == 2 && sequenceContains ==2){
			return false; // EQUALS
		}
		// if it strictly contains the given pattern
		if(patternContains != 0 && sequenceContains != 0){
			return true;
		}
		// otherwise they are different and the given md-seq. is not
		// strictly contained.
		return false;
	}
	
	/**
	 * Check if this md-sequence contains a given md-sequence.
	 * @param sequence2 the given md-sequence
	 * @return true if it  contains the given md-sequence, otherwise false.
	 */
	boolean contains(MDSequence sequence2) {

		// check if the md patterns contains the other one
		int patternContains = getMdpattern().strictlyContains(sequence2.getMdpattern());
		// check if the sequence contains the other one
		int sequenceContains = getSequence().strictlyContains(sequence2.getSequence());
		
		// if it  contains the given pattern
		if(patternContains != 0 && sequenceContains != 0){
			return true;
		}
		// otherwise return false
		return false;
	}
}
