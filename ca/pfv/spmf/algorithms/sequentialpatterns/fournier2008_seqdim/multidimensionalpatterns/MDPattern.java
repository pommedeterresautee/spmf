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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of an MD-Pattern as used by the DIM algorithm proposed by Helen Pinto et al. (2001).
 *
 * @see AlgoDim
 * @see MDPatterns
 * @see MDPatternsDatabase
* @author Philippe Fournier-Viger
 */
public class MDPattern {
	
	/** the list of values in this MDPattern*/
	List<Integer> values = new ArrayList<Integer>();
	
	/** a code representing the special value "*" that can be
	 found in md-patterns*/
	public final static int WILDCARD = 9999;
	
	/** a unique id corresponding to this MD-Pattern*/
	private int id; 
	
	/**a list of the IDs of all the patterns that contain this one. */
	protected Set<Integer> patternsID = null;
	
	/**
	 * Constructor.
	 * @param id an ID that should be given to this pattern.
	 */
	public MDPattern(int id){
		this.id = id;
	}

	/**
	 * Get the ID of this pattern.
	 * @return an integer.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Get the size of this pattern.
	 * @return the size as an integer.
	 */
	public int size(){
		return values.size();
	}
	
	/**
	 * Get the value for the i-th dimension as a int.
	 * @param index  the position i.
	 * @return the value.
	 */
	public int getValue(int index){
		return values.get(index);
	}
	
	/**
	 * Get the value for the i-th dimension as an Integer.
	 * @param index  the position i.
	 * @return the value.
	 */
	public Integer get(int index){
		return values.get(index);
	}
	
	/**
	 * Add a dimension value to this pattern.
	 * @param value  the dimension value (integer)
	 */
	public void addInteger(int value){
		values.add(value);
	}
	
	/**
	 * Add a wildcard as dimension value to this md-pattern.
	 */
	public void addWildCard(){
		values.add(WILDCARD);
	}
	
	/**
	 * Add a dimension value to this pattern in first position.
	 * @param value  the dimension value (int)
	 */
	public void addIntegerFirstPosition(int value){
		values.add(0, value);
	}
	
	/**
	 * Add a wildcard (*) to this pattern in first position.
	 */
	public void addWildCardFirstPosition(){
		values.add(0, WILDCARD);
	}
	
	/**
	 * Get the relative support of this pattern (a double value)
	 * @param databaseSize  the database size
	 * @return the relative support
	 */
	public double getRelativeSupport(int databaseSize) {
//		System.out.println("((( " + transactioncount);
		return ((double)patternsID.size()) / ((double) databaseSize);
	}
	
	/**
	 * Get the support of this MD-Pattern as a int value.
	 * @return the support
	 */
	public int getAbsoluteSupport(){
		if(patternsID == null){
			return 0;
		}
		return patternsID.size();
	}
	
	/**
	 * Get the relative support of this pattern as a percentage with
	 * two decimals (string)
	 * @param databaseSize  the database size
	 * @return the relative support as a string
	 */
	public String getRelativeSupportFormatted(int databaseSize) {
		// calculate the support
		double support = ((double)getAbsoluteSupport()) / ((double) databaseSize);
		// Format to appear with two decimals
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(support);
	}
	
	/**
	 * Print this MD-pattern to System.out.
	 */
	public void print(){
		System.out.print(toString());
	}
	
	/**
	 * Get a string representation of this MD-Pattern.
	 */
	public String toString(){
		// create a string buffer
		StringBuffer r = new StringBuffer("[ ");
		// Print the value for each dimension
		
		// for each dimension
		for(int i=0; i< values.size(); i++)
		{
			// if the value is a wildcard, replace by "*"
			if(values.get(i).equals(WILDCARD)){
				r.append("* ");
			}else{
				// otherwise, just print the value
				r.append(values.get(i));
				r.append(' ');
			}
		}
		r.append(']');
		
		// Print the list of ids that contains this pattern:
		if(getPatternsID() != null){
			r.append("  Patterns ID: ");
			for(Integer id : getPatternsID()){
				r.append(id);
				r.append(' ');
			}
		}
		r.append("   ");
		// return the string
		return r.toString();
	}

	/**
	 * Get an abbreviated string representation of this MD-Pattern.
	 */
	public String toStringShort(){
		// create a string buffer
		StringBuffer r = new StringBuffer("[ ");
		// Print the value for each dimension
		for(int i=0; i< values.size(); i++)
		{
			// if the value is a wildcard, replace by "*"
			if(values.get(i).equals(WILDCARD)){
				r.append("* ");
			}else{
				// otherwise, just print the value
				r.append(values.get(i));
				r.append(' ');
			}
		}
		r.append(']');
		// return the string
		return r.toString();
	}
	
	/**
	 * Get the set of patterns ID containing this pattern.
	 * @return  a set of patterns ID
	 */
	public Set<Integer> getPatternsID() {
		return patternsID;
	}

	/**
	 * Set the set of patterns ID containing this pattern.
	 * @param patternsID  a set of patterns ID
	 */
	public void setPatternsIDList(Set<Integer> patternsID) {
		this.patternsID = patternsID;
	}
	
	/**
	 * This method check if this MD-pattern strictly contains
	 * a given MD-pattern.
	 * Note that this method is only good for patterns having the same size.
	 * @param mdpattern2 the given MD-pattern.
	 * @return Return 1 if this mdpattern STRICTLY contains mdpattern2  *AND HAVE THE SAME SUPPORT*
	 * Return 2 if this mdpattern is exactly the same as mdpattern2  *AND HAVE THE SAME SUPPORT*
	 * Return 0 if this mdpattern does not contains mdpattern2.
	 */
	public int strictlyContains(MDPattern mdpattern2) {
		// if they don't have the same support, then return 0
		if(getAbsoluteSupport() != mdpattern2.getAbsoluteSupport()){
			return 0;
		}
		// check if they all have the same value
		boolean allthesame = true;
		// for each dimension
		for(int i=0; i< values.size(); i++){
			// if not the same values for both patterns, then we note it
			if(values.get(i) != mdpattern2.getValue(i)){
				allthesame = false;
			}
			// if not a "*" and the value is different, then
			// return 0.
			if(values.get(i) != WILDCARD && values.get(i) != mdpattern2.getValue(i)){
				return 0;
			}
		}
		// if all the same return 2
		if(allthesame){
			return 2;
		}
		// otherwise return 1.
		return 1;
	}

	/**
	 * Change the dimension value for a given dimension to a given value.
	 * @param indexDimension the position of the dimension.
	 * @param newValue the new value
	 */
	public void set(int indexDimension, int newValue) {
		// delete the old value
		values.remove(indexDimension);
		// add the new value
		values.add(indexDimension, newValue);
	}

	/**
	 * Set the ID of this pattern.
	 * @param id2 the ID.
	 */
	public void setID(int id2) {
		id =id2;
	}

}
