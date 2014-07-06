package ca.pfv.spmf.algorithms.associationrules.fhsar;
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


import java.util.HashSet;
import java.util.Set;

/**
 * This class is an association rule implementation optimized 
 * for the FHSAR algorithm for
 * association rule hiding.
 * <br/><br/>
 * A rule stores its support as well as the support of its 
 * antecedent.  The antecedent and consequent are stored as
 * as Set of Integers for fast inclusion checking.
 * 
 * @see AlgoFHSAR
 * @author Philippe Fournier-Viger
 */

class Rule {
	// the support of the rule
	int count;
	// the support of the antecedent (left size of the rule)
	int leftSideCount;

	// the tidset of the antecedent (IDs of transaction containing the antecedent)
	Set<Integer> leftSide = new HashSet<Integer>();
	// the tidset of the consequent (IDs of transaction containing the consequent)
	Set<Integer> rightSide = new HashSet<Integer>();
}
