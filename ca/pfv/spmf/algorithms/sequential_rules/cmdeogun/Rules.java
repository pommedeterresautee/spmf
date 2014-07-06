package ca.pfv.spmf.algorithms.sequential_rules.cmdeogun;
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

import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;

/**
 * This class represents a group of sequential rules found by the CMDeo algorithm.
 * 
 * @see AlgoCMDeogun
 * @see Itemset
 * @see Rule
 * @author Philippe Fournier-Viger
 */
public class Rules {
	// A list for storing sequential rules.
	private final List<Rule> rules = new ArrayList<Rule>();  
	
	/// the name of this group of rules
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
			System.out.print("support: " + rule.getAbsoluteSupport(objectsCount) +
					" (" + rule.getRelativeSupport() + "/" + objectsCount + ")  ");
			// print the confidence
			System.out.print("confidence: " + rule.getConfidence());

			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
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
