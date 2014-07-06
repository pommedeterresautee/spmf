package ca.pfv.spmf.algorithms.sequential_rules.topseqrules_and_tns;
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

import java.util.Map;
import java.util.Set;


/**
 * This class represent a sequential rule as output by the TNS and TopSeqRules algorithm.
 * <br/><br/>
 * It is optimized for these algorithms by storing several additional fields that are necessary 
 * for such top-k algorithms. For example, a rule includes the 
 * transactions IDs of the antecedent, the transaction IDs of the consequent, the transaction IDs
 * of the sequences where the antecedent appears before the consequent, maps of occurences for the
 * antecedent and consequent, etc.
 * 
 * @see AlgoTopSeqRules
 * @see AlgoTNS
 * @author Philippe Fournier-Viger
 */
public class Rule implements Comparable<Rule>{
	
	/** antecedent */
	private int[] itemset1; 
	/** consequent */
	private int[] itemset2; 
	/** absolute support */
	int transactioncount; 
	/** the transaction IDs of the antecedent */
	Set<Integer> tidsI;  
	/** the transaction IDs of the consequent */
	Set<Integer> tidsJ; 
	/** transaction IDs of the sequences where the antecedent appears before the consequent */
	Set<Integer> tidsIJ; 	
	/**  maps of first occurences of the antecedent */
	Map<Integer, Short> occurencesIfirst; 
	/**  maps of last occurences of the antecedent */
	Map<Integer, Short> occurencesJlast; 
	/** flag indicating if both left and right expansion should be explored from this rule */
	boolean expandLR = false; 
	 /** confidence of the rule */
	private double confidence; 
	
	/**
	 * Constructor
	 * @param itemset1 antecedent
	 * @param itemset2 consequent
	 * @param confidence confidence of the rule
	 * @param transactioncount absolute support
	 * @param tidsI the transaction IDs of the antecedent
	 * @param tidsJ the transaction IDs of the consequent
	 * @param tidsIJ transaction IDs of the sequences where the antecedent appears before the consequent
	 * @param occurencesIfirst maps of first occurences of the antecedent
	 * @param occurencesJlast maps of last occurences of the antecedent
	 */
	public Rule(int[] itemset1, int[] itemset2, double confidence, int transactioncount, 
			Set<Integer> tidsI, Set<Integer> tidsJ, Set<Integer> tidsIJ, 
			Map<Integer, Short> occurencesIfirst,
			Map<Integer, Short> occurencesJlast){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.confidence = confidence;
		this.transactioncount = transactioncount;
		this.tidsI = tidsI;
		this.tidsJ = tidsJ;
		this.tidsIJ = tidsIJ;
		this.occurencesJlast = occurencesJlast;
		this.occurencesIfirst = occurencesIfirst;
	}

	/**
	 * Get the antecedent itemset.
	 * @return the antecedent.
	 */
	public int[] getItemset1() {
		return itemset1;
	}
	
	/**
	 * Get the consequent itemset.
	 * @return the consequent.
	 */
	public int[] getItemset2() {
		return itemset2;
	}
	
	/**
	 * Get the absolute support (a number of sequences)
	 * @return the absolute support
	 */
	public int getAbsoluteSupport(){
		return transactioncount;
	}
	
	/**
	 * Get the relative support (a percentage)
	 * @param sequencecount  the number of sequence in the original sequence database
	 * @return the relative support
	 */
	public double getRelativeSupport(int sequencecount) {
		return ((double)transactioncount) / ((double) sequencecount);
	}

	/**
	 * Print this rule to the console
	 */
	public void print(){
		System.out.println(toString());
	}
	
	/**
	 * Get a string representation of this rule.
	 * @return the string representation
	 */
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i< itemset1.length; i++){
			buffer.append(itemset1[i]);
			if(i != itemset1.length-1){
				buffer.append(",");
			}
		}
		buffer.append(" ==> ");
		for(int i=0; i< itemset2.length; i++){
			buffer.append(itemset2[i]);
			if(i != itemset2.length-1){
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	/**
	 * Get the confidence of this rule
	 * @return the confidence
	 */
	public double getConfidence() {
		return confidence;
	}
	
	/**
	 * Compare this rule to another rule
	 * @return 0 if equal, 0< if smaller or >0 if larger
	 */
	public int compareTo(Rule o) {
		if(o == this){
			return 0;
		}
		int compare = this.getAbsoluteSupport() - o.getAbsoluteSupport();
		if(compare !=0){
			return compare;
		}
		
		int itemset1sizeA = this.itemset1 == null ? 0 : this.itemset1.length;
		int itemset1sizeB = o.itemset1 == null ? 0 : o.itemset1.length;
		int compare2 = itemset1sizeA - itemset1sizeB;
		if(compare2 !=0){
			return compare2;
		}
		
		int itemset2sizeA = this.itemset2 == null ? 0 : this.itemset2.length;
		int itemset2sizeB = o.itemset2 == null ? 0 : o.itemset2.length;
		int compare3 = itemset2sizeA - itemset2sizeB;
		if(compare3 !=0){
			return compare3;
		}
		
		int compare4 = (int)(this.confidence  - o.confidence);
		if(compare !=0){
			return compare4;
		}

		return this.hashCode() - o.hashCode();
	}
	
	/**
	 * Check if this rule is equal to another (if they have the same items in their antecedent and consequent).
	 * @parameter o another rule
	 * @return true if equal
	 */
	public boolean equals(Object o){
		Rule ruleX = (Rule)o;
		if(ruleX.itemset1.length != this.itemset1.length){
			return false;
		}
		if(ruleX.itemset2.length != this.itemset2.length){
			return false;
		}
		for(int i=0; i< itemset1.length; i++){
			if(this.itemset1[i] != ruleX.itemset1[i]){
				return false;
			}
		}
		for(int i=0; i< itemset2.length; i++){
			if(this.itemset2[i] != ruleX.itemset2[i]){
				return false;
			}
		}
		
		return true;
	}
}
