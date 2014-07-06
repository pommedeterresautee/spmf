package ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules;

/* This file is copyright (c) 2008-2012 Philippe Fournier-Viger
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * This class represents a list of association rules
 * found by the Agrawal algorithm for association rule mining.
 * It is used for storing the rules found by the algorithm,
 * when the user choose to keep the result into memory
 * rather than saving it to a file.
 * 
 * @see   AlgoAgrawalFaster94
 * @see   AssocRule
 * @author Philippe Fournier-Viger
 */

public class AssocRules {
	// a list of association rules
	public final List<AssocRule> rules = new ArrayList<AssocRule>();  // rules
	
	// a name that an algorithm can give to this list of association rules
	private final String name;
	
	/**
	 * Sort the rules by confidence
	 */
	public void sortByConfidence(){
		Collections.sort(rules, new Comparator<AssocRule>() {
			public int compare(AssocRule r1, AssocRule r2) {
				return (int)((r2.getConfidence() - r1.getConfidence() ) * Integer.MAX_VALUE);
			}
		});
	}
	
	/**
	 * Constructor
	 * @param name  a name for this list of association rules (string)
	 */
	public AssocRules(String name){
		this.name = name;
	}
	
	/**
	 * Print all the rules in this list to System.out.
	 * @param databaseSize the number of transactions in the transaction database where the rules were found
	 */
	public void printRules(int databaseSize){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(AssocRule rule : rules){
			System.out.print("  rule " + i + ":  " + rule.toString());
			System.out.print("support :  " + rule.getRelativeSupport(databaseSize) +
					" (" + rule.getAbsoluteSupport() + "/" + databaseSize + ") ");
			System.out.print("confidence :  " + rule.getConfidence());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	/**
	 * Print all the rules in this list to System.out, and include information about the lift measure.
	 * @param databaseSize the number of transactions in the transaction database where the rules were found
	 */
	public void printRulesWithLift(int databaseSize){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(AssocRule rule : rules){
			System.out.print("  rule " + i + ":  " + rule.toString());
			System.out.print("support :  " + rule.getRelativeSupport(databaseSize) +
					" (" + rule.getAbsoluteSupport() + "/" + databaseSize + ") ");
			System.out.print("confidence :  " + rule.getConfidence());
			System.out.print(" lift :  " + rule.getLift());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	/**
	 * Return a string representation of this list of rules
	 * @param databaseSize the number of transactions in the database where the rules were found.
	 * @return a string
	 */
	public String toString(int databaseSize){
		// create a string buffer
		StringBuffer buffer = new StringBuffer(" ------- ");
		buffer.append(name);
		buffer.append(" -------\n");
		int i=0;
		// for each rule
		for(AssocRule rule : rules){
			// append the rule, its support and confidence.
			buffer.append("   rule ");
			buffer.append(i);
			buffer.append(":  ");
			buffer.append(rule.toString());
			buffer.append("support :  ");
			buffer.append(rule.getRelativeSupport(databaseSize));

			buffer.append(" (");
			buffer.append(rule.getAbsoluteSupport());
			buffer.append("/");
			buffer.append(databaseSize);
			buffer.append(") ");
			buffer.append("confidence :  " );
			buffer.append(rule.getConfidence());
			buffer.append("\n");
			i++;
		}
		return buffer.toString(); // return the string
	}
	
	/**
	 * Add a rule to this list of rules
	 * @param rule the rule to be added
	 */
	public void addRule(AssocRule rule){
		rules.add(rule);
	}
	
	/**
	 * Get the number of rules in this list of rules
	 * @return the number of rules
	 */
	public int getRulesCount(){
		return rules.size();
	}

	/**
	 * Get the list of rules.
	 * @return a list of rules.
	 */
	public List<AssocRule> getRules() {
		return rules;
	}
	
	
}
