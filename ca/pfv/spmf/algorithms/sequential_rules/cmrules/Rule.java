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


import ca.pfv.spmf.patterns.itemset_array_integers_with_tids.Itemset;

/**
 * This class represent a rule as found by the CMRules algorithm (an association rules as in Phase 1 of 
 * CMRules or a sequential rule as in phase 2 of CMRules.
 * 
 * This class is specially designed for CMRules. It contains the "transactionCount" which is the support
 * as defined in association rule mining, plus the "sequentialTransactionCount" which is the support
 * as defined in sequential rule mining. Besides, each itemset (antecedent and consequent) carry their  
 * corresponding transaction ID lists (this is necessary for phase 2 of the CMRules algorithm).
 *  
 *  @see AlgoCMRules
 *  @see Rules
 *  @see Itemset
 *  @author Philippe Fournier-Viger
 */
public class Rule {
	/** antecedent itemset */
	private Itemset itemset1;  
	/** consequent  itemset*/
	private Itemset itemset2; 
	/** association support */
	private int transactionCount; 
	/** sequential support  */
	 int sequentialTransactionCount;   
	/** confidence */
	private double confidence;  
	
	/**
	 * Constructor
	 * @param itemset1   antecedent itemset
	 * @param itemset2 consequent  itemset
	 * @param transactionCount association support
	 * @param confidence confidence
	 */
	public Rule(Itemset itemset1, Itemset itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactionCount =  transactionCount;
		this.confidence = confidence;
	}
	
	/**
	 * Constructor by making a copy of a rule.
	 * @param rule  a rule
	 */
	public Rule(Rule rule	) {
		// copy the fields
		itemset1 = rule.getItemset1();
		itemset2 = rule.getItemset2();
		confidence = rule.getConfidence();
		this.transactionCount =  rule.getAbsoluteSupport();
	}

	/**
	 * Get the rule antecedent
	 * @return an Itemset
	 */
	public Itemset getItemset1() {
		return itemset1;
	}

	/**
	 * Get the rule consequent
	 * @return an Itemset
	 */
	public Itemset getItemset2() {
		return itemset2;
	}
	
//	/**
//	 * Get the "causality measure".
//	 * @return
//	 */
//	public double getCausality() {
//		return ((double)sequentialTransactionCount) / ((double) transactionCount);
//	}
	
	/**
	 * Get relative association support of this rule.
	 * @param objectCount  the database size.
	 * @return  a double value
	 */
	public double getRelativeSupport(int objectCount) {
		return ((double)transactionCount) / ((double) objectCount);
	}
	
	/**
	 * Get the association support of this rule as a int.
	 * @return a int value.
	 */
	public int getAbsoluteSupport(){
		return transactionCount;
	}

	/**
	 * Get the confidence.
	 * @return confidence as a double value.
	 */
	public double getConfidence() {
		return confidence;
	}
	
	/**
	 * Print the itemset to System.out.
	 */
	public void print(){
		System.out.println(toString());
	}
	
	/**
	 * Get a String representation of this rule.
	 * @return a string
	 */
	public String toString(){
		return itemset1.toString() +  " ==> " + itemset2.toString();
	}
	
	/**
	 * Get the sequential support of this rule.
	 * @return a int value.
	 */
	public int getSequentialAbsoluteSeqSupport() {
		return sequentialTransactionCount;
	}

	/**
	 * Get the sequential support of this rule as a double, relative value.
	 * @return a double value.
	 */
	public double getSequentialSupport(int objectCount) {
		return ((double)sequentialTransactionCount) / ((double) objectCount);
	}

	/**
	 * Get the sequential confidence of this rule.
	 * @return a doule value.
	 */
	public double getSequentialConfidence() {
		// the confidence is the sequential support divided by
		// the support of itemset1
		return ((double)sequentialTransactionCount) / ((double) itemset1.getAbsoluteSupport());
	}

	/**
	 * Increment the association support of this rule by 1.
	 */
	void incrementTransactionCount() {
		transactionCount++;
	}

}
