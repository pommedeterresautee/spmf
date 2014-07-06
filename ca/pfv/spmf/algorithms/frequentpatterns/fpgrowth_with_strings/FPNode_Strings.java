package ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth_with_strings;

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
 * This is an implementation of a FPTree node for the version of FPGrowth where
 * items are represented by Strings rather than Integers.
 *
 * @see FPTree_Strings
 * @see AlgoFPGrowth_Strings
 * @author Philippe Fournier-Viger
 */
public class FPNode_Strings {
	 // item id
	String itemID = null; 
	// support of that node
	int counter = 1;  
	
	// reference to the parent node or null if this is the root
	FPNode_Strings parent = null; 
	// references to the child(s) of that node if there is some
	List<FPNode_Strings> childs = new ArrayList<FPNode_Strings>();
	
	FPNode_Strings nodeLink = null; // link to next node with the same item id (for the header table).
	
	/**
	 * Default constructor
	 */
	FPNode_Strings(){
		
	}

	/**
	 * Return the immediate child of this node having a given ID.
	 * If there is no such child, return null;
	 */
	FPNode_Strings getChildWithID(String id) {
		// for each child node
		for(FPNode_Strings child : childs){
			// if the id is the one that we are looking for
			if(child.itemID.equals(id)){
				// return that node
				return child;
			}
		}
		// if not found, return null
		return null;
	}

}
