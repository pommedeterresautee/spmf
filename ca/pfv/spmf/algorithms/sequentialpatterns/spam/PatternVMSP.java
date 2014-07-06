package ca.pfv.spmf.algorithms.sequentialpatterns.spam;

/**
 * Implementation of a pattern found by the VMSP algorithm.
 * <br/><br/>
 * 
 * Copyright (c) 2013 Philippe Fournier-Viger, Antonio Gomariz
 *  <br/><br/>
 *  
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 *  <br/><br/>
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <br/><br/>
 * 
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br/><br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with SPMF. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @see AlgoVMSP
*  @see Prefix
*  @author Philippe Fournier-Viger  & Antonio Gomariz
 */
public class PatternVMSP implements Comparable<PatternVMSP>{
	
	PrefixVMSP prefix;
	public int support;

	public PatternVMSP(PrefixVMSP prefix, int suppport) {
		this.prefix = prefix;
		this.support = suppport;
//		if(prefix != null){
//			System.out.println("pat prefix" + prefix.toString() + " " + support);
//		}
	}

	public int compareTo(PatternVMSP o) {
		if(o == this){
			return 0;
		}
		int compare = o.prefix.sumOfEvenItems + o.prefix.sumOfOddItems
				- this.prefix.sumOfEvenItems - this.prefix.sumOfOddItems;
		if(compare !=0){
			return compare;
		}

		return this.hashCode() - o.hashCode();
	}

	public PrefixVMSP getPrefix() {
		return prefix;
	}

	public int getSupport() {
		return support;
	}
}
