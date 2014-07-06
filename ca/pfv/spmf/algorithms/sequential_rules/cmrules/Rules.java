package ca.pfv.spmf.algorithms.sequential_rules.cmrules;
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
 * This class represents a group of sequential rules returned by the CMRules algorithm.
 *
 * @see AlgoCMRules
 * @see Rule
 * @author Philippe Fournier-Viger
 */
public class Rules {
	//  the list of rules stored in this group
	final List<Rule> rules = new ArrayList<Rule>();  
	
	// a name given to this group of rules
	private final String name;
	
	/**
	 * Constructor
	 * @param name  a name that should be given to this set of rules (string)
	 */
	public Rules(String name){
		this.name = name;
	}
	
	/**
	 * Print this group of rules to System.out.
	 * @param objectsCount the number of sequences in the sequence database
	 */
	public void printRules(int objectsCount){
		// print the name
		System.out.println(" ------- " + name + " -------");
		int i=0;
		// for each rule
		for(Rule rule : rules){
			// print the rule
			System.out.print("  rule " + i + ":  ");
			rule.print();
			// print the support
			System.out.print(" seqSupp: " + rule.getSequentialSupport(objectsCount) +
					" (" + rule.getSequentialAbsoluteSeqSupport() + "/" + objectsCount + ") ");
			// print the confidence
			System.out.print(" seqConf: " + rule.getSequentialConfidence() +
					" (" + rule.getAbsoluteSupport() + "/" + rule.getItemset1().getAbsoluteSupport() + ") ");
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	/**
	 * Get of string representatio of the rule
	 * @param objectsCount the number of sequences in the sequence database
	 */
	public String toString(int objectsCount){
		// create a string uffer
		StringBuffer buffer = new StringBuffer(" ------- " + name + " -------\n");
		int i=0;
		// for each rule
		for(Rule rule : rules){
			// print the rule
			buffer.append("  rule ");
			buffer.append(i);
			buffer.append(":  ");
			buffer.append(rule.toString());
			// print the support
			buffer.append("  seqSupp: ");
			buffer.append(rule.getSequentialSupport(objectsCount));
			buffer.append(" (");
			buffer.append(rule.getSequentialAbsoluteSeqSupport());
			buffer.append("/");
			buffer.append(objectsCount);
			// print the confidence
			buffer.append("  seqConf: " );
			buffer.append(rule.getSequentialConfidence());
			buffer.append(" (");
			buffer.append(rule.getAbsoluteSupport());
			buffer.append("/");
			buffer.append(rule.getItemset1().getAbsoluteSupport());
			buffer.append("\n");
			i++;
		}
		buffer.append("--------------------------------\n");
		// return as a string
		return buffer.toString();
	}
	
	/**
	 * Add a rule to this group of rules.
	 * @param rule  the rule (Rule).
	 */
	void addRule(Rule rule){
		rules.add(rule);
	}
	
	/**
	 * Get the number of rules in this group.
	 * @return a int.
	 */
	public int getRulesCount(){
		return rules.size();
	}

	/**
	 * Get the list of rules.
	 * @return A List<Rule> object
	 */
	public List<Rule> getRules() {
		return rules;
	}
}
