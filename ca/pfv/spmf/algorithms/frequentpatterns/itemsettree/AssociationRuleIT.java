package ca.pfv.spmf.algorithms.frequentpatterns.itemsettree;
/**
 * This class represents an association rule.
 * 
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */
public class AssociationRuleIT {
	// support of the rule
	public int support; 
	// confidence of the rule
	public double confidence;
	// the antecedent of the rule
	public int[] itemset1;
	// the consequent of the rule
	public int[] itemset2;
	
	/**
	 * Get a string representation of this rule
	 */
	public String toString(){
		// create a stringbuffer
		StringBuffer buffer = new StringBuffer();
		// append  items from the antecedent
		buffer.append("[ ");
		for(Integer item : itemset1){
			buffer.append(item);
			buffer.append(" ");
		}
		// arrow
		buffer.append(" ] ==> [");
		// append items from the consequent
		for(Integer item : itemset2){
			buffer.append(item);
			buffer.append(" ");
		}
		// append the support and confidence
		buffer.append(" ]  #SUP: ");
		buffer.append(support);
		buffer.append("  #CONF:");
		buffer.append(confidence);
		buffer.append("\n");
		// return the string
		return buffer.toString();
	}
	
}
