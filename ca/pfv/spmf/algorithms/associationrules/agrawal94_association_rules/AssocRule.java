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

import ca.pfv.spmf.patterns.rule_itemset_array_integer_with_count.Rule;

/**
 * This class represent an association rule used by the Agrawal algorithm such that it uses the lift mesure.
 *  * It is based on:  Agrawal &
 * al. 1994, IBM Research Report RJ9839, June 1994.
 * 
 * @author Philippe Fournier-Viger
 * @see   AlgoAgrawalFaster94
 * @see   AssocRules
 */
public class AssocRule extends Rule{

	/** lift of the rule */
	private double lift;

	/**
	 * Constructor
	 * 
	 * @param itemset1
	 *            the antecedent of the rule (an itemset)
	 * @param itemset2
	 *            the consequent of the rule (an itemset)
	 * @param supportAntecedent the coverage of the rule (support of the antecedent)
	 * @param transactionCount
	 *            the absolute support of the rule (integer)
	 * @param confidence
	 *            the confidence of the rule
	 * @param lift   the lift of the rule
	 */
	public AssocRule(int[] itemset1, int[] itemset2, int supportAntecedent,
			int transactionCount, double confidence, double lift) {
		super(itemset1, itemset2, supportAntecedent, transactionCount, confidence);
		this.lift = lift;
	}

	/**
	 * Get the lift of this rule.
	 * 
	 * @return the lift.
	 */
	public double getLift() {
		return lift;
	}

	/**
	 * Print this rule to System.out.
	 */
	public void print() {
		System.out.println(toString());
	}

}
